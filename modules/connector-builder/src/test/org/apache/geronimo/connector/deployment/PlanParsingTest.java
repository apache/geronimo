package org.apache.geronimo.connector.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends TestCase {

    File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    public void testLoadGeronimoDeploymentDescriptor10() throws Exception {
        File geronimoDD = new File(basedir, "src/test-data/connector_1_0/geronimo-ra.xml");
        assertTrue(geronimoDD.exists());
        XmlObject plan = XmlBeansUtil.parse(geronimoDD.toURL());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, "connector", GerConnectorType.type);
        SchemaConversionUtils.validateDD(connector);
        assertEquals(1, connectorDocument.getConnector().getResourceadapterArray().length);
    }

    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        File j2eeDD = new File(basedir, "src/test-data/connector_1_5/ra.xml");
        assertTrue(j2eeDD.exists());
        XmlObject plan = XmlBeansUtil.parse(j2eeDD.toURL());
        ConnectorDocument connectorDocument = (ConnectorDocument) plan.changeType(ConnectorDocument.type);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        SchemaConversionUtils.validateDD(connectorDocument);
    }

    public void testLoadGeronimoDeploymentDescriptor15() throws Exception {
        File geronimoDD = new File(basedir, "src/test-data/connector_1_5/geronimo-ra.xml");
        assertTrue(geronimoDD.exists());
        XmlObject plan = XmlBeansUtil.parse(geronimoDD.toURL());
        GerConnectorDocument connectorDocument = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        GerConnectorType connector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(connectorDocument, "connector", GerConnectorType.type);
        assertEquals(1, connector.getResourceadapterArray().length);
    }

    public void testResourceAdapterNameUniqueness() throws Exception {
        File resourcePlan = new File(basedir, "src/test-data/data/dup-resourceadapter-name.xml");
        assertTrue(resourcePlan.exists());

        XmlObject plan = XmlBeansUtil.parse(resourcePlan.toURL());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            SchemaConversionUtils.validateDD(doc);
            fail("dup resource adapter name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testConnectionFactoryNameUniqueness() throws Exception {
        File resourcePlan = new File(basedir, "src/test-data/data/dup-connectionfactoryinstance-name.xml");
        assertTrue(resourcePlan.exists());

        XmlObject plan = XmlBeansUtil.parse(resourcePlan.toURL());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            SchemaConversionUtils.validateDD(doc);
            fail("dup connection factory name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testAdminObjectNameUniqueness() throws Exception {
        File resourcePlan = new File(basedir, "src/test-data/data/dup-admin-object-name.xml");
        assertTrue(resourcePlan.exists());

        XmlObject plan = XmlBeansUtil.parse(resourcePlan.toURL());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        try {
            SchemaConversionUtils.validateDD(doc);
            fail("dup admin object name is invalid");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testRectifyPlan() throws Exception {
        File resourcePlan = new File(basedir, "src/test-data/data/old-schema-plan.xml");
        assertTrue(resourcePlan.exists());

        XmlObject plan = XmlBeansUtil.parse(resourcePlan.toURL());
        GerConnectorDocument doc = (GerConnectorDocument) plan.changeType(GerConnectorDocument.type);
        if (doc == null) {
            doc = (GerConnectorDocument) plan;
        }
        GerConnectorType gerConnector = doc.getConnector();
        ConnectorPlanRectifier.rectifyPlan(gerConnector);
        gerConnector = (GerConnectorType) SchemaConversionUtils.fixGeronimoSchema(gerConnector, "connector", GerConnectorType.type);
    }

}
