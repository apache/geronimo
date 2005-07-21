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

package org.apache.geronimo.console.logmanager;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.util.LogHelper;

public class LogViewerPortlet extends GenericPortlet {

    public static final int LOGS_PER_PAGE = 10;

    protected PortletRequestDispatcher searchView;

    protected PortletRequestDispatcher helpView;

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderRespose) throws PortletException, IOException {
        helpView.include(renderRequest, renderRespose);
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderRespose) throws PortletException, IOException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String action = renderRequest.getParameter("action");

        if ("refresh".equals(action)) {
            LogHelper.refresh();
        }

        String startPos = renderRequest.getParameter("startPos");
        String endPos = renderRequest.getParameter("endPos");
        String logLevel = renderRequest.getParameter("logLevel");
        String searchString = renderRequest.getParameter("searchString");

        int lines = LogHelper.getLineCount();
        int sPos = (startPos != null && startPos.length() > 0) ? Integer
                .parseInt(startPos) : (lines - LOGS_PER_PAGE);
        int ePos = (endPos != null && endPos.length() > 0) ? Integer
                .parseInt(endPos) : lines;

        try {
            renderRequest.setAttribute("searchResults", LogHelper.searchLogs(
                    sPos, ePos, logLevel, searchString));
        } catch (IOException e) {
            throw new PortletException(e.getMessage());
        }
        renderRequest.setAttribute("lineCount", new Integer(lines));
        renderRequest.setAttribute("startPos", new Integer(sPos));
        renderRequest.setAttribute("endPos", new Integer(ePos));
        renderRequest.setAttribute("logLevel", logLevel);
        renderRequest.setAttribute("searchString", searchString);

        searchView.include(renderRequest, renderRespose);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        PortletContext pc = portletConfig.getPortletContext();
        searchView = pc
                .getRequestDispatcher("/WEB-INF/view/logmanager/search.jsp");
        helpView = pc
                .getRequestDispatcher("/WEB-INF/view/logmanager/viewhelp.jsp");
        super.init(portletConfig);
    }

}