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

import javax.portlet.*;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebAccessLog;
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

        String[] names = PortletManager.getWebManagerNames(renderRequest);  //todo: handle multiple
        if (names != null) {
            String managerName = names[0];  //todo: handle multiple
            String[] containers = PortletManager.getWebContainerNames(renderRequest, managerName);  //todo: handle multiple
            if (containers != null) {
                String containerName = containers[0];  //todo: handle multiple
                WebAccessLog log = PortletManager.getWebAccessLog(renderRequest, managerName, containerName);

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
                    cal1.clear(Calendar.HOUR_OF_DAY);
                    cal2.setTime(cal1.getTime());
                    cal2.add(Calendar.DAY_OF_YEAR, 1);

                    WebAccessLog.SearchResults matchingItems = log.getMatchingItems(log.getLogFileNames()[0], //todo: handle multiple
                                                null, null, null, null, cal1.getTime(), cal2.getTime(), null, null);
                    renderRequest.setAttribute("logs", matchingItems.getResults());
                    renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
                } else {
                    int sdt = Integer.parseInt(startDate),
                        smnth = Integer.parseInt(startMonth),
                        syr = Integer.parseInt(startYear),
                        edt = Integer.parseInt(endDate),
                        emnth = Integer.parseInt(endMonth),
                        eyr = Integer.parseInt(endYear);
                    boolean ignoreDates = renderRequest.getParameter("ignoreDates") == null;
                    String requestHost = (String) renderRequest.getParameter("requestHost");
                    String authUser = (String) renderRequest.getParameter("authUser");
                    String requestMethod = (String) renderRequest.getParameter("requestMethod");
                    String requestedURI = (String) renderRequest.getParameter("requestedURI");
                    if (ignoreDates) {
                        cal1.clear();
                        cal2.clear();
                        cal1.set(Calendar.DATE, sdt);
                        cal1.set(Calendar.MONTH, smnth);
                        cal1.set(Calendar.YEAR, syr);
                        cal2.set(Calendar.DATE, edt);
                        cal2.set(Calendar.MONTH, emnth);
                        cal2.set(Calendar.YEAR, eyr);
                        WebAccessLog.SearchResults matchingItems = log.getMatchingItems(log.getLogFileNames()[0], //todo: handle multiple
                                                        requestHost, authUser, requestMethod, requestedURI, cal1.getTime(), cal2.getTime(), null, null);
                        renderRequest.setAttribute("logs", matchingItems.getResults());
                        renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
                    } else {
                        WebAccessLog.SearchResults matchingItems = log.getMatchingItems(log.getLogFileNames()[0], //todo: handle multiple
                                                        requestHost, authUser, requestMethod, requestedURI, null, null, null, null);
                        renderRequest.setAttribute("logs", matchingItems.getResults());
                        renderRequest.setAttribute("logLength", new Integer(matchingItems.getLineCount()));
                    }
                    renderRequest.setAttribute("ignoreDates", new Boolean(ignoreDates));
                    renderRequest.setAttribute("requestHost", requestHost);
                    renderRequest.setAttribute("authUser", authUser);
                    renderRequest.setAttribute("requestMethod", requestMethod);
                    renderRequest.setAttribute("requestedURI", requestedURI);

                }
                renderRequest.setAttribute("toDate", cal2.getTime());
                renderRequest.setAttribute("fromDate", cal1.getTime());
                searchView.include(renderRequest, renderRespose);
            } else {
                log.error("No web containers found");
            }
        } else {
            log.error("No web managers found");
        }
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
