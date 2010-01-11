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

import java.util.Map;
import java.util.TreeMap;
import org.apache.geronimo.pluto.impl.PageConfig;

/*
 * Class to represent a node in navigation tree.
 *
 * @version $Rev$ $Date$
 */
public class TreeNode {
    
    static final String ID_DELIMIT = "-";
    private static final String NAME_DELIMIT = "/";
    private String ID;
    private PageConfig pageConfig;
    private String label;
    private String icon;
    private String path;
    private Map<String, TreeNode> children;

    /*
     * Used for category node.
     */
    TreeNode(String id, String categoryPath) {

        ID = id;
        path = categoryPath;
        label = categoryPath.substring(categoryPath.lastIndexOf(NAME_DELIMIT) + 1, categoryPath.length());
        icon = "/images/ico_folder_16x16.gif";

    }

    /*
     * Used for leaf node.
     */
    TreeNode(PageConfig _pageConfig) throws Exception {

        this.pageConfig = _pageConfig;
        String pageName = pageConfig.getName();
        path = pageName.substring(pageName.indexOf(NAME_DELIMIT) + 1, pageName.length());
        String[] fragments = pageName.split(NAME_DELIMIT);
        ID = fragments[0];

        // check ID format
        String[] idNumbers = ID.split(ID_DELIMIT);
        try {
            for (String numberString : idNumbers) {

                Integer.parseInt(numberString);

            }
        } catch (NumberFormatException e) {
            throw new Exception("Ilegal ID:" + ID + "in pageConfig:" + pageName, e);
        }

        label = fragments[fragments.length - 1];
        icon = pageConfig.getIcon()==null?"/images/ico_geronimo_16x16.gif":pageConfig.getIcon();
    }

    String getIcon() {
        return icon;
    }

    String getLabel() {
        return label;
    }

    String getId() {
        return ID;
    }

    /*
     * only category node has children.
     */
    Map<String, TreeNode> getChildren() {
        
        if (isLeafNode())
            return null;
        
        if (children == null) {
            children = new TreeMap<String, TreeNode>(new TreeNodeIdComparator());
        }
        
        return children;
    }

    String getPath() {
        return path;
    }

    boolean isLeafNode() {
        return pageConfig == null ? false : true;
    }

    boolean isTopNode() {
        return ID.indexOf(ID_DELIMIT) < 0;
    }

    void populateTree(Map<String, TreeNode> tree) {

        tree.put(ID, this);
        // no parents available
        if (this.ID.indexOf(ID_DELIMIT) < 0) {
            return;
        }

        String parentId = ID.substring(0, ID.lastIndexOf(ID_DELIMIT));

        String parentPath = path.substring(0, path.lastIndexOf(NAME_DELIMIT));

        if (!tree.containsKey(parentId)) {
            TreeNode parentNode = new TreeNode(parentId, parentPath);
            parentNode.populateTree(tree);
            tree.put(parentId, parentNode);
        }

        tree.get(parentId).addChild(this);

    }

    private void addChild(TreeNode child) {
        
        if (isLeafNode())
            return;

        this.getChildren().put(child.getId(), child);
    }

    @Override
    public String toString() {
        return "TreeNode [Id=" + ID + ", label=" + label + "]";
    }

    
}
