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

package org.apache.geronimo.kernel;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);
    private Kernel kernel;
    private AbstractName gbeanName1;
    private AbstractName gbeanName2;
    private ConfigurationData configurationData;
    private ConfigurationManager configurationManager;

    public void testConfigLifecycle() throws Exception {
        Artifact configurationId = configurationData.getId();

        // load -- config should be running and gbean registered but not started
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(configurationName));
        assertNotNull(configuration.getBundle());

        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));

        // start -- gbeans should now be started
        configurationManager.startConfiguration(configurationId);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));

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
        configurationManager.stopConfiguration(configurationId);

        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));


        // unload -- configuration and gbeans should be unloaded
        configurationManager.unloadConfiguration(configurationId);

        assertFalse(kernel.isLoaded(configurationName));
        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));

    }

    public void testConfigStartStopRestart() throws Exception {
        Artifact configurationId = configurationData.getId();

        // load -- config should be running and gbean registered but not started
        configurationManager.loadConfiguration(configurationData);
        Configuration configuration = configurationManager.getConfiguration(configurationId);
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);

        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(configurationName));
        assertNotNull(configuration.getBundle());

        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));


        // start -- gbeans should now be started
        configurationManager.startConfiguration(configurationId);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName2));


        // stop -- gbeans should now be started, but still registered
        configurationManager.stopConfiguration(configurationId);

        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));


        // restart -- gbeans should now be started
        configurationManager.startConfiguration(configurationId);

        assertTrue(kernel.isLoaded(gbeanName1));
        assertTrue(kernel.isLoaded(gbeanName2));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName1));
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(gbeanName2));

        // unload -- configuration and gbeans should be unloaded
        configurationManager.stopConfiguration(configurationId);
        configurationManager.unloadConfiguration(configurationId);

        assertFalse(kernel.isLoaded(configurationName));
        assertFalse(kernel.isLoaded(gbeanName1));
        assertFalse(kernel.isLoaded(gbeanName2));

    }

//    public void testAddToConfig() throws Exception {
//        Artifact configurationId = configurationData.getId();
//
//        // load and start the config
//        configurationManager.loadConfiguration(configurationData);
//        Configuration configuration = configurationManager.getConfiguration(configurationId);
//        assertNotNull(configuration.getConfigurationClassLoader());
//
//        GBeanData mockBean3 = new GBeanData(MockGBean.getGBeanInfo());
//        try {
//            kernel.getGBeanState(mockBean3.getAbstractName());
//            fail("Gbean should not be found yet");
//        } catch (GBeanNotFoundException e) {
//        }
//        mockBean3.setAttribute("value", "1234");
//        mockBean3.setAttribute("name", "child");
//        mockBean3.setAttribute("finalInt", new Integer(1));
//        configurationManager.addGBeanToConfiguration(configurationId, "MyMockGMBean3", mockBean3, true);
//
//        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(mockBean3.getAbstractName()));
//        assertEquals(new Integer(1), kernel.getAttribute(mockBean3.getAbstractName(), "finalInt"));
//        assertEquals("1234", kernel.getAttribute(mockBean3.getAbstractName(), "value"));
//        assertEquals("child", kernel.getAttribute(mockBean3.getAbstractName(), "name"));
//    }

    protected void setUp() throws Exception {
        System.setProperty("geronimo.build.car", "true");
        super.setUp();
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

        ArtifactManager artifactManager = new DefaultArtifactManager();

        DefaultArtifactResolver artifactResolver = new DefaultArtifactResolver();
        artifactResolver.setArtifactManager(artifactManager);

        KernelConfigurationManager configurationManager = new KernelConfigurationManager();
        configurationManager.setArtifactManager(artifactManager);
        configurationManager.setArtifactResolver(artifactResolver);
        configurationManager.setKernel(kernel);
        configurationManager.activate(bundleContext);
        this.configurationManager = configurationManager;

        artifactResolver.setConfigurationManager(configurationManager);

        configurationData = new ConfigurationData(new Artifact("test", "test", "", "car"), kernel.getNaming());
        configurationData.setBundleContext(bundleContext);

        GBeanData mockBean1 = configurationData.addGBean("MyMockGMBean1", MockGBean.getGBeanInfo());
        gbeanName1 = mockBean1.getAbstractName();
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));

        GBeanData mockBean2 = configurationData.addGBean("MyMockGMBean2", MockGBean.getGBeanInfo());
        gbeanName2 = mockBean2.getAbstractName();
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePattern("MockEndpoint", gbeanName1);
        mockBean2.setReferencePattern("EndpointCollection", new AbstractNameQuery(gbeanName1, MockGBean.getGBeanInfo().getInterfaces()));
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        super.tearDown();
    }
}
