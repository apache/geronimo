/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.JCAAdminObject;

import javax.management.ObjectName;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

/**
 * Wrapper around AdminObject that exposes its config-properties as GBeanAttributes and
 * supplies a disconnectable proxy to bind in jndi.
 *
 * @version $Rev$ $Date$
 */
public class AdminObjectWrapper implements DynamicGBean, JCAAdminObject {

    private final String adminObjectInterface;
    private final String adminObjectClass;

    private final DynamicGBeanDelegate delegate;
    private final Object adminObject;


    private final Kernel kernel;
    private final String objectName;

    /**
     * Default constructor required when a class is used as a GBean Endpoint.
     */
    public AdminObjectWrapper() {
        adminObjectInterface = null;
        adminObjectClass = null;
        adminObject = null;
        delegate = null;
        kernel = null;
        objectName = null;
    }

    /**
     * Normal managed constructor.
     *
     * @param adminObjectInterface Interface the proxy will implement.
     * @param adminObjectClass Class of admin object to be wrapped.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public AdminObjectWrapper(final String adminObjectInterface,
                              final String adminObjectClass,
                              final Kernel kernel,
                              final String objectName,
                              final ClassLoader cl) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.adminObjectInterface = adminObjectInterface;
        this.adminObjectClass = adminObjectClass;
        this.kernel = kernel;
        this.objectName = objectName;
        Class clazz = cl.loadClass(adminObjectClass);
        adminObject = clazz.newInstance();
        delegate = new DynamicGBeanDelegate();
        delegate.addAll(adminObject);
    }

    public String getAdminObjectInterface() {
        return adminObjectInterface;
    }

    /**
     * Returns class of wrapped AdminObject.
     * @return class of wrapped AdminObject
     */
    public String getAdminObjectClass() {
        return adminObjectClass;
    }

    /**
     * Returns disconnectable proxy for binding in jndi.
     * @return proxy implementing adminObjectInterface.
     */
    public Object $getResource() {
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

    /**
     * Gets the config properties in the form of a map where the key is the
     * property name and the value is property type (as a String not a Class).
     */
    public Map getConfigProperties() {
        String[] props = delegate.getProperties();
        Map map = new HashMap();
        for (int i = 0; i < props.length; i++) {
            String prop = props[i];
            if(prop.equals("logWriter")) {
                continue;
            }
            map.put(prop, delegate.getPropertyType(prop));
        }
        return map;
    }

    public void setConfigProperty(String property, Object value) throws Exception {
        Class cls = delegate.getPropertyType(property);
        if(value != null && value instanceof String && !cls.getName().equals("java.lang.String")) {
            if(cls.isPrimitive()) {
                if(cls.equals(int.class)) {
                    cls = Integer.class;
                } else if(cls.equals(boolean.class)) {
                    cls = Boolean.class;
                } else if(cls.equals(float.class)) {
                    cls = Float.class;
                } else if(cls.equals(double.class)) {
                    cls = Double.class;
                } else if(cls.equals(long.class)) {
                    cls = Long.class;
                } else if(cls.equals(short.class)) {
                    cls = Short.class;
                } else if(cls.equals(byte.class)) {
                    cls = Byte.class;
                } else if(cls.equals(char.class)) {
                    cls = Character.class;
                }
            }
            Constructor con = cls.getConstructor(new Class[]{String.class});
            value = con.newInstance(new Object[]{value});
        }
        kernel.setAttribute(ObjectName.getInstance(objectName), property, value);
    }

    public Object getConfigProperty(String property) throws Exception {
        return delegate.getAttribute(property);
    }

}
