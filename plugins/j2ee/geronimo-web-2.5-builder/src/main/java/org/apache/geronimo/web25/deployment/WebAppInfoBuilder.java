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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web.info.AuthConstraintInfo;
import org.apache.geronimo.web.info.ErrorPageInfo;
import org.apache.geronimo.web.info.FilterInfo;
import org.apache.geronimo.web.info.FilterMappingInfo;
import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.MultipartConfigInfo;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.SecurityRoleRefInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.info.WebResourceCollectionInfo;
import org.apache.openejb.jee.Dispatcher;
import org.apache.openejb.jee.ErrorPage;
import org.apache.openejb.jee.Filter;
import org.apache.openejb.jee.FilterMapping;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.LocaleEncodingMapping;
import org.apache.openejb.jee.LocaleEncodingMappingList;
import org.apache.openejb.jee.LoginConfig;
import org.apache.openejb.jee.MimeMapping;
import org.apache.openejb.jee.MultipartConfig;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.SecurityConstraint;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebResourceCollection;
import org.apache.openejb.jee.WelcomeFileList;

/**
 * @version $Rev:$ $Date:$
 */
public class WebAppInfoBuilder {

    private final WebApp webApp;

    private final WebAppInfoFactory webAppInfoFactory;

    private WebAppInfo webAppInfo;

    public WebAppInfoBuilder(WebApp webApp, WebAppInfoFactory webAppInfoFactory) {
        this.webApp = webApp;
        this.webAppInfoFactory = webAppInfoFactory;
    }

