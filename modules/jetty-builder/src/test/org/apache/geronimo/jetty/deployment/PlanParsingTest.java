package org.apache.geronimo.jetty.deployment;

import java.io.File;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;

/**
 */
public class PlanParsingTest extends TestCase {
    ObjectName jettyContainerObjectName = JMXUtil.getObjectName("test:type=JettyContainer");
    private JettyModuleBuilder builder = new JettyModuleBuilder(null, new Integer(1800), null, jettyContainerObjectName, null, null, null, null, null);
    private File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        JettyWebAppDocument jettyWebAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType jettyWebAppType = jettyWebAppDoc.addNewWebApp();
        jettyWebAppType.setConfigId("configId");
        jettyWebAppType.setParentId("parentId");
        jettyWebAppType.setContextPriorityClassloader(false);
        GerResourceRefType ref = jettyWebAppType.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(jettyWebAppType);
        System.out.println(jettyWebAppType.toString());
    }

    public void testParseSpecDD() {

    }
}
