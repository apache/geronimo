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

package org.apache.geronimo.tomcat.cluster.wadi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;
import org.apache.geronimo.tomcat.cluster.AbstractClusteredValve;
import org.codehaus.wadi.core.contextualiser.InvocationException;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.web.impl.WebInvocation;


/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusteredValve extends AbstractClusteredValve {
    private final Manager wadiManager;

    public WADIClusteredValve(Manager wadiManager) {
        this.wadiManager = wadiManager;
    }
    
    protected ClusteredInvocation newClusteredInvocation(Request request, Response response) {
        return new WADIWebClusteredInvocation(request, response);
    }
    
    protected class WADIWebClusteredInvocation extends WebClusteredInvocation {
        
        public WADIWebClusteredInvocation(Request request, Response response) {
            super(request, response);
        }

        public void invoke() throws ClusteredInvocationException {
            WebInvocation invocation = new WebInvocation(5000);
            FilterChain chainAdapter = new FilterChain() {
                public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    try {
                        invokeLocally();
                    } catch (ClusteredInvocationException e) {
                        throw (IOException) new IOException().initCause(e);
                    }
                }
            };
            invocation.init(null == request? NoOpHttpServletRequest.SINGLETON: request, response, chainAdapter);
            try {
                wadiManager.contextualise(invocation);
            } catch (InvocationException e) {
                Throwable throwable = e.getCause();
                if (throwable instanceof IOException) {
                    throw new ClusteredInvocationException(throwable);
                } else if (throwable instanceof ServletException) {
                    throw new ClusteredInvocationException(throwable);
                } else {
                    throw new ClusteredInvocationException(e);
                }
            }
        }
    }

    protected static class NoOpHttpServletRequest implements HttpServletRequest {
        public static final NoOpHttpServletRequest SINGLETON = new NoOpHttpServletRequest();

        public String getAuthType() {
            throw new UnsupportedOperationException();
        }

        public String getContextPath() {
            throw new UnsupportedOperationException();
        }

        public Cookie[] getCookies() {
            throw new UnsupportedOperationException();
        }

        public long getDateHeader(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getHeader(String arg0) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        public Enumeration getHeaders(String arg0) {
            throw new UnsupportedOperationException();
        }

        public int getIntHeader(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getMethod() {
            throw new UnsupportedOperationException();
        }

        public String getPathInfo() {
            throw new UnsupportedOperationException();
        }

        public String getPathTranslated() {
            throw new UnsupportedOperationException();
        }

        public String getQueryString() {
            throw new UnsupportedOperationException();
        }

        public String getRemoteUser() {
            throw new UnsupportedOperationException();
        }

        public String getRequestURI() {
            throw new UnsupportedOperationException();
        }

        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException();
        }

        public String getRequestedSessionId() {
            return null;
        }

        public String getServletPath() {
            throw new UnsupportedOperationException();
        }

        public HttpSession getSession() {
            throw new UnsupportedOperationException();
        }

        public HttpSession getSession(boolean arg0) {
            throw new UnsupportedOperationException();
        }

        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException();
        }

        public boolean isUserInRole(String arg0) {
            throw new UnsupportedOperationException();
        }

        public Object getAttribute(String arg0) {
            throw new UnsupportedOperationException();
        }

        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        public int getContentLength() {
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getLocalAddr() {
            throw new UnsupportedOperationException();
        }

        public String getLocalName() {
            throw new UnsupportedOperationException();
        }

        public int getLocalPort() {
            throw new UnsupportedOperationException();
        }

        /**
         * Get the servlet context the request-response pair was last dispatched through.
         *
         * @return the latest ServletContext on the dispatch chain.
         * @since 3.0
         */
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets the associated servlet response.
         *
         * @return the ServletResponse associated with this request.
         * @since 3.0
         */
        public ServletResponse getServletResponse() {
            throw new UnsupportedOperationException();
        }

        /**
         * complete a suspended request.
         *
         * @throws IllegalStateException
         * @since 3.0
         */
        public void complete() throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        /**
         * Suspend request processing.  Must be called by a thread that is processing this request.
         *
         * @param timeoutMilliseconds new timeout period, in milliseconds
         * @throws IllegalStateException if called by a thread not processing this request or after error dispatch
         * @see #complete
         * @see #resume
         * @since 3.0
         */
        public void suspend(long timeoutMilliseconds) throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        /**
         * Similar to suspend(timeoutMilliseconds) but with a container supplied timeout period.
         *
         * @throws IllegalStateException
         * @see #complete
         * @see #resume
         * @since 3.0
         */
        public void suspend() throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        /**
         * Resume a suspended request
         *
         * @throws IllegalStateException if the request is not suspended
         * @see #suspend
         * @since 3.0
         */
        public void resume() throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        /**
         * @return if the request is suspended
         * @since 3.0
         */
        public boolean isSuspended() {
            throw new UnsupportedOperationException();
        }

        /**
         * @return if the request is resumed
         * @since 3.0
         */
        public boolean isResumed() {
            throw new UnsupportedOperationException();
        }

        /**
         * @return if the request is timed out
         * @since 3.0
         */
        public boolean isTimeout() {
            throw new UnsupportedOperationException();
        }

        /**
         * @return if the request has never been suspended (or resumed)
         * @since 3.0
         */
        public boolean isInitial() {
            throw new UnsupportedOperationException();
        }

        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        public Enumeration getLocales() {
            throw new UnsupportedOperationException();
        }

        public String getParameter(String arg0) {
            throw new UnsupportedOperationException();
        }

        public Map getParameterMap() {
            throw new UnsupportedOperationException();
        }

        public Enumeration getParameterNames() {
            throw new UnsupportedOperationException();
        }

        public String[] getParameterValues(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getRealPath(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getRemoteAddr() {
            throw new UnsupportedOperationException();
        }

        public String getRemoteHost() {
            throw new UnsupportedOperationException();
        }

        public int getRemotePort() {
            throw new UnsupportedOperationException();
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            throw new UnsupportedOperationException();
        }

        public String getScheme() {
            throw new UnsupportedOperationException();
        }

        public String getServerName() {
            throw new UnsupportedOperationException();
        }

        public int getServerPort() {
            throw new UnsupportedOperationException();
        }

        public boolean isSecure() {
            throw new UnsupportedOperationException();
        }

        public void removeAttribute(String arg0) {
            throw new UnsupportedOperationException();
        }

        public void setAttribute(String arg0, Object arg1) {
            throw new UnsupportedOperationException();
        }

        public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
