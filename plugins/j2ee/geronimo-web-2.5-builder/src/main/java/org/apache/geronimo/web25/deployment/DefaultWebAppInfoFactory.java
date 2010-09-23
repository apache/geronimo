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

import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web25.deployment.WebAppInfoFactory;

/**
 * @version $Rev:$ $Date:$
 */
public class DefaultWebAppInfoFactory implements WebAppInfoFactory {
    @Override
    public WebAppInfo newWebAppInfo() {
        return new WebAppInfo();
    }

    @Override
    public FilterInfo newFilterInfo() {
        return new FilterInfo();
    }

    @Override
    public ServletInfo newServletInfo() {
        return new ServletInfo();
    }

    @Override
    public void complete(WebAppInfo webAppInfo) {
    }

    @Override
    public ServletInfo newJspInfo(String jspFile) {
        throw new IllegalStateException("default does not support jsps");
    }

    @Override
    public SecurityConstraintInfo newSecurityConstraintInfo() {
        return new SecurityConstraintInfo();
    }

    @Override
    public SecurityConstraintInfo copy(SecurityConstraintInfo securityConstraintInfo) {
        SecurityConstraintInfo copy = new SecurityConstraintInfo();
        copy.authConstraint = securityConstraintInfo.authConstraint;
        copy.userDataConstraint = securityConstraintInfo.userDataConstraint;
        copy.webResourceCollections.addAll(securityConstraintInfo.webResourceCollections);
        return copy;
    }

    @Override
    public ServletInfo copy(ServletInfo servletInfo) {
         ServletInfo copy = new ServletInfo();
         copy.servletClass = servletInfo.servletClass;
         copy.servletMappings.addAll(servletInfo.servletMappings);
         copy.servletName = servletInfo.servletName;
         copy.asyncSupported = servletInfo.asyncSupported;
         copy.initParams.putAll(servletInfo.initParams);
         copy.loadOnStartup = servletInfo.loadOnStartup;
         copy.runAsRole = servletInfo.runAsRole;
         copy.securityRoleRefs.addAll(servletInfo.securityRoleRefs);
         return copy;
     }

     @Override
     public FilterInfo copy(FilterInfo filterInfo) {
         FilterInfo copy = new FilterInfo();
         copy.filterName = filterInfo.filterName;
         copy.filterClass = filterInfo.filterClass;
         copy.servletMappings.addAll(filterInfo.servletMappings);
         copy.urlMappings.addAll(filterInfo.urlMappings);
         copy.asyncSupported = filterInfo.asyncSupported;
         copy.initParams.putAll(filterInfo.initParams);
         return copy;
     }

}
