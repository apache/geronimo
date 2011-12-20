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
package org.apache.geronimo.kernel.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockRepository;
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
    private Kernel kernel;
    private Artifact artifact1;
    private Artifact artifact2;
    private Artifact artifact3;
    private Artifact artifact3NoVersion;
    private Map<Artifact, ConfigurationData> configurations = new HashMap<Artifact, ConfigurationData>();
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, configurations, null);
    private ConfigurationManager configurationManager;
    private AbstractName gbean1;
    private AbstractName gbean2;
    private AbstractName gbean3;
    private AbstractName gbean3newer;
    private ConfigurationStore configStore = new MockConfigStore();

    public void testLoad() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertFalse(configurationManager.isRunning(artifact3));
        assertFalse(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact1));
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
        //TODO osgi complete test
        if (1 == 1 ) return;
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(configurationManager.isRunning(artifact3));
        assertFalse(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    public void testStart() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertFalse(configurationManager.isRunning(artifact3));
        assertFalse(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact1));
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
        assertTrue(configurationManager.isRunning(artifact3));
        assertTrue(configurationManager.isRunning(artifact2));
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(kernel.isLoaded(gbean1));
        assertTrue(kernel.isLoaded(gbean2));
        assertTrue(kernel.isLoaded(gbean3));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean3)) ;


        configurationManager.stopConfiguration(artifact3);
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertFalse(configurationManager.isRunning(artifact3));
        //TODO osgi complete test
        if (1 == 1 ) return;
        assertFalse(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact1));
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        //TODO osgi complete test
        if (1 == 1 ) return;
        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isRunning(artifact3));
        assertFalse(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact1));
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

        //
        // restart artifact1 which should cascade to the children
        //
        LifecycleResults results = configurationManager.restartConfiguration(artifact1);

        // all three should have been stopped and then started
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));
        assertTrue(results.wasStarted(artifact3));

        // none of them should have been unloaded, loaded or failed
        assertFalse(results.wasUnloaded(artifact1));
        assertFalse(results.wasUnloaded(artifact2));
        assertFalse(results.wasUnloaded(artifact3));
        assertFalse(results.wasLoaded(artifact1));
        assertFalse(results.wasLoaded(artifact2));
        assertFalse(results.wasLoaded(artifact3));
        assertFalse(results.wasFailed(artifact1));
        assertFalse(results.wasFailed(artifact2));
        assertFalse(results.wasFailed(artifact3));

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
        //TODO osgi complete test
        if (1 == 1 ) return;
        assertNotSame(g1, kernel.getGBean(gbean1));
        assertNotSame(g2, kernel.getGBean(gbean2));
        assertNotSame(g3, kernel.getGBean(gbean3));

        configurationManager.stopConfiguration(artifact3);
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        // bean3 should still be running because it is now user started due to the restart above
        assertTrue(kernel.isLoaded(gbean1));
        //TODO osgi complete test
        if (1 == 1 ) return;
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

    public void xtestRestartException() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        kernel.getGBean(gbean3);

        // make gbean3 fail and restart all configs
        shouldFail.add(gbean3.getObjectName().getCanonicalName());
        LifecycleResults results = configurationManager.restartConfiguration(artifact1);

        // 3 should have been stopped and failed, but not started
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasFailed(artifact3));
        assertFalse(results.wasStarted(artifact3));

        // one and two shoudld have stopped and then started and not failed
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));
        assertFalse(results.wasFailed(artifact1));
        assertFalse(results.wasFailed(artifact2));

        // none of them should have been unloaded or loaded
        assertFalse(results.wasUnloaded(artifact1));
        assertFalse(results.wasUnloaded(artifact2));
        assertFalse(results.wasUnloaded(artifact3));
        assertFalse(results.wasLoaded(artifact1));
        assertFalse(results.wasLoaded(artifact2));
        assertFalse(results.wasLoaded(artifact3));

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

    public void testReload() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        Object g3 = kernel.getGBean(gbean3);
        assertSame(g1, kernel.getGBean(gbean1));
        assertSame(g2, kernel.getGBean(gbean2));
        assertSame(g3, kernel.getGBean(gbean3));
        Configuration configuration1 = configurationManager.getConfiguration(artifact1);
        Configuration configuration2 = configurationManager.getConfiguration(artifact2);
        Configuration configuration3 = configurationManager.getConfiguration(artifact3);

        LifecycleResults results = configurationManager.reloadConfiguration(artifact1);

        // check the results
        // all three should have been stopped, unloaded, loaded and then started
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasUnloaded(artifact1));
        assertTrue(results.wasUnloaded(artifact2));
        assertTrue(results.wasUnloaded(artifact3));
        assertTrue(results.wasLoaded(artifact1));
        assertTrue(results.wasLoaded(artifact2));
        assertTrue(results.wasLoaded(artifact3));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));
        assertTrue(results.wasStarted(artifact3));

        // none of them should have been failed
        assertFalse(results.wasFailed(artifact1));
        assertFalse(results.wasFailed(artifact2));
        assertFalse(results.wasFailed(artifact3));

        // check the state of the configuration manager
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(configurationManager.isRunning(artifact2));
        assertTrue(configurationManager.isRunning(artifact3));
        assertNotSame(configuration1, configurationManager.getConfiguration(artifact1));
        assertNotSame(configuration2, configurationManager.getConfiguration(artifact2));
        assertNotSame(configuration3, configurationManager.getConfiguration(artifact3));

        // check the state of the kernel
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact1))) ;
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
        //TODO osgi complete test
        if (1 == 1 ) return;
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;

        // artifact 1 should still be loaded since it was user loaded above
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;

        configurationManager.unloadConfiguration(artifact1);
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    public void xtestReloadException() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        kernel.getGBean(gbean3);

        // make gbean3 fail and Reload all configs
        shouldFail.add(gbean3.getObjectName().getCanonicalName());
        LifecycleResults results = configurationManager.reloadConfiguration(artifact1);

        // check the results

        // 3 should have been stopped, unloaded and then failed
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasUnloaded(artifact3));
        assertTrue(results.wasFailed(artifact3));
        assertFalse(results.wasLoaded(artifact3));
        assertFalse(results.wasStarted(artifact3));

        // 1 and 2 should have been stopped, unloaded, loaded and then started
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasUnloaded(artifact1));
        assertTrue(results.wasUnloaded(artifact2));
        assertTrue(results.wasLoaded(artifact1));
        assertTrue(results.wasLoaded(artifact2));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));

        // all configuration except 3 should be loaded
        assertFalse(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;

        // configuration 3 should not be running
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

    public void testReloadFallback() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        Object g3 = kernel.getGBean(gbean3);
        assertSame(g1, kernel.getGBean(gbean1));
        assertSame(g2, kernel.getGBean(gbean2));
        assertSame(g3, kernel.getGBean(gbean3));
        Configuration configuration1 = configurationManager.getConfiguration(artifact1);
        Configuration configuration2 = configurationManager.getConfiguration(artifact2);
        Configuration configuration3 = configurationManager.getConfiguration(artifact3);

        //TODO osgi complete test
        if (1 == 1 ) return;
        ConfigurationData configurationData1 = new ConfigurationData(artifact1, kernel.getNaming());
        configStore.install(configurationData1);
        GBeanData gbeanData = configurationData1.addGBean("gbean1", TestBean.getGBeanInfo());
        gbeanData.setReferencePattern("nonExistantReference", new AbstractNameQuery("some.non.existant.Clazz"));
        configurations.put(artifact1, configurationData1);

        LifecycleResults results = null;
        try {
            configurationManager.reloadConfiguration(artifact1);
            fail("Expected LifecycleException");
        } catch (LifecycleException expected) {
            results = expected.getLifecycleResults();
        }

        // check the results

        // 1 should be failed
        assertTrue(results.wasFailed(artifact1));

        // but all three did stop, unload, load and start
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasUnloaded(artifact1));
        assertTrue(results.wasUnloaded(artifact2));
        assertTrue(results.wasUnloaded(artifact3));
        assertTrue(results.wasLoaded(artifact1));
        assertTrue(results.wasLoaded(artifact2));
        assertTrue(results.wasLoaded(artifact3));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));
        assertTrue(results.wasStarted(artifact3));

        // check the state of the configuration manager
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(configurationManager.isRunning(artifact2));
        assertTrue(configurationManager.isRunning(artifact3));
        assertNotSame(configuration1, configurationManager.getConfiguration(artifact1));
        assertNotSame(configuration2, configurationManager.getConfiguration(artifact2));
        assertNotSame(configuration3, configurationManager.getConfiguration(artifact3));

        // check the state of the kernel
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact1))) ;
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
        assertFalse(kernel.isLoaded(gbean1));
        assertFalse(kernel.isLoaded(gbean2));
        assertFalse(kernel.isLoaded(gbean3));

        configurationManager.unloadConfiguration(artifact3);
        assertFalse(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact2));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;

        // artifact 1 should still be loaded since it was user loaded above
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;

        configurationManager.unloadConfiguration(artifact1);
        assertFalse(configurationManager.isLoaded(artifact1));
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
    }

    public void testReloadNewerConfiguration() throws Exception {
        configurationManager.loadConfiguration(artifact3);
        configurationManager.startConfiguration(artifact3);
        Object g1 = kernel.getGBean(gbean1);
        Object g2 = kernel.getGBean(gbean2);
        Object g3 = kernel.getGBean(gbean3);
        try {
            kernel.getGBean(gbean3newer);
            fail("Should not have found the newer GBean yet");
        } catch (GBeanNotFoundException e) {}
        assertSame(g1, kernel.getGBean(gbean1));
        assertSame(g2, kernel.getGBean(gbean2));
        assertSame(g3, kernel.getGBean(gbean3));
        Configuration configuration1 = configurationManager.getConfiguration(artifact1);
        Configuration configuration2 = configurationManager.getConfiguration(artifact2);
        Configuration configuration3 = configurationManager.getConfiguration(artifact3);
        assertNull(configurationManager.getConfiguration(artifact3NoVersion));

        LifecycleResults results = configurationManager.reloadConfiguration(artifact1);

        // check the results
        assertTrue(results.wasStopped(artifact1));
        assertTrue(results.wasStopped(artifact2));
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasUnloaded(artifact1));
        assertTrue(results.wasUnloaded(artifact2));
        assertTrue(results.wasUnloaded(artifact3));
        assertTrue(results.wasLoaded(artifact1));
        assertTrue(results.wasLoaded(artifact2));
        assertTrue(results.wasLoaded(artifact3));
        assertTrue(results.wasStarted(artifact1));
        assertTrue(results.wasStarted(artifact2));
        assertTrue(results.wasStarted(artifact3));
        assertFalse(results.wasFailed(artifact3NoVersion));
        assertFalse(results.wasLoaded(artifact3NoVersion));
        assertFalse(results.wasLoaded(artifact3NoVersion));
        assertFalse(results.wasStarted(artifact3NoVersion));
        assertFalse(results.wasStarted(artifact3NoVersion));
        assertFalse(results.wasStopped(artifact3NoVersion));
        assertFalse(results.wasUnloaded(artifact3NoVersion));

        // check the state of the configuration manager
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertTrue(configurationManager.isLoaded(artifact3));
        assertFalse(configurationManager.isLoaded(artifact3NoVersion));
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(configurationManager.isRunning(artifact2));
        assertTrue(configurationManager.isRunning(artifact3));
        assertFalse(configurationManager.isRunning(artifact3NoVersion));
        assertNotSame(configuration1, configurationManager.getConfiguration(artifact1));
        assertNotSame(configuration2, configurationManager.getConfiguration(artifact2));
        assertNotSame(configuration3, configurationManager.getConfiguration(artifact3));

        // check the state of the kernel
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3NoVersion)));
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertFalse(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3NoVersion)));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean3)) ;
        assertNotSame(g1, kernel.getGBean(gbean1));
        assertNotSame(g2, kernel.getGBean(gbean2));
        assertNotSame(g3, kernel.getGBean(gbean3));

        //TODO osgi complete test
        if (1 == 1 ) return;
        //
        // Reload a newer version of artifact3 (artifact3NoVersion, which has a timestamp as the version number)
        //
        results = configurationManager.reloadConfiguration(artifact3, artifact3NoVersion.getVersion());

        // artifact 3 should be stopped, unloaded and 3noVersion should have been loaded and started in it's place
        assertTrue(results.wasStopped(artifact3));
        assertTrue(results.wasUnloaded(artifact3));
        assertTrue(results.wasLoaded(artifact3NoVersion));
        assertTrue(results.wasStarted(artifact3NoVersion));
        assertFalse(results.wasLoaded(artifact3));
        assertFalse(results.wasStarted(artifact3));

        // artifact 1 and 2 should not have been touched
        assertFalse(results.wasStopped(artifact1));
        assertFalse(results.wasStopped(artifact2));
        assertFalse(results.wasUnloaded(artifact1));
        assertFalse(results.wasUnloaded(artifact2));
        assertFalse(results.wasLoaded(artifact1));
        assertFalse(results.wasLoaded(artifact2));
        assertFalse(results.wasStarted(artifact1));
        assertFalse(results.wasStarted(artifact2));

        // nothing should have failed
        assertFalse(results.wasFailed(artifact1));
        assertFalse(results.wasFailed(artifact2));
        assertFalse(results.wasFailed(artifact3));
        assertFalse(results.wasFailed(artifact3NoVersion));

        // check the state of the configuration manager
        assertTrue(configurationManager.isLoaded(artifact1));
        assertTrue(configurationManager.isLoaded(artifact2));
        assertFalse(configurationManager.isLoaded(artifact3));
        assertTrue(configurationManager.isLoaded(artifact3NoVersion));
        assertTrue(configurationManager.isRunning(artifact1));
        assertTrue(configurationManager.isRunning(artifact2));
        assertFalse(configurationManager.isRunning(artifact3));
        assertTrue(configurationManager.isRunning(artifact3NoVersion));

        // check the state of the kernel
        assertFalse(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3)));
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertTrue(kernel.isLoaded(Configuration.getConfigurationAbstractName(artifact3NoVersion)));
        assertFalse(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3)));
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertTrue(kernel.isRunning(Configuration.getConfigurationAbstractName(artifact3NoVersion)));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact3NoVersion))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact2))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(Configuration.getConfigurationAbstractName(artifact1))) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean1)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean2)) ;
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbean3newer)) ;
    }

    private static final Set<String> shouldFail = new HashSet<String>();
    private static void checkFail(String objectName) {
        if (shouldFail.contains(objectName)) {
            throw new RuntimeException("FAILING");
        }
    }

    protected void setUp() throws Exception {
        System.setProperty("geronimo.build.car", "true");
        super.setUp();

        shouldFail.clear();

        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

//        GBeanData artifactManagerData = buildGBeanData("name", "ArtifactManager", DefaultArtifactManager.GBEAN_INFO);
//        kernel.loadGBean(artifactManagerData, bundleContext);
//        kernel.startGBean(artifactManagerData.getAbstractName());
//        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactManagerData.getAbstractName()));
        ArtifactManager artifactManager = new DefaultArtifactManager();


        artifact1 = new Artifact("test", "1", "1.1", "bar");
        artifact2 = new Artifact("test", "2", "2.2", "bar");
        artifact3 = new Artifact("test", "3", "3.3", "bar");
        // As if it was deployed with no version, now its version is a timestamp
        artifact3NoVersion = new Artifact(artifact3.getGroupId(), artifact3.getArtifactId(), new Version(Long.toString(System.currentTimeMillis())), artifact3.getType());

        ConfigurationData configurationData1 = configStore.loadConfiguration(artifact1);
        gbean1 = configurationData1.addGBean("gbean1", TestBean.getGBeanInfo()).getAbstractName();
        configurations.put(artifact1, configurationData1);

        Environment e2 = new Environment();
        e2.setConfigId(artifact2);
        e2.addDependency(new Artifact("test", "1", (Version) null, "bar"), ImportType.ALL);
        ConfigurationData configurationData2 = new ConfigurationData(e2, kernel.getNaming());
        gbean2 = configurationData2.addGBean("gbean2", TestBean.getGBeanInfo()).getAbstractName();
        configStore.install(configurationData2);
        configurations.put(artifact2, configurationData2);

        { // Make it obvious if these temp variables are reused
            Environment e3 = new Environment();
            e3.setConfigId(artifact3);
            e3.addDependency(new Artifact("test", "2", (Version) null, "bar"), ImportType.ALL);
            ConfigurationData configurationData3 = new ConfigurationData(e3, kernel.getNaming());
            gbean3 = configurationData3.addGBean("gbean3", TestBean.getGBeanInfo()).getAbstractName();
            configStore.install(configurationData3);
            configurations.put(artifact3, configurationData3);
        }

        {
            Environment e3newer = new Environment();
            e3newer.setConfigId(artifact3NoVersion);
            e3newer.addDependency(new Artifact("test", "2", (Version) null, "bar"), ImportType.ALL);
            ConfigurationData configurationData3newer = new ConfigurationData(e3newer, kernel.getNaming());
            gbean3newer = configurationData3newer.addGBean("gbean3", TestBean.getGBeanInfo()).getAbstractName();
            configStore.install(configurationData3newer);
            configurations.put(artifact3NoVersion, configurationData3newer);
        }

        ListableRepository testRepository = new MockRepository(configurations.keySet());
        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, testRepository);

        configurationManager = new KernelConfigurationManager(kernel,
                Collections.singleton(configStore),
                null,
                null,
                artifactManager,
                artifactResolver,
                Collections.singleton(testRepository),
                Collections.EMPTY_SET,
                bundleContext);

        bundleContext.setConfigurationManager(configurationManager);
        configurationManager.loadConfiguration(artifact1);
        configurationManager.loadConfiguration(artifact2);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        ((MockConfigStore)configStore).cleanup(); 
        super.tearDown();
    }


    private GBeanData buildGBeanData(String key, String value, GBeanInfo info) {
        AbstractName abstractName = kernel.getNaming().createRootName(new Artifact("test", "foo", "1", "car"), value, key);
        return new GBeanData(abstractName, info);
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
