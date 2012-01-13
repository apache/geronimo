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

package org.apache.geronimo.console.servermanager;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class ServerManagerPortlet extends BasePortlet {

    private static final Logger log = LoggerFactory.getLogger(ServerManagerPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher shutdownView;

    private PortletRequestDispatcher helpView;

    private Kernel kernel;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        Bundle framework = ((BundleReference)getClass().getClassLoader()).getBundle().getBundleContext().getBundle(0);
        if (actionRequest.getParameter("reboot") != null) {
            log.info("Reboot initiated by user request: " + actionRequest.getUserPrincipal().getName());
            try {
                framework.update();
            } catch (BundleException e) {
                log.info("Problem rebooting", e);
            }
//            new Thread() {
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                    }
//                    kernel.shutdown();
//                    Daemon.main(new String[0]);
//                }
//            }.start();
        } else if(actionRequest.getParameter("shutdown") != null) {
            log.info("Shutting down by user request: " + actionRequest.getUserPrincipal().getName());
//            kernel.shutdown();
//            System.exit(0);
            try {
                framework.stop();
            } catch (BundleException e) {
                log.info("Problem rebooting", e);
            }
        }
    }

    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        addWarningMessage(request, getLocalizedString(request, "consolebase.warnMsg07"));
        normalView.include(request, response);
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
