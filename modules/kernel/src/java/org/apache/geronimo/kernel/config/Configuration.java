/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.kernel.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * A Configuration represents a collection of runnable services that can be
 * loaded into a Geronimo Kernel and brought online. The primary components in
 * a Configuration are a codebase, represented by a collection of URLs that
 * is used to locate classes, and a collection of GBean instances that define
 * its state.
 * <p/>
 * The persistent attributes of the Configuration are:
 * <ul>
 * <li>its unique configId used to identify this specific config</li>
 * <li>the configId of a parent Configuration on which this one is dependent</li>
 * <li>a List<URI> of code locations (which may be absolute or relative to a baseURL)</li>
 * <li>a byte[] holding the state of the GBeans instances in Serialized form</li>
 * </ul>
 * When a configuration is started, it converts the URIs into a set of absolute
 * URLs by resolving them against the specified baseURL (this would typically
 * be the root of the CAR file which contains the configuration) and then
 * constructs a ClassLoader for that codebase. That ClassLoader is then used
 * to de-serialize the persisted GBeans, ensuring the GBeans can be recycled
 * as necessary. Once the GBeans have been restored, they are brought online
 * by registering them with the MBeanServer.
 * <p/>
 * A dependency on the Configuration is created for every GBean it loads. As a
 * result, a startRecursive() operation on the configuration will result in
 * a startRecursive() for all the GBeans it contains. Similarly, if the
 * Configuration is stopped then all of its GBeans will be stopped as well.
 *
 * @version $Rev$ $Date$
 */
public class Configuration implements GBeanLifecycle, ConfigurationParent {
    private static final Log log = LogFactory.getLog(Configuration.class);

