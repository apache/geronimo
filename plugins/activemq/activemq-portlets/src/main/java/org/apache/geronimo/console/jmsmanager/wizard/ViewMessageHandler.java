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

package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.jmsmanager.JMSDestinationInfo;
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelper;
import org.apache.geronimo.console.jmsmanager.helper.JMSMessageHelperFactory;

/**
 * @version $Rev$ $Date$
 */
public class ViewMessageHandler extends AbstractHandler {

    public ViewMessageHandler() {
        super(VIEW_MESSAGES, "/WEB-INF/view/jmsmanager/viewmessages.jsp");
    }

    public ViewMessageHandler(BasePortlet basePortlet) {
        super(VIEW_MESSAGES, "/WEB-INF/view/jmsmanager/viewmessages.jsp", basePortlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        String adminObjName = request.getParameter(ADMIN_OBJ_NAME);
        String adminObjType = request.getParameter(ADMIN_OBJ_TYPE);
        String physicalName = request.getParameter(PHYSICAL_NAME);
        String adapterObjectName = request.getParameter(RA_ADAPTER_OBJ_NAME);
        String resourceAdapterModuleName = request.getParameter(RESOURCE_ADAPTER_MODULE_NAME);

        if (adminObjName == null) {
            // Takes the values from session.This is required for back functionality
            adminObjName = (String) request.getPortletSession(true).getAttribute(ADMIN_OBJ_NAME);
            adminObjType = (String) request.getPortletSession(true).getAttribute(ADMIN_OBJ_TYPE);
            physicalName = (String) request.getPortletSession(true).getAttribute(PHYSICAL_NAME);
            adapterObjectName = (String) request.getPortletSession(true).getAttribute(RA_ADAPTER_OBJ_NAME);
            resourceAdapterModuleName = (String) request.getPortletSession(true).getAttribute(RESOURCE_ADAPTER_MODULE_NAME);
        } else {
            // Store the values to session
            request.getPortletSession(true).setAttribute(ADMIN_OBJ_NAME, adminObjName);
            request.getPortletSession(true).setAttribute(ADMIN_OBJ_TYPE, adminObjType);
            request.getPortletSession(true).setAttribute(PHYSICAL_NAME, physicalName);
            request.getPortletSession(true).setAttribute(RA_ADAPTER_OBJ_NAME, adapterObjectName);
            request.getPortletSession(true).setAttribute(RESOURCE_ADAPTER_MODULE_NAME,resourceAdapterModuleName);
        }
        response.setRenderParameter(ADMIN_OBJ_NAME, adminObjName);
        response.setRenderParameter(ADMIN_OBJ_TYPE, adminObjType);
        response.setRenderParameter(PHYSICAL_NAME, physicalName);
        response.setRenderParameter(RA_ADAPTER_OBJ_NAME, adapterObjectName);
        response.setRenderParameter(RESOURCE_ADAPTER_MODULE_NAME, resourceAdapterModuleName);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {        
        String resourceAdapterModuleName = request.getParameter(RESOURCE_ADAPTER_MODULE_NAME);
        JMSMessageHelper helper = JMSMessageHelperFactory.getMessageHelper(request, resourceAdapterModuleName);
        JMSMessageInfo[] messages;
        try {
            JMSDestinationInfo jmsDestinationInfo = JMSDestinationInfo.create(request);
            messages = helper.getMessagesList(request, jmsDestinationInfo, null);
            request.setAttribute(MESSAGES, messages);
            request.getPortletSession(true).setAttribute(MESSAGES, messages);
        } catch (Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "activemq.errorMsg03"), e.getMessage());
            request.setAttribute(MESSAGES, new JMSMessageInfo[0]);
            request.getPortletSession(true).setAttribute(MESSAGES, new JMSMessageInfo[0]);
        }        
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        String next = request.getParameter("nextAction");
        return next + BEFORE_ACTION;
    }
}
