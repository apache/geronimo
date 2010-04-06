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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentValidationUtils;
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * @version $Rev$ $Date$
 */
public class ServletMappingUrlPatternMergeHandler implements SubMergeHandler<ServletMappingType, ServletMappingType> {

    @Override
    public void add(ServletMappingType servletMapping, MergeContext mergeContext) throws DeploymentException {
        String servletName = servletMapping.getServletName().getStringValue();
        for (UrlPatternType urlPattern : servletMapping.getUrlPatternArray()) {
            String urlPatternStr = urlPattern.getStringValue();
            if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPatternStr, "web-fragment.xml located in "
                        + mergeContext.getCurrentJarUrl()));
            }
            mergeContext.setAttribute(createServletMappingUrlPatternKey(servletName, urlPattern.getStringValue()), urlPattern);
        }
    }

    @Override
    public void merge(ServletMappingType srcServletMapping, ServletMappingType targetServletMapping, MergeContext mergeContext) throws DeploymentException {
        String servletName = srcServletMapping.getServletName().getStringValue();
        for (UrlPatternType urlPattern : srcServletMapping.getUrlPatternArray()) {
            String urlPatternStr = urlPattern.getStringValue();
            String servletMappingUrlPatternKey = createServletMappingUrlPatternKey(servletName, urlPatternStr);
            if (!mergeContext.containsAttribute(servletMappingUrlPatternKey)) {
                UrlPatternType newUrlPattern = (UrlPatternType) targetServletMapping.addNewUrlPattern().set(urlPattern);
                if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPatternStr, "web-fragment.xml located in "
                            + mergeContext.getCurrentJarUrl()));
                }
                mergeContext.setAttribute(servletMappingUrlPatternKey, newUrlPattern);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (ServletMappingType servletMapping : webApp.getServletMappingArray()) {
            String servletName = servletMapping.getServletName().getStringValue();
            for (UrlPatternType urlPattern : servletMapping.getUrlPatternArray()) {
                String urlPatternStr = urlPattern.getStringValue();
                if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPatternStr, "web.xml"));
                }
                context.setAttribute(createServletMappingUrlPatternKey(servletName, urlPatternStr), urlPattern);
            }
        }
    }

    public static String createServletMappingUrlPatternKey(String servletName, String urlPattern) {
        return "servlet-mapping.servlet-name." + servletName + ".url-pattern." + urlPattern;
    }
}
