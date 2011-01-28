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

package org.apache.geronimo.j2ee.deployment.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resources;
import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@Resource</strong> and <strong>@Resources</strong> annotations to deployment descriptor
 * tags. The ResourceAnnotationHelper class can be used as part of the deployment of a module into
 * the Geronimo server. It performs the following major functions:
 * <p/>
 * <ol>
 * <li>Translates annotations into corresponding deployment descriptor elements (so that the
 * actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 * <p/>
 * <p><strong>Note(s):</strong>
 * <ul>
 * <li>The user is responsible for invoking change to metadata-complete
 * <li>This helper class will validate any changes it makes to the deployment descriptor. An
 * exception will be thrown if it fails to parse
 * </ul>
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>Usage of mappedName
 * </ul>
 *
 * @version $Rev$ $Date$
 * @since 02-2007
 */
public final class ResourceAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Logger log = LoggerFactory.getLogger(ResourceAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private ResourceAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from Resource and Resources annotations
     * @throws Exception if parsing or validation error
     */
    public static void processAnnotations(JndiConsumer annotatedApp, AbstractFinder classFinder, ResourceProcessor resourceProcessor) throws Exception {
        if (annotatedApp != null) {
            if (classFinder.isAnnotationPresent(Resources.class)) {
                processResources(annotatedApp, classFinder, resourceProcessor);
            }
            if (classFinder.isAnnotationPresent(Resource.class)) {
                processResource(annotatedApp, classFinder, resourceProcessor);
            }
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @param resourceProcessor
     * @throws Exception
     */
    private static void processResource(JndiConsumer annotatedApp, AbstractFinder classFinder, ResourceProcessor resourceProcessor) throws Exception {
        log.debug("processResource(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class<?>> classeswithResource = classFinder.findAnnotatedClasses(Resource.class);
        List<Method> methodswithResource = classFinder.findAnnotatedMethods(Resource.class);
        List<Field> fieldswithResource = classFinder.findAnnotatedFields(Resource.class);

        // Class-level annotation
        for (Class cls : classeswithResource) {
            Resource resource = (Resource) cls.getAnnotation(Resource.class);
            if (resource != null) {
                resourceProcessor.processResource(annotatedApp, resource, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodswithResource) {
            Resource resource = (Resource) method.getAnnotation(Resource.class);
            if (resource != null) {
                resourceProcessor.processResource(annotatedApp, resource, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldswithResource) {
            Resource resource = (Resource) field.getAnnotation(Resource.class);
            if (resource != null) {
                resourceProcessor.processResource(annotatedApp, resource, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(annotatedApp);

        log.debug("processResource(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @param resourceProcessor
     * @throws Exception
     */
    private static void processResources(JndiConsumer annotatedApp, AbstractFinder classFinder, ResourceProcessor resourceProcessor) throws Exception {
        log.debug("processResources(): Entry");

        List<Class<?>> classeswithResources = classFinder.findAnnotatedClasses(Resources.class);

        // Class-level annotation(s)
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Class cls : classeswithResources) {
            Resources resources = (Resources) cls.getAnnotation(Resources.class);
            if (resources != null) {
                resourceList.addAll(Arrays.asList(resources.value()));
            }
            for (Resource resource : resourceList) {
                resourceProcessor.processResource(annotatedApp, resource, cls, null, null);
            }
            resourceList.clear();
        }

        log.debug("processResources(): Exit");
    }

    public abstract static class ResourceProcessor extends AnnotationHelper {

        public abstract boolean processResource(JndiConsumer jndiConsumer, Resource annotation, Class cls, Method method, Field field) throws DeploymentException;

        /**
         * Resource name:
         * -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
         * -- When annotation is applied on a method:   Name is JavaBeans property name qualified
         * by the class (or as provided on the annotation)
         * -- When annotation is applied on a field:    Name is the field name qualified by the
         * class (or as provided on the annotation)
         *
         * @param annotation
         * @param method
         * @param field
         * @return
         */
        protected static String getResourceName(Resource annotation, Method method, Field field) {
            return getName(annotation.name(), method, field);
        }

        protected static Class getResourceTypeClass(Resource annotation, Method method, Field field) {
            //------------------------------------------------------------------------------------------
            // Resource type:
            // -- When annotation is applied on a class:    Type must be provided (cannot be inferred)
            // -- When annotation is applied on a method:   Type is the JavaBeans property type (or as
            //                                              provided on the annotation)
            // -- When annotation is applied on a field:    Type is the field type (or as provided on
            //                                              the annotation)
            //------------------------------------------------------------------------------------------
            Class resourceType = annotation.type();
            if (resourceType.equals(Object.class)) {
                if (method != null) {
                    resourceType = method.getParameterTypes()[0];
                } else if (field != null) {
                    resourceType = field.getType();
                }
            }
            return resourceType;
        }
        
        protected static String getResourceType(Resource annotation, Method method, Field field) {
            return getResourceTypeClass(annotation, method, field).getCanonicalName();
        }
    }

}