    public WebAppInfo build() throws DeploymentException {
        if (webAppInfo != null) {
            throw new IllegalStateException("already built");
        }
        List<String> problems = new ArrayList<String>();
        WebAppInfo webAppInfo = webAppInfoFactory.newWebAppInfo();
        addParams(webApp.getContextParam(), webAppInfo.contextParams);
        webAppInfo.contextRoot = webApp.getContextRoot();

        for (Listener listener : webApp.getListener()) {
            webAppInfo.listeners.add(listener.getListenerClass());
        }

        Map<String, ServletInfo> servletMap = new HashMap<String, ServletInfo>();
        for (Servlet servlet : webApp.getServlet()) {
            ServletInfo servletInfo;
            if (servlet.getServletClass() != null) {
                servletInfo = webAppInfoFactory.newServletInfo();
                servletInfo.servletClass = servlet.getServletClass();
            } else if (servlet.getJspFile() != null) {
                servletInfo = webAppInfoFactory.newJspInfo(servlet.getJspFile());
            } else {
                problems.add("\nNo servlet class or jsp file for servlet " + servlet.getServletName());
                continue;
            }
            servletInfo.servletName = servlet.getServletName();
            if (servlet.getAsyncSupported() != null) {
                servletInfo.asyncSupported = servlet.getAsyncSupported();
            }
            servletInfo.loadOnStartup = servlet.getLoadOnStartup();
            if (servlet.getRunAs() != null) {
                servletInfo.runAsRole = servlet.getRunAs().getRoleName().trim();
            }
            if (servlet.getMultipartConfig() != null) {
                MultipartConfig multipartConfig = servlet.getMultipartConfig();
                MultipartConfigInfo multipartConfigInfo = new MultipartConfigInfo();
                multipartConfigInfo.location = multipartConfig.getLocation();
                multipartConfigInfo.maxFileSize = multipartConfig.getMaxFileSize();
                multipartConfigInfo.maxRequestSize = multipartConfig.getMaxRequestSize();
                multipartConfigInfo.fileSizeThreshold = multipartConfig.getFileSizeThreshold();
                servletInfo.multipartConfigInfo = multipartConfigInfo;
            }
            addParams(servlet.getInitParam(), servletInfo.initParams);
            for (SecurityRoleRef securityRoleRef : servlet.getSecurityRoleRef()) {
                SecurityRoleRefInfo securityRoleRefInfo = new SecurityRoleRefInfo();
                if (securityRoleRef.getRoleLink() != null) {
                    securityRoleRefInfo.roleLink = securityRoleRef.getRoleLink().trim();
                }
                securityRoleRefInfo.roleName = securityRoleRef.getRoleName().trim();
                servletInfo.securityRoleRefs.add(securityRoleRefInfo);
            }
            webAppInfo.servlets.add(servletInfo);
            servletMap.put(servletInfo.servletName, servletInfo);
        }
        for (ServletMapping servletMapping : webApp.getServletMapping()) {
            String servletName = servletMapping.getServletName().trim();
            ServletInfo servletInfo = servletMap.get(servletName);
            if (servletInfo == null) {
                problems.add("\nNo servlet matching servlet mappings for " + servletName);
            } else {
                normalizeUrlPatterns(servletMapping.getUrlPattern(), servletInfo.servletMappings);
            }
        }

        Map<String, FilterInfo> filterMap = new HashMap<String, FilterInfo>();
        for (Filter filter : webApp.getFilter()) {
            FilterInfo filterInfo = webAppInfoFactory.newFilterInfo();
            filterInfo.filterName = filter.getFilterName().trim();
            filterInfo.filterClass = filter.getFilterClass();
            filterInfo.asyncSupported = filter.isAsyncSupported();
            addParams(filter.getInitParam(), filterInfo.initParams);
            webAppInfo.filters.add(filterInfo);
            filterMap.put(filterInfo.filterName, filterInfo);
        }
        for (FilterMapping filterMapping : webApp.getFilterMapping()) {
            String filterName = filterMapping.getFilterName().trim();
            FilterInfo filterInfo = filterMap.get(filterName);
            if (filterInfo == null) {
                problems.add("\nNo filter matching filter mappings for " + filterName);
            } else {
                if (!filterMapping.getServletName().isEmpty()) {
                    FilterMappingInfo servletMapping = new FilterMappingInfo();
                    servletMapping.dispatchers = toEnumSet(filterMapping.getDispatcher());
                    servletMapping.mapping.addAll(filterMapping.getServletName());
                    filterInfo.servletMappings.add(servletMapping);
                }
                if (!filterMapping.getUrlPattern().isEmpty()) {
                    FilterMappingInfo urlMapping = new FilterMappingInfo();
                    urlMapping.dispatchers = toEnumSet(filterMapping.getDispatcher());
                    normalizeUrlPatterns(filterMapping.getUrlPattern(), urlMapping.mapping);
                    filterInfo.urlMappings.add(urlMapping);
                }
            }
        }

        webAppInfo.displayName = webApp.getDisplayName();
        
        for (ErrorPage errorPage: webApp.getErrorPage()) {
            ErrorPageInfo errorPageInfo = new ErrorPageInfo();
            errorPageInfo.location = errorPage.getLocation();
            if (errorPage.getErrorCode() != null) {
                errorPageInfo.errorCode = errorPage.getErrorCode().intValue();
            }
            errorPageInfo.exceptionType = errorPage.getExceptionType();
            webAppInfo.errorPages.add(errorPageInfo);
        }

        for (LocaleEncodingMappingList localeEncodingMappingList: webApp.getLocaleEncodingMappingList()) {
            for (LocaleEncodingMapping localeEncodingMapping: localeEncodingMappingList.getLocaleEncodingMapping()) {
                webAppInfo.localeEncodingMappings.put(localeEncodingMapping.getLocale(), localeEncodingMapping.getEncoding());
            }
        }
        for (MimeMapping mimeMapping: webApp.getMimeMapping()) {
            webAppInfo.mimeMappings.put(mimeMapping.getExtension(), mimeMapping.getMimeType());
        }

        for (WelcomeFileList welcomeFileList: webApp.getWelcomeFileList()) {
            webAppInfo.welcomeFiles.addAll(welcomeFileList.getWelcomeFile());
        }

        for (LoginConfig loginConfig: webApp.getLoginConfig()) {
            LoginConfigInfo loginConfigInfo = new LoginConfigInfo();
            loginConfigInfo.authMethod = loginConfig.getAuthMethod();
            loginConfigInfo.realmName = loginConfig.getRealmName();
            if (loginConfig.getFormLoginConfig() != null) {
                loginConfigInfo.formLoginPage = loginConfig.getFormLoginConfig().getFormLoginPage();
                loginConfigInfo.formErrorPage = loginConfig.getFormLoginConfig().getFormErrorPage();
            }
            webAppInfo.loginConfig = loginConfigInfo;
            break;
        }

        for (SecurityConstraint securityConstraint : webApp.getSecurityConstraint()) {
            SecurityConstraintInfo securityConstraintInfo = webAppInfoFactory.newSecurityConstraintInfo();
            if (securityConstraint.getAuthConstraint() != null) {
                securityConstraintInfo.authConstraint = new AuthConstraintInfo();
                securityConstraintInfo.authConstraint.roleNames.addAll(securityConstraint.getAuthConstraint().getRoleName());
            }
            if (securityConstraint.getUserDataConstraint() != null) {
                securityConstraintInfo.userDataConstraint = securityConstraint.getUserDataConstraint().getTransportGuarantee().value();
            }
            for (WebResourceCollection webResourceCollection : securityConstraint.getWebResourceCollection()) {
                WebResourceCollectionInfo webResourceCollectionInfo = new WebResourceCollectionInfo();
                webResourceCollectionInfo.webResourceName = webResourceCollection.getWebResourceName();
                normalizeUrlPatterns(webResourceCollection.getUrlPattern(), webResourceCollectionInfo.urlPatterns);
                if (webResourceCollection.getHttpMethod().size() > 0) {
                    webResourceCollectionInfo.omission = false;
                    webResourceCollectionInfo.httpMethods.addAll(webResourceCollection.getHttpMethod());
                } else {
                    webResourceCollectionInfo.omission = true;
                    webResourceCollectionInfo.httpMethods.addAll(webResourceCollection.getHttpMethodOmission());
                }
                securityConstraintInfo.webResourceCollections.add(webResourceCollectionInfo);
            }
            webAppInfo.securityConstraints.add(securityConstraintInfo);
        }

        for (SecurityRole securityRole : webApp.getSecurityRole()) {
            webAppInfo.securityRoles.add(securityRole.getRoleName().trim());
        }

        webAppInfo.displayName = webApp.getDisplayName();

        for (ErrorPage errorPage: webApp.getErrorPage()) {
            ErrorPageInfo errorPageInfo = new ErrorPageInfo();
            errorPageInfo.location = errorPage.getLocation();
            if (errorPage.getErrorCode() != null) {
                errorPageInfo.errorCode = errorPage.getErrorCode().intValue();
            }
            errorPageInfo.exceptionType = errorPage.getExceptionType();
            webAppInfo.errorPages.add(errorPageInfo);
        }

        for (LocaleEncodingMappingList localeEncodingMappingList: webApp.getLocaleEncodingMappingList()) {
            for (LocaleEncodingMapping localeEncodingMapping: localeEncodingMappingList.getLocaleEncodingMapping()) {
                webAppInfo.localeEncodingMappings.put(localeEncodingMapping.getLocale(), localeEncodingMapping.getEncoding());
            }
        }
        for (MimeMapping mimeMapping: webApp.getMimeMapping()) {
            webAppInfo.mimeMappings.put(mimeMapping.getExtension(), mimeMapping.getMimeType());
        }

        for (WelcomeFileList welcomeFileList: webApp.getWelcomeFileList()) {
            webAppInfo.welcomeFiles.addAll(welcomeFileList.getWelcomeFile());
        }

        for (LoginConfig loginConfig: webApp.getLoginConfig()) {
            LoginConfigInfo loginConfigInfo = new LoginConfigInfo();
            loginConfigInfo.authMethod = loginConfig.getAuthMethod();
            loginConfigInfo.realmName = loginConfig.getRealmName();
            if (loginConfig.getFormLoginConfig() != null) {
                loginConfigInfo.formLoginPage = loginConfig.getFormLoginConfig().getFormLoginPage();
                loginConfigInfo.formErrorPage = loginConfig.getFormLoginConfig().getFormErrorPage();
            }
            webAppInfo.loginConfig = loginConfigInfo;
            break;
        }

        webAppInfoFactory.complete(webAppInfo);

        if (!problems.isEmpty()) {
            throw new DeploymentException("Problems encountered parsing web.xml: " + problems);
        }
        this.webAppInfo = webAppInfo;
        return webAppInfo;
    }

