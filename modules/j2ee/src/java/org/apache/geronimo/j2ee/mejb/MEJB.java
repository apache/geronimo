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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.ListenerNotFoundException;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.Management;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GNotificationInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelMBean;

/**
 * GBean implementing Management interface and supplying proxies to act as the MEJB container.
 *
 * @version $Rev:  $ $Date:  $
 */
public class MEJB implements Management, ListenerRegistration {

    private final Kernel kernel;
    private static final ObjectName ALL_GBEANS_QUERY;

    static {
        try {
            ALL_GBEANS_QUERY = ObjectName.getInstance("*.*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    public MEJB(Kernel kernel) {
        this.kernel = kernel;
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException {
        try {
            return kernel.getAttribute(name, attribute);
        } catch (MBeanException e) {
            throw e;
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException, RemoteException {
        AttributeList attributeList = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];
            try {
                attributeList.add(i, new Attribute(attribute, kernel.getAttribute(name, attribute)));
            } catch (InstanceNotFoundException e) {
                throw e;
            } catch (ReflectionException e) {
                throw e;
            } catch (Exception e) {
                //ignore ?
            }
        }
        return attributeList;
    }

    public String getDefaultDomain() throws RemoteException {
        return kernel.getKernelName();
    }

    public Integer getMBeanCount() throws RemoteException {
        return new Integer(kernel.listGBeans(ALL_GBEANS_QUERY).size());
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException, ReflectionException, RemoteException {
        return mapToMBeanInfo(kernel.getGBeanInfo(name));
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException, RemoteException {
        try {
            return kernel.invoke(name, operationName, params, signature);
        } catch (MBeanException e) {
            throw e;
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public boolean isRegistered(ObjectName name) throws RemoteException {
        return kernel.isLoaded(name);
    }

    public Set queryNames(ObjectName name, QueryExp query) throws RemoteException {
        if (query != null) {
            throw new IllegalArgumentException("NYI");
        }
        return kernel.listGBeans(name);
    }

    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException {
        try {
            kernel.setAttribute(name, attribute.getName(), attribute.getValue());
        } catch (MBeanException e) {
            throw e;
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (InvalidAttributeValueException e) {
            throw e;
        } catch (ReflectionException e) {
            throw e;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException, RemoteException {
        AttributeList set = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            try {
                kernel.setAttribute(name, attribute.getName(), attribute.getValue());
                set.add(attribute);
            } catch (InstanceNotFoundException e) {
                throw e;
            } catch (ReflectionException e) {
                throw e;
            } catch (Exception e) {
                //ignore ?
            }
        }
        return set;
    }

    public ListenerRegistration getListenerRegistry() throws RemoteException {
        throw new RuntimeException("NYI");
    }


    //ListenerRegistration implementation
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, RemoteException {
        try {
            kernel.invoke(name, "addNotificationListener", new Object[]{listener, filter, handback}, new String[]{NotificationListener.class.getName(), NotificationFilter.class.getName(), Object.class.getName()});
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, RemoteException {
        try {
            kernel.invoke(name, "removeNotificationListener", new Object[]{listener}, new String[]{NotificationListener.class.getName()});
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //EJBObject implementation

    public EJBHome getEJBHome() throws RemoteException {
        return null;
    }

    public Handle getHandle() throws RemoteException {
        return null;
    }

    public Object getPrimaryKey() throws RemoteException {
        return null;
    }

    public boolean isIdentical(EJBObject obj) throws RemoteException {
        return false;
    }

    public void remove() throws RemoteException, RemoveException {

    }


    private MBeanInfo mapToMBeanInfo(GBeanInfo gBeanInfo) {
        String className = gBeanInfo.getClassName();
        String description = "No description available";
        Set gbeanAttributes = gBeanInfo.getAttributes();
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[gbeanAttributes.size()];
        int a = 0;
        for (Iterator iterator = gbeanAttributes.iterator(); iterator.hasNext();) {
            GAttributeInfo gAttributeInfo = (GAttributeInfo) iterator.next();
            attributes[a] = new MBeanAttributeInfo(gAttributeInfo.getName(), "no description available", gAttributeInfo.getType(), gAttributeInfo.isReadable().booleanValue(), gAttributeInfo.isWritable().booleanValue(), gAttributeInfo.getGetterName().startsWith("is"));
            a++;
        }

        //we don't expose managed constructors
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[0];

        Set gbeanOperations = gBeanInfo.getOperations();
        MBeanOperationInfo[] operations = new MBeanOperationInfo[gbeanOperations.size()];
        int o = 0;
        for (Iterator iterator = gbeanOperations.iterator(); iterator.hasNext();) {
            GOperationInfo gOperationInfo = (GOperationInfo) iterator.next();
            //list of class names
            List gparameters = gOperationInfo.getParameterList();
            MBeanParameterInfo[] parameters = new MBeanParameterInfo[gparameters.size()];
            int p = 0;
            for (Iterator piterator = gparameters.iterator(); piterator.hasNext();) {
                String type = (String) piterator.next();
                parameters[p] = new MBeanParameterInfo("parameter" + p, type, "no description available");
                p++;
            }
            operations[o] = new MBeanOperationInfo(gOperationInfo.getName(), "no description available", parameters, "java.lang.Object", MBeanOperationInfo.UNKNOWN);
            o++;
        }

        Set gnotifications = gBeanInfo.getNotifications();
        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[gnotifications.size()];
        int n = 0;
        for (Iterator iterator = gnotifications.iterator(); iterator.hasNext();) {
            GNotificationInfo gNotificationInfo = (GNotificationInfo) iterator.next();
            notifications[n] = new MBeanNotificationInfo((String[]) gNotificationInfo.getNotificationTypes().toArray(new String[gnotifications.size()]), gNotificationInfo.getName(), "no description available");
            n++;
        }

        MBeanInfo mbeanInfo = new MBeanInfo(className, description, attributes, constructors, operations, notifications);
        return mbeanInfo;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(MEJB.class.getName());
        infoBuilder.addAttribute("kernel", KernelMBean.class, false);
        infoBuilder.addInterface(Management.class);
        infoBuilder.addInterface(ListenerRegistration.class);

        infoBuilder.setConstructor(new String[]{"kernel"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
