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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.geronimo.pluto.impl.PageConfig;
/*
* @version $Rev$ $Date$
*/
public class TreeNodeTest extends TestCase {

    public void testCategoryTreeNode() {
        
        //second level category
        TreeNode categoryNode=new TreeNode("1-2","server/kernel");
        Assert.assertEquals("1-2", categoryNode.getId());
        Assert.assertEquals("server/kernel", categoryNode.getPath());
        Assert.assertEquals("kernel", categoryNode.getLabel());
        Assert.assertEquals("/images/ico_folder_16x16.gif", categoryNode.getIcon());
        Assert.assertFalse(categoryNode.isLeafNode());
        Assert.assertFalse(categoryNode.isTopNode());
        Assert.assertNotNull(categoryNode.getChildren());
        
        //top level category
        categoryNode=new TreeNode("1","server");
        Assert.assertEquals("1", categoryNode.getId());
        Assert.assertEquals("server", categoryNode.getPath());
        Assert.assertEquals("server", categoryNode.getLabel());
        Assert.assertEquals("/images/ico_folder_16x16.gif", categoryNode.getIcon());
        Assert.assertFalse(categoryNode.isLeafNode());
        Assert.assertTrue(categoryNode.isTopNode());
        Assert.assertNotNull(categoryNode.getChildren());
        
    }

    public void testLeafTreeNode() {
        
        PageConfig pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("1-2/applications/deploy");
        TreeNode leafNode=null;
        try {
           leafNode=new TreeNode(pc);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Assert.assertNotNull(leafNode);
        Assert.assertEquals("1-2", leafNode.getId());
        Assert.assertEquals("applications/deploy", leafNode.getPath());
        Assert.assertEquals("deploy", leafNode.getLabel());
        Assert.assertEquals("/images/ico_deploy_16x16.gif", leafNode.getIcon());
        Assert.assertTrue(leafNode.isLeafNode());
        Assert.assertFalse(leafNode.isTopNode());
        Assert.assertNull(leafNode.getChildren());
        
        //test illegal pageConfig name
        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("applications/deploy");
        leafNode=null;
        try {
           leafNode=new TreeNode(pc);
        } catch (Exception e) {
            return;
        }
        fail("Expected Ilegal ID Exception");
    }
    
   
    public void testPopulateTree() {
        
        List<PageConfig> pageConfigList = new ArrayList<PageConfig>();
        PageConfig pc = new PageConfig();

        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("2-2/cat2/item2-2");
        pageConfigList.add(pc);

        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("1-1-1-1/cat1/cat1-1/cat1-1-1/item1-1-1-1");
        pageConfigList.add(pc);

        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("2-2-1/cat2/cat2-1/item2-2-1");
        pc.setUri("/server/serverlog");

        pageConfigList.add(pc);
        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("1-2/cat1/item1-2");
        pageConfigList.add(pc);
        
        
        Map<String, TreeNode> navigationTree = new TreeMap<String, TreeNode>();
        
        for (PageConfig pageConfig : pageConfigList) {
            try {
                new TreeNode(pageConfig).populateTree(navigationTree);
            } catch (Exception e) {
                continue;
            }
        }
       
       String expectedString="[1, 1-1, 1-1-1, 1-1-1-1, 1-2, 2, 2-2, 2-2-1]";
       Assert.assertEquals(expectedString, navigationTree.keySet().toString());
       System.out.println(navigationTree.keySet());  
        
    }

}
