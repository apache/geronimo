/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.jmx;

import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.MalformedObjectNameException;
import javax.management.loading.ClassLoaderRepository;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;

/**
 * A fake MBeanServer that delegates to a Kernel.
 * @version $Rev$ $Date$
 */
public class KernelMBeanServer implements MBeanServer {
    private static final AbstractNameQuery ALL = new AbstractNameQuery(null, Collections.EMPTY_MAP, Collections.EMPTY_SET);

    private final HashMap<ObjectName, AbstractName> objetNameToAbstractName = new HashMap<ObjectName, AbstractName>();
    private final Kernel kernel;

    public KernelMBeanServer(Kernel kernel) {
        this.kernel = kernel;
    }

    public void doStart() {
        kernel.getLifecycleMonitor().addLifecycleListener(new GBeanRegistrationListener(), ALL);

        Set<AbstractName> allNames = kernel.listGBeans(ALL);
        for (AbstractName abstractName : allNames) {
            register(abstractName);
        }
    }

    public synchronized AbstractName getAbstractNameFor(ObjectName objectName) {
        return objetNameToAbstractName.get(objectName);
    }

    private synchronized void register(AbstractName abstractName) {
        objetNameToAbstractName.put(abstractName.getObjectName(), abstractName);
    }

    private synchronized void unregister(AbstractName abstractName) {
        objetNameToAbstractName.remove(abstractName.getObjectName());
    }

    public void doFail() {
        doStop();
    }

    public synchronized void doStop() {
        objetNameToAbstractName.clear();
    }

    private class GBeanRegistrationListener extends LifecycleAdapter {
        public void loaded(AbstractName abstractName) {
            register(abstractName);
        }

        public void unloaded(AbstractName abstractName) {
            unregister(abstractName);
        }
    }

    public AbstractName toAbstractName(ObjectName objectName) throws InstanceNotFoundException{
        AbstractName abstractName = getAbstractNameFor(objectName);
        if (abstractName == null) {
            throw new InstanceNotFoundException(objectName.getCanonicalName());
        }
        return abstractName;
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        AbstractName abstractName = toAbstractName(name);
        try {
            return kernel.getAttribute(abstractName, attribute);
        } catch (NoSuchAttributeException e) {
            throw (AttributeNotFoundException)new AttributeNotFoundException(attribute).initCause(e);
        } catch (GBeanNotFoundException e) {
            throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        AbstractName abstractName = toAbstractName(name);
        AttributeList attributeList = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
            try {
                Object value = kernel.getAttribute(abstractName, attribute);
                attributeList.add(i, new Attribute(attribute, value));
            } catch (NoSuchAttributeException e) {
                // ignored - caller will simply find no value
            } catch (GBeanNotFoundException e) {
                throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
            } catch (InternalKernelException e) {
                throw new ReflectionException(unwrapInternalKernelException(e));
            } catch (Exception e) {
                // ignored - caller will simply find no value
            }
        }
        return attributeList;
    }

    public String getDefaultDomain() {
        return kernel.getKernelName();
    }

