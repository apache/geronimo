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
package org.apache.geronimo.console.car;

import org.apache.geronimo.console.keystores.BaseKeystoreHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.security.keystore.KeystoreManager;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ListHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(ListHandler.class);

    public ListHandler() {
        super(LIST_MODE, "/WEB-INF/view/car/list.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = (String) request.getAttribute("repository");
        if(repository == null || repository.equals("")) {
            return INDEX_MODE+BEFORE_ACTION;
        }
        response.setRenderParameter("repository", repository);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = request.getParameter("repository");
        loadFromRepository(request, repository);
        request.setAttribute("repository", repository);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

    private void loadFromRepository(RenderRequest request, String repository) throws IOException {
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        URL url = new URL(repository+"geronimo-configurations.properties");
        Set set = new HashSet();
        ConfigurationInfo[] installed = PortletManager.getConfigurations(request, null, false);
        for (int i = 0; i < installed.length; i++) {
            ConfigurationInfo info = installed[i];
            set.add(info.getConfigID().toString());
        }
        InputStream in = url.openStream();
        Properties props = new Properties();
        props.load(in);
        in.close();
        Map results = new HashMap();
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            int pos = key.indexOf('.');
            String type = key.substring(0, pos);
            List values = (List) results.get(type);
            if(values == null) {
                values = new ArrayList();
                results.put(type, values);
            }
            String configId = key.substring(pos + 1);
            values.add(new RepositoryEntry(configId, props.getProperty(key), set.contains(configId)));
        }
        request.setAttribute("categories", results);
    }

    public static class RepositoryEntry implements Serializable {
        private String configId;
        private String name;
        private boolean installed;
        private String version;

        public RepositoryEntry(String configId, String name, boolean installed) {
            this.configId = configId;
            this.name = name;
            this.installed = installed;
            String[] parts = configId.split("/");
            version = parts[2];
        }

        public String getConfigId() {
            return configId;
        }

        public String getName() {
            return name;
        }

        public boolean isInstalled() {
            return installed;
        }

        public String getVersion() {
            return version;
        }
    }
}
