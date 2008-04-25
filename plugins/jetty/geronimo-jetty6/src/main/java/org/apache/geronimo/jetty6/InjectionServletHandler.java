/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty6;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;

import org.mortbay.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.j2ee.annotation.Injection;

/**
 * @version $Rev$ $Date$
 */
class InjectionServletHandler extends ServletHandler {
    private static final Logger log = LoggerFactory.getLogger(InjectionServletHandler.class);

    private final Map<String, List<Injection>> injections;


    public InjectionServletHandler(Map<String, List<Injection>> injections) {
        this.injections = injections;
    }

    public Servlet customizeServlet(Servlet servlet) throws Exception {
        List<Injection> classInjections = injections.get(servlet.getClass().getName());
        if (classInjections != null) {
            for (Injection injection: classInjections) {
                
            }
        }
        servlet = super.customizeServlet(servlet);
        processAnnotations(servlet);
        return servlet;
    }

    /**
     * Inject resources in specified instance.
     */
    public void processAnnotations(Object instance) {
        Context context = null;
        try {
            context = (Context) new InitialContext().lookup("java:comp/env");
        } catch (Exception e) {
            return;
        }
        if (context == null) {
            // No resource injection
            return;
        }

        // Initialize fields annotations
        Field[] fields = instance.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(EJB.class)) {
                EJB annotation = fields[i].getAnnotation(EJB.class);
                lookupFieldResource(context, instance, fields[i], annotation.name());
            }
        }

        // Initialize methods annotations
        Method[] methods = instance.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isAnnotationPresent(EJB.class)) {
                EJB annotation = methods[i].getAnnotation(EJB.class);
                lookupMethodResource(context, instance, methods[i], annotation.name());
            }
        }

    }


    /**
     * Inject resources in specified field.
     */
    protected static void lookupFieldResource(javax.naming.Context context, Object instance, Field field, String name) {
        try {
            Object lookedupResource = null;
            boolean accessibility = false;

            if ((name != null) && (name.length() > 0)) {
                lookedupResource = context.lookup(name);
            } else {
                lookedupResource = context.lookup(instance.getClass().getName() + "/" + field.getName());
            }

            accessibility = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, lookedupResource);
            field.setAccessible(accessibility);
        } catch (Exception e) {
            log.error("Error injecting into " + instance.getClass().getName() + "." + field.getName(), e);
        }
    }


    /**
     * Inject resources in specified method.
     */
    protected static void lookupMethodResource(javax.naming.Context context, Object instance, Method method, String name) {
        try {
            if (!method.getName().startsWith("set")
                    || method.getParameterTypes().length != 1
                    || !method.getReturnType().getName().equals("void")) {
                throw new IllegalArgumentException("Invalid method resource injection annotation");
            }

            Object lookedupResource = null;
            boolean accessibility = false;

            if ((name != null) && (name.length() > 0)) {
                lookedupResource = context.lookup(name);
            } else {
                lookedupResource = context.lookup(instance.getClass().getName() + "/" + method.getName().substring(3));
            }

            accessibility = method.isAccessible();
            method.setAccessible(true);
            method.invoke(instance, lookedupResource);
            method.setAccessible(accessibility);
        } catch (Exception e) {
            log.error("Error injecting into " + instance.getClass().getName() + "." + method.getName()  + "()", e);
        }
    }
}
