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
package org.apache.geronimo.j2ee.mejb;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.Management;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.jmx.GBeanJMXUtil;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;

/**
 * GBean implementing Management interface and supplying proxies to act as the MEJB container.
 *
 * @version $Rev:  $ $Date:  $
 */
public class MEJB implements Management {
    private final Kernel kernel;

    public MEJB(Kernel kernel) {
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
        return GBeanJMXUtil.toMBeanInfo(gbeanInfo);
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
        if (query != null) {
            throw new IllegalArgumentException("NYI");
        }
        Set names = kernel.listGBeans(pattern);
        if (query == null) {
            return names;
        }

        // todo this will not work for non MBean server based queries
        // dain: I think we could create an MBeanServer wraper around
        // kernel that passed though most operations to kernel and
        // threw an UnsupportedOperationException for the operations
        // that have no equivilent Kernel method.
        query.setMBeanServer(kernel.getMBeanServer());

        Set filteredNames = new HashSet(names.size());
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
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

    public ListenerRegistration getListenerRegistry() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }


//    //ListenerRegistration implementation
//    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
//        try {
//            kernel.invoke(name, "addNotificationListener", new Object[]{listener, filter, handback}, new String[]{NotificationListener.class.getName(), NotificationFilter.class.getName(), Object.class.getName()});
//        } catch (InstanceNotFoundException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException {
//        try {
//            kernel.invoke(name, "removeNotificationListener", new Object[]{listener}, new String[]{NotificationListener.class.getName()});
//        } catch (InstanceNotFoundException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    // EJBObject implementation
    public EJBHome getEJBHome() {
        return null;
    }

    public Handle getHandle() {
        return null;
    }

    public Object getPrimaryKey() {
        return null;
    }

    public boolean isIdentical(EJBObject obj) {
        return false;
    }

    public void remove() throws RemoveException {
    }

    private static Exception unwrapInternalKernelException(InternalKernelException e) {
        if (e.getCause() instanceof Exception) {
            return (Exception) e.getCause();
        }
        return e;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(MEJB.class.getName());
        infoBuilder.addAttribute("kernel", KernelMBean.class, false);
        infoBuilder.addInterface(Management.class);

        infoBuilder.setConstructor(new String[]{"kernel"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
