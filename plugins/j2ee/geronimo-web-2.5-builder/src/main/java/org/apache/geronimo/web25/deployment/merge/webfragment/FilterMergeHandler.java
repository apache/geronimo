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
import org.apache.openejb.jee.Filter;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class FilterMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    private List<SubMergeHandler<Filter, Filter>> subMergeHandlers;

    public FilterMergeHandler() {
        subMergeHandlers = new ArrayList<SubMergeHandler<Filter, Filter>>(1);
        subMergeHandlers.add(new FilterInitParamMergeHandler());
    }

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Filter srcFilter : webFragment.getFilter()) {
            String filterName = srcFilter.getFilterName();
            Filter targetFilter = (Filter) mergeContext.getAttribute(createFilterKey(filterName));
            if (targetFilter == null) {
                webApp.getFilter().add(srcFilter);
                mergeContext.setAttribute(createFilterKey(filterName), srcFilter);
                for (SubMergeHandler<Filter, Filter> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.add(srcFilter, mergeContext);
                }
            } else {
                for (SubMergeHandler<Filter, Filter> subMergeHandler : subMergeHandlers) {
                    subMergeHandler.merge(srcFilter, targetFilter, mergeContext);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (SubMergeHandler<Filter, Filter> subMergeHandler : subMergeHandlers) {
            subMergeHandler.postProcessWebXmlElement(webApp, mergeContext);
        }
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Filter filter : webApp.getFilter()) {
            mergeContext.setAttribute(createFilterKey(filter.getFilterName()), filter);
        }
        for (SubMergeHandler<Filter, Filter> subMergeHandler : subMergeHandlers) {
            subMergeHandler.preProcessWebXmlElement(webApp, mergeContext);
        }
    }

    public static String createFilterKey(String filterName) {
        return "filter.filter-name." + filterName;
    }

    public static boolean isFilterConfigured(String filterName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterKey(filterName));
    }

    public static Filter getFilter(String filterName, MergeContext mergeContext) {
        return (Filter) mergeContext.getAttribute(createFilterKey(filterName));
    }

    public static void addFilter(Filter filter, MergeContext mergeContext) {
        mergeContext.setAttribute(createFilterKey(filter.getFilterName()), filter);
    }
}
