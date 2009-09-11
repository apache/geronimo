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
import org.apache.geronimo.console.jmsmanager.JMSMessageInfo;

/**
 * @version $Rev$ $Date$
 */
public class MessageDetailsHandler extends AbstractHandler {

    public MessageDetailsHandler() {
        super(MESSAGE_DETAILS, "/WEB-INF/view/jmsmanager/messageDetails.jsp");
    }

    public MessageDetailsHandler(BasePortlet basePortlet) {
        super(MESSAGE_DETAILS, "/WEB-INF/view/jmsmanager/messageDetails.jsp", basePortlet);
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
        String messageId = request.getParameter(MESSAGE_ID);
        response.setRenderParameter(MESSAGE_ID, messageId);
        return getMode();
    }

    @Override
    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String messageId = request.getParameter(MESSAGE_ID);
        JMSMessageInfo[] messages = (JMSMessageInfo[]) request.getPortletSession(true).getAttribute(MESSAGES);
        for (JMSMessageInfo message : messages) {
            if (message.getMessageID().equals(messageId)) {
                request.setAttribute(MESSAGE_TXT, message.getMessage());
            }
        }
    }

}
