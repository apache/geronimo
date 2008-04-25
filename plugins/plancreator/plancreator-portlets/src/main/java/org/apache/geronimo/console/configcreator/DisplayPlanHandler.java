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
import java.net.URL;

import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class DisplayPlanHandler extends AbstractHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public DisplayPlanHandler() {
        super(DISPLAY_PLAN_MODE, "/WEB-INF/view/configcreator/displayPlan.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getSessionData(request);
        try {
            String plan = JSR88_Util.createDeploymentPlan(request, data, new URL(data.getUploadedWarUri()));
            data.setDeploymentPlan(plan);
        } catch (DDBeanCreateException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidModuleException e) {
            log.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (DeploymentManagerCreationException e) {
            log.error(e.getMessage(), e);
        }
        request.setAttribute(DATA_PARAMETER, data);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getSessionData(request);
        data.setDeploymentPlan(request.getParameter(DEPLOYMENT_PLAN_PARAMETER));
        return DEPLOY_STATUS_MODE;
    }
}
