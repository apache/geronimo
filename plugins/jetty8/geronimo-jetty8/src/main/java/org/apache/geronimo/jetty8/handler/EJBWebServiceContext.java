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
package org.apache.geronimo.jetty8.handler;

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
import org.apache.geronimo.jetty8.security.SecurityHandlerFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.security.SecurityHandler;

/**
 * Specialization of ContextHandler that just has a security and servlet handler.
 * @version $Rev$ $Date$
 */
public class EJBWebServiceContext extends ServletContextHandler {

    public EJBWebServiceContext(String contextPath, SecurityHandler securityHandler, ServletHandler servletHandler, ClassLoader classLoader) {
        super(null, contextPath, null, securityHandler, servletHandler, null);
        this.setContextPath(contextPath);
        setClassLoader(classLoader);
        this.setAllowNullPathInfo(true);
    }

}
