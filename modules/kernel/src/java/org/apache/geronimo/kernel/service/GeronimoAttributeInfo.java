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
package org.apache.geronimo.kernel.service;

import java.lang.reflect.Method;
import javax.management.MBeanAttributeInfo;

import net.sf.cglib.MethodProxy;

/**
 * Describes an attibute of a GeronimoMBean.  This extension allows the properties to be mutable during setup,
 * and once the MBean is deployed an imutable copy of will be made.  This class also adds support for to
 * direct the attibute to a specific target in a multi target GeronimoMBean.  It also supports caching of the
 * attribute value, which can reduce the number of calls on the target.
 *
 * @version $Revision: 1.2 $ $Date: 2003/10/24 22:45:01 $
 */
public class GeronimoAttributeInfo extends MBeanAttributeInfo {
    /**
     * Is this class still mutable from users.
     */
    private final boolean immutable;

    /**
     * Name of this attribute.
     */
    private String name;

    /**
     * Type of this attribute.
     */
    private final String type;

    /**
     * A user displayable descrption of this attribute.
     */
    private String description;

    /**
     * Is this attribute readable?
     */
    private boolean readable = true;

    /**
     * Is this attribute writiable?
     */
    private boolean writable = true;

    /**
     * Is the getter method an is method?
     * This variable is completely useless but required by the spec, so we do our best to honor it.
     */
    private boolean is;

    /**
     * Logical name of the target.
     */
    private String targetName;

    /**
     * Name of the getter method.
     * The default is "get" + name.  In the case of a defualt value we do a caseless search for the name.
     */
    private String getterName;

    /**
     * Name of the setter method.
     * The default is "set" + name.  In the case of a defualt value we do a caseless search for the name.
     */
    private String setterName;

    /**
     * The maximum ammount ot time in seconds that a cached value is valid.
     */
    long cacheTimeLimit;

    //
    // Runtime information -- this is not exposed to clients
    //

    /**
     * The cached value of the attribute.
     */
    Object value;

    /**
     * The object on which the getter and setter will be invoked
     */
    Object target;

    /**
     * The method that will be called to get the attribute value.  If null, the cached value will
     * be returned.
     */
    final MethodProxy getterProxy;

    /**
     * The method that will be called to set the attribute value.  If null, the value will only be
     * set into the cache.
     */
    final MethodProxy setterProxy;

    /**
     * Time stamp from when the value field was last updated.
     */
    long lastUpdate;
    private final int hashCode = System.identityHashCode(this);

    /**
     * Creates an empty mutable GeronimoAttributeInfo.
     */
    public GeronimoAttributeInfo() {
        super("Ignore", "Ignore", null, true, true, false);
        immutable = false;
        getterProxy = null;
        setterProxy = null;
        type = null;
    }

    /**
     * Creates an immutable copy of the source GeronimoAttributeInfo.
     * @param source the source GeronimoAttributeInfo to copy
     * @param parent the GeronimoMBeanInfo that will contain this attribute
     */
    GeronimoAttributeInfo(GeronimoAttributeInfo source, GeronimoMBeanInfo parent) {
        super("Ignore", "Ignore", null, true, true, false);
        immutable = true;

        //
        // Required
        //
        if (source.name == null) {
            throw new IllegalArgumentException("Attribute name is null");
        }
        name = source.name;
        readable = source.readable;
        writable = source.writable;

        if (!readable && !writable) {
            throw new IllegalArgumentException("Attribute is neither readable or writable: name=" + name);
        }

        //
        // Optional
        //
        description = source.description;
        cacheTimeLimit = source.cacheTimeLimit;

        //
        // Optional (derived)
        //
        if (source.target != null) {
            target = source.target;
            targetName = source.targetName;
        } else if (source.targetName == null) {
            target = parent.getTarget();
        } else {
            targetName = source.targetName;
            target = parent.getTarget(targetName);
            if (target == null) {
                throw new IllegalArgumentException("Target not found: targetName=" + targetName);
            }
        }

        Method[] methods = target.getClass().getMethods();
        Class attributeType = null;
        if (readable) {
            Method getterMethod = null;
            if (source.getterName == null) {
                String getterName = "get" + name;
                String isName = "is" + name;
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getParameterTypes().length == 0 &&
                            (getterName.equalsIgnoreCase(method.getName()) ||
                            isName.equalsIgnoreCase(method.getName()))) {
                        getterMethod = method;
                        break;
                    }
                }
            } else {
                try {
                    String methodName = source.getterName;
                    getterMethod = target.getClass().getMethod(methodName, null);
                } catch (Exception e) {
                    // we will throw the formatted exception below
                }
            }

