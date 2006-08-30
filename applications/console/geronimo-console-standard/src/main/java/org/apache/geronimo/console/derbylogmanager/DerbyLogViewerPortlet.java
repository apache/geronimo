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

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.derby.DerbyLog;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.io.Serializable;

public class DerbyLogViewerPortlet extends BasePortlet {
    private final static String CRITERIA_KEY = "org.apache.geronimo.console.derby.log.CRITERIA";

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
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String action = renderRequest.getParameter("action");

        DerbyLog log = (DerbyLog) PortletManager.getManagedBeans(renderRequest, DerbyLog.class)[0];//todo: what if it's not there?
        Criteria criteria;
        if ("refresh".equals(action)) {
            criteria = (Criteria) renderRequest.getPortletSession(true).getAttribute(CRITERIA_KEY, PortletSession.PORTLET_SCOPE);
        } else {
            String startPos = renderRequest.getParameter("startPos");
            String endPos = renderRequest.getParameter("endPos");
            String maxRows = renderRequest.getParameter("maxRows");
            String searchString = renderRequest.getParameter("searchString");
            if(maxRows == null || maxRows.equals("")) {
                maxRows = "10";
            }
            criteria = new Criteria();
            criteria.max = new Integer(maxRows);
            criteria.start = startPos == null || startPos.equals("") ? null : new Integer(startPos);
            criteria.stop = endPos == null || endPos.equals("") ? null : new Integer(endPos);
            criteria.text = searchString == null || searchString.equals("") ? null : searchString;
            renderRequest.getPortletSession(true).setAttribute(CRITERIA_KEY, criteria, PortletSession.PORTLET_SCOPE);
        }

        DerbyLog.SearchResults results = log.searchLog(criteria.start, criteria.stop,
                         criteria.max, criteria.text);
        renderRequest.setAttribute("searchResults", results.getResults());
        renderRequest.setAttribute("lineCount", new Integer(results.getLineCount()));
        renderRequest.setAttribute("startPos", criteria.start);
        renderRequest.setAttribute("endPos", criteria.stop);
        renderRequest.setAttribute("searchString", criteria.text);
        renderRequest.setAttribute("maxRows", criteria.max);
        if(results.isCapped()) {
            renderRequest.setAttribute("capped", Boolean.TRUE);
        }

        normalView.include(renderRequest, renderResponse);
    }

    private static class Criteria implements Serializable {
        Integer max;
        Integer start;
        Integer stop;
        String text;
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
