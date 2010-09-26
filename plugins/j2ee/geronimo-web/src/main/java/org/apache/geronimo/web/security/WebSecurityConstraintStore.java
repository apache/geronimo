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

package org.apache.geronimo.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.geronimo.web.info.AuthConstraintInfo;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.info.WebResourceCollectionInfo;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class WebSecurityConstraintStore {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConstraintStore.class);

    private boolean annotationScanRequired;

    private Bundle bundle;

    /**
     * containerCreatedDynamicServlets contains all instance created using ServletContext.createServlet(Class<T> c)
     */
    private Map<javax.servlet.Servlet, String> containerCreatedDynamicServlets = new IdentityHashMap<javax.servlet.Servlet, String>();

    /**
     * containerCreatedDynamicServletNameClassMap contains all servlets programmatically added using ServletContext.addServlet(), unless addServlet takes
     * instance which is not created using ServletContext.createServlet() method
     */
    private Map<String, String> containerCreatedDynamicServletNameClassMap = new HashMap<String, String>();

    private Map<String, ServletSecurityElement> dynamicServletNameSecurityElementMap = new LinkedHashMap<String, ServletSecurityElement>();
    private Map<RegistrationKey, ServletSecurityElement> registrationSecurityElementMap = new LinkedHashMap<RegistrationKey, ServletSecurityElement>();

    private Set<String> securityRoles = new HashSet<String>();

    private ServletContext servletContext;

    private WebAppInfo webXmlAppInfo;

    //Contains all the url patterns configured in the security-constraint from the web.xml file
    private Set<String> webXmlConstraintUrlPatterns = new HashSet<String>();

    public WebSecurityConstraintStore(WebAppInfo webXmlAppInfo) {
        this(webXmlAppInfo, null, false, null);
    }

    public WebSecurityConstraintStore(WebAppInfo webXmlAppInfo, Bundle bundle, boolean annotationScanRequired, ServletContext servletContext) {
        this.webXmlAppInfo = webXmlAppInfo;
        if (annotationScanRequired && bundle == null) {
            throw new IllegalArgumentException("Bundle parameter could not be null while annotation scanning is required");
        }
        this.bundle = bundle;
        this.servletContext = servletContext;
        this.annotationScanRequired = annotationScanRequired;
        initialize();
    }

    public void setAnnotationScanRequired(boolean scanRequired) {
        annotationScanRequired = scanRequired;
    }

    public void addContainerCreatedDynamicServlet(javax.servlet.Servlet servlet) {
        containerCreatedDynamicServlets.put(servlet, null);
    }

    public boolean isContainerCreatedDynamicServlet(javax.servlet.Servlet servlet) {
        return containerCreatedDynamicServlets.containsKey(servlet);
    }

    public void addContainerCreatedDynamicServletEntry(ServletRegistration.Dynamic registration, String servletClass) {
        if (annotationScanRequired) {
            ServletSecurityElement servletSecurityElement = processServletConstraintAnnotation(servletClass);
            if (servletSecurityElement != null) {
                setDynamicServletSecurity(registration, servletSecurityElement);
            }
        }
    }

    public void addContainerCreatedDynamicServletEntry(String servletName, String servletClass) {
        containerCreatedDynamicServletNameClassMap.put(servletName, servletClass);
    }

    public void declareRoles(String... roleNames) {
        for (String roleName : roleNames) {
            if (roleName == null || roleName.trim().length() == 0) {
                throw new IllegalArgumentException("RoleName of null value or empty string is not allowed in declareRoles method");
            }
            securityRoles.add(roleName);
        }
    }

    public WebAppInfo exportMergedWebAppInfo() {

        /**
         *  a. The security constraints in the portable deployment descriptor are of the highest priority,
         *  b .None security annotations will take effect on the URL patterns explicitly configured in the portable deployment desciptor,
         *      but for those URL patterns are not configured, the security annotations should take effect, except for META-COMPLETE is set with TRUE
         *  c. All the dynamic added servlets should take care the ServletSecurity annotation, two exceptions are :
                c1. Users create the servlet by themselves
                c2. ServletRegistration.Dynamic.setServletConstraint is called
         *  d. For those URL patterns added by ServletContext.getServletRegistration().addMappping, and those target servlets are configured
         *      in the portable deployment plan, ServletSecurity annotation should also be taken care, except for META-COMPLETE is set with TRUE
         */
        List<SecurityConstraintInfo> securityConstraints = new ArrayList<SecurityConstraintInfo>();
        //Scan ServletSecurity annotation if required
        if (annotationScanRequired) {
            //these will already have been added and be in the containerCreatedDynamicServletNameClassMap
//            for (ServletInfo servlet : webXmlAppInfo.servlets) {
//                Collection<String> urlPatterns = servletContext.getServletRegistration(servlet.servletName).getMappings();
//                urlPatterns.removeAll(webXmlConstraintUrlPatterns);
//                processServletConstraintAnnotation(securityConstraints, servlet.servletName, servlet.servletClass, urlPatterns);
//            }

            for (Map.Entry<String, String> entry : containerCreatedDynamicServletNameClassMap.entrySet()) {
                String servletName = entry.getKey();
                Collection<String> urlPatterns = servletContext.getServletRegistration(servletName).getMappings();
                urlPatterns.removeAll(webXmlConstraintUrlPatterns);
                if (!dynamicServletNameSecurityElementMap.containsKey(servletName)) {
                    processServletConstraintAnnotation(securityConstraints, servletName, entry.getValue(), urlPatterns);
                }
            }
        }

        //Process servlet security for dynamically added servlets
        for (Map.Entry<String, ServletSecurityElement> entry : dynamicServletNameSecurityElementMap.entrySet()) {
            Collection<String> urlPatterns = servletContext.getServletRegistration(entry.getKey()).getMappings();
            urlPatterns.removeAll(webXmlConstraintUrlPatterns);
            processServletSecurityElement(securityConstraints, entry.getValue(), urlPatterns);
        }
        for (Map.Entry<RegistrationKey, ServletSecurityElement> entry : registrationSecurityElementMap.entrySet()) {
            Collection<String> urlPatterns = entry.getKey().registration.getMappings();
            urlPatterns.removeAll(webXmlConstraintUrlPatterns);
            processServletSecurityElement(securityConstraints, entry.getValue(), urlPatterns);
        }

        webXmlAppInfo.securityConstraints.addAll(securityConstraints);
        return webXmlAppInfo;
    }

    public Set<String> setDynamicServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement constraint) {
        registrationSecurityElementMap.put(new RegistrationKey(registration), constraint);
        Set<String> uneffectedUrlPatterns = new HashSet<String>(registration.getMappings());
        uneffectedUrlPatterns.retainAll(webXmlConstraintUrlPatterns);
        return uneffectedUrlPatterns;
    }
    public Set<String> setDynamicServletSecurity(String servletName, ServletSecurityElement constraint, Collection<String> urlPatterns) {
        dynamicServletNameSecurityElementMap.put(servletName, constraint);
        Set<String> uneffectedUrlPatterns = new HashSet<String>(urlPatterns);
        uneffectedUrlPatterns.retainAll(webXmlConstraintUrlPatterns);
        return uneffectedUrlPatterns;
    }

    private void initialize() {
        for (SecurityConstraintInfo secuirtyConstraint : webXmlAppInfo.securityConstraints) {
            for (WebResourceCollectionInfo webResourceCollection : secuirtyConstraint.webResourceCollections) {
                webXmlConstraintUrlPatterns.addAll(webResourceCollection.urlPatterns);
            }
        }
    }

    private SecurityConstraintInfo newHTTPMethodSecurityConstraint(String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic,
            String httpMethod, Collection<String> urlPatterns) {
        SecurityConstraintInfo securityConstraint = newSecurityConstraint(rolesAllowed, transportGuarantee, emptyRoleSemantic, true);
        WebResourceCollectionInfo webResourceCollection = securityConstraint.webResourceCollections.get(0);
        webResourceCollection.urlPatterns.addAll(urlPatterns);
        webResourceCollection.httpMethods.add(httpMethod);
        return securityConstraint;
    }

    private SecurityConstraintInfo newHTTPSecurityConstraint(String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic,
            Collection<String> omissionMethods, Collection<String> urlPatterns) {
        SecurityConstraintInfo securityConstraint = newSecurityConstraint(rolesAllowed, transportGuarantee, emptyRoleSemantic, !omissionMethods.isEmpty());
        if (securityConstraint != null) {
            WebResourceCollectionInfo webResourceCollection = securityConstraint.webResourceCollections.get(0);
            webResourceCollection.httpMethods.addAll(omissionMethods);
            webResourceCollection.urlPatterns.addAll(urlPatterns);
            webResourceCollection.omission = true;
        }
        return securityConstraint;
    }

    private SecurityConstraintInfo newSecurityConstraint(String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, boolean force) {
        //IF emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE then
        //  No Constraint
        //END IF
        if (force || rolesAllowed.length > 0 || transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL) || emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
            SecurityConstraintInfo securityConstraint = new SecurityConstraintInfo();
            WebResourceCollectionInfo webResourceCollection = new WebResourceCollectionInfo();
            securityConstraint.webResourceCollections.add(webResourceCollection);
            if (transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL)) {
                securityConstraint.userDataConstraint = TransportGuarantee.CONFIDENTIAL.name();
            }
            if (emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                securityConstraint.authConstraint = new AuthConstraintInfo();
            } else if (rolesAllowed.length > 0) {
                //When rolesAllowed.length == 0 and emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.PERMIT), no need to create the AuthConstraint object, as it means deny all
                AuthConstraintInfo authConstraint = new AuthConstraintInfo();
                for (String roleAllowed : rolesAllowed) {
                    authConstraint.roleNames.add(roleAllowed);
                }
                securityConstraint.authConstraint = authConstraint;
            }
            return securityConstraint;
        }
        return null;
    }

    private void processServletConstraintAnnotation(List<SecurityConstraintInfo> securityConstraints, String servletName, String servletClassName, Collection<String> urlPatterns) {
        try {
            Class<?> cls = bundle.loadClass(servletClassName);
            if (!javax.servlet.Servlet.class.isAssignableFrom(cls)) {
                return;
            }
            ServletSecurity servletSecurity = cls.getAnnotation(ServletSecurity.class);
            if (servletSecurity == null) {
                return;
            }
            if (urlPatterns.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No url pattern for the servlet class " + servletClassName + " is found in the deployment plan, SecurityConstraint annotation is ignored");
                }
                return;
            }
            processServletSecurityAnnotation(securityConstraints, servletSecurity, urlPatterns);
        } catch (ClassNotFoundException e) {
            //Should never occur, as webservice builder  have already checked it.
            logger.error("Fail to load class", e);
        }
    }

    private ServletSecurityElement processServletConstraintAnnotation(String servletClassName) {
        try {
            Class<?> cls = bundle.loadClass(servletClassName);
            if (!javax.servlet.Servlet.class.isAssignableFrom(cls)) {
                return null;
            }
            ServletSecurity servletSecurity = cls.getAnnotation(ServletSecurity.class);
            if (servletSecurity == null) {
                return null;
            }
            return new ServletSecurityElement(servletSecurity);
        } catch (ClassNotFoundException e) {
            //Should never occur, as webservice builder  have already checked it.
            logger.error("Fail to load class", e);
        }
        return null;
    }

    private void processServletSecurityAnnotation(List<SecurityConstraintInfo> securityConstraints, ServletSecurity servletSecurity, Collection<String> urlPatterns) {
        processServletSecurityElement(securityConstraints, new ServletSecurityElement(servletSecurity), urlPatterns);
    }

    private void processServletSecurityElement(List<SecurityConstraintInfo> securityConstraints, ServletSecurityElement servletSecurityElement, Collection<String> urlPatterns) {
        if (servletSecurityElement.getHttpMethodConstraints().size() > 0) {
            for (HttpMethodConstraintElement httpMethodConstraint : servletSecurityElement.getHttpMethodConstraints()) {
                //Generate a security-constraint for each HttpMethodConstraint
                SecurityConstraintInfo securityConstraint = newHTTPMethodSecurityConstraint(httpMethodConstraint.getRolesAllowed(), httpMethodConstraint.getTransportGuarantee(),
                        httpMethodConstraint.getEmptyRoleSemantic(), httpMethodConstraint.getMethodName(), urlPatterns);
                if (securityConstraint != null) {
                    securityConstraints.add(securityConstraint);
                }
                declareRoles(httpMethodConstraint.getRolesAllowed());
            }
        }
        SecurityConstraintInfo securityConstraint = newHTTPSecurityConstraint(servletSecurityElement.getRolesAllowed(), servletSecurityElement.getTransportGuarantee(),
                servletSecurityElement.getEmptyRoleSemantic(), servletSecurityElement.getMethodNames(), urlPatterns);
        if (securityConstraint != null) {
            securityConstraints.add(securityConstraint);
        }
        declareRoles(servletSecurityElement.getRolesAllowed());
    }

    private final static class RegistrationKey {
        private final ServletRegistration.Dynamic registration;

        private RegistrationKey(ServletRegistration.Dynamic registration) {
            this.registration = registration;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof RegistrationKey) &&
                    registration.getName().equals(((RegistrationKey)o).registration.getName());
        }

        @Override
        public int hashCode() {
            return registration.getName().hashCode();
        }
    }

}
