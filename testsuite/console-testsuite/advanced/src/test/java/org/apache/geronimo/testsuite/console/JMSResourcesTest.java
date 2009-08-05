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
import org.apache.geronimo.testsupport.console.ConsoleTestSupport;

@Test
public class JMSResourcesTest extends ConsoleTestSupport {
    @Test
    public void testNewJMSResource() throws Exception {
        try {
            login();
            
            selenium.click("link=JMS Resources");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=For ActiveMQ");
            selenium.waitForPageToLoad("30000");
            selenium.type("instanceName", "UniqueName");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Add Connection Factory']");
            selenium.waitForPageToLoad("30000");
            selenium.select("factoryType", "label=javax.jms.QueueConnectionFactory");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.type("factory.0.instanceName", "ConnectionFactory");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Add Destination']");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.type("destination.0.name", "mdb/Unique");
            selenium.type("destination.0.instance-config-0", "mdb/Unique");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Deploy Now']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("UniqueName (console.jms/UniqueName/1.0/car)"));
            selenium.click("link=J2EE Connectors");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Uninstall");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.getConfirmation().matches("^Are you certain you wish to uninstall console\\.jms/UniqueName/1\\.0/car[\\s\\S]*"));
            selenium.click("link=JMS Resources");
            selenium.waitForPageToLoad("30000");
            assertFalse(selenium.isTextPresent("UniqueName (console.jms/UniqueName/1.0/car)"));
        } finally {
            logout();
        }
    }
}
