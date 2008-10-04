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

import org.apache.geronimo.console.MultiPageModel;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Handler for the screen that creates a new connection factory
 *
 * @version $Rev$ $Date$
 */
public class CreateConnectionFactoryHandler extends AbstractHandler {
    public CreateConnectionFactoryHandler() {
        super(ADD_FACTORY_MODE, "/WEB-INF/view/jmswizard/factory.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        data.getCurrentFactory().setFactoryType(data.getFactoryType());
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        JMSProviderData provider = JMSProviderData.getProviderData(data.getRarURI(), request);
        request.setAttribute("provider", provider);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        if(isEmpty(data.getCurrentFactory().getInstanceName())) {
            return getMode();
        }
        if(data.getCurrentFactory().getTransaction().equals("xa")) {
            data.getCurrentFactory().setXaThreadCaching(false);
            data.getCurrentFactory().setXaTransactionCaching(true);
        } else {
            data.getCurrentFactory().setXaThreadCaching(false);
            data.getCurrentFactory().setXaTransactionCaching(false);
        }
        // todo: process interface parameters
        String next = request.getParameter("nextAction");
        if(next.equals(SELECT_DESTINATION_TYPE_MODE)) {
            data.setCurrentDestinationID(data.getAdminObjects().size());
        } else if(next.equals(SELECT_FACTORY_TYPE_MODE)) {
            data.setCurrentFactoryID(data.getConnectionFactories().size());
        }
        return next+BEFORE_ACTION;
    }
}
