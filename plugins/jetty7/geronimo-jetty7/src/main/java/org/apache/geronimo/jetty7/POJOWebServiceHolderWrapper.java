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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;


/**
 * This is intended to hold the web service stack for an axis POJO web service.
 * It is starting life as a copy of JettyServletHolder.
 *
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.SERVLET_WEB_SERVICE_TEMPLATE)
public class POJOWebServiceHolderWrapper implements ServletNameSource, GBeanLifecycle {
    private final ServletHolder servletHolder;
    private final WebServiceContainer webServiceContainer;
    private final Set<String> servletMappings;
    private final JettyServletRegistration context;
    private final String pojoClassName;

    public POJOWebServiceHolderWrapper(@ParamAttribute(name = "pojoClassName") String pojoClassName,
                                     @ParamAttribute(name = "servletName") String servletName,
                                     @ParamAttribute(name = "initParams") Map<String, String> initParams,
                                     @ParamAttribute(name = "loadOnStartup") Integer loadOnStartup,
                                     @ParamAttribute(name = "servletMappings") Set<String> servletMappings,
                                     @ParamAttribute(name = "runAsRole") String runAsRole,
                                     @ParamReference(name = "WebServiceContainerFactory") WebServiceContainerFactory webServiceContainerFactory,
                                     @ParamReference(name = "JettyServletRegistration", namingType = NameFactory.WEB_MODULE) JettyServletRegistration context) throws Exception {
        Subject runAsSubject = context == null ? null : context.getSubjectForRole(runAsRole);
        servletHolder = new GeronimoServletHolder(context == null ? null : context.getIntegrationContext(), runAsSubject, context);
        //context will be null only for use as "default servlet info holder" in deployer.

        this.pojoClassName = pojoClassName;
        this.context = context;
        this.webServiceContainer = webServiceContainerFactory == null ? null : webServiceContainerFactory.getWebServiceContainer();
        this.servletMappings = servletMappings;
        if (context != null) {
            servletHolder.setName(servletName);
            servletHolder.setClassName(POJOWebServiceServlet.class.getName());
            servletHolder.setInitParameters(initParams);
            if (loadOnStartup != null) {
                servletHolder.setInitOrder(loadOnStartup);
            }
        }
    }

    //todo how do we stop/destroy the servlet?
    //todo is start called twice???

    public String getServletName() {
        return servletHolder.getName();
    }

    /**
     * TODO THIS IS NOT CALLED!!! only the ServletHolder is!!
     * Service a request with this servlet.  Set the ThreadLocal to hold the
     * current JettyServletHolder.
     */
    public void handle(Request baseRequest, ServletRequest request, ServletResponse response)
            throws ServletException, UnavailableException, IOException {

        //  TODO There has to be some way to get this in on the Servlet's init method.
//        request.setAttribute(POJOWebServiceServlet.WEBSERVICE_CONTAINER, webServiceContainer);

        PolicyContext.setHandlerData(Request.getRequest((HttpServletRequest) request));

        servletHolder.handle(baseRequest, request, response);
    }

    public void doStart() throws Exception {
        if (context != null) {
            Class pojoClass = context.getWebClassLoader().loadClass(pojoClassName);

            /* DMB: Hack! I really just want to override initServlet and give a reference of the WebServiceContainer to the servlet before we call init on it.
             * But this will have to do instead....
             */
            ServletContext servletContext = this.context.getServletHandler().getServletContext();

            // Make up an ID for the WebServiceContainer
            // put a reference the ID in the init-params
            // put the WebServiceContainer in the webapp context keyed by its ID
            String webServicecontainerID = getServletName() + WebServiceContainerInvoker.WEBSERVICE_CONTAINER + webServiceContainer.hashCode();
            servletHolder.setInitParameter(WebServiceContainerInvoker.WEBSERVICE_CONTAINER, webServicecontainerID);
            servletContext.setAttribute(webServicecontainerID, webServiceContainer);

            // Same for the POJO Class
            String pojoClassID = getServletName() + POJOWebServiceServlet.POJO_CLASS + pojoClass.hashCode();
            servletHolder.setInitParameter(POJOWebServiceServlet.POJO_CLASS, pojoClassID);
            servletContext.setAttribute(pojoClassID, pojoClass);

            //this now starts the servlet in the appropriate context
            //TODO check that we should not call this a servlet for jsr-77 benefit.
            context.registerServletHolder(servletHolder, getServletName(), this.servletMappings, null);
            servletHolder.start();
        }
    }

    public void doStop() throws Exception {
        servletHolder.stop();
    }

    public void doFail() {
        try {
            servletHolder.stop();
        } catch (Exception e) {
            //ignore ??
        }
    }

}
