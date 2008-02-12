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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.LifecycleMonitor;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;


/**
 * @version $Rev$ $Date$
 */
public class SingleFileHotDeployerTest extends TestCase {
    private static final long NOW = System.currentTimeMillis();
    private static final long PAST = NOW - 1000;

    private final Artifact NEW_ID = new Artifact("new", "new", "new", "new");
    private final Artifact OLD_VERSION_ID = new Artifact("new", "new", "old", "new");
    private final Artifact DIFFERENT_ID = new Artifact("different", "different", "different", "different");
    
    private File basedir = new File(System.getProperty("basedir"));
    
    private File dir;
    private String[] watchPaths;
    private MockConfigurationBuilder builder;
    private MockConfigurationStore store;
    private MockConfigurationManager configurationManager;

    private ArtifactResolver artifactResolver = new DefaultArtifactResolver(null, null);
    private ArrayList existingConfigurationInfos = new ArrayList();

    private boolean shouldUninstall;
    private boolean shouldUnload;
    private boolean shouldLoad;
    private boolean shouldStart;
    private boolean isConfigurationAlreadyLoaded;
    private boolean isConfigurationInstalled;

    private File watchFile1;
    private File watchFile2;

    protected void setUp() throws Exception {
        super.setUp();

        dir = new File(basedir, "target/deployTest");
        dir.mkdirs();

        File someFile = new File(dir, "someFile");
        someFile.createNewFile();

        String watch1 = "watch1";
        String watch2 = "watch2";
        watchPaths = new String[]{watch1, watch2};

        watchFile1 = new File(dir, watch1);
        watchFile2 = new File(dir, watch2);

        builder = new MockConfigurationBuilder();
        store = new MockConfigurationStore();
        configurationManager = new MockConfigurationManager();
    }

    private void touch(File file, long lastModified) throws IOException {
        file.createNewFile();
        file.setLastModified(lastModified);
    }

