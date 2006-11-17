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

package org.apache.geronimo.console.webmanager;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.stats.WebContainerStats;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.IOException;

/**
 * Basic portlet showing statistics for a web container
 *
 * @version $Rev$ $Date$
 */
public class WebManagerPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(WebManagerPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        try {
            WebManager[] managers = PortletManager.getCurrentServer(actionRequest).getWebManagers();
            if (managers != null) {
                WebManager manager = managers[0];  //todo: handle multiple
                WebContainer[] containers = (WebContainer[]) manager.getContainers();
                if (containers != null) {
                    WebContainer container = containers[0];  //todo: handle multiple
                    String server = getWebServerType(container.getClass());
                    String action = actionRequest.getParameter("stats");
                    if (action != null) {
                        boolean stats = action.equals("true");
                        if(server.equals(WEB_SERVER_JETTY)) {
                            setProperty(container, "collectStatistics", stats ? Boolean.TRUE : Boolean.FALSE);
                        }
                        else if (server.equals(WEB_SERVER_TOMCAT)) {
                            //todo:   Any Tomcat specific processing?
                        }
                        else {
                            log.error("Unrecognized Web Container");
                        }
                    }
                    if (actionRequest.getParameter("resetStats") != null) {
                        if(server.equals(WEB_SERVER_JETTY)) {
                            callOperation(container, "resetStatistics", null);
                        }
                        else if (server.equals(WEB_SERVER_TOMCAT)) {
                            //todo:   Any Tomcat specific processing?
                        }
                        else {
                            log.error("Unrecognized Web Container");
                        }
                    }
                }
                else {
                    log.error("Error attempting to retrieve the web containers");
                }
            }
            else {
                log.error("Error attempting to retrieve the web managers");
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        try {
            WebManager[] managers = PortletManager.getCurrentServer(renderRequest).getWebManagers();
            if (managers != null) {
                WebManager manager = managers[0];  //todo: handle multiple
                WebContainer[] containers = (WebContainer[]) manager.getContainers();
                if (containers != null) {
                    WebContainer container = containers[0];  //todo: handle multiple
                    if(container.isStatisticsProvider()) {
                        WebContainerStats webStats = (WebContainerStats) ((StatisticsProvider)container).getStats();
                        if (webStats.isStatsOn()) {
                            renderRequest.setAttribute("statsOn", Boolean.TRUE);
                            renderRequest.setAttribute("totalRequestCount", new Long(webStats.getTotalRequestCount().getCount()));
                            renderRequest.setAttribute("totalConnectionCount", new Long(webStats.getTotalConnectionCount().getCount()));
                            renderRequest.setAttribute("totalErrorCount", new Long(webStats.getTotalErrorCount().getCount()));
                            renderRequest.setAttribute("activeRequestCountCurrent", new Long(webStats.getActiveRequestCount().getCurrent()));
                            renderRequest.setAttribute("activeRequestCountLow", new Long(webStats.getActiveRequestCount().getLowWaterMark()));
                            renderRequest.setAttribute("activeRequestCountHigh", new Long(webStats.getActiveRequestCount().getHighWaterMark()));
                            renderRequest.setAttribute("connectionRequestCountCurrent", new Long(webStats.getConnectionRequestCount().getCurrent()));
                            renderRequest.setAttribute("connectionRequestCountLow", new Long(webStats.getConnectionRequestCount().getLowWaterMark()));
                            renderRequest.setAttribute("connectionRequestCountHigh", new Long(webStats.getConnectionRequestCount().getHighWaterMark()));
    //                          renderRequest.setAttribute("connectionRequestsAve", new Long(0));   /* Can't really compute this for a range ... do we still need it (from old portlet) */
                            renderRequest.setAttribute("openConnectionCountCurrent", new Long(webStats.getOpenConnectionCount().getCurrent()));
                            renderRequest.setAttribute("openConnectionCountLow", new Long(webStats.getOpenConnectionCount().getLowWaterMark()));
                            renderRequest.setAttribute("openConnectionCountHigh", new Long(webStats.getOpenConnectionCount().getHighWaterMark()));
                            renderRequest.setAttribute("requestDurationCount", new Long(webStats.getRequestDuration().getCount()));
                            renderRequest.setAttribute("requestDurationMinTime", new Long(webStats.getRequestDuration().getMinTime()));
                            renderRequest.setAttribute("requestDurationMaxTime", new Long(webStats.getRequestDuration().getMaxTime()));
                            renderRequest.setAttribute("requestDurationTotalTime", new Long(webStats.getRequestDuration().getTotalTime()));
    //                          renderRequest.setAttribute("requestDurationAve", new Long(0));  /* Would this be valuable to calculate?  We used to show this in the old jetty only portlet */
                            renderRequest.setAttribute("connectionDurationCount", new Long(webStats.getConnectionDuration().getCount()));
                            renderRequest.setAttribute("connectionDurationMinTime", new Long(webStats.getConnectionDuration().getMinTime()));
                            renderRequest.setAttribute("connectionDurationMaxTime", new Long(webStats.getConnectionDuration().getMaxTime()));
                            renderRequest.setAttribute("connectionDurationTotalTime", new Long(webStats.getConnectionDuration().getTotalTime()));
    //                          renderRequest.setAttribute("connectionDurationAve", new Long(0));   /* Wouldl this be valueable to calculate?  We used to show this in the old jetty only portlet */
                        } else {
                            renderRequest.setAttribute("statsSupported", Boolean.TRUE);
                            renderRequest.setAttribute("statsMessage", "Statistics are not currently being collected.");
                        }
                    } else {
                        renderRequest.setAttribute("statsSupported", Boolean.FALSE);
                        renderRequest.setAttribute("statsMessage", "Web statistics are not supported for the current web container.");
                    }
                } else {
                    log.error("Error attempting to retrieve the web containers");
                }
            } else {
                log.error("Error attempting to retrieve the web managers");
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/help.jsp");
    }

    public void destroy() {
        helpView = null;
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

}
