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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.MockGBean;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationUtilTest extends TestCase {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);
//    private XStreamConfigurationMarshaler xstreamConfigurationMarshaler = new XStreamConfigurationMarshaler();
    private SerializedConfigurationMarshaler serializedConfigurationMarshaler = new SerializedConfigurationMarshaler();
    private static Artifact artifact3 = new Artifact("test", "3", "3.3", "bar");
    private static final Jsr77Naming naming = new Jsr77Naming();

    public void test() throws Exception {
        ConfigurationData configurationData = createConfigurationData(serializedConfigurationMarshaler);
        ConfigurationData data = copy(configurationData, serializedConfigurationMarshaler, serializedConfigurationMarshaler);
        assertEquals(data, configurationData);

//        configurationData = createConfigurationData(xstreamConfigurationMarshaler);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        xstreamConfigurationMarshaler.writeConfigurationData(configurationData, out);
//        // Test whether the attribute "value" is encrypted
//        assertFalse(out.toString().contains("1234"));
//        assertFalse(out.toString().contains("5678"));
//        // Test whether other attributes are not encrypted
//        assertTrue(out.toString().contains("Parent"));
//        assertTrue(out.toString().contains("child"));
//        data = copy(configurationData, xstreamConfigurationMarshaler, xstreamConfigurationMarshaler);
//        assertEquals(data, configurationData);
//
//        configurationData = createConfigurationData(serializedConfigurationMarshaler);
//        data = copy(configurationData, serializedConfigurationMarshaler, xstreamConfigurationMarshaler);
//        assertEquals(data, configurationData);
//
//        configurationData = createConfigurationData(xstreamConfigurationMarshaler);
//        data = copy(configurationData, xstreamConfigurationMarshaler, serializedConfigurationMarshaler);
//        assertEquals(data, configurationData);
    }

    private void assertEquals(ConfigurationData data, ConfigurationData configurationData) throws InvalidConfigException {
        List gbeans;
        gbeans = data.getGBeans(bundleContext.getBundle());
        assertEquals(configurationData.getId(), data.getId());
        ConfigurationData data3 = (ConfigurationData) data.getChildConfigurations().get("testmodule");
        gbeans = data3.getGBeans(bundleContext.getBundle());
        assertEquals(new QName("namespaceURI", "localPart"), ((GBeanData)gbeans.get(0)).getAttribute("someObject"));
    }

    private static ConfigurationData copy(ConfigurationData configurationData, ConfigurationMarshaler writer, ConfigurationMarshaler reader) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeConfigurationData(configurationData, out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ConfigurationData data = reader.readConfigurationData(in);
        return data;
    }

    private static ConfigurationData createConfigurationData(ConfigurationMarshaler marshaler) throws Exception {
        Artifact artifact1 = new Artifact("test", "1", "1.1", "bar");
        ConfigurationData configurationData = new ConfigurationData(artifact1, naming, marshaler.newGBeanState(Collections.EMPTY_SET));

        GBeanData mockBean1 = configurationData.addGBean("MyMockGMBean1", MockGBean.getGBeanInfo());
        AbstractName gbeanName1 = mockBean1.getAbstractName();
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));

        GBeanData mockBean2 = configurationData.addGBean("MyMockGMBean2", MockGBean.getGBeanInfo());
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setAttribute("someObject", new GBeanData(gbeanName1, MockGBean.getGBeanInfo()));
        mockBean2.setReferencePattern("MockEndpoint", gbeanName1);
        mockBean2.setReferencePattern("EndpointCollection", new AbstractNameQuery(gbeanName1, MockGBean.getGBeanInfo().getInterfaces()));


        ConfigurationData childConfigurationData = new ConfigurationData(artifact3, naming, marshaler.newGBeanState(Collections.EMPTY_SET));
        configurationData.addChildConfiguration("testmodule", childConfigurationData);
        GBeanData childConfigurationGBean = childConfigurationData.addGBean("ChildConfigurationGBean", MockGBean.getGBeanInfo());
        childConfigurationGBean.setAttribute("name", "foo");
        childConfigurationGBean.setAttribute("someObject", new QName("namespaceURI", "localPart"));

        return configurationData;
    }
}
