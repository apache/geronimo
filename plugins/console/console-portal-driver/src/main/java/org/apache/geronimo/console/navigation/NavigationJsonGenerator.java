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
package org.apache.geronimo.console.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.pluto.impl.PageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class is used to generate JSON tree for a list of PageConfig.
 * 
 * Sample:  
 * 
 * pageName of pageConfig1: 1-1/server/server log
 * pageName of pageConfig2: 2-1/applications/web applications
 * pageName of pageConfig3: 1-2/server/server info
 * 
 * will result in following navigation tree:
 * 
 * --server
 *      --server log
 *      --server info
 * --applications
 *      --web applications
 *
 * @version $Rev$ $Date$
 */

public class NavigationJsonGenerator {

    // private static Map<String, Map<String, TreeNode>> navigationTrees = new HashMap<String, Map<String, TreeNode>>();

    public static final String ALL = "all";

    private final static List<String> monitorRolePermitPages = Arrays
            .asList("0/Welcome", "1-1-1/Application Server/System Information/Server Information",
                    "1-1-2/Application Server/System Information/Java System Info",
                    "1-1-4/Application Server/System Information/Thread Pools",
                    "5-1/Monitoring and Troubleshooting/Monitoring");

    private static Map<String, List<String>> rolePagesMap = new HashMap<String, List<String>>();

    static {

        rolePagesMap.put("monitor", monitorRolePermitPages);

    }

    public static List<PageConfig> filterPagesByRole(List<PageConfig> pageConfigList, HttpServletRequest request) {

        List<String> permitPages = null;

        if (request.isUserInRole("monitor")) {

            permitPages = rolePagesMap.get("monitor");
        }

        if (permitPages == null) {
            return pageConfigList;
        }

        List<PageConfig> filteredPageConfigList = new ArrayList<PageConfig>();

        for (PageConfig pc : pageConfigList) {

            if (permitPages.contains(pc.getName())) {

                filteredPageConfigList.add(pc);
            }
        }

        return filteredPageConfigList;

    };

    private ResourceBundle navigationResourcebundle;

    private static final Logger log = LoggerFactory.getLogger(NavigationJsonGenerator.class);

    public NavigationJsonGenerator(Locale locale) {

        if (locale == null) {
            navigationResourcebundle = ResourceBundle.getBundle("org.apache.geronimo.console.i18n.ConsoleResource");
        } else {
            navigationResourcebundle = ResourceBundle.getBundle("org.apache.geronimo.console.i18n.ConsoleResource",
                    locale);
        }
    }

