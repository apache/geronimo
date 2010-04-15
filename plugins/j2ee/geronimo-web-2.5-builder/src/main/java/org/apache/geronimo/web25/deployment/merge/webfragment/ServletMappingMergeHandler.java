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
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class ServletMappingMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    private List<SubMergeHandler<ServletMappingType, ServletMappingType>> subMergeHandlers;

    public ServletMappingMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<ServletMappingType, ServletMappingType>>();
        subMergeHandlers.add(new ServletMappingUrlPatternMergeHandler());
    }

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServletMappingType srcServletMapping : webFragment.getServletMappingArray()) {
            String servletName = srcServletMapping.getServletName().getStringValue();
            ServletMappingType targetServletMapping = (ServletMappingType) mergeContext.getAttribute(createServletMappingKey(servletName));
            if (targetServletMapping == null) {
                targetServletMapping = (ServletMappingType) webApp.addNewServletMapping().set(srcServletMapping);
                mergeContext.setAttribute(createServletMappingKey(servletName), targetServletMapping);
                for (SubMergeHandler<ServletMappingType, ServletMappingType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(targetServletMapping, mergeContext);
                }
            } else {
                if (isServletMappingFromAnnotation(servletName, mergeContext) && srcServletMapping.getUrlPatternArray().length > 0) {
                    //If the current url-patterns configurations are from annotations, so let's drop them
                    targetServletMapping.setUrlPatternArray(new UrlPatternType[0]);
                    mergeContext.removeAttribute(createServletMappingSourceKey(servletName));
                }
                for (SubMergeHandler<ServletMappingType, ServletMappingType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcServletMapping, targetServletMapping, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (SubMergeHandler<ServletMappingType, ServletMappingType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, context);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (ServletMappingType servletMapping : webApp.getServletMappingArray()) {
            String filterName = servletMapping.getServletName().getStringValue();
            context.setAttribute(createServletMappingKey(filterName), servletMapping);
        }
        for (SubMergeHandler<ServletMappingType, ServletMappingType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, context);
        }
    }

    public static String createServletMappingKey(String servletName) {
        return "servlet-mapping.servlet-name" + servletName;
    }

    public static String createServletMappingSourceKey(String servletName) {
        return "servlet-mapping.servlet-name." + servletName + ".sources";
    }

    public static void addServletMapping(ServletMappingType servletMapping, MergeContext mergeContext) {
        mergeContext.setAttribute(createServletMappingKey(servletMapping.getServletName().getStringValue()), servletMapping);
    }

    public static boolean isServletMappingConfigured(String servletName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServletMappingKey(servletName));
    }

    public static boolean isServletMappingFromAnnotation(String servletName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createServletMappingSourceKey(servletName));
        return elementSource != null && elementSource.equals(ElementSource.ANNOTATION);
    }
}
