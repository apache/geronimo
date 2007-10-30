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
package org.apache.geronimo.jaxws.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// TODO: Check for static methods and fields. They are only allowed for client apps.
public abstract class InjectingAnnotationHandler implements AnnotationHandler {

    abstract public Object getAnnotationValue(Annotation annotation,
                                              String name,
                                              Class<?> type)
            throws InjectionException;

    public void processClassAnnotation(Object instance,
                                       Class clazz,
                                       Annotation annotation) {
        // injection is not done for annotations at class level
    }

    public String getJNDIName(Object instance, String name, Field field) {
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return instance.getClass().getName() + "/" + field.getName();
        }
    }

    public String getJNDIName(Object instance, String name, Method method) {
        if (name != null && name.length() > 0) {
            return name;
        } else {
            String propName = method.getName();
            propName = propName.substring(3);
            propName = Character.toLowerCase(propName.charAt(0))
                    + propName.substring(1);
            return instance.getClass().getName() + "/" + propName;
        }
    }

    public Class<?> getType(Class<?> type, Field field) {
        return (type == null || Object.class == type) ? field.getType() : type;
    }

    public Class<?> getType(Class<?> type, Method method) {
        return (type == null || Object.class == type) ? method
                .getParameterTypes()[0] : type;
    }

    protected void injectField(Object instance,
                               Field field,
                               Annotation annotation,
                               String name,
                               Class<?> type) throws InjectionException {

        String jndiName = getJNDIName(instance, name, field);

        Object lookedupResource = getAnnotationValue(annotation, jndiName,
                getType(type, field));

        boolean accessibility = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(instance, lookedupResource);
        } catch (IllegalArgumentException e) {
            throw new InjectionException("Field injection failed", e);
        } catch (IllegalAccessException e) {
            throw new InjectionException("Field injection failed", e);
        } finally {
            field.setAccessible(accessibility);
        }
    }

    protected void injectMethod(Object instance,
                                Method method,
                                Annotation annotation,
                                String name,
                                Class<?> type) throws InjectionException {

        if (!method.getName().startsWith("set")
                || method.getParameterTypes().length != 1
                || !method.getReturnType().equals(Void.class)) {
            throw new IllegalArgumentException(
                    "Invalid method resource injection annotation");
        }

        String jndiName = getJNDIName(instance, name, method);

        Object lookedupResource = getAnnotationValue(annotation, jndiName,
                getType(type, method));

        boolean accessibility = method.isAccessible();
        try {
            method.setAccessible(true);
            method.invoke(instance, lookedupResource);
        } catch (IllegalArgumentException e) {
            throw new InjectionException("Method injection failed", e);
        } catch (IllegalAccessException e) {
            throw new InjectionException("Method injection failed", e);
        } catch (InvocationTargetException e) {
            throw new InjectionException("Method injection failed", e);
        } finally {
            method.setAccessible(accessibility);
        }
    }

}
