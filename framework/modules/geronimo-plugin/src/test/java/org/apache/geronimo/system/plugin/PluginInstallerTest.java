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
package org.apache.geronimo.system.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockWritableListableRepository;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * Tests the plugin installer GBean
 *
 * @version $Rev$ $Date$
 */
public class PluginInstallerTest extends TestCase {
    private String fakeRepo;
    private String testRepo;
    private PluginInstaller installer;
    private String installedPluginsList = "var/config/installedPlugins.properties";
 
    protected void setUp() throws Exception {
        super.setUp();
        fakeRepo = "http://nowhere.com/";
        String url = getClass().getResource("/geronimo-plugins.xml").toString();
        int pos = url.lastIndexOf("/");
        testRepo = url.substring(0, pos);
        ServerInfo serverInfo = new BasicServerInfo(".");
        installer = new PluginInstallerGBean(new MockConfigurationManager(), new MockWritableListableRepository(), new MockConfigStore(),
                installedPluginsList, serverInfo, new ThreadPool() {
            public int getPoolSize() {
                return 0;
            }

            public int getMaximumPoolSize() {
                return 0;
            }

            public int getActiveCount() {
                return 0;
            }

            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            public void execute(String consumerName, Runnable runnable) {
                new Thread(runnable).start();
            }
        }, new ArrayList<ServerInstance>(),
                new PluginRepositoryDownloader(Collections.<String>emptyList(), "", null, false, serverInfo, null, null));
    }

    public void testParsing() throws Exception {
        PluginListType list = installer.listPlugins(new URL(testRepo));
        assertNotNull(list);
        assertEquals(1, list.getDefaultRepository().size());
        assertEquals(fakeRepo, list.getDefaultRepository().get(0));
        assertTrue(list.getPlugin().size() > 0);
        int prereqCount = 0;
        for (PluginType metadata: list.getPlugin()) {
            PluginArtifactType instance = metadata.getPluginArtifact().get(0);
            prereqCount += instance.getPrerequisite().size();
//            for (PrerequisiteType prerequisite: instance.getPrerequisite()) {
//                assertFalse(prerequisite.getId());
//            }
        }
        assertTrue(prereqCount > 0);
    }


}
