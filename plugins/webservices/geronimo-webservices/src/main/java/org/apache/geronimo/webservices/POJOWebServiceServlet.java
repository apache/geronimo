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
package org.apache.geronimo.webservices;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.rpc.server.ServiceLifecycle;

/**
 * Delegates requests to a WebServiceContainer which is presumably for a POJO WebService
 * Nothing stopping us from using this for EJBs or other types of webservices other than
 * it is more than we need.  EJB webservices use the JettyEJBWebServiceContext.
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
public class POJOWebServiceServlet implements Servlet {
    public static final String POJO_CLASS = POJOWebServiceServlet.class.getName()+"@pojoClassName";
    private Servlet stack;

    public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();

        String pojoClassID = config.getInitParameter(POJO_CLASS);
        Class<?> pojoClass = (Class<?>) context.getAttribute(pojoClassID);

        Object pojo;
        try {
            pojo = pojoClass.newInstance();
        } catch (Exception e) {
            throw new ServletException("Unable to instantiate POJO WebService class: " + pojoClass.getName(), e);
        }

        stack = new WebServiceContainerInvoker(pojo);
        if (pojo instanceof ServiceLifecycle) {
            stack = new ServiceLifecycleManager(stack,(ServiceLifecycle)pojo);
        }


        stack.init(config);
    }

    public ServletConfig getServletConfig() {
        return stack.getServletConfig();
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        stack.service(servletRequest, servletResponse);
    }

    public String getServletInfo() {
        return stack.getServletInfo();
    }

    public void destroy() {
        stack.destroy();
    }
}
