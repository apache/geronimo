/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.kernel.Kernel;

/**
 * A Configuration represents a collection of runnable services that can be
 * loaded into a Geronimo Kernel and brought online. The primary components in
 * a Configuration are a codebase, represented by a collection of URLs that
 * is used to locate classes, and a collection of GBean instances that define
 * its state.
 *
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
 *
 * A dependency on the Configuration is created for every GBean it loads. As a
 * result, a startRecursive() operation on the configuration will result in
 * a startRecursive() for all the GBeans it contains. Similarly, if the
 * Configuration is stopped then all of its GBeans will be stopped as well.
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/22 02:46:27 $
 */
public class Configuration implements GBean {
    private static final Log log = LogFactory.getLog(Configuration.class);

    private final URI id;
    private final ConfigurationParent parent;
    private final List classPath;
    private final byte[] gbeanState;

    private GBeanMBeanContext context;
    private URL baseURL;
    private Map gbeans;

    private ClassLoader classLoader;
    private byte[] savedState;

    /**
     * Constructor that can be used to create an offline Configuration, typically
     * only used publically during the deployment process for initial configuration.
     * @param id the unique ID of this Configuration
     * @param parent the parent Configuration; may be null
     * @param classPath a List<URI> of locations that define the codebase for this Configuration
     * @param gbeanState a byte array contain the Java Serialized form of the GBeans in this Configuration
     */
    public Configuration(URI id, ConfigurationParent parent, List classPath, byte[] gbeanState) {
        this.id = id;
        this.parent = parent;
        this.gbeanState = gbeanState;
        this.classPath = classPath;
    }

    public void setGBeanContext(GBeanContext context) {
        this.context = (GBeanMBeanContext)context;
    }

    public void doStart() throws Exception {
        // build classpath
        URL[] urls = new URL[classPath.size()];
        for (int i = 0; i < urls.length; i++) {
            URI path =  (URI) classPath.get(i);
            urls[i] = new URL(baseURL, path.toString());
        }
        if (parent == null) {
            classLoader = new URLClassLoader(urls);
        } else {
            classLoader = new URLClassLoader(urls, parent.getClassLoader());
        }

        // create and initialize GBeans
        gbeans = loadGBeans(gbeanState, classLoader);

        // register all the GBeans
        for (Iterator i = gbeans.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            ObjectName name = (ObjectName) entry.getKey();
            GBeanMBean gbean = (GBeanMBean) entry.getValue();
            MBeanServer mbServer = context.getServer();
            mbServer.registerMBean(gbean, name);
            mbServer.invoke(Kernel.DEPENDENCY_SERVICE, "addDependency", new Object[] { name, context.getObjectName()}, new String[] {ObjectName.class.getName(), ObjectName.class.getName()});
        }
    }

    public void doStop() {
        // unregister all GBeans
        MBeanServer mbServer = context.getServer();
        for (Iterator i = gbeans.keySet().iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
            try {
                mbServer.invoke(Kernel.DEPENDENCY_SERVICE, "removeDependency", new Object[] { name, context.getObjectName()}, new String[] {ObjectName.class.getName(), ObjectName.class.getName()});
            } catch (Exception e) {
                // ignore
                log.warn("Could not remove dependency for child "+name, e);
            }
            try {
                mbServer.unregisterMBean(name);
            } catch (Exception e) {
                // ignore
                log.warn("Could not unregister child "+name, e);
            }
        }

        // save state
        try {
            savedState = storeGBeans(gbeans);
        } catch (InvalidConfigException e) {
            log.info(e);
        }
        gbeans = null;
    }

    public void doFail() {
    }

    /**
     * Return the unique ID
     * @return the unique ID
     */
    public URI getID() {
        return id;
    }

    /**
     * Return the URL that is used to resolve relative classpath locations
     * @return the base URL for the classpath
     */
    public URL getBaseURL() {
        return baseURL;
    }

