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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * Provides support for console-related tests.
 *
 * @version $Rev$ $Date$
 */
public abstract class ConsoleTestSupport extends SeleniumTestSupport
{
	// Setup a link map in order to get right url when the link is not found.
	public static final Map<String,String> link2URL;
	public static String fileName;
	static {
		link2URL = new HashMap<String,String>();
		fileName = System.getProperty("linkPropertyFile");
		try {
			File file = new File(fileName);
			FileReader f_reader;
			try {
				f_reader = new FileReader(file);
			    BufferedReader reader = new BufferedReader(f_reader);
				String str = reader.readLine();
				while (str != null) {
					if(!str.startsWith("#")) {
						String[] lU = str.split(";");
						if(lU.length!=2) {
							throw new IOException("File 'link.properties' formats error.Length is:"+lU.length+lU[0]+"aaa");
						}
						link2URL.put(lU[0], lU[1]);
					}
					str = reader.readLine();
				}
		
				/*
				link2URL.put("link=Welcome", "/console/portal/0/Welcome");
				// under Application Server
				link2URL.put("link=Server Information", "/console/portal/1-1-1/Application Server/System Information/Server Information");
				link2URL.put("link=Java System Info", "/console/portal/1-1-2/Application Server/System Information/Java System Info");
				link2URL.put("link=Thread Pools", "/console/portal/1-1-4/Application Server/System Information/Thread Pools");
				link2URL.put("link=Web Server", "/console/portal/1-2/Application Server/Web Server");
				link2URL.put("link=EJB Server", "/console/portal/1-4/Application Server/EJB Server");
				link2URL.put("link=Shutdown", "/console/portal/1-6/Application Server/Shutdown");
				// under Applications
				link2URL.put("link=Deployer", "/console/portal/2-1/Applications/Deployer");
				link2URL.put("link=Web App WARs", "/console/portal/2-2-1/Applications/User Assets/Web App WARs");
				link2URL.put("link=Application EARs", "/console/portal/2-2-2/Applications/User Assets/Application EARs");
				link2URL.put("link=EJB JARs", "/console/portal/2-2-3/Applications/User Assets/EJB JARs");
				link2URL.put("link=App Clients", "/console/portal/2-2-4/Applications/User Assets/App Clients");
				link2URL.put("link=Application EBAs", "/console/portal/2-2-5/Applications/User Assets/Application EBAs");
				// under Resources
				link2URL.put("link=J2EE Connectors", "/console/portal/3-3/Resources/J2EE Connectors");
				link2URL.put("link=JAR Aliases", "/console/portal/3-4/Resources/JAR Aliases");
				link2URL.put("link=Repository", "/console/portal/3-5/Resources/Repository");
				link2URL.put("link=Apache HTTP", "/console/portal/3-6/Resources/Apache HTTP");
				link2URL.put("link=System Modules","/console/portal/3-7/Resources/System Modules");
				link2URL.put("link=Plugins", "/console/portal/3-8/Resources/Plugins");
				link2URL.put("link=OSGI Bundles", "/console/portal/3-10/Applications/OSGI Bundles");
				// under Security
				link2URL.put("link=Users and Groups", "/console/portal/4-1/Security/Users and Groups");
				link2URL.put("link=Keystores", "/console/portal/4-2/Security/Keystores");
				link2URL.put("link=Certificate Authority", "/console/portal/4-3/Security/Certificate Authority");
				link2URL.put("link=Security Realms", "/console/portal/4-4/Security/Security Realms");
				// under Monitoring and Troubleshooting
				link2URL.put("link=Server Logs", "/console/portal/5-2-2/Monitoring and Troubleshooting/Logs/Server Logs");
				*/
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("!!!!!!!!!!!!!!!!!!!not find link.properties!");
				e.printStackTrace();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    	protected void login() throws Exception {
	        selenium.open("/console");
	        waitForPageLoad();
	        
	        //assertEquals("Apache Geronimo", selenium.getTitle());
	
	        //selenium.deleteAllVisibleCookies();
	
	        //selenium.click("link=Console");
	        //waitForPageLoad();
	        
	        //assertEquals("Geronimo Console Login", selenium.getTitle());
	        
	        selenium.type("j_username", "system");
	        selenium.type("j_password", "manager");
	        selenium.click("submit");
	        waitForPageLoad();
	        //assertEquals("Geronimo Console", selenium.getTitle());
    	}
    
    	protected void logout() throws Exception {
		 	selenium.open("/console");
		 	waitForPageLoad();
	        selenium.click("//a[contains(@href, '/console/logout.jsp')]");
	        waitForPageLoad();
	        
	        //assertEquals("Geronimo Console Login", selenium.getTitle());
	        
	        //selenium.removeCookie("JSESSIONID", "/");
    	}

}
