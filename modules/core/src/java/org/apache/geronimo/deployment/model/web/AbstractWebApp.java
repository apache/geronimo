/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.model.web;

import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.SecurityRole;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;

/**
 *
 * @version $Revision: 1.2 $
 */
public class AbstractWebApp extends Displayable {
    private String version;
    private boolean distributable = false;
    private ContextParam[] contextParam = new ContextParam[0];
    private Filter[] filter = new Filter[0];
    private FilterMapping[] filterMapping = new FilterMapping[0];
    private Listener[] listener = new Listener[0];
    private Servlet[] servlet = new Servlet[0];
    private ServletMapping[] servletMapping = new ServletMapping[0];
    private SessionConfig[] sessionConfig = new SessionConfig[0];
    private MimeMapping[] mimeMapping = new MimeMapping[0];
    private WelcomeFileList[] welcomeFileList = new WelcomeFileList[0];
    private ErrorPage[] errorPage = new ErrorPage[0];
    private JSPConfig[] jspConfig = new JSPConfig[0];
    private SecurityConstraint[] securityConstraint = new SecurityConstraint[0];
    private LoginConfig[] loginConfig = new LoginConfig[0];
    private SecurityRole[] securityRole = new SecurityRole[0];
    private EnvEntry[] envEntry = new EnvEntry[0];
    private LocaleEncodingMappingList[] localeEncodingMappingList = new LocaleEncodingMappingList[0];

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isDistributable() {
        return distributable;
    }

    public void setDistributable(boolean distributable) {
        this.distributable = distributable;
    }

    public ContextParam[] getContextParam() {
        return contextParam;
    }

    public ContextParam getContextParam(int i) {
        return contextParam[i];
    }

    public void setContextParam(ContextParam[] contextParam) {
        this.contextParam = contextParam;
    }

    public void setContextParam(int i, ContextParam contextParam) {
        this.contextParam[i] = contextParam;
    }

    public ErrorPage[] getErrorPage() {
        return errorPage;
    }

    public ErrorPage getErrorPage(int i) {
        return errorPage[i];
    }

    public void setErrorPage(ErrorPage[] errorPage) {
        this.errorPage = errorPage;
    }

    public void setErrorPage(int i, ErrorPage errorPage) {
        this.errorPage[i] = errorPage;
    }

    public Filter[] getFilter() {
        return filter;
    }

    public Filter getFilter(int i) {
        return filter[i];
    }

    public void setFilter(Filter[] filter) {
        this.filter = filter;
    }

    public void setFilter(int i, Filter filter) {
        this.filter[i] = filter;
    }

    public FilterMapping[] getFilterMapping() {
        return filterMapping;
    }

    public FilterMapping getFilterMapping(int i) {
        return filterMapping[i];
    }

    public void setFilterMapping(FilterMapping[] filterMapping) {
        this.filterMapping = filterMapping;
    }

    public void setFilterMapping(int i, FilterMapping filterMapping) {
        this.filterMapping[i] = filterMapping;
    }

    public JSPConfig[] getJspConfig() {
        return jspConfig;
    }

    public JSPConfig getJspConfig(int i) {
        return jspConfig[i];
    }

    public void setJspConfig(JSPConfig[] jspConfig) {
        this.jspConfig = jspConfig;
    }

    public void setJspConfig(int i, JSPConfig jspConfig) {
        this.jspConfig[i] = jspConfig;
    }

    public Listener[] getListener() {
        return listener;
    }

    public Listener getListener(int i) {
        return listener[i];
    }

    public void setListener(Listener[] listener) {
        this.listener = listener;
    }

    public void setListener(int i, Listener listener) {
        this.listener[i] = listener;
    }

    public LocaleEncodingMappingList[] getLocaleEncodingMappingList() {
        return localeEncodingMappingList;
    }

    public LocaleEncodingMappingList getLocaleEncodingMappingList(int i) {
        return localeEncodingMappingList[i];
    }

