package org.apache.geronimo.connector.deployment;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.geronimo.testsupport.TestSupport;

import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends TestSupport {
    private final static QName CONNECTOR_QNAME = GerConnectorDocument.type.getDocumentElementName();
    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testLoadGeronimoDeploymentDescriptor10() throws Exception {
        URL srcXml = classLoader.getResource("connector_1_0/geronimo-ra.xml");
//        File geronimoDD = new File(basedir, "src/test-data/connector_1_0/geronimo-ra.xml");
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, CONNECTOR_QNAME, GerConnectorType.type);
        XmlBeansUtil.validateDD(connector);
        assertEquals(1, connectorDocument.getConnector().getResourceadapterArray().length);
    }

    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        URL srcXml = classLoader.getResource("connector_1_5/ra.xml");
//        File j2eeDD = new File(basedir, "src/test-data/connector_1_5/ra.xml");
//        assertTrue(j2eeDD.exists());
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        ConnectorDocument connectorDocument = (ConnectorDocument) plan.changeType(ConnectorDocument.type);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        XmlBeansUtil.validateDD(connectorDocument);
    }

    public void testLoadGeronimoDeploymentDescriptor15() throws Exception {
        URL srcXml = classLoader.getResource("connector_1_5/geronimo-ra.xml");
//        File geronimoDD = new File(basedir, "src/test-data/connector_1_5/geronimo-ra.xml");
//        assertTrue(geronimoDD.exists());
        XmlObject plan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, CONNECTOR_QNAME, GerConnectorType.type);
        assertEquals(1, connector.getResourceadapterArray().length);
    }

    public void testResourceAdapterNameUniqueness() throws Exception {
        URL srcXml = classLoader.getResource("data/dup-resourceadapter-name.xml");
//        File resourcePlan = new File(basedir, "src/test-data/data/dup-resourceadapter-name.xml");
//        assertTrue(resourcePlan.exists());

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
//        File resourcePlan = new File(basedir, "src/test-data/data/dup-connectionfactoryinstance-name.xml");
//        assertTrue(resourcePlan.exists());

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
//        File resourcePlan = new File(basedir, "src/test-data/data/dup-admin-object-name.xml");
//        assertTrue(resourcePlan.exists());

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
//        File resourcePlan = new File(basedir, "src/test-data/data/old-schema-plan.xml");
//        assertTrue(resourcePlan.exists());

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
