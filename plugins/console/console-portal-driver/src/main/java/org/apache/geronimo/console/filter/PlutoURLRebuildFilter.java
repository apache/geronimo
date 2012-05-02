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
package org.apache.geronimo.console.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

public class PlutoURLRebuildFilter implements Filter {

    private static final String HIDDEN_URL_ELEMENT_NAME = "hiddenurl";

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {

        /*
         *  If the client's browser is not IE, we will not do the wrapper operation. 
         * */
        if (!(request instanceof HttpServletRequest) || !isIE((HttpServletRequest) request)) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        HttpServletRequest wrappedHttpServletRequest = httpServletRequest;
        HttpSession httpSession = httpServletRequest.getSession();
        String actionParameters = null;
        if (httpSession != null) {
            actionParameters = "formId=" + (String) httpSession.getAttribute("formId");
        }
        HttpServletResponse wrappedHttpServletResponse = new PlutoUrlResponse((HttpServletResponse) response,
                httpServletRequest.getContextPath() + httpServletRequest.getServletPath(), actionParameters);
        /*
         * 1. if it is file uploading, skip it, we must not invoke any method on it, or it will corrupt the request
         * object. Maybe, in the future, we could handler file uploading uniformly here         
         */
        if (!isMultipartContent(httpServletRequest)) {
            String sHiddenUrl = httpServletRequest.getParameter(HIDDEN_URL_ELEMENT_NAME);
            if (sHiddenUrl != null)
                wrappedHttpServletRequest = new PlutoUrlRequest(httpServletRequest);
        }
        filterChain.doFilter(wrappedHttpServletRequest, wrappedHttpServletResponse);
    }

    /**
     * Detect whether the client's browser is IE
     * 
     * @param request
     * @return
     */
    private boolean isIE(HttpServletRequest request) {
        String sUserAgent = request.getHeader("user-agent");
        return sUserAgent != null && sUserAgent.indexOf("MSIE") != -1;
    }

    /**
     * Detect whether the user is uploading a file
     * 
     * @param request
     * @return
     */
    private boolean isMultipartContent(HttpServletRequest request) {
        return request.getMethod().toUpperCase().equals("POST") && request.getContentType() != null
                && request.getContentType().toUpperCase().indexOf("MULTIPART/FORM-DATA") == 0;
    }

    public void init(FilterConfig arg0) throws ServletException {

    }

    protected static class PlutoUrlResponse extends HttpServletResponseWrapper {

        private String requestContextServletPath;
        
        private String actionParameters;

        public PlutoUrlResponse(HttpServletResponse response, String requestContextServletPath, String actionParameters) {
            super(response);
            this.actionParameters = actionParameters;
            this.requestContextServletPath = requestContextServletPath;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            // For IE could not handler the url of the length more than 2048 
            // Currently we only handler the location is begin with '/', which means that it should be an absolute path
            // For the location does not contain the http://server:port/, we use 1900 instead of 2048
            
        	location = location.replaceAll("%7C", "|");
        	
            if (location.charAt(0) == '/' && location.length() > 1900) {
        		
        		if (location.endsWith("?"))
        			location = location.substring(0, location.length()-1);
        		
                PrintWriter writer = getWriter();
                writer
                        .write("<html><head></head><body onload='document.hform.submit()'><form name='hform' method='POST' action='");
                writer.write(requestContextServletPath);
                if (actionParameters != null) {
                    writer.write("?" + actionParameters);
                }
                writer.write("'><input type='hidden' name='" + HIDDEN_URL_ELEMENT_NAME + "' value='" + location
                        + "'/></form>");
                writer.write("</body></html>");
                setContentType("text/html;charset=UTF-8");
            } else
                super.sendRedirect(location);
        }
    }

    protected static class PlutoUrlRequest extends HttpServletRequestWrapper {

        private String sNewPathInfo;

        private String sNewPathTranslated;

        private String sNewRequestURI;

        private String sFullRealPath;

        public PlutoUrlRequest(HttpServletRequest request) {
            super(request);
            String sContextServletPath = request.getContextPath() + request.getServletPath();
            HttpSession httpSession = request.getSession();
            String sHiddenUrl = request.getParameter(HIDDEN_URL_ELEMENT_NAME);
            sNewPathInfo = sHiddenUrl.substring(sContextServletPath.length());
            sNewPathTranslated = httpSession.getServletContext().getRealPath(sHiddenUrl);
            sNewRequestURI = sHiddenUrl;
            sFullRealPath = request.getRequestURL().append(sNewPathInfo).toString();
        }

        @Override
        public String getPathInfo() {
            return sNewPathInfo;
        }

        @Override
        public StringBuffer getRequestURL() {
            if (sFullRealPath == null)
                return super.getRequestURL();
            return new StringBuffer(sFullRealPath);
        }

        @Override
        public String getPathTranslated() {
            return sNewPathTranslated;
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRequestURI() {
            return sNewRequestURI;
        }
    }
}
