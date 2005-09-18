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

package org.apache.geronimo.console.derbylogmanager;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.BasePortlet;

public class DerbyLogViewerPortlet extends BasePortlet {

    protected PortletRequestDispatcher normalView;

    protected PortletRequestDispatcher helpView;

    public void destroy() {
        super.destroy();
        normalView = null;
        helpView = null;
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        String action = renderRequest.getParameter("action");
        if ("refresh".equals(action)) {
            DerbyLogHelper.refresh();
        }
        try {
            renderRequest.setAttribute("logs", DerbyLogHelper.getLogs());
            renderRequest.setAttribute("lines", new Integer(DerbyLogHelper
                    .getLineCount()));
            normalView.include(renderRequest, renderResponse);
        } catch (Exception e) {
            renderResponse.setContentType("text/html");
            renderResponse.getWriter().println(
                    "<b>Could not load portlet: " + e.getMessage()
                            + "</b></br>");
            throw new PortletException(e);
        }
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc
                .getRequestDispatcher("/WEB-INF/view/derbylogmanager/view.jsp");
        helpView = pc
                .getRequestDispatcher("/WEB-INF/view/derbylogmanager/help.jsp");
        super.init(portletConfig);
    }
}
