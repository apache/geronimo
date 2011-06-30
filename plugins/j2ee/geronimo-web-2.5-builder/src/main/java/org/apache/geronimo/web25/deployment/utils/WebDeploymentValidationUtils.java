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

package org.apache.geronimo.web25.deployment.utils;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.geronimo.common.DeploymentException;
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.JspPropertyGroup;
import org.apache.openejb.jee.SecurityConstraint;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebResourceCollection;

/**
 * @version $Rev$ $Date$
 */
public class WebDeploymentValidationUtils {

    private static final Pattern HTTP_METHOD_PATTERN = Pattern.compile("[!-~&&[^\\(\\)\\<\\>@,;:\\\\\"/\\[\\]\\?=\\{\\}]]*");

    public static boolean isValidUrlPattern(String urlPattern) {
        //j2ee_1_4.xsd explicitly requires preserving all whitespace. Do not trim.
        return urlPattern.indexOf(0x0D) < 0 && urlPattern.indexOf(0x0A) < 0;
    }

    public static boolean isValidHTTPMethod(String httpMethod) {
        return HTTP_METHOD_PATTERN.matcher(httpMethod).matches();
    }

    public static void validateWebApp(WebApp webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebApp webApp) throws DeploymentException {
        List<FilterMapping> filterMappings = webApp.getFilterMapping();
        for (FilterMapping filterMapping : filterMappings) {
            for (String urlPattern : filterMapping.getUrlPattern()) {
                if (!isValidUrlPattern(urlPattern.trim())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterMapping.getFilterName(), urlPattern
                            , "web.xml"));
                }
            }
        }
        List<ServletMapping> servletMappings = webApp.getServletMapping();
        for (ServletMapping servletMapping : servletMappings) {
            for (String urlPattern : servletMapping.getUrlPattern()) {
                if (!isValidUrlPattern(urlPattern.trim())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletMapping.getServletName(), urlPattern
                            , "web.xml"));
                }
            }
        }
        List<JspConfig> jspConfigs = webApp.getJspConfig();
        for (JspConfig jspConfig : jspConfigs) {
            for (JspPropertyGroup propertyGroup : jspConfig.getJspPropertyGroup()) {
                for (String urlPattern : propertyGroup.getUrlPattern()) {
                    if (!isValidUrlPattern(urlPattern.trim())) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("jsp-config", propertyGroup.getDisplayName(), urlPattern
                                , "web.xml"));
                    }
                }
            }
        }
        List<SecurityConstraint> constraints = webApp.getSecurityConstraint();
        for (SecurityConstraint constraint : constraints) {
            for (WebResourceCollection collection : constraint.getWebResourceCollection()) {
                for (String pattern : collection.getUrlPattern()) {
                    if (!isValidUrlPattern(pattern.trim())) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", collection.getWebResourceName(), pattern
                                , "web.xml"));
                    }
                }
            }
        }
    }

    private static void checkMultiplicities(WebApp webApp) throws DeploymentException {
        if (webApp.getSessionConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("session-config"));
        }
        if (webApp.getJspConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("jsp-config"));
        }
        if (webApp.getLoginConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("login-config"));
        }
    }
    
}
