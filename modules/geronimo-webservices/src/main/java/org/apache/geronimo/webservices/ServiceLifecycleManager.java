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
package org.apache.geronimo.webservices;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.MessageContext;

/**
 * @version $Rev$ $Date$
 */
public class ServiceLifecycleManager implements Servlet {

    private final ServiceLifecycle managedService;
    private final Servlet next;

    public ServiceLifecycleManager(Servlet next, ServiceLifecycle managedService) {
        this.next = next;
        this.managedService = managedService;
    }

    public void init(ServletConfig config) throws ServletException {
        next.init(config);
        try {
            managedService.init(new InstanceContext(config.getServletContext()));
        } catch (ServiceException e) {
            throw new ServletException("Unable to initialize ServiceEndpoint", e);
        }
    }

    public ServletConfig getServletConfig() {
        return next.getServletConfig();
    }

    public String getServletInfo() {
        return next.getServletInfo();
    }

    public void destroy() {
        managedService.destroy();
        next.destroy();
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ServletEndpointContext context = getContext();
        try {
            endpointContext.set(new InvocationContext((HttpServletRequest) req));
            next.service(req, res);
        } finally {
            endpointContext.set(context);
        }
    }

    private static final DefaultContext DEFAULT_CONTEXT = new DefaultContext();

    private static final ThreadLocal endpointContext = new ThreadLocal();


    private static ServletEndpointContext getContext() {
        ServletEndpointContext context = (ServletEndpointContext) endpointContext.get();
        return context != null ? context : DEFAULT_CONTEXT;
    }

    static class InstanceContext implements ServletEndpointContext {
        private final ServletContext servletContext;

        public InstanceContext(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public MessageContext getMessageContext() {
            return getContext().getMessageContext();
        }

        public Principal getUserPrincipal() {
            return getContext().getUserPrincipal();
        }

        public HttpSession getHttpSession() {
            return getContext().getHttpSession();
        }

        public ServletContext getServletContext() {
            return servletContext;
        }

        public boolean isUserInRole(String s) {
            return getContext().isUserInRole(s);
        }
    }

    static class InvocationContext implements ServletEndpointContext {

        private final HttpServletRequest request;

        public InvocationContext(HttpServletRequest request) {
            this.request = request;
        }

        public MessageContext getMessageContext() {
            return (MessageContext) request.getAttribute(WebServiceContainer.MESSAGE_CONTEXT);
        }

        public Principal getUserPrincipal() {
            return request.getUserPrincipal();
        }

        public HttpSession getHttpSession() {
            return request.getSession();
        }

        public ServletContext getServletContext() {
            throw new IllegalAccessError("InstanceContext should never delegate this method.");
        }

        public boolean isUserInRole(String s) {
            return request.isUserInRole(s);
        }
    }

    static class DefaultContext implements ServletEndpointContext {

        public MessageContext getMessageContext() {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }

        public Principal getUserPrincipal() {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }

        public HttpSession getHttpSession() {
            throw new javax.xml.rpc.JAXRPCException("Method cannot be called outside an http request context");
        }

        public ServletContext getServletContext() {
            throw new IllegalAccessError("InstanceContext should never delegate this method.");
        }

        public boolean isUserInRole(String s) {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }
    }
}
