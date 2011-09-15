/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.j2ee.deployment.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.openejb.jee.InjectionTarget;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationHelper {

    protected static String getName(String name, Method method, Field field) {
        String resourceName = name;
        if (resourceName.equals("")) {
            if (method != null) {
                StringBuilder stringBuilder = new StringBuilder(method.getName().substring(3));
                stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
                resourceName = method.getDeclaringClass().getName() + "/" + stringBuilder.toString();
            } else if (field != null) {
                resourceName = field.getDeclaringClass().getName() + "/" + field.getName();
            }
        }
        return resourceName;
    }

    protected static String getInjectionJavaType(Method method, Field field) {
        if (method != null) {
            String injectionJavaType = method.getName().substring(3);
            StringBuilder stringBuilder = new StringBuilder(injectionJavaType);
            stringBuilder.setCharAt(0, Character.toLowerCase(stringBuilder.charAt(0)));
            return stringBuilder.toString();
        } else if (field != null) {
            return field.getName();
        } else {
            throw new IllegalArgumentException("You must supply exactly one of Method, Field");
        }
    }

    protected static String getInjectionClass(Method method, Field field) {
        if (method != null) {
            return method.getDeclaringClass().getName();
        } else if (field != null) {
            return field.getDeclaringClass().getName();
        } else {
            throw new IllegalArgumentException("You must supply exactly one of Method, Field");
        }
    }

    protected static boolean hasTarget(Method method, Field field, Set<InjectionTarget> targets) {
        String injectionJavaType = getInjectionJavaType(method, field);
        String injectionClass = getInjectionClass(method, field);
        for (InjectionTarget target : targets) {
            if (injectionClass.equals(target.getInjectionTargetClass().trim())
                    && injectionJavaType.equals(target.getInjectionTargetName().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configure Injection Target
     *
     * @param method
     * @param field
     */
    protected static InjectionTarget configureInjectionTarget(Method method, Field field) {
        InjectionTarget injectionTarget = new InjectionTarget();
        String injectionJavaType = getInjectionJavaType(method, field);
        String injectionClass = getInjectionClass(method, field);

        injectionTarget.setInjectionTargetClass(injectionClass);
        injectionTarget.setInjectionTargetName(injectionJavaType);
        return injectionTarget;
    }

    protected static String getJndiName(String name) {
        if (name.indexOf(':') == -1) {
            return "java:comp/env/" + name.trim();
        } else {
            return name.trim();
        }
    }

    /**
     * Validate deployment descriptor
     *
     * @param annotatedApp the wrapped deployment descriptor
     * @throws org.apache.geronimo.common.DeploymentException
     *          thrown if deployment descriptor cannot be parsed
     */
//    protected static void validateDD(AnnotatedApp annotatedApp) throws DeploymentException {
//        try {
//            XmlBeansUtil.parse(annotatedApp.toString());
//        } catch (XmlException e) {
//            throw new DeploymentException("Result of processing web service refs invalid.", e);
//        }
//    }
}
