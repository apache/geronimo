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

package org.apache.geronimo.gbean.runtime;

import java.lang.reflect.Method;

import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;

/**
 * @version $Rev$ $Date$
 */
public class GBeanAttribute {
    private final GBeanInstance gbeanInstance;

    private final String name;

    private final Class type;

    private final boolean readable;

    private final MethodInvoker getInvoker;

    private final boolean writable;

    private final MethodInvoker setInvoker;

    private final boolean isConstructorArg;

    private final boolean persistent;

    private Object persistentValue;

    /**
     * Is this a special attribute like objectName, classLoader or gbeanContext?
     * Special attributes are injected at startup just like persistent attrubutes, but are
     * otherwise unmodifiable.
     */
    private final boolean special;

    private final boolean framework;

    private final boolean dynamic;

    static GBeanAttribute createSpecialAttribute(GBeanAttribute attribute, GBeanInstance gbeanInstance, String name, Class type, Object value) {
        return new GBeanAttribute(attribute, gbeanInstance, name, type, value);
    }

    private GBeanAttribute(GBeanAttribute attribute, GBeanInstance gbeanInstance, String name, Class type, Object value) {
        this.special = true;
        this.framework = false;
        this.dynamic = false;

        if (gbeanInstance == null || name == null || type == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }

        // if we have an attribute verify the gbean instance, name and types match
        if (attribute != null) {
            assert (gbeanInstance == attribute.gbeanInstance);
            assert (name.equals(attribute.name));
            if (type != attribute.type) {
                throw new InvalidConfigurationException("Special attribute " + name +
                        " must have the type " + type.getName() + ", but is " +
                        attribute.type.getName() + ": targetClass=" + gbeanInstance.getType().getName());
            }
            if (attribute.isPersistent()) {
                throw new InvalidConfigurationException("Special attributes must not be persistent:" +
                        " name=" + name + ", targetClass=" + gbeanInstance.getType().getName());
            }
        }

        this.gbeanInstance = gbeanInstance;
        this.name = name;
        this.type = type;

        // getter
        this.getInvoker = null;
        this.readable = true;

        // setter
        if (attribute != null) {
            this.setInvoker = attribute.setInvoker;
            this.isConstructorArg = attribute.isConstructorArg;
        } else {
            this.setInvoker = null;
            this.isConstructorArg = false;
        }
        this.writable = false;

        // persistence
        this.persistent = false;
        initializePersistentValue(value);
    }

