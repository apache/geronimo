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


package org.apache.geronimo.tomcat.security.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SavedRequest;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.coyote.ActionCode;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.res.StringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class FormAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(FormAuthenticator.class);

    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);

    private final LoginService loginService;
    private final UserIdentity unauthenticatedIdentity;
    private final String loginPage;
    private final String erroryPage;

    public FormAuthenticator(LoginService loginService, UserIdentity unauthenticatedIdentity, String loginPage, String erroryPage) {
        this.loginService = loginService;
        this.unauthenticatedIdentity = unauthenticatedIdentity;
        this.loginPage = loginPage;
        this.erroryPage = erroryPage;
    }

    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory, UserIdentity cachedIdentity) throws ServerAuthException {
        try {
            Session session = request.getSessionInternal(isAuthMandatory);
            if (session == null) {
                //default identity??
                return new AuthResult(TomcatAuthStatus.SUCCESS, null, false);
            }
            if (matchRequest(request, session)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Restore request from session '" + session.getIdInternal() + "'");
                }
                if (!restoreRequest(request, session)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Proceed to restored request");
                    }
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return new AuthResult(TomcatAuthStatus.SEND_FAILURE, null, false);
                }
            }
            if (cachedIdentity != null) {
                return new AuthResult(TomcatAuthStatus.SUCCESS, cachedIdentity, true);
            }

            //we have not yet completed authentication.
            // Acquire references to objects we will need to evaluate
            MessageBytes uriMB = MessageBytes.newInstance();
            CharChunk uriCC = uriMB.getCharChunk();
            uriCC.setLimit(-1);
            String contextPath = request.getContextPath();
            String requestURI = request.getDecodedRequestURI();

            // Is this the action request from the login page?
            boolean loginAction =
                    requestURI.startsWith(contextPath) &&
                            requestURI.endsWith(Constants.FORM_ACTION);

            // No -- Save this request and redirect to the form login page
            if (!loginAction) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Save request in session '" + session.getIdInternal() + "'");
                }
                if (!isAuthMandatory) {
                    return new AuthResult(TomcatAuthStatus.SUCCESS, null, false);
                }
                try {
                    saveRequest(request, session);
                } catch (IOException ioe) {
                    logger.debug("Request body too big to save during authentication");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            sm.getString("authenticator.requestBodyTooBig"));
                    return new AuthResult(TomcatAuthStatus.SEND_FAILURE, null, false);
                }
                forwardToLoginPage(request, response);
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, unauthenticatedIdentity, false);
            }

            // Yes -- Validate the specified credentials and redirect
            // to the error page if they are not correct
