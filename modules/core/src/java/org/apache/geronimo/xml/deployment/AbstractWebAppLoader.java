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
package org.apache.geronimo.xml.deployment;

import org.apache.geronimo.deployment.model.web.AbstractWebApp;
import org.apache.geronimo.deployment.model.web.ContextParam;
import org.apache.geronimo.deployment.model.web.SecurityConstraint;
import org.apache.geronimo.deployment.model.web.Filter;
import org.apache.geronimo.deployment.model.web.FilterMapping;
import org.apache.geronimo.deployment.model.web.Listener;
import org.apache.geronimo.deployment.model.web.Servlet;
import org.apache.geronimo.deployment.model.web.ServletMapping;
import org.apache.geronimo.deployment.model.web.SessionConfig;
import org.apache.geronimo.deployment.model.web.MimeMapping;
import org.apache.geronimo.deployment.model.web.WelcomeFileList;
import org.apache.geronimo.deployment.model.web.ErrorPage;
import org.apache.geronimo.deployment.model.web.JSPConfig;
import org.apache.geronimo.deployment.model.web.Taglib;
import org.apache.geronimo.deployment.model.web.JSPPropertyGroup;
import org.apache.geronimo.deployment.model.web.WebResourceCollection;
import org.apache.geronimo.deployment.model.web.AuthConstraint;
import org.apache.geronimo.deployment.model.web.UserDataConstraint;
import org.apache.geronimo.deployment.model.web.LoginConfig;
import org.apache.geronimo.deployment.model.web.FormLoginConfig;
import org.apache.geronimo.deployment.model.web.LocaleEncodingMappingList;
import org.apache.geronimo.deployment.model.web.LocaleEncodingMapping;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.w3c.dom.Element;


/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/09/29 12:38:13 $
 */
public abstract class AbstractWebAppLoader {
    public AbstractWebAppLoader() {
    }

    protected void loadCommonElements(AbstractWebApp webApp, Element root) {
        webApp.setVersion(LoaderUtil.getAttribute(root, "version"));

        J2EELoader.loadDisplayable(root, webApp);
        webApp.setDistributable(LoaderUtil.getBoolean(root, "distributable"));
        loadContextParams(webApp, root);
        loadFilterTypes(webApp, root);
        loadFilterMappings(webApp, root);
        loadListeners(webApp, root);
        loadServlets(webApp, root);
        loadServletMappings(webApp, root);
        loadSessionConfigs(webApp, root);
        loadMimeMappings(webApp, root);
        loadWelcomeFileLists(webApp, root);
        loadErrorPages(webApp, root);
        loadJspConfigs(webApp, root);
        loadSecurityConstraints(webApp, root);
        loadLoginConfigs(webApp, root);
        webApp.setSecurityRole(J2EELoader.loadSecurityRoles(root));
        loadLocaleEncodingMappingLists(webApp, root);
    }

    private void loadContextParams(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "context-param");
        ContextParam[] params = new ContextParam[elements.length];

