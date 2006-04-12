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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
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
        ConfigurationInfo[] webApps = PortletManager.getConfigurations(request, ConfigurationModuleType.WAR, true);
        if(model.getWebApps().size() == 0) {
            List list = model.getWebApps();
            for (int i = 0; i < webApps.length; i++) {
                ConfigurationInfo app = webApps[i];
                if(!app.getState().isRunning()) {
                    continue;
                }
                AbstractName storeName = app.getStoreName();
                ConfigurationStore store = (ConfigurationStore) PortletManager.getManagedBean(request, storeName);
                WebModule web = (WebModule) PortletManager.getModule(request, app.getConfigID());
                WebAppData data = new WebAppData(app.getConfigID().toString(), false, null, false);
                data.setContextRoot(web.getContextPath());
                try {
                    String path = getPathToConfiguration(store, app.getParentID() == null ? app.getConfigID() : app.getParentID(), app.getParentID() == null ? null : app.getConfigID());
                    if(app.getParentID() == null) {
                        path = path + File.separator + "web";
                    } else {
                        path = path + File.separator + app.getConfigID();
                    }
                    data.setWebAppDir(path);
                } catch (NoSuchConfigException e) {
                    log.error("I sure didn't expect to get this exception", e);
                }
                list.add(data);
            }
        }
        request.setAttribute("webApps", webApps);
    }

    private String getPathToConfiguration(ConfigurationStore store, Artifact moduleOrParentID, Artifact childID) throws NoSuchConfigException {
        try {
            return store.resolve(moduleOrParentID, childID.toString(), new URI("")).getPath();
        } catch (MalformedURLException e) {
            log.error("Unable to locate path to web app "+moduleOrParentID+(childID == null ? "" : " / "+childID), e);
        } catch (URISyntaxException e) {
            log.error("Unable to locate path to web app "+moduleOrParentID+(childID == null ? "" : " / "+childID), e);
        }
        return "PATH_TO_EXPLODED_WAR_IN_REPO";
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel amodel) throws PortletException, IOException {
        return RESULTS_MODE+BEFORE_ACTION;
    }
}
