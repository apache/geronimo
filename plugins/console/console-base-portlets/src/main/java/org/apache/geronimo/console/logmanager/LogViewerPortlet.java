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

package org.apache.geronimo.console.logmanager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.logging.SystemLog;

/**
 * @version $Rev$ $Date$
 */
public class LogViewerPortlet extends BasePortlet {
    private final static String CRITERIA_KEY = "org.apache.geronimo.console.log.CRITERIA";

    protected PortletRequestDispatcher searchView;

    protected PortletRequestDispatcher helpView;

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderRespose) throws PortletException, IOException {
        helpView.include(renderRequest, renderRespose);
    }

    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {

        //Add all the parameters to the actionResponse Attributes so we can get the back
        actionResponse.setRenderParameters(actionRequest.getParameterMap());

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
        Criteria criteria = (Criteria) renderRequest.getPortletSession(true).getAttribute(CRITERIA_KEY, PortletSession.PORTLET_SCOPE);

        if(criteria != null) {
            // Check if criteria.logFile is in the logFileNames of current logging configuration
            boolean found = false;
            for(String logFile: logFiles) {
                if(criteria.logFile.equals(logFile)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                // This arises when log4j properties file is changed dynamically using LogManagerPortlet
                // and the earlier log file is no longer in the current logging configuration.
                // Change the log file to any one in the current logging configuration so that LogViewer
                // won't run into errors.
                criteria.logFile = logFiles[0];
            }
        }
        if (criteria == null || (action != null && !"refresh".equals(action))) {
            if(criteria == null)
                criteria = new Criteria();
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

            criteria.level = logLevel == null || logLevel.equals("") ? criteria.level : logLevel;
            try{
                criteria.max = maxRows == null || maxRows.equals("") ? criteria.max : Integer.parseInt(maxRows);
            }catch(NumberFormatException e){
                //ignore
            }
            try{
                criteria.start = startPos == null || startPos.equals("") ? null : new Integer(startPos);
            }catch(NumberFormatException e){
            //ignore
            }
            try{
                criteria.stop = endPos == null || endPos.equals("") ? null : new Integer(endPos);
            }catch(NumberFormatException e){
                //ignore
                }
            criteria.logFile = logFile;
            criteria.stackTraces = stackTraces != null && !stackTraces.equals("");

            criteria.text = searchString == null || searchString.equals("") ? null : searchString;
            renderRequest.getPortletSession(true).setAttribute(CRITERIA_KEY, criteria, PortletSession.PORTLET_SCOPE);
        }

        SystemLog.SearchResults results = log.getMatchingItems(criteria.logFile, criteria.start, criteria.stop,
                        criteria.level, criteria.text, criteria.max, criteria.stackTraces);
        renderRequest.setAttribute("searchResults", results.getResults());
        renderRequest.setAttribute("lineCount", Integer.valueOf(results.getLineCount()));
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
        int max = 10;
        Integer start;
        Integer stop;
        String text;
        String level = "WARN";
        String logFile;
        boolean stackTraces;
    }

    public static class LogFile {
        private String fullName;
        private String name;

        public LogFile(String fullName) {
            this.fullName = fullName;
            this.name = (new File(fullName)).getName();
        }

        public String getFullName() {
            return fullName;
        }

        public String getName() {
            return name;
        }
    }
}
