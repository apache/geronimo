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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.geronimo.pluto.impl.PageConfig;

/*
 * @version $Rev$ $Date$
 */
public class NavigationJsonGeneratorTest extends TestCase {

    NavigationJsonGenerator navigationJsonGenerator;
    List<PageConfig> pageList;

    protected void setUp() throws Exception {
        super.setUp();

        navigationJsonGenerator = new NavigationJsonGenerator(null);

        pageList = new ArrayList<PageConfig>();
        PageConfig pc = new PageConfig();

        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("2-2/cat2/item2-2");
        pc.setUri("/server/serverlog");

        pageList.add(pc);

        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("1-1-1-1/cat1/cat1-1/cat1-1-1/item1-1-1-1");
        pc.setUri("/server/serverlog");
        pageList.add(pc);

        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("2-2-1/cat2/cat2-1/item2-2-1");
        pc.setUri("/server/serverlog");

        pageList.add(pc);

        pc = new PageConfig();
        pc.setIcon("/images/ico_deploy_16x16.gif");
        pc.setName("1-2/cat1/item1-2");
        pc.setUri("/server/serverlog");
        pageList.add(pc);

    }

    public void testGenerateTreeJSON() {
        System.out.println("testGenerateTreeJSON() ------ ");

        Map<String, TreeNode> navigationTree = null;

        navigationTree = navigationJsonGenerator.getNavigationTree(pageList, "all");
        String json = navigationJsonGenerator.generateTreeJSON(navigationTree, "console",
                "/images/ico_deploy_16x16.gif", "all", 3);

        String expected = "[\n{label: \'cat1\',id: \'1\',children: [{label: \'cat1-1\',id: \'1-1\',children: [{label: \'cat1-1-1\',id: \'1-1-1\',children: [{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item1-1-1-1\',href: \'console/portal/1-1-1-1/cat1/cat1-1/cat1-1-1/item1-1-1-1\',id: \'1-1-1-1\'}]\n}]\n},{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item1-2\',href: \'console/portal/1-2/cat1/item1-2\',id: \'1-2\'}]\n},\n{label: \'cat2\',id: \'2\',children: [{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item2-2\',href: \'console/portal/2-2/cat2/item2-2\',id: \'2-2\'}]\n}\n]";

        System.out.println(json);
        Assert.assertEquals(expected, json);

        navigationTree = navigationJsonGenerator.getNavigationTree(pageList, "all");
        json = navigationJsonGenerator.generateTreeJSON(navigationTree, "console", "/images/ico_deploy_16x16.gif",
                "all", 5);

        expected = "[\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item1-1-1-1\',href: \'console/portal/1-1-1-1/cat1/cat1-1/cat1-1-1/item1-1-1-1\',id: \'1-1-1-1\'},\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item1-2\',href: \'console/portal/1-2/cat1/item1-2\',id: \'1-2\'},\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item2-2\',href: \'console/portal/2-2/cat2/item2-2\',id: \'2-2\'},\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\" alt=\"\" border=\"0\">&nbsp;item2-2-1\',href: \'console/portal/2-2-1/cat2/cat2-1/item2-2-1\',id: \'2-2-1\'}\n]";
        
        System.out.println(json);
        Assert.assertEquals(expected, json);

    }

    public void testGenerateQuickLauncherJSON() {
        System.out.println("testGenerateQuickLauncherJSON() ------ ");

        Map<String, TreeNode> navigationTree = navigationJsonGenerator.getNavigationTree(pageList, "all");
        String json = navigationJsonGenerator.generateQuickLauncherJSON(navigationTree, "console",
                "/images/ico_deploy_16x16.gif", "all");
        String expected = "[\n\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\">&nbsp;<span>item1-1-1-1</span>\',name: \'item1-1-1-1\',href:\'console/portal/1-1-1-1/cat1/cat1-1/cat1-1-1/item1-1-1-1'},\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\">&nbsp;<span>item1-2</span>\',name: \'item1-2\',href:\'console/portal/1-2/cat1/item1-2\'},\n{label: \'<img src=\"console/images/ico_deploy_16x16.gif\">&nbsp;<span>item2-2</span>\',name: \'item2-2\',href:\'console/portal/2-2/cat2/item2-2\'}\n]";

        System.out.println(json);
        Assert.assertEquals(expected, json);

    }
}
