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
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.xbeans.javaee6.FilterType;
import org.apache.geronimo.xbeans.javaee6.ParamValueType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * @version $Rev$ $Date$
 */
public class FilterInitParamMergeHandler implements SubMergeHandler<FilterType, FilterType> {

    @Override
    public void add(FilterType servlet, MergeContext mergeContext) throws DeploymentException {
        String servletName = servlet.getFilterName().getStringValue();
        for (ParamValueType paramValue : servlet.getInitParamArray()) {
            addFilterInitParam(servletName, paramValue, ElementSource.WEB_FRAGMENT, mergeContext.getCurrentJarUrl(), mergeContext);
        }
    }

    @Override
    public void merge(FilterType srcFilter, FilterType targetFilter, MergeContext mergeContext) throws DeploymentException {
        String filterName = srcFilter.getFilterName().getStringValue();
        for (ParamValueType paramValue : srcFilter.getInitParamArray()) {
            MergeItem existedMergeItem = (MergeItem) mergeContext.getAttribute(createFilterInitParamKey(filterName, paramValue.getParamName().getStringValue()));
            if (existedMergeItem == null) {
                targetFilter.addNewInitParam().set(paramValue);
                addFilterInitParam(filterName, paramValue, ElementSource.WEB_FRAGMENT, mergeContext.getCurrentJarUrl(), mergeContext);
            } else {
                ParamValueType existedParamValue = (ParamValueType) existedMergeItem.getValue();
                switch (existedMergeItem.getSourceType()) {
                case WEB_XML:
                    continue;
                case WEB_FRAGMENT:
                    if (!existedParamValue.getParamValue().getStringValue().equals(paramValue.getParamValue().getStringValue())) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("filter " + filterName, "param-name", paramValue.getParamName().getStringValue(), "param-value",
                                existedParamValue.getParamValue().getStringValue(), existedMergeItem.getBelongedURL(), paramValue.getParamValue().getStringValue(), mergeContext.getCurrentJarUrl()));
                    }
                    break;
                case ANNOTATION:
                    //Spec 8.1.n.iii Init params for servlets and filters defined via annotations, will be
                    //overridden in the descriptor if the name of the init param exactly matches
                    //the name specified via the annotation. Init params are additive between the
                    //annotations and descriptors.
                    //In my understanding, the value of init-param should be overridden even if it is merged from annotation before the current web-fragment.xml file
                    existedParamValue.getParamValue().set(paramValue.getParamValue());
                    existedMergeItem.setBelongedURL(mergeContext.getCurrentJarUrl());
                    existedMergeItem.setSourceType(ElementSource.WEB_FRAGMENT);
                }
            }
        }

    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (FilterType filter : webApp.getFilterArray()) {
            String filterName = filter.getFilterName().getStringValue();
            for (ParamValueType paramValue : filter.getInitParamArray()) {
                addFilterInitParam(filterName, paramValue, ElementSource.WEB_XML, null, context);
            }
        }
    }

    public static String createFilterInitParamKey(String filterName, String paramName) {
        return "filter." + filterName + ".init-param.param-name." + paramName;
    }

    public static boolean isFilterInitParamConfigured(String filterName, String paramName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createFilterInitParamKey(filterName, paramName));
    }

    public static void addFilterInitParam(String filterName, ParamValueType paramValue, ElementSource source, String relativeUrl, MergeContext mergeContext) {
        mergeContext.setAttribute(createFilterInitParamKey(filterName, paramValue.getParamName().getStringValue()), new MergeItem(paramValue, relativeUrl, source));
    }
}
