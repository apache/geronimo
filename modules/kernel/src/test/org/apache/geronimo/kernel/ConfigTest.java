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

import java.net.URI;
import java.util.Collections;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private ObjectName gbeanName1;
    private Kernel kernel;
    private byte[] state;
    private ObjectName gbeanName2;

    public void testOnlineConfig() throws Exception {
        URI id = new URI("test");
        GBeanData config = new GBeanData(Configuration.getConfigurationObjectName(id), Configuration.GBEAN_INFO);
        config.setAttribute("ID", id);
        config.setReferencePatterns("Parent", null);
        config.setAttribute("classPath", Collections.EMPTY_LIST);
        config.setAttribute("gBeanState", state);
        config.setAttribute("dependencies", Collections.EMPTY_LIST);
        ConfigurationManager configurationManager = kernel.getConfigurationManager();
        ObjectName configName = configurationManager.load(config, null, getClass().getClassLoader());
        kernel.invoke(configName, "startRecursive", null, null);

        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getAttribute(configName, "state"));
        assertNotNull(kernel.getAttribute(configName, "configurationClassLoader"));

        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getAttribute(gbeanName1, "state"));
        Object state = kernel.getAttribute(gbeanName2, "state");
        assertEquals(new Integer(State.RUNNING_INDEX), state);
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

        kernel.invoke(configName, "stop", null, null);
        try {
            kernel.getAttribute(gbeanName1, "value");
            fail();
        } catch (GBeanNotFoundException e) {
            // ok
        }
        assertEquals(new Integer(State.STOPPED.toInt()), kernel.getAttribute(configName, "state"));
        configurationManager.unload(configName);
        assertFalse(kernel.isLoaded(configName));
    }

    protected void setUp() throws Exception {
        kernel = new Kernel("test");
        kernel.boot();

        gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
        GBeanData mockBean1 = new GBeanData(gbeanName1, MockGBean.getGBeanInfo());
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));

        gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
        GBeanData mockBean2 = new GBeanData(gbeanName2, MockGBean.getGBeanInfo());
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePatterns("MockEndpoint", Collections.singleton(gbeanName1));
        mockBean2.setReferencePatterns("EndpointCollection", Collections.singleton(gbeanName1));

        state = Configuration.storeGBeans(new GBeanData[] {mockBean1, mockBean2});
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
