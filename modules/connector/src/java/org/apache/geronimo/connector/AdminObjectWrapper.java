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

package org.apache.geronimo.connector;

import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.DynamicGBeanDelegate;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * Wrapper around AdminObject that exposes its config-properties as GBeanAttributes and
 * supplies a disconnectable proxy to bind in jndi.
 *
 * @version $Rev$ $Date$
 */
public class AdminObjectWrapper implements DynamicGBean {

    private final Class adminObjectInterface;
    private final Class adminObjectClass;

    private final DynamicGBeanDelegate delegate;
    private final Object adminObject;

    /**
     * Default constructor required when a class is used as a GBean Endpoint.
     */
    public AdminObjectWrapper() {
        adminObjectInterface = null;
        adminObjectClass = null;
        adminObject = null;
        delegate = null;
    }

    /**
     * Normal managed constructor.
     *
     * @param adminObjectInterface Interface the proxy will implement.
     * @param adminObjectClass Class of admin object to be wrapped.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public AdminObjectWrapper(final Class adminObjectInterface,
                              final Class adminObjectClass) throws IllegalAccessException, InstantiationException {
        this.adminObjectInterface = adminObjectInterface;
        this.adminObjectClass = adminObjectClass;
        adminObject = adminObjectClass.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(adminObject);
    }

    public Class getAdminObjectInterface() {
        return adminObjectInterface;
    }

    /**
     * Returns class of wrapped AdminObject.
     * @return class of wrapped AdminObject
     */
    public Class getAdminObjectClass() {
        return adminObjectClass;
    }

    /**
     * Returns disconnectable proxy for binding in jndi.
     * @return proxy implementing adminObjectInterface.
     */
    public Object getProxy() {
        return adminObject;
    }


    //DynamicGBean implementation

    /**
     * Delegating DynamicGBean getAttribute method.
     * @param name of attribute.
     * @return attribute value.
     * @throws Exception
     */
    public Object getAttribute(final String name) throws Exception {
        return delegate.getAttribute(name);
    }

    /**
     * Delegating DynamicGBean setAttribute method.
     * @param name of attribute.
     * @param value of attribute to be set.
     * @throws Exception
     */
    public void setAttribute(final String name, final Object value) throws Exception {
        delegate.setAttribute(name, value);
    }

    /**
     * no-op DynamicGBean method
     * @param name
     * @param arguments
     * @param types
     * @return nothing, there are no operations.
     * @throws Exception
     */
    public Object invoke(final String name, final Object[] arguments, final String[] types) throws Exception {
        //we have no dynamic operations.
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AdminObjectWrapper.class);
        infoFactory.addAttribute("adminObjectInterface", Class.class, true);
        infoFactory.addAttribute("adminObjectClass", Class.class, true);

        infoFactory.addOperation("getProxy");

        infoFactory.setConstructor(new String[]{
            "adminObjectInterface",
            "adminObjectClass",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
