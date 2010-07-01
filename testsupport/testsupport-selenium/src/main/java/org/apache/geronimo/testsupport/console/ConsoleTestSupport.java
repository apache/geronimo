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
	// Setup a link map in order to get right url when the link is not found.
	public static final Map<String,String> link2URL;
	static {
		link2URL = new HashMap<String,String>();
		link2URL.put("link=Welcome", "/console/portal/0/Welcome");
		// under server
		link2URL.put("link=Information", "/console/portal/1-1/Server/Information");
		link2URL.put("link=Java System Info", "/console/portal/1-2/Server/Java System Info");
		link2URL.put("link=Server Logs", "/console/portal/1-3/Server/Server Logs");
		link2URL.put("link=Shutdown", "/console/portal/1-4/Server/Shutdown");
		link2URL.put("link=Web Server", "/console/portal/1-5/Server/Web Server");
		link2URL.put("link=Thread Pools", "/console/portal/1-6/Server/Thread Pools");
		link2URL.put("link=Apache HTTP", "/console/portal/1-7/Server/Apache HTTP");
		link2URL.put("link=Monitoring", "/console/portal/1-8/Server/Monitoring");
		link2URL.put("link=JAR Aliases", "/console/portal/1-9/Server/JAR Aliases");
		link2URL.put("link=JMS Server", "/console/portal/1-10/Server/JMS Server");
		link2URL.put("link=EJB Server", "/console/portal/1-11/Server/EJB Server");
		// under services
		link2URL.put("link=Repository", "/console/portal/2-1/Services/Repository");
		link2URL.put("link=JMS Resources", "/console/portal/2-2/Services/JMS Resources");
		link2URL.put("link=Database Pools", "/console/portal/2-3/Services/Database Pools");
		// under applications
		link2URL.put("link=Web App WARs", "/console/portal/3-1/Applications/Web App WARs");
		link2URL.put("link=System Modules", "/console/portal/3-2/Applications/System Modules");
		link2URL.put("link=Application EARs", "/console/portal/3-3/Applications/Application EARs");
		link2URL.put("link=EJB JARs", "/console/portal/3-4/Applications/EJB JARs");
		link2URL.put("link=J2EE Connectors","/console/portal/3-5/Applications/J2EE Connectors");
		link2URL.put("link=App Clients", "/console/portal/3-6/Applications/App Clients");
		link2URL.put("link=Plan Creator", "/console/portal/3-7/Applications/Plan Creator");
		link2URL.put("link=Deployer", "/console/portal/3-8/Applications/Deploy New");
		link2URL.put("link=Plugins", "/console/portal/3-9/Applications/Plugins");
		// under security
		link2URL.put("link=Users and Groups", "/console/portal/4-1/Security/Users and Groups");
		link2URL.put("link=Keystores", "/console/portal/4-2/Security/Keystores");
		link2URL.put("link=Certificate Authority", "/console/portal/4-3/Security/Certificate Authority");
		link2URL.put("link=Security Realms", "/console/portal/4-4/Security/Security Realms");
		// under debug views
		link2URL.put("link=JMX Viewer", "/console/portal/5-1/Debug Views/JMX Viewer");
		link2URL.put("link=LDAP Viewer", "/console/portal/5-2/Debug Views/LDAP Viewer");
		link2URL.put("link=ClassLoader Viewer", "/console/portal/5-3/Debug Views/ClassLoader Viewer");
		link2URL.put("link=JNDI Viewer", "/console/portal/5-4/Debug Views/JNDI Viewer");
		link2URL.put("link=Dependency Viewer", "/console/portal/5-5/Debug Views/Dependency Viewer");
		// under Embedded DB
		link2URL.put("link=DB Info", "/console/portal/6-1/Embedded DB/DB Info");
		link2URL.put("link=DB Manager", "/console/portal/6-2/Embedded DB/DB Manager");
		link2URL.put("link=Derby Logs", "/console/portal/6-3/Embedded DB/Derby Logs");
		
	}
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
}

