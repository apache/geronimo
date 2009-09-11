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

import javax.jms.TextMessage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class SendMessageHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(SendMessageHandler.class);

    public SendMessageHandler(BasePortlet portlet) {
        super(SEND_MESSAGE, "/WEB-INF/view/jmsmanager/sendMessage.jsp", portlet);
    }

    @Override
    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        String next = request.getParameter("nextAction");
        return next + BEFORE_ACTION;
    }

    @Override
    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        String adminObjName = request.getParameter(ADMIN_OBJ_NAME);
        String adminObjType = request.getParameter(ADMIN_OBJ_TYPE);
        String physicalName = request.getParameter(PHYSICAL_NAME);
        String adapterObjectName = request.getParameter(RA_ADAPTER_OBJ_NAME);
        String resourceAdapterModuleName = request.getParameter(RESOURCE_ADAPTER_MODULE_NAME);

        response.setRenderParameter(ADMIN_OBJ_NAME, adminObjName);
        response.setRenderParameter(ADMIN_OBJ_TYPE, adminObjType);
        response.setRenderParameter(PHYSICAL_NAME, physicalName);
        response.setRenderParameter(RA_ADAPTER_OBJ_NAME, adapterObjectName);
        response.setRenderParameter(RESOURCE_ADAPTER_MODULE_NAME, resourceAdapterModuleName);

        String submit = request.getParameter(SUBMIT);
        if (submit != null) {
            String correlationId = request.getParameter(CORRELATION_ID);
            String isPersistentStr = request.getParameter(IS_PERSISTENT);
            String priority = request.getParameter(PRIORITY);
            String jmsType = request.getParameter(JMS_TYPE);
            String message = request.getParameter(MESSAGE);

            response.setRenderParameter(SUBMIT, submit);
            if (correlationId != null)
                response.setRenderParameter(CORRELATION_ID, correlationId);
            if (isPersistentStr != null)
                response.setRenderParameter(IS_PERSISTENT, isPersistentStr);
            if (priority != null)
                response.setRenderParameter(PRIORITY, priority);
            if (jmsType != null)
                response.setRenderParameter(JMS_TYPE, jmsType);
            response.setRenderParameter(MESSAGE, message);
        }

        return getMode();
    }

    @Override
    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {

        String adminObjName = request.getParameter(ADMIN_OBJ_NAME);
        String adminObjType = request.getParameter(ADMIN_OBJ_TYPE);
        String physicalName = request.getParameter(PHYSICAL_NAME);
        String adapterObjectName = request.getParameter(RA_ADAPTER_OBJ_NAME);
        String submit = request.getParameter(SUBMIT);
        String resourceAdapterModuleName = request.getParameter(RESOURCE_ADAPTER_MODULE_NAME);

        request.setAttribute(ADMIN_OBJ_NAME, adminObjName);
        request.setAttribute(ADMIN_OBJ_TYPE, adminObjType);
        request.setAttribute(PHYSICAL_NAME, physicalName);
        request.setAttribute(RA_ADAPTER_OBJ_NAME, adapterObjectName);
        request.setAttribute(RESOURCE_ADAPTER_MODULE_NAME, resourceAdapterModuleName);

        if (submit != null) {
            String correlationId = request.getParameter(CORRELATION_ID);
            String isPersistentStr = request.getParameter(IS_PERSISTENT);
            boolean isPersistent = isPersistentStr != null ? isPersistentStr.equals("on") ? true : false : false;

            String priority = request.getParameter(PRIORITY);
            String jmsType = request.getParameter(JMS_TYPE);
            String message = request.getParameter(MESSAGE);

            JMSMessageInfo messageInfo = new JMSMessageInfo();            
            messageInfo.setCorrelationId(correlationId);
            messageInfo.setPersistent(isPersistent);

            if (priority != null) {
                messageInfo.setPriority(Integer.parseInt(priority));
            }else{
                messageInfo.setPriority(TextMessage.DEFAULT_PRIORITY);
            }
            messageInfo.setJmsType(jmsType);
            messageInfo.setMessage(message);

            JMSMessageHelper helper = JMSMessageHelperFactory.getMessageHelper(request, resourceAdapterModuleName);
            try {
                helper.sendMessage(request, JMSDestinationInfo.create(request), messageInfo);
                portlet.addInfoMessage(request, portlet.getLocalizedString(request, "activemq.infoMsg01"));
            } catch (Exception e) {
                portlet.addErrorMessage(request, portlet.getLocalizedString(request, "activemq.errorMsg01"), e.getMessage());
                log.error("Error encountered while sending message.", e);
                // throw new PortletException(e);
            }
        }
    }

}
