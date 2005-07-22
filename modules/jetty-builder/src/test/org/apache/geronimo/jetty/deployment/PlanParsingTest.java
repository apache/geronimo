package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.util.jar.JarFile;
import java.util.Collections;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.deployment.util.UnpackedJarFile;

/**
 */
public class PlanParsingTest extends TestCase {
    ObjectName jettyContainerObjectName = JMXUtil.getObjectName("test:type=JettyContainer");
    ObjectName pojoWebServiceTemplate = null;
    WebServiceBuilder webServiceBuilder = null;
    private JettyModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));

    public PlanParsingTest() throws Exception {
        builder = new JettyModuleBuilder(null, new Integer(1800), null, jettyContainerObjectName, null, null, null, pojoWebServiceTemplate, webServiceBuilder, null, null);
    }

    public void testContents() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        GerWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testOldFormat() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan2.xml");
        assertTrue(resourcePlan.exists());
        GerWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testOldFormatExploded() throws Exception {
        File war = new File(basedir, "src/test-resources/deployables/war5");
        assertTrue(war.exists());
        UnpackedJarFile moduleFile = new UnpackedJarFile(war);
        GerWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testOldFormatPackaged() throws Exception {
        File war = new File(basedir, "src/test-resources/deployables/war6.war");
        assertTrue(war.exists());
        JarFile moduleFile = new JarFile(war);
        GerWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        GerWebAppDocument jettyWebAppDoc = GerWebAppDocument.Factory.newInstance();
        GerWebAppType jettyWebAppType = jettyWebAppDoc.addNewWebApp();
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
