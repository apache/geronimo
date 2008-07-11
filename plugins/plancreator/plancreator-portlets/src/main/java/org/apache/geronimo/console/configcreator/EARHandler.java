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
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.configcreator.configData.EARConfigData;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class EARHandler extends AbstractHandler {
    //private static final Log log = LogFactory.getLog(EARHandler.class);

    public EARHandler() {
        super(EAR_MODE, "/WEB-INF/view/configcreator/enterpriseApp.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        EARConfigData data = getEARSessionData(request);
        request.setAttribute(DATA_PARAMETER, data);
        List<String> commonLibs = JSR77_Util.getCommonLibs(request);
        request.setAttribute(COMMON_LIBS_PARAMETER, commonLibs);
        request.setAttribute(DEPLOYED_SECURITY_REALMS_PARAMETER, JSR77_Util.getDeployedSecurityRealms(request));
        request.setAttribute(DEPLOYED_CREDENTIAL_STORES_PARAMETER, JSR77_Util.getDeployedCredentialStores(request));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return "";
    }
}
