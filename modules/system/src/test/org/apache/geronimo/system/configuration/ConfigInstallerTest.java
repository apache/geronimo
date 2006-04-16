/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.configuration;

import java.net.URL;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

/**
 * Tests the config installer GBean
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigInstallerTest extends TestCase {
    private URL testRepo;
    private ConfigurationInstaller installer;

    protected void setUp() throws Exception {
        String url = getClass().getResource("/geronimo-plugins.xml").toString();
        int pos = url.lastIndexOf("/");
        testRepo = new URL(url.substring(0, pos));
        installer = new ConfigInstallerGBean(new MockConfigManager(), new MockRepository(), new MockConfigStore(),
                new BasicServerInfo("."));
    }

    public void testParsing() throws Exception {
        ConfigurationList list = installer.listConfigurations(testRepo, null, null);
        assertEquals(0, list.getBackupRepositories().length);
        assertEquals(testRepo, list.getMainRepository());
        assertTrue(list.getConfigurations().length > 0);
        int prereqCount = 0;
        for (int i = 0; i < list.getConfigurations().length; i++) {
            ConfigurationMetadata metadata = list.getConfigurations()[i];
            prereqCount += metadata.getPrerequisites().length;
            for (int j = 0; j < metadata.getPrerequisites().length; j++) {
                ConfigurationMetadata.Prerequisite prerequisite = metadata.getPrerequisites()[j];
                assertFalse(prerequisite.isPresent());
            }
        }
        assertTrue(prereqCount > 0);
    }

    private static class MockConfigStore extends NullConfigurationStore {

    }

    private static class MockRepository implements WritableListableRepository {
        public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        }

        public void copyToRepository(InputStream source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        }

        public boolean contains(Artifact artifact) {
            return false;
        }

        public File getLocation(Artifact artifact) {
            return null;
        }

        public LinkedHashSet getDependencies(Artifact artifact) {
            return new LinkedHashSet();
        }

        public SortedSet list() {
            return new TreeSet();
        }

        public SortedSet list(Artifact query) {
            return new TreeSet();
        }
    }

    private static class MockConfigManager implements ConfigurationManager {
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

        public boolean isConfiguration(Artifact artifact) {
            return false;
        }

        public Configuration getConfiguration(Artifact configId) {
            return null;
        }

        public void loadConfiguration(Artifact configID) throws NoSuchConfigException, IOException, InvalidConfigException {
        }

        public Configuration loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, IOException, InvalidConfigException {
            return null;
        }

        public void unloadConfiguration(Artifact configID) throws NoSuchConfigException {
        }

        public void unloadConfiguration(Configuration configuration) throws NoSuchConfigException {
        }

        public void startConfiguration(Artifact configID) throws InvalidConfigException {
        }

        public void startConfiguration(Configuration configuration) throws InvalidConfigException {
        }

        public void stopConfiguration(Artifact configID) throws NoSuchConfigException {
        }

        public void stopConfiguration(Configuration configuration) throws NoSuchConfigException {
        }

        public boolean isRunning(Artifact configurationId) {
            return false;
        }

        public List listConfigurations() {
            return null;
        }

        public void uninstallConfiguration(Artifact configId) throws IOException, NoSuchConfigException {
        }
    }
}
