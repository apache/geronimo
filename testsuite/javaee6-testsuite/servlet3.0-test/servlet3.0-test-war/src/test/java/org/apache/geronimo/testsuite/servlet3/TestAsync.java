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
 
package org.apache.geronimo.testsuite.servlet3;

import org.testng.annotations.Test;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestAsync extends SeleniumTestSupport {
	
		@Test
	public void testAsyncServlet() throws Exception {
		String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);
		selenium.selectFrame("sampleDocumentFrame");
		selenium.click("link=Test AsyncServlet.");
		waitForPageLoad();
		
		assertTrue(selenium.isTextPresent("Servlet starts at:"));
		assertTrue(selenium.isTextPresent("Task assigned to executor.Servlet ends at:"));
		assertTrue(selenium.isTextPresent("TaskExecutor starts at:"));
		assertTrue(selenium.isTextPresent("Task finishes."));
		assertTrue(selenium.isTextPresent("TaskExecutor ends at:"));
		

        // servlet finish time
		String set = selenium.getText("xpath=//p[2]").substring(60, 62);
        // servlet task start time
	    String tst = selenium.getText("xpath=//p[3]").substring(41, 43);
        // servlet task finish time
        String tet = selenium.getText("xpath=//p[5]").substring(39, 41);
        int seti = Integer.parseInt(set);
        int tsti = Integer.parseInt(tst);
        int teti = Integer.parseInt(tet);        
        assertTrue(seti==tsti);
        assertTrue(seti==teti - 10);       
	}

}
