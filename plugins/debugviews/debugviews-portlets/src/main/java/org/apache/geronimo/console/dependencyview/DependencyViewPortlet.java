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
package org.apache.geronimo.console.dependencyview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.console.util.StringTree;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;

public class DependencyViewPortlet extends BasePortlet {

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/dependencyview/view.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/dependencyview/view.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/dependencyview/help.jsp";

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        renderRequest.getPortletSession().setAttribute("dependencyTree",
                getJSONTrees(renderRequest));

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);

    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

    private static ArrayList parentNodes = new ArrayList();

    public static String getJSONTrees(RenderRequest renderRequest) {
        List list = getTrees(renderRequest);
        if (list == null)
            return "[]";

        StringBuffer stb = new StringBuffer();
        stb.append("[");
        for (int i = 0; i < list.size(); i++) {
            StringTree node = (StringTree) list.get(i);
            if (i != 0)
                stb.append(",");
            stb.append(node.toJSONObject("" + i));
        }
        stb.append("]");
        return stb.toString();
    }

    public static void addDependencies(StringTree curr, Configuration conf) {
        if (curr == null || conf == null)
            return;
        StringTree dep = new StringTree("dependencies");
        curr.addChild(dep);
        for (Iterator iterator = conf.getDependencies().iterator(); iterator
                .hasNext();) {
            dep.addChild(iterator.next().toString());
        }
        for (Iterator iterator = conf.getServiceParents().iterator(); iterator
                .hasNext();) {
            Configuration config = (Configuration) iterator.next();
            dep.addChild(config.getId().toString());
        }
    }

    public static ArrayList getTrees(RenderRequest request) {

        ArrayList arryList = new ArrayList();
        StringTree treeEAR = new StringTree("Enterprise Applications");
        arryList.add(treeEAR);

        StringTree treeEJB = new StringTree("EJBModule");
        arryList.add(treeEJB);

        StringTree treeWeb = new StringTree("WebModule");
        arryList.add(treeWeb);

        StringTree treeRAR = new StringTree("ResourceAdapterModule");
        arryList.add(treeRAR);

        StringTree treeCLI = new StringTree("AppClientModule");
        arryList.add(treeCLI);

        StringTree treeSys = new StringTree("System Module");
        arryList.add(treeSys);

        org.apache.geronimo.kernel.Kernel kernel = org.apache.geronimo.kernel.KernelRegistry
                .getSingleKernel();

        ConfigurationManager configManager = ConfigurationUtil
                .getConfigurationManager(kernel);

        List infos = configManager.listConfigurations();
        for (Iterator infoIterator = infos.iterator(); infoIterator.hasNext();) {
            ConfigurationInfo info = (ConfigurationInfo) infoIterator.next();
            Configuration conf = configManager.getConfiguration(info
                    .getConfigID());
            if (conf != null) {
                StringTree curr = new StringTree(info.getConfigID().toString());
                switch (info.getType().getValue()) {
                case 0:// EAR
                {
                    treeEAR.addChild(curr);
                    break;
                }
                case 1:// EJB
                {
                    treeEJB.addChild(curr);
                    break;
                }
                case 2:// CAR
                {
                    treeCLI.addChild(curr);
                    break;
                }

                case 3:// RAR
                {
                    treeRAR.addChild(curr);
                    break;
                }
                case 4:// WAR
                {
                    treeWeb.addChild(curr);
                    break;
                }
                case 5:// SERVICE
                {
                    treeSys.addChild(curr);
                    break;
                }
                }

                addDependencies(curr, conf);

                if (info.getType().getValue() == ConfigurationModuleType.EAR.getValue()) {
                    StringTree nodeEJB = new StringTree("EJBModule");
                    curr.addChild(nodeEJB);

                    StringTree nodeWeb = new StringTree("WebModule");
                    curr.addChild(nodeWeb);

                    StringTree nodeRAR = new StringTree("ResourceAdapterModule");
                    curr.addChild(nodeRAR);

                    StringTree nodeCLI = new StringTree("AppClientModule");
                    curr.addChild(nodeCLI);

                    Map<String, String> query = new HashMap<String, String>();
                    query.put("j2eeType", "EJBModule");
                    query.put("J2EEApplication", info.getConfigID().toString());
                    Set<AbstractName> setEnt = kernel.listGBeans(new AbstractNameQuery(null, query));
                    for (AbstractName gb : setEnt) {
                        StringTree subCurr = new StringTree(info.getConfigID().getGroupId()
                                + "/"
                                + info.getConfigID().getArtifactId()
                                + "_"
                                + gb.getNameProperty("name")
                                + "/"
                                + info.getConfigID().getVersion()
                                + "/"
                                + info.getConfigID().getType());
                        nodeEJB.addChild(subCurr);
                        addDependencies(subCurr, configManager
                                .getConfiguration(gb.getArtifact()));
                    }

                    Map<String, String> query1 = new HashMap<String, String>();
                    query1.put("j2eeType", "ResourceAdapterModule");
                    query1.put("J2EEApplication", info.getConfigID().toString());
                    Set<AbstractName> setEnt1 = kernel.listGBeans(new AbstractNameQuery(null, query1));

                    for (AbstractName gb : setEnt1) {
                        StringTree subCurr = new StringTree(info.getConfigID().getGroupId()
                                + "/"
                                + info.getConfigID().getArtifactId()
                                + "_"
                                + gb.getNameProperty("name")
                                + "/"
                                + info.getConfigID().getVersion()
                                + "/"
                                + info.getConfigID().getType());
                        nodeRAR.addChild(subCurr);
                        addDependencies(subCurr, configManager.getConfiguration(gb.getArtifact()));
                    }

                    for (Configuration config: conf.getChildren()) {
                        StringTree subCurr = new StringTree(config.getAbstractName().toString());
                        nodeWeb.addChild(subCurr);
                        addDependencies(subCurr, config);
                    }

                    for (Artifact name : conf.getOwnedConfigurations()) {
                        StringTree subCurr = new StringTree(name.toString());
                        nodeCLI.addChild(subCurr);
                        addDependencies(subCurr, configManager.getConfiguration(name));
                    }

                }

            }

        }

        StringTree treeRepo = new StringTree("Repository");
        arryList.add(treeRepo);

        ListableRepository[] repos = PortletManager.getCurrentServer(request)
                .getRepositories();
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            final SortedSet artifacts = repo.list();
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                String fileName = iterator.next().toString();
                treeRepo.addChild(fileName);
            }

        }

        return arryList;

    }

}
