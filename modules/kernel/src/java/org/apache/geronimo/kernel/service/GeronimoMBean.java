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
import java.lang.reflect.Method;
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
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;

import net.sf.cglib.reflect.FastClass;

/**
 * A GeronimoMBean is a J2EE Management Managed Object, and is standard base for Geronimo services.
 * This wraps one or more target POJOs and exposes the attributes and opperation according to a supplied
 * GeronimoMBeanInfo instance.  The GeronimoMBean also support caching of attribute values and invocation results
 * which can reduce the number of calls to a target.
 *
 * @version $Revision: 1.13 $ $Date: 2004/01/22 08:10:27 $
 */
public class GeronimoMBean extends AbstractManagedObject2 implements DynamicMBean {
    public static final FastClass fastClass = FastClass.create(GeronimoMBean.class);

    private final Log log = LogFactory.getLog(getClass());
    private final Map attributeInfoMap = new HashMap();
    private final Map operationInfoMap = new HashMap();
    private GeronimoMBeanContext context;
    private GeronimoMBeanInfo mbeanInfo;
    private ObjectName classSpace;
    private ClassLoader classLoader;

    public GeronimoMBean() {
    }

    public GeronimoMBean(GeronimoMBeanInfo mbeanInfo) {
        this.mbeanInfo = mbeanInfo;
    }
    /**
     * Static helper to try to get the GeronimoMBeanInfo from the class supplied.
     * @param className
     * @return GeronimoMBeanInfo generated by supplied class
     * @throws Exception
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo(String className) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        Class clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            //Most likely, descriptor was an xml file, not a class name.
            return null;
        }
        Method m = clazz.getDeclaredMethod("getGeronimoMBeanInfo", new Class[] {});
        if (m != null) {
            return (GeronimoMBeanInfo)m.invoke(clazz, new Object[] {});
        }
        return null;
    }

    /**
     * "Bootstrapping" constructor.  The class specified is loaded and the static method
     * "getGeronimoMBeanInfo" is called to get the mbean info.  Usually one will include
     * this static method in the class to be wrapped in the GeronimoMBean instance.
     * @param className
     * @throws Exception
     */
    public GeronimoMBean(String className) throws Exception {
        mbeanInfo = getGeronimoMBeanInfo(className);
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        super.preRegister(server, name);
        if (mbeanInfo == null) {
            throw new Exception("No MBean info set for Geronimo MBean");
        }

        classLoader = ClassSpaceUtil.getClassLoader(server, classSpace);
        context = new GeronimoMBeanContext(server, this, name);
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set the class loader
            Thread.currentThread().setContextClassLoader(classLoader);

            // @todo there is an issue here with restarted deployments
            addManagedObjectMBeanInfo();
            mbeanInfo = new GeronimoMBeanInfo(mbeanInfo);
            // build the attribute map
            Set attributes = mbeanInfo.getAttributeSet();
            for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
                GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) iterator.next();
                final String attributeName = attributeInfo.getName();
                attributeInfoMap.put(attributeName, attributeInfo);

