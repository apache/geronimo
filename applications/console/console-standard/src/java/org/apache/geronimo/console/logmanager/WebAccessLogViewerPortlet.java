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
import java.util.Calendar;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.portlet.*;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebAccessLogViewerPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(WebAccessLogViewerPortlet.class);

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

        String[] names = PortletManager.getWebManagerNames(renderRequest);

        //todo: new
        Map products = new LinkedHashMap();
        String chosen = renderRequest.getParameter("selectedContainer");
        if(chosen != null) { // Carry on to render the results with the right selection
            renderRequest.setAttribute("selectedContainer", chosen);
        }
        WebAccessLog chosenLog = null;
        if(names != null) {
            for (int i = 0; i < names.length; i++) {
                String webManagerName = names[i];
                WebManager manager = (WebManager) PortletManager.getManagedBean(renderRequest, webManagerName);
                String[] containers = PortletManager.getWebContainerNames(renderRequest, webManagerName);
                if (containers != null) {
                    for (int j = 0; j < containers.length; j++) {
                        String containerName = containers[j];
                        String combined = webManagerName+"%"+containerName;
                        if(containers.length == 1) {
                            products.put(manager.getProductName(), combined);
                        } else {
                            try {
                                ObjectName containerON = ObjectName.getInstance(containerName);
                                products.put(manager.getProductName()+" ("+containerON.getKeyProperty(NameFactory.J2EE_NAME)+")", combined);
                            } catch (MalformedObjectNameException e) {
                                log.error("Unable to parse ObjectName", e);
                            }
                        }
                        if(chosenLog == null) { // will pick the correct match, or the first if no selection is specified
                            if(chosen == null || chosen.equals(combined)) {
                                chosenLog = PortletManager.getWebAccessLog(renderRequest, webManagerName, containerName);
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


        //todo: completely revamp this argument processing
        String startDate = (String) renderRequest.getParameter("startDate");
        String startMonth = (String) renderRequest.getParameter("startMonth");
        String startYear = (String) renderRequest.getParameter("startYear");
        String endDate = (String) renderRequest.getParameter("endDate");
        String endMonth = (String) renderRequest.getParameter("endMonth");
        String endYear = (String) renderRequest.getParameter("endYear");

        Calendar cal1 = Calendar.getInstance(), cal2 = Calendar.getInstance();
        // If not all dates were passed we assume than no fields were passed and just
        // filter on the current date.
        if (startDate == null || startMonth == null || startYear == null
                || endDate == null || endMonth == null || endYear == null) {
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
                                        null, null, null, null, cal1.getTime(), cal2.getTime(), null, null);
            renderRequest.setAttribute("logs", matchingItems.getResults());
            renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
        } else {
            cal1.clear();
            cal2.clear();
            // get the requested start date (defaults to 00:00:00:000 for time
            cal1.set(Calendar.DATE, Integer.parseInt(startDate));
            cal1.set(Calendar.MONTH, Integer.parseInt(startMonth));
            cal1.set(Calendar.YEAR, Integer.parseInt(startYear));
            // get the requested end date - Note: must set time to end of day
            cal2.set(Calendar.DATE, Integer.parseInt(endDate));
            cal2.set(Calendar.MONTH, Integer.parseInt(endMonth));
            cal2.set(Calendar.YEAR, Integer.parseInt(endYear));
            cal2.set(Calendar.HOUR_OF_DAY, cal2.getMaximum(Calendar.HOUR_OF_DAY));
            cal2.set(Calendar.MINUTE, cal2.getMaximum(Calendar.MINUTE));
            cal2.set(Calendar.SECOND, cal2.getMaximum(Calendar.SECOND));
            cal2.set(Calendar.MILLISECOND, cal2.getMaximum(Calendar.MILLISECOND));
            // Get other search criteria
            String requestHost = (String) renderRequest.getParameter("requestHost");
            String authUser = (String) renderRequest.getParameter("authUser");
            String requestMethod = (String) renderRequest.getParameter("requestMethod");
            String requestedURI = (String) renderRequest.getParameter("requestedURI");
            boolean ignoreDates = renderRequest.getParameter("ignoreDates") != null;
            if (ignoreDates) {
                WebAccessLog.SearchResults matchingItems = chosenLog.getMatchingItems(logToSearch,
                                                requestHost, authUser, requestMethod, requestedURI, null, null, null, null);
                renderRequest.setAttribute("logs", matchingItems.getResults());
                renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            } else {
                WebAccessLog.SearchResults matchingItems = chosenLog.getMatchingItems(logToSearch,
                                                requestHost, authUser, requestMethod, requestedURI, cal1.getTime(), cal2.getTime(), null, null);
                renderRequest.setAttribute("logs", matchingItems.getResults());
                renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
            }
            if (ignoreDates) renderRequest.setAttribute("ignoreDates", new Boolean(ignoreDates));
            renderRequest.setAttribute("requestHost", requestHost);
            renderRequest.setAttribute("authUser", authUser);
            renderRequest.setAttribute("requestMethod", requestMethod);
            renderRequest.setAttribute("requestedURI", requestedURI);

        }
        renderRequest.setAttribute("toDate", cal2.getTime());
        renderRequest.setAttribute("fromDate", cal1.getTime());
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
