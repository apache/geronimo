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
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingUrlPatternMergeHandler implements SubMergeHandler<FilterMappingType, FilterMappingType> {

    @Override
    public void add(FilterMappingType filterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = filterMapping.getFilterName().getStringValue();
        for (UrlPatternType urlPattern : filterMapping.getUrlPatternArray()) {
            String urlPatternStr = urlPattern.getStringValue();
            if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPatternStr, "web-fragment.xml located in "
                        + mergeContext.getCurrentJarUrl()));
            }
            mergeContext.setAttribute(createFilterMappingUrlPatternKey(filterName, urlPatternStr), urlPattern);
        }
    }

    @Override
    public void merge(FilterMappingType srcFilterMapping, FilterMappingType targetFilterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilterMapping.getFilterName().getStringValue();
        for (UrlPatternType urlPattern : srcFilterMapping.getUrlPatternArray()) {
            String urlPatternStr = urlPattern.getStringValue();
            String filterMappingUrlPatternKey = createFilterMappingUrlPatternKey(filterName, urlPatternStr);
            if (!mergeContext.containsAttribute(filterMappingUrlPatternKey)) {
                UrlPatternType newUrlPattern = (UrlPatternType) targetFilterMapping.addNewUrlPattern().set(urlPattern);
                if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPatternStr, "web-fragment.xml located in "
                            + mergeContext.getCurrentJarUrl()));
                }
                mergeContext.setAttribute(filterMappingUrlPatternKey, newUrlPattern);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (FilterMappingType filterMapping : webApp.getFilterMappingArray()) {
            String filterName = filterMapping.getFilterName().getStringValue();
            for (UrlPatternType urlPattern : filterMapping.getUrlPatternArray()) {
                String urlPatternStr = urlPattern.getStringValue();
                if (!WebDeploymentValidationUtils.isUrlPatternValid(urlPatternStr)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPatternStr, "web.xml"));
                }
                context.setAttribute(createFilterMappingUrlPatternKey(filterName, urlPatternStr), urlPattern);
            }
        }
    }

    public static String createFilterMappingUrlPatternKey(String filterName, String urlPattern) {
        return "filter-mapping.filter-name." + filterName + ".url-pattern." + urlPattern;
    }
}
