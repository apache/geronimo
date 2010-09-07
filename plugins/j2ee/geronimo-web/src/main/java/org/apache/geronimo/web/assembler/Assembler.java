/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.web.assembler;

import java.util.Map;

import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.FilterMappingInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev:$ $Date:$
 */
public class Assembler {

    public void assemble(ServletContext servletContext, WebAppInfo webAppInfo) {
        for (Map.Entry<String, String> entry: webAppInfo.contextParams.entrySet()) {
            servletContext.setInitParameter(entry.getKey(), entry.getValue());
        }
        for (ServletInfo servletInfo: webAppInfo.servlets) {
            addServlet(servletContext, servletInfo);
        }
        for (FilterInfo filterInfo: webAppInfo.filters) {
            addFilter(servletContext, filterInfo);
        }
        for (String listener: webAppInfo.listeners) {
            addListener(servletContext, listener);
        }
//        servletContext.declareRoles();
//        servletContext.setSessionTrackingModes();
    }

    private void addListener(ServletContext servletContext, String listener) {
        servletContext.addListener(listener);
    }

    private FilterRegistration.Dynamic addFilter(ServletContext servletContext, FilterInfo filterInfo) {
        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(filterInfo.filterName, filterInfo.filterClass);
        filterRegistration.setAsyncSupported(filterInfo.asyncSupported);
        filterRegistration.setInitParameters(filterInfo.initParams);
        for (FilterMappingInfo servletMapping: filterInfo.servletMappings) {
            filterRegistration.addMappingForServletNames(servletMapping.dispatchers, true, servletMapping.mapping.toArray(new String[servletMapping.mapping.size()]));
        }
        for (FilterMappingInfo urlMapping: filterInfo.urlMappings) {
            filterRegistration.addMappingForUrlPatterns(urlMapping.dispatchers, true, urlMapping.mapping.toArray(new String[urlMapping.mapping.size()]));
        }
        return filterRegistration;
    }

    protected ServletRegistration.Dynamic addServlet(ServletContext servletContext, ServletInfo servletInfo) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(servletInfo.servletName, servletInfo.servletClass);
        servletRegistration.setInitParameters(servletInfo.initParams);
        servletRegistration.setAsyncSupported(servletInfo.asyncSupported);
        if (servletInfo.loadOnStartup != null) {
            servletRegistration.setLoadOnStartup(servletInfo.loadOnStartup);
        }
        if (servletInfo.multipartConfigInfo != null) {
            servletRegistration.setMultipartConfig(new MultipartConfigElement(servletInfo.multipartConfigInfo.location,
                    servletInfo.multipartConfigInfo.maxFileSize,
                    servletInfo.multipartConfigInfo.maxRequestSize,
                    servletInfo.multipartConfigInfo.fileSizeThreshold));
        }
        servletRegistration.setRunAsRole(servletInfo.runAsRole);
//        servletRegistration.setServletSecurity();
        servletRegistration.addMapping(servletInfo.servletMappings.toArray(new String[servletInfo.servletMappings.size()]));
        return servletRegistration;
    }
}
