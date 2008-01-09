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

package org.apache.geronimo.console.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Filter that wrappers HTTP requests forwarded from a separate
 * context via a named request dispatcher.  The wrapped HTTP
 * requests return attributes from the original request instead
 * of the forwarded instance of the request. Deployment
 * descriptors that use this filter should specify "FORWARD" as
 * the dispatcher type in their filter-mapping elements, as per
 * the 2.4 Servlet Specification. e.g.
 * 
 *     <pre>
 *     <filter-mapping>
 *        <filter-name>myfilter</filter-name>
 *        <servlet-name>myservlet</servlet-name>
 *        <dispatcher>FORWARD</dispatcher>
 *     </filter-mapping>
 *     </pre>
 *     
 */
public class ForwardDispatchFilter implements Filter {

    protected FilterConfig filterConfig;
    
    public void init(FilterConfig config) throws ServletException {
        filterConfig = config;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            chain.doFilter(new ForwardRequest((HttpServletRequest)request), response);
        } else {
            throw new ServletException("ServletRequest is not an instance of HttpServletRequest");
        }
    }

    public void destroy() {}
    
    /* An HTTP servlet request wrapper that maps the following
     * attributes from the original request
     *  
     * # javax.servlet.forward.request_uri
     * # javax.servlet.forward.context_path
     * # javax.servlet.forward.servlet_path
     * # javax.servlet.forward.path_info
     * # javax.servlet.forward.query_string
     */
    protected static class ForwardRequest extends HttpServletRequestWrapper {
        private final HttpServletRequest request;
        public ForwardRequest(HttpServletRequest req) {
            super(req);
            request = req;
        }
        public String getRequestURI() {
            return String.valueOf(request.getAttribute("javax.servlet.forward.request_uri")); 
        }
        public String getContextPath() {
            return String.valueOf(request.getAttribute("javax.servlet.forward.context_path")); 
        }
        public String getServletPath() {
            return String.valueOf(request.getAttribute("javax.servlet.forward.servlet_path")); 
        }
        public String getPathInfo() {
            return String.valueOf(request.getAttribute("javax.servlet.forward.path_info")); 
        }
        public String getQueryString() {
            return String.valueOf(request.getAttribute("javax.servlet.forward.query_string")); 
        }
    }
}
