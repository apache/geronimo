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
import java.util.Map;

import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatEJBWebServiceContext extends StandardContext {

    private static final Logger log = LoggerFactory.getLogger(TomcatEJBWebServiceContext.class);

    private final WebServiceContainer webServiceContainer;
    private final ClassLoader classLoader;

    public TomcatEJBWebServiceContext(String contextPath, WebServiceContainer webServiceContainer, ClassLoader classLoader) {
        this.webServiceContainer = webServiceContainer;
        this.setPath(contextPath);
        this.setDocBase("");
        this.setParentClassLoader(classLoader);
        this.setDelegate(true);

        if (log.isDebugEnabled()) {
            log.debug("EJB Webservice Context = " + contextPath);
        }
        this.classLoader = classLoader;

        //Create a dummy wrapper
        Wrapper wrapper = this.createWrapper();
        String name = String.valueOf(System.currentTimeMillis());
        wrapper.setName(name);
        this.addChild(wrapper);
        this.addServletMapping("/*", name);
        setProcessTlds(false);

    }

    protected void startInternal() throws LifecycleException {
        super.startInternal();
        addValve(new EJBWebServiceValve());
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

            request.setAttribute(WebServiceContainer.SERVLET_REQUEST, req);
            request.setAttribute(WebServiceContainer.SERVLET_RESPONSE, res);
            // TODO: add support for context
            request.setAttribute(WebServiceContainer.SERVLET_CONTEXT, null);

            req.finishRequest();

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

        public URI getURI() {
            if (uri == null) {
                try {
                    //String uriString = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + request.getRequestURI();
                    //return new java.net.URI(uri.getScheme(),uri.getHost(),uri.getPath(),uri.);
                    uri = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), request.getRequestURI(), request.getQueryString(), null);
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
            Integer method = methods.get(request.getMethod());
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

        private static final Map<String, Integer> methods = new HashMap<String, Integer>();

        static {
            methods.put("OPTIONS", Integer.valueOf(OPTIONS));
            methods.put("GET", Integer.valueOf(GET));
            methods.put("HEAD", Integer.valueOf(HEAD));
            methods.put("POST", Integer.valueOf(POST));
            methods.put("PUT", Integer.valueOf(PUT));
            methods.put("DELETE", Integer.valueOf(DELETE));
            methods.put("TRACE", Integer.valueOf(TRACE));
            methods.put("CONNECT", Integer.valueOf(CONNECT));
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
