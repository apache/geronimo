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


package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

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
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.plugin.model.CopyFileType;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.threads.ThreadPool;
import org.apache.geronimo.testsupport.TestSupport;

/**
 * Tests the plugin installer GBean
 *
 * @version $Rev$ $Date$
 */
public class CopyFileTest extends TestSupport {
    private static int count = 0;
    private ServerInfo serverInfo;
    private ConfigurationStore configStore;
    private PluginInstallerGBean installer;
    private Artifact artifact = new Artifact("test", "module", "1.0", "car");

    protected void setUp() throws Exception {
        super.setUp();
        //set up test server location

        File serverBase = null;
        for (int i = 0; i < 100; i++) {
            serverBase = new File(new File(new File(new File(getBaseDir(), "target"), "test-resources"), "CopyFileTest"), "server" + count++);
            if (serverBase.mkdirs()) {
                break;
            }
            if (i == 100) {
                throw new RuntimeException("Could not create server base: " + serverBase);
            }
        }
        serverInfo = new BasicServerInfo(serverBase.getAbsolutePath(), false);
        File repoBase = new File(new File(new File(new File(new File(getBaseDir(), "src"), "test"), "resources"), "copyfiletest"), "repository");
        if (!repoBase.exists()) {
            throw new RuntimeException("Could not locate repo :" + repoBase);
        }
        Maven2Repository repo = new Maven2Repository(repoBase.toURI(), serverInfo, true);
        configStore = new RepositoryConfigurationStore(repo);
        installer = new PluginInstallerGBean(new MockConfigManager(),
                repo,
                configStore,
                serverInfo,
                new ThreadPool() {
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

    public void testCopyFile() throws Exception {
        CopyFileType copyFile = new CopyFileType();
        copyFile.setRelativeTo("geronimo");
        copyFile.setDestDir("");
        copyFile.setValue("emptyfile1");
        checkCopy(copyFile);
        copyFile.setDestDir("foo/bar");
        checkCopy(copyFile);
    }

    public void testCopyEmptyDir() throws Exception {
        CopyFileType copyFile = new CopyFileType();
        copyFile.setRelativeTo("geronimo");
        copyFile.setDestDir("");
        copyFile.setValue("emptydir1");
        checkCopy(copyFile);
        copyFile.setDestDir("foo/bar");
        checkCopy(copyFile);
    }

    public void testCopyDir() throws Exception {
        CopyFileType copyFile = new CopyFileType();
        copyFile.setRelativeTo("geronimo");
        copyFile.setDestDir("");
        copyFile.setValue("dir1");
        File dir = checkCopy(copyFile);
        assertTrue(new File(dir, "emptydir1").exists());
        assertTrue(new File(dir, "emptyfile1").exists());
        copyFile.setDestDir("foo/bar");
        dir = checkCopy(copyFile);
        assertTrue(new File(dir, "emptydir1").exists());
        assertTrue(new File(dir, "emptyfile1").exists());
    }

    public void testCopyDirs() throws Exception {
        CopyFileType copyFile = new CopyFileType();
        copyFile.setRelativeTo("geronimo");
        copyFile.setDestDir("");
        copyFile.setValue("dir1/");
        File dir = checkCopy(copyFile);
        assertTrue(new File(dir, "emptydir1").exists());
        assertTrue(new File(dir, "emptyfile1").exists());
        copyFile.setDestDir("foo/bar");
        dir = checkCopy(copyFile);
        assertTrue(new File(dir, "emptydir1").exists());
        assertTrue(new File(dir, "emptyfile1").exists());
    }

    private File checkCopy(CopyFileType copyFile) throws IOException {
        installer.copyFile(copyFile, artifact);
        File target;
        if (copyFile.getValue().endsWith("/")) {
            target = serverInfo.resolve("");
        } else {
            target = serverInfo.resolve(("".equals(copyFile.getDestDir())? "": copyFile.getDestDir() + '/') + copyFile.getValue());
        }
        assertTrue(target.getPath(), target.exists());
        return target;
    }

    private static class MockConfigManager implements ConfigurationManager {

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
    }
}
