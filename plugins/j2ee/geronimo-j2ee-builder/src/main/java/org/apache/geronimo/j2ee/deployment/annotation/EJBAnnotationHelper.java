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

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBs;
import javax.ejb.Local;
import javax.ejb.Remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.xbeans.javaee.DescriptionType;
import org.apache.geronimo.xbeans.javaee.EjbLinkType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefNameType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.LocalType;
import org.apache.geronimo.xbeans.javaee.RemoteType;
import org.apache.geronimo.xbeans.javaee.XsdStringType;
import org.apache.xbean.finder.ClassFinder;

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
    public static boolean annotationsPresent(ClassFinder classFinder) {
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
    public static void processAnnotations(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
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
    private static void processEJB(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processEJB(): Entry: AnnotatedApp: " + annotatedApp.toString());

        List<Class> classesWithEJB = classFinder.findAnnotatedClasses(EJB.class);
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
            EJB ejb = (EJB) method.getAnnotation(EJB.class);
            if (ejb != null) {
                addEJB(annotatedApp, ejb, null, method, null);
            }
        }

        // Field-level annotation
        for (Field field : fieldsWithEJB) {
            EJB ejb = (EJB) field.getAnnotation(EJB.class);
            if (ejb != null) {
                addEJB(annotatedApp, ejb, null, null, field);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
        validateDD(annotatedApp);

        log.debug("processEJB(): Exit: AnnotatedApp: " + annotatedApp.toString());
    }


    /**
     * Process multiple annotations
     *
     * @param annotatedApp
     * @param classFinder
     * @throws Exception
     */
    private static void processEJBs(AnnotatedApp annotatedApp, ClassFinder classFinder) throws Exception {
        log.debug("processEJBs(): Entry");

        List<Class> classesWithEJBs = classFinder.findAnnotatedClasses(EJBs.class);

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
    private static void addEJB(AnnotatedApp annotatedApp, EJB annotation, Class cls, Method method, Field field) {
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

            EjbLocalRefType ejbLocalRef = null;
            
            EjbLocalRefType[] ejbLocalRefEntries = annotatedApp.getEjbLocalRefArray();
            for (EjbLocalRefType ejbLocalRefEntry : ejbLocalRefEntries) {
                if (ejbLocalRefEntry.getEjbRefName().getStringValue().trim().equals(localRefName)) {
                    ejbLocalRef = ejbLocalRefEntry;
                    break;
                }
            }
            if (ejbLocalRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + localRefName);

                    // Doesn't exist in deployment descriptor -- add new
                    ejbLocalRef = annotatedApp.addNewEjbLocalRef();

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    EjbRefNameType ejbRefName = ejbLocalRef.addNewEjbRefName();
                    ejbRefName.setStringValue(localRefName);
                    ejbLocalRef.setEjbRefName(ejbRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-local-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // local
                    if (interfce != null) {
                        String localAnnotation = interfce.getName();
                        if (!localAnnotation.equals("")) {
                            LocalType local = ejbLocalRef.addNewLocal();
                            local.setStringValue(localAnnotation);
                            ejbLocalRef.setLocal(local);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.equals("")) {
                        EjbLinkType ejbLink = ejbLocalRef.addNewEjbLink();
                        ejbLink.setStringValue(beanName);
                        ejbLocalRef.setEjbLink(ejbLink);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.equals("")) {
                        XsdStringType mappedName = ejbLocalRef.addNewMappedName();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        ejbLocalRef.setMappedName(mappedName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.equals("")) {
                        DescriptionType description = ejbLocalRef.addNewDescription();
                        description.setStringValue(descriptionAnnotation);
                    }
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <ejb-local-ref>", e);
                }
            }
            
            // injectionTarget
            if (method != null || field != null) {
                InjectionTargetType[] targets = ejbLocalRef.getInjectionTargetArray();
                if (!hasTarget(method, field, targets)) {
                    configureInjectionTarget(ejbLocalRef.addNewInjectionTarget(), method, field);
                }
            }
        }                                                                           // end if local
        else if (remoteFlag) {                                                      // remote

            //--------------------------------------------------------------------------------------
            // 2. <ejb-ref>
            //--------------------------------------------------------------------------------------

            log.debug("addEJB(): <ejb-ref> found");

            String remoteRefName = getName(annotation.name(), method, field);

            EjbRefType ejbRef = null;

            EjbRefType[] ejbRefEntries = annotatedApp.getEjbRefArray();
            for (EjbRefType ejbRefEntry : ejbRefEntries) {
                if (ejbRefEntry.getEjbRefName().getStringValue().trim().equals(remoteRefName)) {
                    ejbRef = ejbRefEntry;
                    break;
                }
            }
            if (ejbRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + remoteRefName);

                    // Doesn't exist in deployment descriptor -- add new
                    ejbRef = annotatedApp.addNewEjbRef();

                    //------------------------------------------------------------------------------
                    // <ejb-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    EjbRefNameType ejbRefName = ejbRef.addNewEjbRefName();
                    ejbRefName.setStringValue(remoteRefName);
                    ejbRef.setEjbRefName(ejbRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // remote
                    if (interfce != null) {
                        String remoteAnnotation = interfce.getName();
                        if (!remoteAnnotation.equals("")) {
                            RemoteType remote = ejbRef.addNewRemote();
                            remote.setStringValue(remoteAnnotation);
                            ejbRef.setRemote(remote);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.equals("")) {
                        EjbLinkType ejbLink = ejbRef.addNewEjbLink();
                        ejbLink.setStringValue(beanName);
                        ejbRef.setEjbLink(ejbLink);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.equals("")) {
                        XsdStringType mappedName = ejbRef.addNewMappedName();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        ejbRef.setMappedName(mappedName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.equals("")) {
                        DescriptionType description = ejbRef.addNewDescription();
                        description.setStringValue(descriptionAnnotation);
                    }
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <ejb-ref>", e);
                }
            }
            
            // injectionTarget
            if (method != null || field != null) {
                InjectionTargetType[] targets = ejbRef.getInjectionTargetArray();
                if (!hasTarget(method, field, targets)) {
                    configureInjectionTarget(ejbRef.addNewInjectionTarget(), method, field);
                }
            }
        }                                                                           // end if remote
        else {                                                                      // ambiguous

            //--------------------------------------------------------------------------------------
            // 3. <UNKNOWN>
            //--------------------------------------------------------------------------------------
            log.debug("addEJB(): <UNKNOWN> found");

            String remoteRefName = getName(annotation.name(), method, field);

            EjbRefType ejbRef = null;

            EjbRefType[] ejbRefEntries = annotatedApp.getEjbRefArray();
            for (EjbRefType ejbRefEntry : ejbRefEntries) {
                if (ejbRefEntry.getEjbRefName().getStringValue().trim().equals(remoteRefName)) {
                    ejbRef = ejbRefEntry;
                    break;
                }
            }
            if (ejbRef == null) {
                try {

                    log.debug("addEJB(): Does not exist in DD: " + remoteRefName);

                    // Doesn't exist in deployment descriptor -- add as an <ejb-ref> to the
                    // ambiguous list so that it can be resolved later
                    ejbRef = EjbRefType.Factory.newInstance();
                    annotatedApp.getAmbiguousEjbRefs().add(ejbRef);

                    //------------------------------------------------------------------------------
                    // <ejb-ref> required elements:
                    //------------------------------------------------------------------------------

                    // ejb-ref-name
                    EjbRefNameType ejbRefName = ejbRef.addNewEjbRefName();
                    ejbRefName.setStringValue(remoteRefName);
                    ejbRef.setEjbRefName(ejbRefName);

                    //------------------------------------------------------------------------------
                    // <ejb-ref> optional elements:
                    //------------------------------------------------------------------------------

                    // remote
                    if (interfce != null) {
                        String remoteAnnotation = interfce.getName();
                        if (!remoteAnnotation.equals("")) {
                            RemoteType remote = ejbRef.addNewRemote();
                            remote.setStringValue(remoteAnnotation);
                            ejbRef.setRemote(remote);
                        }
                    }

                    // ejb-link
                    String beanName = annotation.beanName();
                    if (!beanName.equals("")) {
                        EjbLinkType ejbLink = ejbRef.addNewEjbLink();
                        ejbLink.setStringValue(beanName);
                        ejbRef.setEjbLink(ejbLink);
                    }

                    // mappedName
                    String mappdedNameAnnotation = annotation.mappedName();
                    if (!mappdedNameAnnotation.equals("")) {
                        XsdStringType mappedName = ejbRef.addNewMappedName();
                        mappedName.setStringValue(mappdedNameAnnotation);
                        ejbRef.setMappedName(mappedName);
                    }

                    // description
                    String descriptionAnnotation = annotation.description();
                    if (!descriptionAnnotation.equals("")) {
                        DescriptionType description = ejbRef.addNewDescription();
                        description.setStringValue(descriptionAnnotation);
                    }
                }
                catch (Exception e) {
                    log.debug("EJBAnnotationHelper: Exception caught while processing <UNKNOWN>", e);
                }                
            }
            
            // injectionTarget
            if (method != null || field != null) {
                InjectionTargetType[] targets = ejbRef.getInjectionTargetArray();
                if (!hasTarget(method, field, targets)) {
                    configureInjectionTarget(ejbRef.addNewInjectionTarget(), method, field);
                }
            }

        }
        log.debug("addEJB(): Exit");
    }

}
