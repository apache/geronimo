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
package org.apache.geronimo.gbean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision: 1.12 $ $Date: 2004/02/24 18:41:45 $
 */
public class GBeanInfoFactory {

    private static final Class[] NO_ARGS = {};

    private final String name;

    private final String className;

    private final Map attributes = new HashMap();

    private GConstructorInfo constructor;

    private final Map operations = new HashMap();

    private final Set references = new HashSet();

    private final Set notifications = new HashSet();

    public GBeanInfoFactory(String name) {
        this(name, name);
    }

    public GBeanInfoFactory(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("argument is null");
        }
        this.name = this.className = clazz.getName();
    }

    public GBeanInfoFactory(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public GBeanInfoFactory(String className, GBeanInfo source) {
        this(className, className, source);
    }

    public GBeanInfoFactory(Class clazz, GBeanInfo source) {
        this(clazz.getName(), clazz.getName(), source);
    }

    public GBeanInfoFactory(String name, String className, GBeanInfo source) {
        if (name == null || className == null || source == null) {
            throw new IllegalArgumentException("null argument(s) supplied");
        }
        this.name = name;
        this.className = className;
        Set sourceAttrs = source.getAttributes();
        if (sourceAttrs != null && !sourceAttrs.isEmpty()) {
            for (Iterator it = sourceAttrs.iterator(); it.hasNext();) {
                GAttributeInfo gattrInfo = (GAttributeInfo) it.next();
                attributes.put(gattrInfo.getName(), gattrInfo);
            }
        }

        Set sourceOps = source.getOperations();
        if (sourceOps != null && !sourceOps.isEmpty()) {
            for (Iterator it = sourceOps.iterator(); it.hasNext();) {
                GOperationInfo gopInfo = (GOperationInfo) it.next();
                operations.put(gopInfo.getName(), gopInfo);
            }
        }
        references.addAll(source.getReferences());
        notifications.addAll(source.getNotifications());
        //in case subclass constructor has same parameters as superclass.
        constructor = source.getConstructor();
    }

    public void addInterface(Class intf) {
        addInterface(intf, new String[0]);
    }

    public void addInterface(Class intf, String[] persistentAttributes) {
        Set persistentName = new HashSet(Arrays.asList(persistentAttributes));

        Method[] methods = intf.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            if ((name.startsWith("get") || name.startsWith("is")) && parameterTypes.length == 0) {
                String attributeName = (name.startsWith("get")) ? name.substring(3) : name.substring(2);
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
                if (attribute == null) {
                    attributes.put(attributeName, new GAttributeInfo(attributeName,
                            persistentName.contains(attributeName), name, null));
                } else {
                    attributes.put(attributeName, new GAttributeInfo(attributeName,
                            persistentName.contains(attributeName), name, attribute.getSetterName()));
                }
            } else if (name.startsWith("set") && parameterTypes.length == 1) {
                String attributeName = name.substring(3);
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
                if (attribute == null) {
                    attributes.put(attributeName, new GAttributeInfo(attributeName,
                            persistentName.contains(attributeName), null, name));
                } else {
                    attributes.put(attributeName, new GAttributeInfo(attributeName,
                            persistentName.contains(attributeName), attribute.getSetterName(), name));
                }
            } else {
                List parameters = new ArrayList(parameterTypes.length);
                for (int j = 0; j < parameterTypes.length; j++) {
                    parameters.add(parameterTypes[j].getName());
                }
                addOperation(new GOperationInfo(name, name, parameters));
            }
        }
    }

    public void addAttribute(String name, boolean persistent) {
        addAttribute(new GAttributeInfo(name, persistent));
    }

    public void addAttribute(GAttributeInfo info) {
        attributes.put(info.getName(), info);
    }

    public void setConstructor(GConstructorInfo constructor) {
        this.constructor = constructor;
    }

    public void setConstructor(String[] names, Class[] types) {
        constructor = new GConstructorInfo(names, types);
    }

    public void addOperation(GOperationInfo info) {
        operations.put(info.getName(), info);
    }

    public void addOperation(String name) {
        addOperation(new GOperationInfo(name, NO_ARGS));
    }

    public void addOperation(String name, Class[] paramTypes) {
        addOperation(new GOperationInfo(name, paramTypes));
    }

    public void addReference(GReferenceInfo info) {
        references.add(info);
    }

    public void addReference(String name, Class type) {
        addReference(new GReferenceInfo(name, type));
    }

    public void addNotification(GNotificationInfo info) {
        notifications.add(info);
    }

    public GBeanInfo getBeanInfo() {
        return new GBeanInfo(name, className, attributes.values(), constructor, operations.values(), references,
                notifications);
    }
}
