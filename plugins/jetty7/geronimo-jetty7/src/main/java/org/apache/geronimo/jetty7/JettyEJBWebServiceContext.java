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
package org.apache.geronimo.jetty7;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.webservices.WebServiceContainer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.http.HttpException;

/**
 * Delegates requests to a WebServiceContainer which is presumably for an EJB WebService.
 * <p/>
 * WebServiceContainer delegates to an EJBContainer that will ultimately provide the JNDI,
 * TX, and Security services for this web service.
 * <p/>
 * Nothing stopping us from using this for POJOs or other types of webservices if shared
 * Context (JNDI, tx, security) wasn't required to be supplied by the web context.
 * <p/>
 * From a 10,000 foot view the Jetty architecture has:
 * Container -> Context -> Holder -> Servlet
 * <p/>
 * A Container has multiple Contexts, typically webapps
 * A Context provides the JNDI, TX, and Security for the webapp and has many Holders
 * A Holder simply wraps each Servlet
 * <p/>
 * The POJO Web Service architecture on Jetty looks like this:
 * Container -> WebApp Context -> JettyPOJOWebServiceHolder -> POJOWebServiceServlet
 * <p/>
 * The EJB Web Service architecure, on the other hand, creates one Context for each EJB:
 * Container -> JettyEJBWebServiceContext
 *
 * @version $Rev$ $Date$
 */
public class JettyEJBWebServiceContext extends ContextHandler {

    private final String contextPath;
    private final WebServiceContainer webServiceContainer;
//    private final Authenticator authenticator;
//    private final JAASJettyRealm realm;
    private final boolean isConfidentialTransportGuarantee;
    private final boolean isIntegralTransportGuarantee;
    private final ClassLoader classLoader;


    public JettyEJBWebServiceContext(String contextPath, WebServiceContainer webServiceContainer, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) {
        this.contextPath = contextPath;
        this.webServiceContainer = webServiceContainer;
        this.setContextPath(contextPath);
        this.setAllowNullPathInfo(true);
        
//        if (internalJAASJettyRealm != null) {
        if (realmName != null) {
//            realm = new JAASJettyRealm(realmName, internalJAASJettyRealm);
            //TODO
            //not used???
            //setUserRealm(realm);
//            this.realm = realm;
            if ("NONE".equals(transportGuarantee)) {
                isConfidentialTransportGuarantee = false;
                isIntegralTransportGuarantee = false;
            } else if ("INTEGRAL".equals(transportGuarantee)) {
                isConfidentialTransportGuarantee = false;
                isIntegralTransportGuarantee = true;
            } else if ("CONFIDENTIAL".equals(transportGuarantee)) {
                isConfidentialTransportGuarantee = true;
                isIntegralTransportGuarantee = false;
            } else {
                throw new IllegalArgumentException("Invalid transport-guarantee: " + transportGuarantee);
            }
//            if ("BASIC".equals(authMethod)) {
//                authenticator = new BasicAuthenticator();
//            } else if ("DIGEST".equals(authMethod)) {
//                authenticator = new DigestAuthenticator();
//            } else if ("CLIENT-CERT".equals(authMethod)) {
//                authenticator = new ClientCertAuthenticator();
//            } else if ("NONE".equals(authMethod)) {
//                authenticator = null;
//            } else {
//                throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
//            }
        } else {
//            realm = null;
//            authenticator = null;
            isConfidentialTransportGuarantee = false;
            isIntegralTransportGuarantee = false;
        }
        this.classLoader = classLoader;
    }

    public String getName() {
        //need a better name
        return contextPath;
    }

