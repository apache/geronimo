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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceProperty;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.Property;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@PersistenceContext</strong> and <strong>@PersistenceContexts</strong> annotations to deployment
 * descriptor tags. The PersistenceContextAnnotationHelper class can be used as part of the deployment of
 * a module into the Geronimo server. It performs the following major functions:
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
 * <li>None
 * </ul>
 *
 * @version $Rev $Date$
 *
 */
public final class PersistenceContextAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Logger log = LoggerFactory.getLogger(PersistenceContextAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private PersistenceContextAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the PersistenceContext and PersistenceContexts annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        if (annotatedApp != null) {
            if (classFinder.isAnnotationPresent(PersistenceContexts.class)) {
                processPersistenceContexts(annotatedApp, classFinder);
            }
            if (classFinder.isAnnotationPresent(PersistenceContext.class)) {
                processPersistenceContext(annotatedApp, classFinder);
            }
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processPersistenceContext(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceContext(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class<?>> classeswithPersistenceContext = classFinder.findAnnotatedClasses(PersistenceContext.class);
        List<Method> methodswithPersistenceContext = classFinder.findAnnotatedMethods(PersistenceContext.class);
        List<Field> fieldswithPersistenceContext = classFinder.findAnnotatedFields(PersistenceContext.class);

        // Class-level annotation
        for (Class cls : classeswithPersistenceContext) {
            PersistenceContext persistenceContext = (PersistenceContext) cls.getAnnotation(PersistenceContext.class);
            if (persistenceContext != null) {
                addPersistenceContext(annotatedApp, persistenceContext, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodswithPersistenceContext) {
            PersistenceContext persistenceContext = method.getAnnotation(PersistenceContext.class);
            if (persistenceContext != null) {
                addPersistenceContext(annotatedApp, persistenceContext, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldswithPersistenceContext) {
            PersistenceContext persistenceContext = field.getAnnotation(PersistenceContext.class);
            if (persistenceContext != null) {
                addPersistenceContext(annotatedApp, persistenceContext, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(annotatedApp);

        log.debug("processPersistenceContext(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processPersistenceContexts(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceContexts(): Entry");

        List<Class<?>> classeswithPersistenceContexts = classFinder.findAnnotatedClasses(PersistenceContexts.class);

        // Class-level annotation(s)
        List<PersistenceContext> persistenceContextList = new ArrayList<PersistenceContext>();
        for (Class cls : classeswithPersistenceContexts) {
            PersistenceContexts persistenceContexts = (PersistenceContexts) cls.getAnnotation(PersistenceContexts.class);
            if (persistenceContexts != null) {
                persistenceContextList.addAll(Arrays.asList(persistenceContexts.value()));
            }
            for (PersistenceContext persistenceContext : persistenceContextList) {
                addPersistenceContext(annotatedApp, persistenceContext, cls, null, null);
            }
            persistenceContextList.clear();
        }

        log.debug("processPersistenceContexts(): Exit");
    }


    /**
     * Add @PersistenceContext and @PersistenceContexts annotations to the deployment descriptor. XMLBeans are used to
     * read and manipulate the deployment descriptor as necessary. The PersistenceContext annotation(s) will be
     * converted to one of the following deployment descriptors:
     *
     * <ol>
     *      <li><persistence-context-ref> -- Describes a single container-managed entity manager
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *          existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @PersistenceContext annotation
     * @param cls        Class name with the @PersistenceContext annoation
     * @param method     Method name with the @PersistenceContext annoation
     * @param field      Field name with the @PersistenceContext annoation
     * @param annotatedApp  Access to the specc dd
     */
    private static void addPersistenceContext(JndiConsumer annotatedApp, PersistenceContext annotation, Class cls, Method method, Field field) {
        log.debug("addPersistenceContext( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // PersistenceContextRef name:
        // -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   Name is JavaBeans property name qualified
        //                                              by the class (or as provided on the
        //                                              annotation)
        // -- When annotation is applied on a field:    Name is the field name qualified by the
        //                                              class (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String persistenceContextRefName = getName(annotation.name(), method, field);

        log.debug("addPersistenceContext(): PersistenceContextRefName: " + persistenceContextRefName);

        // If there is already xml for the persistence context ref, just add injection targets and return.
        Collection<PersistenceContextRef> persistenceContextRefs = annotatedApp.getPersistenceContextRef();
        for (PersistenceContextRef persistenceContextRef : persistenceContextRefs) {
            if (persistenceContextRef.getPersistenceContextRefName().trim().equals(persistenceContextRefName)) {
                if (method != null || field != null) {
                    Set<InjectionTarget> targets = persistenceContextRef.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        persistenceContextRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }
                return;
            }
        }

        // Doesn't exist in deployment descriptor -- add new
        PersistenceContextRef persistenceContextRef = new PersistenceContextRef();

        //------------------------------------------------------------------------------
        // <persistence-context-ref> required elements:
        //------------------------------------------------------------------------------

        // persistence-context-ref-name
        persistenceContextRef.setPersistenceContextRefName(persistenceContextRefName);

        //------------------------------------------------------------------------------
        // <persistence-context-ref> optional elements:
        //------------------------------------------------------------------------------

        // persistence-unit-name
        String unitNameAnnotation = annotation.unitName();
        if (!unitNameAnnotation.isEmpty()) {
            persistenceContextRef.setPersistenceUnitName(unitNameAnnotation);
        }

        // persistence-context-type
        if (annotation.type() == PersistenceContextType.TRANSACTION) {
            persistenceContextRef.setPersistenceContextType(org.apache.openejb.jee.PersistenceContextType.TRANSACTION);
        } else if (annotation.type() == PersistenceContextType.EXTENDED) {
            persistenceContextRef.setPersistenceContextType(org.apache.openejb.jee.PersistenceContextType.EXTENDED);
        }

        // persistence-context-properties
        PersistenceProperty[] properties = annotation.properties();
        for (PersistenceProperty property : properties) {
            Property prop = new Property();
            prop.setName(property.name());
            prop.setValue(property.value());
            persistenceContextRef.getPersistenceProperty().add(prop);
        }

        // injection targets
        if (method != null || field != null) {
            persistenceContextRef.getInjectionTarget().add(configureInjectionTarget(method, field));
        }
        annotatedApp.getPersistenceContextRef().add(persistenceContextRef);

    }

}
