/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
import java.io.OutputStream;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.webservices.WebServiceInvoker;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * @version $Rev:  $ $Date:  $
 */
public class JettyWebServiceHandler extends HttpContext implements HttpHandler, GBeanLifecycle {

    private final String contextPath;
    private final WebServiceInvoker webServiceInvoker;
    private final JettyContainer jettyContainer;

    private HttpContext httpContext;

    public JettyWebServiceHandler(String contextPath, WebServiceInvoker webServiceInvoker, JettyContainer jettyContainer) {
        this.contextPath = contextPath;
        this.webServiceInvoker = webServiceInvoker;
        this.jettyContainer = jettyContainer;
    }

    public String getName() {
        //need a better name
        return contextPath;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    public void initialize(HttpContext httpContext) {
        this.httpContext = httpContext;
    }

    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        response.setContentType("text/xml");

        if (request.getParameter("wsdl") != null) {
            OutputStream out = response.getOutputStream();
            try {
                webServiceInvoker.getWsdl(null, out);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (HttpException) new HttpException(500).initCause(e);
            }
            //WHO IS RESPONSIBLE FOR CLOSING OUT?
        } else {
            try {
                webServiceInvoker.invoke(request.getInputStream(), response.getOutputStream());
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw (HttpException) new HttpException(500).initCause(e);
            }
        }

    }

    public String getContextPath() {
        return contextPath;
    }

    public void doStart() throws Exception {
        jettyContainer.addContext(this);
    }

    public void doStop() throws Exception {
        jettyContainer.removeContext(this);
    }

    public void doFail() {
        jettyContainer.removeContext(this);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JettyWebServiceHandler.class, NameFactory.WEB_MODULE);
        infoBuilder.addAttribute("contextPath", String.class, true);
        infoBuilder.addReference("WebServiceInvoker", WebServiceInvoker.class);
        infoBuilder.addReference("JettyContainer", JettyContainer.class);

        infoBuilder.setConstructor(new String[] {"contextPath", "WebServiceInvoker", "JettyContainer"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
