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
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * Servlet that lets you download a CAR from the repository
 *
 * @version $Rev$ $Date$
 */
public class CARExportServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String configId = request.getParameter("configId");
        if(configId == null) {
            throw new ServletException("No configId specified for CAR download");
        }
        Artifact artifact = Artifact.create(configId);
        Kernel kernel = KernelRegistry.getSingleKernel();
        ConfigurationManager mgr = PortletManager.getConfigurationManager();
        ConfigurationStore store = mgr.getStoreForConfiguration(artifact);
        try {
            response.setContentType("application/zip");
            String filename = artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType();
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            store.exportConfiguration(artifact, response.getOutputStream());
        } catch (NoSuchConfigException e) {
            throw new ServletException("No such configuration '"+configId+"'");
        }
    }
}
