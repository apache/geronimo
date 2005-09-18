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
import java.io.Serializable;
import java.io.File;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletSession;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.logging.SystemLog;

public class LogViewerPortlet extends BasePortlet {
    private final static String CRITERIA_KEY = "org.apache.geronimo.console.log.CRITERIA";

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

        SystemLog log = PortletManager.getCurrentSystemLog(renderRequest);
        String[] logFiles = log.getLogFileNames();
        LogFile[] files = new LogFile[logFiles.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new LogFile(logFiles[i]);
        }
        Criteria criteria;
        if ("refresh".equals(action)) {
            criteria = (Criteria) renderRequest.getPortletSession(true).getAttribute(CRITERIA_KEY, PortletSession.PORTLET_SCOPE);
        } else {
            String startPos = renderRequest.getParameter("startPos");
            String endPos = renderRequest.getParameter("endPos");
            String maxRows = renderRequest.getParameter("maxRows");
            String logLevel = renderRequest.getParameter("logLevel");
            String searchString = renderRequest.getParameter("searchString");
            String stackTraces = renderRequest.getParameter("stackTraces");
            String logFile = renderRequest.getParameter("logFile");
            if(logFile == null || logFile.equals("")) {
                logFile = logFiles[0];
            }
            if(logLevel == null || logLevel.equals("")) {
                logLevel = "WARN";
            }
            if(maxRows == null || maxRows.equals("")) {
                maxRows = "10";
            }
            criteria = new Criteria();
            criteria.max = Integer.parseInt(maxRows);
            criteria.start = startPos == null || startPos.equals("") ? null : new Integer(startPos);
            criteria.stop = endPos == null || endPos.equals("") ? null : new Integer(endPos);
            criteria.logFile = logFile;
            criteria.stackTraces = stackTraces != null && !stackTraces.equals("");
            criteria.level = logLevel;
            criteria.text = searchString == null || searchString.equals("") ? null : searchString;
            renderRequest.getPortletSession(true).setAttribute(CRITERIA_KEY, criteria, PortletSession.PORTLET_SCOPE);
        }

        SystemLog.SearchResults results = log.getMatchingItems(criteria.logFile, criteria.start, criteria.stop,
                        criteria.level, criteria.text, criteria.max, criteria.stackTraces);
        renderRequest.setAttribute("searchResults", results.getResults());
        renderRequest.setAttribute("lineCount", new Integer(results.getLineCount()));
        renderRequest.setAttribute("startPos", criteria.start);
        renderRequest.setAttribute("endPos", criteria.stop);
        renderRequest.setAttribute("logLevel", criteria.level);
        renderRequest.setAttribute("searchString", criteria.text);
        renderRequest.setAttribute("maxRows", Integer.toString(criteria.max));
        renderRequest.setAttribute("logFile", criteria.logFile);
        renderRequest.setAttribute("logFiles", files);
        if(criteria.stackTraces) {
            renderRequest.setAttribute("stackTraces", Boolean.TRUE);
        }
        if(results.isCapped()) {
            renderRequest.setAttribute("capped", Boolean.TRUE);
        }

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

    private static class Criteria implements Serializable {
        int max;
        Integer start;
        Integer stop;
        String text;
        String level;
        String logFile;
        boolean stackTraces;
    }

    public static class LogFile {
        private String fullName;
        private String name;

        public LogFile(String fullName) {
            this.fullName = fullName;
            //todo: what if portla JVM has different separator than server JVM?
            int pos = fullName.lastIndexOf(File.separatorChar);
            if(pos > -1) {
                name = fullName.substring(pos+1);
            } else {
                name = fullName;
            }
        }

        public String getFullName() {
            return fullName;
        }

        public String getName() {
            return name;
        }
    }
}