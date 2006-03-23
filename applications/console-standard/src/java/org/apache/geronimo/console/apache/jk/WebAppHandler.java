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

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.net.URI;

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
                ObjectName base = app.getStoreName();
                WebAppData data = new WebAppData(app.getConfigID().toString(), false, null, false);
                try {
                    ObjectName module = ObjectName.getInstance(base.getDomain()+":J2EEServer="+base.getKeyProperty("J2EEServer")+",J2EEApplication="+app.getParentID()+",j2eeType=WebModule,name="+app.getConfigID());
                    WebModule web = (WebModule) PortletManager.getManagedBean(request, module.getCanonicalName());
                    data.setContextRoot(web.getContextPath());
                    ConfigurationStore store = (ConfigurationStore) PortletManager.getManagedBean(request, app.getStoreName().getCanonicalName());
                    String path = getPathToConfiguration(store, app.getParentID() == null ? app.getConfigID() : app.getParentID());
                    if(app.getParentID() == null) {
                        path = path + File.separator + "web";
                    } else {
                        path = path + File.separator + app.getConfigID();
                    }
                    data.setWebAppDir(path);
                } catch (MalformedObjectNameException e) {
                    log.error("I sure didn't expect to get this exception", e);
                } catch (NoSuchConfigException e) {
                    log.error("I sure didn't expect to get this exception", e);
                }
                list.add(data);
            }
        }
        request.setAttribute("webApps", webApps);
    }

    private String getPathToConfiguration(ConfigurationStore store, URI moduleOrParentID) throws NoSuchConfigException {
        return "PATH_IN_CONFIG_STORE"; // todo: replace this with code to actually look up the path to the module
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel amodel) throws PortletException, IOException {
        return RESULTS_MODE+BEFORE_ACTION;
    }
}
