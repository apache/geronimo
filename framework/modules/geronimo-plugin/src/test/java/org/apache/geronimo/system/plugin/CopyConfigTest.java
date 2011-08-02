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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.apache.geronimo.system.configuration.AttributesXmlUtil;
import org.apache.geronimo.system.configuration.GBeanOverride;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.plugin.model.AttributeType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.testsupport.DOMUtils;
import org.w3c.dom.Document;

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
            "            <version>2.1-TEST</version>\n" +
            "            <type>car</type>\n" +
            "        </module-id>\n" +
            "        <geronimo-version>2.1-TEST</geronimo-version>\n" +
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
    private static final String ATTRIBUTE_VALUE =
            "<environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "        <environment:dependencies>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.cts</environment:groupId>\n" +
            "                <environment:artifactId>server-security</environment:artifactId>\n" +
            "                <environment:version>${version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.cts</environment:groupId>\n" +
            "                <environment:artifactId>database</environment:artifactId>\n" +
            "                <environment:version>${version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.cts</environment:groupId>\n" +
            "                <environment:artifactId>jms</environment:artifactId>\n" +
            "                <environment:version>${version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
            "                <environment:artifactId>j2ee-server</environment:artifactId>\n" +
            "                <environment:version>${geronimo.version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.cts</environment:groupId>\n" +
            "                <environment:artifactId>server-ior</environment:artifactId>\n" +
            "                <environment:version>${version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
            "                <environment:artifactId>uddi-jetty6</environment:artifactId>\n" +
            "                <environment:version>${geronimo.version}</environment:version>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                        </environment:dependency>\n" +
            "                    </environment:dependencies>\n" +
            "                </environment:environment>";

    public void testCopyConfig() throws Exception {
        InputStream in = new ByteArrayInputStream(CONFIG.getBytes());
        PluginType pluginType = PluginXmlUtil.loadPluginMetadata(in);
        List<GbeanType> gbeans = pluginType.getPluginArtifact().get(0).getConfigXmlContent().get(0).getGbean();
        assertEquals(2, gbeans.size());
        GBeanOverride override = new GBeanOverride(gbeans.get(0), new JexlExpressionParser());
        String attributeValue = override.getAttribute("defaultEnvironment");
        
        Document expectedDoc = DOMUtils.load(ATTRIBUTE_VALUE);
        Document actualDoc = DOMUtils.load(attributeValue);
        
        DOMUtils.compareNodes(expectedDoc, actualDoc);
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
        PluginArtifactType instance = PluginXmlUtil.loadPluginArtifactMetadata(in);
        assertEquals("DirectoryService", instance.getConfigXmlContent().get(0).getGbean().get(0).getName());
        assertEquals("default", instance.getConfigXmlContent().get(0).getServer());
    }

    private static final String INSTANCE2 = "                        <plugin-artifact>\n" +
            "                            <config-xml-content>\n" +
            "                                <gbean name=\"ResourceRefBuilder\">\n" +
            "                                    <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
            "                                    <attribute name=\"defaultEnvironment\">\n" +
            "                                        <environment xmlns=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "                                            <dependencies>\n" +
            "                                                <dependency>\n" +
            "                                                    <groupId>org.apache.geronimo.configs</groupId>\n" +
            "                                                    <artifactId>j2ee-corba-yoko</artifactId>\n" +
            "                                                    <type>car</type>\n" +
            "                                                </dependency>\n" +
            "                                            </dependencies>\n" +
            "                                        </environment>\n" +
            "                                    </attribute>\n" +
            "                                </gbean>\n" +
            "\n" +
            "                                <gbean name=\"AdminObjectRefBuilder\">\n" +
            "                                    <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
            "                                </gbean>\n" +
            "\n" +
            "                                <gbean name=\"ClientResourceRefBuilder\">\n" +
            "                                    <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
            "                                    <attribute name=\"defaultEnvironment\">\n" +
            "                                        <environment xmlns=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "                                            <dependencies>\n" +
            "                                                <dependency>\n" +
            "                                                    <groupId>org.apache.geronimo.configs</groupId>\n" +
            "                                                    <artifactId>client-corba-yoko</artifactId>\n" +
            "                                                    <type>car</type>\n" +
            "                                                </dependency>\n" +
            "                                            </dependencies>\n" +
            "                                        </environment>\n" +
            "                                    </attribute>\n" +
            "                                </gbean>\n" +
            "                            </config-xml-content>\n" +
            "                        </plugin-artifact>";

    private static final String ATTR = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<ns2:attribute name=\"defaultEnvironment\" xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\">\n" +
            "    <environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "        <environment:dependencies>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
            "                <environment:artifactId>j2ee-corba-yoko</environment:artifactId>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                                                </environment:dependency>\n" +
            "                                            </environment:dependencies>\n" +
            "                                        </environment:environment>\n" +
            "                                    </ns2:attribute>";

    private static final String VALUE =
            "<environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
            "        <environment:dependencies>\n" +
            "            <environment:dependency>\n" +
            "                <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
            "                <environment:artifactId>j2ee-corba-yoko</environment:artifactId>\n" +
            "                <environment:type>car</environment:type>\n" +
            "                                                </environment:dependency>\n" +
            "                                            </environment:dependencies>\n" +
            "                                        </environment:environment>";
    
    public void testXmlAttribute() throws Exception {
        Reader in = new StringReader(INSTANCE2);
        PluginArtifactType instance = PluginXmlUtil.loadPluginArtifactMetadata(in);
        List<GbeanType> gbeans = instance.getConfigXmlContent().get(0).getGbean();
        assertEquals(3, gbeans.size());
        List contents = gbeans.get(0).getAttributeOrReference();
        assertEquals(2, contents.size());
        AttributeType attr = (AttributeType) contents.get(1);
        String value = AttributesXmlUtil.extractAttributeValue(attr);
        
        Document expectedDoc = DOMUtils.load(VALUE);
        Document actualDoc = DOMUtils.load(value);
        
        DOMUtils.compareNodes(expectedDoc, actualDoc);
    }
}
