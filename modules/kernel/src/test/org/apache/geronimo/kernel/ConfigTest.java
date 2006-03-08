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

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private Kernel kernel;
    private AbstractName gbeanName1;
    private AbstractName gbeanName2;
    private ConfigurationData configurationData;
    private ConfigurationManager configurationManager;
    private final String BASE_NAME = "test:J2EEServer=geronimo";

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

        GBeanData mockBean3 = buildGBeanData(configuration.getId(), new String[] {"J2EEServer", "name"}, new String[] {"geronimo", "MyMockGMBean3"}, MockGBean.getGBeanInfo());
        try {
            kernel.getGBeanState(mockBean3.getAbstractName());
            fail("Gbean should not be found yet");
        } catch (GBeanNotFoundException e) {
        }
        mockBean3.setAttribute("value", "1234");
        mockBean3.setAttribute("name", "child");
        mockBean3.setAttribute("finalInt", new Integer(1));
        configuration.addGBean(mockBean3, true);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(mockBean3.getAbstractName()));
        assertEquals(new Integer(1), kernel.getAttribute(mockBean3.getAbstractName(), "finalInt"));
        assertEquals("1234", kernel.getAttribute(mockBean3.getAbstractName(), "value"));
        assertEquals("child", kernel.getAttribute(mockBean3.getAbstractName(), "name"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();

        Artifact baseArtifact = new Artifact("test", "base", "1", "car");
        GBeanData artifactManagerData = buildGBeanData(baseArtifact, new String[] {"module", "j2eeType"}, new String[] {"base", "ArtifactManager"}, DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManagerData, getClass().getClassLoader());
        kernel.startGBean(artifactManagerData.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactManagerData.getAbstractName()));

        GBeanData artifactResolverData = buildGBeanData(baseArtifact, new String[] {"module", "j2eeType"}, new String[] {"base", "ArtifactResolver"}, DefaultArtifactResolver.GBEAN_INFO);
        artifactResolverData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        kernel.loadGBean(artifactResolverData, getClass().getClassLoader());
        kernel.startGBean(artifactResolverData.getAbstractName());
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(artifactResolverData.getAbstractName()));

        GBeanData configurationManagerData = buildGBeanData(baseArtifact, new String[] {"module", "name"}, new String[] {"base", "BasicConfigurationManager"}, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManagerData.getAbstractName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolverData.getAbstractName());

        kernel.loadGBean(configurationManagerData, getClass().getClassLoader());
        kernel.startGBean(configurationManagerData.getAbstractName());
        configurationManager = ConfigurationUtil.getConfigurationManager(kernel);

        Environment environment = new Environment();
        environment.setConfigId(new Artifact("geronimo", "test", "1", "car"));
        Map properties = new HashMap();
        properties.put(Configuration.JSR77_BASE_NAME_PROPERTY, BASE_NAME);
        environment.setProperties(properties);

        ArrayList gbeans = new ArrayList();

        GBeanData mockBean1 = buildGBeanData(environment.getConfigId(), new String[] {"J2EEServer", "name"}, new String[] {"geronimo", "MyMockGMBean1"}, MockGBean.getGBeanInfo());
        gbeanName1 = mockBean1.getAbstractName();
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));
        gbeans.add(mockBean1);

        GBeanData mockBean2 = buildGBeanData(environment.getConfigId(), new String[] {"J2EEServer", "name"}, new String[] {"geronimo", "MyMockGMBean2"}, MockGBean.getGBeanInfo());
        gbeanName2 = mockBean2.getAbstractName();
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePattern("MockEndpoint", gbeanName1);
        mockBean2.setReferencePattern("EndpointCollection", new AbstractNameQuery(gbeanName1));
        gbeans.add(mockBean2);


        configurationData = new ConfigurationData(ConfigurationModuleType.CAR, null, gbeans, null, environment, null);
    }

    private GBeanData buildGBeanData(Artifact artifact, String[] key, String[] value, GBeanInfo info) throws MalformedObjectNameException {
         AbstractName abstractName = buildAbstractName(artifact, key, value, info);
         return new GBeanData(abstractName, info);
     }

     private AbstractName buildAbstractName(Artifact artifact, String[] key, String[] value, GBeanInfo info) throws MalformedObjectNameException {
         Hashtable names = new Hashtable();
         for (int i = 0; i < key.length; i++) {
             String k = key[i];
             String v = value[i];
             names.put(k, v);
         }
         return new AbstractName(artifact, names, info.getInterfaces(), new ObjectName("test", names));
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
            AbstractName configurationName = Configuration.getConfigurationAbstractName(configId);
            GBeanData configData = new GBeanData(configurationName, Configuration.GBEAN_INFO);
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
