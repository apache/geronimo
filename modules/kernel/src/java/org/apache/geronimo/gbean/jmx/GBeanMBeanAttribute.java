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

package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.management.MBeanAttributeInfo;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public class GBeanMBeanAttribute {
    private static final Log log = LogFactory.getLog(GBeanMBeanAttribute.class);

    private final GBeanMBean gmbean;

    private final String name;

    private final Class type;

    private final boolean readable;

    private final MethodInvoker getInvoker;

    private final boolean writable;

    private final MethodInvoker setInvoker;

    private final boolean isConstructorArg;

    private final boolean persistent;

    private final MBeanAttributeInfo mbeanAttributeInfo;

    private Object persistentValue;

    /**
     * Is this a special attribute like objectName, classLoader or gbeanContext?
     * Special attributes are injected at startup just like persistent attrubutes, but are
     * otherwise unmodifiable.
     */
    private final boolean special;

    GBeanMBeanAttribute(GBeanMBeanAttribute attribute, GBeanMBean gmbean, String name, Class type, MethodInvoker getInvoker) {
        if (gmbean == null || name == null || type == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }

        // if we have an attribute verify the gmbean, namd and types match
        if (attribute != null) {
            assert (gmbean == attribute.gmbean);
            assert (name.equals(attribute.name));
            if (type != attribute.type) {
                throw new InvalidConfigurationException("Special attribute " + name +
                        " must have the type " + type.getName() + ", but is " +
                        attribute.type.getName() + ": targetClass=" + gmbean.getType().getName());
            }
            if (attribute.isPersistent()) {
                throw new InvalidConfigurationException("Special attributes must not be persistent:" +
                        " name=" + name + ", targetClass=" + gmbean.getType().getName());
            }
        }

        this.gmbean = gmbean;
        this.name = name;
        this.type = type;
        if (getInvoker != null) {
            this.getInvoker = getInvoker;
        } else if (attribute != null) {
            this.getInvoker = attribute.getInvoker;
        } else {
            this.getInvoker = null;
        }
        this.readable = (this.getInvoker != null);
        this.writable = false;
        if (attribute != null) {
            this.setInvoker = attribute.setInvoker;
            this.isConstructorArg = attribute.isConstructorArg;
        } else {
            this.setInvoker = null;
            this.isConstructorArg = false;
        }
        this.persistent = false;
        this.special = true;
        if (this.getInvoker == null) {
            this.mbeanAttributeInfo = null;
        } else {
            this.mbeanAttributeInfo = new MBeanAttributeInfo(name, type.getName(), null, readable, writable, type == Boolean.TYPE);
        }
    }


    GBeanMBeanAttribute(GBeanMBean gmbean, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker) {
        if (gmbean == null || name == null || type == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }
        if (getInvoker == null && setInvoker == null) {
            throw new InvalidConfigurationException("An attribute must be readable, writable, or persistent: +"
                    + " name=" + name + ", targetClass=" + gmbean.getType().getName());
        }
        this.gmbean = gmbean;
        this.name = name;
        this.type = type;
        this.readable = (getInvoker != null);
        this.getInvoker = getInvoker;
        this.writable = (setInvoker != null);
        this.setInvoker = setInvoker;
        this.isConstructorArg = false;
        this.persistent = false;
        if (!readable && !writable) {
            this.mbeanAttributeInfo = null;
        } else {
            this.mbeanAttributeInfo = new MBeanAttributeInfo(name, type.getName(), null, readable, writable, type == Boolean.TYPE);
        }
        special = false;
    }

    public GBeanMBeanAttribute(GBeanMBean gmbean, GAttributeInfo attributeInfo) throws InvalidConfigurationException {
        this(gmbean, attributeInfo, false);
    }

    public GBeanMBeanAttribute(GBeanMBean gmbean, GAttributeInfo attributeInfo, boolean isConstructorArg) throws InvalidConfigurationException {
        if (gmbean == null || attributeInfo == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }
        if (attributeInfo.isReadable() == Boolean.FALSE && attributeInfo.isWritable() == Boolean.FALSE && !attributeInfo.isPersistent()) {
            throw new InvalidConfigurationException("An attribute must be readable, writable, or persistent: +"
                    + " name=" + attributeInfo.getName() + " targetClass=" + gmbean.getType().getName());
        }
        this.gmbean = gmbean;
        this.name = attributeInfo.getName();
        this.isConstructorArg = isConstructorArg;
        try {
            this.type = ClassLoading.loadClass(attributeInfo.getType(), gmbean.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load attribute class: " + attributeInfo.getType());
        }
        this.persistent = attributeInfo.isPersistent();

        boolean isIs;

        // If attribute is persistent or not tagged as unreadable, search for a
        // getter method
        if (attributeInfo instanceof DynamicGAttributeInfo) {
            readable = attributeInfo.isReadable().booleanValue();
            if (readable) {
                getInvoker = new DynamicGetterMethodInvoker(name);
            } else {
                getInvoker = null;
            }
            writable = attributeInfo.isWritable().booleanValue();
            if (writable) {
                setInvoker = new DynamicSetterMethodInvoker(name);
            } else {
                setInvoker = null;
            }
            isIs = false;
        } else {
            Method getterMethod = null;
            if (attributeInfo.isPersistent() || attributeInfo.isReadable() != Boolean.FALSE) {
                getterMethod = searchForGetter(gmbean, attributeInfo, type);
            }
            if (getterMethod != null) {
                getInvoker = new FastMethodInvoker(getterMethod);

                // this attribute is readable as long as it was not explicitly
                // tagged as unreadable
                readable = attributeInfo.isReadable() != Boolean.FALSE;
            } else {
                getInvoker = null;
                readable = false;
            }

            // If attribute is persistent or not tagged as unwritable, search
            // for a setter method
            Method setterMethod = null;
            if (attributeInfo.isPersistent() || attributeInfo.isWritable() != Boolean.FALSE) {
                setterMethod = searchForSetter(gmbean, attributeInfo, type);
            }
            if (setterMethod != null) {
                setInvoker = new FastMethodInvoker(setterMethod);

                // this attribute is writable as long as it was not explicitly
                // tagged as unwritable
                writable = attributeInfo.isWritable() != Boolean.FALSE;
                isIs = setterMethod.getName().startsWith("is");
            } else {
                setInvoker = null;
                writable = false;
                isIs = false;
            }
        }

        mbeanAttributeInfo = new MBeanAttributeInfo(attributeInfo.getName(), type.getName(), null, readable, writable, isIs);

        if (persistent && type.isPrimitive()) {
            if (type == Boolean.TYPE) {
                persistentValue = Boolean.FALSE;
            } else if (type == Byte.TYPE) {
                persistentValue = new Byte((byte) 0);
            } else if (type == Short.TYPE) {
                persistentValue = new Short((short) 0);
            } else if (type == Integer.TYPE) {
                persistentValue = new Integer(0);
            } else if (type == Long.TYPE) {
                persistentValue = new Long(0);
            } else if (type == Character.TYPE) {
                persistentValue = new Character((char) 0);
            } else if (type == Float.TYPE) {
                persistentValue = new Float(0);
            } else /** if (type == Double.TYPE) */ {
                persistentValue = new Double(0);
            }
        }
        special = false;
    }

    public String getName() {
        return name;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public Class getType() {
        return type;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public MBeanAttributeInfo getMBeanAttributeInfo() {
        return mbeanAttributeInfo;
    }

    public void online() throws Exception {
        // if this is a persistent attirubte and was not set via a constructor
        // set the value into the gbean
        if ((persistent || special) && !isConstructorArg && setInvoker != null) {
            try {
                assert gmbean.getTarget() != null : "online() invoked, however the corresponding GBeanMBean is " +
                        "not fully initialized (perhaps online() has been called directly instead by a Kernel)";
                setInvoker.invoke(gmbean.getTarget(), new Object[]{persistentValue});
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof Exception) {
                    throw (Exception) targetException;
                } else if (targetException instanceof Error) {
                    throw (Error) targetException;
                }
                throw e;
            }
        }
    }

    public void offline() {
        if (persistent && getInvoker != null) {
            try {
                persistentValue = getInvoker.invoke(gmbean.getTarget(), null);
            } catch (Throwable throwable) {
                log.error("Could not get the current value of persistent attribute while going offline.  The "
                        + "persistent attribute will not reflect the current state attribute. " + getDescription(), throwable);
            }
        }
    }

    public Object getValue() throws ReflectionException {
        if (gmbean.isOffline()) {
            if (persistent || special) {
                return persistentValue;
            } else {
                throw new IllegalStateException("Only persistent or special attributes can be accessed while offline. " + getDescription());
            }
        } else {
            if (!readable) {
                if (persistent) {
                    throw new IllegalStateException("This persistent attribute is not accessible while online. " + getDescription());
                } else {
                    throw new IllegalArgumentException("This attribute is not readable. " + getDescription());
                }
            }
            try {
                Object value = getInvoker.invoke(gmbean.getTarget(), null);
                return value;
            } catch (Throwable throwable) {
                throw new ReflectionException(new InvocationTargetException(throwable));
            }
        }
    }

    public Object getPersistentValue() {
        if (!persistent) {
        }
        if (getInvoker != null && gmbean.getTarget() != null) {
            try {
                persistentValue = getInvoker.invoke(gmbean.getTarget(), null);
            } catch (Throwable throwable) {
                log.error("Could not get the current value of persistent attribute.  The persistent " +
                        "attribute will not reflect the current state attribute. " + getDescription(), throwable);
            }
        }
        return persistentValue;
    }

    public void setValue(Object value) throws ReflectionException {
        if (gmbean.isOffline()) {
            if (persistent || special) {
                if (value == null && type.isPrimitive()) {
                    throw new IllegalArgumentException("Cannot assign null to a primitive attribute. " + getDescription());
                }
                // @todo actually check type
                this.persistentValue = value;
            } else {
                throw new IllegalStateException("Only persistent attributes can be modified while offline. " + getDescription());
            }
        } else {
            if (!writable) {
                if (persistent) {
                    throw new IllegalStateException("This persistent attribute is not modifable while online. " + getDescription());
                } else {
                    throw new IllegalArgumentException("This attribute is not writable. " + getDescription());
                }
            }
            if (value == null && type.isPrimitive()) {
                throw new IllegalArgumentException("Cannot assign null to a primitive attribute. " + getDescription());
            }
            // @todo actually check type
            try {
                setInvoker.invoke(gmbean.getTarget(), new Object[]{value});
            } catch (Throwable throwable) {
                throw new ReflectionException(new InvocationTargetException(throwable));
            }
        }
    }

    private String getDescription() {
        return "Attribute Name: " + getName() + ", Type: " + getType() + ", GBean: " + gmbean.getName();
    }

    private static Method searchForGetter(GBeanMBean gMBean, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        Method getterMethod = null;
        if (attributeInfo.getGetterName() == null) {
            // no explicit name give so we must search for a name
            String getterName = "get" + attributeInfo.getName();
            String isName = "is" + attributeInfo.getName();
            Method[] methods = gMBean.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getParameterTypes().length == 0 && methods[i].getReturnType() != Void.TYPE
                        && (getterName.equalsIgnoreCase(methods[i].getName()) || isName.equalsIgnoreCase(methods[i].getName()))) {

                    // found it
                    getterMethod = methods[i];
                    break;
                }
            }
        } else {
            // we have an explicit name, so no searching is necessary
            try {
                getterMethod = gMBean.getType().getMethod(attributeInfo.getGetterName(), null);
                if (getterMethod.getReturnType() == Void.TYPE) {
                    throw new InvalidConfigurationException("Getter method return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gMBean.getType().getName());
                }
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // if the return type of the getter doesn't match, throw an exception
        if (getterMethod != null && !type.equals(getterMethod.getReturnType())) {
            throw new InvalidConfigurationException("Incorrect return type for getter method:" +
                    " name=" + attributeInfo.getName() +
                    ", targetClass=" + gMBean.getType().getName() +
                    ", getter type=" + getterMethod.getReturnType() +
                    ", expected type=" + type.getName());
        }

        // if this attribute was explicity tagged as being readable but there is not getter
        if (getterMethod == null && attributeInfo.isReadable() == Boolean.TRUE) {
            throw new InvalidConfigurationException("Getter method not found on target:" +
                    " name=" + attributeInfo.getName() +
                    ", type=" + type.getName() +
                    ", targetClass=" + gMBean.getType().getName());
        }

        return getterMethod;
    }

    private static Method searchForSetter(GBeanMBean gMBean, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        if (attributeInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + attributeInfo.getName();
            Method[] methods = gMBean.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getParameterTypes()[0].equals(type) &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // we have an explicit name, so no searching is necessary
            try {
                Method method = gMBean.getType().getMethod(attributeInfo.getSetterName(), new Class[]{type});
                if (method.getReturnType() != Void.TYPE) {
                    throw new InvalidConfigurationException("Setter method must return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gMBean.getType().getName());
                }
                return method;
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // An attribute must have a setter if it was explicitly tagged as
        // writable or if it is persistent and it is not a constructor arg
        // (if it is persistent we must have a way to set the data into the
        // instance)
        if (attributeInfo.isWritable() == Boolean.TRUE) {
            throw new InvalidConfigurationException("Setter method not found on target:" +
                    " name=" + attributeInfo.getName() +
                    ", type=" + type.getName() +
                    ", targetClass=" + gMBean.getType().getName());
        }

        // a setter is not necessary for this attribute
        return null;
    }

    private static final class DynamicGetterMethodInvoker implements MethodInvoker {
        private final String name;

        public DynamicGetterMethodInvoker(String name) {
            this.name = name;
        }

        public Object invoke(Object target, Object[] arguments) throws Exception {
            return ((DynamicGBean) target).getAttribute(name);
        }
    }

    private static final class DynamicSetterMethodInvoker implements MethodInvoker {
        private final String name;

        public DynamicSetterMethodInvoker(String name) {
            this.name = name;
        }

        public Object invoke(Object target, Object[] arguments) throws Exception {
            ((DynamicGBean) target).setAttribute(name, arguments[0]);
            return null;
        }
    }
}