        for (int i=0; i < elements.length; i++) {
            Element e = elements[i];
            ContextParam param = new ContextParam();

            param.setParamName(LoaderUtil.getChildContent(e, "param-name"));
            param.setParamValue(LoaderUtil.getChildContent(e, "param-value"));

            params[i] = param;
        }
        webApp.setContextParam(params);
    }

    private void loadFilterTypes(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "filter");
        Filter[] filters = new Filter[elements.length];

        for (int i=0; i < elements.length; i++) {
            Element e = elements[i];
            final Filter filter = new Filter();

            J2EELoader.loadDisplayable(e, filter);
            filter.setFilterName(LoaderUtil.getChildContent(e, "filter-name"));
            filter.setFilterClass(LoaderUtil.getChildContent(e, "filter-class"));
            filter.setInitParam(J2EELoader.loadInitParams(e));

            filters[i] = filter;
        }
        webApp.setFilter(filters);
    }


    private void loadFilterMappings(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "filter-mapping");
        FilterMapping[] mappings = new FilterMapping[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            FilterMapping mapping = new FilterMapping();

            mapping.setFilterName(LoaderUtil.getChildContent(e, "filter-name"));
            mapping.setUrlPattern(LoaderUtil.getChildContent(e, "url-pattern"));
            mapping.setServletName(LoaderUtil.getChildContent(e, "servlet-name"));
            loadDispatcher(mapping, e);

            mappings[i] = mapping;
        }
        webApp.setFilterMapping(mappings);
    }

    private void loadDispatcher(FilterMapping mapping, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "dispatcher");
        String[] dispatchers = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            dispatchers[i] = LoaderUtil.getContent(e);
        }
        mapping.setDispatcher(dispatchers);
    }

    private void loadListeners(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "listener");
        Listener[] listeners = new Listener[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            Listener listener = new Listener();

            J2EELoader.loadDisplayable(e, listener);
            listener.setListenerClass(LoaderUtil.getChildContent(e, "listener-class"));

            listeners[i] = listener;
        }
        webApp.setListener(listeners);

    }

    private void loadServlets(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "servlet");
        Servlet[] servlets = new Servlet[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            final Servlet servlet = new Servlet();

            J2EELoader.loadDisplayable(e, servlet);
            servlet.setServletName(LoaderUtil.getChildContent(e, "servlet-name"));
            servlet.setServletClass(LoaderUtil.getChildContent(e, "servlet-class"));
            servlet.setJspFile(LoaderUtil.getChildContent(e, "jsp-file"));
            servlet.setInitParam(J2EELoader.loadInitParams(e));
            String value = LoaderUtil.getChildContent(e, "load-on-startup");
            if (value != null) {
                servlet.setLoadOnStartup(new Integer(value));
            }
            servlet.setRunAs(J2EELoader.loadRunAs(e));
            servlet.setSecurityRoleRef(J2EELoader.loadSecurityRoleRefs(e));

            servlets[i] = servlet;
        }
        webApp.setServlet(servlets);
    }

    private void loadServletMappings(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "servlet-mapping");
        ServletMapping[] mappings = new ServletMapping[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            ServletMapping mapping = new ServletMapping();

            mapping.setServletName(LoaderUtil.getChildContent(e, "servlet-name"));
            mapping.setUrlPattern(LoaderUtil.getChildContent(e, "url-pattern"));

            mappings[i] = mapping;
        }
        webApp.setServletMapping(mappings);
    }

    private void loadSessionConfigs(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "session-config");
        SessionConfig[] sessionConfigs = new SessionConfig[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            SessionConfig sessionConfig = new SessionConfig();

            String value = LoaderUtil.getChildContent(e, "session-timeout");
            if (value != null) {
                sessionConfig.setSessionTimeout(new Integer(value));
            }

            sessionConfigs[i] = sessionConfig;
        }
        webApp.setSessionConfig(sessionConfigs);
    }

    private void loadMimeMappings(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "mime-mapping");
        MimeMapping[] mappings = new MimeMapping[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            MimeMapping mapping = new MimeMapping();

            mapping.setExtension(LoaderUtil.getChildContent(e, "extension"));
            mapping.setMimeType(LoaderUtil.getChildContent(e, "mime-type"));

            mappings[i] = mapping;
        }
        webApp.setMimeMapping(mappings);
    }

    private void loadWelcomeFileLists(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "welcome-file-list");
        WelcomeFileList[] fileLists = new WelcomeFileList[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            WelcomeFileList fileList = new WelcomeFileList();

            loadWelcomeFiles(fileList, e);

            fileLists[i] = fileList;
        }
        webApp.setWelcomeFileList(fileLists);
    }

    private void loadWelcomeFiles(WelcomeFileList fileList, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "welcome-file");
        String[] constraints = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            constraints[i] = LoaderUtil.getContent(elements[i]);
        }
        fileList.setWelcomeFile(constraints);
    }

    private void loadErrorPages(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "error-page");
        ErrorPage[] errorPages = new ErrorPage[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            ErrorPage errorPage = new ErrorPage();

            errorPage.setErrorCode(new Integer(LoaderUtil.getChildContent(e, "error-code")));
            errorPage.setExceptionType(LoaderUtil.getChildContent(e, "exception-type"));
            errorPage.setLocation(LoaderUtil.getChildContent(e, "location"));

            errorPages[i] = errorPage;
        }
        webApp.setErrorPage(errorPages);
    }

    private void loadJspConfigs(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "jsp-config");
        JSPConfig[] jspConfigs = new JSPConfig[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            JSPConfig jspConfig = new JSPConfig();

            loadTagLib(jspConfig, e);
            loadJspPropertyGroup(jspConfig, e);

            jspConfigs[i] = jspConfig;
        }
        webApp.setJspConfig(jspConfigs);
    }

    private void loadTagLib(JSPConfig jspConfig, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "taglib");
        Taglib[] taglibs = new Taglib[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            Taglib taglib = new Taglib();

            taglib.setTaglibUri(LoaderUtil.getChildContent(e, "taglib-uri"));
            taglib.setTaglibLocation(LoaderUtil.getChildContent(e, "taglib-location"));

            taglibs[i] = taglib;
        }
        jspConfig.setTaglib(taglibs);
    }

    private void loadJspPropertyGroup(JSPConfig jspConfig, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "jsp-property-group");
        JSPPropertyGroup[] jspPropertyGroups = new JSPPropertyGroup[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            JSPPropertyGroup jspPropertyGroup = new JSPPropertyGroup();

            J2EELoader.loadDisplayable(e, jspPropertyGroup);
            loadUrlPatterns(jspPropertyGroup, e);
            String value = LoaderUtil.getChildContent(e, "el-ignored");
            if (value != null) {
                jspPropertyGroup.setELIgnored(new Boolean(value));
            }
            value = LoaderUtil.getChildContent(e, "page-encoding");
            if (value != null) {
                jspPropertyGroup.setPageEncoding(value);
            }
            value = LoaderUtil.getChildContent(e, "scripting-invalid");
            if (value != null) {
                jspPropertyGroup.setScriptingInvalid(new Boolean(value));
            }
            value = LoaderUtil.getChildContent(e, "is-xml");
            if (value != null) {
                jspPropertyGroup.setXML(new Boolean(value));
            }
            loadIncludePrelude(jspPropertyGroup, e);
            loadIncludeCoda(jspPropertyGroup, e);

            jspPropertyGroups[i] = jspPropertyGroup;
        }
        jspConfig.setJSPPropertyGroup(jspPropertyGroups);
    }

    private void loadUrlPatterns(JSPPropertyGroup jspPropertyGroup, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "url-pattern");
        String[] patterns = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            patterns[i] = LoaderUtil.getContent(e);
        }
        jspPropertyGroup.setURLPattern(patterns);
    }

    private void loadIncludePrelude(JSPPropertyGroup jspPropertyGroup, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "include-prelude");
        String[] includes = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            includes[i] = LoaderUtil.getContent(e);
        }
        jspPropertyGroup.setIncludePrelude(includes);
    }

    private void loadIncludeCoda(JSPPropertyGroup jspPropertyGroup, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "include-coda");
        String[] includes = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            includes[i] = LoaderUtil.getContent(e);
        }
        jspPropertyGroup.setIncludeCoda(includes);
    }

    private void loadSecurityConstraints(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "security-constraint");
        SecurityConstraint[] constraints = new SecurityConstraint[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            SecurityConstraint constraint = new SecurityConstraint();

            constraint.setDisplayName(loadDisplayable(e));
            loadWebResourceCollections(constraint, e);
            loadAuthConstraint(constraint, e);
            loadUserDataConstraint(constraint, e);

            constraints[i] = constraint;
        }
        webApp.setSecurityConstraint(constraints);
    }

    private static DisplayName[] loadDisplayable(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "display-name");
        DisplayName[] ds = new DisplayName[roots.length];

        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            ds[i] = new DisplayName();

            ds[i].setLang(root.getAttribute("lang"));
            ds[i].setContent(LoaderUtil.getContent(root));
        }
        return ds;
    }

    private void loadWebResourceCollections(SecurityConstraint constraint, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "web-resource-collection");
        WebResourceCollection[] collections = new WebResourceCollection[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            WebResourceCollection collection = new WebResourceCollection();

            collection.setWebResourceName(LoaderUtil.getChildContent(e, "web-resource-name"));
            J2EELoader.loadDescribable(e, collection);
            loadUrlPatterns(collection, e);
            loadHttpMethods(collection, e);

            collections[i] = collection;
        }
        constraint.setWebResourceCollection(collections);
    }

    private void loadUrlPatterns(WebResourceCollection collection, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "url-pattern");
        String[] patterns = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            patterns[i] = LoaderUtil.getContent(e);
        }
        collection.setUrlPattern(patterns);
    }

    private void loadHttpMethods(WebResourceCollection collection, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "http-method");
        String[] patterns = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            patterns[i] = LoaderUtil.getContent(e);
        }
        collection.setHttpMethod(patterns);
    }

    private void loadAuthConstraint(SecurityConstraint securityConstraint, Element root) {
        Element element = LoaderUtil.getChild(root, "auth-constraint");

        if (element != null) {
            AuthConstraint constraint = new AuthConstraint();

            J2EELoader.loadDescribable(element, constraint);
            loadRoleNames(constraint, element);

            securityConstraint.setAuthConstraint(constraint);
        }
    }

    private void loadRoleNames(AuthConstraint constraint, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "role-name");
        String[] names = new String[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            names[i] = LoaderUtil.getContent(e);
        }
        constraint.setRoleName(names);
    }

    private void loadUserDataConstraint(SecurityConstraint securityConstraint, Element root) {
        Element element = LoaderUtil.getChild(root, "user-data-constraint");

        if (element != null) {
            UserDataConstraint constraint = new UserDataConstraint();

            J2EELoader.loadDescribable(element, constraint);
            constraint.setTransportGuarantee(LoaderUtil.getChildContent(element, "transport-guarantee"));

            securityConstraint.setUserDataConstraint(constraint);
        }
    }

    private void loadLoginConfigs(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "login-config");
        LoginConfig[] loginConfigs = new LoginConfig[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            LoginConfig loginConfig = new LoginConfig();

            loadAuthMethod(loginConfig, e);
            loadRealmName(loginConfig, e);
            loadFormLoginConfig(loginConfig, e);

            loginConfigs[i] = loginConfig;
        }
        webApp.setLoginConfig(loginConfigs);
    }

    private void loadAuthMethod(LoginConfig loginConfig, Element root) {
        Element element = LoaderUtil.getChild(root, "auth-method");

        if (element != null) {
            loginConfig.setAuthMethod(LoaderUtil.getContent(element));
        }
    }

    private void loadRealmName(LoginConfig loginConfig, Element root) {
        Element element = LoaderUtil.getChild(root, "realm-name");

        if (element != null) {
            loginConfig.setRealmName(LoaderUtil.getContent(element));
        }
    }

    private void loadFormLoginConfig(LoginConfig loginConfig, Element root) {
        Element element = LoaderUtil.getChild(root, "form-login-config");

        if (element != null) {
            FormLoginConfig formLoginConfig = new FormLoginConfig();

            formLoginConfig.setFormLoginPage(LoaderUtil.getChildContent(element, "form-login-page"));
            formLoginConfig.setFormErrorPage(LoaderUtil.getChildContent(element, "form-error-page"));

            loginConfig.setFormLoginConfig(formLoginConfig);
        }
    }

    private void loadLocaleEncodingMappingLists(AbstractWebApp webApp, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "locale-encoding-mapping-list");
        LocaleEncodingMappingList[] mappingLists = new LocaleEncodingMappingList[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            LocaleEncodingMappingList mappingList = new LocaleEncodingMappingList();

            loadLocaleEncodingMappings(mappingList, e);

            mappingLists[i] = mappingList;
        }
        webApp.setLocaleEncodingMappingList(mappingLists);
    }

    private void loadLocaleEncodingMappings(LocaleEncodingMappingList mappingList, Element root) {
        Element[] elements = LoaderUtil.getChildren(root, "locale-encoding-mapping");
        LocaleEncodingMapping[] mappings = new LocaleEncodingMapping[elements.length];

        for (int i=0; i<elements.length; i++) {
            Element e = elements[i];
            LocaleEncodingMapping mapping = new LocaleEncodingMapping();

            mapping.setLocale(LoaderUtil.getChildContent(e, "locale"));
            mapping.setEncoding(LoaderUtil.getChildContent(e, "encoding"));

            mappings[i] = mapping;
        }
        mappingList.setLocaleEncodingMapping(mappings);
    }
}
