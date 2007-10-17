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
package org.apache.geronimo.jetty6;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import org.apache.geronimo.management.LazyStatisticsProvider;

/**
 * @version $Rev$ $Date$
 */
public class StatTest extends AbstractWebModuleTest {

    private ObjectName webModuleName;

    public void testStats() throws Exception {
        JettyWebAppContext app;
        app = setUpAppContext(null, null, null, null, null, null, null, "war1/");

        setUpStaticContentServlet(app);
        
        // start statistics collection
        if (connector instanceof LazyStatisticsProvider) {
        assertTrue("Stats should be off initially", !connector.isStatsOn());
        connector.setStatsOn(true);
        }
        container.setCollectStatistics(true);
        int n = 4; // no of connections
        for (int k = 0; k < n; k++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/hello.txt")
                    .openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
            
            Stats[] allStats = {connector.getStats()};
            Stats stats;
            for (int j = 0; j < allStats.length; j++) {
                stats = allStats[j];
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
                	continue; // ignore this statistics JSR77.6.10.1.1
                    }
                    aStts = stats.getStatistic(sttsName);
                    assertTrue("startTime was not set for " + sttsName, aStts.getStartTime() != 0);
                    assertTrue("lastSampleTime was not set for " + sttsName, aStts.getLastSampleTime() != 0);
                    /* System.out.println("              lastSampleTime = " + aStts.getLastSampleTime() + 
                	    "  startTime = " + aStts.getStartTime());
                    System.out.println(aStts);*/
                }
            }
            if (k == n-2) connector.resetStats(); // test reset
            connection.disconnect();
            Thread.sleep(1000);  // connection interval
        }       
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
}
