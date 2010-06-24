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
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingUrlPatternMergeHandler implements SubMergeHandler<FilterMapping, FilterMapping> {

    @Override
    public void add(FilterMapping filterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = filterMapping.getFilterName();
        for (String urlPattern : filterMapping.getUrlPattern()) {
            if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPattern, "web-fragment.xml located in "
                        + mergeContext.getCurrentJarUrl()));
            }
            mergeContext.setAttribute(createFilterMappingUrlPatternKey(filterName, urlPattern), urlPattern);
        }
    }

    @Override
    public void merge(FilterMapping srcFilterMapping, FilterMapping targetFilterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilterMapping.getFilterName();
        for (String urlPattern : srcFilterMapping.getUrlPattern()) {
            String filterMappingUrlPatternKey = createFilterMappingUrlPatternKey(filterName, urlPattern);
            if (!mergeContext.containsAttribute(filterMappingUrlPatternKey)) {
                targetFilterMapping.getUrlPattern().add(urlPattern);
                if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPattern, "web-fragment.xml located in "
                            + mergeContext.getCurrentJarUrl()));
                }
                mergeContext.setAttribute(filterMappingUrlPatternKey, urlPattern);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (FilterMapping filterMapping : webApp.getFilterMapping()) {
            String filterName = filterMapping.getFilterName();
            for (String urlPattern : filterMapping.getUrlPattern()) {
                if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterName, urlPattern, "web.xml"));
                }
                context.setAttribute(createFilterMappingUrlPatternKey(filterName, urlPattern), urlPattern);
            }
        }
    }

    public static String createFilterMappingUrlPatternKey(String filterName, String urlPattern) {
        return "filter-mapping.filter-name." + filterName + ".url-pattern." + urlPattern;
    }
}
