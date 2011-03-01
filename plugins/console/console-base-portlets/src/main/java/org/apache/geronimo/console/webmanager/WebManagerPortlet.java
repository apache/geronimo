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
import org.apache.geronimo.console.webmanager.TimeUtils;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.stats.WebContainerStats;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.LazyStatisticsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(WebManagerPortlet.class);

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
                    if (actionRequest.getParameter("stats") != null) {
                        Boolean stats = actionRequest.getParameter("stats").equals("true") ? Boolean.TRUE : Boolean.FALSE;
                        if(server.equals(WEB_SERVER_JETTY)) {
                            setProperty(container, "statsOn", stats);
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
                            callOperation(container, "resetStats", null);
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
                        boolean populateStats = false;
                        renderRequest.setAttribute("statsSupported", Boolean.TRUE);  // indicate that statistics are supported for this container

                        if (container instanceof LazyStatisticsProvider) {
                            renderRequest.setAttribute("statsLazy", Boolean.TRUE);   // indicate that enable/disable should be shown for this container

                            if (((LazyStatisticsProvider)container).isStatsOn()) {
                                renderRequest.setAttribute("statsOn", Boolean.TRUE); // indicate that stats are to be displayed
                                populateStats = true;      // this is a Lazy provider and stats are enabled so populate the stats
                            } else {
                                renderRequest.setAttribute("statsOn", Boolean.FALSE);  // indicate that stats are currently disabled
                                addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.warnMsg04"));
                            }
                        } else {
                            renderRequest.setAttribute("statsLazy", Boolean.FALSE);  // indicate that enable/disable should not be shown for this container
                            renderRequest.setAttribute("statsOn", Boolean.TRUE);     // indicate that stats are to be displayed
                            populateStats=true;     // this is not a lazy provider so just populate the stats
                        }

                        if (populateStats) {
                            // get the detailed stats
                            WebContainerStats webStats = (WebContainerStats) ((StatisticsProvider)container).getStats();
                            //renderRequest.setAttribute("totalRequestCount", new Long(webStats.getTotalRequestCount().getCount()));
                            renderRequest.setAttribute("activeRequestCountCurrent", new Long(webStats.getActiveRequestCount().getCurrent()));
                            renderRequest.setAttribute("activeRequestCountLow", new Long(webStats.getActiveRequestCount().getLowWaterMark()));
                            renderRequest.setAttribute("activeRequestCountHigh", new Long(webStats.getActiveRequestCount().getHighWaterMark()));

                            Long count = new Long(webStats.getRequestDuration().getCount());
                            Long totalTime = new Long(webStats.getRequestDuration().getTotalTime());
                            renderRequest.setAttribute("totalRequestCount", count);
                            renderRequest.setAttribute("requestDurationMinTime", new Long(webStats.getRequestDuration().getMinTime()));
                            renderRequest.setAttribute("requestDurationMaxTime", new Long(webStats.getRequestDuration().getMaxTime()));
                            renderRequest.setAttribute("requestDurationTotalTime", totalTime);
                            // renderRequest.setAttribute("requestDurationAvg", new Long(webStats.getRequestDurationAvg().getCount()));
                            long avg = count == 0 ? 0 : totalTime / count;
                            renderRequest.setAttribute("requestDurationAvg", avg);
                            renderRequest.setAttribute("response1xx", Long.valueOf(webStats.getResponses1xx().getCount()));
                            renderRequest.setAttribute("response2xx", Long.valueOf(webStats.getResponses2xx().getCount()));
                            renderRequest.setAttribute("response3xx", Long.valueOf(webStats.getResponses3xx().getCount()));
                            renderRequest.setAttribute("response4xx", Long.valueOf(webStats.getResponses4xx().getCount()));
                            renderRequest.setAttribute("response5xx", Long.valueOf(webStats.getResponses5xx().getCount()));
                            renderRequest.setAttribute("elapsedTime", TimeUtils.formatDuration(webStats.getStatsOnMs().getCount()));
                        }
                    } else {
                        renderRequest.setAttribute("statsSupported", Boolean.FALSE);   // indicate that statistics are not supported for this container
                        addWarningMessage(renderRequest, getLocalizedString(renderRequest, "consolebase.warnMsg03"));
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
