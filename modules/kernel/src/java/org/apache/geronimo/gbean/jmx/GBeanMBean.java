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

package org.apache.geronimo.gbean.jmx;

import java.util.Iterator;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * A GBeanMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and operations according to a supplied
 * {@link GBeanInfo} instance.  The GBeanMBean also supports caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Rev$ $Date$
 */
public final class GBeanMBean implements DynamicMBean, MBeanRegistration, NotificationEmitter {
    private static final Log log = LogFactory.getLog(GBeanMBean.class);
    private static final MBeanInfo DEFAULT_MBEAN_INFO = new MBeanInfo("java.lang.Object", "", new MBeanAttributeInfo[0], new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);

    /**
     * Gets the context class loader from the thread or the system class loader if there is no context class loader.
     *
     * @return the context class loader or the system classloader
     */
    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = GBeanMBean.class.getClassLoader();
        }
        return classLoader;
    }

    /**
     * Attribute name used to retrieve the GBeanData for the GBean
     */
    public static final String GBEAN_DATA = "$$GBEAN_DATA$$";

    /**
     * The kernel in which this server is registered.
     */
    private Kernel kernel;

    /**
     * The unique name of this service.
     */
    private ObjectName objectName;

    /**
     * The data of the
     */
    private GBeanData gbeanData;

    /**
     * The instance for this gbean mbean
     */
    private GBeanInstance gbeanInstance;

    /**
     * The classloader used for all invocations and creating targets.
     */
    private final ClassLoader classLoader;

    /**
     * JMX sped mbeanInfo for this gbean (translation of the above gbeanInfo
     */
    private MBeanInfo mbeanInfo = DEFAULT_MBEAN_INFO;

    /**
     * The broadcaster for notifications
     */
    private JMXLifecycleBroadcaster lifecycleBroadcaster;

    /**
     * This is the constructor used by the kernel.  This constructor should not be used dirctly.
     * Instedad you should use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(Kernel kernel, GBeanData gbeanData, ClassLoader classLoader) throws InvalidConfigurationException {
        this.kernel = kernel;
        this.gbeanData = gbeanData;
        this.classLoader = classLoader;

        this.objectName = gbeanData.getName();
    }

    /**
     * This constructor allows the kernel to bootstrap and existing GBeanInstance directly into the MBeanServer.
     * @deprecated DO NOT USE
     */
    public GBeanMBean(Kernel kernel, GBeanInstance gbeanInstance, JMXLifecycleBroadcaster lifecycleBroadcaster) throws InvalidConfigurationException {
        this.kernel = kernel;
        this.gbeanInstance = gbeanInstance;
        this.lifecycleBroadcaster = lifecycleBroadcaster;

        this.objectName = gbeanInstance.getObjectNameObject();
        this.gbeanData = gbeanInstance.getGBeanData();
        this.classLoader = gbeanInstance.getClassLoader();
    }

    /**
     * Constructa a GBeanMBean using the supplied GBeanData and class loader
     *
     * @param gbeanData the data for the new GBean including GBeanInfo, intial attribute values, and reference patterns
     * @param classLoader the class loader used to load the gbean instance and attribute/reference types
     * @throws InvalidConfigurationException if the gbeanInfo is inconsistent with the actual java classes, such as
     * mismatched attribute types or the intial data can not be set
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(GBeanData gbeanData, ClassLoader classLoader) throws InvalidConfigurationException {
        this.classLoader = classLoader;
        this.gbeanData = gbeanData;
    }

    /**
     * Constructa a GBeanMBean using the supplied gbeanInfo and class loader
     *
     * @param gbeanInfo the metadata describing the attributes, operations, constructor and references of the gbean
     * @param classLoader the class loader used to load the gbean instance and attribute/reference types
     * @throws InvalidConfigurationException if the gbeanInfo is inconsistent with the actual java classes, such as
     * mismatched attribute types
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(GBeanInfo gbeanInfo, ClassLoader classLoader) throws InvalidConfigurationException {
        this(new GBeanData(gbeanInfo), classLoader);
    }

    /**
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(GBeanInfo gbeanInfo) throws InvalidConfigurationException {
        this(new GBeanData(gbeanInfo), getContextClassLoader());
    }

    /**
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(String className, ClassLoader classLoader) throws Exception {
        this(new GBeanData(GBeanInfo.getGBeanInfo(className, classLoader)), classLoader);
    }

    /**
     * @deprecated use kernel.loadGBean(GBeanData gbeanData, ClassLoader classLoader)
     */
    public GBeanMBean(String className) throws Exception {
        this(className, ClassLoader.getSystemClassLoader());
    }

    /**
     * Gets the MBeanInfo equivilent of the GBeanInfo used to construct this gbean.
     *
     * @return the MBeanInfo for this gbean
     */
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public synchronized ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        if (gbeanInstance == null) {
            this.objectName = objectName;
            try {
                String kernelName = (String) server.getAttribute(Kernel.KERNEL, "KernelName");
                kernel = Kernel.getKernel(kernelName);
            } catch (Exception e) {
                throw new IllegalStateException("No kernel is registered in this MBeanServer");
            }

            gbeanData.setName(objectName);
            lifecycleBroadcaster = new JMXLifecycleBroadcaster();
            gbeanInstance = new GBeanInstance(kernel, gbeanData, lifecycleBroadcaster, classLoader);
            mbeanInfo = GBeanJMXUtil.toMBeanInfo(gbeanInstance.getGBeanInfo());
        }
        return gbeanInstance.getObjectNameObject();
    }

    public synchronized void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue()) {
            if (gbeanInstance != null) {
                gbeanInstance.destroy();
                gbeanInstance = null;
            }
            mbeanInfo = DEFAULT_MBEAN_INFO;
            lifecycleBroadcaster = null;
            kernel = null;
            objectName = null;
        }
    }

    public void preDeregister() throws Exception {
    }

    public synchronized void postDeregister() {
        if (gbeanInstance != null) {
            gbeanData = gbeanInstance.getGBeanData();
            gbeanInstance.destroy();
            gbeanInstance = null;
        }
        mbeanInfo = DEFAULT_MBEAN_INFO;
        kernel = null;
        objectName = null;
    }

    /**
     * Gets the gbean data for the gbean held by this gbean mbean.
     * @return the gbean data
     */
    public GBeanData getGBeanData() {
        if (gbeanInstance != null) {
            return gbeanInstance.getGBeanData();
        } else {
            return gbeanData;
        }
    }

    public void setGBeanData(GBeanData gbeanData) throws Exception {
        if (gbeanInstance != null) {
            gbeanInstance.setGBeanData(gbeanData);
        } else {
            this.gbeanData = gbeanData;
        }
    }

    public Object getAttribute(String name) throws ReflectionException, AttributeNotFoundException {
        if (gbeanInstance == null) {
            return gbeanData.getAttribute(name);
        } else {
            try {
                return gbeanInstance.getAttribute(name);
            } catch (NoSuchAttributeException e) {
                throw new AttributeNotFoundException(name);
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
    }

    public void setAttribute(String name, Object value) throws ReflectionException, AttributeNotFoundException {
        if (gbeanInstance == null) {
            gbeanData.setAttribute(name, value);
        } else {
            try {
                gbeanInstance.setAttribute(name, value);
            } catch (NoSuchAttributeException e) {
                throw new AttributeNotFoundException(name);
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
    }

    public void setAttribute(Attribute attribute) throws ReflectionException, AttributeNotFoundException {
        String name = attribute.getName();
        Object value = attribute.getValue();
        if (gbeanInstance == null) {
            gbeanData.setAttribute(name, value);
        } else {
            try {
                gbeanInstance.setAttribute(name, value);
            } catch (NoSuchAttributeException e) {
                throw new AttributeNotFoundException(name);
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList results = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String name = attributes[i];
            try {
                Object value = getAttribute(name);
                results.add(new Attribute(name, value));
            } catch (JMException e) {
                log.warn("Exception while getting attribute " + name, e);
            }
        }
        return results;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList results = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            try {
                setAttribute(attribute);
                results.add(attribute);
            } catch (JMException e) {
                log.warn("Exception while setting attribute " + attribute.getName(), e);
            }
        }
        return results;
    }

    public Object invoke(String operationName, Object[] arguments, String[] types) throws ReflectionException {
        if (gbeanInstance == null) {
            throw new IllegalStateException("An offline gbean can not be invoked: " + objectName);
        } else {
            try {
                return gbeanInstance.invoke(operationName, arguments, types);
            } catch (NoSuchOperationException e) {
                throw new ReflectionException(new NoSuchMethodException(new GOperationSignature(operationName, types).toString()));
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
    }

    /**
     * Gets the object name patters for a reference.
     *
     * @param name the reference name
     * @return the object name patterns for the reference
     */
    public Set getReferencePatterns(String name) {
        if (gbeanInstance != null) {
            return gbeanInstance.getReferencePatterns(name);
        } else {
            return gbeanData.getReferencePatterns(name);
        }
    }

    /**
     * Sets a single object name pattern for a reference.
     *
     * @param name the reference name
     * @param pattern the new single object name pattern for the reference
     */
    public void setReferencePattern(String name, ObjectName pattern) {
        if (gbeanInstance != null) {
            gbeanInstance.setReferencePattern(name, pattern);
        } else {
            gbeanData.setReferencePattern(name, pattern);
        }
    }

    /**
     * Sets the object name patterns for a reference.
     *
     * @param name the reference name
     * @param patterns the new object name patterns for the reference
     */
    public void setReferencePatterns(String name, Set patterns) {
        if (gbeanInstance != null) {
            gbeanInstance.setReferencePatterns(name, patterns);
        } else {
            gbeanData.setReferencePatterns(name, patterns);
        }
    }

    public final String getObjectName() {
        return objectName.getCanonicalName();
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{
            new MBeanNotificationInfo(NotificationType.TYPES, "javax.management.Notification", "J2EE Notifications")
        };
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
        lifecycleBroadcaster.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        lifecycleBroadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        lifecycleBroadcaster.removeNotificationListener(listener, filter, handback);
    }

    public String toString() {
        if (objectName == null) {
            return super.toString();
        }
        return objectName.toString();
    }
}