            if (getterMethod == null) {
                throw new IllegalArgumentException("Getter method not found on target:" +
                        " name=" + name +
                        " targetClass=" + target.getClass().getName());
            }

            getterName = getterMethod.getName();
            is = getterName.startsWith("is");
            getterProxy = MethodProxy.create(getterMethod, getterMethod);
            attributeType = getterMethod.getReturnType();
        } else {
            getterName = null;
            readable = false;
            getterProxy = null;
        }

        if (writable) {
            Method setterMethod = null;
            String methodName = null;
            if (source.setterName == null) {
                methodName = "set" + name;
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getParameterTypes().length == 1 && methodName.equalsIgnoreCase(method.getName())) {
                        setterMethod = method;
                        break;
                    }
                }
            } else {
                // even though we have an exact name we need to search the methods becaus we don't know the parameter type
                methodName = source.setterName;
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getParameterTypes().length == 1 && methodName.equals(method.getName())) {
                        setterMethod = method;
                        break;
                    }
                }
            }

            if (setterMethod == null) {
                throw new IllegalArgumentException("Setter method not found on target:" +
                        " setterName=" + methodName +
                        " targetClass=" + source.target.getClass().getName());
            }

            setterName = setterMethod.getName();
            setterProxy = MethodProxy.create(setterMethod, setterMethod);
            attributeType = setterMethod.getParameterTypes()[0];
        } else {
            setterName = null;
            setterProxy = null;
        }
        type = attributeType.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.description = description;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.writable = writable;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.targetName = targetName;
    }

    public String getGetterName() {
        return getterName;
    }

    public void setGetterName(String getterName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.getterName = getterName;
    }

    public String getSetterName() {
        return setterName;
    }

    public void setSetterName(String setterName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.setterName = setterName;
        is = (setterName != null && setterName.startsWith("is"));
    }

    public String getType() {
        return type;
    }

    public boolean isIs() {
        return is;
    }

    public long getCacheTimeLimit() {
        return cacheTimeLimit;
    }

    public void setCacheTimeLimit(long cacheTimeLimit) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.cacheTimeLimit = cacheTimeLimit;
    }

    public String getCachePolicy() {
        if (cacheTimeLimit < 0) {
            return GeronimoMBeanInfo.NEVER;
        } else if (cacheTimeLimit == 0) {
            return GeronimoMBeanInfo.ALWAYS;
        } else {
            return "" + cacheTimeLimit;
        }

    }

    public void setCachePolicy(String cacheTimeLimit) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        if (cacheTimeLimit == null || cacheTimeLimit.length() == 0) {
            throw new IllegalArgumentException("cacheTimeLimit is null");
        }
        if (GeronimoMBeanInfo.ALWAYS.equalsIgnoreCase(cacheTimeLimit)) {
            this.cacheTimeLimit = 0;
        } else if (GeronimoMBeanInfo.NEVER.equalsIgnoreCase(cacheTimeLimit)) {
            this.cacheTimeLimit = -1;
        } else {
            this.cacheTimeLimit = Long.parseLong(cacheTimeLimit);
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object object) {
        return (this == object);
    }

    public String toString() {
        return "[GeronimoAttributeInfo: name=" + name + " description=" + description + "]";
    }
}
