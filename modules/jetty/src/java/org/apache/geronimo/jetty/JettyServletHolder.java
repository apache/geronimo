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
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.security.jacc.PolicyContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.Servlet;

import org.mortbay.jetty.servlet.ServletHolder;


/**
 * This ServletHolder's sole purpose is to provide the thread's current
 * ServletHolder for realms that are interested in the current servlet, e.g.
 * current servlet name.
 *
 * It is also being our servlet gbean for now.  We could gbean-ize the superclass to avoid the thread local access.
 *
 * @version $Rev$ $Date$
 * @see org.apache.geronimo.jetty.JAASJettyRealm#isUserInRole(java.security.Principal, java.lang.String)
 */
public class JettyServletHolder extends ServletHolder implements Servlet {
    private static final ThreadLocal currentServletName = new ThreadLocal();
    private final String objectName;

    //todo consider interface instead of this constructor for endpoint use.
    public JettyServletHolder() {
        this.objectName = null;
    }

    public JettyServletHolder(String objectName,
                              String servletName,
                              String servletClassName,
                              String jspFile,
                              Map initParams,
                              Integer loadOnStartup,
                              Set servletMappings,
                              Map webRoleRefPermissions,
                              String runAsRole,
                              ServletHolder previous,  //dependency for startup ordering
                              JettyServletRegistration context) throws Exception {
        super(context == null? null: context.getServletHandler(), servletName, servletClassName, jspFile);
        //context will be null only for use as "default servlet info holder" in deployer.

        if (context != null) {
            putAll(initParams);
            if (loadOnStartup != null) {
                //This has no effect on the actual start order, the gbean references "previous" control that.
                setInitOrder(loadOnStartup.intValue());
            }
            //this now starts the servlet in the appropriate context
            context.registerServletHolder(this, servletName, servletMappings, webRoleRefPermissions == null? Collections.EMPTY_MAP: webRoleRefPermissions);
        }
        setRunAs(runAsRole);
        this.objectName = objectName;
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

        setCurrentServletName(getServletName());

        super.handle(request, response);
    }

    /**
     * Provide the thread's current JettyServletHolder
     *
     * @return the thread's current JettyServletHolder
     * @see org.apache.geronimo.jetty.JAASJettyRealm#isUserInRole(java.security.Principal, java.lang.String)
     */
    static String getCurrentServletName() {
        return (String) currentServletName.get();
    }

    static void setCurrentServletName(String servletName) {
        currentServletName.set(servletName);
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JettyServletHolder.class, NameFactory.DEFAULT_SERVLET);
        //todo replace with interface
        infoBuilder.addInterface(ServletHolder.class);

        infoBuilder.addAttribute("servletName", String.class, true);
        infoBuilder.addAttribute("servletClass", String.class, true);
        infoBuilder.addAttribute("jspFile", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);
        infoBuilder.addAttribute("loadOnStartup", Integer.class, true);
        infoBuilder.addAttribute("servletMappings", Set.class, true);
        infoBuilder.addAttribute("webRoleRefPermissions", Map.class, true);
        infoBuilder.addAttribute("runAsRole", String.class, true);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addInterface(Servlet.class);

        infoBuilder.addReference("Previous", ServletHolder.class, NameFactory.SERVLET);
        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[] {"objectName",
                                                 "servletName",
                                                 "servletClass",
                                                 "jspFile",
                                                 "initParams",
                                                 "loadOnStartup",
                                                 "servletMappings",
                                                 "webRoleRefPermissions",
                                                 "runAsRole",
                                                 "Previous",
                                                 "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
