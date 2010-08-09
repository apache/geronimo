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
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    private List<SubMergeHandler<FilterMapping, FilterMapping>> subMergeHandlers;

    public FilterMappingMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<FilterMapping, FilterMapping>>(3);
        subMergeHandlers.add(new FilterMappingUrlPatternMergeHandler());
        subMergeHandlers.add(new FilterMappingServletNameMergeHandler());
        subMergeHandlers.add(new FilterMappingDispatcherMergeHandler());
    }

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (FilterMapping srcFilterMapping : webFragment.getFilterMapping()) {
            String filterName = srcFilterMapping.getFilterName();
            FilterMapping targetFilterMapping = (FilterMapping) mergeContext.getAttribute(createFilterMappingKey(filterName));
            if (targetFilterMapping == null) {
                webApp.getFilterMapping().add(srcFilterMapping);
                mergeContext.setAttribute(createFilterMappingKey(filterName), srcFilterMapping);
                for (SubMergeHandler<FilterMapping, FilterMapping> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(srcFilterMapping, mergeContext);
                }
            } else {
                if (isFilterMappingFromWebXml(filterName, mergeContext)) {
                    continue;
                }
                if (isFilterMappingFromAnnotation(filterName, mergeContext)) {
                    //If the current url-patterns configurations are from annotations, so let's drop them
                    targetFilterMapping.getUrlPattern().clear();
                    targetFilterMapping.getDispatcher().clear();
                    targetFilterMapping.getServletName().clear();
                    mergeContext.removeAttribute(createFilterMappingSourceKey(filterName));
                }
                for (SubMergeHandler<FilterMapping, FilterMapping> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcFilterMapping, targetFilterMapping, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (SubMergeHandler<FilterMapping, FilterMapping> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, context);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (FilterMapping filterMapping : webApp.getFilterMapping()) {
            String filterName = filterMapping.getFilterName();
            context.setAttribute(createFilterMappingKey(filterName), filterMapping);
            context.setAttribute(createFilterMappingSourceKey(filterName), ElementSource.WEB_XML);
        }
        for (SubMergeHandler<FilterMapping, FilterMapping> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, context);
        }
    }

    public static String createFilterMappingKey(String filterName) {
        return "filter-mapping.filter-name" + filterName;
    }

    public static boolean isFilterMappingConfigured(String filterName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterMappingKey(filterName));
    }

    public static FilterMapping getFilterMapping(String filterName, MergeContext mergeContext) {
        return (FilterMapping) mergeContext.getAttribute(createFilterMappingKey(filterName));
    }

    public static String createFilterMappingSourceKey(String filterName) {
        return "filter-mapping.filter-name." + filterName + ".sources";
    }

    public static boolean isFilterMappingFromAnnotation(String filterName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createFilterMappingSourceKey(filterName));
        return elementSource != null && elementSource.equals(ElementSource.ANNOTATION);
    }

    public static boolean isFilterMappingFromWebXml(String filterName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createFilterMappingSourceKey(filterName));
        return elementSource != null && elementSource.equals(ElementSource.WEB_XML);
    }

    public static void addFilterMapping(FilterMapping filterMapping, MergeContext mergeContext) {
        mergeContext.setAttribute(createFilterMappingKey(filterMapping.getFilterName()), filterMapping);
    }
}
