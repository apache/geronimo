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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
public class DeployStatusHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(DeployStatusHandler.class);

    public DeployStatusHandler(BasePortlet portlet) {
        super(DEPLOY_STATUS_MODE, "/WEB-INF/view/configcreator/deployStatus.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getWARSessionData(request);
        try {
            File moduleFile = new File(new URI(data.getUploadedWarUri()));

            File planFile = File.createTempFile("console-deployment", ".xml");
            planFile.deleteOnExit();
            FileWriter out = new FileWriter(planFile);
            out.write(data.getDeploymentPlan());
            out.close();

            String[] status = JSR88_Util.deploy(request, moduleFile, planFile);
            if (null != status[1] && 0 != status[1].length()) {
                portlet.addErrorMessage(request, portlet.getLocalizedString(request, status[0]), status[1]);
            }
            else {
                portlet.addInfoMessage(request, portlet.getLocalizedString(request, status[0]));
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        request.setAttribute(DATA_PARAMETER, data);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return "";
    }
}
