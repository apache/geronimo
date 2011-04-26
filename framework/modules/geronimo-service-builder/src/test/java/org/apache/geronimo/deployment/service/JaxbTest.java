/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.deployment.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.geronimo.deployment.service.plan.JaxbUtil;
import org.apache.geronimo.deployment.service.plan.ModuleType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Rev:$ $Date:$
 */
public class JaxbTest {

//    private static String PLAN = "<module xmlns=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
//            "\n" +
//            "    <environment>\n" +
//            "        <moduleId>\n" +
//            "            <groupId>geronimo</groupId>\n" +
//            "            <artifactId>foo4</artifactId>\n" +
//            "            <version>DEV</version>\n" +
//            "            <type>car</type>\n" +
//            "        </moduleId>\n" +
//            "    </environment>\n" +
//            "    <gbean name=\"MyMockGMBean\" class=\"org.apache.geronimo.deployment.MockGBean\">\n" +
//            "        <attribute name=\"value\">1234</attribute>\n" +
//            "        <attribute name=\"intValue\">1234</attribute>\n" +
//            "        <reference name=\"MockEndpoint\">\n" +
//            "            <name>MyMockGMBean</name>\n" +
//            "        </reference>\n" +
//            "    </gbean>\n" +
//            "</module>";

    private static String PLAN = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<module xmlns:ns2=\"http://geronimo.apache.org/xml/ns/deployment/javabean-1.0\" xmlns=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "    <environment>\n" +
            "        <moduleId>\n" +
            "            <groupId>org.apache.geronimo.framework.config</groupId>\n" +
            "            <artifactId>j2ee-system</artifactId>\n" +
            "            <version>3.0-SNAPSHOT</version>\n" +
            "            <type>car</type>\n" +
            "        </moduleId>\n" +
            "    </environment>\n" +
            "    <gbean name=\"ServerInfo\" class=\"org.apache.geronimo.system.serverinfo.BasicServerInfo\">\n" +
            "        <attribute name=\"useSystemProperties\">true</attribute>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"Repository\" class=\"org.apache.geronimo.system.repository.Maven2Repository\">\n" +
            "        <attribute name=\"root\">repository/</attribute>\n" +
            "        <reference name=\"ServerInfo\">\n" +
            "            <name>ServerInfo</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"ServerStatus\" class=\"org.apache.geronimo.system.main.ServerStatusGBean\">\n" +
            "        <attribute name=\"serverStarted\">false</attribute>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"Local\" class=\"org.apache.geronimo.system.configuration.RepositoryConfigurationStore\">\n" +
            "        <reference name=\"Repository\">\n" +
            "            <name>Repository</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"AttributeManager\" class=\"org.apache.geronimo.system.configuration.LocalAttributeManager\">\n" +
            "        <attribute name=\"configFile\">var/config/config.xml</attribute>\n" +
            "        <attribute name=\"substitutionsFile\">var/config/config-substitutions.properties</attribute>\n" +
            "        <attribute name=\"substitutionPrefix\">org.apache.geronimo.config.substitution.</attribute>\n" +
            "        <reference name=\"ServerInfo\">\n" +
            "            <name>ServerInfo</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"ArtifactManager\" class=\"org.apache.geronimo.kernel.repository.DefaultArtifactManager\"/>\n" +
            "    <gbean name=\"ArtifactResolver\" class=\"org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver\">\n" +
            "        <attribute name=\"versionMapLocation\">var/config/artifact_aliases.properties</attribute>\n" +
            "        <reference name=\"ArtifactManager\">\n" +
            "            <name>ArtifactManager</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"Repositories\"/>\n" +
            "        <reference name=\"ServerInfo\">\n" +
            "            <name>ServerInfo</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"ConfigurationManagers\">\n" +
            "            <name>ConfigurationManager</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"ConfigurationManager\" class=\"org.apache.geronimo.kernel.config.EditableKernelConfigurationManager\">\n" +
            "        <attribute name=\"defaultStoreNameQuery\">?name=Local</attribute>\n" +
            "        <reference name=\"Repositories\"/>\n" +
            "        <reference name=\"Stores\"/>\n" +
            "        <reference name=\"Watchers\"/>\n" +
            "        <reference name=\"AttributeStore\">\n" +
            "            <name>AttributeManager</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"PersistentConfigurationList\">\n" +
            "            <type>AttributeStore</type>\n" +
            "            <name>AttributeManager</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"ArtifactManager\">\n" +
            "            <name>ArtifactManager</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"ArtifactResolver\">\n" +
            "            <name>ArtifactResolver</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"Logger\" class=\"org.apache.geronimo.system.logging.OSGILogServiceWrapper\"/>\n" +
            "    <gbean name=\"GeronimoOBR\" class=\"org.apache.geronimo.obr.GeronimoOBRGBean\">\n" +
            "        <attribute name=\"exclusions\">\n" +
            "             org.apache.felix/org.apache.felix.framework//,\n" +
            "             org.eclipse/osgi//\n" +
            "        </attribute>\n" +
            "        <reference name=\"Repository\">\n" +
            "            <name>Repository</name>\n" +
            "        </reference>\n" +
            "        <reference name=\"ServerInfo\">\n" +
            "            <name>ServerInfo</name>\n" +
            "        </reference>\n" +
            "    </gbean>\n" +
            "    <gbean name=\"EmbeddedDaemon\" class=\"org.apache.geronimo.system.main.EmbeddedDaemon\"/>\n" +
            "</module>";

    @Test
    public void testMarshalling() throws Exception {
//         InputStream in = getClass().getClassLoader().getResourceAsStream("service/plan1.xml");
         InputStream in = new ByteArrayInputStream(PLAN.getBytes());
        ModuleType moduleType;
        moduleType = checkUnmarshall(in);
        StringWriter out = new StringWriter();
        JaxbUtil.marshalModule(moduleType, out);
        in = new ByteArrayInputStream(out.toString().getBytes());
        checkUnmarshall(in);
    }

    private ModuleType checkUnmarshall(InputStream in) throws XMLStreamException, JAXBException, IOException {
        ModuleType moduleType;
        try {
            moduleType = JaxbUtil.unmarshalModule(in, false);
            assertTrue(moduleType.getEnvironment() != null);
            assertTrue(moduleType.getEnvironment().getModuleId() != null);
//            assertEquals("foo4", moduleType.getEnvironment().getModuleId().getArtifactId());
            assertEquals("j2ee-system", moduleType.getEnvironment().getModuleId().getArtifactId());

            assertEquals(11, moduleType.getGbean().size());

        } finally {
            in.close();
        }
        return moduleType;
    }
}