    public Map<String, TreeNode> getNavigationTree(List<PageConfig> pageConfigList, String mode) {
        Map<String, TreeNode> navigationTree = new TreeMap<String, TreeNode>();

        for (PageConfig pc : pageConfigList) {
            if (mode.equals(ALL) || (pc.getMode() != null && mode.equals(pc.getMode()))) {
                try {
                    new TreeNode(pc).populateTree(navigationTree);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return navigationTree;
    }

    public String generateTreeJSON(Map<String, TreeNode> navigationTree, String contextPath, String DefaultIcon,
            String mode, int threshold) {

        // Map<String, TreeNode> navigationTree = this.getNavigationTree(pageConfigList, mode);

        boolean isTreeAList = this.isTreeAList(navigationTree, mode, threshold);

        StringBuilder sb = new StringBuilder(10);
        sb.append("[");

        if (!isTreeAList) {

            boolean hasTopNode = false;
            for (TreeNode node : navigationTree.values()) {

                // when menu items number is less than 6, let's only display the basic menu to the user.
                // because the user won't need a t to organize the.

                if (node.isTopNode()) {
                    sb.append("\n");
                    appendNodeToTreeJSON(sb, node, contextPath, DefaultIcon);
                    hasTopNode = true;
                }

            }

            // remove the extra ','
            if (hasTopNode) {
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append("\n]");

        } else {

            boolean hasLeafNode = false;
            for (TreeNode node : navigationTree.values()) {
                if (node.isLeafNode()) {

                    sb.append("\n");

                    appendBasicListJSON(sb, node, contextPath, DefaultIcon);

                    hasLeafNode = true;
                }
            }
            // remove the extra ','
            if (hasLeafNode) {
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append("\n]");

        }

        return sb.toString();
    }

    private void appendNodeToTreeJSON(StringBuilder sb, TreeNode node, String contextPath, String DefaultIcon) {

        sb.append("{");

        if (!node.isLeafNode()) {
            sb.append("label: \'" + getLocalizedString(node.getLabel()) + "\'");
        } else {
            sb.append("label: \'<img src=\"" + contextPath + node.getIcon() + "\" alt=\"\" border=\"0\">&nbsp;" + getLocalizedString(node.getLabel()) + "\'");
            
            sb.append(",");
            sb.append("href: \'" + contextPath + "/portal/" + node.getId() + "/" + node.getPath() + "\'");
        }

        sb.append(",");
        sb.append("id: \'" + node.getId() + "\'");

        if (node.getChildren() != null) {
            sb.append(",");
            sb.append("children: [");

            for (TreeNode child : node.getChildren().values()) {
                appendNodeToTreeJSON(sb, child, contextPath, DefaultIcon);
            }

            // remove the extra ','
            sb.deleteCharAt(sb.length() - 1);

            sb.append("]\n");
        }

        sb.append("},");

    }

    private void appendBasicListJSON(StringBuilder sb, TreeNode node, String contextPath, String DefaultIcon) {

        sb.append("{");

        if (node.isLeafNode()) {
            sb.append("label: \'<img src=\"" + contextPath + node.getIcon() + "\" alt=\"\" border=\"0\">&nbsp;" + getLocalizedString(node.getLabel()) + "\'");
            
            sb.append(",");
            sb.append("href: \'" + contextPath + "/portal/" + node.getId() + "/" + node.getPath() + "\'");
        }

        sb.append(",");
        sb.append("id: \'" + node.getId() + "\'");

        sb.append("},");

    }

    public String generateQuickLauncherJSON(Map<String, TreeNode> navigationTree, String contextPath,
            String DefaultIcon, String mode) {

        // Map<String, TreeNode> navigationTree = this.getNavigationTree(pageConfigList, mode);

        StringBuilder sb = new StringBuilder(10);
        sb.append("[\n");

        for (TreeNode node : navigationTree.values()) {

            if (node.isTopNode()) {
                appendNodeToQuickLauncherJSON(sb, node, contextPath, DefaultIcon);
            }

        }
        // remove the extra ','
        sb.deleteCharAt(sb.length() - 1);

        sb.append("\n]");
        return sb.toString();
    }

    private void appendNodeToQuickLauncherJSON(StringBuilder sb, TreeNode node, String contextPath, String DefaultIcon) {

        if (node.isLeafNode()) {
            sb.append("\n{");
            sb.append("label: \'<img src=\"").append(contextPath).append(node.getIcon()).append("\">&nbsp;");
            sb.append("<span>").append(getLocalizedString(node.getLabel())).append("</span>\'");
            sb.append(",");
            sb.append("name: \'").append(getLocalizedString(node.getLabel())).append("\'");
            sb.append(",");
            sb.append("href:\'").append(contextPath + "/portal/" + node.getId() + "/" + node.getPath() + "\'");

            sb.append("},");
        }

        if (node.getChildren() != null) {

            for (TreeNode child : node.getChildren().values()) {
                appendNodeToQuickLauncherJSON(sb, child, contextPath, DefaultIcon);
            }

            // remove the extra ','
            // sb.deleteCharAt(sb.length()-1);
        }

    }

    private String getLocalizedString(String key) {

        try {

            return navigationResourcebundle.getString(key);

        } catch (Exception e) {

            log.debug("error when get localized string by key:" + key
                    + ", fallbacking to the key as the string returned", e);

        }

        return key;

    }

    public boolean isTreeHasValidItem(Map<String, TreeNode> navigationTree, String mode) {

        // Map<String, TreeNode> navigationTree = this.getNavigationTree(pageConfigList, mode);

        for (TreeNode node : navigationTree.values()) {

            if (node.isLeafNode()) {
                return true;
            }

        }

        return false;
    }

    private boolean isTreeAList(Map<String, TreeNode> navigationTree, String mode, int threshold) {

        // Map<String, TreeNode> navigationTree = this.getNavigationTree(pageConfigList, mode);

        int leafNodeCount = 0;

        for (TreeNode node : navigationTree.values()) {

            if (node.isLeafNode()) {
                leafNodeCount = leafNodeCount + 1;
            }

        }

        return leafNodeCount < threshold;
    }

}
