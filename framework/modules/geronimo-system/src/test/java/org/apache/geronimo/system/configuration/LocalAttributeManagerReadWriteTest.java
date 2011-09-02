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
    			"<attributes xmlns:ns2=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns=\"http://geronimo.apache.org/xml/ns/attributes-1.2\">\n" +
    			"    <comment>This is a test comment.  Without it, a default warning comment will be created</comment>\n" +
    			"    <module name=\"org.apache.geronimo.configs/ca-helper-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jasper/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/j2ee-server/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/transaction/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jetty6/2.1/car\">\n" +
    			"        <gbean name=\"JettyWebConnector\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${HTTPPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"redirectPort\">${HTTPSPortPrimary + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"JettyAJP13Connector\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${AJPPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"redirectPort\">${HTTPSPortPrimary + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"JettySSLConnector\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${HTTPSPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/clustering/2.1/car\">\n" +
    			"        <gbean name=\"Node\">\n" +
    			"            <attribute name=\"nodeName\">${clusterNodeName}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/webservices-common/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/myfaces-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/myfaces/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/j2ee-deployer/2.1/car\">\n" +
    			"        <gbean name=\"WebBuilder\">\n" +
    			"            <attribute name=\"defaultNamespace\">http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"EnvironmentEntryBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/activemq-ra/2.1/car\">\n" +
    			"        <gbean name=\"ActiveMQ RA\">\n" +
    			"            <attribute name=\"ServerUrl\">tcp://${ServerHostname}:${ActiveMQPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/activemq-broker/2.1/car\">\n" +
    			"        <gbean name=\"ActiveMQ.tcp.default\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${ActiveMQPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"ActiveMQ.stomp.default\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${ActiveMQStompPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/system-database/2.1/car\">\n" +
    			"        <gbean name=\"DerbyNetwork\">\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${DerbyPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/connector-deployer/2.1/car\">\n" +
    			"        <gbean name=\"ResourceRefBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"            <attribute name=\"defaultEnvironment\">\n" +
    			"                <environment:environment xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
    			"                    <environment:dependencies>\n" +
    			"                        <environment:dependency>\n" +
    			"                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
    			"                            <environment:artifactId>j2ee-corba-yoko</environment:artifactId>\n" +
    			"                            <environment:type>car</environment:type>\n" +
    			"            </environment:dependency>\n" +
    			"          </environment:dependencies>\n" +
    			"        </environment:environment>\n" +
    			"            </attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"AdminObjectRefBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"ClientResourceRefBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"            <attribute name=\"defaultEnvironment\">\n" +
    			"                <environment:environment xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
    			"                    <environment:dependencies>\n" +
    			"                        <environment:dependency>\n" +
    			"                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
    			"                            <environment:artifactId>client-corba-yoko</environment:artifactId>\n" +
    			"                            <environment:type>car</environment:type>\n" +
    			"            </environment:dependency>\n" +
    			"          </environment:dependencies>\n" +
    			"        </environment:environment>\n" +
    			"            </attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jasper-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jetty6-deployer/2.1/car\"/>\n" +
    			"    <module load=\"false\" name=\"org.apache.geronimo.configs/client-security/2.1/car\"/>\n" +
    			"    <module load=\"false\" name=\"org.apache.geronimo.configs/client/2.1/car\"/>\n" +
    			"    <module condition=\"props['org.apache.geronimo.jaxws.provider'] == 'axis2'\" name=\"org.apache.geronimo.configs/axis2-ejb-deployer/2.1/car\">\n" +
    			"        <gbean name=\"Axis2ModuleBuilderExtension\">\n" +
    			"            <attribute name=\"listener\">?name=${webcontainer}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jaxws-ejb-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jaxws-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/openejb-deployer/2.1/car\">\n" +
    			"        <gbean name=\"EjbRefBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"ClientEjbRefBuilder\">\n" +
    			"            <attribute name=\"eeNamespaces\">http://java.sun.com/xml/ns/j2ee,http://java.sun.com/xml/ns/javaee</attribute>\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"            <attribute name=\"port\">${OpenEJBPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/openejb/2.1/car\">\n" +
    			"        <gbean name=\"EJBNetworkService\">\n" +
    			"            <attribute name=\"port\">${OpenEJBPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"host\">${ServerHostname}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/openjpa/2.1/car\"/>\n" +
    			"    <module condition=\"props['org.apache.geronimo.jaxws.provider'] == 'axis2'\" name=\"org.apache.geronimo.configs/axis2-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/axis2/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/axis2-ejb/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/system-database-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/console-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/pluto-support/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/spring/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.framework/transformer-agent/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/remote-deploy-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/cxf-ejb/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/cxf/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/debugviews-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/dojo-jetty6/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/persistence-jpa10-deployer/2.1/car\">\n" +
    			"        <gbean name=\"PersistenceUnitBuilder\">\n" +
    			"            <attribute name=\"defaultPersistenceProviderClassName\">org.apache.openjpa.persistence.PersistenceProviderImpl</attribute>\n" +
    			"            <attribute name=\"defaultPersistenceUnitProperties\">openjpa.Log=commons\n" +
    			"                                        openjpa.jdbc.SynchronizeMappings=buildSchema(ForeignKeys=true)\n" +
    			"                                        openjpa.jdbc.UpdateManager=operation-order\n" +
    			"                                        openjpa.Sequence=table(Table=OPENJPASEQ, Increment=100)</attribute>\n" +
    			"            <attribute name=\"defaultEnvironment\">\n" +
    			"                <environment:environment xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
    			"                    <environment:dependencies>\n" +
    			"                        <environment:dependency>\n" +
    			"                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
    			"                            <environment:artifactId>openjpa</environment:artifactId>\n" +
    			"                            <environment:type>car</environment:type>\n" +
    			"            </environment:dependency>\n" +
    			"          </environment:dependencies>\n" +
    			"        </environment:environment>\n" +
    			"            </attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/activemq-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jetty6-clustering-wadi/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/wadi-clustering/2.1/car\">\n" +
    			"        <gbean name=\"DefaultBackingStrategyFactory\">\n" +
    			"            <attribute name=\"nbReplica\">${ReplicaCount}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"DefaultDispatcherHolder\">\n" +
    			"            <attribute name=\"endPointURI\">${EndPointURI}</attribute>\n" +
    			"            <attribute name=\"clusterName\">${ClusterName}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/uddi-jetty6/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/axis/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/axis-deployer/2.1/car\">\n" +
    			"        <gbean name=\"AxisModuleBuilderExtension\">\n" +
    			"            <attribute name=\"listener\">?name=${webcontainer}</attribute>\n" +
    			"            <attribute name=\"defaultEnvironment\">\n" +
    			"                <environment:environment xmlns:ns2=\"http://geronimo.apache.org/xml/ns/attributes-1.2\" xmlns=\"http://geronimo.apache.org/xml/ns/plugins-1.3\" xmlns:environment=\"http://geronimo.apache.org/xml/ns/deployment-1.2\">\n" +
    			"                    <environment:dependencies>\n" +
    			"                        <environment:dependency>\n" +
    			"                            <environment:groupId>org.apache.geronimo.configs</environment:groupId>\n" +
    			"                            <environment:artifactId>${webcontainerName}</environment:artifactId>\n" +
    			"                            <environment:type>car</environment:type>\n" +
    			"            </environment:dependency>\n" +
    			"          </environment:dependencies>\n" +
    			"        </environment:environment>\n" +
    			"            </attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/j2ee-corba-yoko/2.1/car\">\n" +
    			"        <gbean name=\"NameServer\">\n" +
    			"            <attribute name=\"port\">${COSNamingPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"host\">${COSNamingHost}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"Server\">\n" +
    			"            <attribute name=\"port\">${ORBSSLPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"host\">${ORBSSLHost}</attribute>\n" +
    			"        </gbean>\n" +
    			"        <gbean name=\"UnprotectedServer\">\n" +
    			"            <attribute name=\"port\">${ORBPort + PortOffset}</attribute>\n" +
    			"            <attribute name=\"host\">${ORBHost}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/mejb/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/jetty6-clustering-builder-wadi/2.1/car\">\n" +
    			"        <gbean name=\"JettyClusteringBuilder\">\n" +
    			"            <attribute name=\"defaultSweepInterval\">${DefaultWadiSweepInterval}</attribute>\n" +
    			"            <attribute name=\"defaultNumPartitions\">${DefaultWadiNumPartitions}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/hot-deployer/2.1/car\">\n" +
    			"        <gbean name=\"HotDeployer\">\n" +
    			"            <attribute name=\"path\">deploy/</attribute>\n" +
    			"            <attribute name=\"pollIntervalMillis\">2000</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.configs/openejb-corba-deployer/2.1/car\"/>\n" +
    			"    <module load=\"false\" name=\"org.apache.geronimo.configs/client-corba-yoko/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/javamail/2.1/car\">\n" +
    			"        <gbean name=\"SMTPTransport\">\n" +
    			"            <attribute name=\"host\">${SMTPHost}</attribute>\n" +
    			"            <attribute name=\"port\">${SMTPPort + PortOffset}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module condition=\"props.getProperty('org.apache.geronimo.jaxws.provider', 'cxf') == 'cxf'\" name=\"org.apache.geronimo.configs/cxf-deployer/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/welcome-jetty/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/sharedlib/2.1/car\"/>\n" +
    			"    <module name=\"org.apache.geronimo.configs/client-deployer/2.1/car\"/>\n" +
    			"    <module load=\"false\" name=\"org.apache.geronimo.configs/client-transaction/2.1/car\"/>\n" +
    			"    <module condition=\"props.getProperty('org.apache.geronimo.jaxws.provider', 'cxf') == 'cxf'\" name=\"org.apache.geronimo.configs/cxf-ejb-deployer/2.1/car\">\n" +
    			"        <gbean name=\"CXFModuleBuilderExtension\">\n" +
    			"            <attribute name=\"listener\">?name=${webcontainer}</attribute>\n" +
    			"        </gbean>\n" +
    			"    </module>\n" +
    			"    <module name=\"org.apache.geronimo.plugins/plancreator-jetty/2.1/car\"/>\n" +
    			"</attributes>\n";
    
    public void testReadWrite() throws Exception {
        Reader reader = new StringReader(CONFIG);
        JexlExpressionParser parser = new JexlExpressionParser();
        ServerOverride serverOverride = LocalAttributeManager.read(reader, parser);
        StringWriter writer = new StringWriter();
        LocalAttributeManager.write(serverOverride, writer);
        String result = writer.toString();
        
        Document expectedDoc = DOMUtils.load(CONFIG);
        Document actualDoc = DOMUtils.load(result);
        
        DOMUtils.compareNodes(expectedDoc, actualDoc);
    }
}
