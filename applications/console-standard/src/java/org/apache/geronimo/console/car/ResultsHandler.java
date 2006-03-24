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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

/**
 * Handler for the import results screen.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ResultsHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(ResultsHandler.class);

    public ResultsHandler() {
        super(RESULTS_MODE, "/WEB-INF/view/car/results.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        request.setAttribute("configId", configId);
        List configs = (List) request.getPortletSession(true).getAttribute("car.install.configurations");
        List deps = (List) request.getPortletSession(true).getAttribute("car.install.dependencies");
        request.setAttribute("dependencies", deps);
        request.setAttribute("configurations", configs);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        try {
            ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(KernelRegistry.getSingleKernel());
            List list = mgr.loadRecursive(new URI(configId));
            for (Iterator it = list.iterator(); it.hasNext();) {
                URI uri = (URI) it.next();
                mgr.loadGBeans(uri);
                mgr.start(uri);
            }
            return LIST_MODE+BEFORE_ACTION;
        } catch (Exception e) {
            log.error("Unable to start configuration "+configId, e);
            response.setRenderParameter("configId", configId);
            return getMode()+BEFORE_ACTION;
        }
    }
}