    public void setLocaleEncodingMappingList(LocaleEncodingMappingList[] localeEncodingMappingList) {
        this.localeEncodingMappingList = localeEncodingMappingList;
    }

    public void setLocaleEncodingMappingList(int i, LocaleEncodingMappingList localeEncodingMappingList) {
        this.localeEncodingMappingList[i] = localeEncodingMappingList;
    }

    public LoginConfig[] getLoginConfig() {
        return loginConfig;
    }

    public LoginConfig getLoginConfig(int i) {
        return loginConfig[i];
    }

    public void setLoginConfig(LoginConfig[] loginConfig) {
        this.loginConfig = loginConfig;
    }

    public void setLoginConfig(int i, LoginConfig loginConfig) {
        this.loginConfig[i] = loginConfig;
    }

    public MimeMapping[] getMimeMapping() {
        return mimeMapping;
    }

    public MimeMapping getMimeMapping(int i) {
        return mimeMapping[i];
    }

    public void setMimeMapping(MimeMapping[] mimeMapping) {
        this.mimeMapping = mimeMapping;
    }

    public void setMimeMapping(int i, MimeMapping mimeMapping) {
        this.mimeMapping[i] = mimeMapping;
    }

    public SecurityConstraint[] getSecurityConstraint() {
        return securityConstraint;
    }

    public SecurityConstraint getSecurityConstraint(int i) {
        return securityConstraint[i];
    }

    public void setSecurityConstraint(SecurityConstraint[] securityConstraint) {
        this.securityConstraint = securityConstraint;
    }

    public void setSecurityConstraint(int i, SecurityConstraint securityConstraint) {
        this.securityConstraint[i] = securityConstraint;
    }

    public SecurityRole[] getSecurityRole() {
        return securityRole;
    }

    public SecurityRole getSecurityRole(int i) {
        return securityRole[i];
    }

    public void setSecurityRole(SecurityRole[] securityRole) {
        this.securityRole = securityRole;
    }

    public void setSecurityRole(int i, SecurityRole securityRole) {
        this.securityRole[i] = securityRole;
    }

    public Servlet[] getServlet() {
        return servlet;
    }

    public Servlet getServlet(int i) {
        return servlet[i];
    }

    public void setServlet(Servlet[] servlet) {
        this.servlet = servlet;
    }

    public void setServlet(int i, Servlet servlet) {
        this.servlet[i] = servlet;
    }

    public ServletMapping[] getServletMapping() {
        return servletMapping;
    }

    public ServletMapping getServletMapping(int i) {
        return servletMapping[i];
    }

    public void setServletMapping(ServletMapping[] servletMapping) {
        this.servletMapping = servletMapping;
    }

    public void setServletMapping(int i, ServletMapping servletMapping) {
        this.servletMapping[i] = servletMapping;
    }

    public SessionConfig[] getSessionConfig() {
        return sessionConfig;
    }

    public SessionConfig getSessionConfig(int i) {
        return sessionConfig[i];
    }

    public void setSessionConfig(SessionConfig[] sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    public void setSessionConfig(int i, SessionConfig sessionConfig) {
        this.sessionConfig[i] = sessionConfig;
    }

    public WelcomeFileList[] getWelcomeFileList() {
        return welcomeFileList;
    }

    public WelcomeFileList getWelcomeFileList(int i) {
        return welcomeFileList[i];
    }

    public void setWelcomeFileList(WelcomeFileList[] welcomeFileList) {
        this.welcomeFileList = welcomeFileList;
    }

    public void setWelcomeFileList(int i, WelcomeFileList welcomeFileList) {
        this.welcomeFileList[i] = welcomeFileList;
    }

    public EnvEntry[] getEnvEntry() {
        return envEntry;
    }

    public EnvEntry getEnvEntry(int i) {
        return envEntry[i];
    }

    public void setEnvEntry(EnvEntry[] envEntry) {
        this.envEntry = envEntry;
    }

    public void setEnvEntry(int i, EnvEntry envEntry) {
        this.envEntry[i] = envEntry;
    }
}
