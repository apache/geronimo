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
import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Rev$ $Date$
 */
public class ConfigTest extends TestCase {
    private ObjectName gbeanName1;
    private Kernel kernel;
    private MBeanServer mbServer;
    private byte[] state;
    private ObjectName gbeanName2;

    public void testOfflineConfig() throws Exception {
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("test"));
        config.setReferencePatterns("Parent", null);
    }

    public void testOnlineConfig() throws Exception {
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("test"));
        config.setReferencePatterns("Parent", null);
        config.setAttribute("classPath", Collections.EMPTY_LIST);
        config.setAttribute("gBeanState", state);
        config.setAttribute("dependencies", Collections.EMPTY_LIST);
        ConfigurationManager configurationManager = kernel.getConfigurationManager();
        ObjectName configName = configurationManager.load(config, null);
        mbServer.invoke(configName, "startRecursive", null, null);

        assertEquals(new Integer(State.RUNNING_INDEX), mbServer.getAttribute(configName, "state"));
        assertNotNull(mbServer.getAttribute(configName, "configurationClassLoader"));

        assertEquals(new Integer(State.RUNNING_INDEX), mbServer.getAttribute(gbeanName1, "state"));
        Object state = mbServer.getAttribute(gbeanName2, "state");
        assertEquals(new Integer(State.RUNNING_INDEX), state);
        assertEquals(new Integer(1), mbServer.getAttribute(gbeanName1, "finalInt"));
        assertEquals("1234", mbServer.getAttribute(gbeanName1, "value"));
        assertEquals(new Integer(3), mbServer.getAttribute(gbeanName2, "finalInt"));

        mbServer.setAttribute(gbeanName2, new Attribute("mutableInt", new Integer(44)));
        assertEquals(new Integer(44), mbServer.getAttribute(gbeanName2, "mutableInt"));

        mbServer.invoke(gbeanName2, "doSetMutableInt", new Object[]{new Integer(55)}, new String[]{"int"});
        assertEquals(new Integer(55), mbServer.getAttribute(gbeanName2, "mutableInt"));

        assertEquals("no endpoint", mbServer.invoke(gbeanName1, "checkEndpoint", null, null));
        assertEquals("endpointCheck", mbServer.invoke(gbeanName2, "checkEndpoint", null, null));

        assertEquals(new Integer(0), mbServer.invoke(gbeanName1, "checkEndpointCollection", null, null));
        assertEquals(new Integer(1), mbServer.invoke(gbeanName2, "checkEndpointCollection", null, null));

        mbServer.setAttribute(gbeanName2, new Attribute("endpointMutableInt", new Integer(99)));
        assertEquals(new Integer(99), mbServer.getAttribute(gbeanName2, "endpointMutableInt"));
        assertEquals(new Integer(99), mbServer.getAttribute(gbeanName1, "mutableInt"));

        mbServer.invoke(configName, "stop", null, null);
        try {
            mbServer.getAttribute(gbeanName1, "value");
            fail();
        } catch (InstanceNotFoundException e) {
            // ok
        }
        assertEquals(new Integer(State.STOPPED.toInt()), mbServer.getAttribute(configName, "state"));
        configurationManager.unload(configName);
        assertFalse(mbServer.isRegistered(configName));
    }

    protected void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "geronimo");
        kernel.boot();

        mbServer = kernel.getMBeanServer();

        gbeanName1 = new ObjectName("geronimo.test:name=MyMockGMBean1");
        GBeanMBean mockBean1 = new GBeanMBean(MockGBean.getGBeanInfo());
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));
        gbeanName2 = new ObjectName("geronimo.test:name=MyMockGMBean2");
        GBeanMBean mockBean2 = new GBeanMBean(MockGBean.getGBeanInfo());
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setReferencePatterns("MockEndpoint", Collections.singleton(gbeanName1));
        mockBean2.setReferencePatterns("EndpointCollection", Collections.singleton(gbeanName1));

        Map gbeans = new HashMap();
        gbeans.put(gbeanName1, mockBean1);
        gbeans.put(gbeanName2, mockBean2);
        state = Configuration.storeGBeans(gbeans);
    }

    protected void tearDown() throws Exception {
        mbServer = null;
        kernel.shutdown();
    }
}
