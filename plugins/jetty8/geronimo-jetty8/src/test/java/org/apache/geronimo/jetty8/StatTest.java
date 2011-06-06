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
package org.apache.geronimo.jetty8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import org.apache.geronimo.management.LazyStatisticsProvider;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * @version $Rev$ $Date$
 */
public class StatTest extends AbstractWebModuleTest {

    private ObjectName webModuleName;

    public void testContainerStats() throws Exception {
        statsTest(container);
    }

    public void testConnectorStats() throws Exception {
        statsTest(connector);
    }

    public void statsTest(LazyStatisticsProvider component) throws Exception {
        // start statistics collection
        if (component instanceof LazyStatisticsProvider) {
            assertTrue("Stats should be off initially", !component.isStatsOn());
            component.setStatsOn(true);
        }
        int n = 4; // no of connections
        for (int k = 0; k < n; k++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/hello.txt")
                    .openConnection();
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals("Hello World", reader.readLine());

            Stats stats = component.getStats();
            Statistic[] stts = stats.getStatistics();
            Statistic aStts;
            String[] sttsNames = stats.getStatisticNames();
            for (int i = 0; i < sttsNames.length; i++) {
                // check that the names match the getter methods
                String sttsName = sttsNames[i];
                assertFalse(sttsName.equals(stts[i].getName()));
                try {
                    stats.getClass().getMethod("get" + sttsName, new Class[0]);
                } catch (NoSuchMethodException e) {
                    continue; // ignore this statistics for now, JSR77.6.10.1.1
                }
                aStts = stats.getStatistic(sttsName);
                assertTrue("startTime was not set for " + sttsName, aStts.getStartTime() != 0);
                assertTrue("lastSampleTime was not set for " + sttsName, aStts.getLastSampleTime() != 0);
                /*System.out.println("              lastSampleTime = " + aStts.getLastSampleTime() +
            	    "  startTime = " + aStts.getStartTime());
                System.out.println(aStts);*/
            }
            if (k == n - 2) component.resetStats(); // test reset
            connection.disconnect();
            Thread.sleep(1000);  // connection interval
        }
    }

    protected void setUp() throws Exception {
        appPath = "war1";
        super.setUp();
        WebAppInfo webAppInfo = new WebAppInfo();
        setUpStaticContentServlet(webAppInfo);
        setUpAppContext(null, null, "policyContextID", null, "war1/", webAppInfo);
    }
}
