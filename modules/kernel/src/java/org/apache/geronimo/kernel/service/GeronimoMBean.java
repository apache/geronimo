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
package org.apache.geronimo.kernel.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.service.AbstractManagedObject;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.deployment.DeploymentException;

/**
 * A GeronimoMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and opperation according to a supplied
 * GeronimoMBeanInfo instance.  The GeronimoMBean also support caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:35 $
 */
public class GeronimoMBean extends AbstractManagedObject implements DynamicMBean {
    private final Log log = LogFactory.getLog(getClass());
    private final Map attributeInfoMap = new HashMap();
    private final Map operationInfoMap = new HashMap();
    private GeronimoMBeanContext context;
    private GeronimoMBeanInfo mbeanInfo;
    private ClassLoader classLoader;

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        super.preRegister(server, name);
        if (classLoader == null) {
            throw new DeploymentException("No class loader set for Geronimo MBean");
        }
        if (mbeanInfo == null) {
            throw new DeploymentException("No MBean info set for Geronimo MBean");
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            addManagedObjectMBeanInfo();
            mbeanInfo = new GeronimoMBeanInfo(mbeanInfo);
            Set attributes = mbeanInfo.getAttributeSet();
            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
                GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) iterator.next();
                attributeInfoMap.put(attributeInfo.getName(), attributeInfo);
            }
            Set operations = mbeanInfo.getOperationsSet();
            for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
                GeronimoOperationInfo operationInfo = (GeronimoOperationInfo) iterator.next();
                operationInfoMap.put(new MethodKey(operationInfo.getName(), operationInfo.getParameterTypes()), operationInfo);
            }

            context = new GeronimoMBeanContext(server, this, name);
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    ((GeronimoMBeanTarget) target).setMBeanContext(context);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        return this.objectName;
    }

    public void postDeregister() {
        super.postDeregister();
        for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
            Object target = i.next();
            if (target instanceof GeronimoMBeanTarget) {
                ((GeronimoMBeanTarget) target).setMBeanContext(null);
            }
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public void setMBeanInfo(GeronimoMBeanInfo mbeanInfo) throws MBeanException, RuntimeOperationsException {
        if (mbeanInfo == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("MBean info cannot be null"));
        }
        if (server != null) {
            throw new RuntimeOperationsException(new IllegalStateException("MBean info cannot changed while registered with the MBean server"));
        }

        this.mbeanInfo = mbeanInfo;
    }

    protected boolean canStart() {
        for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
            Object target = i.next();
            if (target instanceof GeronimoMBeanTarget) {
                if (!((GeronimoMBeanTarget) target).canStart()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void doStart() throws Exception {
        for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
            Object target = i.next();
            if (target instanceof GeronimoMBeanTarget) {
                ((GeronimoMBeanTarget) target).doStart();
            }
        }
    }

    protected boolean canStop() {
        for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
            Object target = i.next();
            if (target instanceof GeronimoMBeanTarget) {
                if (!((GeronimoMBeanTarget) target).canStop()) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void doStop() throws Exception {
        for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
            Object target = i.next();
            if (target instanceof GeronimoMBeanTarget) {
                ((GeronimoMBeanTarget) target).doStop();
            }
        }
    }

    public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) attributeInfoMap.get(attributeName);
        if (attributeInfo == null) {
            throw new AttributeNotFoundException("Unknown attribute " + attributeName);
        }

        return getAttribute(attributeInfo);
    }

    private Object getAttribute(GeronimoAttributeInfo attributeInfo) throws ReflectionException {
        // if the attribute is never stale, just return the cached value
        long cacheTimeLimit = attributeInfo.cacheTimeLimit;
        if (cacheTimeLimit == 0) {
            // must be synchronized to assure a consistent view of the cache
            synchronized (attributeInfo) {
                // has this attribute every been loaded?
                if (attributeInfo.lastUpdate > 0) {
                    return attributeInfo.value;
                }
            }
        }

        if (cacheTimeLimit > 0) {
            long now = System.currentTimeMillis();

            // check if the current cached value is still valid
            // this must be done in a synchronized block to assure a consistent view of the cache
            synchronized (attributeInfo) {
                long lastUpdate = attributeInfo.lastUpdate;
                if (now < lastUpdate + (cacheTimeLimit * 1000)) {
                    return attributeInfo.value;
                }
            }
        }

        // invoke the getter
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Object value = attributeInfo.getterProxy.invoke(attributeInfo.target, null);

            // if we need to update the cache do it in a synchonized block to assure a
            // consistent view of the cache
            if (cacheTimeLimit >= 0) {
                synchronized (attributeInfo) {
                    attributeInfo.value = value;
                    attributeInfo.lastUpdate = System.currentTimeMillis();
                }
            }
            return value;
        } catch (Throwable throwable) {
            throw new ReflectionException(new InvocationTargetException(throwable));
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) attributeInfoMap.get(attribute.getName());
        if (attributeInfo == null) {
            throw new AttributeNotFoundException("Unknown attribute " + attribute);
        }

        setAttribute(attributeInfo, attribute.getValue());
    }

    private void setAttribute(GeronimoAttributeInfo attributeInfo, Object value) throws ReflectionException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            attributeInfo.setterProxy.invoke(attributeInfo.target, new Object[]{value});
        } catch (Throwable throwable) {
            throw new ReflectionException(new InvocationTargetException(throwable));
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // if we use caching at all...
        long cacheTimeLimit = attributeInfo.cacheTimeLimit;
        if (cacheTimeLimit >= 0) {
            // this must be in a synchronized block to assure a consistent view of the cache
            synchronized (attributeInfo) {
                // if have a specific time out, we need to update the last update time stamp
                if (cacheTimeLimit > 0) {
                    attributeInfo.lastUpdate = System.currentTimeMillis();
                }

                // finally set the cached value
                attributeInfo.value = value;
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

    public Object invoke(String methodName, Object[] arguments, String[] types) throws MBeanException, ReflectionException {
        MethodKey key = new MethodKey(methodName, types);
        Object info = operationInfoMap.get(key);
        if (info == null) {
            throw new ReflectionException(new NoSuchMethodException("Unknown operation " + key));
        }


        // If this is an attribute accessor get call the getAttibute or setAttribute method,
        // so caching and such are respected.
        if (info instanceof GeronimoAttributeInfo) {
            GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) info;
            if (arguments == null || arguments.length == 0) {
                return getAttribute(attributeInfo);
            } else {
                setAttribute(attributeInfo, arguments[0]);
                return null;
            }
        }

        GeronimoOperationInfo operationInfo = (GeronimoOperationInfo) info;

        // if the attribute is never stale, just return the cached value
        long cacheTimeLimit = operationInfo.cacheTimeLimit;
        if (cacheTimeLimit == 0) {
            // must be done in a synchronized block to assure a consistent view of the cache
            synchronized (operationInfo) {
                // has this method every been invoked?
                if (operationInfo.lastUpdate > 0) {
                    return operationInfo.value;
                }
            }
        }

        if (cacheTimeLimit > 0) {
            long now = System.currentTimeMillis();

            // check if the current cached value is still valid
            // this must be done in a synchronized block to assure a consistent view of the cache
            synchronized (operationInfo) {
                long lastUpdate = operationInfo.lastUpdate;
                if (now < lastUpdate + (cacheTimeLimit * 1000)) {
                    return operationInfo.value;
                }
            }
        }

        // invoke the operations
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Object value = operationInfo.methodProxy.invoke(operationInfo.target, arguments);

            // if we need to update the cache do it in a synchonized block to assure a
            // consistent view of the cache
            if (cacheTimeLimit >= 0) {
                synchronized (operationInfo) {
                    operationInfo.value = value;
                    operationInfo.lastUpdate = System.currentTimeMillis();
                }
            }
            return value;
        } catch (Throwable throwable) {
            throw new ReflectionException(new InvocationTargetException(throwable));
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void load() throws MBeanException, InstanceNotFoundException, RuntimeOperationsException {
        throw new MBeanException(null, "Persistence is not supported");
    }

    public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
        throw new MBeanException(null, "Persistence is not supported");
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return mbeanInfo.getNotifications();
    }

    private void addManagedObjectMBeanInfo() {
        GeronimoAttributeInfo attributeInfo;

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("state");
        attributeInfo.target = this;
        attributeInfo.setDescription("J2EE Management State");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(-1);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("objectName");
        attributeInfo.target = this;
        attributeInfo.setDescription("JMX Object Name");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("startTime");
        attributeInfo.target = this;
        attributeInfo.setDescription("Time the MBean started");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(-1);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("stateManageable");
        attributeInfo.target = this;
        attributeInfo.setDescription("Is this MBean state manageable?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("statisticsProvider");
        attributeInfo.target = this;
        attributeInfo.setDescription("Does this MBean provide statistics?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("eventProvider");
        attributeInfo.target = this;
        attributeInfo.setDescription("Does this MBean provide events?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        GeronimoOperationInfo operationInfo;

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("start");
        operationInfo.target = this;
        operationInfo.setDescription("Starts the MBean");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("startRecursive");
        operationInfo.target = this;
        operationInfo.setDescription("Starts the MBean and then starts all the dependent MBeans");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("stop");
        operationInfo.target = this;
        operationInfo.setDescription("Stops the MBean");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        GeronimoNotificationInfo notificationInfo = new GeronimoNotificationInfo();

        notificationInfo.setName("javax.management.Notification");
        notificationInfo.setDescription("J2EE Notifications");
        notificationInfo.addAllNotificationTypes(NotificationType.TYPES);
        mbeanInfo.addNotificationInfo(notificationInfo);
    }

    private final static String[] NO_TYPES = new String[0];

    private final class MethodKey {
        private final String name;
        private final String[] argumentTypes;

        public MethodKey(String name, String[] argumentTypes) {
            this.name = name;
            if (argumentTypes != null) {
                this.argumentTypes = argumentTypes;
            } else {
                this.argumentTypes = NO_TYPES;
            }
        }

        public boolean equals(Object object) {
            if (!(object instanceof MethodKey)) {
                return false;
            }

            // match names
            MethodKey methodKey = (MethodKey) object;
            if (!methodKey.name.equals(name)) {
                return false;
            }

            // match arg length
            int length = methodKey.argumentTypes.length;
            if (length != argumentTypes.length) {
                return false;
            }

            // match each arg
            for (int i = 0; i < length; i++) {
                if (!methodKey.argumentTypes[i].equals(argumentTypes[i])) {
                    return false;
                }
            }
            return true;
        }

        public int hashCode() {
            int result = 17;
            result = 37 * result + name.hashCode();
            for (int i = 0; i < argumentTypes.length; i++) {
                result = 37 * result + argumentTypes[i].hashCode();
            }
            return result;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer(name);
            for (int i = 0; i < argumentTypes.length; i++) {
                buffer.append(argumentTypes[i]);
            }
            return buffer.toString();
        }
    }
}
