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

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.logging.SystemLog;

public class LogManagerPortlet extends BasePortlet {

    protected PortletRequestDispatcher normalView;

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
        SystemLog log = PortletManager.getCurrentSystemLog(renderRequest);
//        renderRequest.setAttribute("configFile", log.getConfigFileName());
//        renderRequest.setAttribute("configuration", LogHelper.getConfiguration());
        renderRequest.setAttribute("logLevel", log.getRootLoggerLevel());
//        try{
//            renderRequest.setAttribute("refreshPeriod", Integer.valueOf(log.getRefreshPeriodSeconds()));
//        }catch(NumberFormatException e){
//            //ignore
//        }

        normalView.include(renderRequest, renderRespose);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc.getRequestDispatcher("/WEB-INF/view/logmanager/view.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/logmanager/help.jsp");
        super.init(portletConfig);
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        SystemLog log = PortletManager.getCurrentSystemLog(actionRequest);

        String action = actionRequest.getParameter("action");
        String logLevel = actionRequest.getParameter("logLevel");
        String configFile = actionRequest.getParameter("configFile");
        //String configuration = actionRequest.getParameter("append");
        String refreshPeriod = actionRequest.getParameter("refreshPeriod");
        String currentLevel = log.getRootLoggerLevel();

        if ("update".equals(action)) {
            if (refreshPeriod != null) {
                int refreshPeriodInt = 0;
                try{
                    refreshPeriodInt = Integer.parseInt(refreshPeriod);
                }catch(NumberFormatException e){
                    //ignore
                }
//                if (refreshPeriodInt != log.getRefreshPeriodSeconds()) {
//                    log.setRefreshPeriodSeconds(refreshPeriodInt);
//                }
            }
//            if (!log.getConfigFileName().equals(configFile)) {
//                log.setConfigFileName(configFile);
//            }
            if (!currentLevel.equals(logLevel)) {
                log.setRootLoggerLevel(logLevel);
            }
        }
    }
}
