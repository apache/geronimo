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

import java.util.regex.Pattern;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebResourceCollectionType;

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

    public static void validateWebApp(WebAppType webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebAppType webApp) throws DeploymentException {
        FilterMappingType[] filterMappings = webApp.getFilterMappingArray();
        for (FilterMappingType filterMapping : filterMappings) {
            for (UrlPatternType urlPattern : filterMapping.getUrlPatternArray()) {
                if (!isValidUrlPattern(urlPattern.getStringValue().trim())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("filter-mapping", filterMapping.getFilterName().getStringValue(), urlPattern
                            .getStringValue(), "web.xml"));
                }
            }
        }
        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (ServletMappingType servletMapping : servletMappings) {
            for (UrlPatternType urlPattern : servletMapping.getUrlPatternArray()) {
                if (!isValidUrlPattern(urlPattern.getStringValue().trim())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("servlet-mapping", servletMapping.getServletName().getStringValue(), urlPattern
                            .getStringValue(), "web.xml"));
                }
            }
        }
        SecurityConstraintType[] constraints = webApp.getSecurityConstraintArray();
        for (SecurityConstraintType constraint : constraints) {
            WebResourceCollectionType[] collections = constraint.getWebResourceCollectionArray();
            for (WebResourceCollectionType collection : collections) {
                UrlPatternType[] patterns = collection.getUrlPatternArray();
                for (UrlPatternType pattern : patterns) {
                    if (!isValidUrlPattern(pattern.getStringValue().trim())) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", collection.getWebResourceName().getStringValue(), pattern
                                .getStringValue(), "web.xml"));
                    }
                }
            }
        }
    }

    private static void checkMultiplicities(WebAppType webApp) throws DeploymentException {
        if (webApp.getSessionConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("session-config"));
        }
        if (webApp.getJspConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("jsp-config"));
        }
        if (webApp.getLoginConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("login-config"));
        }
    }
}
