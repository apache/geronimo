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

import java.util.Set;
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
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.Management;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanServerDelegate;
import org.apache.geronimo.management.J2EEManagedObject;

/**
 * GBean implementing Management interface and supplying proxies to act as the MEJB container.
 *
 * @version $Rev$ $Date$
 */
public class MEJB implements Management {
    private final MBeanServer mbeanServer;
    private final String objectName;

    public MEJB(String objectName, Kernel kernel) {
        mbeanServer = new MBeanServerDelegate(kernel);
        this.objectName = objectName;
    }

    public MBeanInfo getMBeanInfo(ObjectName objectName) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return mbeanServer.getMBeanInfo(objectName);
    }

    public String getDefaultDomain() {
        return mbeanServer.getDefaultDomain();
    }

    public Object getAttribute(ObjectName objectName, String s) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return mbeanServer.getAttribute(objectName, s);
    }

    public void setAttribute(ObjectName objectName, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        mbeanServer.setAttribute(objectName, attribute);
    }

    public AttributeList getAttributes(ObjectName objectName, String[] strings) throws InstanceNotFoundException, ReflectionException {
        return mbeanServer.getAttributes(objectName, strings);
    }

    public AttributeList setAttributes(ObjectName objectName, AttributeList attributeList) throws InstanceNotFoundException, ReflectionException {
        return mbeanServer.setAttributes(objectName, attributeList);
    }

    public Object invoke(ObjectName objectName, String s, Object[] objects, String[] strings) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return mbeanServer.invoke(objectName, s, objects, strings);
    }

    public Integer getMBeanCount() {
        return mbeanServer.getMBeanCount();
    }

    public boolean isRegistered(ObjectName objectName) {
        return mbeanServer.isRegistered(objectName);
    }

    public Set queryNames(ObjectName objectName, QueryExp queryExp) {
        return mbeanServer.queryNames(objectName, queryExp);
    }

    public ListenerRegistration getListenerRegistry() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }


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

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MEJB.class);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(Management.class);
        infoBuilder.addInterface(J2EEManagedObject.class);

        infoBuilder.setConstructor(new String[]{"objectName", "kernel"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
