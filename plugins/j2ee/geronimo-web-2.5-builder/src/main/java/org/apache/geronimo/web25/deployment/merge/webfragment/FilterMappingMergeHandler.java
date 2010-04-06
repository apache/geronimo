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
import org.apache.geronimo.xbeans.javaee6.DispatcherType;
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.ServletNameType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    private List<SubMergeHandler<FilterMappingType, FilterMappingType>> subMergeHandlers;

    public FilterMappingMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<FilterMappingType, FilterMappingType>>();
        subMergeHandlers.add(new FilterMappingUrlPatternMergeHandler());
        subMergeHandlers.add(new FilterMappingServletNameMergeHandler());
        subMergeHandlers.add(new FilterMappingDispatcherMergeHandler());
    }

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (FilterMappingType srcFilterMapping : webFragment.getFilterMappingArray()) {
            String filterName = srcFilterMapping.getFilterName().getStringValue();
            FilterMappingType targetFilterMapping = (FilterMappingType) mergeContext.getAttribute(createFilterMappingKey(filterName));
            if (targetFilterMapping == null) {
                targetFilterMapping = (FilterMappingType) webApp.addNewFilterMapping().set(srcFilterMapping);
                mergeContext.setAttribute(createFilterMappingKey(filterName), targetFilterMapping);
                for (SubMergeHandler<FilterMappingType, FilterMappingType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(targetFilterMapping, mergeContext);
                }
            } else {
                if (isFilterMappingFromAnnotation(filterName, mergeContext)) {
                    //If the current url-patterns configurations are from annotations, so let's drop them
                    targetFilterMapping.setUrlPatternArray(new UrlPatternType[0]);
                    targetFilterMapping.setDispatcherArray(new DispatcherType[0]);
                    targetFilterMapping.setServletNameArray(new ServletNameType[0]);
                    mergeContext.removeAttribute(createFilterMappingSourceKey(filterName));
                }
                for (SubMergeHandler<FilterMappingType, FilterMappingType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcFilterMapping, targetFilterMapping, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (SubMergeHandler<FilterMappingType, FilterMappingType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, context);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (FilterMappingType filterMapping : webApp.getFilterMappingArray()) {
            String filterName = filterMapping.getFilterName().getStringValue();
            context.setAttribute(createFilterMappingKey(filterName), filterMapping);
        }
        for (SubMergeHandler<FilterMappingType, FilterMappingType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, context);
        }
    }

    public static String createFilterMappingKey(String filterName) {
        return "filter-mapping.filter-name" + filterName;
    }

    public static boolean isFilterMappingConfigured(String filterName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterMappingKey(filterName));
    }

    public static FilterMappingType getFilterMappingType(String filterName, MergeContext mergeContext) {
        return (FilterMappingType) mergeContext.getAttribute(createFilterMappingKey(filterName));
    }

    public static String createFilterMappingSourceKey(String filterName) {
        return "filter-mapping.filter-name." + filterName + ".sources";
    }

    public static boolean isFilterMappingFromAnnotation(String filterName, MergeContext mergeContext) {
        ElementSource elementSource = (ElementSource) mergeContext.getAttribute(createFilterMappingSourceKey(filterName));
        return elementSource != null && elementSource.equals(ElementSource.ANNOTATION);
    }

    public static void addFilterMapping(FilterMappingType filterMapping, MergeContext mergeContext) {
        mergeContext.setAttribute(createFilterMappingKey(filterMapping.getFilterName().getStringValue()), filterMapping);
    }
}
