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
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.security.jacc.PolicyContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpRequest;


/**
 * This is intended to hold the web service stack for an axis POJO web service.
 * It is starting life as a copy of JettyServletHolder.
 *
 * @version $Rev: 154436 $ $Date: 2005-02-19 10:22:02 -0800 (Sat, 19 Feb 2005) $
 */
public class JettyAxisPOJOWebService extends ServletHolder {

    //todo consider interface instead of this constructor for endpoint use.
    public JettyAxisPOJOWebService() {

    }

    public JettyAxisPOJOWebService(String servletName,
                              Map initParams,
                              Integer loadOnStartup,
                              Set servletMappings,
                              Map webRoleRefPermissions,
                              JettyServletRegistration context) throws Exception {
        super(context == null? null: context.getServletHandler(), servletName, DummyServlet.class.getName(), null);
        //context will be null only for use as "default servlet info holder" in deployer.

        if (context != null) {
            putAll(initParams);
            if (loadOnStartup != null) {
                setInitOrder(loadOnStartup.intValue());
            }
            //this now starts the servlet in the appropriate context
            context.registerServletHolder(this, servletName, servletMappings, webRoleRefPermissions == null? Collections.EMPTY_MAP: webRoleRefPermissions);
//            start();
        }
    }

    //todo how do we stop/destroy the servlet?
    //todo is start called twice???

    public String getServletName() {
        return getName();
    }

    /**
     * Service a request with this servlet.  Set the ThreadLocal to hold the
     * current JettyServletHolder.
     */
    public void handle(ServletRequest request, ServletResponse response)
            throws ServletException, UnavailableException, IOException {

        JettyServletHolder.currentServletHolder.set(this);
        PolicyContext.setHandlerData(ServletHttpRequest.unwrap(request));

        super.handle(request, response);
    }

    public static class DummyServlet implements Servlet {

        public void init(ServletConfig config) throws ServletException {

        }

        public ServletConfig getServletConfig() {
            return null;
        }

        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
           //just for fun, copy input into output
            InputStream in = req.getInputStream();
            OutputStream out = res.getOutputStream();
            byte[] buf = new byte[1024];
            int i;
            while ((i = in.read(buf)) > 0) {
                out.write(buf, 0, i);
            }
        }

        public String getServletInfo() {
            return null;
        }

        public void destroy() {

        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JettyAxisPOJOWebService.class, NameFactory.SERVLET_WEB_SERVICE_TEMPLATE);
        //todo replace with interface
        infoBuilder.addInterface(ServletHolder.class);
        
        infoBuilder.addAttribute("servletName", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);
        infoBuilder.addAttribute("loadOnStartup", Integer.class, true);
        infoBuilder.addAttribute("servletMappings", Set.class, true);
        infoBuilder.addAttribute("webRoleRefPermissions", Map.class, true);
        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[] {"servletName",
                                                 "initParams",
                                                 "loadOnStartup", 
                                                 "servletMappings",
                                                 "webRoleRefPermissions",
                                                 "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