    /**
     * Set the URL that should be used to resolve relative class locations
     * @param baseURL the base URL for the classpath
     */
    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public byte[] getSavedState() {
        return savedState;
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
     * @param gbeanState the serialized form of the GBeans
     * @param cl the ClassLoader used to locate classes needed during deserialization
     * @return a Map<ObjectName, GBeanMBean> of GBeans loaded from the persisted state
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a problem deserializing the state
     */
    public static Map loadGBeans(byte[] gbeanState, ClassLoader cl) throws InvalidConfigException {
        Map gbeans = new HashMap();
        try {
            ObjectInputStream ois = new ConfigInputStream(new ByteArrayInputStream(gbeanState), cl);
            try {
                while (true) {
                    ObjectName objectName = (ObjectName) ois.readObject();
                    GBeanInfo info = (GBeanInfo) ois.readObject();
                    GBeanMBean gbean = new GBeanMBean(info, cl);
                    loadGMBeanState(gbean, ois);
                    gbeans.put(objectName, gbean);
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

    static void loadGMBeanState(GBeanMBean gbean, ObjectInputStream ois) throws IOException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, ClassNotFoundException {
        int attributeCount = ois.readInt();
        for (int i = 0; i < attributeCount; i ++) {
            gbean.setAttribute((String)ois.readObject(), ois.readObject());
        }
        int endpointCount = ois.readInt();
        for (int i = 0; i < endpointCount; i ++) {
            gbean.setEndpointPatterns((String)ois.readObject(), (Set)ois.readObject());
        }
    }

    /**
     * Return a byte array containing the persisted form of the supplied GBeans
     * @param gbeans a Map<ObjectName, GBeanMBean> of GBeans to store
     * @return the persisted GBeans
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a problem serializing the state
     */
    public static byte[] storeGBeans(Map gbeans) throws InvalidConfigException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            for (Iterator i = gbeans.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                ObjectName objectName = (ObjectName) entry.getKey();
                GBeanMBean gbean = (GBeanMBean) entry.getValue();
                oos.writeObject(objectName);
                oos.writeObject(gbean.getGBeanInfo());
                storeGMBeanState(gbean, oos);
            }
            oos.flush();
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to serialize GBeanState", e);
        }
        return baos.toByteArray();
    }

    static void storeGMBeanState(GBeanMBean gbean, ObjectOutputStream oos) throws IOException, AttributeNotFoundException, MBeanException, ReflectionException {
        List persistentAttributes = gbean.getGBeanInfo().getPersistentAttributes();
        oos.writeInt(persistentAttributes.size());
        for (Iterator j = persistentAttributes.iterator(); j.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) j.next();
            oos.writeObject(attributeInfo.getName());
            oos.writeObject(gbean.getAttribute(attributeInfo.getName()));
        }
        Set endpointsSet = gbean.getGBeanInfo().getEndpointsSet();
        oos.writeInt(endpointsSet.size());
        for (Iterator iterator = endpointsSet.iterator(); iterator.hasNext();) {
            GEndpointInfo gEndpointInfo = (GEndpointInfo) iterator.next();
            oos.writeObject(gEndpointInfo.getName());
            oos.writeObject(gbean.getEndpointPatterns(gEndpointInfo.getName()));
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        Set attributes = new HashSet();
        attributes.add(new GAttributeInfo("ID", true));
        attributes.add(new GAttributeInfo("ClassPath", true));
        attributes.add(new GAttributeInfo("GBeanState", true));
        attributes.add(new GAttributeInfo("BaseURL"));
        attributes.add(new GAttributeInfo("ObjectName"));
        attributes.add(new GAttributeInfo("ClassLoader"));
        attributes.add(new GAttributeInfo("SavedState"));
        List constructorNames = new ArrayList();
        constructorNames.add("ID");
        constructorNames.add("Parent");
        constructorNames.add("ClassPath");
        constructorNames.add("GBeanState");
        List constructorTypes = new ArrayList();
        constructorTypes.add(URI.class);
        constructorTypes.add(ConfigurationParent.class);
        constructorTypes.add(List.class);
        constructorTypes.add(byte[].class);
        GConstructorInfo constructor = new GConstructorInfo(constructorNames, constructorTypes);
        Set endpoints = new HashSet();
        endpoints.add(new GEndpointInfo("Parent", ConfigurationParent.class.getName()));
        Set operations = Collections.EMPTY_SET;
        GBEAN_INFO = new GBeanInfo(Configuration.class.getName(), attributes, constructor, operations, endpoints, Collections.EMPTY_SET);
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
