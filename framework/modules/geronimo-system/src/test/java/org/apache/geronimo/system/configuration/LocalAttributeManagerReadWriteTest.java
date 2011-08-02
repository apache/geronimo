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


package org.apache.geronimo.system.configuration;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.testsupport.DOMUtils;
import org.w3c.dom.Document;

/**
 * @version $Rev$ $Date$
 */
public class LocalAttributeManagerReadWriteTest extends TestCase {

    private static final String CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<ns2:attributes xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\">\n" +
                    "    <ns2:comment>This is a test comment.  Without it, a default warning comment will be created</ns2:comment>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/ca-helper-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jasper/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/j2ee-server/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/transaction/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jetty6/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"JettyWebConnector\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${HTTPPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"redirectPort\">${HTTPSPortPrimary + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"JettyAJP13Connector\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${AJPPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"redirectPort\">${HTTPSPortPrimary + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"JettySSLConnector\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${HTTPSPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/clustering/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"Node\">\n" +
                    "            <ns2:attribute name=\"nodeName\">${clusterNodeName}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/webservices-common/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/myfaces-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/myfaces/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/j2ee-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"WebBuilder\">\n" +
                    "            <ns2:attribute name=\"defaultNamespace\">http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"EnvironmentEntryBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/activemq-ra/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"ActiveMQ RA\">\n" +
                    "            <ns2:attribute name=\"ServerUrl\">tcp://${ServerHostname}:${ActiveMQPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/activemq-broker/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"ActiveMQ.tcp.default\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${ActiveMQPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"ActiveMQ.stomp.default\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${ActiveMQStompPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/system-database/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"DerbyNetwork\">\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${DerbyPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/connector-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"ResourceRefBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultEnvironment\">\n" +
                    "                <environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
                    "                    <environment:dependencies>\n" +
                    "                        <environment:dependency>\n" +
                    "                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
                    "                            <environment:artifactId>j2ee-corba-yoko</environment:artifactId>\n" +
                    "                            <environment:type>car</environment:type>\n" +
                    "            </environment:dependency>\n" +
                    "          </environment:dependencies>\n" +
                    "        </environment:environment></ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"AdminObjectRefBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"ClientResourceRefBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultEnvironment\">\n" +
                    "                <environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
                    "                    <environment:dependencies>\n" +
                    "                        <environment:dependency>\n" +
                    "                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
                    "                            <environment:artifactId>client-corba-yoko</environment:artifactId>\n" +
                    "                            <environment:type>car</environment:type>\n" +
                    "            </environment:dependency>\n" +
                    "          </environment:dependencies>\n" +
                    "        </environment:environment></ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jasper-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jetty6-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/client-security/2.1-TEST/car\" load=\"false\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/client/2.1-TEST/car\" load=\"false\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis2-ejb-deployer/2.1-TEST/car\" condition=\"props['org.apache.geronimo.jaxws.provider'] == 'axis2'\">\n" +
                    "        <ns2:gbean name=\"Axis2ModuleBuilderExtension\">\n" +
                    "            <ns2:attribute name=\"listener\">?name=${webcontainer}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jaxws-ejb-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jaxws-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/openejb-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"EjbRefBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"ClientEjbRefBuilder\">\n" +
                    "            <ns2:attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${OpenEJBPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/openejb/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"EJBNetworkService\">\n" +
                    "            <ns2:attribute name=\"port\">${OpenEJBPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"host\">${ServerHostname}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/openjpa/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis2-deployer/2.1-TEST/car\" condition=\"props['org.apache.geronimo.jaxws.provider'] == 'axis2'\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis2/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis2-ejb/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/system-database-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/console-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/pluto-support/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/spring/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.framework/transformer-agent/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/remote-deploy-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/cxf-ejb/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/cxf/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/debugviews-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/dojo-jetty6/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/persistence-jpa10-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"PersistenceUnitBuilder\">\n" +
                    "            <ns2:attribute name=\"defaultPersistenceProviderClassName\">org.apache.openjpa.persistence.PersistenceProviderImpl</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultPersistenceUnitProperties\">openjpa.Log=commons\n" +
                    "                                        openjpa.jdbc.SynchronizeMappings=buildSchema(ForeignKeys=true)\n" +
                    "                                        openjpa.jdbc.UpdateManager=operation-order\n" +
                    "                                        openjpa.Sequence=table(Table=OPENJPASEQ, Increment=100)</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultEnvironment\">\n" +
                    "                <environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
                    "                    <environment:dependencies>\n" +
                    "                        <environment:dependency>\n" +
                    "                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
                    "                            <environment:artifactId>openjpa</environment:artifactId>\n" +
                    "                            <environment:type>car</environment:type>\n" +
                    "            </environment:dependency>\n" +
                    "          </environment:dependencies>\n" +
                    "        </environment:environment></ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/activemq-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jetty6-clustering-wadi/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/wadi-clustering/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"DefaultBackingStrategyFactory\">\n" +
                    "            <ns2:attribute name=\"nbReplica\">${ReplicaCount}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"DefaultDispatcherHolder\">\n" +
                    "            <ns2:attribute name=\"endPointURI\">${EndPointURI}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"clusterName\">${ClusterName}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/uddi-jetty6/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/axis-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"AxisModuleBuilderExtension\">\n" +
                    "            <ns2:attribute name=\"listener\">?name=${webcontainer}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultEnvironment\">\n" +
                    "                <environment:environment xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
                    "                    <environment:dependencies>\n" +
                    "                        <environment:dependency>\n" +
                    "                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
                    "                            <environment:artifactId>${webcontainerName}</environment:artifactId>\n" +
                    "                            <environment:type>car</environment:type>\n" +
                    "            </environment:dependency>\n" +
                    "          </environment:dependencies>\n" +
                    "        </environment:environment></ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/j2ee-corba-yoko/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"NameServer\">\n" +
                    "            <ns2:attribute name=\"port\">${COSNamingPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"host\">${COSNamingHost}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"Server\">\n" +
                    "            <ns2:attribute name=\"port\">${ORBSSLPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"host\">${ORBSSLHost}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "        <ns2:gbean name=\"UnprotectedServer\">\n" +
                    "            <ns2:attribute name=\"port\">${ORBPort + PortOffset}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"host\">${ORBHost}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/mejb/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/jetty6-clustering-builder-wadi/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"JettyClusteringBuilder\">\n" +
                    "            <ns2:attribute name=\"defaultSweepInterval\">${DefaultWadiSweepInterval}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"defaultNumPartitions\">${DefaultWadiNumPartitions}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/hot-deployer/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"HotDeployer\">\n" +
                    "            <ns2:attribute name=\"path\">deploy/</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"pollIntervalMillis\">2000</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/openejb-corba-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/client-corba-yoko/2.1-TEST/car\" load=\"false\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/javamail/2.1-TEST/car\">\n" +
                    "        <ns2:gbean name=\"SMTPTransport\">\n" +
                    "            <ns2:attribute name=\"host\">${SMTPHost}</ns2:attribute>\n" +
                    "            <ns2:attribute name=\"port\">${SMTPPort + PortOffset}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/cxf-deployer/2.1-TEST/car\" condition=\"props.getProperty('org.apache.geronimo.jaxws.provider', 'cxf') == 'cxf'\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/welcome-jetty/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/sharedlib/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/client-deployer/2.1-TEST/car\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/client-transaction/2.1-TEST/car\" load=\"false\"/>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.configs/cxf-ejb-deployer/2.1-TEST/car\" condition=\"props.getProperty('org.apache.geronimo.jaxws.provider', 'cxf') == 'cxf'\">\n" +
                    "        <ns2:gbean name=\"CXFModuleBuilderExtension\">\n" +
                    "            <ns2:attribute name=\"listener\">?name=${webcontainer}</ns2:attribute>\n" +
                    "        </ns2:gbean>\n" +
                    "    </ns2:module>\n" +
                    "    <ns2:module name=\"org.apache.geronimo.plugins/plancreator-jetty/2.1-TEST/car\"/>\n" +
                    "</ns2:attributes>\n";
    
    public void testReadWrite() throws Exception {
        Reader reader = new StringReader(CONFIG);
        JexlExpressionParser parser = new JexlExpressionParser();
        ServerOverride serverOverride = LocalAttributeManager.read(reader, parser);
        StringWriter writer = new StringWriter();
        LocalAttributeManager.write(serverOverride, writer);
        String result = writer.toString();
        
        Document expectedDoc = DOMUtils.load(CONFIG);
        Document actualDoc = DOMUtils.load(result);
        
        DOMUtils.compareNodes(expectedDoc, actualDoc, true);
    }
}
