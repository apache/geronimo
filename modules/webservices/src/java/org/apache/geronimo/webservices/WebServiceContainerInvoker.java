/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version $Rev$ $Date$
 */
public class WebServiceContainerInvoker implements Servlet {

    public static final String WEBSERVICE_CONTAINER = WebServiceContainerInvoker.class.getName()+"@WebServiceContainer";

    private final Object pojo;
    private WebServiceContainer service;

    public WebServiceContainerInvoker(Object pojo) {
        this.pojo = pojo;
    }

    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        String webServiceContainerID = config.getInitParameter(WEBSERVICE_CONTAINER);
        service = (WebServiceContainer) context.getAttribute(webServiceContainerID);
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.setContentType("text/xml");
        RequestAdapter request = new RequestAdapter((HttpServletRequest) req);
        ResponseAdapter response = new ResponseAdapter((HttpServletResponse) res);

        // This is the guy the WebServiceContainer should invoke
        req.setAttribute(WebServiceContainer.POJO_INSTANCE, pojo);

        if (request.getParameter("wsdl") != null || request.getParameter("WSDL") != null) {
            try {
                service.getWsdl(request, response);
            } catch (IOException e) {
                throw e;
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Could not fetch wsdl!", e);
            }
        } else {
            try {
                service.invoke(request, response);
            } catch (IOException e) {
                throw e;
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Could not process message!", e);
            }
        }
    }

    public String getServletInfo() {
        return null;
    }

    public void destroy() {
    }

    private static class RequestAdapter implements WebServiceContainer.Request {
        private final HttpServletRequest request;

        public RequestAdapter(HttpServletRequest request) {
            this.request = request;
        }

        public String getHeader(String name) {
            return request.getHeader(name);
        }

        public java.net.URI getURI() {
            try {
                String uriString = request.getRequestURI();
                return new java.net.URI(uriString);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e.getMessage());
            }
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

        public Object getAttribute(String s) {
            return request.getAttribute(s);
        }

        public void setAttribute(String s, Object o) {
            request.setAttribute(s, o);
        }

    }

    private static class ResponseAdapter implements WebServiceContainer.Response {
        private final HttpServletResponse response;

        public ResponseAdapter(HttpServletResponse response) {
            this.response = response;
        }

        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public String getHeader(String name) {
            throw new UnsupportedOperationException("Not possible to implement");
        }

        public OutputStream getOutputStream() {
            try {
                return response.getOutputStream();
            } catch (IOException e) {
                throw (IllegalStateException) new IllegalStateException().initCause(e);
            }
        }

        public void setStatusCode(int code) {
            response.setStatus(code);
        }

        public int getStatusCode() {
            throw new UnsupportedOperationException("Not possible to implement");
        }

        public void setContentType(String type) {
            response.setContentType(type);
        }

        public String getContentType() {
            return response.getContentType();
        }

        public void setStatusMessage(String responseString) {
            response.setStatus(getStatusCode(), responseString);
        }
    }
}