    public static ObjectName getConfigurationObjectName(URI configId) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toString()));
    }
    public static ObjectName getConfigurationObjectName(Artifact configId) throws InvalidConfigException {
        try {
            return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toURI().toString()));
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Could not construct object name for configuration", e);
        } catch (URISyntaxException e) {
            throw new InvalidConfigException("Could not construct uri for artifact for configuration", e);
        }
    }

    public static boolean isConfigurationObjectName(ObjectName name) {
        return name.getDomain().equals("geronimo.config") && name.getKeyPropertyList().size() == 1 && name.getKeyProperty("name") != null;
    }

    public static URI getConfigurationID(ObjectName objectName) throws URISyntaxException {
        if (isConfigurationObjectName(objectName)) {
            String name = ObjectName.unquote(objectName.getKeyProperty("name"));
            URI uri = new URI(name);
            return uri;
        } else {
            throw new IllegalArgumentException("ObjectName " + objectName + " is not a Configuration name");
        }
    }

    /**
     * The kernel in which this configuration is registered
     */
    private final Kernel kernel;

    /**
     * The registered objectName for this configuraion
     */
    private final String objectNameString;
    private final ObjectName objectName;

    private final Environment environment;

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;

    /**
     * List of the parent configurations
     */
    private final List parents;

    private final List classPath;

    /**
     * The names of all GBeans contained in this configuration.
     */
    private Set objectNames;

    /**
     * The classloader used to load the child GBeans contained in this configuration.
     */
    private MultiParentClassLoader configurationClassLoader;

    /**
     * The GBeanData for the GBeans contained in this configuration.  These must be persisted as a ByteArray, becuase
     * the data can only be deserialized in the configurationClassLoader, which is not available until this Configuration
     * is deserialized and started.
     */
    private byte[] gbeanState;

    /**
     * Base path used to resolve relative class path entries.
     */
    private final URL baseURL;

    /**
     * The repositories used dependencies.
     */
    private final Collection repositories;

    /**
     * Only used to allow declaration as a reference.
     */
    public Configuration() {
        environment = null;
        kernel = null;
        objectNameString = null;
        objectName = null;
        moduleType = null;
        parents = null;
        objectNames = null;
        configurationClassLoader = null;
        classPath = null;
        baseURL = null;
        repositories = null;
    }

    /**
     * Constructor that can be used to create an offline Configuration, typically
     * only used publically during the deployment process for initial configuration.
     *
     * @param id           the unique id of this Configuration
     * @param moduleType   the module type identifier
     * @param nameKeys
     * @param classPath    a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState   a byte array contain the Java Serialized form of the GBeans in this Configuration
     * @param repositories a Collection<Repository> of repositories used to resolve dependencies
     * @param dependencies a List<URI> of dependencies
     */
    public Configuration(Kernel kernel,
                         String objectName,
                         Artifact id,
                         ConfigurationModuleType moduleType,
                         URL baseURL,
                         Artifact[] parentIds,
                         Map nameKeys,
                         List classPath,
                         boolean inverseClassLoading,
                         String[] hiddenClasses,
                         String[] nonOverridableClasses,
                         byte[] gbeanState,
                         Collection repositories,
                         List dependencies) throws Exception {
        this(kernel,
                objectName,
                moduleType,
                createEnvironment(id, nameKeys, parentIds, inverseClassLoading, hiddenClasses, nonOverridableClasses, dependencies),
                baseURL,
                classPath,
                gbeanState,
                repositories);
    }

    /**
     * Constructor that can be used to create an offline Configuration, typically
     * only used publically during the deployment process for initial configuration.
     *
     * @param moduleType   the module type identifier
     * @param environment
     * @param classPath    a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState   a byte array contain the Java Serialized form of the GBeans in this Configuration
     * @param repositories a Collection<Repository> of repositories used to resolve dependencies
     */
    public Configuration(Kernel kernel,
            String objectName,
            ConfigurationModuleType moduleType,
            Environment environment,
            URL baseURL,
            List classPath,
            byte[] gbeanState,
            Collection repositories) throws Exception {

        this.kernel = kernel;
        this.environment = environment;
        this.objectNameString = objectName;
        this.objectName = JMXUtil.getObjectName(objectName);
        this.moduleType = moduleType;
        this.baseURL = baseURL;
        this.gbeanState = gbeanState;
        this.repositories = repositories;
        if (classPath != null) {
            this.classPath = classPath;
        } else {
            this.classPath = Collections.EMPTY_LIST;
        }

        // get proxies to my parent configurations
        parents = new ArrayList();
        LinkedHashSet dependencies = environment.getDependencies();
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (artifact.getType().equals("car")) {
                ObjectName parentName = getConfigurationObjectName(artifact);
                parents.add(kernel.getProxyManager().createProxy(parentName, Configuration.class));
            }
        }

        // declarae dependencies on parents
        Set parentNames = new HashSet();
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            ObjectName parentName = getConfigurationObjectName(configuration.getId());
            parentNames.add(parentName);
        }
        DependencyManager dependencyManager = this.kernel.getDependencyManager();
        dependencyManager.addDependencies(this.objectName, parentNames);

        // build configurationClassLoader
        URL[] urls = buildClassPath();
        log.debug("ClassPath for " + environment.getConfigId() + " resolved to " + Arrays.asList(urls));

        if (parents.size() == 0) {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            configurationClassLoader = new MultiParentClassLoader(environment.getConfigId(), urls, getClass().getClassLoader());
        } else {
            configurationClassLoader = new MultiParentClassLoader(environment.getConfigId(), urls, getParentClassLoaders());
        }
    }

    private static Environment createEnvironment(Artifact id, Map nameKeys, Artifact[] parentIds, boolean inverseClassLoading, String[] hiddenClasses, String[] nonOverridableClasses, List dependencies) {
        Environment environment = new Environment();
        environment.setConfigId(id);
        if (nameKeys != null) {
            environment.setProperties(nameKeys);
        }
        environment.setInverseClassLoading(inverseClassLoading);
        if (hiddenClasses != null) {
            environment.setHiddenClasses(Arrays.asList(hiddenClasses));
        }

        if (nonOverridableClasses != null) {
            environment.setNonOverrideableClasses(Arrays.asList(nonOverridableClasses));
        }

        List deps = new ArrayList();
        if (parentIds != null) {
            deps.addAll(Arrays.asList(parentIds));
        }
        if (dependencies != null) {
            deps.addAll(dependencies);
        }
        environment.setDependencies(deps);

        return environment;
    }

    public String getObjectName() {
        return objectNameString;
    }

    public void loadGBeans(ManageableAttributeStore attributeStore) throws InvalidConfigException, GBeanAlreadyExistsException {
        // DSS: why exactally are we doing this?  I bet there is a reason, but
        // we should state why here.
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);

            // create and initialize GBeans
            Collection gbeans = loadGBeans();
            if (attributeStore != null) {
                try {
                    gbeans = attributeStore.setAttributes(environment.getConfigId().toURI(), gbeans, configurationClassLoader);
                } catch (URISyntaxException e) {
                    throw new InvalidConfigException(e);
                }
            }

            // register all the GBeans
            Set objectNames = new HashSet();
            for (Iterator i = gbeans.iterator(); i.hasNext();) {
                GBeanData gbeanData = (GBeanData) i.next();
                // massage the GBeanData and add the GBean to the kernel
                loadGBean(gbeanData, objectNames);
            }
            this.objectNames = objectNames;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void startRecursiveGBeans() throws GBeanNotFoundException {
        for (Iterator iterator = objectNames.iterator(); iterator.hasNext();) {
            ObjectName gbeanName = (ObjectName) iterator.next();
            if (kernel.isGBeanEnabled(gbeanName)) {
                kernel.startRecursiveGBean(gbeanName);
            }
        }
    }

    public void stopGBeans() throws GBeanNotFoundException {
        for (Iterator iterator = objectNames.iterator(); iterator.hasNext();) {
            ObjectName gbeanName = (ObjectName) iterator.next();
            kernel.stopGBean(gbeanName);
        }
    }

    public void unloadGBeans() throws GBeanNotFoundException {
        for (Iterator iterator = objectNames.iterator(); iterator.hasNext();) {
            ObjectName gbeanName = (ObjectName) iterator.next();
            kernel.getDependencyManager().removeDependency(gbeanName, objectName);
            kernel.unloadGBean(gbeanName);
        }
    }

    private ClassLoader[] getParentClassLoaders() throws Exception {
        ClassLoader[] classLoaders = new ClassLoader[parents.size()];
        for (ListIterator iterator = parents.listIterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            classLoaders[iterator.previousIndex()] = configuration.getConfigurationClassLoader();
        }
        return classLoaders;
    }

    private URL[] buildClassPath() throws MalformedURLException, MissingDependencyException {
        LinkedHashSet dependencies = environment.getDependencies();
        List urls = new ArrayList(dependencies.size() + classPath.size());
        for (Iterator i = dependencies.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
            if (artifact.getType().equals("car")) {
                File file = null;
                for (Iterator j = repositories.iterator(); j.hasNext();) {
                    Repository repository = (Repository) j.next();
                    if (repository.contains(artifact)) {
                        file = repository.getLocation(artifact);
                        break;
                    }
                }
                if (file == null) {
                    throw new MissingDependencyException("Unable to resolve dependency " + artifact);
                }
                urls.add(file.toURL());
            }
        }
        for (Iterator i = classPath.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            urls.add(new URL(baseURL, uri.toString()));
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private static void setGBeanBaseUrl(GBeanData gbeanData, URL baseUrl) {
        GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
        Set attributes = gbeanInfo.getAttributes();
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            GAttributeInfo attribute = (GAttributeInfo) iterator.next();
            if (attribute.getName().equals("configurationBaseUrl") && attribute.getType().equals("java.net.URL")) {
                gbeanData.setAttribute("configurationBaseUrl", baseUrl);
                return;
            }
        }
    }

    public void doStart() throws Exception {
        log.debug("Started configuration " + environment.getConfigId());
    }

    public void doFail() {
        shutdown();
    }

    public synchronized void doStop() throws Exception {
        log.debug("Stopping configuration " + environment.getConfigId());
        // shutdown the configuration and unload all beans
        shutdown();

    }

    private void shutdown() {
        // unregister all GBeans
        if (objectNames != null) {
            for (Iterator i = objectNames.iterator(); i.hasNext();) {
                ObjectName name = (ObjectName) i.next();
                kernel.getDependencyManager().removeDependency(name, objectName);
                try {
                    log.trace("Unregistering GBean " + name);
                    if (kernel.isLoaded(name)) {
                        kernel.unloadGBean(name);
                    }
                } catch (Exception e) {
                    // ignore
                    log.warn("Could not unregister child " + name, e);
                }
            }
        }

        // destroy the class loader
        if (configurationClassLoader != null) {
            configurationClassLoader.destroy();
            configurationClassLoader = null;
        }
    }

    /**
     * Gets the parent configurations of this configuration.
     * @return the parents of this configuration
     */
    public List getParents() {
        return parents;
    }

    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Return the unique Id
     *
     * @return the unique Id
     */
    public Artifact getId() {
        return environment.getConfigId();
    }

    /**
     * Gets the type of the configuration (WAR, RAR et cetera)
     *
     * @return Type of the configuration.
     */
    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public byte[] getGBeanState() {
        return gbeanState;
    }

    public ClassLoader getConfigurationClassLoader() {
        return configurationClassLoader;
    }

    public synchronized boolean containsGBean(ObjectName gbean) {
        return objectNames.contains(gbean);
    }

    public synchronized void addGBean(GBeanData beanData, boolean start) throws InvalidConfigException, GBeanAlreadyExistsException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);
            ObjectName name = loadGBean(beanData, objectNames);
            if (start) {
                try {
                    kernel.startRecursiveGBean(name);
                } catch (GBeanNotFoundException e) {
                    throw new IllegalStateException("How could we not find a GBean that we just loaded ('" + name + "')?");
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public synchronized void removeGBean(ObjectName name) throws GBeanNotFoundException {
        if (!objectNames.contains(name)) {
            throw new GBeanNotFoundException(name);
        }
        kernel.getDependencyManager().removeDependency(name, this.objectName);
        try {
            if (kernel.getGBeanState(name) == State.RUNNING_INDEX) {
                kernel.stopGBean(name);
            }
            kernel.unloadGBean(name);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }
        objectNames.remove(name);
    }

    private ObjectName loadGBean(GBeanData beanData, Set objectNames) throws GBeanAlreadyExistsException {
        ObjectName name = beanData.getName();
        setGBeanBaseUrl(beanData, baseURL);

        log.trace("Registering GBean " + name);
        kernel.loadGBean(beanData, configurationClassLoader);
        objectNames.add(name);
        // todo change this to a dependency on the gbeanData itself as soon as we add that feature
        kernel.getDependencyManager().addDependency(name, this.objectName);
        return name;
    }


    /**
     * Load GBeans from the supplied byte array using the supplied ClassLoader
     *
     * @return a Map<ObjectName, GBeanMBean> of GBeans loaded from the persisted state
     * @throws InvalidConfigException if there is a problem deserializing the state
     */
    public Collection loadGBeans() throws InvalidConfigException {
        Map gbeans = new HashMap();
        try {
            ObjectInputStream ois = new ObjectInputStreamExt(new ByteArrayInputStream(gbeanState), configurationClassLoader);
            try {
                while (true) {
                    GBeanData gbeanData = new GBeanData();
                    gbeanData.readExternal(ois);

                    gbeans.put(gbeanData.getName(), gbeanData);
                }
            } catch (EOFException e) {
                // ok
            } finally {
                ois.close();
            }
            // avoid duplicate object names
            return gbeans.values();
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to deserialize GBeanState", e);
        }
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     *
     * @return the persisted GBeans
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *          if there is a problem serializing the state
     */
    public synchronized GBeanData[] storeCurrentGBeans() throws InvalidConfigException {
        // get the gbean data for all gbeans
        GBeanData[] gbeans = new GBeanData[objectNames.size()];
        Iterator iterator = objectNames.iterator();
        for (int i = 0; i < gbeans.length; i++) {
            ObjectName objectName = (ObjectName) iterator.next();
            try {
                gbeans[i] = kernel.getGBeanData(objectName);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to serialize GBeanData for " + objectName, e);
            }
        }

        // save state
        try {
            gbeanState = storeGBeans(gbeans);
        } catch (InvalidConfigException e) {
            log.warn("Unable to update persistent state during shutdown", e);
        }
        return gbeans;
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     *
     * @param gbeans the gbean data to persist
     * @return the persisted GBeans
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *          if there is a problem serializing the state
     */
    public static byte[] storeGBeans(GBeanData[] gbeans) throws InvalidConfigException {
        return storeGBeans(Arrays.asList(gbeans));
    }

    public static byte[] storeGBeans(List gbeans) throws InvalidConfigException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("Unable to initialize ObjectOutputStream").initCause(e);
        }
        for (Iterator iterator = gbeans.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            try {
                gbeanData.writeExternal(oos);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to serialize GBeanData for " + gbeanData.getName(), e);
            }
        }
        try {
            oos.flush();
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("Unable to flush ObjectOutputStream").initCause(e);
        }
        return baos.toByteArray();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Configuration.class);//does not use jsr-77 naming
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("id", Artifact.class, true, false);
        infoFactory.addAttribute("type", ConfigurationModuleType.class, true, false);
        infoFactory.addAttribute("parentId", Artifact[].class, true, false);
        infoFactory.addAttribute("nameKeys", Map.class, true, false);
        infoFactory.addAttribute("classPath", List.class, true, false);
        infoFactory.addAttribute("inverseClassLoading", boolean.class, true, false);
        infoFactory.addAttribute("hiddenClasses", String[].class, true, false);
        infoFactory.addAttribute("nonOverridableClasses", String[].class, true, false);
        infoFactory.addAttribute("dependencies", List.class, true, false);
        infoFactory.addAttribute("gBeanState", byte[].class, true, false);
        infoFactory.addAttribute("baseURL", URL.class, true, false);
        infoFactory.addAttribute("configurationClassLoader", ClassLoader.class, false);

        infoFactory.addReference("Repositories", Repository.class, "GBean");

        infoFactory.addOperation("addGBean", new Class[]{GBeanData.class, boolean.class});
        infoFactory.addOperation("removeGBean", new Class[]{ObjectName.class});
        infoFactory.addOperation("containsGBean", new Class[]{ObjectName.class});
        infoFactory.addOperation("loadGBeans", new Class[]{});
        infoFactory.addOperation("loadGBeans", new Class[]{ManageableAttributeStore.class});
        infoFactory.addOperation("startRecursiveGBeans");
        infoFactory.addOperation("stopGBeans");
        infoFactory.addOperation("unloadGBeans");

        infoFactory.setConstructor(new String[]{
            "kernel",
            "objectName",
            "id",
            "type",
            "baseURL",
            "parentId",
            "nameKeys",
            "classPath",
            "inverseClassLoading",
            "hiddenClasses",
            "nonOverridableClasses",
            "gBeanState",
            "Repositories",
            "dependencies",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
