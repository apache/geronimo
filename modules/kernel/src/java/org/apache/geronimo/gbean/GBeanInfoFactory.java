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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/01/23 16:43:06 $
 */
public class GBeanInfoFactory {
    private final String name;
    private final String className;
    private final Set attributes = new HashSet();
    private GConstructorInfo constructor;
    private final Set operations = new HashSet();
    private final Set endpoints = new HashSet();
    private final Set notifications = new HashSet();

    public GBeanInfoFactory(String name) {
        this(name, name);
    }

    public GBeanInfoFactory(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public GBeanInfoFactory(String className, GBeanInfo source) {
        this(className, className, source);
    }

    public GBeanInfoFactory(String name, String className, GBeanInfo source) {
        assert name != null && className != null && source != null;
        this.name = name;
        this.className = className;
        attributes.addAll(source.getAttributeSet());
        operations.addAll(source.getOperationsSet());
        endpoints.addAll(source.getEndpointsSet());
        notifications.addAll(source.getNotificationsSet());
        //in case subclass constructor has same parameters as superclass.
        constructor = source.getConstructor();
    }

    public void addInterface(Class intf) {
        addInterface(intf, new String[0]);
    }

    public void addInterface(Class intf, String[] persistentAttriubtes) {
        Set persistentName = new HashSet(Arrays.asList(persistentAttriubtes));
        Map tempAttributes = new HashMap();

        Method[] methods = intf.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            if ((name.startsWith("get") || name.startsWith("is")) && parameterTypes.length == 0) {
                String attributeName = (name.startsWith("get")) ? name.substring(3) : name.substring(2);
                GAttributeInfo attribute = (GAttributeInfo) tempAttributes.get(attributeName);
                if (attribute == null) {
                    tempAttributes.put(attributeName, new GAttributeInfo(attributeName, persistentName.contains(attributeName), name, null));
                } else {
                    tempAttributes.put(attributeName, new GAttributeInfo(attributeName, persistentName.contains(attributeName), name, attribute.getSetterName()));
                }
            } else if (name.startsWith("set") && parameterTypes.length == 1) {
                String attributeName = name.substring(3);
                GAttributeInfo attribute = (GAttributeInfo) tempAttributes.get(attributeName);
                if (attribute == null) {
                    tempAttributes.put(attributeName, new GAttributeInfo(attributeName, persistentName.contains(attributeName), null, name));
                } else {
                    tempAttributes.put(attributeName, new GAttributeInfo(attributeName, persistentName.contains(attributeName), attribute.getSetterName(), name));
                }
            } else {
                List parameters = new ArrayList(parameterTypes.length);
                for (int j = 0; j < parameterTypes.length; j++) {
                    parameters.add(parameterTypes[j].getName());
                }
                operations.add(new GOperationInfo(name, name, parameters));
            }
        }
        attributes.addAll(tempAttributes.values());
    }

    public void addAttribute(GAttributeInfo info) {
        attributes.add(info);
    }

    public void setConstructor(GConstructorInfo constructor) {
        this.constructor = constructor;
    }

    public void addOperation(GOperationInfo info) {
        operations.add(info);
    }

    public void addEndpoint(GEndpointInfo info) {
        endpoints.add(info);
    }

    public void addNotification(GNotificationInfo info) {
        notifications.add(info);
    }

    public GBeanInfo getBeanInfo() {
        return new GBeanInfo(name, className, attributes, constructor, operations, endpoints, notifications);
    }
}
