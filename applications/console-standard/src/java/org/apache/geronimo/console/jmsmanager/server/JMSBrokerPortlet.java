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
package org.apache.geronimo.console.jmsmanager.server;

import java.io.IOException;
import java.util.Map;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletConfig;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic list of JMS brokers
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public class JMSBrokerPortlet extends BaseJMSPortlet {
    private final static Log log = LogFactory.getLog(JMSBrokerPortlet.class);
    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String mode = actionRequest.getParameter("mode");
            String name = actionRequest.getParameter("objectName");
            if(mode.equals("start")) {
                try {
                    //todo: this only goes into the "starting" state, doesn't make it to "running" -- what's up with that?
                    PortletManager.getManagedBean(actionRequest, name).startRecursive();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
            } else if(mode.equals("stop")) {
                try {
                    PortletManager.getManagedBean(actionRequest, name).stop();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
            } else if(mode.equals("edit")) {
                //todo: is there anything to edit?
            } else if(mode.equals("delete")) {
                //todo: add a method to JMSManager to handle this
            } else if(mode.equals("new")) {
                //todo: add a method to JMSManager to handle this -- it needs to let you pick a configuration that has ActiveMQ on the path...
            }
            actionResponse.setRenderParameter("mode", "list");
        } catch (Throwable e) {
            log.error("Unable to process portlet action", e);
            if(e instanceof PortletException) {
                throw (PortletException)e;
            }
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        try {
            if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
                return;
            }
            Map map = getBrokerMap(renderRequest);
            renderRequest.setAttribute("brokers", map.entrySet());
            if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
                normalView.include(renderRequest, renderResponse);
            } else {
                maximizedView.include(renderRequest, renderResponse);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/jmsmanager/server/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/jmsmanager/server/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/jmsmanager/server/help.jsp");
    }

    public void destroy() {
        helpView = null;
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

}
