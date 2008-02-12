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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockWritableListableRepository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
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

    protected void setUp() throws Exception {
        super.setUp();
        fakeRepo = "http://nowhere.com/";
        String url = getClass().getResource("/geronimo-plugins.xml").toString();
        int pos = url.lastIndexOf("/");
        testRepo = url.substring(0, pos);
        installer = new PluginInstallerGBean(new MockConfigManager(), new MockWritableListableRepository(), new MockConfigStore(),
                new BasicServerInfo("."), new ThreadPool() {
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
        }, new ArrayList<ServerInstance>());
    }

    public void testParsing() throws Exception {
        PluginListType list = installer.listPlugins(new URL(testRepo), null, null);
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

    static class MockConfigManager implements ConfigurationManager {

        public boolean isInstalled(Artifact configurationId) {
            return false;
        }

        public Artifact[] getInstalled(Artifact query) {
            return new Artifact[0];
        }

        public Artifact[] getLoaded(Artifact query) {
            return new Artifact[0];
        }

        public Artifact[] getRunning(Artifact query) {
            return new Artifact[0];
        }

        public boolean isLoaded(Artifact configID) {
            return false;
        }

        public List listStores() {
            return Collections.EMPTY_LIST;
        }

        public ConfigurationStore[] getStores() {
            return new ConfigurationStore[0];
        }

        public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
            return null;
        }

        public List listConfigurations(AbstractName store) throws NoSuchStoreException {
            return Collections.EMPTY_LIST;
        }

        public boolean isRunning(Artifact configurationId) {
            return false;
        }

        public List listConfigurations() {
            return null;
        }

        public boolean isConfiguration(Artifact artifact) {
            return false;
        }

        public Configuration getConfiguration(Artifact configurationId) {
            return null;
        }

        public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            return null;
        }

        public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            return null;
        }

        public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {

        }

        public ArtifactResolver getArtifactResolver() {
            return null;
        }

        public boolean isOnline() {
            return true;
        }

        public void setOnline(boolean online) {
        }

        public Collection<? extends Repository> getRepositories() {
            return null;
        }

        public LinkedHashSet<Artifact> sort(List<Artifact> ids, LifecycleMonitor monitor) throws InvalidConfigException, IOException, NoSuchConfigException, MissingDependencyException {
            return null;
        }
    }

}
