package org.apache.geronimo.connector.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends TestCase {

    File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceAdapterNameUniqueness() throws Exception {
        File resourcePlan = new File(basedir, "src/test-data/data/dup-resourceadapter-name.xml");
        assertTrue(resourcePlan.exists());

        XmlObject plan = SchemaConversionUtils.parse(resourcePlan.toURL());
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

        XmlObject plan = SchemaConversionUtils.parse(resourcePlan.toURL());
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

        XmlObject plan = SchemaConversionUtils.parse(resourcePlan.toURL());
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

}
