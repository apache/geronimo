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


package org.apache.geronimo.web25.deployment;

import java.util.List;
import java.util.Map;

import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev:$ $Date:$
 */
public class StandardWebAppInfoFactory extends DefaultWebAppInfoFactory {

    private ServletInfo jspServletInfo;

    private final WebAppInfo defaultWebAppInfo;

    public StandardWebAppInfoFactory(WebAppInfo defaultWebAppInfo, ServletInfo jspServletInfo) {
        this.defaultWebAppInfo = defaultWebAppInfo;
        this.jspServletInfo = jspServletInfo;
    }

    @Override
    public void complete(WebAppInfo webAppInfo) {
        for (ServletInfo servletInfo : defaultWebAppInfo.servlets) {
            if (noServlet(servletInfo.servletName, webAppInfo.servlets)) {
                webAppInfo.servlets.add(copy(servletInfo));
            }
        }
        for (FilterInfo filterInfo : defaultWebAppInfo.filters) {
            if (noFilter(filterInfo.filterName, webAppInfo.filters)) {
                webAppInfo.filters.add(copy(filterInfo));
            }
        }
        webAppInfo.listeners.addAll(defaultWebAppInfo.listeners);

        for (Map.Entry<String, String> entry: defaultWebAppInfo.contextParams.entrySet()) {
            if (!webAppInfo.contextParams.containsKey(entry.getKey())) {
                webAppInfo.contextParams.put(entry.getKey(), entry.getValue());
            }

        }
        for (SecurityConstraintInfo securityConstraintInfo : defaultWebAppInfo.securityConstraints) {
            webAppInfo.securityConstraints.add(copy(securityConstraintInfo));
        }
        webAppInfo.securityRoles.addAll(defaultWebAppInfo.securityRoles);
        if (webAppInfo.welcomeFiles.isEmpty()) {
            webAppInfo.welcomeFiles.addAll(defaultWebAppInfo.welcomeFiles);
        }
        webAppInfo.errorPages.addAll(defaultWebAppInfo.errorPages);
        for (Map.Entry<String, String> entry: defaultWebAppInfo.mimeMappings.entrySet()) {
            if (!webAppInfo.mimeMappings.containsKey(entry.getKey())) {
                webAppInfo.mimeMappings.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean noServlet(String servletName, List<ServletInfo> servlets) {
        for (ServletInfo servletInfo: servlets) {
            if (servletName.equals(servletInfo.servletName)) {
                return false;
            }
        }
        return true;
    }
    private boolean noFilter(String filterName, List<FilterInfo> filters) {
        for (FilterInfo filterInfo: filters) {
            if (filterName.equals(filterInfo.filterName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ServletInfo newJspInfo(String jspFile) {
        ServletInfo servletInfo = copy(jspServletInfo);
        servletInfo.initParams.put("jspFile", jspFile);
        return servletInfo;
    }

    public void setJspServletInfo(ServletInfo jspServletInfo) {
        this.jspServletInfo = jspServletInfo;
    }
}
