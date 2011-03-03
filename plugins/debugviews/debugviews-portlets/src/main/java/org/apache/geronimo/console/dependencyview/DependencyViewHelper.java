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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.servlet.http.HttpSession;

import org.apache.geronimo.console.util.Tree;
import org.apache.geronimo.console.util.TreeEntry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RemoteProxy
public class DependencyViewHelper {

    private static final String NO_CHILD = "none";
    
    private static final String NOT_LEAF_TYPE = "not_leaf";
    
    private static final String NORMAL_TYPE = "normal";
    
    private static final Logger logger = LoggerFactory.getLogger(DependencyViewHelper.class);

    public static void addDependencies(TreeEntry curr, Configuration conf) {
        if (curr == null || conf == null)
            return;
        TreeEntry dep = new TreeEntry("dependencies", NOT_LEAF_TYPE);
        curr.addChild(dep);
        for (Iterator<Dependency> iterator = conf.getEnvironment().getDependencies().iterator(); iterator.hasNext();) {
            dep.addChild(new TreeEntry(iterator.next().getArtifact().toString(), NORMAL_TYPE));
        }
        for (Iterator<Artifact> iterator = conf.getDependencyNode().getServiceParents().iterator(); iterator.hasNext();) {
            Artifact artifact = iterator.next();
            dep.addChild(new TreeEntry(artifact.toString(), NORMAL_TYPE));
        }
    }

    @RemoteMethod
    public static Tree getTrees(HttpSession session) {
        Tree dependencyTree = new Tree(null, "name");

        TreeEntry treeEAR = new TreeEntry("Enterprise Applications", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeEAR);

        TreeEntry treeEJB = new TreeEntry("EJBModule", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeEJB);

        TreeEntry treeWeb = new TreeEntry("WebModule", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeWeb);

        TreeEntry treeRAR = new TreeEntry("ResourceAdapterModule", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeRAR);

        TreeEntry treeCLI = new TreeEntry("AppClientModule", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeCLI);

        TreeEntry treeSys = new TreeEntry("System Module", NOT_LEAF_TYPE);
        dependencyTree.addItem(treeSys);

        org.apache.geronimo.kernel.Kernel kernel = org.apache.geronimo.kernel.KernelRegistry.getSingleKernel();

        ConfigurationManager configManager = null;
        try {
        	configManager = ConfigurationUtil.getConfigurationManager(kernel);
        } catch	(GBeanNotFoundException e) {
        	// Ignore
        }

        List infos = configManager.listConfigurations();
        for (Iterator infoIterator = infos.iterator(); infoIterator.hasNext();) {
            ConfigurationInfo info = (ConfigurationInfo) infoIterator.next();
            Configuration conf = configManager.getConfiguration(info.getConfigID());
            if (conf != null) {
                TreeEntry curr = new TreeEntry(info.getConfigID().toString(), NORMAL_TYPE);
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
                    TreeEntry nodeEJB = new TreeEntry("EJBModule", NOT_LEAF_TYPE);
                    curr.addChild(nodeEJB);

                    TreeEntry nodeWeb = new TreeEntry("WebModule", NOT_LEAF_TYPE);
                    curr.addChild(nodeWeb);

                    TreeEntry nodeRAR = new TreeEntry("ResourceAdapterModule", NOT_LEAF_TYPE);
                    curr.addChild(nodeRAR);

                    TreeEntry nodeCLI = new TreeEntry("AppClientModule", NOT_LEAF_TYPE);
                    curr.addChild(nodeCLI);

                    Map<String, String> query = new HashMap<String, String>();
                    query.put("j2eeType", "EJBModule");
                    query.put("J2EEApplication", info.getConfigID().toString());
                    Set<AbstractName> setEnt = kernel.listGBeans(new AbstractNameQuery(null, query));
                    for (AbstractName gb : setEnt) {
                        TreeEntry subCurr = new TreeEntry(info.getConfigID().getGroupId() + "/"
                                + info.getConfigID().getArtifactId() + "_" + gb.getNameProperty("name") + "/"
                                + info.getConfigID().getVersion() + "/" + info.getConfigID().getType(), NORMAL_TYPE);
                        nodeEJB.addChild(subCurr);
                        addDependencies(subCurr, configManager.getConfiguration(gb.getArtifact()));
                    }

                    Map<String, String> query1 = new HashMap<String, String>();
                    query1.put("j2eeType", "ResourceAdapterModule");
                    query1.put("J2EEApplication", info.getConfigID().toString());
                    Set<AbstractName> setEnt1 = kernel.listGBeans(new AbstractNameQuery(null, query1));

                    for (AbstractName gb : setEnt1) {
                        TreeEntry subCurr = new TreeEntry(info.getConfigID().getGroupId() + "/"
                                + info.getConfigID().getArtifactId() + "_" + gb.getNameProperty("name") + "/"
                                + info.getConfigID().getVersion() + "/" + info.getConfigID().getType(), NORMAL_TYPE);
                        nodeRAR.addChild(subCurr);
                        addDependencies(subCurr, configManager.getConfiguration(gb.getArtifact()));
                    }

                    for (Configuration config : conf.getChildren()) {
                        TreeEntry subCurr = new TreeEntry(config.getAbstractName().toString(), NORMAL_TYPE);
                        nodeWeb.addChild(subCurr);
                        addDependencies(subCurr, config);
                    }

                    for (Artifact name : conf.getOwnedConfigurations()) {
                        TreeEntry subCurr = new TreeEntry(name.toString(), NORMAL_TYPE);
                        nodeCLI.addChild(subCurr);
                        addDependencies(subCurr, configManager.getConfiguration(name));
                    }

                }

            }

        }

        TreeEntry treeRepo = new TreeEntry("Repository", NORMAL_TYPE);
        dependencyTree.addItem(treeRepo);
        J2EEServer server = (J2EEServer) session.getAttribute(DependencyViewPortlet.Server_Key);
        if (null == server) {
            logger.error("can not find expected J2EEServer object");
            treeRepo.addChild(new TreeEntry("Not found the content of repository", NORMAL_TYPE));// Ignore the error at client
            return dependencyTree;
        }
        session.removeAttribute(DependencyViewPortlet.Server_Key);
        ListableRepository[] repos = server.getRepositories();
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            final SortedSet artifacts = repo.list();
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                String fileName = iterator.next().toString();
                treeRepo.addChild(new TreeEntry(fileName, NORMAL_TYPE));
            }
        }
        check_no_child(dependencyTree.getItems());
        return dependencyTree;
    }

    private static void check_no_child(List<TreeEntry> list) {
        List<TreeEntry> children;
        for (TreeEntry entry : list) {
            children = entry.getChildren();
            if (children.size() > 0)
                check_no_child(children);
            else if (entry.getType().equals(NOT_LEAF_TYPE))
                children.add(new TreeEntry(NO_CHILD, NORMAL_TYPE));
        }
    }
}
