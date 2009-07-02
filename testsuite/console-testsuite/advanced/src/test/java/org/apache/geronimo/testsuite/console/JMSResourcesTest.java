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
public class JMSResourcesTest extends TestSupport {
    @Test
    public void testNewJMSResource() throws Exception {
        selenium.click("link=JMS Resources");
        waitForPageLoad();
        selenium.click("link=For ActiveMQ");
        waitForPageLoad();
        selenium.type("instanceName", "UniqueName");
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.click("//input[@value='Add Connection Factory']");
        waitForPageLoad();
        selenium.select("factoryType", "label=javax.jms.QueueConnectionFactory");
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.type("factory.0.instanceName", "ConnectionFactory");
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.click("//input[@value='Add Destination']");
        waitForPageLoad();
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.type("destination.0.name", "mdb/Unique");
        selenium.type("destination.0.instance-config-0", "mdb/Unique");
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.click("//input[@value='Deploy Now']");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("UniqueName (console.jms/UniqueName/1.0/rar)"));
        selenium.click("link=J2EE Connectors");
        waitForPageLoad();
        selenium.click("link=Uninstall"); 
        Thread.sleep(45 * 1000);
        selenium.click("dijit_form_Button_0");
        waitForPageLoad();
        selenium.click("link=JMS Resources");
        waitForPageLoad();
        assertFalse(selenium.isTextPresent("UniqueName (console.jms/UniqueName/1.0/rar)"));
    }
}
