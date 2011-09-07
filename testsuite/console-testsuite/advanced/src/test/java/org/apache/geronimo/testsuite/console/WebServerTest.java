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

package org.apache.geronimo.testsuite.console;

import org.testng.annotations.Test;

/**
 * @version $Rev$ $Date$
 */
public class WebServerTest
    extends TestSupport
{
    public static final String TOMCAT = "Tomcat";
    public static final String JETTY = "Jetty";

    @Test
    public void testNewConnector() throws Exception {
        String name = "uniquename";
        addConnector(name, 8081);
        
        deleteConnector(name);
        //return to main window
        selenium.selectWindow("null");
    }

   @Test
    public void testEditConnector() throws Exception {
    	selenium.setSpeed("3000");
        String name = "uniquename2";
        addConnector(name, 8082);

        String connectorSelector = "//tr[td[1] = \"" + name + "\"]";
        
        selenium.click(connectorSelector + "/td[5]/a[2]");
        waitForPageLoad();
        //deleteConnector(name);

        selenium.type("port", "8008");
        selenium.click("submit");
        waitForPageLoad();
        selenium.isTextPresent("8008");
        
        selenium.click(connectorSelector + "/td[5]/a[2]"); 

        waitForPageLoad();
        selenium.type("port", "8009");
        selenium.click("submit");
        waitForPageLoad();
        selenium.isTextPresent("8009");

        deleteConnector(name);
        //return to main window
        selenium.selectWindow("null");
    }
    
    @Test
    public void testStartStopConnector() throws Exception {
        String name = "uniquename3";
        addConnector(name, 8083);

        String connectorSelector = "//tr[td[1] = \"" + name + "\"]";

        assertEquals("running", selenium.getText(connectorSelector + "/td[4]"));
        selenium.click(connectorSelector + "/td[5]/a[1]");
        waitForPageLoad();
        assertEquals("stopped", selenium.getText(connectorSelector + "/td[4]"));
        selenium.click(connectorSelector + "/td[5]/a[1]");
        waitForPageLoad();
        assertEquals("running", selenium.getText(connectorSelector + "/td[4]"));

        deleteConnector(name);
        //return to main window
        selenium.selectWindow("null");
    }

    private void addConnector(String name, int port) throws Exception {
    	//selenium.click(getNavigationTreeNodeLocation("Server"));
        selenium.click("link=Web Server");
        waitForPageLoad();
        String container = JETTY;
        //selenium.selectFrame("index=0");
        if (selenium.isTextPresent(TOMCAT)) {
            container = TOMCAT;
        }
        
        selenium.click("link=" + container + " BIO HTTP Connector");
        waitForPageLoad();
        selenium.type("uniqueName", name);
        selenium.type("port", String.valueOf(port));
        selenium.click("submit");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent(name));
    }

    private void deleteConnector(String name) throws Exception {
        selenium.click("//a[@onclick=\"return confirm('Are you sure you want to delete " + name + "?');\"]");
        waitForPageLoad();
        assertTrue(selenium.getConfirmation().matches("Are you sure you want to delete " + name + "[\\s\\S]?"));
    }
}

