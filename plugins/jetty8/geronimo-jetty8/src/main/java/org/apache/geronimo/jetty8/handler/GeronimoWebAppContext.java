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
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.geronimo.osgi.web.WebApplicationConstants;
import org.apache.geronimo.osgi.web.WebApplicationUtils;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoWebAppContext extends WebAppContext {

    private final IntegrationContext integrationContext;
    private final String modulePath;
    private ServiceRegistration serviceRegistration;

    public GeronimoWebAppContext(SecurityHandler securityHandler, SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler, IntegrationContext integrationContext, ClassLoader classLoader, String modulePath) {
        super(sessionHandler, securityHandler, servletHandler, errorHandler);
        this.integrationContext = integrationContext;
        setClassLoader(classLoader);
        setAttribute(WebApplicationConstants.BUNDLE_CONTEXT_ATTRIBUTE, integrationContext.getBundle().getBundleContext());
        this.modulePath = modulePath;
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
            try {
                super.doStart();
            } finally {
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
        try {
            try {
                super.doScope(target, baseRequest, request, response);
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, baseRequest, newContext);
            }
        } finally {
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
        if (modulePath != null) {
            uriInContext = modulePath + uriInContext;
        }
        return lookupResource(uriInContext);
    }
    
    private Resource lookupResource(String uriInContext) {
        Bundle bundle = integrationContext.getBundle();
        URL url = BundleUtils.getEntry(bundle, uriInContext);
        if (url == null) {
            return null;
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
}