    public WebApp getWebApp() {
        return webApp;
    }

    public WebAppInfo getWebAppInfo() {
        return webAppInfo;
    }

    public ServletInfo copy(ServletInfo servletInfo) {
        return webAppInfoFactory.copy(servletInfo);
    }

    public FilterInfo copy(FilterInfo filterInfo) {
        return webAppInfoFactory.copy(filterInfo);
    }

    public static void normalizeUrlPatterns(Collection<String> source, Collection<String> target) {
        for (String pattern : source) {
            pattern = pattern.trim();
            if (!pattern.startsWith("*") && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
                //log.info("corrected url pattern to " + pattern);
            }
            target.add(pattern);
        }
    }

    protected void addParams(List<ParamValue> params, Map<String, String> paramMap) {
        for (ParamValue paramValue : params) {
            if (!paramMap.containsKey(paramValue.getParamName())) {
                paramMap.put(paramValue.getParamName(), paramValue.getParamValue());
            }
        }
    }

    private EnumSet<DispatcherType> toEnumSet(List<Dispatcher> dispatchers) {
        if (dispatchers.isEmpty()) {
            return EnumSet.of(DispatcherType.REQUEST);
        }
        List<DispatcherType> types = new ArrayList<DispatcherType>(dispatchers.size());
        for (Dispatcher dispatcher : dispatchers) {
            types.add(toDispatcherType(dispatcher));
        }
        return EnumSet.copyOf(types);
    }

    private DispatcherType toDispatcherType(Dispatcher dispatcher) {
        return DispatcherType.valueOf(dispatcher.name());
    }
}
