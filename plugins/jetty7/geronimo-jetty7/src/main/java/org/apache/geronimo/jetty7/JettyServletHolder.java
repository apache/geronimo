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
package org.apache.geronimo.jetty7;

import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.net.URL;

import javax.security.auth.Subject;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.Servlet;
import org.eclipse.jetty.servlet.ServletHolder;


/**
 * This ServletHolder's sole purpose is to provide the thread's current
 * ServletHolder for realms that are interested in the current servlet, e.g.
 * current servlet name.
 * <p/>
 * It is also being our servlet gbean for now.  We could gbean-ize the superclass to avoid the thread local access.
 *
 * @version $Rev$ $Date$
 */
public class JettyServletHolder implements ServletNameSource, Servlet, GBeanLifecycle {


    private final JettyServletRegistration servletRegistration;
    private final ServletHolder servletHolder;
    private final String objectName;

    //todo consider interface instead of this constructor for endpoint use.
    public JettyServletHolder() {
        servletRegistration = null;
        servletHolder = null;
        objectName = null;
    }

    public JettyServletHolder(String objectName,
            String servletName,
            String servletClassName,
            String jspFile,
            Map initParams,
            Integer loadOnStartup,
            Set<String> servletMappings,
            String runAsRole,
            JettyServletRegistration context) throws Exception {
        servletRegistration = context;
        Subject runAsSubject = context == null? null: context.getSubjectForRole(runAsRole);
        servletHolder = new InternalJettyServletHolder(context == null? null: context.getLifecycleChain(), runAsSubject, servletRegistration);
        servletHolder.setName(servletName);
        servletHolder.setClassName(servletClassName);
        //context will be null only for use as "default servlet info holder" in deployer.

        if (context != null) {
            servletHolder.setInitParameters(initParams);
            servletHolder.setForcedPath(jspFile);
            if (loadOnStartup != null) {
                //This has no effect on the actual start order, the gbean references "previous" control that.
                servletHolder.setInitOrder(loadOnStartup);
            }
            //this now starts the servlet in the appropriate context
            context.registerServletHolder(servletHolder, servletName, servletMappings, objectName);
        }
        this.objectName = objectName;
    }

    public String getServletName() {
        return servletHolder.getName();
    }

    public String getServletClassName() {
        return servletHolder.getClassName();
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

    public void doStart() throws Exception {
        //start actually handled in constructor
//        servletHolder.start();
    }

    public void doStop() throws Exception {
        servletHolder.stop();
        if (servletRegistration != null) {
            servletRegistration.unregisterServletHolder(servletHolder, servletHolder.getName(), null, objectName);
        }
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //?? ignore
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyServletHolder.class, NameFactory.SERVLET);
        //todo replace with interface
//        infoBuilder.addInterface(ServletHolder.class);

        infoBuilder.addAttribute("servletName", String.class, true);
        infoBuilder.addAttribute("servletClass", String.class, true);
        infoBuilder.addAttribute("jspFile", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);
        infoBuilder.addAttribute("loadOnStartup", Integer.class, true);
        infoBuilder.addAttribute("servletMappings", Set.class, true);
        infoBuilder.addAttribute("runAsRole", String.class, true);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addInterface(Servlet.class);

        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class, NameFactory.WEB_MODULE);

        infoBuilder.setConstructor(new String[]{"objectName",
                "servletName",
                "servletClass",
                "jspFile",
                "initParams",
                "loadOnStartup",
                "servletMappings",
                "runAsRole",
                "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
