/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;

/**
 * @version $Revision: 1.15 $ $Date: 2004/02/25 09:57:48 $
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
        this(checkNotNull(clazz).getName(), clazz.getName(), null);
    }

    public GBeanInfoFactory(String name, String className) {
        this(name, className, null);
    }

    public GBeanInfoFactory(String className, GBeanInfo source) {
        this(className, className, source);
    }

    public GBeanInfoFactory(Class clazz, GBeanInfo source) {
        this(checkNotNull(clazz).getName(), clazz.getName(), source);
    }

    public GBeanInfoFactory(String name, String className, GBeanInfo source) {
        checkNotNull(name);
        checkNotNull(className);

        this.name = name;
        this.className = className;
        if (source != null) {
            Set sourceAttributes = source.getAttributes();
            if (sourceAttributes != null && !sourceAttributes.isEmpty()) {
                for (Iterator it = sourceAttributes.iterator(); it.hasNext();) {
                    GAttributeInfo attributeInfo = (GAttributeInfo) it.next();
                    attributes.put(attributeInfo.getName(), attributeInfo);
                }
            }

            Set sourceOperations = source.getOperations();
            if (sourceOperations != null && !sourceOperations.isEmpty()) {
                for (Iterator it = sourceOperations.iterator(); it.hasNext();) {
                    GOperationInfo operationInfo = (GOperationInfo) it.next();
                    operations.put(
                            new MBeanOperationSignature(operationInfo.getName(), operationInfo.getParameterList()),
                            operationInfo);
                }
            }
            references.addAll(source.getReferences());
            notifications.addAll(source.getNotifications());
            //in case subclass constructor has same parameters as superclass.
            constructor = source.getConstructor();
        }
    }

    /**
     * Checks whether or not the input argument is null; otherwise it throws
     * {@link IllegalArgumentException}.
     *
     * @param clazz the input argument to validate
     * @throws IllegalArgumentException if input is null
     */
    private static Class checkNotNull(final Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null argument supplied");
        }
        return clazz;
    }

    /**
     * Checks whether or not the input argument is null; otherwise it throws
     * {@link IllegalArgumentException}.
     *
     * @param string the input argument to validate
     * @throws IllegalArgumentException if input is null
     */
    private static String checkNotNull(final String string) {
        if (string == null) {
            throw new IllegalArgumentException("null argument supplied");
        }
        return string;
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

    public void addOperation(GOperationInfo operationInfo) {
        operations.put(
                new MBeanOperationSignature(operationInfo.getName(), operationInfo.getParameterList()),
                operationInfo);
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
        return new GBeanInfo(
                name,
                className,
                attributes.values(),
                constructor,
                operations.values(),
                references,
                notifications);
    }
}
