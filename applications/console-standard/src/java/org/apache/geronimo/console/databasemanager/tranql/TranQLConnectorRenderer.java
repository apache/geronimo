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

package org.apache.geronimo.console.databasemanager.tranql;

import java.io.IOException;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.databasemanager.DetailViewRenderer;
import org.apache.geronimo.kernel.Kernel;

public class TranQLConnectorRenderer implements DetailViewRenderer {
    private final Kernel kernel;

    private final PortletRequestDispatcher detailView;

    private final PortletRequestDispatcher configView;

    public TranQLConnectorRenderer(Kernel kernel, PortletContext context) {
        this.kernel = kernel;
        detailView = context
                .getRequestDispatcher("/WEB-INF/view/databasemanager/tranql/normal.jsp");
        configView = context
                .getRequestDispatcher("/WEB-INF/view/databasemanager/tranql/config.jsp");
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse, ObjectName gbeanName)
            throws PortletException, IOException {
        String password1 = actionRequest.getParameter("password1");
        if (!password1.equals(actionRequest.getParameter("password2"))) {
            actionResponse.setRenderParameter("badPassword", "true");
            actionResponse.setRenderParameter("mode", "config");
        } else {
            if (!"detail".equals(actionRequest.getParameter("mode"))) {
                try {
                    kernel.setAttribute(gbeanName, "UserName", actionRequest
                            .getParameter("UserName"));
                    kernel.setAttribute(gbeanName, "Password", password1);
                    kernel.setAttribute(gbeanName, "Driver", actionRequest
                            .getParameter("Driver"));
                    kernel.setAttribute(gbeanName, "ConnectionURL",
                            actionRequest.getParameter("ConnectionURL"));
                    kernel.setAttribute(gbeanName, "ExceptionSorterClass",
                            actionRequest.getParameter("ExceptionSorterClass"));
                } catch (Exception e) {
                    throw new PortletException(e);
                }
            }
            actionResponse.setRenderParameter("mode", "detail");
        }
        actionResponse.setRenderParameter("name", actionRequest
                .getParameter("name"));
    }

    public void render(RenderRequest request, RenderResponse response,
            ObjectName gbeanName) throws PortletException, IOException {
        request.setAttribute("badPassword", Boolean.valueOf(request
                .getParameter("badPassword")));
        TranQLInfo info = new TranQLInfo();
        try {
            info.setObjectName(gbeanName.toString());
            info.setName(gbeanName.getKeyProperty("name"));
            info.setDriver((String) kernel.getAttribute(gbeanName, "Driver"));
            info.setConnectionURL((String) kernel.getAttribute(gbeanName,
                    "ConnectionURL"));
            info.setUserName((String) kernel
                    .getAttribute(gbeanName, "UserName"));
            info.setExceptionSorterClass((String) kernel.getAttribute(
                    gbeanName, "ExceptionSorterClass"));
        } catch (Exception e) {
            throw new PortletException(e);
        }
        request.setAttribute("ds", info);
        if ("config".equals(request.getParameter("mode"))) {
            configView.include(request, response);
        } else {
            detailView.include(request, response);
        }
    }
}
