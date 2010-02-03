/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.monitoring.console;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;


/**
 * @version $Rev$ $Date$
 */
public class GeneralSTest extends SeleneseTestCase {
    public void setUp() throws Exception {
            setUp("http://localhost:8080/console/portal/welcome", "*chrome");
    }
    public void testNew() throws Exception {
            selenium.open("http://localhost:8080/console/portal/welcome");
            selenium.type("<portlet:namespace/>j_username", "system");
            selenium.type("<portlet:namespace/>j_password", "manager");
            selenium.click("submit");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Monitoring");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Add Server");
            selenium.waitForPageToLoad("30000");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_name", "foo");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_ip", "localhost");
            selenium.click("Pluto__monitoring_monitoring_126896788_0_protocol1");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_username", "system");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_password", "manager");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_password2", "manager");
            selenium.click("//input[@value='Add']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Enable Query");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Edit");
            selenium.waitForPageToLoad("30000");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_snapshot", "60");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_retention", "5");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Home");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Add Graph");
            selenium.waitForPageToLoad("30000");
            selenium.select("Pluto__monitoring_monitoring_126896788_0_server_id", "label=foo - localhost");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_name", "foo");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_description", "foo");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_xlabel", "x");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_ylabel", "y");
            selenium.select("Pluto__monitoring_monitoring_126896788_0_mbean", "label=JettyWebConnector");
            selenium.select("Pluto__monitoring_monitoring_126896788_0_dataname1", "label=Connections Request Current");
            selenium.click("//input[@value='Add']");
            assertEquals("Timeframe needs to be at least 120", selenium.getAlert());
            selenium.type("Pluto__monitoring_monitoring_126896788_0_timeframe", "120");
            selenium.click("//input[@value='Add']");
            selenium.waitForPageToLoad("30000");
            selenium.click("//div[@id='/monitoring.monitoring!126896788|0']/div[2]/table[3]/tbody/tr[2]/td[5]/a");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Home");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Create View");
            selenium.waitForPageToLoad("30000");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_name", "foo");
            selenium.type("Pluto__monitoring_monitoring_126896788_0_description", "foo");
            selenium.click("graph_ids");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=foo");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Home");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Edit");
            selenium.waitForPageToLoad("30000");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Home");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=foo");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Home");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Edit");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Delete this view");
            selenium.waitForPageToLoad("30000");
            selenium.click("//div[@id='/monitoring.monitoring!126896788|0']/div[2]/table[3]/tbody/tr[2]/td[5]/a");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Delete this graph");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Disable Query");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Edit");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Delete this server");
            selenium.waitForPageToLoad("30000");
            selenium.click("//img[@alt='Logout']");
            selenium.waitForPageToLoad("30000");
    }
}
