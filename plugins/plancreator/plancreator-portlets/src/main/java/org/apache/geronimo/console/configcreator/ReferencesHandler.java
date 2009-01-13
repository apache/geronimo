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
package org.apache.geronimo.console.configcreator;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.configcreator.configData.WARConfigData;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class ReferencesHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(ReferencesHandler.class);

    public ReferencesHandler(BasePortlet portlet) {
        super(REFERENCES_MODE, "/WEB-INF/view/configcreator/references.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getWARSessionData(request);
        request.setAttribute(DATA_PARAMETER, data);
        request.setAttribute(DEPLOYED_EJBS_PARAMETER, JSR77_Util.getDeployedEJBs(request));
        request.setAttribute(DEPLOYED_JDBC_CONNECTION_POOLS_PARAMETER, JSR77_Util.getJDBCConnectionPools(request));
        request.setAttribute(DEPLOYED_JMS_CONNECTION_FACTORIES_PARAMETER, JSR77_Util.getJMSConnectionFactories(request));
        request.setAttribute(DEPLOYED_JMS_DESTINATIONS_PARAMETER, JSR77_Util.getJMSDestinations(request));
        request.setAttribute(DEPLOYED_JAVAMAIL_SESSIONS_PARAMETER, JSR77_Util.getJavaMailSessions(request));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getWARSessionData(request);
        data.readReferencesData(request);
        if (data.isReferenceNotResolved()) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "errorMsg03"));
            return getMode() + "-before";
        }
        if (data.getSecurity() != null) {
            return SECURITY_MODE + "-before";
        }
        return DEPENDENCIES_MODE + "-before";
    }
}
