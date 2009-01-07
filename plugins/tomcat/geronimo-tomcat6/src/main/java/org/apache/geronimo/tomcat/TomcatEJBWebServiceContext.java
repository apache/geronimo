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
package org.apache.geronimo.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.authenticator.SSLAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.tomcat.realm.TomcatEJBWSGeronimoRealm;
import org.apache.geronimo.webservices.WebServiceContainer;

public class TomcatEJBWebServiceContext extends StandardContext{

    private static final Logger log = LoggerFactory.getLogger(TomcatEJBWebServiceContext.class);

    private final String contextPath;
    private final WebServiceContainer webServiceContainer;
    private final boolean isSecureTransportGuarantee;
    private final ClassLoader classLoader;
    private final Set<String> secureMethods;

    public TomcatEJBWebServiceContext(String contextPath, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, String[] protectedMethods, ClassLoader classLoader) {
        this.contextPath = contextPath;
        this.webServiceContainer = webServiceContainer;
        this.secureMethods = initSecureMethods(protectedMethods);
        this.setPath(contextPath);
        this.setDocBase("");
        this.setParentClassLoader(classLoader);
        this.setDelegate(true);

        log.debug("EJB Webservice Context = " + contextPath);
        if (securityRealmName != null) {

            TomcatEJBWSGeronimoRealm realm = new TomcatEJBWSGeronimoRealm();
            realm.setAppName(securityRealmName);
            realm.setUserClassNames("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
            realm.setRoleClassNames("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
            setRealm(realm);
            this.realm = realm;

            if ("NONE".equals(transportGuarantee)) {
                isSecureTransportGuarantee = false;
            } else if ("INTEGRAL".equals(transportGuarantee) ||
                       "CONFIDENTIAL".equals(transportGuarantee)) {
                isSecureTransportGuarantee = true;
            } else {
                throw new IllegalArgumentException("Invalid transport-guarantee: " + transportGuarantee);
            }

            if ("NONE".equals(authMethod) ||
                "BASIC".equals(authMethod) ||
                "DIGEST".equals(authMethod) ||
                "CLIENT-CERT".equals(authMethod)) {

                //Setup a login configuration
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(authMethod);
                loginConfig.setRealmName(realmName);
                this.setLoginConfig(loginConfig);

                //Setup a default Security Constraint
                SecurityCollection collection = new SecurityCollection();
                if (secureMethods == null) {
                    // protect all
                    collection.addMethod("GET");
                    collection.addMethod("POST");
                    collection.addMethod("PUT");
                    collection.addMethod("DELETE");
                    collection.addMethod("HEAD");
                    collection.addMethod("OPTIONS");
                    collection.addMethod("TRACE");
                    collection.addMethod("CONNECT");
                } else {
                    // protect specified
                    for (String method : secureMethods) {
                        collection.addMethod(method);
                    }
                }
                collection.addPattern("/*");
                collection.setName("default");
                SecurityConstraint sc = new SecurityConstraint();
                sc.addAuthRole("*");
                sc.addCollection(collection);
                sc.setAuthConstraint(true);
                sc.setUserConstraint(transportGuarantee);
                this.addConstraint(sc);
                this.addSecurityRole("default");

                //Set the proper authenticator
                if ("BASIC".equals(authMethod) ){
                    this.addValve(new BasicAuthenticator());
                } else if ("DIGEST".equals(authMethod) ){
                    this.addValve(new DigestAuthenticator());
                } else if ("CLIENT-CERT".equals(authMethod) ){
                    this.addValve(new SSLAuthenticator());
                } else if ("NONE".equals(authMethod)) {
                    this.addValve(new NonLoginAuthenticator());
                }

            } else {
                throw new IllegalArgumentException("Invalid authMethod: " + authMethod);
            }
        } else {
            isSecureTransportGuarantee = false;
        }
        
        this.classLoader = classLoader;
        this.addValve(new EJBWebServiceValve());
        
        //Create a dummy wrapper
        Wrapper wrapper = this.createWrapper();
        String name = System.currentTimeMillis() + "";
        wrapper.setName(name);
        this.addChild(wrapper);
        this.addServletMapping("/*", name);

    }
    
    private Set<String> initSecureMethods(String[] protectedMethods) {
        if (protectedMethods == null) {
            return null;
        }
        Set<String> methods = null;
        for (String method : protectedMethods) {
            if (method == null) {
                continue;
            }
            method = method.trim();
            if (method.length() == 0) {
                continue;
            }
            method = method.toUpperCase();
            
            if (methods == null) {
                methods = new HashSet<String>();
            }
            methods.add(method);
        }
        return methods;
    }
    
    public class EJBWebServiceValve extends ValveBase {

        public void invoke(Request req, Response res) throws IOException, ServletException {
            Thread currentThread = Thread.currentThread();
            ClassLoader oldClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(classLoader);
            try {
                handle(req, res);                
            } finally {
                currentThread.setContextClassLoader(oldClassLoader);
            }
        }
        
        private void handle(Request req, Response res) throws IOException, ServletException {
            res.setContentType("text/xml");            
            RequestAdapter request = new RequestAdapter(req);
            ResponseAdapter response = new ResponseAdapter(res);

            request.setAttribute(WebServiceContainer.SERVLET_REQUEST, (HttpServletRequest)req);
            request.setAttribute(WebServiceContainer.SERVLET_RESPONSE, (HttpServletResponse)res);
            // TODO: add support for context
            request.setAttribute(WebServiceContainer.SERVLET_CONTEXT, null);

            req.finishRequest();
            
            if (secureMethods == null || secureMethods.contains(req.getMethod())) {
                if (isSecureTransportGuarantee && !req.isSecure()) {
                    res.sendError(403);
                    return;
                }
            }
            if (isWSDLRequest(req)) {
                try {
                    webServiceContainer.getWsdl(request, response);
                    //WHO IS RESPONSIBLE FOR CLOSING OUT?
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    res.sendError(500,"Could not fetch wsdl!");
                    return;
                }
            } else {
                try {
                    webServiceContainer.invoke(request, response);
                    req.finishRequest();
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    res.sendError(500, "Could not process message!");
                }
            }
        }

        private boolean isWSDLRequest(Request req) {
            return ("GET".equals(req.getMethod()) && (req.getParameter("wsdl") != null || req.getParameter("xsd") != null));            
        }
        
    }

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
                    //String uriString = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + request.getRequestURI();
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
            return request.getContextPath();
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
            return response.getStream();
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
