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
package org.apache.geronimo.kernel.jmx;

import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import javax.management.loading.ClassLoaderRepository;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.Kernel;
 
/**
 * A MBeanServerImplementation that delegates to a Kernel.
 * @version $Rev:  $ $Date:  $
 */
public class MBeanServerDelegate implements MBeanServer {
    private final Kernel kernel;

    public MBeanServerDelegate(Kernel kernel) {
        this.kernel = kernel;
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        try {
            return kernel.getAttribute(name, attribute);
        } catch (NoSuchAttributeException e) {
            throw new AttributeNotFoundException(attribute);
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(name.getCanonicalName());
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        AttributeList attributeList = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
            try {
                Object value = kernel.getAttribute(name, attribute);
                attributeList.add(i, new Attribute(attribute, value));
            } catch (NoSuchAttributeException e) {
                // ignored - caller will simply find no value
            } catch (GBeanNotFoundException e) {
                throw new InstanceNotFoundException(name.getCanonicalName());
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
        return new Integer(kernel.listGBeans((ObjectName)null).size());
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, ReflectionException {
        GBeanInfo gbeanInfo;
        try {
            gbeanInfo = kernel.getGBeanInfo(name);
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(name.toString());
        } catch (InternalKernelException e) {
            throw new ReflectionException(unwrapInternalKernelException(e));
        }
        return JMXUtil.toMBeanInfo(gbeanInfo);
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
        try {
            return kernel.invoke(name, operationName, params, signature);
        } catch (NoSuchOperationException e) {
            throw new ReflectionException(new NoSuchMethodException(e.getMessage()));
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(name.getCanonicalName());
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public boolean isRegistered(ObjectName name) {
        return kernel.isLoaded(name);
    }

    public Set queryNames(ObjectName pattern, QueryExp query) {
        Set names = kernel.listGBeans(pattern);
        if (query == null) {
            return names;
        }

        Set filteredNames = new HashSet(names.size());
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            // this must be done for each objectName applied
            query.setMBeanServer(this);

            ObjectName name = (ObjectName) iterator.next();
            try {
                if (query.apply(name)) {
                    filteredNames.add(name);
                }
            } catch (Exception e) {
                // reject any name that threw an exception
            }
        }
        return filteredNames;
    }

    public Set queryMBeans(ObjectName pattern, QueryExp query) {
        Set names = queryNames(pattern, query);
        Set objectInstances = new HashSet(names.size());
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName name = (ObjectName) iterator.next();
            try {
                objectInstances.add(getObjectInstance(name));
            } catch (InstanceNotFoundException e) {
                // ignore
            }
        }
        return objectInstances;
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, MBeanException {
        String attributeName = attribute.getName();
        Object attributeValue = attribute.getValue();
        try {
            kernel.setAttribute(name, attributeName, attributeValue);
        } catch (NoSuchAttributeException e) {
            throw new AttributeNotFoundException(attributeName);
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(name.getCanonicalName());
        } catch (InternalKernelException e) {
            throw new MBeanException(unwrapInternalKernelException(e));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        AttributeList set = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            String attributeName = attribute.getName();
            Object attributeValue = attribute.getValue();
            try {
                kernel.setAttribute(name, attributeName, attributeValue);
                set.add(attribute);
            } catch (NoSuchAttributeException e) {
                // ignored - caller will see value was not set because this attribute will not be in the attribute list
            } catch (GBeanNotFoundException e) {
                throw new InstanceNotFoundException(name.getCanonicalName());
            } catch (InternalKernelException e) {
                throw new ReflectionException(unwrapInternalKernelException(e));
            } catch (Exception e) {
                // ignored - caller will see value was not set because this attribute will not be in the attribute list
            }
        }
        return set;
    }

    public String[] getDomains() {
        Set domains = new HashSet();
        Set names = kernel.listGBeans((ObjectName)null);
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            ObjectName objectName = (ObjectName) iterator.next();
            domains.add(objectName.getDomain());
        }
        return (String[]) domains.toArray(new String[domains.size()]);
    }

    public ObjectInstance getObjectInstance(ObjectName objectName) throws InstanceNotFoundException {
        try {
            GBeanInfo gbeanInfo = kernel.getGBeanInfo(objectName);
            return new ObjectInstance(objectName, gbeanInfo.getClassName());
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(objectName.getCanonicalName());
        }
    }

    public ClassLoader getClassLoaderFor(ObjectName objectName) throws InstanceNotFoundException {
        try {
            return kernel.getClassLoaderFor(objectName);
        } catch (GBeanNotFoundException e) {
            throw new InstanceNotFoundException(objectName.getCanonicalName());
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

    public ObjectInputStream deserialize(String s, ObjectName objectName, byte[] bytes) throws InstanceNotFoundException, OperationsException, ReflectionException {
        throw new SecurityException("Operation not allowed");
    }

    public ObjectInputStream deserialize(String s, byte[] bytes) throws OperationsException, ReflectionException {
        throw new SecurityException("Operation not allowed");
    }

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
