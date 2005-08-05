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

import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class HttpsConnectorPortlet extends GenericPortlet {

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private ObjectName connector = JMXUtil
            .getObjectName(ObjectNameConstants.JETTY_HTTPS_CONNECTOR_NAME);

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String id = actionRequest.getParameter("gbeanId");
            ObjectName con = JMXUtil.getObjectName(id);
            kernel.setAttribute(con, "port", Integer.valueOf(actionRequest
                    .getParameter("port")));
            kernel.setAttribute(con, "needClientAuth", Boolean
                    .valueOf(actionRequest.getParameter("needClientAuth")));
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        try {
            String mode = renderRequest.getParameter("mode");
            if (mode != null && mode.equals("edit")) {
                String id = renderRequest.getParameter("gbeanId");
                ObjectName con = JMXUtil.getObjectName(id);
                GBeanData gbeanData = kernel.getGBeanData(con);
                GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
                HttpsConnectorBean bean = new HttpsConnectorBean();
                bean.setId(con.toString());
                bean.setName(gbeanInfo.getName());
                bean.setPort(((Integer) gbeanData.getAttribute("port"))
                        .intValue());
                bean.setNeedClientAuth(((Boolean) gbeanData
                        .getAttribute("needClientAuth")).booleanValue());

                renderRequest.setAttribute("connector", bean);
            } else {
                ArrayList connectors = new ArrayList();
                Set gbeans = kernel.listGBeans(connector);

                Iterator iterator = gbeans.iterator();
                while (iterator.hasNext()) {
                    ObjectName con = (ObjectName) iterator.next();
                    GBeanData gbeanData = kernel.getGBeanData(con);
                    GBeanInfo gbeanInfo = gbeanData.getGBeanInfo();
                    HttpsConnectorBean bean = new HttpsConnectorBean();
                    bean.setId(con.toString());
                    bean.setName(gbeanInfo.getName());
                    bean.setPort(((Integer) gbeanData.getAttribute("port"))
                            .intValue());
                    connectors.add(bean);
                }
                renderRequest.setAttribute("connectors", connectors
                        .toArray(new HttpsConnectorBean[0]));
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }

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
                "/WEB-INF/view/webmanager/https/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/https/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/https/help.jsp");
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        kernel = null;
        super.destroy();
    }

}
