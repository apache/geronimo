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
package org.apache.geronimo.gbean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision: 1.22 $ $Date: 2004/07/12 06:07:49 $
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
        this(name, name, null);
    }

    public GBeanInfoFactory(Class clazz) {
        this(checkNotNull(clazz).getName(), clazz.getName(), null);
    }

    public GBeanInfoFactory(String name, String className) {
        this(name, className, null);
    }

    public GBeanInfoFactory(String name, Class clazz) {
        this(name, checkNotNull(clazz).getName(), null);
    }

    public GBeanInfoFactory(Class clazz, GBeanInfo source) {
        this(checkNotNull(clazz).getName(), clazz.getName(), source);
    }

    public GBeanInfoFactory(String name, GBeanInfo source) {
        this(checkNotNull(name), name, source);
    }

    public GBeanInfoFactory(String name, ClassLoader cl) {
        this(checkNotNull(name), name, GBeanInfo.getGBeanInfo(name, cl));
    }

    public GBeanInfoFactory(String name, Class clazz, GBeanInfo source) {
        this(name, checkNotNull(clazz).getName(), source);
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
                    operations.put(new GOperationSignature(operationInfo.getName(),
                            operationInfo.getParameterList()), operationInfo);
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
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(intf);
        } catch (IntrospectionException e) {
            IllegalArgumentException ex = new IllegalArgumentException("Can not introspect interface");
            ex.initCause(e);
            throw ex;
        }
        
        PropertyDescriptor[] attDescriptors = beanInfo.getPropertyDescriptors();
        Set processed = new HashSet();
        for (int i = 0; i < attDescriptors.length; i++) {
            PropertyDescriptor desc = attDescriptors[i];
            GAttributeInfo info = (GAttributeInfo) attributes.get(desc.getName());
            String oldGetter = null;
            String oldSetter = null;
            if ( null != info ) {
                oldGetter = info.getGetterName();
                oldSetter = info.getSetterName();
            }
            attributes.put(desc.getName(),
                new GAttributeInfo(desc.getName(),
                    desc.getPropertyType().getName(), 
                    persistentName.contains(desc.getName()),
                    null == desc.getReadMethod() ? oldGetter : desc.getReadMethod().getName(), 
                    null == desc.getWriteMethod() ? oldSetter : desc.getWriteMethod().getName()));
            if ( null != desc.getReadMethod() ) {
                processed.add(desc.getReadMethod());
            }
            if ( null != desc.getWriteMethod() ) {
                processed.add(desc.getWriteMethod());
            }
        }
        
        MethodDescriptor[] opDescriptors = beanInfo.getMethodDescriptors();
        for (int i = 0; i < opDescriptors.length; i++) {
            MethodDescriptor desc = opDescriptors[i];
            if ( processed.contains(desc.getMethod()) ) {
                continue;
            }
            Class[] params = desc.getMethod().getParameterTypes();
            List parameters = new ArrayList(params.length);
            for (int j = 0; j < params.length; j++) {
                parameters.add(params[j].getName());
            }
            addOperation(new GOperationInfo(desc.getName(), desc.getName(), parameters));
        }
    }

    public void addAttribute(String name, Class type, boolean persistent) {
        addAttribute(new GAttributeInfo(name, type.getName(), persistent));
    }

    public void addAttribute(String name, String type, boolean persistent) {
        addAttribute(new GAttributeInfo(name, type, persistent));
    }

    public void addAttribute(GAttributeInfo info) {
        attributes.put(info.getName(), info);
    }

    public void setConstructor(GConstructorInfo constructor) {
        this.constructor = constructor;
    }

    public void setConstructor(String[] names) {
        constructor = new GConstructorInfo(names);
    }

    public void addOperation(GOperationInfo operationInfo) {
        operations.put(new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList()), operationInfo);
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
        return new GBeanInfo(name, className, attributes.values(), constructor, operations.values(), references, notifications);
    }
}
