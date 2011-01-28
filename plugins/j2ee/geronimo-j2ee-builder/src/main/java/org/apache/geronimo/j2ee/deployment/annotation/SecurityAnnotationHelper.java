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

import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.servlet.Servlet;
import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.AbstractFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Static helper class used to encapsulate all the functions related to the translation of
 * <strong>@DeclareRoles</strong> and <strong>@RunAs</strong> annotations to deployment
 * descriptor tags. The SecurityAnnotationHelper class can be used as part of the deployment of a
 * module into the Geronimo server. It performs the following major functions:
 *
 * <ol>
 *      <li>Translates annotations into corresponding deployment descriptor elements (so that the
 *          actual deployment descriptor in the module can be updated or even created if necessary)
 * </ol>
 *
 * <p><strong>Note(s):</strong>
 * <ul>
 *      <li>Supports only servlets
 *      <li>The user is responsible for invoking change to metadata-complete
 *      <li>This helper class will validate any changes it makes to the deployment descriptor. An
 *          exception will be thrown if it fails to parse
 * </ul>
 *
 * @version $Rev $Date
 * @since 04-2007
 */
public final class SecurityAnnotationHelper extends AnnotationHelper {

    // Private instance variables
    private static final Logger log = LoggerFactory.getLogger(SecurityAnnotationHelper.class);

    // Private constructor to prevent instantiation
    private SecurityAnnotationHelper() {
    }

    /**
     * Update the deployment descriptor from the DeclareRoles and RunAs annotations
     *
     * @param webApp Access to the spec dd
     * @param classFinder  Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    public static void processAnnotations(WebApp webApp, AbstractFinder classFinder) throws DeploymentException {
        if (webApp != null && classFinder != null) {
            if (classFinder.isAnnotationPresent(DeclareRoles.class)) {
                processDeclareRoles(webApp, classFinder);
            }
            if (classFinder.isAnnotationPresent(RunAs.class)) {
                processRunAs(webApp, classFinder);
            }
        }
    }


    /**
     * Process @DeclareRole annotations (for servlets only)
     *
     * @param webApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processDeclareRoles(WebApp webApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processDeclareRoles(): Entry: webApp: " + webApp.toString());

        List<Class<?>> classesWithDeclareRoles = classFinder.findAnnotatedClasses(DeclareRoles.class);

        // Class-level annotation
        for (Class cls : classesWithDeclareRoles) {
            DeclareRoles declareRoles = (DeclareRoles) cls.getAnnotation(DeclareRoles.class);
            if (declareRoles != null && Servlet.class.isAssignableFrom(cls)) {
                addDeclareRoles(webApp, declareRoles, cls);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(new AnnotatedWebApp(webApp));

        log.debug("processDeclareRoles(): Exit: webApp: " + webApp.toString());
    }


    /**
     * Process @RunAs annotations (for servlets only)
     *
     * @param webApp Access to the spec dd
     * @param classFinder Access to the classes of interest
     * @throws DeploymentException if parsing or validation error
     */
    private static void processRunAs(WebApp webApp, AbstractFinder classFinder) throws DeploymentException {
        log.debug("processRunAs(): Entry: webApp: " + webApp.toString());

        List<Class<?>> classesWithRunAs = classFinder.findAnnotatedClasses(RunAs.class);

        // Class-level annotation
        for (Class cls : classesWithRunAs) {
            RunAs runAs = (RunAs) cls.getAnnotation(RunAs.class);
            if (runAs != null && Servlet.class.isAssignableFrom(cls)) {
                addRunAs(webApp, runAs, cls);
            }
        }

        // Validate deployment descriptor to ensure it's still okay
//        validateDD(new AnnotatedWebApp(webApp));

        log.debug("processRunAs(): Exit: webApp: " + webApp.toString());
    }


    /**
     * Add @DeclareRoles annotations to the deployment descriptor. XMLBeans are used to read and
     * manipulate the deployment descriptor as necessary. The DeclareRoles annotation(s) will be
     * converted to one of the following deployment descriptors:
     *
     * <ol>
     *      <li><security-role> -- Describes a single security role
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *          existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param webApp  Access to the spec dd
     * @param annotation    @DeclareRoles annotation
     * @param cls           Class name with the @DeclareRoles annoation
     */
    private static void addDeclareRoles(WebApp webApp, DeclareRoles annotation, Class cls) {
        log.debug("addDeclareRoles( [webApp] " + webApp.toString() + "," + '\n' +
                  "[annotation] " + annotation.toString() + "," + '\n' +
                  "[cls] " + (cls != null ? cls.getName() : null) + "): Entry");

        // Get all the <security-role> tags from the deployment descriptor
        List<SecurityRole> securityRoles = webApp.getSecurityRole();

        String[] annotationRoleNames = annotation.value();
        for (String annotationRoleName : annotationRoleNames) {
            if (!annotationRoleName.equals("")) {
                boolean exists = false;
                for (SecurityRole securityRole : securityRoles) {
                    if (securityRole.getRoleName().trim().equals(annotationRoleName)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    log.debug("addDeclareRoles: <security-role> entry found: " + annotationRoleName);
                }
                else {
                    log.debug("addDeclareRoles: <security-role> entry NOT found: " + annotationRoleName);
                    SecurityRole securityRole = new SecurityRole();
                    securityRole.setRoleName(annotationRoleName);
                    webApp.getSecurityRole().add(securityRole);
                }
            }
        }

        log.debug("addDeclareRoles(): Exit");
    }


    /**
     * Add @RunAs annotations to the deployment descriptor. XMLBeans are used to read and manipulate
     * the deployment descriptor as necessary. The DeclareRoles annotation(s) will be converted to
     * one of the following deployment descriptors:
     *
     * <ol>
     *      <li><run-as> -- Describes a run-as security identity to be used for the execution of a
     *      component
     * </ol>
     *
     * <p><strong>Note(s):</strong>
     * <ul>
     *      <li>The deployment descriptor is the authoritative source so this method ensures that
     *          existing elements in it are not overwritten by annoations
     * </ul>
     *
     * @param webApp Access to the spec dd
     * @param annotation    @RunAs annotation
     * @param cls           Class name with the @RunAs annoation
     */
    private static void addRunAs(WebApp webApp, RunAs annotation, Class cls) {
        log.debug("addRunAs( [webApp] " + webApp.toString() + "," + '\n' +
                  "[annotation] " + annotation.toString() + "," + '\n' +
                  "[cls] " + (cls != null ? cls.getName() : null) + "): Entry");

        String annotationRunAs = annotation.value();
        if (!annotationRunAs.equals("")) {
            List<org.apache.openejb.jee.Servlet> servlets = webApp.getServlet();
            boolean exists = false;
            for (org.apache.openejb.jee.Servlet servlet : servlets) {
                if (servlet.getServletClass().trim().equals(cls.getName())) {
                    if (servlet.getRunAs() == null) {
                        org.apache.openejb.jee.RunAs runAs = new org.apache.openejb.jee.RunAs();
                        runAs.setRoleName(annotationRunAs);
                        servlet.setRunAs(runAs);
                    }
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                log.warn("RunAs servlet not found in webApp: " + cls.getName());
            }
        }

        log.debug("addRunAs(): Exit");
    }

}
