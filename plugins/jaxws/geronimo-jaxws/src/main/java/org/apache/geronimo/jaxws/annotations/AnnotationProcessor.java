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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class AnnotationProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationProcessor.class);

    private Map<Class<? extends Annotation>, AnnotationHandler> handlers;

    public AnnotationProcessor() {
        this.handlers = new HashMap<Class<? extends Annotation>, AnnotationHandler>();
    }

    public void registerHandler(AnnotationHandler handler) {
        this.handlers.put(handler.getAnnotationType(), handler);
    }

    public void processAnnotations(Object instance) throws AnnotationException {
        // process class annotations
        Class clazz = instance.getClass();
        Iterator iter = this.handlers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Class annotationType = (Class) entry.getKey();
            AnnotationHandler handler = (AnnotationHandler) entry.getValue();

            if (clazz.isAnnotationPresent(annotationType)) {
                Annotation annotation = clazz.getAnnotation(annotationType);
                handler.processClassAnnotation(instance, clazz, annotation);
            }
        }

        // process fields annotations
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            iter = this.handlers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Class annotationType = (Class) entry.getKey();
                AnnotationHandler handler = (AnnotationHandler) entry
                        .getValue();

                if (fields[i].isAnnotationPresent(annotationType)) {
                    Annotation annotation = fields[i]
                            .getAnnotation(annotationType);
                    handler.processFieldAnnotation(instance, fields[i],
                            annotation);
                }
            }
        }

        // process method annotations
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            iter = this.handlers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Class annotationType = (Class) entry.getKey();
                AnnotationHandler handler = (AnnotationHandler) entry
                        .getValue();

                if (methods[i].isAnnotationPresent(annotationType)) {
                    Annotation annotation = methods[i]
                            .getAnnotation(annotationType);
                    handler.processMethodAnnotation(instance, methods[i],
                            annotation);
                }
            }
        }
    }

    public void invokePostConstruct(Object instance) {
        for (Method method : getMethods(instance.getClass(),
                PostConstruct.class)) {
            PostConstruct pc = method.getAnnotation(PostConstruct.class);
            if (pc != null) {
                boolean accessible = method.isAccessible();
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (IllegalAccessException e) {
                    LOG.warn("@PostConstruct method is not visible: " + method);
                } catch (InvocationTargetException e) {
                    LOG.warn("@PostConstruct method threw exception", e);
                } finally {
                    method.setAccessible(accessible);
                }
            }
        }
    }

    public void invokePreDestroy(Object instance) {
        for (Method method : getMethods(instance.getClass(), PreDestroy.class)) {
            PreDestroy pc = method.getAnnotation(PreDestroy.class);
            if (pc != null) {
                boolean accessible = method.isAccessible();
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (IllegalAccessException e) {
                    LOG.warn("@PreDestroy method is not visible: " + method);
                } catch (InvocationTargetException e) {
                    LOG.warn("@PreDestroy method threw exception", e);
                } finally {
                    method.setAccessible(accessible);
                }
            }
        }
    }

    private Collection<Method> getMethods(Class target,
                                          Class<? extends Annotation> annotationType) {
        Collection<Method> methods = new HashSet<Method>();
        addMethods(target.getMethods(), annotationType, methods);
        addMethods(target.getDeclaredMethods(), annotationType, methods);
        return methods;
    }

    private void addMethods(Method[] methods,
                            Class<? extends Annotation> annotationType,
                            Collection<Method> methodsCol) {
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationType)) {
                methodsCol.add(method);
            }
        }
    }

}