    static GBeanAttribute createFrameworkAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker) {
        return new GBeanAttribute(gbeanInstance, name, type, getInvoker, null, false, null);
    }

    static GBeanAttribute createFrameworkAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker, boolean persistent, Object persistentValue) {
        return new GBeanAttribute(gbeanInstance, name, type, getInvoker, setInvoker, persistent, persistentValue);
    }

    private GBeanAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker, boolean persistent, Object persistentValue) {
        this.special = false;
        this.framework = true;
        this.dynamic = false;

        if (gbeanInstance == null || name == null || type == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }

        this.gbeanInstance = gbeanInstance;
        this.name = name;
        this.type = type;

        // getter
        this.getInvoker = getInvoker;
        this.readable = (this.getInvoker != null);

        // setter
        this.setInvoker = setInvoker;
        this.isConstructorArg = false;
        this.writable = (this.setInvoker != null);

        // persistence
        this.persistent = persistent;
        initializePersistentValue(persistentValue);
    }

    public GBeanAttribute(GBeanInstance gbeanInstance, GAttributeInfo attributeInfo, boolean isConstructorArg) throws InvalidConfigurationException {
        this.special = false;
        this.framework = false;

        if (gbeanInstance == null || attributeInfo == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }
        if (attributeInfo.isReadable() == Boolean.FALSE && attributeInfo.isWritable() == Boolean.FALSE && !attributeInfo.isPersistent()) {
            throw new InvalidConfigurationException("An attribute must be readable, writable, or persistent: +"
                    + " name=" + attributeInfo.getName() + " targetClass=" + gbeanInstance.getType().getName());
        }
        this.gbeanInstance = gbeanInstance;
        this.name = attributeInfo.getName();
        this.isConstructorArg = isConstructorArg;
        try {
            this.type = ClassLoading.loadClass(attributeInfo.getType(), gbeanInstance.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load attribute class: " + attributeInfo.getType());
        }
        this.persistent = attributeInfo.isPersistent();

        // If attribute is persistent or not tagged as unreadable, search for a
        // getter method
        if (attributeInfo instanceof DynamicGAttributeInfo) {
            this.dynamic = true;
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
        } else {
            this.dynamic = false;
            Method getterMethod = null;
            if (attributeInfo.isPersistent() || attributeInfo.isReadable() != Boolean.FALSE) {
                getterMethod = searchForGetter(gbeanInstance, attributeInfo, type);
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
                setterMethod = searchForSetter(gbeanInstance, attributeInfo, type);
            }
            if (setterMethod != null) {
                setInvoker = new FastMethodInvoker(setterMethod);

                // this attribute is writable as long as it was not explicitly
                // tagged as unwritable
                writable = attributeInfo.isWritable() != Boolean.FALSE;
            } else {
                setInvoker = null;
                writable = false;
            }
        }

        initializePersistentValue(null);
    }

    private void initializePersistentValue(Object value) {
        if (persistent || special) {
            if (value == null && type.isPrimitive()) {
                if (type == Boolean.TYPE) {
                    value = Boolean.FALSE;
                } else if (type == Byte.TYPE) {
                    value = new Byte((byte) 0);
                } else if (type == Short.TYPE) {
                    value = new Short((short) 0);
                } else if (type == Integer.TYPE) {
                    value = new Integer(0);
                } else if (type == Long.TYPE) {
                    value = new Long(0);
                } else if (type == Character.TYPE) {
                    value = new Character((char) 0);
                } else if (type == Float.TYPE) {
                    value = new Float(0);
                } else /** if (type == Double.TYPE) */ {
                    value = new Double(0);
                }
            }
            persistentValue = value;
        }
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

    public boolean isFramework() {
        return framework;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isSpecial() {
        return special;
    }

    public void inject() throws Exception {
        if ((persistent || special) && !isConstructorArg && writable) {
            setValue(persistentValue);
        }
    }

    public Object getPersistentValue() {
        if (!persistent && !special) {
            throw new IllegalStateException("Attribute is not persistent " + getDescription());
        }
        return persistentValue;
    }

    public void setPersistentValue(Object persistentValue) {
        if (!persistent && !special) {
            throw new IllegalStateException("Attribute is not persistent " + getDescription());
        }

        if (persistentValue == null && type.isPrimitive()) {
            throw new IllegalArgumentException("Cannot assign null to a primitive attribute. " + getDescription());
        }

        // @todo actually check type
        this.persistentValue = persistentValue;
    }

    public Object getValue() throws Exception {
        if (!readable) {
            if (persistent) {
                throw new IllegalStateException("This persistent attribute is not accessible while started. " + getDescription());
            } else {
                throw new IllegalStateException("This attribute is not readable. " + getDescription());
            }
        }

        if (special) {
            return persistentValue;
        }
        
        // get the target to invoke
        Object target = gbeanInstance.getTarget();
        if (target == null && !framework) {
            throw new IllegalStateException("GBeanMBean does not have a target instance to invoke. " + getDescription());
        }

        // call the getter
        Object value = getInvoker.invoke(target, null);
        return value;
    }

    public void setValue(Object value) throws Exception {
        if (!writable) {
            if (persistent) {
                throw new IllegalStateException("This persistent attribute is not modifable while running. " + getDescription());
            } else {
                throw new IllegalStateException("This attribute is not writable. " + getDescription());
            }
        }

        // the value can not be null for primitives
        if (value == null && type.isPrimitive()) {
            throw new IllegalArgumentException("Cannot assign null to a primitive attribute. " + getDescription());
        }

        // @todo actually check type

        // get the target to invoke
        Object target = gbeanInstance.getTarget();
        if (target == null && !framework) {
            throw new IllegalStateException("GBeanMBean does not have a target instance to invoke. " + getDescription());
        }

        // call the setter
        setInvoker.invoke(target, new Object[]{value});
    }

    public String getDescription() {
        return "Attribute Name: " + getName() + ", Type: " + getType() + ", GBeanInstance: " + gbeanInstance.getName();
    }

    private static Method searchForGetter(GBeanInstance gbeanInstance, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        Method getterMethod = null;
        if (attributeInfo.getGetterName() == null) {
            // no explicit name give so we must search for a name
            String getterName = "get" + attributeInfo.getName();
            String isName = "is" + attributeInfo.getName();
            Method[] methods = gbeanInstance.getType().getMethods();
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
                getterMethod = gbeanInstance.getType().getMethod(attributeInfo.getGetterName(), null);
                if (getterMethod.getReturnType() == Void.TYPE) {
                    throw new InvalidConfigurationException("Getter method return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gbeanInstance.getType().getName());
                }
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // if the return type of the getter doesn't match, throw an exception
        if (getterMethod != null && !type.equals(getterMethod.getReturnType())) {
            throw new InvalidConfigurationException("Incorrect return type for getter method:" +
                    " name=" + attributeInfo.getName() +
                    ", targetClass=" + gbeanInstance.getType().getName() +
                    ", getter type=" + getterMethod.getReturnType() +
                    ", expected type=" + type.getName());
        }

        // if this attribute was explicity tagged as being readable but there is not getter
        if (getterMethod == null && attributeInfo.isReadable() == Boolean.TRUE) {
            throw new InvalidConfigurationException("Getter method not found on target:" +
                    " name=" + attributeInfo.getName() +
                    ", type=" + type.getName() +
                    ", targetClass=" + gbeanInstance.getType().getName());
        }

        return getterMethod;
    }

    private static Method searchForSetter(GBeanInstance gbeanInstance, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        if (attributeInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + attributeInfo.getName();
            Method[] methods = gbeanInstance.getType().getMethods();
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
                Method method = gbeanInstance.getType().getMethod(attributeInfo.getSetterName(), new Class[]{type});
                if (method.getReturnType() != Void.TYPE) {
                    throw new InvalidConfigurationException("Setter method must return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gbeanInstance.getType().getName());
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
                    ", targetClass=" + gbeanInstance.getType().getName());
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
