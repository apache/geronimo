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
package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationManagerTest extends TestCase {
    private static Kernel kernel;
    private Artifact artifact1;
    private Artifact artifact2;
    private Artifact artifact3;
    private Map configurations = new HashMap();
    private ConfigurationManager configurationManager;
    private AbstractName gbean1;
    private AbstractName gbean2;
    private AbstractName gbean3;

    public void testLoad() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));


        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    public void testStart() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        configurationManager.startConfiguration(artifact3);
        assertTrue(kernel.isLoaded(gbean1));
        assertTrue(kernel.isLoaded(gbean2));
        assertTrue(kernel.isLoaded(gbean3));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean3)) ;


        configurationManager.stopConfiguration(artifact3);
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    public void testRestart() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        Object g3 = kernel.getGBean(gbean3);
        assertSame(g1, kernel.getGBean(gbean1));
        assertSame(g2, kernel.getGBean(gbean2));
        assertSame(g3, kernel.getGBean(gbean3));

        LifecycleResults results = configurationManager.restartConfiguration(artifact1);

        // check the results
        assertTrue(results.wasRestarted(artifact1));
        assertTrue(results.wasRestarted(artifact2));
        assertTrue(results.wasRestarted(artifact3));

        // check the state of the kernel
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean3)) ;
        assertNotSame(g1, kernel.getGBean(gbean1));
        assertNotSame(g2, kernel.getGBean(gbean2));
        assertNotSame(g3, kernel.getGBean(gbean3));

        configurationManager.stopConfiguration(artifact3);
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        // bean3 should still be running because it is now user started due to the restart above
        assertTrue(kernel.isLoaded(gbean1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;

        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;

        // artifact 1 should still be loaded and running since it was user started above
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertTrue(kernel.isLoaded(gbean1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1));

        configurationManager.unloadConfiguration(artifact1);
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertFalse(kernel.isLoaded(gbean1));

    }

    public void testRestartException() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        kernel.getGBean(gbean3);

        // make gbean3 fail and restart all configs
        shouldFail.add(gbean3.getObjectName().getCanonicalName());
        LifecycleResults results = configurationManager.restartConfiguration(artifact1);

        // check the results
        assertTrue(results.wasRestarted(artifact1));
        assertTrue(results.wasRestarted(artifact2));
        assertTrue(results.wasFailed(artifact3));

        // all configuration should be loaded
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;

        // but configuration 3 should not be running
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact3));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertFalse(kernel.isLoaded(gbean3));

        // make sure that gbean 1 and 2 were recreated
        assertNotSame(g1, kernel.getGBean(gbean1));
        assertNotSame(g2, kernel.getGBean(gbean2));

        configurationManager.unloadConfiguration(artifact1);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    private static final Set shouldFail = new HashSet();
    private static void checkFail(String objectName) {
        if (shouldFail.contains(objectName)) {
            throw new RuntimeException("FAILING");
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        GBeanData artifactManagerData = buildGBeanData("name", "ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManagerData, getClass().getClassLoader());
        kernel.startGBean(artifactManagerData.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactManagerData.getAbstractName()));
        ArtifactManager artifactManager = (ArtifactManager) kernel.getGBean(artifactManagerData.getAbstractName());

        TestConfigStore configStore = new TestConfigStore();
        TestRepository testRepository = new TestRepository();
        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, testRepository);

        artifact1 = new Artifact("test", "1", "1.1", "bar");
        artifact2 = new Artifact("test", "2", "2.2", "bar");
        artifact3 = new Artifact("test", "3", "3.3", "bar");

        Environment e1 = new Environment();
        e1.setConfigId(artifact1);
        ConfigurationData configurationData1 = new ConfigurationData(e1, kernel.getNaming());
        configurationData1.setConfigurationStore(configStore);
        gbean1 = configurationData1.addGBean("gbean1", TestBean.getGBeanInfo()).getAbstractName();
        configurations.put(artifact1, configurationData1);

        Environment e2 = new Environment();
        e2.setConfigId(artifact2);
        e2.addDependency(new Artifact("test", "1", (Version) null, "bar"), ImportType.ALL);
        ConfigurationData configurationData2 = new ConfigurationData(e2, kernel.getNaming());
        gbean2 = configurationData2.addGBean("gbean2", TestBean.getGBeanInfo()).getAbstractName();
        configurationData2.setConfigurationStore(configStore);
        configurations.put(artifact2, configurationData2);

        Environment e3 = new Environment();
        e3.setConfigId(artifact3);
        e3.addDependency(new Artifact("test", "2", (Version) null, "bar"), ImportType.ALL);
        ConfigurationData configurationData3 = new ConfigurationData(e3, kernel.getNaming());
        gbean3 = configurationData3.addGBean("gbean3", TestBean.getGBeanInfo()).getAbstractName();
        configurationData3.setConfigurationStore(configStore);
        configurations.put(artifact3, configurationData3);


        configurationManager = new KernelConfigurationManager(kernel,
                Collections.singleton(configStore),
                null,
                null,
                artifactManager,
                artifactResolver,
                Collections.singleton(testRepository),
                KernelConfigurationManager.class.getClassLoader());
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }

    private class TestConfigStore extends NullConfigurationStore {
        public ConfigurationData loadConfiguration(Artifact configId) throws IOException, InvalidConfigException, NoSuchConfigException {
            return (ConfigurationData) configurations.get(configId);
        }

        public boolean containsConfiguration(Artifact configId) {
            return configurations.containsKey(configId);
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

        public Set resolve(Artifact configId, String moduleName, String pattern) throws NoSuchConfigException, MalformedURLException {
            throw new UnsupportedOperationException();
        }
    }

    private GBeanData buildGBeanData(String key, String value, GBeanInfo info) {
        AbstractName abstractName = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), value, key);
        return new GBeanData(abstractName, info);
    }

    private class TestRepository implements ListableRepository {
        public SortedSet list() {
            return new TreeSet(configurations.keySet());
        }

        public SortedSet list(Artifact query) {
            TreeSet artifacts = new TreeSet();
            for (Iterator iterator = configurations.keySet().iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                if (query.matches(artifact)) {
                    artifacts.add(artifact);
                }
            }
            return artifacts;
        }

        public boolean contains(Artifact artifact) {
            return configurations.containsKey(artifact);
        }

        public File getLocation(Artifact artifact) {
            throw new UnsupportedOperationException();
        }

        public LinkedHashSet getDependencies(Artifact artifact) {
            return new LinkedHashSet();
        }
    }

    public static class TestBean {
        public TestBean(String objectName) {
            checkFail(objectName);
        }

        private static final GBeanInfo GBEAN_INFO;
        static {
            GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(TestBean.class);
            builder.addAttribute("objectName", String.class, false);
            builder.setConstructor(new String[] {"objectName"});
            GBEAN_INFO = builder.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }
    }
}
