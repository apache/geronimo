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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.jacc.PolicyContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.ServletContext;

import org.apache.axis.description.TypeDesc;
import org.apache.geronimo.axis.server.TypeDescInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.StoredObject;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpRequest;


/**
 * This is intended to hold the web service stack for an axis POJO web service.
 * It is starting life as a copy of JettyServletHolder.
 *
 * @version $Rev: 154436 $ $Date: 2005-02-19 10:22:02 -0800 (Sat, 19 Feb 2005) $
 */
public class JettyPOJOWebServiceHolder extends ServletHolder implements GBeanLifecycle {
    private StoredObject storedWebServiceContainer;
    private StoredObject storedClassToTypeDescInfo;
    private Set servletMappings;
    private Map webRoleRefPermissions;
    private JettyServletRegistration context;
    private String pojoClassName;

    //todo consider interface instead of this constructor for endpoint use.
    public JettyPOJOWebServiceHolder() {

    }

    public JettyPOJOWebServiceHolder(String pojoClassName,
                                     String servletName,
                                     Map initParams,
                                     Integer loadOnStartup,
                                     Set servletMappings,
                                     Map webRoleRefPermissions,
                                     StoredObject storedClassToTypeDescInfo,
                                     StoredObject storedWebServiceContainer,
                                     JettyServletRegistration context) throws Exception {
        super(context == null ? null : context.getServletHandler(), servletName, POJOWebServiceServlet.class.getName(), null);
        //context will be null only for use as "default servlet info holder" in deployer.

        this.pojoClassName = pojoClassName;
        this.context = context;
        this.storedWebServiceContainer = storedWebServiceContainer;
        if (context != null) {
            putAll(initParams);
            if (loadOnStartup != null) {
                setInitOrder(loadOnStartup.intValue());
            }
            this.servletMappings = servletMappings;
            this.webRoleRefPermissions = webRoleRefPermissions == null ? Collections.EMPTY_MAP : webRoleRefPermissions;
        }

        this.storedClassToTypeDescInfo = storedClassToTypeDescInfo;
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

        //  TODO There has to be some way to get this in on the Servlet's init method.
//        request.setAttribute(POJOWebServiceServlet.WEBSERVICE_CONTAINER, webServiceContainer);

        JettyServletHolder.currentServletHolder.set(this);
        PolicyContext.setHandlerData(ServletHttpRequest.unwrap(request));

        super.handle(request, response);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JettyPOJOWebServiceHolder.class, NameFactory.SERVLET_WEB_SERVICE_TEMPLATE);
        //todo replace with interface
        infoBuilder.addInterface(ServletHolder.class);

        infoBuilder.addAttribute("pojoClassName", String.class, true);
        infoBuilder.addAttribute("servletName", String.class, true);
        infoBuilder.addAttribute("initParams", Map.class, true);
        infoBuilder.addAttribute("loadOnStartup", Integer.class, true);
        infoBuilder.addAttribute("servletMappings", Set.class, true);
        infoBuilder.addAttribute("webRoleRefPermissions", Map.class, true);
        infoBuilder.addAttribute("classToTypeDescInfo", StoredObject.class, true);
        infoBuilder.addAttribute("webServiceContainer", StoredObject.class, true);
        infoBuilder.addReference("JettyServletRegistration", JettyServletRegistration.class);

        infoBuilder.setConstructor(new String[]{"pojoClassName",
                                                "servletName",
                                                "initParams",
                                                "loadOnStartup",
                                                "servletMappings",
                                                "webRoleRefPermissions",
                                                "classToTypeDescInfo",
                                                "webServiceContainer",
                                                "JettyServletRegistration"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void doStart() throws Exception {
        if (context != null) {
            Map classToTypeDesc = (Map) storedClassToTypeDescInfo.getObject(context.getWebClassLoader());
            for (Iterator iter = classToTypeDesc.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Class clazz = (Class) entry.getKey();
                TypeDescInfo typeDescInfo = (TypeDescInfo) entry.getValue();
                TypeDesc.registerTypeDescForClass(clazz, typeDescInfo.buildTypeDesc());
            }
            
            Class pojoClass = context.getWebClassLoader().loadClass(pojoClassName);
            WebServiceContainer webServiceContainer = (WebServiceContainer) storedWebServiceContainer.getObject(context.getWebClassLoader());

            /* DMB: Hack! I really just want to override initServlet and give a reference of the WebServiceContainer to the servlet before we call init on it.
             * But this will have to do instead....
             */
            ServletContext servletContext = this.context.getServletHandler().getServletContext();

            // Make up an ID for the WebServiceContainer
            // put a reference the ID in the init-params
            // put the WebServiceContainer in the webapp context keyed by its ID
            String webServicecontainerID = getServletName() + WebServiceContainerInvoker.WEBSERVICE_CONTAINER + webServiceContainer.hashCode();
            put(WebServiceContainerInvoker.WEBSERVICE_CONTAINER, webServicecontainerID);
            servletContext.setAttribute(webServicecontainerID, webServiceContainer);

            // Same for the POJO Class
            String pojoClassID = getServletName() + POJOWebServiceServlet.POJO_CLASS + pojoClass.hashCode();
            put(POJOWebServiceServlet.POJO_CLASS, pojoClassID);
            servletContext.setAttribute(pojoClassID, pojoClass);

            //this now starts the servlet in the appropriate context
            context.registerServletHolder(this, getServletName(), this.servletMappings, this.webRoleRefPermissions);
//            start();
        }
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }


}
