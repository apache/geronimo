/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/16 23:31:21 $
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

    public GBeanMBeanAttribute(GBeanMBean gmbean, String name, Class type, MethodInvoker getInvoker, MethodInvoker setInvoker) {
        this.gmbean = gmbean;
        this.name = name;
        this.type = type;
        this.readable = (getInvoker != null);
        this.getInvoker = getInvoker;
        this.writable = (setInvoker != null);
        this.setInvoker = setInvoker;
        this.isConstructorArg = false;
        this.persistent = false;
        mbeanAttributeInfo = new MBeanAttributeInfo(name, type.getName(), null, readable, writable, type == Boolean.TYPE);
    }

    public GBeanMBeanAttribute(GBeanMBean gMBean, GAttributeInfo attributeInfo, Class constructorType) throws InvalidConfigurationException {
        if (attributeInfo.isReadable() == Boolean.FALSE &&
                attributeInfo.isWritable() == Boolean.FALSE &&
                !attributeInfo.isPersistent()) {
            throw new InvalidConfigurationException("An attribute must be readable, writable, or persistent: +" +
                    " name=" + attributeInfo.getName() +
                    " targetClass=" + gMBean.getType().getName());
        }
        this.gmbean = gMBean;
        this.name = attributeInfo.getName();
        this.persistent = attributeInfo.isPersistent();
        this.isConstructorArg = (constructorType != null);

        boolean isIs;

        // If attribute is persistent or not tagged as unreadable, search for a getter method
        if (attributeInfo instanceof DynamicGAttributeInfo) {
            type = Object.class;
            readable = attributeInfo.isReadable().booleanValue();
            if (readable) {
                getInvoker = new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        DynamicGBean dynamicGBean = (DynamicGBean) GBeanMBeanAttribute.this.gmbean.getTarget();
                        return dynamicGBean.getAttribute(name);
                    }
                };
            } else {
                getInvoker = null;
            }
            writable = attributeInfo.isWritable().booleanValue();
            if (writable) {
                setInvoker = new MethodInvoker() {
                    public Object invoke(Object target, Object[] arguments) throws Exception {
                        DynamicGBean dynamicGBean = (DynamicGBean) GBeanMBeanAttribute.this.gmbean.getTarget();
                        dynamicGBean.setAttribute(name, arguments[0]);
                        return null;
                    }
                };
            } else {
                setInvoker = null;
            }
            isIs = false;
        } else {
            Method getterMethod = null;
            if (attributeInfo.isPersistent() || attributeInfo.isReadable() != Boolean.FALSE) {
                getterMethod = searchForGetter(gMBean, attributeInfo);
            }
            if (getterMethod != null) {
                getInvoker = new FastMethodInvoker(getterMethod);

                // this attribute is readable as long as it was not explicitly tagged as unreadable
                readable = attributeInfo.isReadable() != Boolean.FALSE;
            } else {
                getInvoker = null;
                readable = false;
            }

            // If attribute is persistent or not tagged as unwritable, search for a setter method
            Method setterMethod = null;
            if (attributeInfo.isPersistent() || attributeInfo.isWritable() != Boolean.FALSE) {
                setterMethod = searchForSetter(gMBean, attributeInfo);
            }
            if (setterMethod != null) {
                setInvoker = new FastMethodInvoker(setterMethod);

                // this attribute is writable as long as it was not explicitly tagged as unwritable
                writable = attributeInfo.isWritable() != Boolean.FALSE;
                isIs = setterMethod.getName().startsWith("is");
            } else {
                setInvoker = null;
                writable = false;
                isIs = false;
            }

            // getter and setter types are consistent
            if (getInvoker != null && setInvoker != null &&
                    getterMethod.getReturnType() != setterMethod.getParameterTypes()[0]) {
                throw new InvalidConfigurationException("Getter and setter methods do not have the same types:" +
                        " name=" + attributeInfo.getName() +
                        " geterMethod=" + getterMethod.getName() +
                        " seterMethod=" + setterMethod.getName() +
                        " targetClass=" + gMBean.getType().getName());
            }

            // getter and constructor types are consistent
            if (constructorType != null && getterMethod != null &&
                    constructorType != getterMethod.getReturnType()) {
                throw new InvalidConfigurationException("Constructor argument and getter method do not have the same type:" +
                        " name=" + attributeInfo.getName() +
                        " constructorType=" + constructorType.getName() +
                        " getterMethod=" + getterMethod.getName() +
                        " targetClass=" + gMBean.getType().getName());
            }

            // setter and constructor types are consistent
            if (constructorType != null && setterMethod != null &&
                    constructorType != setterMethod.getParameterTypes()[0]) {
                throw new InvalidConfigurationException("Constructor argument and setter method do not have the same type:" +
                        " name=" + attributeInfo.getName() +
                        " constructorType=" + constructorType.getName() +
                        " setterMethod=" + setterMethod.getName() +
                        " targetClass=" + gMBean.getType().getName());
            }

            // set the attribute type
            if (constructorType != null) {
                type = constructorType;
            } else if (getterMethod != null) {
                type = getterMethod.getReturnType();
            } else if (setterMethod != null) {
                type = setterMethod.getParameterTypes()[0];
            } else {
                // neither getter/setter/or constructor argument
                type = Object.class;
            }
        }

        mbeanAttributeInfo = new MBeanAttributeInfo(
                attributeInfo.getName(),
                type.getName(),
                null,
                readable,
                writable,
                isIs);
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

    public void online() throws ReflectionException {
        if (persistent && !isConstructorArg && setInvoker != null) {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(gmbean.getClassLoader());
                setInvoker.invoke(gmbean.getTarget(), new Object[]{persistentValue});
            } catch (Throwable throwable) {
                throw new ReflectionException(new InvocationTargetException(throwable));
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }

    public void offline() {
        if (persistent && getInvoker != null) {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(gmbean.getClassLoader());
                persistentValue = getInvoker.invoke(gmbean.getTarget(), null);
            } catch (Throwable throwable) {
                log.error("Could not get the current value of persistent attribute while going offline.  The " +
                        "persistent attribute will not reflect the current state attribute: name=" + name, throwable);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }

    public Object getValue() throws ReflectionException {
        if (gmbean.isOffline()) {
            if (persistent) {
                return persistentValue;
            } else {
                throw new IllegalStateException("Only persistent attributes can be accessed while offline");
            }
        } else {
            if (!readable) {
                if (persistent) {
                    throw new IllegalStateException("This persistent attribute is not accessible while online");
                } else {
                    throw new IllegalArgumentException("This attribute is not readable");
                }
            }
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(gmbean.getClassLoader());
                Object value = getInvoker.invoke(gmbean.getTarget(), null);
                return value;
            } catch (Throwable throwable) {
                throw new ReflectionException(new InvocationTargetException(throwable));
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }

    public void setValue(Object value) throws ReflectionException {
        if (gmbean.isOffline()) {
            if (persistent) {
                this.persistentValue = value;
            } else {
                throw new IllegalStateException("Only persistent attributes can be modified while offline");
            }
        } else {
            if (!writable) {
                if (persistent) {
                    throw new IllegalStateException("This persistent attribute is not modifable while online");
                } else {
                    throw new IllegalArgumentException("This attribute is not writable");
                }
            }
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(gmbean.getClassLoader());
                setInvoker.invoke(gmbean.getTarget(), new Object[]{value});
            } catch (Throwable throwable) {
                throw new ReflectionException(new InvocationTargetException(throwable));
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }

    private static Method searchForGetter(GBeanMBean gMBean, GAttributeInfo attributeInfo) throws InvalidConfigurationException {
        if (attributeInfo.getGetterName() == null) {
            // no explicit name give so we must search for a name
            String getterName = "get" + attributeInfo.getName();
            String isName = "is" + attributeInfo.getName();
            Method[] methods = gMBean.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 0 &&
                        method.getReturnType() != Void.TYPE &&
                        (getterName.equalsIgnoreCase(method.getName()) ||
                        isName.equalsIgnoreCase(method.getName()))) {

                    return method;
                }
            }
        } else {
            // we have an explicit name, so no searching is necessary
            try {
                Method method = gMBean.getType().getMethod(attributeInfo.getGetterName(), null);
                if (method.getReturnType() != Void.TYPE) {
                    return method;
                }
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // if this attribute was explicity tagged as being readable but there is not getter
        if (attributeInfo.isReadable() == Boolean.TRUE) {
            throw new InvalidConfigurationException("Getter method not found on target:" +
                    " name=" + attributeInfo.getName() +
                    " targetClass=" + gMBean.getType().getName());
        }

        // a getter is not necessary for this attribute
        return null;
    }

    private static Method searchForSetter(GBeanMBean gMBean, GAttributeInfo attributeInfo) throws InvalidConfigurationException {
        if (attributeInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + attributeInfo.getName();
            Method[] methods = gMBean.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // even though we have an exact name we need to search the methods becaus we don't know the parameter type
            Method[] methods = gMBean.getType().getMethods();
            String setterName = attributeInfo.getSetterName();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equals(method.getName())) {

                    return method;
                }
            }
        }

        // An attribute must have a setter if it was explicitly tagged as writable or
        // if it is persistent and it is not a constructor arg (if it is persistent we must have
        // a way toget the data into the instance)
        if (attributeInfo.isWritable() == Boolean.TRUE) {
            throw new InvalidConfigurationException("Setter method not found on target:" +
                    " name=" + attributeInfo.getName() +
                    " targetClass=" + gMBean.getType().getName());
        }

        // a setter is not necessary for this attribute
        return null;
    }
}
