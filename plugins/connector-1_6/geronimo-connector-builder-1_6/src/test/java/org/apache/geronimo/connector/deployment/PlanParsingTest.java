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
package org.apache.geronimo.connector.deployment;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerConnectorType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends TestSupport {
    private final static QName CONNECTOR_QNAME = GerConnectorDocument.type.getDocumentElementName();
    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testLoadGeronimoDeploymentDescriptor10() throws Exception {
        URL srcXml = classLoader.getResource("connector_1_0/geronimo-ra.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, CONNECTOR_QNAME, GerConnectorType.type);
        XmlBeansUtil.validateDD(connector);
        assertEquals(1, connectorDocument.getConnector().getResourceadapterArray().length);
    }
    //TODO move to openejb-jee
//    public void testLoadJavaEEDeploymentDescriptor() throws Exception {
//        URL srcXml = classLoader.getResource("connector_1_6/ra.xml");
//        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
//        ConnectorDocument connectorDocument = (ConnectorDocument) plan.changeType(ConnectorDocument.type);
//        assertNotNull(connectorDocument.getConnector().getResourceadapter());
//        XmlBeansUtil.validateDD(connectorDocument);
//    }

    public void testLoadGeronimoDeploymentDescriptor15() throws Exception {
        URL srcXml = classLoader.getResource("connector_1_6/geronimo-ra.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, CONNECTOR_QNAME, GerConnectorType.type);
        assertEquals(1, connector.getResourceadapterArray().length);
    }

    public void testResourceAdapterNameUniqueness() throws Exception {
        URL srcXml = classLoader.getResource("data/dup-resourceadapter-name.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            XmlBeansUtil.validateDD(doc);
            fail("dup resource adapter name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testConnectionFactoryNameUniqueness() throws Exception {
        URL srcXml = classLoader.getResource("data/dup-connectionfactoryinstance-name.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            XmlBeansUtil.validateDD(doc);
            fail("dup connection factory name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testAdminObjectNameUniqueness() throws Exception {
        URL srcXml = classLoader.getResource("data/dup-admin-object-name.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            XmlBeansUtil.validateDD(doc);
            fail("dup admin object name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testRectifyPlan() throws Exception {
        URL srcXml = classLoader.getResource("data/old-schema-plan.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        GerConnectorType gerConnector = doc.getConnector();
        ConnectorPlanRectifier.rectifyPlan(gerConnector);
        gerConnector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(gerConnector, CONNECTOR_QNAME, GerConnectorType.type);
    }
}
