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


package org.apache.geronimo.jetty8.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyContextException;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.geronimo.osgi.web.WABApplicationConstants;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.WebApplicationName;
import org.apache.geronimo.web.WebModuleListener;
import org.apache.geronimo.web.assembler.Assembler;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.apache.geronimo.web.security.WebSecurityConstraintStore;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebAppContext extends WebAppContext {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoWebAppContext.class);

    private final IntegrationContext integrationContext;
    private final String modulePath;
    private final ClassLoader classLoader;
    private final WebAppInfo webAppInfo;
    private final WebSecurityConstraintStore webSecurityConstraintStore;
    private final String policyContextId;
    private final ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;
    private ServiceRegistration serviceRegistration;
    boolean fullyStarted = false;
    private String webModuleName;
    private final List webModuleListeners;

    public GeronimoWebAppContext(SecurityHandler securityHandler,
                                 SessionHandler sessionHandler,
                                 ServletHandler servletHandler,
                                 ErrorHandler errorHandler,
                                 IntegrationContext integrationContext,
                                 ClassLoader classLoader,
                                 String modulePath,
                                 WebAppInfo webAppInfo,
                                 String policyContextId,
                                 ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager,
                                 List<String> webModuleListenerClassNames) {
        super(sessionHandler, securityHandler, servletHandler, errorHandler);
        _scontext = new Context();
        this.integrationContext = integrationContext;
        setClassLoader(classLoader);
        this.classLoader = classLoader;

        Bundle bundle = BundleUtils.unwrapBundle(integrationContext.getBundle());

        setAttribute(WABApplicationConstants.BUNDLE_CONTEXT_ATTRIBUTE,
                     bundle.getBundleContext());

        setAttribute(WebApplicationConstants.WEB_APP_INFO, webAppInfo);

        setAttribute("org.springframework.osgi.web." + BundleContext.class.getName(),
                     bundle.getBundleContext());

        setAttribute(WebApplicationConstants.WEB_APP_NAME, webModuleName);

        // now set the module context ValidatorFactory in a context property.
        try {
            javax.naming.Context ctx = integrationContext.getComponentContext();
            Object validatorFactory = ctx.lookup("comp/ValidatorFactory");
            setAttribute("javax.faces.validator.beanValidator.ValidatorFactory", validatorFactory);
        } catch (NamingException e) {
            // ignore.  We just don't set the property if it's not available.
        }
        this.modulePath = modulePath;
        this.webAppInfo = webAppInfo;
        this.policyContextId = policyContextId;
        this.applicationPolicyConfigurationManager = applicationPolicyConfigurationManager;
        //TODO schemaVersion >= 2.5f && !metaComplete but only for a while....
        boolean annotationScanRequired = true;
        webSecurityConstraintStore = new WebSecurityConstraintStore(webAppInfo, integrationContext.getBundle(), annotationScanRequired, _scontext);

        if (webModuleListenerClassNames != null && webModuleListenerClassNames.size() > 0) {
            webModuleListeners = new ArrayList(webModuleListenerClassNames.size());
            for (String webModuleListenerClassName : webModuleListenerClassNames) {
                try {
                    Class<?> cls = classLoader.loadClass(webModuleListenerClassName);
                    Object webModuleListener = cls.newInstance();
                    webModuleListeners.add(webModuleListener);
                } catch (ClassNotFoundException e) {
                    logger.warn("Unable to load the listener class" + webModuleListenerClassName, e);
                } catch (InstantiationException e) {
                    logger.warn("Unable to create the listener instance " + webModuleListenerClassName, e);
                } catch (IllegalAccessException e) {
                    logger.warn("Unable to create the listener instance " + webModuleListenerClassName, e);
                }
            }
        } else {
            webModuleListeners = Collections.emptyList();
        }
    }

    public void setWebModuleName(String webModuleName) {
        this.webModuleName = webModuleName;
    }

    public void registerServletContext() {
        // for OSGi Web Applications support register ServletContext in service registry
        Bundle bundle = integrationContext.getBundle();
        if (WebApplicationUtils.isWebApplicationBundle(bundle)) {
            serviceRegistration = WebApplicationUtils.registerServletContext(bundle, getServletContext());
        }
    }

    public void unregisterServletContext() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Override
    protected void doStart() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        String oldApplicationName = WebApplicationName.getName();
        WebApplicationName.setName(integrationContext.getWebApplicationIdentity());
        try {
            setRestrictListeners(false);
            for (Object webModuleListener : webModuleListeners) {
                if (webModuleListener instanceof WebModuleListener) {
                    ((WebModuleListener) webModuleListener).moduleInitialized(getServletContext());
                } else {
                    logger.warn("Invalid WebModuleListener " + webModuleListener.getClass().getName());
                }
            }
            try {
                Assembler assembler = new Assembler();
                assembler.assemble(getServletContext(), webAppInfo);
                webSecurityConstraintStore.setAnnotationScanRequired(true);
                ((GeronimoWebAppContext.Context) _scontext).webXmlProcessed = true;
                for (Map.Entry<ServletContainerInitializer, Set<Class<?>>> entry: integrationContext.getServletContainerInitializerMap().entrySet()) {
                     entry.getKey().onStartup(entry.getValue(), getServletContext());
                }
                super.doStart();
                if (!isAvailable()) {
                    Throwable e = getUnavailableException();
                    if (e instanceof Exception) {
                        throw (Exception)e;
                    }
                    if (e instanceof Throwable) {
                        throw new Exception("Could not start web app", e);
                    }
                    throw new Exception("Could not start web app for unknown reason");
                }
                if (applicationPolicyConfigurationManager != null) {
                    SpecSecurityBuilder specSecurityBuilder = new SpecSecurityBuilder(webSecurityConstraintStore.exportMergedWebAppInfo());
                    Map<String, ComponentPermissions> contextIdPermissionsMap = new HashMap<String, ComponentPermissions>();
                    contextIdPermissionsMap.put(policyContextId, specSecurityBuilder.buildSpecSecurityConfig());
                    //Update ApplicationPolicyConfigurationManager
                    try {
                        applicationPolicyConfigurationManager.updateApplicationPolicyConfiguration(contextIdPermissionsMap);
                    } catch (LoginException e) {
                        throw new RuntimeException("Fail to set application policy configurations", e);
                    } catch (PolicyContextException e) {
                        throw new RuntimeException("Fail to set application policy configurations", e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Fail to set application policy configurations", e);
                    } finally {
                        //Clear SpecSecurityBuilder
                        specSecurityBuilder.clear();
                    }
                }
                fullyStarted = true;
            } finally {
                setRestrictListeners(true);
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
            WebApplicationName.setName(oldApplicationName);
        }
    }

    @Override
    protected void doStop() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        String oldApplicationName = WebApplicationName.getName();
        WebApplicationName.setName(integrationContext.getWebApplicationIdentity());
        try {
            try {
                super.doStop();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
            for (Object webModuleListener : webModuleListeners) {
                if (webModuleListener instanceof WebModuleListener) {
                    ((WebModuleListener) webModuleListener).moduleDestoryed(getServletContext());
                } else {
                    logger.warn("Invalid WebModuleListener " + webModuleListener.getClass().getName());
                }
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
            WebApplicationName.setName(oldApplicationName);
        }
    }

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(baseRequest);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(baseRequest, newContext);
        String oldApplicationName = WebApplicationName.getName();
        WebApplicationName.setName(integrationContext.getWebApplicationIdentity());
        try {
            try {
                super.doScope(target, baseRequest, request, response);
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, baseRequest, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, baseRequest);
            WebApplicationName.setName(oldApplicationName);
        }
    }

    @Override
    protected boolean isProtectedTarget(String target) {
        while (target.startsWith("//")) {
            target=URIUtil.compactPath(target);
        }

        return StringUtil.startsWithIgnoreCase(target, "/web-inf") ||
               StringUtil.startsWithIgnoreCase(target, "/meta-inf") ||
               StringUtil.startsWithIgnoreCase(target, "/osgi-inf") ||
               StringUtil.startsWithIgnoreCase(target, "/osgi-opt");
    }

    @Override
    public Resource newResource(String url) throws IOException {
        if (url == null) {
            return null;
        }
        return newResource(new URL(url));
    }

    @Override
    public Resource newResource(URL url) throws IOException {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol();
        if ("bundle".equals(protocol) ||
            "bundleentry".equals(protocol)) {
            return lookupResource(url.getPath());
        } else {
            return super.newResource(url);
        }
    }

    @Override
    public Resource getResource(String uriInContext) throws MalformedURLException {
        if (uriInContext == null || !uriInContext.startsWith("/")) {
            throw new MalformedURLException("Path must not be null and must start with '/': " + uriInContext);
        }
        if (modulePath != null) {
            uriInContext = modulePath + uriInContext;
        }
        return lookupResource(uriInContext);
    }

    @Override
    public Set<String> getResourcePaths(String uriInContext) {
        if (uriInContext == null || !uriInContext.startsWith("/")) {
            return Collections.emptySet();
        }
        if (modulePath != null) {
            uriInContext = modulePath + uriInContext;
        }
        HashSet<String> paths = new HashSet<String>();
        Bundle bundle = integrationContext.getBundle();
        Enumeration<String> e = bundle.getEntryPaths(uriInContext);
        if (e != null) {
            while (e.hasMoreElements()) {
                paths.add("/" + e.nextElement());
            }
        }
        return paths;
    }

    @Override
    protected ServletRegistration.Dynamic dynamicHolderAdded(ServletHolder holder) {
        ServletRegistration.Dynamic registration = holder.getRegistration();
        String servletClassName = holder.getClassName();
        Servlet servlet = holder.getServletInstance();
        if (servlet == null || webSecurityConstraintStore.isContainerCreatedDynamicServlet(servlet)) {
            webSecurityConstraintStore.addContainerCreatedDynamicServletEntry(registration, servletClassName);
        }
        return registration;
    }

    public Set<String> setServletSecurity(ServletRegistration.Dynamic registration, ServletSecurityElement servletSecurityElement) {
        return webSecurityConstraintStore.setDynamicServletSecurity(registration, servletSecurityElement);
    }

    @Override
    protected void addRoles(String... roles) {
        webSecurityConstraintStore.declareRoles(roles);
    }

    private Resource lookupResource(String uriInContext) {
        Bundle bundle = integrationContext.getBundle();
        URL url;
        try {
            url = BundleUtils.getEntry(bundle, uriInContext);
        } catch (MalformedURLException e) {
            logger.warn("MalformedURLException when getting entry:" + uriInContext + " from bundle " + bundle.getSymbolicName(), e);
            url = null;
        }
        if (url == null) {
            url = bundle.getResource("META-INF/resources" + uriInContext);
            if (url == null) {
                return null;
            }
        }
        if (uriInContext.endsWith("/")) {
            Enumeration<String> paths = BundleUtils.getEntryPaths(bundle, uriInContext);
            return new BundlePathResource(url, paths);
        } else {
            return new BundleFileResource(url);
        }
    }

    private static class BundleFileResource extends URLResource {

        protected BundleFileResource(URL url) {
            super(url, null);
        }

        /*
         * Always return true as we are pretty sure the resource does exist. This prevents
         * NPE as described at https://bugs.eclipse.org/bugs/show_bug.cgi?id=193269
         */
        @Override
        public boolean exists() {
            return true;
        }
    }

    public class Context extends WebAppContext.Context {

        protected boolean webXmlProcessed = false;

        @Override
        public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
            try {
                return (T) integrationContext.getHolder().newInstance(c.getName(), classLoader, integrationContext.getComponentContext());
            } catch (IllegalAccessException e) {
                throw new ServletException("Could not create filter " + c.getName(), e);
            } catch (InstantiationException e) {
                throw new ServletException("Could not create filter " + c.getName(), e);
            }
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
            try {
                return (T) integrationContext.getHolder().newInstance(c.getName(), classLoader, integrationContext.getComponentContext());
            } catch (IllegalAccessException e) {
                throw new ServletException("Could not create listener " + c.getName(), e);
            } catch (InstantiationException e) {
                throw new ServletException("Could not create listener " + c.getName(), e);
            }
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
            try {
                T servlet = (T) integrationContext.getHolder().newInstance(c.getName(), classLoader, integrationContext.getComponentContext());
                webSecurityConstraintStore.addContainerCreatedDynamicServlet(servlet);
                return servlet;
            } catch (IllegalAccessException e) {
                throw new ServletException("Could not create servlet " + c.getName(), e);
            } catch (InstantiationException e) {
                throw new ServletException("Could not create servlet " + c.getName(), e);
            }
        }

    }

}
