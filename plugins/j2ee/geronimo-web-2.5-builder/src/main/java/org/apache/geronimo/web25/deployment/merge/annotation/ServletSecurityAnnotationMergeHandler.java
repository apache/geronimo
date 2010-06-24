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

import javax.servlet.Servlet;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentValidationUtils;
//import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.osgi.framework.Bundle;

/**
 *  For supporting dynamic security configurations for servlets, the detailed analysis for ServletConstraint are delayed to start time.
 *  This ServletSecurityAnnotationMergeHandler is mainly used for annotation configuration validation
 * @version $Rev$ $Date$
 */
public class ServletSecurityAnnotationMergeHandler implements AnnotationMergeHandler {

    @Override
    public void merge(Class<?>[] classes, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        try {
            Bundle bundle = mergeContext.getBundle();
            for (org.apache.openejb.jee.Servlet servlet : webApp.getServlet()) {
                if (servlet.getServletClass() == null || servlet.getServletClass().isEmpty()) {
                    continue;
                }
                String servletClassName = servlet.getServletClass();
                Class<?> cls = bundle.loadClass(servletClassName);
                if (!Servlet.class.isAssignableFrom(cls)) {
                    continue;
                }
                ServletSecurity servletSecurity = cls.getAnnotation(ServletSecurity.class);
                if (servletSecurity == null) {
                    continue;
                }
                if (servletSecurity.httpMethodConstraints().length > 0) {
                    for (HttpMethodConstraint httpMethodConstraint : servletSecurity.httpMethodConstraints()) {
                        String httpMethod = httpMethodConstraint.value();
                        if (httpMethod == null || httpMethod.trim().isEmpty()) {
                            throw new DeploymentException("HTTP protocol method could not be null or empty string in the ServletSecurity annotation of the class " + servletClassName);
                        }
                        httpMethod = httpMethod.trim();
                        if (!WebDeploymentValidationUtils.isValidHTTPMethod(httpMethod)) {
                            throw new DeploymentException("Invalid HTTP method value is found in the ServletSecurity annotation of the class " + servletClassName);
                        }
                    }
                } else {
                    HttpConstraint httpConstraint = servletSecurity.value();
                    if (httpConstraint.rolesAllowed().length > 0 && httpConstraint.value().equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                        throw new DeploymentException("EmptyRoleSemantic with value DENY is not allowed in combination with a non-empty rolesAllowed list in the class " + servletClassName);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Fail to load servlet class", e);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
    }
}
