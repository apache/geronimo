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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.http.HttpException;
import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * ServletHandler that always delegates to a WebServiceContainer, presumably an EJB web service.
 *
 * @version $Rev$ $Date$
 */
public class EJBServletHandler extends ServletHandler {

    private final WebServiceContainer webServiceContainer;

    public EJBServletHandler(WebServiceContainer webServiceContainer) {
        this.webServiceContainer = webServiceContainer;
    }

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (false)
            nextScope(target,baseRequest,request,response);
        else if (_nextScope!=null)
            _nextScope.doScope(target,baseRequest,request, response);
        else if (_outerScope!=null)
            _outerScope.doHandle(target,baseRequest,request, response);
        else
            doHandle(target,baseRequest,request, response);
        // end manual inline (pathentic attempt to reduce stack depth)
    }

    @Override
    public void doHandle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        Response jettyResponse = (Response) res;
        res.setContentType("text/xml");
        RequestAdapter request = new RequestAdapter(baseRequest);
        ResponseAdapter response = new ResponseAdapter(jettyResponse);

        request.setAttribute(WebServiceContainer.SERVLET_REQUEST, req);
        request.setAttribute(WebServiceContainer.SERVLET_RESPONSE, res);
        // TODO: add support for context
        request.setAttribute(WebServiceContainer.SERVLET_CONTEXT, null);

        if (isWSDLRequest(req)) {
            try {
                webServiceContainer.getWsdl(request, response);
                baseRequest.setHandled(true);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (HttpException) new HttpException(500, "Could not fetch wsdl!").initCause(e);
            }
        } else {
            try {
                webServiceContainer.invoke(request, response);
                baseRequest.setHandled(true);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (HttpException) new HttpException(500, "Could not process message!").initCause(e);
            }
        }
    }

    private boolean isWSDLRequest(HttpServletRequest req) {
        return ("GET".equals(req.getMethod()) && (req.getParameter("wsdl") != null || req.getParameter("xsd") != null));            
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
            Integer method = methods.get(request.getMethod());
            return method == null ? UNSUPPORTED : method;
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

        private static final Map<String, Integer> methods = new HashMap<String, Integer>();

        static {
            methods.put("OPTIONS", OPTIONS);
            methods.put("GET", GET);
            methods.put("HEAD", HEAD);
            methods.put("POST", POST);
            methods.put("PUT", PUT);
            methods.put("DELETE", DELETE);
            methods.put("TRACE", TRACE);
            methods.put("CONNECT", CONNECT);
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

        public void flushBuffer() throws java.io.IOException {
            response.flushBuffer();
        }
    }
    
}
