/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.kernel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private Kernel kernel;
    private ObjectName gbeanName1;
    private ObjectName gbeanName2;
    private ConfigurationData configurationData;
    private ConfigurationManager configurationManager;

    public void testConfigLifecycle() throws Exception {

        // load -- config should be running and gbean registered but not started
        Configuration configuration = configurationManager.loadConfiguration(configurationData);
        ObjectName configurationName = new ObjectName(configuration.getObjectName());

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(configurationName));
        assertNotNull(configuration.getConfigurationClassLoader());

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));

        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName2));

        // start -- gbeans should now be started
        configurationManager.startConfiguration(configuration);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName2));

        assertEquals(new Integer(1), kernel.getAttribute(gbeanName1, "finalInt"));
        assertEquals("1234", kernel.getAttribute(gbeanName1, "value"));
        assertEquals(new Integer(3), kernel.getAttribute(gbeanName2, "finalInt"));

        kernel.setAttribute(gbeanName2, "mutableInt", new Integer(44));
        assertEquals(new Integer(44), kernel.getAttribute(gbeanName2, "mutableInt"));

        kernel.invoke(gbeanName2, "doSetMutableInt", new Object[]{new Integer(55)}, new String[]{"int"});
        assertEquals(new Integer(55), kernel.getAttribute(gbeanName2, "mutableInt"));

        assertEquals("no endpoint", kernel.invoke(gbeanName1, "checkEndpoint", null, null));
        assertEquals("endpointCheck", kernel.invoke(gbeanName2, "checkEndpoint", null, null));

        assertEquals(new Integer(0), kernel.invoke(gbeanName1, "checkEndpointCollection", null, null));
        assertEquals(new Integer(1), kernel.invoke(gbeanName2, "checkEndpointCollection", null, null));

        kernel.setAttribute(gbeanName2, "endpointMutableInt", new Integer(99));
        assertEquals(new Integer(99), kernel.getAttribute(gbeanName2, "endpointMutableInt"));
        assertEquals(new Integer(99), kernel.getAttribute(gbeanName1, "mutableInt"));


        // stop -- gbeans should now be started, but still registered
        configurationManager.stopConfiguration(configuration);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));

        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName2));


        // unload -- configuration and gbeans should be unloaded
        configurationManager.unloadConfiguration(configuration);

        assertFalse(kernel.isLoaded(configurationName));
        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));

    }

    public void testConfigStartStopRestart() throws Exception {
        // load -- config should be running and gbean registered but not started
        Configuration configuration = configurationManager.loadConfiguration(configurationData);
        ObjectName configurationName = new ObjectName(configuration.getObjectName());

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(configurationName));
        assertNotNull(configuration.getConfigurationClassLoader());

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName2));


        // start -- gbeans should now be started
        configurationManager.startConfiguration(configuration);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName2));


        // stop -- gbeans should now be started, but still registered
        configurationManager.stopConfiguration(configuration);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.STOPPED_INDEX, kernel.getGBeanState(gbeanName2));


        // restart -- gbeans should now be started
        configurationManager.startConfiguration(configuration);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName2));

        // unload -- configuration and gbeans should be unloaded
        configurationManager.unloadConfiguration(configuration);

        assertFalse(kernel.isLoaded(configurationName));
        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));

    }

    public void testAddToConfig() throws Exception {
        // load and start the config
        Configuration configuration = configurationManager.loadConfiguration(configurationData);
        assertNotNull(configuration.getConfigurationClassLoader());

        ObjectName gbeanName3 = new ObjectName("geronimo.test:name=MyMockGMBean3");
        try {
            kernel.getGBeanState(gbeanName3);
            fail("Gbean should not be found yet");
        } catch (GBeanNotFoundException e) {
        }
        GBeanData mockBean3 = new GBeanData(gbeanName3, MockGBean.getGBeanInfo());
        mockBean3.setAttribute("value", "1234");
        mockBean3.setAttribute("name", "child");
        mockBean3.setAttribute("finalInt", new Integer(1));
        configuration.addGBean(mockBean3, true);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName3));
        assertEquals(new Integer(1), kernel.getAttribute(gbeanName3, "finalInt"));
        assertEquals("1234", kernel.getAttribute(gbeanName3, "value"));
        assertEquals("child", kernel.getAttribute(gbeanName3, "name"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        ObjectName artifactManagerName = new ObjectName(":j2eeType=ArtifactManager");
        GBeanData artifactManagerData = new GBeanData(artifactManagerName, DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManagerData, getClass().getClassLoader());
        kernel.startGBean(artifactManagerName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactManagerName));

        ObjectName artifactResolverName = new ObjectName(":j2eeType=ArtifactResolver");
        GBeanData artifactResolverData = new GBeanData(artifactResolverName, DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerName);
        kernel.loadGBean(artifactResolverData, getClass().getClassLoader());
        kernel.startGBean(artifactResolverName);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactResolverName));

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerName);
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverName);
        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerName);
        configurationManager = ConfigurationUtil.getConfigurationManager(kernel);


        ArrayList gbeans = new ArrayList();

        gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
        GBeanData mockBean1 = new GBeanData(gbeanName1, MockGBean.getGBeanInfo());
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));
        gbeans.add(mockBean1);

        gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
        GBeanData mockBean2 = new GBeanData(gbeanName2, MockGBean.getGBeanInfo());
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePatterns("MockEndpoint", Collections.singleton(gbeanName1));
        mockBean2.setReferencePatterns("EndpointCollection", Collections.singleton(gbeanName1));
        gbeans.add(mockBean2);

        Environment environment = new Environment();
        environment.setConfigId(new Artifact("geronimo", "test", "1", "car"));

        configurationData = new ConfigurationData(ConfigurationModuleType.CAR, null, gbeans, null, environment, null);
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }

    public static class MockConfigStore implements ConfigurationStore {

        URL baseURL;

        public MockConfigStore() {
        }

        public MockConfigStore(URL baseURL) {
            this.baseURL = baseURL;
        }

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        }

        public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        }

        public GBeanData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            ObjectName configurationObjectName = Configuration.getConfigurationObjectName(configId);
            GBeanData configData = new GBeanData(configurationObjectName, Configuration.GBEAN_INFO);
            Environment environment = new Environment();
            environment.setConfigId(configId);
            environment.getProperties().put("foo", "geronimo.test:J2EEServer=geronimo");
            configData.setAttribute("environment", environment);
            configData.setAttribute("gBeanState", NO_OBJECTS_OS);
            configData.setAttribute("configurationStore", this);
            return configData;
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public String getObjectName() {
            return null;
        }

        public List listConfigurations() {
            return null;
        }

        public File createNewConfigurationDir(Artifact configId) {
            return null;
        }

        public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
            return baseURL;
        }

        public final static GBeanInfo GBEAN_INFO;

        private static final byte[] NO_OBJECTS_OS;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, "ConfigurationStore");
            infoBuilder.addInterface(ConfigurationStore.class);
            GBEAN_INFO = infoBuilder.getBeanInfo();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.flush();
                NO_OBJECTS_OS = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
