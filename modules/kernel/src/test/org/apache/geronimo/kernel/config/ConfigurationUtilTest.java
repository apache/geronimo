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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.MockGBean;
import org.apache.geronimo.kernel.config.xstream.XStreamConfigurationMarshaler;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationUtilTest extends TestCase {
    private XStreamConfigurationMarshaler xstreamConfigurationMarshaler = new XStreamConfigurationMarshaler();
    private SerializedConfigurationMarshaler serializedConfigurationMarshaler = new SerializedConfigurationMarshaler();
    private ConfigurationData configurationData1;
    private ConfigurationData configurationData2;
//    private ConfigurationData configurationData3;

    public void test() throws Exception {
        copyTest(configurationData1);
        copyTest(configurationData2);
    }

    private void copyTest(ConfigurationData configurationData) throws Exception {
        List gbeans = configurationData.getGBeans(getClass().getClassLoader());
        ConfigurationData data = copy(configurationData, serializedConfigurationMarshaler, serializedConfigurationMarshaler);
        gbeans = data.getGBeans(getClass().getClassLoader());
        assertEquals(configurationData.getId(), data.getId());

        gbeans = configurationData.getGBeans(getClass().getClassLoader());
        data = copy(configurationData, xstreamConfigurationMarshaler, xstreamConfigurationMarshaler);
        gbeans = data.getGBeans(getClass().getClassLoader());
        assertEquals(configurationData.getId(), data.getId());

//        gbeans = configurationData.getGBeans(getClass().getClassLoader());
//        data = copy(configurationData, serializedConfigurationMarshaler, xstreamConfigurationMarshaler);
//        gbeans = data.getGBeans(getClass().getClassLoader());
//        assertEquals(configurationData.getId(), data.getId());
    }

    private static ConfigurationData copy(ConfigurationData configurationData, ConfigurationMarshaler writer, ConfigurationMarshaler reader) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeConfigurationData(configurationData, out);
//        System.out.println(new String(out.toByteArray()));
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ConfigurationData data = reader.readConfigurationData(in);
        return data;
    }

    protected void setUp() throws Exception {
        super.setUp();

        Jsr77Naming naming = new Jsr77Naming();

        Artifact artifact1 = new Artifact("test", "1", "1.1", "bar");
        Artifact artifact2 = new Artifact("test", "2", "2.2", "bar");

        Environment e1 = new Environment();
        e1.setConfigId(artifact1);
        configurationData1 = new ConfigurationData(e1, naming);
        configurationData1 = new ConfigurationData(new Artifact("test", "test", "", "car"), naming);

        GBeanData mockBean1 = configurationData1.addGBean("MyMockGMBean1", MockGBean.getGBeanInfo());
        AbstractName gbeanName1 = mockBean1.getAbstractName();
        mockBean1.setAttribute("value", "1234");
        mockBean1.setAttribute("name", "child");
        mockBean1.setAttribute("finalInt", new Integer(1));

        GBeanData mockBean2 = configurationData1.addGBean("MyMockGMBean2", MockGBean.getGBeanInfo());
//        AbstractName gbeanName2 = mockBean2.getAbstractName();
        mockBean2.setAttribute("value", "5678");
        mockBean2.setAttribute("name", "Parent");
        mockBean2.setAttribute("finalInt", new Integer(3));
        mockBean2.setAttribute("someObject", new GBeanData(gbeanName1, MockGBean.getGBeanInfo()));
        mockBean2.setReferencePattern("MockEndpoint", gbeanName1);
        mockBean2.setReferencePattern("EndpointCollection", new AbstractNameQuery(gbeanName1, MockGBean.getGBeanInfo().getInterfaces()));


        Environment e2 = new Environment();
        e2.setConfigId(artifact2);
        e2.addDependency(new Artifact("test", "1", (Version) null, "bar"), ImportType.ALL);
        configurationData2 = new ConfigurationData(e2, naming);

//        Environment e3 = new Environment();
//        e3.setConfigId(artifact3);
//        e3.addDependency(new Artifact("test", "2", (Version) null, "bar"), ImportType.ALL);
//        configurationData3 = new ConfigurationData(e3, kernel.getNaming());
    }
}
