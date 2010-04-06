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
import org.apache.geronimo.xbeans.javaee6.DispatcherType;
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * @version $Rev$ $Date$
 */
public class FilterMappingDispatcherMergeHandler implements SubMergeHandler<FilterMappingType, FilterMappingType> {

    @Override
    public void add(FilterMappingType filterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = filterMapping.getFilterName().getStringValue();
        for (DispatcherType dispatcher : filterMapping.getDispatcherArray()) {
            mergeContext.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher.getStringValue(), mergeContext), Boolean.TRUE);
        }
    }

    @Override
    public void merge(FilterMappingType srcFilterMapping, FilterMappingType targetFilterMapping, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilterMapping.getFilterName().getStringValue();
        for (DispatcherType dispatcher : srcFilterMapping.getDispatcherArray()) {
            if (isFilterMappingDispatcherConfigured(filterName, dispatcher.getStringValue(), mergeContext)) {
                continue;
            }
            mergeContext.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher.getStringValue(), mergeContext), Boolean.TRUE);
            targetFilterMapping.addNewDispatcher().setStringValue(dispatcher.getStringValue());
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (FilterMappingType filterMapping : webApp.getFilterMappingArray()) {
            String filterName = filterMapping.getFilterName().getStringValue();
            for (DispatcherType dispatcher : filterMapping.getDispatcherArray()) {
                context.setAttribute(createFilterMappingDispatcherKey(filterName, dispatcher.getStringValue(), context), Boolean.TRUE);
            }
        }
    }

    public static String createFilterMappingDispatcherKey(String filterName, String dispatcherValue, MergeContext mergeContext) {
        return "filter-mapping.filter-name." + filterName + ".dispatcher." + dispatcherValue;
    }

    public static boolean isFilterMappingDispatcherConfigured(String filterName, String dispatcherValue, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterMappingDispatcherKey(filterName, dispatcherValue, mergeContext));
    }
}
