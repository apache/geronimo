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

import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@PersistenceUnit</strong> and <strong>@PersistenceUnits</strong> annotations to deployment
 * descriptor tags. The PersistenceUnitAnnotationHelper class can be used as part of the deployment of
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
 * @version $Rev $Date
 * @since 04-2007
 */
public final class PersistenceUnitAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Logger log = LoggerFactory.getLogger(PersistenceUnitAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private PersistenceUnitAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the PersistenceUnit and PersistenceUnits annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        if (annotatedApp != null) {
            if (classFinder.isAnnotationPresent(PersistenceUnits.class)) {
                processPersistenceUnits(annotatedApp, classFinder);
            }
            if (classFinder.isAnnotationPresent(PersistenceUnit.class)) {
                processPersistenceUnit(annotatedApp, classFinder);
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
    private static void processPersistenceUnit(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceUnit(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class<?>> classeswithPersistenceUnit = classFinder.findAnnotatedClasses(PersistenceUnit.class);
        List<Method> methodswithPersistenceUnit = classFinder.findAnnotatedMethods(PersistenceUnit.class);
        List<Field> fieldswithPersistenceUnit = classFinder.findAnnotatedFields(PersistenceUnit.class);

        // Class-level annotation
        for (Class cls : classeswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = (PersistenceUnit) cls.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = method.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldswithPersistenceUnit) {
            PersistenceUnit persistenceUnit = field.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                addPersistenceUnit(annotatedApp, persistenceUnit, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(annotatedApp);

        log.debug("processPersistenceUnit(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processPersistenceUnits(JndiConsumer annotatedApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processPersistenceUnits(): Entry");

        List<Class<?>> classeswithPersistenceUnits = classFinder.findAnnotatedClasses(PersistenceUnits.class);

        // Class-level annotation(s)
        List<PersistenceUnit> persistenceUnitList = new ArrayList<PersistenceUnit>();
        for (Class cls : classeswithPersistenceUnits) {
            PersistenceUnits persistenceUnits = (PersistenceUnits) cls.getAnnotation(PersistenceUnits.class);
            if (persistenceUnits != null) {
                persistenceUnitList.addAll(Arrays.asList(persistenceUnits.value()));
            }
            for (PersistenceUnit persistenceUnit : persistenceUnitList) {
                addPersistenceUnit(annotatedApp, persistenceUnit, cls, null, null);
            }
            persistenceUnitList.clear();
        }

        log.debug("processPersistenceUnits(): Exit");
    }


    /**
     * Add @PersistenceUnit and @PersistenceUnits annotations to the deployment descriptor. XMLBeans are used to
     * read and manipulate the deployment descriptor as necessary. The PersistenceUnit annotation(s) will be
     * converted to one of the following deployment descriptors:
     *
     * <ol>
     *      <li><persistence-unit-ref> -- Describes a single entity manager factory reference for the
     *          persistence unit
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *          existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotation @PersistenceUnit annotation
     * @param cls        Class name with the @PersistenceUnit annoation
     * @param method     Method name with the @PersistenceUnit annoation
     * @param field      Field name with the @PersistenceUnit annoation
     * @param annotatedApp  Access to the specc dd
     */
    private static void addPersistenceUnit(JndiConsumer annotatedApp, PersistenceUnit annotation, Class cls, Method method, Field field) {
        log.debug("addPersistenceUnit( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        //------------------------------------------------------------------------------------------
        // PersistenceUnitRef name:
        // -- When annotation is applied on a class:    Name must be provided (cannot be inferred)
        // -- When annotation is applied on a method:   Name is JavaBeans property name qualified
        //                                              by the class (or as provided on the
        //                                              annotation)
        // -- When annotation is applied on a field:    Name is the field name qualified by the
        //                                              class (or as provided on the annotation)
        //------------------------------------------------------------------------------------------
        String persistenceUnitRefName = getName(annotation.name(), method, field);

        log.debug("addPersistenceUnit(): persistenceUnitRefName: " + persistenceUnitRefName);

        // If there is already xml for the persistence unit ref, just add injection targets and return.
        Collection<PersistenceUnitRef> persistenceUnitRefs = annotatedApp.getPersistenceUnitRef();
        for (PersistenceUnitRef persistenceUnitRef : persistenceUnitRefs) {
            if (persistenceUnitRef.getPersistenceUnitRefName().trim().equals(persistenceUnitRefName)) {
                if (method != null || field != null) {
                    Set<InjectionTarget> targets = persistenceUnitRef.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        persistenceUnitRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }
                return;
            }
        }

        // Doesn't exist in deployment descriptor -- add new
        PersistenceUnitRef persistenceUnitRef = new PersistenceUnitRef();

        //------------------------------------------------------------------------------
        // <persistence-unit-ref> required elements:
        //------------------------------------------------------------------------------

        // persistence-unit-ref-name
        persistenceUnitRef.setPersistenceUnitRefName(persistenceUnitRefName);

        //------------------------------------------------------------------------------
        // <persistence-unit-ref> optional elements:
        //------------------------------------------------------------------------------

        // persistence-unit-name
        String unitNameAnnotation = annotation.unitName();
        if (!unitNameAnnotation.equals("")) {
            persistenceUnitRef.setPersistenceUnitName(unitNameAnnotation);
        }

        // injection targets
        if (method != null || field != null) {
            persistenceUnitRef.getInjectionTarget().add(configureInjectionTarget(method, field));
        }
        annotatedApp.getPersistenceUnitRef().add(persistenceUnitRef);
    }

}
