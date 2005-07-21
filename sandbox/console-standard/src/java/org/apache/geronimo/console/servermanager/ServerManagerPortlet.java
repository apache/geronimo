/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.servermanager;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.system.main.Daemon;

public class ServerManagerPortlet extends GenericPortlet {

    private static final Log log = LogFactory.getLog("ServerManager");

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher shutdownView;

    private PortletRequestDispatcher helpView;

    private Kernel kernel;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        if (actionRequest.getParameter("reboot") != null) {
            log.info("Reboot initiated by user request: "
                    + actionRequest.getUserPrincipal());
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    kernel.shutdown();
                    Daemon.main(new String[0]);
                }
            }.start();
        }
    }

    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        if (request.getParameter("shutdown") != null) {
            log.info("Shutting down by user request: "
                    + request.getUserPrincipal());
            shutdownView.include(request, response);
            response.flushBuffer();
            kernel.shutdown();
            System.exit(0);
        } else {
            normalView.include(request, response);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/servermanager/normal.jsp");
        shutdownView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/servermanager/shutdown.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/servermanager/help.jsp");
        kernel = KernelRegistry.getSingleKernel();
    }

    public void destroy() {
        normalView = null;
        shutdownView = null;
        helpView = null;
        super.destroy();
    }

}
