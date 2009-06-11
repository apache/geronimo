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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.gbean.ContextForward;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Servlet that forwards GET and POST requests to a servlet in an alternate
 * context. The servlet path and alternate context are defined in GBeans of
 * type ContextForward, and this one servlet handles the forwarding for all
 * those different paths.
 *
 * NOTE: This does not work for DWR, because it changes the request path info
 * while forwarding, and DWR requires the exact initial request info in order
 * to construct URLs in the data that it returns.  It should work to forward
 * to most typical servlets, JSPs, and static content.
 */
public class GenericForwardServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(GenericForwardServlet.class);
    private Map forwards = new HashMap(); // Maps a prefix String to ForwardData
    private Kernel kernel;
    private LifecycleListener listener;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        kernel = KernelRegistry.getSingleKernel();
        AbstractNameQuery query = new AbstractNameQuery(ContextForward.class.getName());
        Set set = kernel.listGBeans(query);
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            addGBean(name);
        }
        kernel.getLifecycleMonitor().addLifecycleListener(listener = new LifecycleAdapter() {
            public void running(AbstractName abstractName) {
                addGBean(abstractName);
            }

            public void stopping(AbstractName abstractName) {
                removeGBean(abstractName);
            }

            public void stopped(AbstractName abstractName) {
                removeGBean(abstractName);
            }

            public void failed(AbstractName abstractName) {
                removeGBean(abstractName);
            }

            public void unloaded(AbstractName abstractName) {
                removeGBean(abstractName);
            }
        }, query);
    }

    public void destroy() {
        if(listener != null) {
            kernel.getLifecycleMonitor().removeLifecycleListener(listener);
            listener = null;
        }
    }

    private void addGBean(AbstractName name) {
        ContextForward forward = (ContextForward) kernel.getProxyManager().createProxy(name, ContextForward.class);
        forwards.put(forward.getPortalPathPrefix(), new ForwardData(forward.getPortletContextPath(), 
                                                                    forward.getPortletServletPath(), 
                                                                    name));
    }

    private void removeGBean(AbstractName name) {
        for (Iterator it = forwards.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            ForwardData data = (ForwardData) entry.getValue();
            if(data.getGbean().equals(name)) {
                it.remove();
                break;
            }
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if(path == null) {
            log.error("Unable to forward request; no path information provided.  Path is used to identify where to forward to.");
            throw new ServletException("Unable to forward request");
        }
        ForwardData forward = null;
        for (Iterator it = forwards.keySet().iterator(); it.hasNext();) {
            String prefix = (String) it.next();
            if(path.startsWith(prefix)) {
                forward = (ForwardData) forwards.get(prefix);
                path = path.substring(prefix.length());
                break;
            }
        }
        if(forward == null) {
            log.error("Unable to forward URL "+path+"; does not match any known ContextForward definitions.");
            throw new ServletException("Unable to forward request");
        }
        if(!path.equals("") && !path.startsWith("/")) path = "/"+path;
        String queryString = req.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }
        path = forward.getServletPath()+path;
        ServletContext ctx = forward.getForwardContext(getServletContext());
        if (ctx == null) {
            log.error("Unable to forward URL " + path + ". Context not found: " + forward.getContextPath());
            throw new ServletException("Unable to forward request");
        }
        RequestDispatcher dispatcher = ctx.getRequestDispatcher(path);
        dispatcher.forward(req, resp);
    }

    private static class ForwardData {
        private String contextPath;
        private String servletPath;
        private AbstractName gbean;

        public ForwardData(String contextPath, String servletPath, AbstractName gbean) {
            this.contextPath = contextPath;
            this.servletPath = servletPath;
            this.gbean = gbean;
        }

        public ServletContext getForwardContext(ServletContext ctx) {
            return ctx.getContext(contextPath);
        }
        
        public String getContextPath() {
            return contextPath;
        }

        public String getServletPath() {
            return servletPath;
        }

        public AbstractName getGbean() {
            return gbean;
        }
    }
}
