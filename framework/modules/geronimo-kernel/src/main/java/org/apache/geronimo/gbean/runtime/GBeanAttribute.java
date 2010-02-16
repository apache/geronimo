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

package org.apache.geronimo.gbean.runtime;

import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.annotation.EncryptionSetting;
import org.apache.geronimo.kernel.ClassLoading;

import java.lang.reflect.Method;

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

    private final boolean persistent;

    private final boolean manageable;

    private final EncryptionSetting encrypted;

    private Object persistentValue;
    /**
     * Is this a special attribute like objectName, classLoader or gbeanContext?
     * Special attributes are injected at startup just like persistent attrubutes, but are
     * otherwise unmodifiable.
     */
    private final boolean special;

    private final boolean framework;

    private final boolean dynamic;

    private final GAttributeInfo attributeInfo;

    static GBeanAttribute createSpecialAttribute(GBeanAttribute attribute, GBeanInstance gbeanInstance, String name, Class type, Object value) {
        return new GBeanAttribute(attribute, gbeanInstance, name, type, value);
    }

    private GBeanAttribute(GBeanAttribute attribute, GBeanInstance gbeanInstance, String name, Class type, Object value) {
        this.special = true;
        this.persistentValue = value;

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
        } else {
            this.setInvoker = null;
        }
        this.writable = false;

        // persistence
        this.persistent = false;

        // not manageable
        this.manageable = false;

        // special attributes are not encrypted
        this.encrypted = EncryptionSetting.PLAINTEXT;

        // create an attribute info for this gbean
        if (attribute != null) {
            GAttributeInfo attributeInfo = attribute.getAttributeInfo();
            this.attributeInfo = new GAttributeInfo(this.name,
                    this.type.getName(),
                    this.persistent,
                    this.manageable,
                    this.encrypted,
                    this.readable,
                    this.writable,
                    attributeInfo.getGetterName(),
                    attributeInfo.getSetterName());
        } else {
            this.attributeInfo = new GAttributeInfo(this.name,
                    this.type.getName(),
                    this.persistent,
                    this.manageable,
                    this.encrypted,
                    this.readable,
                    this.writable,
                    null,
                    null);
        }
    }

    static GBeanAttribute createFrameworkAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker) {
        return new GBeanAttribute(gbeanInstance, name, type, getInvoker, null, false, null, true);
    }

    static GBeanAttribute createFrameworkAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker, boolean persistent, Object persistentValue, boolean manageable) {
        return new GBeanAttribute(gbeanInstance, name, type, getInvoker, setInvoker, persistent, persistentValue, manageable);
    }

    private GBeanAttribute(GBeanInstance gbeanInstance, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker, boolean persistent, Object persistentValue, boolean manageable) {
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
        this.writable = (this.setInvoker != null);

        // persistence
        this.persistent = persistent;

        // manageable
        this.manageable = manageable;

        // create an attribute info for this gbean
        attributeInfo = new GAttributeInfo(this.name,
                this.type.getName(),
                this.persistent,
                this.manageable,
                this.readable,
                this.writable,
                null,
                null);

        this.encrypted = attributeInfo.getEncryptedSetting();
        this.persistentValue = encrypted.decrypt(persistentValue);
    }

    public GBeanAttribute(GBeanInstance gbeanInstance, GAttributeInfo attributeInfo) throws InvalidConfigurationException {
        this.special = false;
        this.framework = false;

        if (gbeanInstance == null || attributeInfo == null) {
            throw new IllegalArgumentException("null param(s) supplied");
        }
        this.gbeanInstance = gbeanInstance;
        this.attributeInfo = attributeInfo;
        this.name = attributeInfo.getName();
        try {
            this.type = ClassLoading.loadClass(attributeInfo.getType(), gbeanInstance.getBundle());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load attribute class: " + attributeInfo.getType(), e);
        }
        this.persistent = attributeInfo.isPersistent();
        this.manageable = attributeInfo.isManageable();
        this.encrypted = attributeInfo.getEncryptedSetting();

        readable = attributeInfo.isReadable();
        writable = attributeInfo.isWritable();

        // If attribute is persistent or not tagged as unreadable, search for a
        // getter method
        if (attributeInfo instanceof DynamicGAttributeInfo) {
            this.dynamic = true;
            if (readable) {
                getInvoker = new DynamicGetterMethodInvoker(name);
            } else {
                getInvoker = null;
            }
            if (writable) {
                setInvoker = new DynamicSetterMethodInvoker(name);
            } else {
                setInvoker = null;
            }
        } else {
            this.dynamic = false;
            if (attributeInfo.getGetterName() != null) {
                try {
                    String getterName = attributeInfo.getGetterName();
                    Method getterMethod = gbeanInstance.getType().getMethod(getterName, (Class[])null);

                    if (!getterMethod.getReturnType().equals(type)) {
                        if (getterMethod.getReturnType().getName().equals(type.getName())) {
                            throw new InvalidConfigurationException("Getter return type in wrong classloader: type: " + type + " wanted in classloader: " + type.getClassLoader() + " actual: " + getterMethod.getReturnType().getClassLoader());
                        } else {
                            throw new InvalidConfigurationException("Getter method of wrong type: " + getterMethod.getReturnType() + " expected " + getDescription());
                        }
                    }
                    if (AbstractGBeanReference.NO_PROXY) {
                        getInvoker = new ReflectionMethodInvoker(getterMethod);
                    } else {
                        getInvoker = new FastMethodInvoker(getterMethod);
                    }
                } catch (NoSuchMethodException e) {
                    throw new InvalidConfigurationException("Getter method not found " + getDescription(), e);
                } catch (NoClassDefFoundError e) {
                    throw new InvalidConfigurationException("Getter method not found " + getDescription(), e);
                }
            } else {
                getInvoker = null;
            }

            // If attribute is persistent or not tagged as unwritable, search for a setter method
            if (attributeInfo.getSetterName() != null) {
                try {
                    String setterName = attributeInfo.getSetterName();
                    Method setterMethod = gbeanInstance.getType().getMethod(setterName, type);
                    if (AbstractGBeanReference.NO_PROXY) {
                        setInvoker = new ReflectionMethodInvoker(setterMethod);
                    } else {
                        setInvoker = new FastMethodInvoker(setterMethod);
                    }
                } catch (NoSuchMethodException e) {
                    throw new InvalidConfigurationException("Setter method not found " + getDescription(), e);
                }
            } else {
                setInvoker = null;
            }
        }
    }

    public String getName() {
        return name;
    }

    public GAttributeInfo getAttributeInfo() {
        return attributeInfo;
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

    public boolean isManageable() {
        return manageable;
    }

    public boolean isEncrypted() {
        return encrypted == EncryptionSetting.ENCRYPTED;
    }

    public EncryptionSetting getEncryptionSetting() {
        return encrypted;
    }

    public boolean isSpecial() {
        return special;
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
        this.persistentValue = encrypted.decrypt(persistentValue);
    }

    public Object getValue(Object target) throws Exception {
        if (!readable) {
            if (persistent) {
                return persistentValue;
            } else {
                throw new IllegalStateException("This attribute is not readable. " + getDescription());
            }
        }

        if (special) {
            return persistentValue;
        }

        // get the target to invoke
        if (target == null && !framework) {
            throw new IllegalStateException("GBean does not have a target instance to invoke. " + getDescription());
        }

        // call the getter
        return getInvoker.invoke(target, null);
    }

    public void setValue(Object target, Object value) throws Exception {
        if (!writable) {
            if (persistent) {
                throw new IllegalStateException("This persistent attribute is not modifable while the gbean is running. " + getDescription());
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
        if (target == null && !framework) {
            throw new IllegalStateException("GBean does not have a target instance to invoke. " + getDescription());
        }

        // call the setter
        value = encrypted.decrypt(value);
        setInvoker.invoke(target, new Object[]{value});
    }

    public String getDescription() {
        return "Attribute Name: " + getName() + ", Type: " + getType() + ", GBeanInstance: " + gbeanInstance.getName();
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

    public void inject(Object target) throws Exception {
        if ((persistent || special) && writable && null != persistentValue) {
            setValue(target, persistentValue);
        }
    }

}
