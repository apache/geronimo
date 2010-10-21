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
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.geronimo.osgi.web.WebApplicationConstants;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
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
import org.osgi.framework.ServiceRegistration;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebAppContext extends WebAppContext {

    private final IntegrationContext integrationContext;
    private final String modulePath;
    private final ClassLoader classLoader;
    private final WebAppInfo webAppInfo;
    private final WebSecurityConstraintStore webSecurityConstraintStore;
    private final String policyContextId;
    private final ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;
    private ServiceRegistration serviceRegistration;
    boolean fullyStarted = false;

    public GeronimoWebAppContext(SecurityHandler securityHandler,
                                 SessionHandler sessionHandler,
                                 ServletHandler servletHandler,
                                 ErrorHandler errorHandler,
                                 IntegrationContext integrationContext,
                                 ClassLoader classLoader,
                                 String modulePath,
                                 WebAppInfo webAppInfo, String policyContextId, ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager) {
        super(sessionHandler, securityHandler, servletHandler, errorHandler);
        _scontext = new Context();
        this.integrationContext = integrationContext;
        setClassLoader(classLoader);
        this.classLoader = classLoader;
        setAttribute(WebApplicationConstants.BUNDLE_CONTEXT_ATTRIBUTE, integrationContext.getBundle().getBundleContext());
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
        try {
            setRestrictListeners(false);
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
        }
    }

    @Override
    protected void doStop() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        try {
            try {
                super.doStop();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
        }
    }

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(baseRequest);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(baseRequest, newContext);
        Map<String, Object> owbContext = integrationContext.contextEntered();
        try {
            try {
                super.doScope(target, baseRequest, request, response);
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, baseRequest, newContext);
            }
        } finally {
            integrationContext.contextExited(owbContext);
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, baseRequest);
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
        URL url = BundleUtils.getEntry(bundle, uriInContext);
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
