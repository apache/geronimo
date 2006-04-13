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
package org.apache.geronimo.console.apache.jk;

import java.io.IOException;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.ConfigurationData;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.management.geronimo.WebModule;

/**
 * Handler for the screen where you select the webapps to expose through Apache
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class WebAppHandler extends BaseApacheHandler {
    private final static Log log = LogFactory.getLog(WebAppHandler.class);

    public WebAppHandler() {
        super(WEB_APP_MODE, "/WEB-INF/view/apache/jk/webApps.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel amodel) throws PortletException, IOException {
        ApacheModel model = (ApacheModel) amodel;
        ConfigurationData[] webApps = PortletManager.getConfigurations(request, ConfigurationModuleType.WAR, true);
        if(model.getWebApps().size() == 0) {
            List list = model.getWebApps();
            for (int i = 0; i < webApps.length; i++) {
                ConfigurationData app = webApps[i];
log.error("*********** "+app.getType()+": "+app.getParentName()+" / "+app.getChildName());
                if(!app.getState().isRunning()) {
                    continue;
                }
                WebModule web = (WebModule) PortletManager.getManagedBean(request, app.getModuleBeanName());
                WebAppData data = new WebAppData(app.getParentName().toString(), app.getChildName(), app.getModuleBeanName().toString(), false, null, false);
                data.setContextRoot(web.getContextPath());
                String path;
                if(web.getWARDirectory().getProtocol().equals("file")) {
                    path = web.getWARDirectory().getPath();
                } else {
                    path = "WARMustBeUnpacked";
                }

                data.setWebAppDir(path);
                list.add(data);
            }
        }
        request.setAttribute("webApps", webApps);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel amodel) throws PortletException, IOException {
        return RESULTS_MODE+BEFORE_ACTION;
    }
}
