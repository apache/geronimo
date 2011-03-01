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

package org.apache.geronimo.console.infomanager;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.JVM;
import org.apache.geronimo.system.serverinfo.ServerConstants;

/**
 * Calculates various information about the server to display in the server
 * info portlet view (on of several JSPs depending on the portlet state).
 *
 * @version $Rev$ $Date$
 */
public class ServerInfoPortlet extends BasePortlet {
    private static final String NORMALVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoNormal.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoMaximized.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoHelp.jsp";

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        Map svrProps = new HashMap();
        Map jvmProps = new HashMap();

        JVM jvm = PortletManager.getCurrentJVM(renderRequest);

        Date bootDate = jvm.getKernelBootTime();
        svrProps.put("Kernel Boot Time", bootDate);
        svrProps.put("Geronimo Version", ServerConstants.getVersion());
        svrProps.put("Build", ServerConstants.getBuildDate() + "-" + ServerConstants.getBuildTime());
        svrProps.put("os.name", System.getProperty("os.name"));
        svrProps.put("os.version", System.getProperty("os.version"));
        svrProps.put("sun.os.patch.level", System.getProperty("sun.os.patch.level"));
        svrProps.put("os.arch", System.getProperty("os.arch"));
        svrProps.put("os.locale", System.getProperty("user.language") + "_" + System.getProperty("user.country"));
        renderRequest.setAttribute("svrProps", svrProps);

        jvmProps.put("Java Version", jvm.getJavaVersion());
        jvmProps.put("Java Vendor", jvm.getJavaVendor());
        jvmProps.put("Node", jvm.getNode());
        jvmProps.put("Available Processors", Integer.valueOf(jvm.getAvailableProcessors()));
        renderRequest.setAttribute("jvmProps", jvmProps);

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }
}