    public void testDeploy() throws Exception {
        shouldUninstall = false;
        shouldUnload = false;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                false);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertTrue(singleFileHotDeployer.wasDeployed());
        assertFalse(singleFileHotDeployer.isForceDeploy());
    }

    public void testRedeploySame() throws Exception {
        shouldUninstall = true;
        shouldUnload = true;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        touch(watchFile1, NOW);
        touch(watchFile2, NOW);

        existingConfigurationInfos.add(new ConfigurationInfo(null, NEW_ID, ConfigurationModuleType.CAR, PAST, null, null, dir));

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                false);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertTrue(singleFileHotDeployer.wasDeployed());
        assertFalse(singleFileHotDeployer.isForceDeploy());
    }

    public void testRedeployCompletelyNew() throws Exception {
        shouldUninstall = true;
        shouldUnload = true;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        touch(watchFile1, NOW);
        touch(watchFile2, NOW);

        existingConfigurationInfos.add(new ConfigurationInfo(null, DIFFERENT_ID, ConfigurationModuleType.CAR, PAST, null, null, dir));

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                false);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertTrue(singleFileHotDeployer.wasDeployed());
        assertFalse(singleFileHotDeployer.isForceDeploy());
    }

    public void testRedeployNewVersion() throws Exception {
        shouldUninstall = true;
        shouldUnload = true;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        touch(watchFile1, NOW);
        touch(watchFile2, NOW);

        existingConfigurationInfos.add(new ConfigurationInfo(null, OLD_VERSION_ID, ConfigurationModuleType.CAR, PAST, null, null, dir));

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                false);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertTrue(singleFileHotDeployer.wasDeployed());
        assertFalse(singleFileHotDeployer.isForceDeploy());
    }

    public void testNoRedeploy() throws Exception {
        shouldUninstall = false;
        shouldUnload = false;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        touch(watchFile1, PAST);
        touch(watchFile2, PAST);

        existingConfigurationInfos.add(new ConfigurationInfo(null, NEW_ID, ConfigurationModuleType.CAR, NOW, null, null, dir));

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                false);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertFalse(singleFileHotDeployer.wasDeployed());
        assertFalse(singleFileHotDeployer.isForceDeploy());
    }

    public void testForceRedeploy() throws Exception {
        shouldUninstall = true;
        shouldUnload = true;
        shouldLoad = true;
        shouldStart = true;
        isConfigurationAlreadyLoaded = true;
        isConfigurationInstalled = false;

        touch(watchFile1, PAST);
        touch(watchFile2, PAST);

        existingConfigurationInfos.add(new ConfigurationInfo(null, OLD_VERSION_ID, ConfigurationModuleType.CAR, NOW, null, null, dir));

        SingleFileHotDeployer singleFileHotDeployer = new SingleFileHotDeployer(dir,
                watchPaths,
                Collections.singleton(builder),
                store,
                configurationManager,
                true);
        assertEquals(NEW_ID, singleFileHotDeployer.getConfigurationId());
        assertEquals(dir, singleFileHotDeployer.getDir());
        assertTrue(singleFileHotDeployer.wasDeployed());
        assertTrue(singleFileHotDeployer.isForceDeploy());
    }

    private class MockConfigurationBuilder implements ConfigurationBuilder {
        public Object getDeploymentPlan(File planFile, JarFile module, ModuleIDBuilder idBuilder) throws DeploymentException {
            return new Object();
        }

        public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
            return NEW_ID;
        }

        public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile module, Collection configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
            return new DeploymentContext(dir,
                    dir,
                    new Environment(configId),
                    null,
                    ConfigurationModuleType.CAR,
                    new Jsr77Naming(),
                    new SimpleConfigurationManager(Collections.singletonList(store), artifactResolver, Collections.EMPTY_SET));
        }
    }

    private class MockConfigurationStore implements ConfigurationStore {
        public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
            throw new UnsupportedOperationException();
        }

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        }

        public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
            throw new UnsupportedOperationException();
        }

        public ConfigurationData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            throw new UnsupportedOperationException();
        }

        public boolean containsConfiguration(Artifact configId) {
            throw new UnsupportedOperationException();
        }

        public String getObjectName() {
            throw new UnsupportedOperationException();
        }

        public AbstractName getAbstractName() {
            throw new UnsupportedOperationException();
        }

        public List listConfigurations() {
            throw new UnsupportedOperationException();
        }

        public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
            throw new UnsupportedOperationException();
        }

        public Set resolve(Artifact configId, String moduleName, String path) throws NoSuchConfigException, MalformedURLException {
            throw new UnsupportedOperationException();
        }

        public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
            throw new UnsupportedOperationException();
        }
    }

    private class MockConfigurationManager implements ConfigurationManager {
        private ConfigurationData loadedConfigurationData;

        public boolean isInstalled(Artifact configurationId) {
            return isConfigurationInstalled;
        }

        public boolean isLoaded(Artifact configurationId) {
            return isConfigurationAlreadyLoaded;
        }

        public boolean isRunning(Artifact configurationId) {
            throw new UnsupportedOperationException();
        }

        public Artifact[] getInstalled(Artifact query) {
            throw new UnsupportedOperationException();
        }

        public Artifact[] getLoaded(Artifact query) {
            throw new UnsupportedOperationException();
        }

        public Artifact[] getRunning(Artifact query) {
            throw new UnsupportedOperationException();
        }

        public List listConfigurations() {
            return existingConfigurationInfos;
        }

        public List listStores() {
            throw new UnsupportedOperationException();
        }

        public ConfigurationStore[] getStores() {
            return new ConfigurationStore[]{store};
        }

        public ConfigurationStore getStoreForConfiguration(Artifact configuration) {
            throw new UnsupportedOperationException();
        }

        public List listConfigurations(AbstractName store) throws NoSuchStoreException {
            throw new UnsupportedOperationException();
        }

        public boolean isConfiguration(Artifact artifact) {
            throw new UnsupportedOperationException();
        }

        public Configuration getConfiguration(Artifact configurationId) {
            try {
                return new Configuration(Collections.EMPTY_SET, loadedConfigurationData, new ConfigurationResolver(configurationId, dir), null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            assertTrue("Did not expect configuration to be loaded " + configurationId, shouldLoad);
            return null;
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            loadedConfigurationData = configurationData;
            return null;
        }

        public LifecycleResults loadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults loadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId) throws NoSuchConfigException {
            assertTrue("Did not expect configuration to be unloaded " + configurationId, shouldUnload);
            return null;
        }

        public LifecycleResults unloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults startConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            assertTrue("Did not expect configuration to be started " + configurationId, shouldStart);
            return null;
        }

        public LifecycleResults startConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults stopConfiguration(Artifact configurationId) throws NoSuchConfigException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults stopConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults restartConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults restartConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(Artifact configurationId, Version version, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public LifecycleResults reloadConfiguration(ConfigurationData configurationData, LifecycleMonitor monitor) throws NoSuchConfigException, LifecycleException {
            throw new UnsupportedOperationException();
        }

        public void uninstallConfiguration(Artifact configurationId) throws IOException, NoSuchConfigException {
            assertTrue("Did not expect configuration to be uninstalled " + configurationId, shouldUninstall);
        }

        public ArtifactResolver getArtifactResolver() {
            return artifactResolver;
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