    public void doHandle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException
    {
        //TODO
        //do we need to check that this request should be handled by this handler?
//        if (! target.startsWith(contextPath)) {
//            return;
//        }
//        Request jettyRequest = (Request) req;
        Response jettyResponse = (Response) res;
        res.setContentType("text/xml");
        RequestAdapter request = new RequestAdapter(baseRequest);
        ResponseAdapter response = new ResponseAdapter(jettyResponse);

        request.setAttribute(WebServiceContainer.SERVLET_REQUEST, req);
        request.setAttribute(WebServiceContainer.SERVLET_RESPONSE, res);
        // TODO: add support for context
        request.setAttribute(WebServiceContainer.SERVLET_CONTEXT, null);

        if (req.getParameter("wsdl") != null) {
            try {
                webServiceContainer.getWsdl(request, response);
                baseRequest.setHandled(true);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (HttpException) new HttpException(500, "Could not fetch wsdl!").initCause(e);
            }
        } else {
            if (isConfidentialTransportGuarantee) {
                if (!req.isSecure()) {
                    throw new HttpException(403, null);
                }
            } else if (isIntegralTransportGuarantee) {
                if (!baseRequest.getConnection().isIntegral(baseRequest)) {
                    throw new HttpException(403, null);
                }
            }
            Thread currentThread = Thread.currentThread();
            ClassLoader oldClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(classLoader);
            //hard to imagine this could be anything but null, but....
//            Subject oldSubject = ContextManager.getCurrentCaller();
            try {
//                if (authenticator != null) {
//                    String pathInContext = org.mortbay.util.URIUtil.canonicalPath(req.getContextPath());
//                    if (authenticator.authenticate(realm, pathInContext, jettyRequest, jettyResponse) == null) {
//                        throw new HttpException(403, null);
//                    }
//                } else {
//                    //EJB will figure out correct defaultSubject shortly
//                    //TODO consider replacing the GenericEJBContainer.DefaultSubjectInterceptor with this line
//                    //setting the defaultSubject.
//                    ContextManager.popCallers(null);
//                }
                try {
                    webServiceContainer.invoke(request, response);
                    baseRequest.setHandled(true);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw (HttpException) new HttpException(500, "Could not process message!").initCause(e);
                }
            } finally {
//                ContextManager.setCurrentCaller(oldSubject);
                currentThread.setContextClassLoader(oldClassLoader);
            }
        }

    }

    public String getContextPath() {
        return contextPath;
    }

//    public String getSecurityRealmName() {
//        if (realm == null) {
//            return null;
//        } else {
//            return realm.getSecurityRealmName();
//        }
//    }

    public static class RequestAdapter implements WebServiceContainer.Request {
        private final Request request;
        private URI uri;

        public RequestAdapter(Request request) {
            this.request = request;
        }

        public String getHeader(String name) {
            return request.getHeader(name);
        }

        public java.net.URI getURI() {
            if (uri == null) {
                try {
                    //String uriString = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
                    //return new java.net.URI(uri.getScheme(),uri.getHost(),uri.getPath(),uri.);
                    uri = new java.net.URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString(), null);
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            return uri;
        }

        public int getContentLength() {
            return request.getContentLength();
        }

        public String getContentType() {
            return request.getContentType();
        }

        public InputStream getInputStream() throws IOException {
            return request.getInputStream();
        }

        public int getMethod() {
            Integer method = (Integer) methods.get(request.getMethod());
            return method == null ? UNSUPPORTED : method.intValue();
        }

        public String getParameter(String name) {
            return request.getParameter(name);
        }

        public Map getParameters() {
            return request.getParameterMap();
        }

        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        public void setAttribute(String name, Object value) {
            request.setAttribute(name, value);
        }

        public String getRemoteAddr() {
            return request.getRemoteAddr();
        }

        public String getContextPath() {
            //request.getContextPath() isn't working correctly and returned null.  
            //use getRequestURI() for now before it is fixed.
            //return request.getContextPath();
            return request.getRequestURI();
        }

        private static final Map methods = new HashMap();

        static {
            methods.put("OPTIONS", new Integer(OPTIONS));
            methods.put("GET", new Integer(GET));
            methods.put("HEAD", new Integer(HEAD));
            methods.put("POST", new Integer(POST));
            methods.put("PUT", new Integer(PUT));
            methods.put("DELETE", new Integer(DELETE));
            methods.put("TRACE", new Integer(TRACE));
            methods.put("CONNECT", new Integer(CONNECT));
        }

    }

    public static class ResponseAdapter implements WebServiceContainer.Response {
        private final Response response;

        public ResponseAdapter(Response response) {
            this.response = response;
        }

        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public String getHeader(String name) {
            return response.getHeader(name);
        }

        public OutputStream getOutputStream() {
            try {
                return response.getOutputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public void setStatusCode(int code) {
            response.setStatus(code);
        }

        public int getStatusCode() {
            return response.getStatus();
         }

        public void setContentType(String type) {
            response.setContentType(type);
        }

        public String getContentType() {
            return response.getContentType();
        }

        public void setStatusMessage(String responseString) {
            response.setStatus(response.getStatus(), responseString);
        }

        public void flushBuffer() throws java.io.IOException{
            response.flushBuffer();
        }
    }
    
}
