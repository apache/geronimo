package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.config.GerJettyDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends TestCase {
    ObjectName jettyContainerObjectName = JMXUtil.getObjectName("test:type=JettyContainer");
    ObjectName pojoWebServiceTemplate = null;
    WebServiceBuilder webServiceBuilder = null;
    private JettyModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));

    public PlanParsingTest() throws Exception {
        builder = new JettyModuleBuilder(new URI[]{URI.create("defaultParent")}, new Integer(1800), false, null, jettyContainerObjectName, new HashSet(), new HashSet(), new HashSet(), pojoWebServiceTemplate, webServiceBuilder, null, null);
    }

    public void testContents() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity1() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1A.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity2() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1B.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity3() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1C.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
//        System.out.println(jettyWebApp.xmlText());
    }

    public void testOldFormat() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan2.xml");
        assertTrue(resourcePlan.exists());
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testOldFormatExploded() throws Exception {
        File war = new File(basedir, "src/test-resources/deployables/war5");
        assertTrue(war.exists());
        UnpackedJarFile moduleFile = new UnpackedJarFile(war);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void XtestOldFormatPackaged() throws Exception {
        File war = new File(basedir, "src/test-resources/deployables/war6.war");
        assertTrue(war.exists());
        JarFile moduleFile = new JarFile(war);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        JettyWebAppDocument jettyWebAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType webApp = jettyWebAppDoc.addNewWebApp();
        webApp.setConfigId("configId");
        webApp.setParentId("parentId");
        webApp.setContextPriorityClassloader(false);
        GerResourceRefType ref = webApp.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(webApp);
        System.out.println(webApp.toString());
    }

    public void testContextPriorityClassloader() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan3.xml");
        assertTrue(resourcePlan.exists());

        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, false, null, null);
        assertFalse(jettyWebApp.getContextPriorityClassloader());
    }

    public void testContextPriorityClassloaderTrue() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan4.xml");
        assertTrue(resourcePlan.exists());

        JettyWebAppType jettyWebApp = builder.getJettyWebApp(resourcePlan, null, false, null, null);
        assertTrue(jettyWebApp.getContextPriorityClassloader());

    }

    /** This test has 2 purposes: one the obvious one explicitly tested,
     * the other that passing a JettyWebAppType XmlObject in works.  This latter
     * models a web-app element inside an ear plan.
     *
     * @throws Exception
     */
    public void testContextRootWithPlanAndContextSet() throws Exception {

        JettyWebAppDocument jettyWebAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType webApp = jettyWebAppDoc.addNewWebApp();
        webApp.setConfigId("myId");
        webApp.setContextRoot("myContextRoot");
        webApp.setContextPriorityClassloader(false);

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));

        webApp = builder.getJettyWebApp(webApp, dummyFile, true, null, null);

        assertEquals("myContextRoot", webApp.getContextRoot());

    }

    public void testContextRootWithoutPlanStandAlone() throws Exception {

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, true, null, null);

        assertEquals("war2", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanAndTargetPath() throws Exception {

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", null);

        assertEquals("myTargetPath", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanButWebApp() throws Exception {

        WebAppDocument webAppDocument = WebAppDocument.Factory.newInstance();
        WebAppType webAppType = webAppDocument.addNewWebApp();
        webAppType.setId("myId");

        JarFile dummyFile = new JarFile(new File(basedir, "src/test-resources/deployables/war2.war"));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", webAppType);

        assertEquals("myId", GerWebAppType.getContextRoot());

    }

    public void testParseSpecDD() {

    }

    public void testConvertToJettySchema() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan4.xml");
        assertTrue(resourcePlan.exists());
        XmlObject rawPlan = XmlBeansUtil.parse(resourcePlan.toURL());
        XmlObject webPlan = new GenericToSpecificPlanConverter(GerJettyDocument.type.getDocumentElementName().getNamespaceURI(),
                JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "jetty").convertToSpecificPlan(rawPlan);
        File ConvertedPlan = new File(basedir, "src/test-resources/plans/plan4-converted.xml");
        assertTrue(ConvertedPlan.exists());
        XmlObject converted = XmlBeansUtil.parse(ConvertedPlan.toURL());
        XmlCursor c = converted.newCursor();
        SchemaConversionUtils.findNestedElement(c, "web-app");
        c.toFirstChild();
        ArrayList problems = new ArrayList();
        compareXmlObjects(webPlan, c, problems);
        assertEquals("problems: " + problems, 0, problems.size());
    }

    private boolean compareXmlObjects(XmlObject xmlObject, XmlCursor expected, List problems) {
        XmlCursor test = xmlObject.newCursor();
        boolean similar = true;
        int elementCount = 0;
        while (toNextStartToken(test)) {
            elementCount++;
            if (!toNextStartToken(expected)) {
                problems.add("test longer than expected at element: " + elementCount);
                return false;
            }
            String actualChars = test.getName().getLocalPart();
            String expectedChars = expected.getName().getLocalPart();
            if (!actualChars.equals(expectedChars)) {
                problems.add("Different elements at elementCount: " + elementCount + ", test: " + actualChars + ", expected: " + expectedChars);
                similar = false;
            }
            test.toNextToken();
            expected.toNextToken();
        }
        return similar;
    }

    private boolean toNextStartToken(XmlCursor cursor) {
        while (!cursor.isStart()) {
            if (!cursor.hasNextToken()) {
                return false;
            }
            cursor.toNextToken();
        }
        return true;
    }


}
