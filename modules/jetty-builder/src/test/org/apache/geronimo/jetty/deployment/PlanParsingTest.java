package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.net.URI;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;

/**
 */
public class PlanParsingTest extends TestCase {
    ObjectName jettyContainerObjectName = JMXUtil.getObjectName("test:type=JettyContainer");
    ObjectName pojoWebServiceTemplate = null;
    WebServiceBuilder webServiceBuilder = null;
    private JettyModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));

    public PlanParsingTest() throws Exception {
        builder = new JettyModuleBuilder(new URI[] {URI.create("defaultParent")}, new Integer(1800), false, null, jettyContainerObjectName, new HashSet(), new HashSet(), new HashSet(), pojoWebServiceTemplate, webServiceBuilder, null, null);
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
        GerWebAppType GerWebAppType = jettyWebAppDoc.addNewWebApp();
        GerWebAppType.setConfigId("configId");
        GerWebAppType.setParentId("parentId");
        GerWebAppType.setContextPriorityClassloader(false);
        GerResourceRefType ref = GerWebAppType.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(GerWebAppType);
        System.out.println(GerWebAppType.toString());
    }

    public void testContextPriorityClassloader() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan3.xml");
        assertTrue(resourcePlan.exists());

        GerWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, false, null, null);
        assertFalse(jettyWebApp.getContextPriorityClassloader());
    }

    public void testContextPriorityClassloaderTrue() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan4.xml");
        assertTrue(resourcePlan.exists());

        GerWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, false, null, null);
        assertTrue(jettyWebApp.getContextPriorityClassloader());

    }

    public void testContextRootWithPlanAndContextSet() throws Exception {

        GerWebAppDocument jettyWebAppDoc = GerWebAppDocument.Factory.newInstance();
        GerWebAppType GerWebAppType = jettyWebAppDoc.addNewWebApp();
        GerWebAppType.setConfigId("myId");
        GerWebAppType.setContextRoot("myContextRoot");
        GerWebAppType.setContextPriorityClassloader(false);

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));

        GerWebAppType = builder.getJettyWebApp(GerWebAppType, dummyFile, true, null, null);

        assertEquals("myContextRoot", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanStandAlone() throws Exception {

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        GerWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, true, null, null);

        assertEquals("war2", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanAndTargetPath() throws Exception {

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        GerWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", null);

        assertEquals("myTargetPath", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanButWebApp() throws Exception {

        WebAppDocument webAppDocument = WebAppDocument.Factory.newInstance();
        WebAppType webAppType = webAppDocument.addNewWebApp();
        webAppType.setId("myId");

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        GerWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", webAppType);

        assertEquals("myId", GerWebAppType.getContextRoot());

    }

     public void testParseSpecDD() {

    }
}
