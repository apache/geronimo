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
package org.apache.geronimo.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * Delegates requests to a WebServiceContainer which is presumably for a POJO WebService
 * Nothing stopping us from using this for EJBs or other types of webservices other than
 * it is more than we need.  EJB webservices use the JettyEJBWebServiceContext.
 *
 * From a 10,000 foot view the Jetty architecture has:
 * Container -> Context -> Holder -> Servlet
 *
 * A Container has multiple Contexts, typically webapps
 * A Context provides the JNDI, TX, and Security for the webapp and has many Holders
 * A Holder simply wraps each Servlet
 *
 * The POJO Web Service architecture on Jetty looks like this:
 * Container -> WebApp Context -> JettyPOJOWebServiceHolder -> POJOWebServiceServlet
 *
 * The EJB Web Service architecure, on the other hand, creates one Context for each EJB:
 * Container -> JettyEJBWebServiceContext
 * 
 * @version $Rev$ $Date$
 */
public class POJOWebServiceServlet implements Servlet {
    public static final String WEBSERVICE_CONTAINER = "webServiceContainer";
    public static final String WEBSERVICE_CONTAINER_BYTES = "webServiceContainerBytes";

    public void init(ServletConfig config) throws ServletException {

    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        WebServiceContainer service = (WebServiceContainer) req.getAttribute(WEBSERVICE_CONTAINER);

        res.setContentType("text/xml");
        RequestAdapter request = new RequestAdapter((HttpServletRequest)req);
        ResponseAdapter response = new ResponseAdapter((HttpServletResponse)res);

        if (request.getParameter("wsdl") != null) {
            try {
                service.getWsdl(request,response);
            } catch (IOException e) {
                throw e;
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Could not fetch wsdl!", e);
            }
        } else {            
            try {
                service.invoke(request,response);
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
            throw new java.lang.UnsupportedOperationException("Not possible to implement");
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
            throw new java.lang.UnsupportedOperationException("Not possible to implement");
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
