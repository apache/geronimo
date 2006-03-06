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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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

    /**
     * @deprecated Use artifact version of this method
     */
    public static ObjectName getConfigurationObjectName(URI configId) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toString()));
    }

    public static ObjectName getConfigurationObjectName(Artifact configId) throws InvalidConfigException {
        try {
            return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toString()));
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Could not construct object name for configuration", e);
        }
    }

    public static boolean isConfigurationObjectName(ObjectName name) {
        return name.getDomain().equals("geronimo.config") && name.getKeyPropertyList().size() == 1 && name.getKeyProperty("name") != null;
    }

    public static Artifact getConfigurationID(ObjectName objectName) {
        if (isConfigurationObjectName(objectName)) {
            String name = ObjectName.unquote(objectName.getKeyProperty("name"));
            return Artifact.create(name);
        } else {
            throw new IllegalArgumentException("ObjectName " + objectName + " is not a Configuration name");
        }
    }

    /**
     * The kernel in which this configuration is registered.
     */
    private final Kernel kernel;

    private final ConfigurationStore configurationStore;

    /**
     * The artifact id for this configuration.
     */
    private final Artifact id;

    /**
     * The registered objectName for this configuraion.
     */
    private final ObjectName objectName;

    /**
     * Defines the environment requred for this configuration.
     */
    private final Environment environment;

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;

    /**
     * List of the parent configurations
     */
    private final List parents;

    /**
     * The GBeanData objects by ObjectName
     */
    private final Map gbeans = new HashMap();

    /**
     * The classloader used to load the child GBeans contained in this configuration.
     */
    private final MultiParentClassLoader configurationClassLoader;

    /**
     * The repositories used dependencies.
     */
    private final Collection repositories;

    /**
     * The artifact manager in which loaded artifacts are registered.
     */
    private final ArtifactManager artifactManager;

    /**
     * Only used to allow declaration as a reference.
     */
    public Configuration() {
        environment = null;
        kernel = null;
        configurationStore = null;
        id = null;
        objectName = null;
        moduleType = null;
        parents = null;
        configurationClassLoader = null;
        repositories = null;
        artifactManager = null;
    }

    /**
     * Creates a configuration.
     * @param parents parents of this configuation (not ordered)
     * @param moduleType   the module type identifier
     * @param environment
     * @param classPath    a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState   a byte array contain the Java Serialized form of the GBeans in this Configuration
     * @param repositories a Collection<Repository> of repositories used to resolve dependencies
     */
    public Configuration(Collection parents, Kernel kernel, String objectName, ConfigurationModuleType moduleType, Environment environment, List classPath, byte[] gbeanState, Collection repositories, ConfigurationStore configurationStore, ArtifactManager artifactManager, ArtifactResolver artifactResolver) throws MissingDependencyException, MalformedURLException, NoSuchConfigException, InvalidConfigException {
        if (parents == null) parents = Collections.EMPTY_SET;
        this.parents = orderParents(parents, environment);
        this.kernel = kernel;
        this.environment = environment;
        this.moduleType = moduleType;
        this.repositories = repositories;

        this.id = environment.getConfigId();
        this.objectName = objectName == null ? null : JMXUtil.getObjectName(objectName);
        if (objectName.equals(getConfigurationObjectName(id))) {
            throw new IllegalArgumentException("Expected objectName " +
                    "<" + getConfigurationObjectName(id).getCanonicalName() + ">" +
                    ", but actual objectName is " +
                    "<" + this.objectName.getCanonicalName() + ">");
        }

        this.configurationStore = configurationStore;

        this.artifactManager = artifactManager;

        if (artifactResolver == null) {
            artifactResolver = new DefaultArtifactResolver(artifactManager, repositories);
        }

        //propagate non overridable classes etc from parents.
        determineInherited();

        // resolve dependencies
        LinkedHashSet dependencies = environment.getDependencies();
        dependencies = recursiveResolve(artifactResolver, dependencies);
        environment.setDependencies(dependencies);

        // resolve references
        LinkedHashSet references = environment.getReferences();
        references = artifactResolver.resolve(parents, references);
        environment.setReferences(references);

        // build configurationClassLoader
        URL[] urls = buildClassPath(configurationStore, classPath);
        log.debug("ClassPath for " + id + " resolved to " + Arrays.asList(urls));
        if (parents.size() == 0) {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            configurationClassLoader = new MultiParentClassLoader(environment.getConfigId(), urls, getClass().getClassLoader());
        } else {
            ClassLoader[] parentClassLoaders = new ClassLoader[parents.size()];
            for (ListIterator iterator = this.parents.listIterator(); iterator.hasNext();) {
                Configuration configuration = (Configuration) iterator.next();
                parentClassLoaders[iterator.previousIndex()] = configuration.getConfigurationClassLoader();
            }
            configurationClassLoader = new MultiParentClassLoader(environment.getConfigId(), urls, parentClassLoaders);
        }

        //
        // Deserialize the GBeans
        //
        if (gbeanState != null && gbeanState.length > 0) {
            // Set the thread context classloader so deserializing classes can grab the cl from the thread
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(configurationClassLoader);

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
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to deserialize GBeanState", e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }

    private List orderParents(Collection parents, Environment environment) {
        Map parentsById = new HashMap();
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            Artifact id = configuration.getId();
            parentsById.put(id, configuration);
        }
        LinkedHashSet imports = environment.getImports();
        if (!parentsById.keySet().equals(imports)) {
            throw new IllegalArgumentException(environment.getConfigId() + " : Expected parents " +
                    imports +
                    ", but actual parents are " +
                    parentsById.keySet());
        }

        List orderedParents = new ArrayList(parents.size());
        for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
            Artifact id = (Artifact) iterator.next();
            Configuration configuration = (Configuration) parentsById.get(id);
            if (configuration == null) throw new IllegalStateException("Could not find parent " + id + " in the parents collection");
            orderedParents.add(configuration);
        }
        return orderedParents;
    }

    private void determineInherited() {
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration parent = (Configuration) iterator.next();

            Environment parentEnvironment = parent.getEnvironment();
            Set nonOverridableClasses = parentEnvironment.getNonOverrideableClasses();
            environment.addNonOverrideableClasses(nonOverridableClasses);
        }
    }

    private LinkedHashSet recursiveResolve(ArtifactResolver artifactResolver, LinkedHashSet dependencies) throws MissingDependencyException {
        dependencies = artifactResolver.resolve(parents, dependencies);
        for (Iterator iterator = new ArrayList(dependencies).iterator(); iterator.hasNext();) {
            Artifact dependency = (Artifact) iterator.next();
            for (Iterator iterator1 = repositories.iterator(); iterator1.hasNext();) {
                Repository repository = (Repository) iterator1.next();
                if (repository.contains(dependency)) {
                    LinkedHashSet subDependencies = repository.getDependencies(dependency);
                    subDependencies = recursiveResolve(artifactResolver, subDependencies);
                    dependencies.addAll(subDependencies);
                }
            }
        }
        return dependencies;
    }

    private URL[] buildClassPath(ConfigurationStore configurationStore, List classPath) throws MalformedURLException, MissingDependencyException, NoSuchConfigException {
        LinkedHashSet dependencies = environment.getDependencies();
        List urls = new ArrayList();
        for (Iterator i = dependencies.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
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
        if (classPath != null) {
            for (Iterator i = classPath.iterator(); i.hasNext();) {
                URI uri = (URI) i.next();
                urls.add(configurationStore.resolve(id, uri));
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    /**
     * Return the unique Id
     * @return the unique Id
     */
    public Artifact getId() {
        return id;
    }

    /**
     * Gets the unique name of this configuration within the kernel.
     * @return the unique name of this configuration
     */
    public String getObjectName() {
        return objectName.getCanonicalName();
    }

    /**
     * Gets the parent configurations of this configuration.
     * @return the parents of this configuration
     */
    public List getParents() {
        return parents;
    }

    /**
     * Gets the declaration of the environment in which this configuration runs.
     * @return the environment of this configuration
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Gets the type of the configuration (WAR, RAR et cetera)
     * @return Type of the configuration.
     */
    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    /**
     * Gets the class loader for this configuration.
     * @return the class loader for this configuration
     */
    public ClassLoader getConfigurationClassLoader() {
        return configurationClassLoader;
    }

    public ConfigurationStore getConfigurationStore() {
        return configurationStore;
    }

    /**
     * Gets an unmodifiable map of the GBeanDatas for the GBeans in this configuration by ObjectName.
     * @return the GBeans in this configuration
     */
    public Map getGBeans() {
        return Collections.unmodifiableMap(gbeans);
    }

    /**
     * Determines of this configuration constains the specified GBean.
     * @param gbean the name of the GBean
     * @return true if this configuration contains the specified GBean; false otherwise
     */
    public synchronized boolean containsGBean(ObjectName gbean) {
        return gbeans.containsKey(gbean);
    }

    public synchronized void addGBean(GBeanData beanData, boolean start) throws InvalidConfigException, GBeanAlreadyExistsException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);

            log.trace("Registering GBean " + beanData.getName());

            // add a dependency on this configuration
            beanData.getDependencies().add(objectName);

            // register the bean with the kernel
            kernel.loadGBean(beanData, configurationClassLoader);

            // start the configuration
            if (start) {
                try {
                    kernel.startRecursiveGBean(beanData.getName());
                } catch (GBeanNotFoundException e) {
                    throw new IllegalStateException("How could we not find a GBean that we just loaded ('" + beanData.getName() + "')?");
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        gbeans.put(beanData.getName(), beanData);
    }

    public synchronized void removeGBean(ObjectName name) throws GBeanNotFoundException {
        if (!gbeans.containsKey(name)) {
            throw new GBeanNotFoundException(name);
        }
        try {
            if (kernel.getGBeanState(name) == State.RUNNING_INDEX) {
                kernel.stopGBean(name);
            }
            kernel.unloadGBean(name);
        } catch (GBeanNotFoundException e) {
            // Bean is no longer loaded
        }

        gbeans.remove(name);
    }

    public void doStart() throws Exception {
        assert objectName != null;

        // declare dependencies on parents
        Set parentNames = new HashSet();
        for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();
            ObjectName parentName = getConfigurationObjectName(configuration.getId());
            parentNames.add(parentName);
        }
        DependencyManager dependencyManager = this.kernel.getDependencyManager();
        dependencyManager.addDependencies(this.objectName, parentNames);

        // declare the artifacts as loaded
        LinkedHashSet artifacts = new LinkedHashSet();
        artifacts.addAll(environment.getDependencies());
        artifacts.addAll(environment.getReferences());
        if (artifactManager != null) {
            artifactManager.loadArtifacts(id, artifacts);
        }

        log.debug("Started configuration " + id);
    }

    public synchronized void doStop() throws Exception {
        log.debug("Stopping configuration " + id);
        shutdown();

    }

    public void doFail() {
        log.debug("Failed configuration " + id);
        shutdown();
    }

    private void shutdown() {
        // unregister all GBeans
        for (Iterator i = gbeans.keySet().iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
            try {
                if (kernel.isLoaded(name)) {
                    log.trace("Unregistering GBean " + name);
                    kernel.unloadGBean(name);
                }
            } catch (Exception e) {
                log.warn("Could not unregister child " + name, e);
            }
        }
        gbeans.clear();

        // destroy the class loader
        if (configurationClassLoader != null) {
            configurationClassLoader.destroy();
        }

        // declare all artifacts as unloaded
        if (artifactManager != null) {
            artifactManager.unloadAllArtifacts(id);
        }
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     *
     * @param gbeans the gbean data to persist
     * @return the persisted GBeans
     * @throws InvalidConfigException if there is a problem serializing the state
     */
    public static byte[] storeGBeans(GBeanData[] gbeans) throws InvalidConfigException {
        return storeGBeans(Arrays.asList(gbeans));
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     *
     * @param gbeans the gbean data to persist
     * @return the persisted GBeans
     * @throws InvalidConfigException if there is a problem serializing the state
     */
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
        infoFactory.addAttribute("environment", Environment.class, true, false);
        infoFactory.addAttribute("type", ConfigurationModuleType.class, true, false);
        infoFactory.addAttribute("classPath", List.class, true, false);
        infoFactory.addAttribute("gBeanState", byte[].class, true, false);
        infoFactory.addAttribute("configurationClassLoader", ClassLoader.class, false);
        //make id readable for convenience
        infoFactory.addAttribute("id", Artifact.class, false);
        //NOTE THESE IS NOT REFERENCES
        infoFactory.addAttribute("configurationStore", ConfigurationStore.class, true);
        infoFactory.addAttribute("artifactManager", ArtifactManager.class, true);
        infoFactory.addAttribute("artifactResolver", ArtifactResolver.class, true);

        infoFactory.addReference("Parents", Configuration.class);
        infoFactory.addReference("Repositories", Repository.class, "Repository");

        infoFactory.addInterface(Configuration.class);

        infoFactory.setConstructor(new String[]{
                "Parents",
                "kernel",
                "objectName",
                "type",
                "environment",
                "classPath",
                "gBeanState",
                "Repositories",
                "configurationStore",
                "artifactManager",
                "artifactResolver"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
