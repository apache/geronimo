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

package org.apache.geronimo.web25.deployment.merge.annotation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletMappingMergeHandler;
import org.apache.geronimo.xbeans.javaee6.AuthConstraintType;
import org.apache.geronimo.xbeans.javaee6.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.ServletType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebResourceCollectionType;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ServletSecurityAnnotationMergeHandler implements AnnotationMergeHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServletSecurityAnnotationMergeHandler.class);

    @Override
    public void merge(Class<?>[] classes, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        try {
            Bundle bundle = mergeContext.getBundle();
            Map<String, Set<String>> servletClassNameUrlPatternsMap = genetateServletClassUrlPatternsMap(webApp, mergeContext);
            for (ServletType servlet : webApp.getServletArray()) {
                if (servlet.getServletClass() == null || servlet.getServletClass().getStringValue().isEmpty()) {
                    continue;
                }
                String servletClassName = servlet.getServletClass().getStringValue();
                Class<?> cls = bundle.loadClass(servletClassName);
                if (!Servlet.class.isAssignableFrom(cls)) {
                    continue;
                }
                ServletSecurity servletSecurity = cls.getAnnotation(ServletSecurity.class);
                if (servletSecurity == null) {
                    continue;
                }
                Set<String> urlPatterns = servletClassNameUrlPatternsMap.get(servletClassName);
                if (urlPatterns == null || urlPatterns.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No url pattern for the servlet class " + servletClassName + " is found in the deployment plan, SecurityConstraint annotation is ignored");
                    }
                    continue;
                }
                HttpConstraint httpConstraint = servletSecurity.value();
                if (servletSecurity.httpMethodConstraints().length > 0) {
                    String[] omissionMethods = new String[servletSecurity.httpMethodConstraints().length];
                    int iIndex = 0;
                    for (HttpMethodConstraint httpMethodConstraint : servletSecurity.httpMethodConstraints()) {
                        //Generate a security-constraint for each HttpMethodConstraint
                        String httpMethod = normalizeHTTPMethod(servletClassName, httpMethodConstraint.value());
                        omissionMethods[iIndex++] = httpMethod;
                        addNewHTTPMethodSecurityConstraint(webApp, servletClassName, httpMethodConstraint.rolesAllowed(), httpMethodConstraint.transportGuarantee(), httpMethodConstraint
                                .emptyRoleSemantic(), httpMethod, urlPatterns);
                    }
                } else {
                    addNewHTTPSecurityConstraint(webApp, servletClassName, httpConstraint.rolesAllowed(), httpConstraint.transportGuarantee(), httpConstraint.value(), new String[] {}, urlPatterns);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Fail to load servlet class", e);
        }
    }

    /**
     * Create Security Constraint based on the arguments
     * @param webApp
     * @param rolesAllowed
     * @param transportGuarantee
     * @param emptyRoleSemantic
     * @return null when emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE
     */
    private SecurityConstraintType addNewSecurityConstraint(WebAppType webApp, String className, String[] rolesAllowed, TransportGuarantee transportGuarantee,
            ServletSecurity.EmptyRoleSemantic emptyRoleSemantic) throws DeploymentException {
        if (rolesAllowed.length > 0 && emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
            throw new DeploymentException("EmptyRoleSemantic with value DENY is not allowed in combination with a non-empty rolesAllowed list in the class " + className);
        }
        //If emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE then
        //No Constraint
        if (rolesAllowed.length > 0 || transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL) || emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
            SecurityConstraintType securityConstraint = webApp.addNewSecurityConstraint();
            if (transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL)) {
                securityConstraint.addNewUserDataConstraint().addNewTransportGuarantee().setStringValue(TransportGuarantee.CONFIDENTIAL.name());
            }
            if (emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                securityConstraint.addNewAuthConstraint();
            } else {
                AuthConstraintType authConstraint = securityConstraint.addNewAuthConstraint();
                for (String roleAllowed : rolesAllowed) {
                    authConstraint.addNewRoleName().setStringValue(roleAllowed);
                }
            }
            return securityConstraint;
        }
        return null;
    }

    private SecurityConstraintType addNewHTTPSecurityConstraint(WebAppType webApp, String className, String[] rolesAllowed, TransportGuarantee transportGuarantee,
            ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, String[] omissionMethods, Set<String> urlPatterns) throws DeploymentException {
        SecurityConstraintType securityConstraint = addNewSecurityConstraint(webApp, className, rolesAllowed, transportGuarantee, emptyRoleSemantic);
        if (omissionMethods.length > 0 || securityConstraint != null) {
            if (securityConstraint == null) {
                securityConstraint = webApp.addNewSecurityConstraint();
            }
            WebResourceCollectionType webResourceCollection = securityConstraint.getWebResourceCollectionArray().length == 0 ? securityConstraint.addNewWebResourceCollection() : securityConstraint
                    .getWebResourceCollectionArray(0);
            for (String omissionMethod : omissionMethods) {
                webResourceCollection.addNewHttpMethodOmission().setStringValue(omissionMethod);
            }
            for (String urlPattern : urlPatterns) {
                webResourceCollection.addNewUrlPattern().setStringValue(urlPattern);
            }
        }
        return securityConstraint;
    }

    private SecurityConstraintType addNewHTTPMethodSecurityConstraint(WebAppType webApp, String className, String[] rolesAllowed, TransportGuarantee transportGuarantee,
            ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, String httpMethod, Set<String> urlPatterns) throws DeploymentException {
        SecurityConstraintType securityConstraint = addNewSecurityConstraint(webApp, className, rolesAllowed, transportGuarantee, emptyRoleSemantic);
        if (securityConstraint == null) {
            securityConstraint = webApp.addNewSecurityConstraint();
        }
        WebResourceCollectionType webResourceCollection = securityConstraint.getWebResourceCollectionArray().length == 0 ? securityConstraint.addNewWebResourceCollection() : securityConstraint
                .getWebResourceCollectionArray(0);
        for (String urlPattern : urlPatterns) {
            webResourceCollection.addNewUrlPattern().setStringValue(urlPattern);
        }
        webResourceCollection.addNewHttpMethod().setStringValue(httpMethod);
        return securityConstraint;
    }

    private String normalizeHTTPMethod(String servletClassName, String httpMethod) throws DeploymentException {
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new DeploymentException("HTTP protocol method could not be null or empty string in the ServletSecurity anntation of the class " + servletClassName);
        }
        return httpMethod;
    }

    private Map<String, Set<String>> genetateServletClassUrlPatternsMap(WebAppType webApp, MergeContext mergeContext) {
        Set<String> urlPatternsConfiguredInSecurityConstraint = new HashSet<String>();
        for (SecurityConstraintType secuirtyConstrait : webApp.getSecurityConstraintArray()) {
            for (WebResourceCollectionType webResourceCollection : secuirtyConstrait.getWebResourceCollectionArray()) {
                for (UrlPatternType urlPattern : webResourceCollection.getUrlPatternArray()) {
                    urlPatternsConfiguredInSecurityConstraint.add(urlPattern.getStringValue());
                }
            }
        }
        Map<String, Set<String>> servletClassUrlPatternsMap = new HashMap<String, Set<String>>();
        for (ServletType servlet : webApp.getServletArray()) {
            if (servlet.getServletClass() == null || servlet.getServletClass().getStringValue().isEmpty()) {
                continue;
            }
            String servletClassName = servlet.getServletClass().getStringValue();
            Set<String> urlPatterns = servletClassUrlPatternsMap.get(servlet.getServletClass().getStringValue());
            if (urlPatterns == null) {
                urlPatterns = new HashSet<String>();
                servletClassUrlPatternsMap.put(servletClassName, urlPatterns);
            }
            ServletMappingType servletMapping = (ServletMappingType) mergeContext.getAttribute(ServletMappingMergeHandler.createServletMappingKey(servlet.getServletName().getStringValue()));
            if (servletMapping != null) {
                for (UrlPatternType urlPattern : servletMapping.getUrlPatternArray()) {
                    String urlPatternValue = urlPattern.getStringValue();
                    if (!urlPatternsConfiguredInSecurityConstraint.contains(urlPatternValue)) {
                        urlPatterns.add(urlPatternValue);
                    }
                }
            }
        }
        return servletClassUrlPatternsMap;
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
    }
}
