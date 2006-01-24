/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Handler for the screen that configures a resource adapter instance.
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class ConfigureRAInstanceHandler extends AbstractHandler {
    public ConfigureRAInstanceHandler() {
        super(CONFIGURE_RA_MODE, "/WEB-INF/view/jmswizard/raInstance.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, JMSResourceData data) throws PortletException, IOException {
        JMSProviderData provider = JMSProviderData.getProviderData(data.getRarURI(), request);
        request.setAttribute("provider", provider);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        if(isEmpty(data.getInstanceName())) {
            return getMode()+BEFORE_ACTION;
        }
        String next = request.getParameter("nextAction");
        if(next.equals(SELECT_DESTINATION_TYPE_MODE)) {
            data.setCurrentDestinationID(data.getAdminObjects().size());
        } else if(next.equals(SELECT_FACTORY_TYPE_MODE)) {
            data.setCurrentFactoryID(data.getConnectionFactories().size());
        }
        return next+BEFORE_ACTION;
    }
}
