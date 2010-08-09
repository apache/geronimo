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

package org.apache.geronimo.web25.deployment.merge.webfragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class ServletMappingMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    private List<SubMergeHandler<ServletMapping, ServletMapping>> subMergeHandlers;

    public ServletMappingMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<ServletMapping, ServletMapping>>(1);
        subMergeHandlers.add(new ServletMappingUrlPatternMergeHandler());
    }

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServletMapping srcServletMapping : webFragment.getServletMapping()) {
            String servletName = srcServletMapping.getServletName();
            ServletMapping targetServletMapping = (ServletMapping) mergeContext.getAttribute(createServletMappingKey(servletName));
            if (targetServletMapping == null) {
                webApp.getServletMapping().add(srcServletMapping);
                mergeContext.setAttribute(createServletMappingKey(servletName), srcServletMapping);
                for (SubMergeHandler<ServletMapping, ServletMapping> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(srcServletMapping, mergeContext);
                }
            } else {
                //If the servlet-mapping is configured in the central web.xml file, all the configurations from the fragment xml and annotations are ignored
                if(isServletMappingFromWebXml(servletName, mergeContext)) {
                    continue;
                }
                if (isServletMappingFromAnnotation(servletName, mergeContext) && !srcServletMapping.getUrlPattern().isEmpty()) {
                    //If the current url-patterns configurations are from annotations, so let's drop them
                    targetServletMapping.getUrlPattern().clear();
                    mergeContext.removeAttribute(createServletMappingSourceKey(servletName));
                }
                for (SubMergeHandler<ServletMapping, ServletMapping> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcServletMapping, targetServletMapping, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (SubMergeHandler<ServletMapping, ServletMapping> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, context);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (ServletMapping servletMapping : webApp.getServletMapping()) {
            String servletName = servletMapping.getServletName();
            context.setAttribute(createServletMappingKey(servletName), servletMapping);
            context.setAttribute(createServletMappingSourceKey(servletName), ElementSource.WEB_XML);
        }
        for (SubMergeHandler<ServletMapping, ServletMapping> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, context);
        }
    }

    public static String createServletMappingKey(String servletName) {
        return "servlet-mapping.servlet-name" + servletName;
    }

    public static String createServletMappingSourceKey(String servletName) {
        return "servlet-mapping.servlet-name." + servletName + ".sources";
    }

    public static void addServletMapping(ServletMapping servletMapping, MergeContext mergeContext) {
        mergeContext.setAttribute(createServletMappingKey(servletMapping.getServletName()), servletMapping);
    }

    public static boolean isServletMappingConfigured(String servletName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletMappingKey(servletName));
    }

    public static boolean isServletMappingFromAnnotation(String servletName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createServletMappingSourceKey(servletName));
        return elementSource != null && elementSource.equals(ElementSource.ANNOTATION);
    }

    public static boolean isServletMappingFromWebXml(String servletName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createServletMappingSourceKey(servletName));
        return elementSource != null && elementSource.equals(ElementSource.WEB_XML);
    }
}
