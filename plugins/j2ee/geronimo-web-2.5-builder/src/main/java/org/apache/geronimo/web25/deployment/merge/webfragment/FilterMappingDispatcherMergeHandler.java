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
import org.apache.openejb.jee.Dispatcher;
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingDispatcherMergeHandler implements SubMergeHandler<FilterMapping, FilterMapping> {

    @Override
    public void add(FilterMapping filterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = filterMapping.getFilterName();
        for (Dispatcher dispatcher : filterMapping.getDispatcher()) {
            mergeContext.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher, mergeContext), Boolean.TRUE);
        }
    }

    @Override
    public void merge(FilterMapping srcFilterMapping, FilterMapping targetFilterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilterMapping.getFilterName();
        for (Dispatcher dispatcher : srcFilterMapping.getDispatcher()) {
            if (isFilterMappingDispatcherConfigured(filterName, dispatcher, mergeContext)) {
                continue;
            }
            mergeContext.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher, mergeContext), Boolean.TRUE);
            targetFilterMapping.getDispatcher().add(dispatcher);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (FilterMapping filterMapping : webApp.getFilterMapping()) {
            String filterName = filterMapping.getFilterName();
            for (Dispatcher dispatcher : filterMapping.getDispatcher()) {
                context.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher, context), Boolean.TRUE);
            }
        }
    }

    public static String createFilterMappingDispatcherKey(String filterName, Dispatcher dispatcherValue, MergeContext mergeContext) {
        return "filter-mapping.filter-name." + filterName + ".dispatcher." + dispatcherValue;
    }

    public static boolean isFilterMappingDispatcherConfigured(String filterName, Dispatcher dispatcherValue, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterMappingDispatcherKey(filterName, dispatcherValue, mergeContext));
    }
}
