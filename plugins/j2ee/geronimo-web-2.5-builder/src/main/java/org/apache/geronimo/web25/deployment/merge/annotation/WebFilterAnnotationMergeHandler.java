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
import org.apache.openejb.jee.Dispatcher;
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Text;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class WebFilterAnnotationMergeHandler implements AnnotationMergeHandler {

    @Override
    public void merge(Class<?>[] classes, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
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
                org.apache.openejb.jee.Filter targetFilter = FilterMergeHandler.getFilter(filterName, mergeContext);
                //merge init-param
                for (WebInitParam webInitParam : webFilter.initParams()) {
                    String paramName = webInitParam.name();
                    if (FilterInitParamMergeHandler.isFilterInitParamConfigured(filterName, paramName, mergeContext)) {
                        continue;
                    }
                    ParamValue newParamValue = newParamValue(webInitParam);
                    targetFilter.getInitParam().add(newParamValue);
                    FilterInitParamMergeHandler.addFilterInitParam(filterName, newParamValue, ElementSource.ANNOTATION, mergeContext.getCurrentJarUrl(), mergeContext);
                }
            } else {
                //Create filter element
                org.apache.openejb.jee.Filter newFilter = new org.apache.openejb.jee.Filter();
                webApp.getFilter().add(newFilter);
                newFilter.setFilterName(filterName);
                newFilter.setAsyncSupported(webFilter.asyncSupported());
                if (!webFilter.description().isEmpty()) {
                    newFilter.addDescription(new Text(null, webFilter.description()));
                }
                if (!webFilter.displayName().isEmpty()) {
                    newFilter.addDisplayName(new Text(null, webFilter.displayName()));
                }
                newFilter.setFilterClass(cls.getName());
                for (WebInitParam webInitParam : webFilter.initParams()) {
                    newFilter.getInitParam().add(newParamValue(webInitParam));
                }
                if (!webFilter.smallIcon().isEmpty() || !webFilter.largeIcon().isEmpty()) {
                    Icon icon = new Icon();
                    if (!webFilter.smallIcon().isEmpty()) {
                        icon.setSmallIcon(webFilter.smallIcon());
                    }
                    if (!webFilter.largeIcon().isEmpty()) {
                        icon.setLargeIcon(webFilter.largeIcon());
                    }
                    newFilter.getIconMap().put(null, icon);
                }
                FilterMergeHandler.addFilter(newFilter, mergeContext);
            }
            //filter-mapping configured in web.xml and web-fragment.xml will override the configurations from annotation
            if (!FilterMappingMergeHandler.isFilterMappingConfigured(filterName, mergeContext)) {
                //create filter-mapping element
                FilterMapping filterMapping = new FilterMapping();
                filterMapping.setFilterName(filterName);
                for (String servletName : webFilter.servletNames()) {
                    filterMapping.getServletName().add(servletName);
                }
                for (DispatcherType dispatcherType : webFilter.dispatcherTypes()) {
                    filterMapping.getDispatcher().add(Dispatcher.fromValue(dispatcherType.name()));
                }
                for (String urlPattern : urlPatterns) {
                    filterMapping.getUrlPattern().add(urlPattern);
                }
                webApp.getFilterMapping().add(filterMapping);
                FilterMappingMergeHandler.addFilterMapping(filterMapping, mergeContext);
                //Set this tag, so that if any following web-fragment.xml has defined the url-patterns explicitly, it could drop the configurations from annotation
                mergeContext.setAttribute(FilterMappingMergeHandler.createFilterMappingSourceKey(filterName), ElementSource.ANNOTATION);
            }
        }
    }

    public static ParamValue newParamValue(WebInitParam webInitParam) {
        ParamValue newParamValue = new ParamValue();
        newParamValue.addDescription(new Text(null,webInitParam.description()));
        newParamValue.setParamName(webInitParam.name());
        newParamValue.setParamValue(webInitParam.value());
        return newParamValue;
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
    }
}
