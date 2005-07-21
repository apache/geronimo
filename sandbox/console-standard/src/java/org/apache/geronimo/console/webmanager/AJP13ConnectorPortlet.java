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

package org.apache.geronimo.console.webmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class AJP13ConnectorPortlet extends GenericPortlet {

    private static final ObjectName QUERY = JMXUtil
            .getObjectName("*:type=WebConnector,protocol=AJP13,*");

    private Kernel kernel;

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

        Set connectors = kernel.listGBeans(QUERY);
        ArrayList beans = new ArrayList(connectors.size());
        for (Iterator i = connectors.iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
            ConnectorInfo info = new ConnectorInfo();
            info.setObjectName(name);
            try {
                info.setState(((Integer) kernel.getAttribute(name, "state"))
                        .intValue());
                info.setPort(((Integer) kernel.getAttribute(name, "port"))
                        .intValue());
            } catch (Exception e) {
                throw new PortletException(e);
            }
            beans.add(info);
        }
        renderRequest.setAttribute("connectors", beans);

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
        kernel = KernelRegistry.getSingleKernel();
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/ajp13/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/ajp13/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/ajp13/help.jsp");
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        kernel = null;
        super.destroy();
    }

}
