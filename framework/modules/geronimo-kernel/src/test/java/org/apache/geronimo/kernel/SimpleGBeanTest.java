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

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.KernelConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class SimpleGBeanTest extends TestCase {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);

    public void test() throws Exception {
        // boot the kernel
        Kernel kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);

        // load the configuration manager bootstrap service
//        ConfigurationData bootstrap = new ConfigurationData(new Artifact("bootstrap", "bootstrap", "", "car"), kernel.getNaming());
//        bootstrap.addGBean("ConfigurationManager", KernelConfigurationManager.class);
//        ConfigurationUtil.loadBootstrapConfiguration(kernel, bootstrap, bundleContext);
//        ConfigurationManager configurationManager = kernel.getGBean(ConfigurationManager.class);
        KernelConfigurationManager configurationManager = new KernelConfigurationManager();
        configurationManager.setKernel(kernel);
        configurationManager.activate(bundleContext);

        // create a configuration for our test bean
        Artifact configurationId = new Artifact("test", "test", "", "car");
        ConfigurationData configurationData = new ConfigurationData(configurationId, kernel.getNaming());
        configurationData.setBundleContext(bundleContext);
        GBeanData mockBean1 = configurationData.addGBean("MyBean", TestGBean.getGBeanInfo());
        mockBean1.setAttribute("value", "1234");

        // load and start the configuration
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(configurationId);

        // invoke GBean directly
        TestGBean testGBean = (TestGBean) kernel.getGBean("MyBean");
        assertEquals("1234", testGBean.getValue());
        assertEquals("1234", testGBean.fetchValue());

        // invoke GBean by short name
        assertEquals("1234", kernel.getAttribute("MyBean", "value"));
        assertEquals("1234", kernel.invoke("MyBean", "fetchValue"));
        // this does not work without addOperation
        assertEquals("1234", kernel.invoke("MyBean", "getValue"));
        
        // invoke GBean by type
        assertEquals("1234", kernel.getAttribute(TestGBean.class, "value"));
        assertEquals("1234", kernel.invoke(TestGBean.class, "fetchValue"));

        // invoke GBean by name and type
        assertEquals("1234", kernel.getAttribute("MyBean", TestGBean.class, "value"));
        assertEquals("1234", kernel.invoke("MyBean", TestGBean.class, "fetchValue"));

        // stop and unload configuration
        configurationManager.stopConfiguration(configurationId);
        configurationManager.unloadConfiguration(configurationId);

        // stop the kernel
        kernel.shutdown();
    }

    public static class TestGBean {
        private final String value;

        public TestGBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String fetchValue() {
            return value;
        }


        private static final GBeanInfo GBEAN_INFO;
        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TestGBean.class);
            infoBuilder.setConstructor(new String[] {"value"});
            // FIXME : The getters/setters should have been added as an operation
            infoBuilder.addOperation("getValue");
            GBEAN_INFO = infoBuilder.getBeanInfo();
        }
        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }
    }
}