    public Integer getMBeanCount() {
        return kernel.listGBeans((AbstractNameQuery) null).size();
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, ReflectionException {
        AbstractName abstractName = toAbstractName(name);
        GBeanInfo gbeanInfo;
        try {
            gbeanInfo = kernel.getGBeanInfo(abstractName);
        } catch (GBeanNotFoundException e) {
            throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
        } catch (InternalKernelException e) {
            throw new ReflectionException(unwrapInternalKernelException(e));
        }
        return JMXUtil.toMBeanInfo(gbeanInfo);
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
        AbstractName abstractName = toAbstractName(name);
        try {
            return kernel.invoke(abstractName, operationName, params, signature);
        } catch (NoSuchOperationException e) {
            throw new ReflectionException((NoSuchMethodException)new NoSuchMethodException(e.getMessage()).initCause(e));
        } catch (GBeanNotFoundException e) {
            if(name.equals(e.getGBeanName())) {
                throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
            }
            throw new MBeanException(e);
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public boolean isRegistered(ObjectName name) {
        AbstractName abstractName = getAbstractNameFor(name);
        if (abstractName == null) {
            return false;
        }
        return kernel.isLoaded(abstractName);
    }

    public Set<ObjectName> queryNames(ObjectName pattern, QueryExp query) {
        // normalize the name
        if (pattern != null && pattern.getDomain().length() == 0) {
            try {
                pattern = new ObjectName(kernel.getKernelName(), pattern.getKeyPropertyList());
            } catch (MalformedObjectNameException e) {
                throw new AssertionError(e);
            }
        }

        Set<ObjectName> names;
        synchronized (this) {
            names = new LinkedHashSet<ObjectName>(objetNameToAbstractName.keySet());
        }

        // fairly dumb implementation that iterates the list of all registered GBeans
        Set<ObjectName> result = new HashSet<ObjectName>(names.size());
        for (ObjectName name : names) {
            if (pattern == null || pattern.apply(name)) {
                if (query != null) {
                    query.setMBeanServer(this);

                    try {
                        if (query.apply(name)) {
                            result.add(name);
                        }
                    } catch (Exception e) {
                        // reject any name that threw an exception
                    }
                } else {
                    result.add(name);
                }
            }
        }

        return result;
    }

    public Set<ObjectInstance> queryMBeans(ObjectName pattern, QueryExp query) {
        Set<ObjectName> names = queryNames(pattern, query);
        Set<ObjectInstance> objectInstances = new HashSet<ObjectInstance>(names.size());
        for (ObjectName name : names) {
            try {
                objectInstances.add(getObjectInstance(name));
            } catch (InstanceNotFoundException e) {
                // ignore
            }
        }
        return objectInstances;
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, MBeanException {
        AbstractName abstractName = toAbstractName(name);
        String attributeName = attribute.getName();
        Object attributeValue = attribute.getValue();
        try {
            kernel.setAttribute(abstractName, attributeName, attributeValue);
        } catch (NoSuchAttributeException e) {
            throw (AttributeNotFoundException)new AttributeNotFoundException(attributeName).initCause(e);
        } catch (GBeanNotFoundException e) {
            throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        AbstractName abstractName = toAbstractName(name);
        AttributeList set = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            String attributeName = attribute.getName();
            Object attributeValue = attribute.getValue();
            try {
                kernel.setAttribute(abstractName, attributeName, attributeValue);
                set.add(attribute);
            } catch (NoSuchAttributeException e) {
                // ignored - caller will see value was not set because this attribute will not be in the attribute list
            } catch (GBeanNotFoundException e) {
                throw (InstanceNotFoundException)new InstanceNotFoundException(name.getCanonicalName()).initCause(e);
            } catch (InternalKernelException e) {
                throw new ReflectionException(unwrapInternalKernelException(e));
            } catch (Exception e) {
                // ignored - caller will see value was not set because this attribute will not be in the attribute list
            }
        }
        return set;
    }

    public String[] getDomains() {
        throw new SecurityException("Operation not allowed");
//        Set<String> domains = new HashSet<String>();
//        Set<AbstractName> names = kernel.listGBeans((AbstractNameQuery)null);
//        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
//            ObjectName objectName = (ObjectName) iterator.next();
//            domains.add(objectName.getDomain());
//        }
//        return domains.toArray(new String[domains.size()]);
    }

    public ObjectInstance getObjectInstance(ObjectName objectName) throws InstanceNotFoundException {
        AbstractName abstractName = toAbstractName(objectName);
        try {
            GBeanInfo gbeanInfo = kernel.getGBeanInfo(abstractName);
            return new ObjectInstance(objectName, gbeanInfo.getClassName());
        } catch (GBeanNotFoundException e) {
            throw (InstanceNotFoundException)new InstanceNotFoundException(objectName.getCanonicalName()).initCause(e);
        }
    }

    public ClassLoader getClassLoaderFor(ObjectName objectName) throws InstanceNotFoundException {
        AbstractName abstractName = toAbstractName(objectName);
        try {
            return new BundleClassLoader(kernel.getBundleFor(abstractName));
        } catch (GBeanNotFoundException e) {
            throw (InstanceNotFoundException)new InstanceNotFoundException(objectName.getCanonicalName()).initCause(e);
        }
    }

    private static Exception unwrapInternalKernelException(InternalKernelException e) {
        if (e.getCause() instanceof Exception) {
            return (Exception) e.getCause();
        }
        return e;
    }

    //////////////////////////////////////////////
    //
    // NOT ALLOWED
    //
    //////////////////////////////////////////////

    public void addNotificationListener(ObjectName objectName, NotificationListener notificationListener, NotificationFilter notificationFilter, Object o) throws InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public void addNotificationListener(ObjectName objectName, ObjectName objectName1, NotificationFilter notificationFilter, Object o) throws InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public void removeNotificationListener(ObjectName objectName, ObjectName objectName1) throws InstanceNotFoundException, ListenerNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public void removeNotificationListener(ObjectName objectName, NotificationListener notificationListener) throws InstanceNotFoundException, ListenerNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public void removeNotificationListener(ObjectName objectName, ObjectName objectName1, NotificationFilter notificationFilter, Object o) throws InstanceNotFoundException, ListenerNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public void removeNotificationListener(ObjectName objectName, NotificationListener notificationListener, NotificationFilter notificationFilter, Object o) throws InstanceNotFoundException, ListenerNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public boolean isInstanceOf(ObjectName objectName, String s) throws InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInstance createMBean(String s, ObjectName objectName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInstance createMBean(String s, ObjectName objectName, ObjectName objectName1) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInstance createMBean(String s, ObjectName objectName, Object[] objects, String[] strings) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInstance createMBean(String s, ObjectName objectName, ObjectName objectName1, Object[] objects, String[] strings) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public Object instantiate(String s) throws ReflectionException, MBeanException {
        throw new SecurityException("Operation not allowed");
    }

    public Object instantiate(String s, ObjectName objectName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public Object instantiate(String s, Object[] objects, String[] strings) throws ReflectionException, MBeanException {
        throw new SecurityException("Operation not allowed");
    }

    public Object instantiate(String s, ObjectName objectName, Object[] objects, String[] strings) throws ReflectionException, MBeanException, InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInstance registerMBean(Object o, ObjectName objectName) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        throw new SecurityException("Operation not allowed");
    }

    @Deprecated
    public ObjectInputStream deserialize(String s, ObjectName objectName, byte[] bytes) throws InstanceNotFoundException, OperationsException, ReflectionException {
        throw new SecurityException("Operation not allowed");
    }

    @Deprecated
    public ObjectInputStream deserialize(String s, byte[] bytes) throws OperationsException, ReflectionException {
        throw new SecurityException("Operation not allowed");
    }

    @Deprecated
    public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes) throws InstanceNotFoundException, OperationsException {
        throw new SecurityException("Operation not allowed");
    }

    public ClassLoader getClassLoader(ObjectName objectName) throws InstanceNotFoundException {
        throw new SecurityException("Operation not allowed");
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        return new ClassLoaderRepository() {
            public Class loadClass(String className) throws ClassNotFoundException {
                throw new ClassNotFoundException(className);
            }

            public Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException {
                throw new ClassNotFoundException(className);
            }

            public Class loadClassBefore(ClassLoader loader, String className) throws ClassNotFoundException {
                throw new ClassNotFoundException(className);
            }
        };
    }

    public void unregisterMBean(ObjectName objectName) throws InstanceNotFoundException, MBeanRegistrationException {
        throw new SecurityException("Operation not allowed");
    }
}
