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
package org.apache.geronimo.console.car;

import java.io.IOException;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Handler for the import results screen.
 *
 * @version $Rev$ $Date$
 */
public class ResultsHandler extends BaseImportExportHandler {
    private static final Logger log = LoggerFactory.getLogger(ResultsHandler.class);

    public ResultsHandler() {
        super(RESULTS_MODE, "/WEB-INF/view/car/results.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        String[] configId = request.getParameterValues("configId");
        request.setAttribute("configId", configId);
        List deps = (List) request.getPortletSession(true).getAttribute("car.install.results");
        request.setAttribute("dependencies", deps);
        request.setAttribute("repository", repo);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] configId = request.getParameterValues("configId");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        response.setRenderParameter("repository", repo);
        if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        try {
            //todo: hide this in PortletManager/ManagementHelper
            for(int i=0; i<configId.length; i++) {
                ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(KernelRegistry.getSingleKernel());
                Artifact artifact = Artifact.create(configId[i]); 
                mgr.loadConfiguration(artifact);
                mgr.startConfiguration(artifact);
            }
            return INDEX_MODE;
            //return LIST_MODE;
        } catch (Exception e) {
            log.error("Unable to start configuration "+configId, e);
            response.setRenderParameter("configId", configId);
            return getMode()+BEFORE_ACTION;
        }
    }
}
