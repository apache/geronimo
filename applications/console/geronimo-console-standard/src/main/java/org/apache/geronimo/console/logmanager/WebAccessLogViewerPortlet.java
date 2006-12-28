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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class WebAccessLogViewerPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(WebAccessLogViewerPortlet.class);
    private static final int DEFAULT_MAX_RESULTS = 10;

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

        WebManager[] managers = PortletManager.getCurrentServer(renderRequest).getWebManagers();

        //todo: new
        Map products = new LinkedHashMap();
        String chosen = renderRequest.getParameter("selectedContainer");
        if(chosen != null) { // Carry on to render the results with the right selection
            renderRequest.setAttribute("selectedContainer", chosen);
        }
        WebAccessLog chosenLog = null;
        if(managers != null) {
            for (int i = 0; i < managers.length; i++) {
                WebManager manager = managers[i];
                AbstractName managerName = PortletManager.getNameFor(renderRequest, manager);
                WebContainer[] containers = (WebContainer[]) manager.getContainers();
                if (containers != null) {
                    for (int j = 0; j < containers.length; j++) {
                        WebContainer container = containers[j];
                        AbstractName containerName = PortletManager.getNameFor(renderRequest, container);
                        String combined = managerName+"%"+containerName;
                        if(containers.length == 1) {
                            products.put(manager.getProductName(), combined);
                        } else {
                            products.put(manager.getProductName()+" ("+containerName.getName().get(NameFactory.J2EE_NAME)+")", combined);
                        }
                        if(chosenLog == null) { // will pick the correct match, or the first if no selection is specified
                            if(chosen == null || chosen.equals(combined)) {
                                chosenLog = PortletManager.getWebAccessLog(renderRequest, managerName, containerName);
                            }
                        }
                    }
                } else {
                    log.error("No web containers found for manager "+manager.getProductName());
                }
            }
        } else {
            log.error("No web managers found!");
        }
        renderRequest.setAttribute("webContainers", products);
        final String[] logNames = chosenLog.getLogNames();
        renderRequest.setAttribute("webLogs", logNames);
        String logToSearch = renderRequest.getParameter("selectedLog");
        if(logToSearch == null) {
            logToSearch = logNames[0];
        } else { //what if the log options for Jetty were showing, but the user picked Tomcat to search?  todo: fix this with some AJAX to repopulate the form when container is changed
            boolean found = false;
            for (int i = 0; i < logNames.length; i++) {
                String test = logNames[i];
                if(test.equals(logToSearch)) {
                    found = true;
                    break;
                }
            }
            if(!found) { // Must has been for the other container -- make it work.
                logToSearch = logNames[0];
            }
        }

        String action = renderRequest.getParameter("action");
        if ("refresh".equals(action)) {
            //todo: currently refreshes on every request; that's pretty slow.
        }

        String fromDateStr = (String) renderRequest.getParameter("fromDate");
        String toDateStr = (String) renderRequest.getParameter("toDate");

        Calendar cal1 = Calendar.getInstance(), cal2 = Calendar.getInstance();
        // If not all dates were passed we assume than no fields were passed and just
        // filter on the current date.
        if(fromDateStr == null || toDateStr == null) {
            // just keep the month date and year
            cal1.set(Calendar.MILLISECOND, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.HOUR_OF_DAY, 0);

            cal2.set(Calendar.HOUR_OF_DAY, cal2.getMaximum(Calendar.HOUR_OF_DAY));
            cal2.set(Calendar.MINUTE, cal2.getMaximum(Calendar.MINUTE));
            cal2.set(Calendar.SECOND, cal2.getMaximum(Calendar.SECOND));
            cal2.set(Calendar.MILLISECOND, cal2.getMaximum(Calendar.MILLISECOND));

            WebAccessLog.SearchResults matchingItems = chosenLog.getMatchingItems(logToSearch,
                                        null, null, null, null, cal1.getTime(), cal2.getTime(), null, Integer.valueOf(DEFAULT_MAX_RESULTS - 1));
            renderRequest.setAttribute("logs", matchingItems.getResults());
            renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            renderRequest.setAttribute("maxResult", Integer.valueOf(DEFAULT_MAX_RESULTS));
        } else {
            // Get other search criteria
            String requestHost = (String) renderRequest.getParameter("requestHost");
            String authUser = (String) renderRequest.getParameter("authUser");
            String requestMethod = (String) renderRequest.getParameter("requestMethod");
            String requestedURI = (String) renderRequest.getParameter("requestedURI");
            String startResult = (String) renderRequest.getParameter("startResult");
            String maxResult = (String) renderRequest.getParameter("maxResult");
            Integer iStartResult = null;
            Integer iMaxResult = Integer.valueOf(DEFAULT_MAX_RESULTS);
            try{
                iStartResult = Integer.valueOf(startResult);
            }catch(NumberFormatException e){
                //ignore
            }
            try{
                iMaxResult = Integer.valueOf(maxResult);
            }catch(NumberFormatException e){
                //ignore
            }
            
            boolean ignoreDates = renderRequest.getParameter("ignoreDates") != null;
            if (ignoreDates) {
                WebAccessLog.SearchResults matchingItems = chosenLog.getMatchingItems(logToSearch,
                                                requestHost, authUser, requestMethod, requestedURI, null, null, iStartResult, Integer.valueOf(iMaxResult.intValue()-1));
                renderRequest.setAttribute("logs", matchingItems.getResults());
                renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            } else {
                Date fromDate = null, toDate = null;
                // Check if the from and to date format is MM/DD/YYYY
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    fromDate = df.parse(fromDateStr);
                    // get the requested start date (defaults to 00:00:00:000 for time)
                    cal1.setTime(fromDate);
                    String mmddyyyy = (cal1.get(Calendar.MONTH) < 9 ? "0":"") + (cal1.get(Calendar.MONTH)+1);
                    mmddyyyy += "/"+(cal1.get(Calendar.DAY_OF_MONTH) < 10 ? "0":"") + (cal1.get(Calendar.DAY_OF_MONTH));
                    mmddyyyy += "/"+cal1.get(Calendar.YEAR);
                    if(!mmddyyyy.equals(fromDateStr)) {
                        // This should not arise since date input has been validated using javascript.
                        // If this does occur, ignore dates in search criteria and log a WARNING
                        log.warn("From date must be a date in MM/DD/YYYY format, not '"+fromDateStr+"'. Dates will be ignored.");
                        fromDate = null;
                    }
                    toDate = df.parse(toDateStr);
                    cal2.setTime(toDate);
                    mmddyyyy = (cal2.get(Calendar.MONTH) < 9 ? "0":"") + (cal2.get(Calendar.MONTH)+1);
                    mmddyyyy += "/"+(cal2.get(Calendar.DAY_OF_MONTH) < 10 ? "0":"") + (cal2.get(Calendar.DAY_OF_MONTH));
                    mmddyyyy += "/"+cal2.get(Calendar.YEAR);
                    if(!mmddyyyy.equals(toDateStr)) {
                        // This should not arise since date input has been validated using javascript.
                        // If this does occur, ignore to date in search criteria and log a WARNING
                        log.warn("To date must be a date in MM/DD/YYYY format, not "+toDateStr+"'. Dates will be ignored.");
                        toDate = null;
                    } else {
                        // get the requested end date - Note: must set time to end of day
                        cal2.set(Calendar.HOUR_OF_DAY, cal2.getMaximum(Calendar.HOUR_OF_DAY));
                        cal2.set(Calendar.MINUTE, cal2.getMaximum(Calendar.MINUTE));
                        cal2.set(Calendar.SECOND, cal2.getMaximum(Calendar.SECOND));
                        cal2.set(Calendar.MILLISECOND, cal2.getMaximum(Calendar.MILLISECOND));
                        toDate = cal2.getTime();
                    }
                } catch (ParseException e) {
                    // Should not occur since date input has been validated using javascript.
                    // If this does occur, ignore from and to dates and log a WARNING
                    log.warn("Error parsing input dates.  Dates will be ignored.", e);
                }
                if(fromDate == null || toDate == null) {
                    fromDate = toDate = null;
                }
                WebAccessLog.SearchResults matchingItems = chosenLog.getMatchingItems(logToSearch,
                                                requestHost, authUser, requestMethod, requestedURI, fromDate, toDate, iStartResult, Integer.valueOf(iMaxResult.intValue()-1));
                renderRequest.setAttribute("logs", matchingItems.getResults());
                renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            }
            if (ignoreDates) renderRequest.setAttribute("ignoreDates", new Boolean(ignoreDates));
            renderRequest.setAttribute("requestHost", requestHost);
            renderRequest.setAttribute("authUser", authUser);
            renderRequest.setAttribute("requestMethod", requestMethod);
            renderRequest.setAttribute("requestedURI", requestedURI);
            if(iStartResult != null)renderRequest.setAttribute("startResult", iStartResult);
            if(iMaxResult != null)renderRequest.setAttribute("maxResult", iMaxResult);
        }
        renderRequest.setAttribute("toDate", toDateStr);
        renderRequest.setAttribute("fromDate", fromDateStr);
        searchView.include(renderRequest, renderRespose);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        PortletContext pc = portletConfig.getPortletContext();
        searchView = pc.getRequestDispatcher("/WEB-INF/view/webaccesslogmanager/view.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/webaccesslogmanager/help.jsp");
        super.init(portletConfig);
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        //todo: according to portlet spec, all forms should submit to Action not Render
    }

}
