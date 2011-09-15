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
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBs;
import javax.ejb.Local;
import javax.ejb.Remote;

import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Text;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@EJB</strong> and <strong>@EJBs</strong> annotations to deployment descriptor tags. The
 * EJBAnnotationHelper class can be used as part of the deployment of a module into the Geronimo
 * server. It performs the following major functions:
 *
 * <ol>
 * <li>Translates annotations into corresponding deployment descriptor elements (so that the
 * actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 *
 * <p><strong>Note(s):</strong>
 * <ul>
 * <li>The user is responsible for invoking change to metadata-complete
 * <li>This helper class will validate any changes it makes to the deployment descriptor. An
 * exception will be thrown if it fails to parse
 * </ul>
 *
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 *      <li>Usage of mappedName
 * </ul>
 *
 * @version $Rev$ $Date$
 * @since 02-2007
 */

public final class EJBAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Logger log = LoggerFactory.getLogger(EJBAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private EJBAnnotationHelper() {
    }


    /**
     * Determine if there are any annotations present
     *
     * @return true or false
     */
    public static boolean annotationsPresent(AbstractFinder classFinder) {
        if (classFinder.isAnnotationPresent(EJB.class)) return true;
        if (classFinder.isAnnotationPresent(EJBs.class)) return true;
        return false;
    }


    /**
     * Process the annotations
     *
     * @return Updated deployment descriptor
     * @throws Exception if parsing or validation error
     */
    public static void processAnnotations(JndiConsumer annotatedApp, AbstractFinder classFinder) throws Exception {
        if (annotatedApp != null) {
            processEJBs(annotatedApp, classFinder);
            processEJB(annotatedApp, classFinder);
        }
    }


    /**
     * Process annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @throws Exception
     */
    private static void processEJB(JndiConsumer annotatedApp, AbstractFinder classFinder) throws Exception {
        log.debug("processEJB(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class<?>> classesWithEJB = classFinder.findAnnotatedClasses(EJB.class);
        List<Method> methodsWithEJB = classFinder.findAnnotatedMethods(EJB.class);
        List<Field> fieldsWithEJB = classFinder.findAnnotatedFields(EJB.class);

        // Class-level annotation
        for (Class cls : classesWithEJB) {
            EJB ejb = (EJB) cls.getAnnotation(EJB.class);
            if (ejb != null) {
                addEJB(annotatedApp, ejb, cls, null, null);
            }
        }

        // Method-level annotation
        for (Method method : methodsWithEJB) {
            EJB ejb = method.getAnnotation(EJB.class);
            if (ejb != null) {
                addEJB(annotatedApp, ejb, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldsWithEJB) {
            EJB ejb = field.getAnnotation(EJB.class);
            if (ejb != null) {
                addEJB(annotatedApp, ejb, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(annotatedApp);

        log.debug("processEJB(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @throws Exception
     */
    private static void processEJBs(JndiConsumer annotatedApp, AbstractFinder classFinder) throws Exception {
        log.debug("processEJBs(): Entry");

        List<Class<?>> classesWithEJBs = classFinder.findAnnotatedClasses(EJBs.class);

        // Class-level annotation(s)
        List<EJB> ejbList = new ArrayList<EJB>();
        for (Class cls : classesWithEJBs) {
            EJBs ejbs = (EJBs) cls.getAnnotation(EJBs.class);
            if (ejbs != null) {
                ejbList.addAll(Arrays.asList(ejbs.value()));
            }
            for (EJB ejb : ejbList) {
                addEJB(annotatedApp, ejb, cls, null, null);
            }
            ejbList.clear();
        }

        log.debug("processEJBs(): Exit");
    }


    /**
     * Add @EJB and @EJBs annotations to the deployment descriptor. XMLBeans are used to read and
     * manipulate the deployment descriptor as necessary. The EJB annotation(s) will be converted to
     * one of the following deployment descriptors if possible. Otherwise they will be listed as
     * ambiguous and resolved in OpenEJB.
     * <p/>
     * <ol>
     * <li><ejb-ref>
     * <li><ejb-local-ref>
     * </ol>
     * <p/>
     * <p><strong>Note(s):</strong>
     * <ul>
     * <li>The deployment descriptor is the authoritative source so this method ensures that
     * existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param annotatedApp
     * @param annotation   @EJB annotation
     * @param cls          Class name with the @EJB annoation
     * @param method       Method name with the @EJB annoation
     * @param field        Field name with the @EJB annoation
     */
    private static void addEJB(JndiConsumer annotatedApp, EJB annotation, Class cls, Method method, Field field) {
        log.debug("addEJB( [annotatedApp] " + annotatedApp.toString() + "," + '\n' +
                "[annotation] " + annotation.toString() + "," + '\n' +
                "[cls] " + (cls != null ? cls.getName() : null) + "," + '\n' +
                "[method] " + (method != null ? method.getName() : null) + "," + '\n' +
                "[field] " + (field != null ? field.getName() : null) + " ): Entry");

        // First determine if the interface is "Local" or "Remote" (if we can--we may not be able to)
        boolean localFlag = false;
        boolean remoteFlag = false;
        Class interfce = annotation.beanInterface();
        if (interfce.equals(Object.class)) {
            if (method != null) {
                interfce = method.getParameterTypes()[0];
            } else if (field != null) {
                interfce = field.getType();
            } else {
                interfce = null;
            }
        }
        log.debug("addEJB(): interfce: " + interfce);

        // Just in case local and/or remote homes are still being implemented (even though
        // they are optional in EJB 3.0)
        if (interfce != null && !interfce.equals(Object.class)) {
            if (EJBHome.class.isAssignableFrom(interfce)) {
                for (Method m : interfce.getMethods()) {
                    if (m.getName().startsWith("create")) {
                        interfce = m.getReturnType();
                        break;
                    }
                }
                remoteFlag = true;
            } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                for (Method m : interfce.getMethods()) {
                    if (m.getName().startsWith("create")) {
                        interfce = m.getReturnType();
                        break;
                    }
                }
                localFlag = true;
            } else {
                if (interfce.getAnnotation(Local.class) != null) {
                    localFlag = true;
                } else if (interfce.getAnnotation(Remote.class) != null) {
                    remoteFlag = true;
                }
            }
        }
        log.debug("addEJB(): localFlag: " + localFlag);
        log.debug("addEJB(): remoteFlag: " + remoteFlag);

        //------------------------------------------------------------------------------------------
        // 1. <ejb-local-ref>
        //------------------------------------------------------------------------------------------
        if (localFlag) {

            log.debug("addEJB(): <ejb-local-ref> found");

            String localRefName = getName(annotation.name(), method, field);

            EjbLocalRef ejbLocalRef = annotatedApp.getEjbLocalRefMap().get(getJndiName(localRefName));

            if (ejbLocalRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + localRefName);

                    // Doesn't exist in deployment descriptor -- add new
                    ejbLocalRef = new EjbLocalRef();

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    ejbLocalRef.setEjbRefName(localRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // local
                    if (interfce != null) {
                        String localAnnotation = interfce.getName();
                        if (!localAnnotation.isEmpty()) {
                            ejbLocalRef.setLocal(localAnnotation);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.isEmpty()) {
                        ejbLocalRef.setEjbLink(beanName);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.isEmpty()) {
                        ejbLocalRef.setMappedName(mappdedNameAnnotation);
                    }

                    // lookup
                    String lookupName = annotation.lookup();
                    if (!lookupName.isEmpty()) {
                        ejbLocalRef.setLookupName(lookupName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.isEmpty()) {
                        ejbLocalRef.setDescriptions(new Text[] {new Text(null, descriptionAnnotation)});
                    }

                    ejbLocalRef.setRefType(EjbReference.Type.LOCAL);
                    annotatedApp.getEjbLocalRef().add(ejbLocalRef);
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <ejb-local-ref>", e);
                }
            }

            // injectionTarget
            if (method != null || field != null) {
                Set<InjectionTarget> targets = ejbLocalRef.getInjectionTarget();
                if (!hasTarget(method, field, targets)) {
                    ejbLocalRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                }
            }
        }                                                                           // end if local
        else if (remoteFlag) {                                                      // remote

            //--------------------------------------------------------------------------------------
            // 2. <ejb-ref>
            //--------------------------------------------------------------------------------------

            log.debug("addEJB(): <ejb-ref> found");

            String remoteRefName = getName(annotation.name(), method, field);

            EjbRef ejbRef = annotatedApp.getEjbRefMap().get(getJndiName(remoteRefName));

            if (ejbRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + remoteRefName);

                    // Doesn't exist in deployment descriptor -- add new
                    ejbRef = new EjbRef();

                    //------------------------------------------------------------------------------
                    // <ejb-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    ejbRef.setEjbRefName(remoteRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // remote
                    if (interfce != null) {
                        String remoteAnnotation = interfce.getName();
                        if (!remoteAnnotation.isEmpty()) {
                            ejbRef.setRemote(remoteAnnotation);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.isEmpty()) {
                        ejbRef.setEjbLink(beanName);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.isEmpty()) {
                        ejbRef.setMappedName(mappdedNameAnnotation);
                    }

                    // lookup
                    String lookupName = annotation.lookup();
                    if (!lookupName.isEmpty()) {
                        ejbRef.setLookupName(lookupName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.isEmpty()) {
                        ejbRef.setDescriptions(new Text[] {new Text(null, descriptionAnnotation) });
                    }

                    ejbRef.setRefType(EjbReference.Type.REMOTE);
                    annotatedApp.getEjbRef().add(ejbRef);
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <ejb-ref>", e);
                }
            }

            // injectionTarget
            if (method != null || field != null) {
                Set<InjectionTarget> targets = ejbRef.getInjectionTarget();
                if (!hasTarget(method, field, targets)) {
                    ejbRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                }
            }
        }                                                                           // end if remote
        else {                                                                      // ambiguous

            //--------------------------------------------------------------------------------------
            // 3. <UNKNOWN>
            //--------------------------------------------------------------------------------------
            log.debug("addEJB(): <UNKNOWN> found");

            String remoteRefName = getName(annotation.name(), method, field);

            EjbRef ejbRef = annotatedApp.getEjbRefMap().get(getJndiName(remoteRefName));

            if (ejbRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + remoteRefName);

                    // Doesn't exist in deployment descriptor -- add as an <ejb-ref> to the
                    // ambiguous list so that it can be resolved later
                    ejbRef = new EjbRef();

                    //------------------------------------------------------------------------------
                    // <ejb-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    ejbRef.setEjbRefName(remoteRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // remote
                    if (interfce != null) {
                        String remoteAnnotation = interfce.getName();
                        if (!remoteAnnotation.isEmpty()) {
                            ejbRef.setRemote(remoteAnnotation);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.isEmpty()) {
                        ejbRef.setEjbLink(beanName);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.isEmpty()) {
                        ejbRef.setMappedName(mappdedNameAnnotation);
                    }

                    // lookup
                    String lookupName = annotation.lookup();
                    if (!lookupName.isEmpty()) {
                        ejbRef.setLookupName(lookupName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.isEmpty()) {
                        ejbRef.setDescriptions(new Text[] {new Text(null, descriptionAnnotation) });
                    }

                    ejbRef.setRefType(EjbReference.Type.UNKNOWN);
                    //openejb sorts out ambiguous ejb refs.
                    annotatedApp.getEjbRef().add(ejbRef);
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <UNKNOWN>", e);
                }
            }

            // injectionTarget
            if (method != null || field != null) {
                Set<InjectionTarget> targets = ejbRef.getInjectionTarget();
                if (!hasTarget(method, field, targets)) {
                    ejbRef.getInjectionTarget().add(configureInjectionTarget(method, field));
                }
            }

        }
        log.debug("addEJB(): Exit");
    }

}
