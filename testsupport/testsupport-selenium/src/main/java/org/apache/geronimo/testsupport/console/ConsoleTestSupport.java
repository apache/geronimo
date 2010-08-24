/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsupport.console;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * Provides support for console-related tests.
 *
 * @version $Rev$ $Date$
 */
public abstract class ConsoleTestSupport
    extends SeleniumTestSupport
{
    protected void login() throws Exception {    	
        selenium.open("/");              
        assertEquals("Apache Geronimo", selenium.getTitle());
        selenium.deleteAllVisibleCookies();
        selenium.click("link=Console");
        waitForPageLoad();        
        assertEquals("Geronimo Console Login", selenium.getTitle());
        selenium.type("//input[@name='j_username']", "system");
        selenium.type("//input[@name='j_password']", "manager");
        selenium.click("submit");
        waitForPageLoad();
        assertEquals("Geronimo Console", selenium.getTitle());
    }
    
    protected void logout() throws Exception {
    	selenium.open("/console");
        selenium.click("//a[contains(@href, '/console/logout.jsp')]");
        waitForPageLoad();
        
        assertEquals("Geronimo Console Login", selenium.getTitle());
        
        //selenium.removeCookie("JSESSIONID", "/");
    }
    protected String getNavigationTreeNodeLocation(String navigationTreeLabel){
		//map label to id according to treeData
		Map<String,Integer> navigationTreeLabel2Index=new HashMap<String,Integer>();
		navigationTreeLabel2Index.put("Welcome",0);
		navigationTreeLabel2Index.put("Server",1);
		navigationTreeLabel2Index.put("Services",2);
		navigationTreeLabel2Index.put("Applications",3);
		navigationTreeLabel2Index.put("Security",4);
		navigationTreeLabel2Index.put("Debug Views",5);
		navigationTreeLabel2Index.put("Embedded DB",6);
		//get tree node id dynamicly 
		String script=" var navigationTree=this.browserbot.getCurrentWindow().dijit.byId('navigationTree');";
	 	script=script+"var wrapperNode =navigationTree._itemNodesMap["+navigationTreeLabel2Index.get(navigationTreeLabel).intValue()+"];";
	 	script+="wrapperNode[0].id;";
	 	String navigationTreeNodeId=selenium.getEval(script);
	 	
	 	//collapse the tree node 
	 	script=" var navigationTree=this.browserbot.getCurrentWindow().dijit.byId('navigationTree');";
	 	script=script+"var wrapperNode =navigationTree._itemNodesMap["+navigationTreeLabel2Index.get(navigationTreeLabel).intValue()+"];";
	 	script+="navigationTree._collapseNode(wrapperNode[0]);";
		selenium.getEval(script);
        return "xpath=//div[@id='"+navigationTreeNodeId+"']/div[1]/img";
	}
}