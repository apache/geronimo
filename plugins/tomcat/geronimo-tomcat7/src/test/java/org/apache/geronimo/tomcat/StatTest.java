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
package org.apache.geronimo.tomcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Revision$ $Date$
 */
public class StatTest extends AbstractWebModuleTest {

    //private ObjectName webModuleName;

    public void testStats() throws Exception {
        WebAppInfo webAppInfo = new WebAppInfo();
        setUpStaticContentServlet(webAppInfo);
        TomcatWebAppContext webModule = setUpInsecureAppContext("war1", null, null, null, null, null, webAppInfo);
            HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/hello.txt")
                    .openConnection();
            BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
            //connection.disconnect();
            // Stats stats = (Stats) kernel.getAttribute(webModuleName, "stats");
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception e) {
                }
        }

        int n = 3;
        for (int k = 0; k < n; k++) {
            if (k == n-1) {
                connector.resetStats();
                webModule.resetStats();
            }
            //System.out.println("******* NOW IS " + System.currentTimeMillis());
            Stats[] allStats = { webModule.getStats(), connector.getStats() };
            Stats stats;
            for (int j = 0; j < allStats.length; j++) {
                stats = allStats[j];
                Statistic[] stts = stats.getStatistics();
                Statistic aStts;
                String[] sttsNames = stats.getStatisticNames();
                for (int i = 0; i < sttsNames.length; i++) {
                    assertFalse(sttsNames[i].equals(stts[i].getName()));
                    aStts = stats.getStatistic(sttsNames[i]);
                    assertTrue("startTime was not set", aStts.getStartTime() != 0);
                    assertTrue("lastSampleTime was not set", aStts.getLastSampleTime() != 0);
                    //System.out.println("lastSampleTime = " + aStts.getLastSampleTime() + "  startTime = " + aStts.getStartTime());
                    //System.out.println(aStts);
                }
            }
            Thread.sleep(1000);  // collection interval
        }
        connection.disconnect();
    }

    protected void setUp() throws Exception {
        super.setUp();
        super.init(null);
    }
}
