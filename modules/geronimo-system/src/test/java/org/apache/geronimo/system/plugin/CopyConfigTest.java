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


package org.apache.geronimo.system.plugin;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.configuration.GBeanOverride;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;

/**
 * @version $Rev$ $Date$
 */
public class CopyConfigTest extends TestCase {

    private final static String CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<geronimo-plugin xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\">\n" +
            "    <name>Geronimo Configs :: J2EE Deployer</name>\n" +
            "    <description>Apache Geronimo, the J2EE server project of the Apache Software Foundation.</description>\n" +
            "    <url>http://geronimo.apache.org/</url>\n" +
            "    <author>The Apache Geronimo development community</author>\n" +
            "    <license osi-approved=\"true\">The Apache Software License, Version 2.0</license>\n" +
            "    <plugin-artifact>\n" +
            "        <module-id>\n" +
            "            <groupId>org.apache.geronimo.configs</groupId>\n" +
            "            <artifactId>j2ee-deployer</artifactId>\n" +
            "            <version>2.1-SNAPSHOT</version>\n" +
            "            <type>car</type>\n" +
            "        </module-id>\n" +
            "        <geronimo-version>2.1-SNAPSHOT</geronimo-version>\n" +
            "        <jvm-version>1.5</jvm-version>\n" +
            "        <source-repository>http://www.geronimoplugins.com/repository/geronimo-1.1</source-repository>\n" +
            "        <source-repository>http://repo1.maven.org/maven2/</source-repository>\n" +
            "        <config-xml-content>\n" +
            "        <gbean name=\"EARBuilder\"  xmlns=\"http://geronimo.apache.org/xml/ns/attributes-1.2\">\n" +
            "            <attribute name=\"defaultEnvironment\">\n" +
            "                <environment xmlns=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "                    <dependencies>\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.cts</groupId>\n" +
            "                            <artifactId>server-security</artifactId>\n" +
            "                            <version>${version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.cts</groupId>\n" +
            "                            <artifactId>database</artifactId>\n" +
            "                            <version>${version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.cts</groupId>\n" +
            "                            <artifactId>jms</artifactId>\n" +
            "                            <version>${version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.configs</groupId>\n" +
            "                            <artifactId>j2ee-server</artifactId>\n" +
            "                            <version>${geronimo.version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.cts</groupId>\n" +
            "                            <artifactId>server-ior</artifactId>\n" +
            "                            <version>${version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "\n" +
            "                        <dependency>\n" +
            "                            <groupId>org.apache.geronimo.configs</groupId>\n" +
            "                            <artifactId>uddi-jetty6</artifactId>\n" +
            "                            <version>${geronimo.version}</version>\n" +
            "                            <type>car</type>\n" +
            "                        </dependency>\n" +
            "                    </dependencies>\n" +
            "                </environment>\n" +
            "            </attribute>\n" +
            "        </gbean>\n" +
            "\n" +
            "        <gbean name=\"WebBuilder\"  xmlns=\"http://geronimo.apache.org/xml/ns/attributes-1.2\">\n" +
            "            <attribute name=\"defaultNamespace\">http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.2</attribute>\n" +
            "        </gbean>\n" +
            "        \n" +
            "        </config-xml-content>\n" +
            "    </plugin-artifact>\n" +
            "</geronimo-plugin>";
    private static final String ATTRIBUTE_VALUE = "<dependencies><dependency><groupId>org.apache.geronimo.cts</groupId><artifactId>server-security</artifactId><version>${version}</version><type>car</type>                         </dependency><dependency><groupId>org.apache.geronimo.cts</groupId><artifactId>database</artifactId><version>${version}</version><type>car</type>                         </dependency><dependency><groupId>org.apache.geronimo.cts</groupId><artifactId>jms</artifactId><version>${version}</version><type>car</type>                         </dependency><dependency><groupId>org.apache.geronimo.configs</groupId><artifactId>j2ee-server</artifactId><version>${geronimo.version}</version><type>car</type>                         </dependency><dependency><groupId>org.apache.geronimo.cts</groupId><artifactId>server-ior</artifactId><version>${version}</version><type>car</type>                         </dependency><dependency><groupId>org.apache.geronimo.configs</groupId><artifactId>uddi-jetty6</artifactId><version>${geronimo.version}</version><type>car</type>                         </dependency>                     </dependencies>\n" +
            "            ";

    public void testCopyConfig() throws Exception {
        InputStream in = new ByteArrayInputStream(CONFIG.getBytes());
        PluginType pluginType = PluginInstallerGBean.loadPluginMetadata(in);
        List<GbeanType> gbeans = pluginType.getPluginArtifact().get(0).getConfigXmlContent().getGbean();
        assertEquals(2, gbeans.size());
        GBeanOverride override = new GBeanOverride(gbeans.get(0), new JexlExpressionParser());
        String attributeValue = override.getAttribute("defaultEnvironment");
        assertEquals(ATTRIBUTE_VALUE, attributeValue);

    }

    private static final String INSTANCE = "                        <plugin-artifact>\n" +
            "                            <copy-file relative-to=\"server\" dest-dir=\"var/directory\">META-INF/server.xml</copy-file>\n" +
            "                            <config-xml-content>\n" +
            "                                <gbean name=\"DirectoryService\">\n" +
            "                                    <attribute name=\"configFile\">var/directory/server.xml</attribute>\n" +
            "                                    <attribute name=\"workingDir\">var/directory</attribute>\n" +
            "                                    <attribute name=\"providerURL\">#{providerURL}</attribute>\n" +
            "                                </gbean>\n" +
            "                            </config-xml-content>\n" +
            "                            <config-substitution key=\"providerURL\">ou=system</config-substitution>\n" +
            "                        </plugin-artifact>";
    
    public void testReadNoNS() throws Exception {
        Reader in = new StringReader(INSTANCE);
        PluginArtifactType instance = PluginInstallerGBean.loadPluginArtifactMetadata(in);
        assertEquals("DirectoryService", instance.getConfigXmlContent().getGbean().get(0).getName());
    }
}
