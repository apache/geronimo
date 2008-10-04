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

package org.apache.geronimo.console.jmsmanager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.jmsmanager.handlers.CreateDestinationHandler;
import org.apache.geronimo.console.jmsmanager.handlers.PortletResponseHandler;
import org.apache.geronimo.console.jmsmanager.handlers.RemoveDestinationHandler;
import org.apache.geronimo.console.jmsmanager.handlers.StatisticsHandler;
import org.apache.geronimo.console.jmsmanager.renderers.CreateDestinationRenderer;
import org.apache.geronimo.console.jmsmanager.renderers.PortletRenderer;
import org.apache.geronimo.console.jmsmanager.renderers.StatisticsRenderer;
import org.apache.geronimo.console.jmsmanager.renderers.ViewDLQRenderer;
import org.apache.geronimo.console.jmsmanager.renderers.ViewDestinationsRenderer;
import org.apache.geronimo.console.jmsmanager.renderers.ViewMessagesRenderer;

public class JMSManagerPortlet extends BasePortlet {

    private PortletRequestDispatcher edit;

    private PortletRequestDispatcher help;

    private ConnectionFactory cf;

    private Map handlers;

    private Map renderers;

    private PortletContext context;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        context = portletConfig.getPortletContext();

        help = context
                .getRequestDispatcher("/WEB-INF/view/jmsmanager/help.jsp");
        edit = context
                .getRequestDispatcher("/WEB-INF/view/jmsmanager/edit.jsp");

        renderers = new HashMap();
        renderers.put("createDestination", new CreateDestinationRenderer());
        renderers.put("viewDestinations", new ViewDestinationsRenderer());
        renderers.put("statistics", new StatisticsRenderer());
        renderers.put("viewMessages", new ViewMessagesRenderer());
        renderers.put("viewDLQ", new ViewDLQRenderer());

        handlers = new HashMap();
        handlers.put("createDestination", new CreateDestinationHandler());
        handlers.put("removeDestination", new RemoveDestinationHandler());
        handlers.put("statistics", new StatisticsHandler());

    }

    public void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        help.include(renderRequest, renderResponse);
    }

    public void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {

        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        String view = renderRequest.getParameter("processAction");
        if (view == null) {
            // no specific content requested - show the destinations list
            view = "viewDestinations";
        }

        PortletRenderer renderer = (PortletRenderer) renderers.get(view);
        if (renderer == null) {
            throw new PortletException("Invalid view parameter specified: "
                    + view);
        }

        String include = renderer.render(renderRequest, renderResponse);
        if (include != null) {
            PortletRequestDispatcher requestDispatcher = context
                    .getRequestDispatcher(include);
            requestDispatcher.include(renderRequest, renderResponse);
        }

    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String processAction = actionRequest.getParameter("processaction");

        PortletResponseHandler handler = (PortletResponseHandler) handlers
                .get(processAction);

        if (handler == null) {
            // throw new RuntimeException( "no handlers for processAction = " +
            // processAction );
            handler = (PortletResponseHandler) handlers.get("viewDestinations");
        }

        handler.processAction(actionRequest, actionResponse);
    }

    public void destroy() {

        help = null;
        edit = null;
        super.destroy();
    }

}
