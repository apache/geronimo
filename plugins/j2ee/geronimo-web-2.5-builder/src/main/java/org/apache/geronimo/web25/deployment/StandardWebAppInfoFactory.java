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

import java.util.ArrayList;

import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev:$ $Date:$
 */
public class StandardWebAppInfoFactory extends DefaultWebAppInfoFactory {

    private final ServletInfo jspServletInfo;
    private final WebAppInfo defaultWebAppInfo;

    public StandardWebAppInfoFactory(WebAppInfo defaultWebAppInfo, ServletInfo jspServletInfo) {
        this.defaultWebAppInfo = defaultWebAppInfo;
        this.jspServletInfo = jspServletInfo;
    }

    @Override
    public void complete(WebAppInfo webAppInfo) {
        for (ServletInfo servletInfo : defaultWebAppInfo.servlets) {
            webAppInfo.servlets.add(copy(servletInfo));
        }
        for (FilterInfo filterInfo : defaultWebAppInfo.filters) {
            webAppInfo.filters.add(copy(filterInfo));
        }
        webAppInfo.listeners.addAll(defaultWebAppInfo.listeners);
        webAppInfo.contextParams.putAll(defaultWebAppInfo.contextParams);
        webAppInfo.contextRoot = defaultWebAppInfo.contextRoot;
    }

    @Override
    public ServletInfo newJspInfo(String jspFile) {
        ServletInfo servletInfo = copy(jspServletInfo);
        servletInfo.initParams.put("jspFile", jspFile);
        return servletInfo;
    }

}
