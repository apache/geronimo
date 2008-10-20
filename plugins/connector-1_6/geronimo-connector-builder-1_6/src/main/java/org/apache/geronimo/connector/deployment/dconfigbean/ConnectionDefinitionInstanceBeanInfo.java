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
package org.apache.geronimo.connector.deployment.dconfigbean;

import java.beans.*;
import java.awt.*;

/**
 * @version $Revision: 1.0$
 */
public class ConnectionDefinitionInstanceBeanInfo implements BeanInfo {
    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by humans when using the bean.
     *
     * @return Index of default event in the EventSetDescriptor array
     *         returned by getEventSetDescriptors.
     *         <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return -1;
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     *
     * @return Index of default property in the PropertyDescriptor array
     *         returned by getPropertyDescriptors.
     *         <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return -1;
    }

    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p/>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p/>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p/>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param iconKind The kind of icon requested.  This should be
     *                 one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32,
     *                 ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return An image object representing the requested icon.  May
     *         return null if no suitable icon is available.
     */
    public Image getIcon(int iconKind) {
        return null;
    }

    /**
     * Gets the beans <code>BeanDescriptor</code>.
     *
     * @return A BeanDescriptor providing overall information about
     *         the bean, such as its displayName, its customizer, etc.  May
     *         return null if the information should be obtained by automatic
     *         analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConnectionDefinitionInstance.class);
        bd.setDisplayName("Geronimo Connection Configurations");
        bd.setShortDescription("The Resource Adapter defines what type of connections may be made (e.g. to a database, or to JMS).  These entries configure a specific connection instance (to a specific database or JMS server).  This is done primarily by setting appropriate config properties.");
        return bd;
    }

    /**
     * This method allows a BeanInfo object to return an arbitrary collection
     * of other BeanInfo objects that provide additional information on the
     * current bean.
     * <P>
     * If there are conflicts or overlaps between the information provided
     * by different BeanInfo objects, then the current BeanInfo takes precedence
     * over the getAdditionalBeanInfo objects, and later elements in the array
     * take precedence over earlier ones.
     *
     * @return an array of BeanInfo objects.  May return null.
     */
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     *
     * @return An array of EventSetDescriptors describing the kinds of
     *         events fired by this bean.  May return null if the information
     *         should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return null;
    }

    /**
     * Gets the beans <code>MethodDescriptor</code>s.
     *
     * @return An array of MethodDescriptors describing the externally
     *         visible methods supported by this bean.  May return null if
     *         the information should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }

    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     *         properties supported by this bean.  May return null if the
     *         information should be obtained by automatic analysis.
     *         <p/>
     *         If a property is indexed, then its entry in the result array will
     *         belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     *         A client of getPropertyDescriptors can use "instanceof" to check
     *         if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("name", ConnectionDefinitionInstance.class);
            name.setDisplayName("Connection Name");
            name.setShortDescription("A name that identifies this connection.  It will be used by application resource references to map to this connection.");
            PropertyDescriptor jndiName = new PropertyDescriptor("globalJNDIName", ConnectionDefinitionInstance.class);
            jndiName.setDisplayName("Global JNDI Name");
            jndiName.setShortDescription("Where to register this connection in the global JNDI tree.  This is only necessary for non-J2EE application clients; it is not used for nornal resource references.");
            PropertyDescriptor configs = new PropertyDescriptor("configProperty", ConnectionDefinitionInstance.class, "getConfigProperty", null);
            configs.setDisplayName("Configuration Properties");
            configs.setShortDescription("The configuration properties that point this connection instance to a particular destination (database, JMS server, etc.).");
            return new PropertyDescriptor[]{
                name, jndiName, configs
            };
        } catch (IntrospectionException e) {
            throw new RuntimeException("Unable to configure bean properties", e);
        }
    }
}
