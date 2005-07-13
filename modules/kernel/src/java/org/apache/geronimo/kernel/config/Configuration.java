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
import java.net.MalformedURLException;
import java.net.URI;
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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;

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
public class Configuration implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(Configuration.class);

    public static ObjectName getConfigurationObjectName(URI configId) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configId.toString()));
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

    /**
     * URI used to referr to this configuration in the configuration manager
     */
    private final URI id;

    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;

    /**
     * The uri of the parent of this configuration.  May be null.
     */
    private final URI parentId;

    /**
     * The configuration store from which this configuration was loaded.  May be null if this configuration
     * was not loaded from a store.  The store is notified when the configurations stopps so it can update
     * the saved state of the configration.
     */
    private final ConfigurationStore configurationStore;

    private final List dependencies;
    private final List classPath;
    private final String domain;
    private final String server;

    /**
     * The names of all GBeans contained in this configuration.
     */
    private Set objectNames;

    /**
     * The classloadeder used to load the child GBeans contained in this configuration.
     */
    private ConfigurationClassLoader configurationClassLoader;

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
     * Parent of this configuration
     */
    private final ConfigurationParent parent;

    /**
     * The repositories used dependencies.
     */
    private final Collection repositories;

    /**
     * Only used to allow declaration as a reference.
     */
    public Configuration() {
        kernel = null;
        objectNameString = null;
        objectName = null;
        id = null;
        moduleType = null;
        parentId = null;
        configurationStore = null;
        domain = null;
        server = null;
        objectNames = null;
        configurationClassLoader = null;
        dependencies = null;
        classPath = null;
        baseURL = null;
        parent = null;
        repositories = null;
    }

    /**
     * Constructor that can be used to create an offline Configuration, typically
     * only used publically during the deployment process for initial configuration.
     *
     * @param id           the unique id of this Configuration
     * @param moduleType   the module type identifier
     * @param parent       the parent Configuration; may be null
     * @param classPath    a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState   a byte array contain the Java Serialized form of the GBeans in this Configuration
     * @param repositories a Collection<Repository> of repositories used to resolve dependencies
     * @param dependencies a List<URI> of dependencies
     */
    public Configuration(Kernel kernel,
            String objectName,
            URI id,
            ConfigurationModuleType moduleType,
            URL baseURL,
            URI parentId,
            ConfigurationParent parent,
            String domain,
            String server,
            List classPath,
            byte[] gbeanState,
            Collection repositories,
            List dependencies,
            ConfigurationStore configurationStore) throws Exception {

        this.kernel = kernel;
        this.objectNameString = objectName;
        this.objectName = JMXUtil.getObjectName(objectName);
        this.id = id;
        this.moduleType = moduleType;
        this.baseURL = baseURL;
        this.parentId = parentId;
        this.parent = parent;
        this.gbeanState = gbeanState;
        this.repositories = repositories;
        if (classPath != null) {
            this.classPath = classPath;
        } else {
            this.classPath = Collections.EMPTY_LIST;
        }
        if (dependencies != null) {
            this.dependencies = dependencies;
        } else {
            this.dependencies = Collections.EMPTY_LIST;
        }

        this.configurationStore = configurationStore;

        this.domain = domain;
        this.server = server;
    }

    public String getObjectName() {
        return objectNameString;
    }

    public String getDomain() {
        return domain;
    }

    public String getServer() {
        return server;
    }

    public void doStart() throws Exception {
        // build configurationClassLoader
        URL[] urls = resolveClassPath(classPath, baseURL, dependencies, repositories);
        log.debug("ClassPath for " + id + " resolved to " + Arrays.asList(urls));

        if (parent == null) {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            configurationClassLoader = new ConfigurationClassLoader(id, urls, getClass().getClassLoader());
        } else {
            configurationClassLoader = new ConfigurationClassLoader(id, urls, parent.getConfigurationClassLoader());
        }

        // DSS: why exactally are we doing this?  I bet there is a reason, but
        // we should state why here.
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(configurationClassLoader);

            // create and initialize GBeans
            Collection gbeans = loadGBeans(gbeanState, configurationClassLoader);

            // set configurationBaseUrl attribute on each gbean
            for (Iterator i = gbeans.iterator(); i.hasNext();) {
                GBeanData gbeanData = (GBeanData) i.next();
                setGBeanBaseUrl(gbeanData, baseURL);
            }

            // register all the GBeans
            Set objectNames = new HashSet();
            for (Iterator i = gbeans.iterator(); i.hasNext();) {
                GBeanData gbeanData = (GBeanData) i.next();
                ObjectName name = gbeanData.getName();
                log.trace("Registering GBean " + name);
                kernel.loadGBean(gbeanData, configurationClassLoader);
                objectNames.add(name);
                // todo change this to a dependency on the gbeanData itself as soon as we add that feature
                kernel.getDependencyManager().addDependency(name, this.objectName);
            }
            this.objectNames = objectNames;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        log.info("Started configuration " + id);
    }

    private static URL[] resolveClassPath(List classPath, URL baseURL, List dependencies, Collection repositories) throws MalformedURLException, MissingDependencyException {
        if (classPath == null) {
            classPath = Collections.EMPTY_LIST;
        }
        if (dependencies == null) {
            dependencies = Collections.EMPTY_LIST;
        }

        URL[] urls = new URL[dependencies.size() + classPath.size()];
        int idx = 0;
        for (Iterator i = dependencies.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            URL url = null;
            for (Iterator j = repositories.iterator(); j.hasNext();) {
                Repository repository = (Repository) j.next();
                if (repository.hasURI(uri)) {
                    url = repository.getURL(uri);
                    break;
                }
            }
            if (url == null) {
                throw new MissingDependencyException("Unable to resolve dependency " + uri);
            }
            urls[idx++] = url;
        }
        for (Iterator i = classPath.iterator(); i.hasNext();) {
            URI uri = (URI) i.next();
            urls[idx++] = new URL(baseURL, uri.toString());
        }
        assert idx == urls.length;
        return urls;
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

    public void doStop() throws Exception {
        log.info("Stopping configuration " + id);

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
            log.info("Unable to update persistent state during shutdown", e);
        }

        // shutdown the configuration and unload all beans
        shutdown();

        // update the configuation store
        if (configurationStore != null) {
            ConfigurationData configurationData = new ConfigurationData();
            configurationData.setId(id);
            configurationData.setModuleType(moduleType);
            configurationData.setDomain(domain);
            configurationData.setServer(server);
            configurationData.setParentId(parentId);
            configurationData.setGBeans(Arrays.asList(gbeans));
            configurationData.setDependencies(dependencies);
            configurationData.setClassPath(classPath);
            configurationStore.updateConfiguration(configurationData);
        }
    }

    private void shutdown() {
        // unregister all GBeans
        if (objectNames != null) {
            for (Iterator i = objectNames.iterator(); i.hasNext();) {
                ObjectName name = (ObjectName) i.next();
                kernel.getDependencyManager().removeDependency(name, objectName);
                try {
                    log.trace("Unregistering GBean " + name);
                    kernel.unloadGBean(name);
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

    public void doFail() {
        shutdown();
    }

    /**
     * Return the unique id of this Configuration's parent
     *
     * @return the unique id of the parent, or null if it does not have one
     */
    public URI getParentId() {
        return parentId;
    }

    /**
     * Return the unique Id
     *
     * @return the unique Id
     */
    public URI getId() {
        return id;
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

    /**
     * Load GBeans from the supplied byte array using the supplied ClassLoader
     *
     * @param gbeanState the serialized form of the GBeans
     * @param cl         the ClassLoader used to locate classes needed during deserialization
     * @return a Map<ObjectName, GBeanMBean> of GBeans loaded from the persisted state
     * @throws InvalidConfigException if there is a problem deserializing the state
     */
    private static Collection loadGBeans(byte[] gbeanState, ClassLoader cl) throws InvalidConfigException {
        Map gbeans = new HashMap();
        try {
            ObjectInputStream ois = new ObjectInputStreamExt(new ByteArrayInputStream(gbeanState), cl);
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
     * @param gbeans the gbean data to persist
     * @return the persisted GBeans
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a problem serializing the state
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Configuration.class);//does not use jsr-77 naming
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("id", URI.class, true);
        infoFactory.addAttribute("type", ConfigurationModuleType.class, true);
        infoFactory.addAttribute("parentId", URI.class, true);
        infoFactory.addAttribute("domain", String.class, true);
        infoFactory.addAttribute("server", String.class, true);
        infoFactory.addAttribute("classPath", List.class, true);
        infoFactory.addAttribute("dependencies", List.class, true);
        infoFactory.addAttribute("gBeanState", byte[].class, true);
        infoFactory.addAttribute("baseURL", URL.class, true);
        infoFactory.addAttribute("configurationClassLoader", ClassLoader.class, false);

        infoFactory.addReference("Parent", ConfigurationParent.class);
        infoFactory.addReference("Repositories", Repository.class, "GBean");
        infoFactory.addReference("ConfigurationStore", ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{
            "kernel",
            "objectName",
            "id",
            "type",
            "baseURL",
            "parentId",
            "Parent",
            "domain",
            "server",
            "classPath",
            "gBeanState",
            "Repositories",
            "dependencies",
            "ConfigurationStore"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
