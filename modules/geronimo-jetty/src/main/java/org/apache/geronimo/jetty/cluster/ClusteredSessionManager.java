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
package org.apache.geronimo.jetty.cluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.mortbay.jetty.servlet.AbstractSessionManager;


/**
 *
 * @version $Rev$ $Date$
 */
public class ClusteredSessionManager extends AbstractSessionManager {
    private static final Object ALL_SESSION_PLACEHOLDER = new Object();
    
    private final SessionManager sessionManager;
    
    public ClusteredSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        String workerName = sessionManager.getNode().getName();
        workerName = workerName.replaceAll(" ", "");
        setWorkerName(workerName);
        // implementation note: enables cross context session id such that a mock HttpServletRequest having a defined 
        // requestedSessionId attribute can be used to re-create an HttpSession with a defined session ID in this
        // manager.
        setCrossContextSessionIDs(true);
        
        sessionManager.registerListener(new MigrationListener());

        // sessions are not removed by this manager. They are invalidated via a callback mechanism
        setMaxInactiveInterval(-1);
    }

    protected Session newSession(HttpServletRequest request) {
        return new ClusteredSession(request);
    }

    private class MigrationListener implements SessionListener {
        
        public void notifyInboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            String sessionId = session.getSessionId();
            synchronized (__allSessions) {
                if (__allSessions.containsKey(sessionId)) {
                    throw new IllegalStateException("ID [" + sessionId + "] is already defined.");
                }
                __allSessions.add(sessionId, ALL_SESSION_PLACEHOLDER);
                newHttpSession(new RequestWithBoundSession(session));
                __allSessions.removeValue(sessionId, ALL_SESSION_PLACEHOLDER);
            }
        }
        
        public void notifyOutboundSessionMigration(org.apache.geronimo.clustering.Session session) {
            String sessionId = session.getSessionId();
            synchronized (__allSessions) {
                __allSessions.remove(sessionId);
            }

            synchronized (_sessions) {
                _sessions.remove(sessionId);
            } 
        }
    }

    protected class ClusteredSession extends Session {
        private static final String FORCE_SET_VALUES = "$$$JETTY_FORCE_SET_VALUES$$$"; 

        private final org.apache.geronimo.clustering.Session session;

        protected ClusteredSession(HttpServletRequest request) {
            super(request);
            
            if (request instanceof RequestWithBoundSession) {
                this.session = ((RequestWithBoundSession) request).session;
                // implementation note: set a dummy attribute such that the underlying attribute map is initialized
                // with the state of the inbound session.
                setAttribute(FORCE_SET_VALUES, FORCE_SET_VALUES);
            } else {
                try {
                    this.session = sessionManager.createSession(getId());
                } catch (SessionAlreadyExistException e) {
                    throw (IllegalStateException) new IllegalStateException().initCause(e);
                }
            }
        }
        
        protected Map newAttributeMap() {
            return session.getState();
        }
    }

    /**
     * Implementation note: this is a mock HttpServletRequest which is used to create an HttpSession with the same
     * session ID than the wrapped Session.
     */
    private class RequestWithBoundSession implements HttpServletRequest {
        private final org.apache.geronimo.clustering.Session session;

        public RequestWithBoundSession(org.apache.geronimo.clustering.Session session) {
            this.session = session;
        }

        public void setAttribute(String arg0, Object arg1) {
        }

        public Object getAttribute(String arg0) {
            return null;
        }

        public String getRequestedSessionId() {
            return session.getSessionId();
        }

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

        public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException();
        }
    }
}
