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

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletInitParamMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletMappingMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.ServletMergeHandler;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.Text;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class WebServletAnnotationMergeHandler implements AnnotationMergeHandler {

    @Override
    public void merge(Class<?>[] classes, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Class<?> cls : classes) {
            if (!HttpServlet.class.isAssignableFrom(cls)) {
                throw new DeploymentException("The class " + cls.getName() + " with WebServlet annotation must extend javax.servlet.HttpServlet");
            }
            WebServlet webServlet = cls.getAnnotation(WebServlet.class);
            boolean valueAttributeConfigured = webServlet.value().length > 0;
            boolean urlPatternsAttributeConfigured = webServlet.urlPatterns().length > 0;
            if (valueAttributeConfigured && urlPatternsAttributeConfigured) {
                throw new DeploymentException("value and urlPatterns must not be configured on the same WebServlet annotation in the class " + cls.getName());
            }
            if (!valueAttributeConfigured && !urlPatternsAttributeConfigured) {
                throw new DeploymentException("At least one of value and urlPatterns attribute should be configured on the WebServlet annotation in the class " + cls.getName());
            }
            String servletName = webServlet.name().length() == 0 ? cls.getName() : webServlet.name();
            String[] urlPatterns = valueAttributeConfigured ? webServlet.value() : webServlet.urlPatterns();
            if (ServletMergeHandler.isServletConfigured(servletName, mergeContext)) {
                Servlet targetServlet = ServletMergeHandler.getServlet(servletName, mergeContext);
                //merge init-params, we only merge those init-param that are not explicitly configured in the web.xml or web-fragment.xml
                for (WebInitParam webInitParam : webServlet.initParams()) {
                    String paramName = webInitParam.name();
                    if (ServletInitParamMergeHandler.isServletInitParamConfigured(servletName, paramName, mergeContext)) {
                        continue;
                    }
                    ParamValue newParamValue = WebFilterAnnotationMergeHandler.newParamValue(webInitParam);
                    targetServlet.getInitParam().add(newParamValue);
                    ServletInitParamMergeHandler.addServletInitParam(servletName, newParamValue, ElementSource.ANNOTATION, mergeContext.getCurrentJarUrl(), mergeContext);
                }
            } else {
                //Add a new Servlet
                //create servlet element
                Servlet newServlet = new Servlet();
                if (!webServlet.displayName().isEmpty()) {
                    newServlet.addDisplayName(new Text(null, webServlet.displayName()));
                }
                newServlet.setServletClass(cls.getName());
                newServlet.setServletName(servletName);
                newServlet.setAsyncSupported(webServlet.asyncSupported());
                if (!webServlet.description().isEmpty()) {
                    newServlet.addDescription(new Text(null, webServlet.description()));
                }
                if (webServlet.loadOnStartup() != -1) {
                    newServlet.setLoadOnStartup(webServlet.loadOnStartup());
                }
                for (WebInitParam webInitParam : webServlet.initParams()) {
                    newServlet.getInitParam().add(WebFilterAnnotationMergeHandler.newParamValue(webInitParam));
                }
                if (!webServlet.smallIcon().isEmpty() || !webServlet.largeIcon().isEmpty()) {
                    Icon icon = new Icon();
                    if (!webServlet.smallIcon().isEmpty()) {
                        icon.setSmallIcon(webServlet.smallIcon());
                    }
                    if (!webServlet.largeIcon().isEmpty()) {
                        icon.setLargeIcon(webServlet.largeIcon());
                    }
                    newServlet.getIconMap().put(null, icon);
                }
                MultipartConfig multipartConfig = cls.getAnnotation(MultipartConfig.class);
                if (multipartConfig != null) {
                    org.apache.openejb.jee.MultipartConfig mpc = new org.apache.openejb.jee.MultipartConfig();
                    mpc.setFileSizeThreshold(multipartConfig.fileSizeThreshold());
                    mpc.setLocation(multipartConfig.location());
                    mpc.setMaxFileSize(multipartConfig.maxFileSize());
                    mpc.setMaxRequestSize(multipartConfig.maxRequestSize());
                    newServlet.setMultipartConfig(mpc);
                }
                webApp.getServlet().add(newServlet);
                ServletMergeHandler.addServlet(newServlet, mergeContext);
            }
            if (!ServletMappingMergeHandler.isServletMappingConfigured(servletName, mergeContext)) {
                //merge url-patterns, spec 8.1.n.vi. url-patterns, when specified in a descriptor for a given servlet name overrides the url patterns specified via the annotation.
                //FIXME To my understanding of the spec, once there are url-patterns configured in the descriptors, those configurations from the annotations are ignored
                ServletMapping newServletMapping = new ServletMapping();
                //create servlet-mapping element
                newServletMapping.setServletName(servletName);
                for (String urlPattern : urlPatterns) {
                    newServletMapping.getUrlPattern().add(urlPattern);
                }
                webApp.getServletMapping().add(newServletMapping);
                ServletMappingMergeHandler.addServletMapping(newServletMapping, mergeContext);
                //Set this tag, so that if any following web-fragment.xml has defined the url-patterns explicitly, it could drop the configurations from annotation
                mergeContext.setAttribute(ServletMappingMergeHandler.createServletMappingSourceKey(servletName), ElementSource.ANNOTATION);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        //TODO double check whether there are annotations are missed due to they are from excluded jars
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
    }
}
