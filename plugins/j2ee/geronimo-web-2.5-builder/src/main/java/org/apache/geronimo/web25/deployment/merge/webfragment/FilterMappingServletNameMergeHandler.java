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
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingServletNameMergeHandler implements SubMergeHandler<FilterMapping, FilterMapping> {

    @Override
    public void add(FilterMapping filterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = filterMapping.getFilterName();
        for (String servletName : filterMapping.getServletName()) {
            mergeContext.setAttribute(createFilterMappingServletNameKey(filterName, servletName), Boolean.TRUE);
        }
    }

    @Override
    public void merge(FilterMapping srcFilterMapping, FilterMapping targetFilterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilterMapping.getFilterName();
        for (String servletName : srcFilterMapping.getServletName()) {
            String filterMappingServletNameKey = createFilterMappingServletNameKey(filterName, servletName);
            if (!mergeContext.containsAttribute(filterMappingServletNameKey)) {
                targetFilterMapping.getServletName().add(servletName);
                mergeContext.setAttribute(filterMappingServletNameKey, Boolean.TRUE);
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
            for (String servletName : filterMapping.getServletName()) {
                context.setAttribute(createFilterMappingServletNameKey(filterName, servletName), Boolean.TRUE);
            }
        }
    }

    public static String createFilterMappingServletNameKey(String filterName, String servletName) {
        return "filter-mapping.filter-name." + filterName + ".servlet-name." + servletName;
    }
}
