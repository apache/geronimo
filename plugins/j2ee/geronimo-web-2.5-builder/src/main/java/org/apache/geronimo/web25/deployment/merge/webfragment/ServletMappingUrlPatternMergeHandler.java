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
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class ServletMappingUrlPatternMergeHandler implements SubMergeHandler<ServletMapping, ServletMapping> {

    @Override
    public void add(ServletMapping servletMapping, MergeContext mergeContext) throws DeploymentException {
        String servletName = servletMapping.getServletName();
        for (String urlPattern : servletMapping.getUrlPattern()) {
            if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPattern, "web-fragment.xml located in "
                        + mergeContext.getCurrentJarUrl()));
            }
            mergeContext.setAttribute(createServletMappingUrlPatternKey(servletName, urlPattern), urlPattern);
        }
    }

    @Override
    public void merge(ServletMapping srcServletMapping, ServletMapping targetServletMapping, MergeContext mergeContext) throws DeploymentException {
        String servletName = srcServletMapping.getServletName();
        for (String urlPattern : srcServletMapping.getUrlPattern()) {
            String servletMappingUrlPatternKey = createServletMappingUrlPatternKey(servletName, urlPattern);
            if (!mergeContext.containsAttribute(servletMappingUrlPatternKey)) {
                 targetServletMapping.getUrlPattern().add(urlPattern);
                if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPattern, "web-fragment.xml located in "
                            + mergeContext.getCurrentJarUrl()));
                }
                mergeContext.setAttribute(servletMappingUrlPatternKey, urlPattern);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (ServletMapping servletMapping : webApp.getServletMapping()) {
            String servletName = servletMapping.getServletName();
            for (String urlPattern : servletMapping.getUrlPattern()) {
                if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletName, urlPattern, "web.xml"));
                }
                context.setAttribute(createServletMappingUrlPatternKey(servletName, urlPattern), urlPattern);
            }
        }
    }

    public static String createServletMappingUrlPatternKey(String servletName, String urlPattern) {
        return "servlet-mapping.servlet-name." + servletName + ".url-pattern." + urlPattern;
    }
}
