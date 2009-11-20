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
package org.apache.geronimo.welcome;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Stands in for servlets that are not yet installed, offering to install them.
 *
 * @version $Rev$ $Date$
 */
public class AbsentSampleServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String install = request.getParameter("install");
        if (install != null && !install.equals("")) {
            doInstall(request, response);
        } else {
            doMessage(request, response);
        }
    }

    private void doMessage(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/sampleNotInstalled.jsp");
        dispatcher.forward(request, response);
    }

    private void doInstall(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Kernel kernel = KernelRegistry.getSingleKernel();
        PluginInstaller installer = getPluginInstaller(kernel);
        String moduleIdName = getInitParameter("moduleId");
        moduleIdName = moduleIdName.replaceAll("SERVER", getServerType());
        URL repo = getFirstPluginRepository(kernel);
        PluginType target = new PluginType();
        target.setName("Sample Application");
        target.setCategory("Samples");
        target.setDescription("A sample application");
        PluginArtifactType instance = new PluginArtifactType();
        target.getPluginArtifact().add(instance);
        instance.getDependency().add(PluginInstallerGBean.toDependencyType(new Dependency(Artifact.create(moduleIdName), ImportType.ALL), true));
        PluginListType list = new PluginListType();
        list.getPlugin().add(target);
//        list.getDefaultRepository().add(repo.toString());
        //todo this is surely wrong
        list.getDefaultRepository().add("http://www.ibiblio.org/maven2/");
        DownloadResults results = installer.install(list, repo.toString(), false, null, null);
        if (results.isFailed()) {
            throw new ServletException("Unable to install sample application", results.getFailure());
        }
        ConfigurationManager mgr = null;
        try {
            mgr = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            throw new ServletException("Unable to locate configuration manager", e);
        }
        for (Artifact artifact: results.getInstalledConfigIDs()) {
            if (mgr.isConfiguration(artifact)) {
                try {
                    if (!mgr.isLoaded(artifact)) {
                        mgr.loadConfiguration(artifact);
                    }
                    if (!mgr.isRunning(artifact)) {
                        mgr.startConfiguration(artifact);
                    }
                } catch (NoSuchConfigException e) {
                    throw new ServletException("Unable to start sample application", e);
                } catch (LifecycleException e) {
                    throw new ServletException("Unable to start sample application", e);
                }
            }
        }
        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/");
    }

    private String getServerType() {
        return getServletContext().getServerInfo().toLowerCase().indexOf("jetty") > -1 ? "jetty" : "tomcat";
    }

    private PluginInstaller getPluginInstaller(Kernel kernel) throws ServletException {
        Set installers = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        if (installers.size() == 0) {
            throw new ServletException("Unable to install sample application; no plugin installer found");
        }
        try {
            return (PluginInstaller) kernel.getGBean((AbstractName) installers.iterator().next());
        } catch (GBeanNotFoundException e) {
            throw new ServletException("Unable to install sample application, plugin installer cannot be retrieved from kernel");
        }
    }

    private URL getFirstPluginRepository(Kernel kernel) throws ServletException {
        Set installers = kernel.listGBeans(new AbstractNameQuery(PluginRepositoryList.class.getName()));
        if (installers.size() == 0) {
            throw new ServletException("Unable to install sample application; no plugin repository list found");
        }
        PluginRepositoryList repos = ((PluginRepositoryList) kernel.getProxyManager().createProxy((AbstractName) installers.iterator().next(),
                PluginRepositoryList.class));

        List<URL> urls = repos.getRepositories();
        if (urls.isEmpty()) {
            repos.refresh();
            urls = repos.getRepositories();
            if (urls.isEmpty()) {
                throw new ServletException("Unable to install sample applicatoin; unable to download repository list");
            }
        }
        return urls.get(0);
    }
}