                if (attributeInfo.isReadable()) {
                    String getterName = (attributeInfo.isIs() ? "is" : "get") +
                            Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
                    operationInfoMap.put(new MBeanOperationSignature(getterName, new String[0]), attributeInfo);
                }
                if (attributeInfo.isWritable()) {
                    String setterName = "set" + Character.toUpperCase(attributeName.charAt(0)) + attributeName.substring(1);
                    operationInfoMap.put(new MBeanOperationSignature(setterName, new String[]{attributeInfo.getType()}), attributeInfo);
                }
            }

            // build the operation map
            Set operations = mbeanInfo.getOperationsSet();
            for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
                GeronimoOperationInfo operationInfo = (GeronimoOperationInfo) iterator.next();
                operationInfoMap.put(new MBeanOperationSignature(operationInfo.getName(), operationInfo.getParameterTypes()), operationInfo);
            }

            // set the context
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    try {
                        ((GeronimoMBeanTarget) target).setMBeanContext(context);
                    } catch (RuntimeException e) {
                        log.warn("Ignoring RuntimeException from setMBeanContext(context): objectName" + context.getObjectName(), e);
                    }
                }
            }
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                endpoint.setMBeanContext(context);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        return this.objectName;
    }

    public void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue()) {
            context = null;
            return;
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set the class loader
            Thread.currentThread().setContextClassLoader(classLoader);

            super.postRegister(registrationDone);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        //For use primarily in boot mbeans before service deployer is set up
        if (mbeanInfo.isAutostart()) {
            try {
                start();
            } catch (Exception e) {
                log.info("Exception auto-starting GeronimoMBean " + objectName, e);
            }
        }
    }

    public void preDeregister() {
        //For use primarily in boot mbeans before service deployer is set up
        if (mbeanInfo.isAutostart()) {
            try {
                stop();
            } catch (Exception e) {
                log.info("Exception auto-starting GeronimoMBean " + objectName, e);
            }
        }
    }

    public void postDeregister() {
        ObjectName objectName = context.getObjectName();
        context = null;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set the class loader
            Thread.currentThread().setContextClassLoader(classLoader);
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    try {
                        ((GeronimoMBeanTarget) target).setMBeanContext(null);
                    } catch (RuntimeException e) {
                        log.warn("Ignoring RuntimeException from setMBeanContext(null): objectName" + objectName, e);
                    }
                }
            }
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                endpoint.setMBeanContext(null);
            }
            super.postDeregister();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            classLoader = null;
        }
    }

    public ObjectName getClassSpace() {
        return classSpace;
    }

    public void setClassSpace(ObjectName classSpace) {
        this.classSpace = classSpace;
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
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                if (!endpoint.canStart()) {
                    return false;
                }
            }
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    if (!((GeronimoMBeanTarget) target).canStart()) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected void doStart() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                endpoint.doStart();
            }
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    ((GeronimoMBeanTarget) target).doStart();
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected boolean canStop() {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                if (!endpoint.canStop()) {
                    return false;
                }
            }
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    if (!((GeronimoMBeanTarget) target).canStop()) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected void doStop() throws Exception {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                endpoint.doStop();
            }
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    ((GeronimoMBeanTarget) target).doStop();
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected void doFail() {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            for (Iterator i = mbeanInfo.getEndpointsSet().iterator(); i.hasNext();) {
                GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) i.next();
                endpoint.doFail();
            }
            for (Iterator i = mbeanInfo.targets.values().iterator(); i.hasNext();) {
                Object target = i.next();
                if (target instanceof GeronimoMBeanTarget) {
                    ((GeronimoMBeanTarget) target).doFail();
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
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
            Object value = attributeInfo.getterMethod.invoke(attributeInfo.target, null);

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
            attributeInfo.setterMethod.invoke(attributeInfo.target, new Object[]{value});
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
        MBeanOperationSignature key = new MBeanOperationSignature(methodName, types);
        Object info = operationInfoMap.get(key);
        if (info == null) {
            log.info("Operation not found on mbean" + this.objectName + ", method: " + methodName + ", paramtypes: " + types);
            log.info("MBeanInfo is immutable: " + mbeanInfo.immutable + " on mbeanInfo: " + mbeanInfo);
            for (Iterator iterator = operationInfoMap.keySet().iterator(); iterator.hasNext();) {
                MBeanOperationSignature mBeanOperationSignature = (MBeanOperationSignature) iterator.next();
                log.info("Operation: " + mBeanOperationSignature);
            }
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
            Object value = operationInfo.method.invoke(operationInfo.target, arguments);

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
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("J2EE Management State");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(-1);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("objectName");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("JMX Object Name");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("startTime");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("Time the MBean started");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(-1);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("stateManageable");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("Is this MBean state manageable?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("statisticsProvider");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("Does this MBean provide statistics?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("eventProvider");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("Does this MBean provide events?");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(0);
        mbeanInfo.addAttributeInfo(attributeInfo);

        GeronimoOperationInfo operationInfo;

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("start");
        operationInfo.target = this;
        operationInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        operationInfo.setDescription("Starts the MBean");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("startRecursive");
        operationInfo.target = this;
        operationInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        operationInfo.setDescription("Starts the MBean and then starts all the dependent MBeans");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        operationInfo = new GeronimoOperationInfo();
        operationInfo.setName("stop");
        operationInfo.target = this;
        operationInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        operationInfo.setDescription("Stops the MBean");
        operationInfo.setImpact(MBeanOperationInfo.ACTION);
        operationInfo.setCacheTimeLimit(-1);
        mbeanInfo.addOperationInfo(operationInfo);

        GeronimoNotificationInfo notificationInfo = new GeronimoNotificationInfo();

        notificationInfo.setName("javax.management.Notification");
        notificationInfo.setDescription("J2EE Notifications");
        notificationInfo.addAllNotificationTypes(NotificationType.TYPES);
        mbeanInfo.addNotificationInfo(notificationInfo);

        // Geronimo MBean Extra attibutes
        attributeInfo = new GeronimoAttributeInfo();
        attributeInfo.setName("classSpace");
        attributeInfo.target = this;
        attributeInfo.setTargetName(GeronimoMBeanInfo.GERONIMO_MBEAN_TARGET_NAME);
        attributeInfo.setDescription("Class Space for this MBean");
        attributeInfo.setReadable(true);
        attributeInfo.setWritable(false);
        attributeInfo.setCacheTimeLimit(-1);
        mbeanInfo.addAttributeInfo(attributeInfo);

    }
}
