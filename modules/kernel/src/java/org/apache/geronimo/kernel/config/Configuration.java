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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.management.AttributeNotFoundException;
import javax.management.JMRuntimeException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
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
 * <li>its unique configID used to identify this specific config</li>
 * <li>the configID of a parent Configuration on which this one is dependent</li>
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

    public static ObjectName getConfigurationObjectName(URI configID) throws MalformedObjectNameException {
        return new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()));
    }

    private final Kernel kernel;
    private final String objectNameString;
    private final ObjectName objectName;
    private final URI id;
    /**
     * Identifies the type of configuration (WAR, RAR et cetera)
     */
    private final ConfigurationModuleType moduleType;
    private final URI parentID;
    private final ConfigurationParent parent;
    private final List classPath;
    private final List dependencies;
    private byte[] gbeanState;
    private final Collection repositories;
    private final ConfigurationStore configurationStore;

    private URL baseURL;
    private Map gbeans;

    private ClassLoader classLoader;

    /**
     * Constructor that can be used to create an offline Configuration, typically
     * only used publically during the deployment process for initial configuration.
     *
     * @param id           the unique ID of this Configuration
     * @param moduleType   the module type identifier
     * @param parent       the parent Configuration; may be null
     * @param classPath    a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState   a byte array contain the Java Serialized form of the GBeans in this Configuration
     * @param repositories a Collection<Repository> of repositories used to resolve dependencies
     * @param dependencies a List<URI> of dependencies
     */
    public Configuration(Kernel kernel, String objectName, URI id, ConfigurationModuleType moduleType, URI parentID, ConfigurationParent parent, List classPath, byte[] gbeanState, Collection repositories, List dependencies, ConfigurationStore configurationStore) {
        this.kernel = kernel;
        this.objectNameString = objectName;
        this.objectName = JMXUtil.getObjectName(objectName);
        this.id = id;
        this.moduleType = moduleType;
        this.parentID = parentID;
        this.parent = parent;
        this.gbeanState = gbeanState;
        if (classPath == null) {
            this.classPath = Collections.EMPTY_LIST;
        } else {
            this.classPath = classPath;
        }
        if (dependencies == null) {
            this.dependencies = Collections.EMPTY_LIST;
        } else {
            this.dependencies = dependencies;
        }
        this.repositories = repositories;
        this.configurationStore = configurationStore;
    }

    public void doStart() throws Exception {
        // build classpath
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
        log.debug("ClassPath for " + id + " resolved to " + Arrays.asList(urls));

        if (parent == null) {
            // no explicit parent set, so use the class loader of this class as
            // the parent... this class should be in the root geronimo classloader,
            // which is normally the system class loader but not always, so be safe
            classLoader = new URLClassLoader(urls, getClass().getClassLoader());
        } else {
            classLoader = new URLClassLoader(urls, parent.getClassLoader());
        }

        // DSS: why exactally are we doing this?  I bet there is a reason, but
        // we should state why here.
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            // create and initialize GBeans
            gbeans = loadGBeans(gbeanState, classLoader);

            // set configurationBaseUrl attribute on each gbean
            for (Iterator i = gbeans.values().iterator(); i.hasNext();) {
                GBeanMBean gbean = (GBeanMBean) i.next();
                setGBeanBaseUrl(gbean, baseURL);
            }

            // register all the GBeans
            for (Iterator i = gbeans.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                ObjectName name = (ObjectName) entry.getKey();
                GBeanMBean gbean = (GBeanMBean) entry.getValue();
                log.trace("Registering GBean " + name);
                try {
                    kernel.loadGBean(name, gbean);
                } catch (JMRuntimeException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else if (cause instanceof Error) {
                        throw (Error) cause;
                    }
                    throw e;
                }
                kernel.getDependencyManager().addDependency(name, objectName);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

        log.info("Started configuration " + id);
    }

    public String getObjectName() {
        return objectNameString;
    }

    private static void setGBeanBaseUrl(GBeanMBean gbean, URL baseUrl) throws ReflectionException, AttributeNotFoundException {
        GBeanInfo gbeanInfo = gbean.getGBeanInfo();
        Set attributes = gbeanInfo.getAttributes();
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            GAttributeInfo attribute = (GAttributeInfo) iterator.next();
            if (attribute.getName().equals("configurationBaseUrl") && attribute.getType().equals("java.net.URL")) {
                gbean.setAttribute("configurationBaseUrl", baseUrl);
                return;
            }
        }
    }

    public void doStop() throws Exception {
        log.info("Stopping configuration " + id);
        if (gbeans == null) {
            return;
        }

        // unregister all GBeans
        for (Iterator i = gbeans.keySet().iterator(); i.hasNext();) {
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

        // save state
        try {
            gbeanState = storeGBeans(gbeans);
        } catch (InvalidConfigException e) {
            log.info(e);
        }

        if (configurationStore != null) {
            configurationStore.updateConfiguration(this);
        }

        gbeans = null;
    }

    public void doFail() {
    }

    /**
     * Return the unique ID of this Configuration's parent
     *
     * @return the unique ID of the parent, or null if it does not have one
     */
    public URI getParentID() {
        return parentID;
    }

    /**
     * Return the unique ID
     *
     * @return the unique ID
     */
    public URI getID() {
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

    /**
     * Return the URL that is used to resolve relative classpath locations
     *
     * @return the base URL for the classpath
     */
    public URL getBaseURL() {
        return baseURL;
    }

    /**
     * Set the URL that should be used to resolve relative class locations
     *
     * @param baseURL the base URL for the classpath
     */
    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public byte[] getGbeanState() {
        return gbeanState;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    private static class ConfigInputStream extends ObjectInputStream {
        private final ClassLoader cl;

        public ConfigInputStream(InputStream in, ClassLoader cl) throws IOException {
            super(in);
            this.cl = cl;
        }

        protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                return cl.loadClass(desc.getName());
            } catch (ClassNotFoundException e) {
                // let the parent try
                return super.resolveClass(desc);
            }
        }
    }

    /**
     * Load GBeans from the supplied byte array using the supplied ClassLoader
     *
     * @param gbeanState the serialized form of the GBeans
     * @param cl         the ClassLoader used to locate classes needed during deserialization
     * @return a Map<ObjectName, GBeanMBean> of GBeans loaded from the persisted state
     * @throws InvalidConfigException if there is a problem deserializing the state
     */
    private static Map loadGBeans(byte[] gbeanState, ClassLoader cl) throws InvalidConfigException {
        Map gbeans = new HashMap();
        try {
            ObjectInputStream ois = new ConfigInputStream(new ByteArrayInputStream(gbeanState), cl);
            try {
                while (true) {
                    GBeanData gbeanData = new GBeanData();
                    gbeanData.readExternal(ois);
                    GBeanMBean gbean = new GBeanMBean(gbeanData, cl);

                    gbeans.put(gbeanData.getName(), gbean);
                }
            } catch (EOFException e) {
                // ok
            } finally {
                ois.close();
            }
            return gbeans;
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to deserialize GBeanState", e);
        }
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     *
     * @param gbeans a Map<ObjectName, GBeanMBean> of GBeans to store
     * @return the persisted GBeans
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *          if there is a problem serializing the state
     */
    public static byte[] storeGBeans(Map gbeans) throws InvalidConfigException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            throw (AssertionError) new AssertionError("Unable to initialize ObjectOutputStream").initCause(e);
        }
        for (Iterator i = gbeans.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ObjectName objectName = (ObjectName) entry.getKey();
            GBeanMBean gbean = (GBeanMBean) entry.getValue();
            try {
                GBeanData gbeanData = gbean.getGBeanData();
                // todo we must explicitly set the bean name here from the gbean key because the gbean mbean may
                // not have been brought online, so the object namve in the gbean mbean will be null
                gbeanData.setName(objectName);
                gbeanData.writeExternal(oos);
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to serialize GBeanState for " + objectName, e);
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Configuration.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("ID", URI.class, true);
        infoFactory.addAttribute("type", ConfigurationModuleType.class, true);
        infoFactory.addAttribute("parentID", URI.class, true);
        infoFactory.addAttribute("classPath", List.class, true);
        infoFactory.addAttribute("dependencies", List.class, true);
        infoFactory.addAttribute("gBeanState", byte[].class, true);
        infoFactory.addAttribute("baseURL", URL.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("Parent", ConfigurationParent.class);
        infoFactory.addReference("Repositories", Repository.class);
        infoFactory.addReference("ConfigurationStore", ConfigurationStore.class);

        infoFactory.setConstructor(new String[]{
            "kernel",
            "objectName",
            "ID",
            "type",
            "parentID",
            "Parent",
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
