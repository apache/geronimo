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

package org.apache.geronimo.web25.deployment.merge.annotation;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.webfragment.FilterInitParamMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.FilterMappingMergeHandler;
import org.apache.geronimo.web25.deployment.merge.webfragment.FilterMergeHandler;
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.FilterType;
import org.apache.geronimo.xbeans.javaee6.IconType;
import org.apache.geronimo.xbeans.javaee6.ParamValueType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;

/**
 * @version $Rev$ $Date$
 */
public class WebFilterAnnotationMergeHandler implements AnnotationMergeHandler {

    @Override
    public void merge(Class<?>[] classes, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (Class<?> cls : classes) {
            if (!Filter.class.isAssignableFrom(cls)) {
                throw new DeploymentException("The class " + cls.getName() + " with WebFilter annotation must implement javax.servlet.Filter");
            }
            WebFilter webFilter = cls.getAnnotation(WebFilter.class);
            String filterName = webFilter.filterName().length() == 0 ? cls.getName() : webFilter.filterName();
            boolean valueAttributeConfigured = webFilter.value().length > 0;
            boolean urlPatternsAttributeConfigured = webFilter.urlPatterns().length > 0;
            if (valueAttributeConfigured && urlPatternsAttributeConfigured) {
                throw new DeploymentException("value and urlPatterns must not be configured on the same WebFilter annotation in the class " + cls.getName());
            }
            boolean servletNamesAttributeConfigured = webFilter.servletNames().length == 0;
            if (!valueAttributeConfigured && !urlPatternsAttributeConfigured && !servletNamesAttributeConfigured) {
                throw new DeploymentException("At least one of value, urlPatterns and servletNames attributes are configured on the WebFilter annotation in the class " + cls.getName());
            }
            String[] urlPatterns = valueAttributeConfigured ? webFilter.value() : webFilter.urlPatterns();
            if (FilterMergeHandler.isFilterConfigured(filterName, mergeContext)) {
                //merge the filter annotation configuration to current web.xml
                FilterType targetFilter = FilterMergeHandler.getFilter(filterName, mergeContext);
                //merge init-param
                for (WebInitParam webInitParam : webFilter.initParams()) {
                    String paramName = webInitParam.name();
                    if (FilterInitParamMergeHandler.isFilterInitParamConfigured(filterName, paramName, mergeContext)) {
                        continue;
                    }
                    ParamValueType newParamValue = targetFilter.addNewInitParam();
                    newParamValue.addNewDescription().setStringValue(webInitParam.description());
                    newParamValue.addNewParamName().setStringValue(webInitParam.name());
                    newParamValue.addNewParamValue().setStringValue(webInitParam.value());
                    FilterInitParamMergeHandler.addFilterInitParam(filterName, newParamValue, ElementSource.ANNOTATION, mergeContext.getCurrentJarUrl(), mergeContext);
                }
            } else {
                //Create filter element
                FilterType newFilter = webApp.addNewFilter();
                newFilter.addNewFilterName().setStringValue(filterName);
                newFilter.addNewAsyncSupported().setBooleanValue(webFilter.asyncSupported());
                if (!webFilter.description().isEmpty()) {
                    newFilter.addNewDescription().setStringValue(webFilter.description());
                }
                if (!webFilter.displayName().isEmpty()) {
                    newFilter.addNewDisplayName().setStringValue(webFilter.displayName());
                }
                newFilter.addNewFilterClass().setStringValue(cls.getName());
                for (WebInitParam webInitParam : webFilter.initParams()) {
                    ParamValueType paramValue = newFilter.addNewInitParam();
                    paramValue.addNewDescription().setStringValue(webInitParam.description());
                    paramValue.addNewParamName().setStringValue(webInitParam.name());
                    paramValue.addNewParamValue().setStringValue(webInitParam.value());
                }
                if (!webFilter.smallIcon().isEmpty() || !webFilter.largeIcon().isEmpty()) {
                    IconType iconType = newFilter.addNewIcon();
                    if (!webFilter.smallIcon().isEmpty()) {
                        iconType.addNewSmallIcon().setStringValue(webFilter.smallIcon());
                    }
                    if (!webFilter.largeIcon().isEmpty()) {
                        iconType.addNewLargeIcon().setStringValue(webFilter.largeIcon());
                    }
                }
                FilterMergeHandler.addFilter(newFilter, mergeContext);
            }
            //filter-mapping configured in web.xml and web-fragment.xml will override the configurations from annotation
            if (!FilterMappingMergeHandler.isFilterMappingConfigured(filterName, mergeContext)) {
                //create filter-mapping element
                FilterMappingType filterMapping = webApp.addNewFilterMapping();
                filterMapping.addNewFilterName().setStringValue(filterName);
                for (String servletName : webFilter.servletNames()) {
                    filterMapping.addNewServletName().setStringValue(servletName);
                }
                for (DispatcherType dispatcherType : webFilter.dispatcherTypes()) {
                    filterMapping.addNewDispatcher().setStringValue(dispatcherType.name());
                }
                for (String urlPattern : urlPatterns) {
                    filterMapping.addNewUrlPattern().setStringValue(urlPattern);
                }
                FilterMappingMergeHandler.addFilterMapping(filterMapping, mergeContext);
                //Set this tag, so that if any following web-fragment.xml has defined the url-patterns explicitly, it could drop the configurations from annotation
                mergeContext.setAttribute(FilterMappingMergeHandler.createFilterMappingSourceKey(filterName), ElementSource.ANNOTATION);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
    }
}
