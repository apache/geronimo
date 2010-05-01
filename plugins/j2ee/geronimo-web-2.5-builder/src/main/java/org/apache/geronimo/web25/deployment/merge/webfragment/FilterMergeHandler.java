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
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.xbeans.javaee6.FilterType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class FilterMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    private List<SubMergeHandler<FilterType, FilterType>> subMergeHandlers;

    public FilterMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<FilterType, FilterType>>();
        subMergeHandlers.add(new FilterInitParamMergeHandler());
    }

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (FilterType srcFilter : webFragment.getFilterArray()) {
            String filterName = srcFilter.getFilterName().getStringValue();
            FilterType targetFilter = (FilterType) mergeContext.getAttribute(createFilterKey(filterName));
            if (targetFilter == null) {
                targetFilter = (FilterType) webApp.addNewFilter().set(srcFilter);
                mergeContext.setAttribute(createFilterKey(filterName), targetFilter);
                for (SubMergeHandler<FilterType, FilterType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(targetFilter, mergeContext);
                }
            } else {
                for (SubMergeHandler<FilterType, FilterType> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcFilter, targetFilter, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (SubMergeHandler<FilterType, FilterType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, mergeContext);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (FilterType filter : webApp.getFilterArray()) {
            mergeContext.setAttribute(createFilterKey(filter.getFilterName().getStringValue()), filter);
        }
        for (SubMergeHandler<FilterType, FilterType> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, mergeContext);
        }
    }

    public static String createFilterKey(String filterName) {
        return "filter.filter-name." + filterName;
    }

    public static boolean isFilterConfigured(String filterName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterKey(filterName));
    }

    public static FilterType getFilter(String filterName, MergeContext mergeContext) {
        return (FilterType) mergeContext.getAttribute(createFilterKey(filterName));
    }

    public static void addFilter(FilterType filter, MergeContext mergeContext) {
        mergeContext.setAttribute(createFilterKey(filter.getFilterName().getStringValue()), filter);
    }
}