//            if (characterEncoding != null) {
//                request.setCharacterEncoding(characterEncoding);
//            }
            String username = request.getParameter(Constants.FORM_USERNAME);
            String password = request.getParameter(Constants.FORM_PASSWORD);
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticating username '" + username + "'");
            }
            UserIdentity userIdentity = loginService.login(username, password);
            if (userIdentity == null) {
                forwardToErrorPage(request, response);
                //TODO right status?
                return new AuthResult(TomcatAuthStatus.SEND_FAILURE, unauthenticatedIdentity, false);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Authentication of '" + username + "' was successful");
            }

            session = request.getSessionInternal(false);
            if (session == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("User took so long to log on the session expired");
                }
                response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                        sm.getString("authenticator.sessionExpired"));
                return new AuthResult(TomcatAuthStatus.SEND_FAILURE, unauthenticatedIdentity, false);
            }

            // Redirect the user to the original request URI (which will cause
            // the original request to be restored)
            requestURI = savedRequestURL(session);
            if (logger.isDebugEnabled()) {
                logger.debug("Redirecting to original '" + requestURI + "'");
            }
            if (requestURI == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        sm.getString("authenticator.formlogin"));
                return new AuthResult(TomcatAuthStatus.SEND_FAILURE, null, false);
            } else {
                response.sendRedirect(response.encodeRedirectURL(requestURI));
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, userIdentity, true);
            }
        } catch (IOException e) {
            throw new ServerAuthException(e);
        }

    }

    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        return true;
    }

    public String getAuthType() {
        return HttpServletRequest.FORM_AUTH;
    }

    /**
     * Called to forward to the login page
     *
     * @param request  Request we are processing
     * @param response Response we are creating
     */
    protected void forwardToLoginPage(Request request, HttpServletResponse response) {
        RequestDispatcher disp = request.getRequestDispatcher(loginPage);
        try {
            disableClientCache(response);
            disp.forward(request.getRequest(), response);
            response.flushBuffer();
        } catch (Throwable t) {
            logger.warn("Unexpected error forwarding to login page", t);
        }
    }


    /**
     * Called to forward to the error page
     *
     * @param request  Request we are processing
     * @param response Response we are creating
     */
    protected void forwardToErrorPage(Request request, HttpServletResponse response) {
        RequestDispatcher disp = request.getRequestDispatcher(erroryPage);
        try {
            disableClientCache(response);
            disp.forward(request.getRequest(), response);
            response.flushBuffer();
        } catch (Throwable t) {
            logger.warn("Unexpected error forwarding to error page", t);
        }
    }


    /**
     * Does this request match the saved one (so that it must be the redirect
     * we signalled after successful authentication?
     *
     * @param request The request to be verified
     * @param session
     */
    protected boolean matchRequest(Request request, Session session) {

        // Is there a saved request?
        SavedRequest sreq = (SavedRequest)
                session.getNote(Constants.FORM_REQUEST_NOTE);
        if (sreq == null)
            return (false);

        // Does the request URI match?
        String requestURI = request.getRequestURI();
        if (requestURI == null)
            return false;
        return requestURI.equals(sreq.getRequestURI());

    }


    /**
     * Restore the original request from information stored in our session.
     * If the original request is no longer present (because the session
     * timed out), return <code>false</code>; otherwise, return
     * <code>true</code>.
     *
     * @param request The request to be restored
     * @param session The session containing the saved information
     */
    protected boolean restoreRequest(Request request, Session session)
            throws IOException {

        // Retrieve and remove the SavedRequest object from our session
        SavedRequest saved = (SavedRequest)
                session.getNote(Constants.FORM_REQUEST_NOTE);
        session.removeNote(Constants.FORM_REQUEST_NOTE);
        //session.removeNote(Constants.FORM_PRINCIPAL_NOTE);
        if (saved == null)
            return (false);

        // Modify our current request to reflect the original one
        request.clearCookies();
        Iterator<Cookie> cookies = saved.getCookies();
        while (cookies.hasNext()) {
            request.addCookie(cookies.next());
        }

        MimeHeaders rmh = request.getCoyoteRequest().getMimeHeaders();
        rmh.recycle();
        boolean cachable = "GET".equalsIgnoreCase(saved.getMethod()) ||
                "HEAD".equalsIgnoreCase(saved.getMethod());
        Iterator<String> names = saved.getHeaderNames();
        while (names.hasNext()) {
            String name = names.next();
            // The browser isn't expecting this conditional response now.
            // Assuming that it can quietly recover from an unexpected 412.
            // BZ 43687
            if (!("If-Modified-Since".equalsIgnoreCase(name) ||
                    (cachable && "If-None-Match".equalsIgnoreCase(name)))) {
                Iterator<String> values = saved.getHeaderValues(name);
                while (values.hasNext()) {
                    rmh.addValue(name).setString(values.next());
                }
            }
        }

        request.clearLocales();
        Iterator<Locale> locales = saved.getLocales();
        while (locales.hasNext()) {
            request.addLocale(locales.next());
        }

        request.getCoyoteRequest().getParameters().recycle();

        if ("POST".equalsIgnoreCase(saved.getMethod())) {
            ByteChunk body = saved.getBody();

            if (body != null) {
                request.getCoyoteRequest().action
                        (ActionCode.REQ_SET_BODY_REPLAY, body);

                // Set content type
                MessageBytes contentType = MessageBytes.newInstance();

                //If no content type specified, use default for POST
                String savedContentType = saved.getContentType();
                if (savedContentType == null) {
                    savedContentType = "application/x-www-form-urlencoded";
                }

                contentType.setString(savedContentType);
                request.getCoyoteRequest().setContentType(contentType);
            }
        }
        request.getCoyoteRequest().method().setString(saved.getMethod());

        request.getCoyoteRequest().queryString().setString
                (saved.getQueryString());

        request.getCoyoteRequest().requestURI().setString
                (saved.getRequestURI());
        disableClientCache(request.getResponse().getResponse());
        return (true);

    }


    /**
     * Save the original request information into our session.
     *
     * @param request The request to be saved
     * @param session The session to contain the saved information
     * @throws IOException
     */
    protected void saveRequest(Request request, Session session)
            throws IOException {

        // Create and populate a SavedRequest object for this request
        SavedRequest saved = new SavedRequest();
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                saved.addCookie(cookies[i]);
        }
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                saved.addHeader(name, value);
            }
        }
        Enumeration<Locale> locales = request.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();
            saved.addLocale(locale);
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            ByteChunk body = new ByteChunk();
            body.setLimit(request.getConnector().getMaxSavePostSize());

            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream is = request.getInputStream();

            while ((bytesRead = is.read(buffer)) >= 0) {
                body.append(buffer, 0, bytesRead);
            }
            saved.setContentType(request.getContentType());
            saved.setBody(body);
        }

        saved.setMethod(request.getMethod());
        saved.setQueryString(request.getQueryString());
        saved.setRequestURI(request.getRequestURI());

        // Stash the SavedRequest in our session for later use
        session.setNote(Constants.FORM_REQUEST_NOTE, saved);

    }


    /**
     * Return the request URI (with the corresponding query string, if any)
     * from the saved request so that we can redirect to it.
     *
     * @param session Our current session
     */
    protected String savedRequestURL(Session session) {

        SavedRequest saved =
                (SavedRequest) session.getNote(Constants.FORM_REQUEST_NOTE);
        if (saved == null)
            return (null);
        if (saved.getQueryString() != null) {
            StringBuilder sb = new StringBuilder(saved.getRequestURI());
            sb.append('?');
            sb.append(saved.getQueryString());
            return sb.toString();
        }
        return saved.getRequestURI();

    }

    private void disableClientCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "No-cache,no-store");
        response.setDateHeader("Expires", 1);
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        UserIdentity userIdentity = loginService.login(username, password);
        if (userIdentity != null) {
            return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, true);
        }
        return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
    }

    @Override
    public void logout(Request request) {
    }

}
