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

package org.apache.geronimo.testsuite.enterprise.jms;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

import org.testng.annotations.Test;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Test
public class JMSTest
extends SeleniumTestSupport {
    @Test
    public void testIndexContent() throws Exception {
        selenium.open("/testjms/JMSQueueSender");
        waitForPageLoad();
        assertEquals("JMS Sender", selenium.getTitle());
        assertEquals("Sent JMS Queue Message", selenium.getText("xpath=/html/body"));

        selenium.open("/testjms/JMSQueueReceiver");
        waitForPageLoad();
        assertEquals("JMS Receiver", selenium.getTitle());
        assertEquals("Received JMS Queue Message", selenium.getText("xpath=/html/body"));

        selenium.open("/testjms/JMSTopicSenderReceiver");
        waitForPageLoad();
        assertEquals("JMS Topic Sender Receiver", selenium.getTitle());
        assertEquals("Received JMS Topic Message", selenium.getText("xpath=/html/body"));

    }
}

