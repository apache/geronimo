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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that forwards GET and POST requests to a servlet
 * in an alternate context. The servlet path and alternate
 * context are passed in as configuration parameters (e.g.
 * via <config-param> elements in the web.xml).
 */
public class ContextForwardServlet extends HttpServlet {

    // name of the configuration parameter containing the context path
    public static final String CONTEXT_PATH = "context-path";
    // name of the configuration parameter containing the servlet path
    public static final String SERVLET_PATH = "servlet-path";

    private String servletPath;
    private String contextPath;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        contextPath = config.getInitParameter(CONTEXT_PATH);
        servletPath = config.getInitParameter(SERVLET_PATH);
        if (contextPath == null || servletPath == null) {
            throw new UnavailableException("context-path and servlet-path " +
                    "must be provided as configuration parameters");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dispatchURI = servletPath + (req.getPathInfo() == null ? "" : req.getPathInfo());
        String queryString = req.getQueryString();
        if (queryString != null) {
            dispatchURI += "?" + queryString;
        }
        ServletContext forwardContext = getServletContext().getContext(contextPath);
        RequestDispatcher dispatcher = forwardContext.getRequestDispatcher(dispatchURI);    
        dispatcher.forward(req, resp);
    }
}
