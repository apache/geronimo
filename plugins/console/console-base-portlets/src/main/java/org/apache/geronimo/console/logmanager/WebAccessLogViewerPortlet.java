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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;
import java.io.Serializable;
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
    private final static String CRITERIA_KEY = "org.apache.geronimo.console.web.log.CRITERIA";
    private static final Logger log = LoggerFactory.getLogger(WebAccessLogViewerPortlet.class);
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

        Criteria criteria = (Criteria) renderRequest.getPortletSession(true).getAttribute(CRITERIA_KEY, PortletSession.PORTLET_SCOPE);


        //todo: new
        Map products = new LinkedHashMap();
        String chosenContainer = null;;
//      Temporarily disable container selection.
//      We don't current enable this in the portlet anyway and at the moment it is just unnecessary data traveling back and forth.
//      chosenContainer = renderRequest.getParameter("selectedContainer");
//      if(chosenContainer != null) { // Carry on to render the results with the right selection
//          renderRequest.setAttribute("selectedContainer", chosenContainer);
//      }
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
                            if(chosenContainer == null || chosenContainer.equals(combined)) {
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
//      Temporarily disable container and log parameters.
//      We don't current enable this in the portlet anyway and at the moment it is just unnecessary data traveling back and forth.
//      renderRequest.setAttribute("webContainers", products);
        String[] logNames = chosenLog.getLogNames();
        if (logNames.length == 0) {
            searchView.include(renderRequest, renderRespose);
            return;
        }
//      renderRequest.setAttribute("webLogs", logNames);
        String logToSearch = null;
//      String logToSearch = renderRequest.getParameter("selectedLog");
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
        if (criteria == null || (action != null && !"refresh".equals(action))) {
            if(criteria == null)
                criteria = new Criteria();

            String fromDate = renderRequest.getParameter("fromDate");
            String toDate = renderRequest.getParameter("toDate");
            String requestHost = renderRequest.getParameter("requestHost");
            String authUser = renderRequest.getParameter("authUser");
            String method = renderRequest.getParameter("requestMethod");
            String uri = renderRequest.getParameter("requestedURI");
            String result = renderRequest.getParameter("startResult");
            Integer max = criteria.maxResult;
            try{
                max = Integer.parseInt(renderRequest.getParameter("maxResult"));
            }catch(NumberFormatException e){
            //ignore
            }
            String ignoreDates = renderRequest.getParameter("ignoreDates");

            criteria.fromDate = fromDate == null || fromDate.equals("") ? null : fromDate;
            criteria.toDate = toDate == null || toDate.equals("") ? null : toDate;
            criteria.requestHost = requestHost == null || requestHost.equals("") ? null : requestHost;
            criteria.authUser = authUser == null || authUser.equals("") ? null : authUser;
            criteria.requestMethod = method == null || method.equals("") ? null : method;
            criteria.requestedURI = uri == null || uri.equals("") ? null : uri;
            criteria.startResult = result == null || result.equals("") ? null : result;
            criteria.maxResult = max;
            criteria.ignoreDates = ignoreDates != null && !ignoreDates.equals("");
        }
        String fromDateStr = criteria.fromDate;
        String toDateStr = criteria.toDate;

        Calendar cal1 = Calendar.getInstance(), cal2 = Calendar.getInstance();
        // If not all dates were passed and ignoreDates is not enabled, filter on the current date.
        // Note: This happens only when the portlet is loaded for the first time. Subsequent invocation (other than
        // using "Refresh") will have either ignoreDates enabled or both the dates non null.
        if((fromDateStr == null || toDateStr == null) && !criteria.ignoreDates) {
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
                                        null, null, null, null, cal1.getTime(), cal2.getTime(), null, Integer.valueOf(criteria.maxResult.intValue()-1));
            renderRequest.setAttribute("logs", matchingItems.getResults());
            renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            renderRequest.setAttribute("maxResult", criteria.maxResult);
            renderRequest.setAttribute("ignoreDates", Boolean.valueOf(criteria.ignoreDates));

        } else {
            // Get other search criteria
            String requestHost = criteria.requestHost;
            String authUser = criteria.authUser;
            String requestMethod = criteria.requestMethod;
            String requestedURI = criteria.requestedURI;
            String startResult = criteria.startResult;
            Integer iStartResult = null;
            Integer iMaxResult = criteria.maxResult;
            try{
                iStartResult = Integer.valueOf(startResult);
            }catch(NumberFormatException e){
                //ignore
            }

            boolean ignoreDates = criteria.ignoreDates;
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
                renderRequest.setAttribute("logLength", Integer.valueOf(matchingItems.getLineCount()));
            }
            renderRequest.setAttribute("ignoreDates", Boolean.valueOf(ignoreDates));
            renderRequest.setAttribute("requestHost", requestHost);
            renderRequest.setAttribute("authUser", authUser);
            renderRequest.setAttribute("requestMethod", requestMethod);
            renderRequest.setAttribute("requestedURI", requestedURI);
            if(iStartResult != null)renderRequest.setAttribute("startResult", iStartResult);
            if(iMaxResult != null)renderRequest.setAttribute("maxResult", iMaxResult);
        }
        renderRequest.setAttribute("toDate", toDateStr);
        renderRequest.setAttribute("fromDate", fromDateStr);
        renderRequest.getPortletSession(true).setAttribute(CRITERIA_KEY, criteria, PortletSession.PORTLET_SCOPE);
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
        String[] paramNames = {"action",
                               "fromDate",
                               "toDate",
                               "ignoreDates",
                               "requestHost",
                               "authUser",
                               "requestMethod",
                               "requestedURI",
                               "startResult",
                               "maxResult"};
        // copy all action parameters to render parameters
        for(int i = 0; i < paramNames.length; i++) {
            if(actionRequest.getParameter(paramNames[i]) != null) {
                actionResponse.setRenderParameter(
                        paramNames[i],
                        actionRequest.getParameter(paramNames[i]));
            }
        }
    }
    private static class Criteria implements Serializable {
        Integer maxResult = Integer.valueOf(DEFAULT_MAX_RESULTS);
        String fromDate = null;
        String toDate = null;
        boolean ignoreDates = false;
        String requestHost = null;
        String authUser = null;
        String requestMethod = "ANY";
        String requestedURI = null;
        String startResult = null;
    }

}
