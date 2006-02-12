package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.Environment;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
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
    private ClassLoader classLoader = this.getClass().getClassLoader();

    ObjectName jettyContainerObjectName = JMXUtil.getObjectName("test:type=JettyContainer");
    ObjectName pojoWebServiceTemplate = null;
    WebServiceBuilder webServiceBuilder = null;
    private Environment defaultEnvironment = new Environment();
    private JettyModuleBuilder builder;

    public PlanParsingTest() throws Exception {
        builder = new JettyModuleBuilder(defaultEnvironment, new Integer(1800), false, null, jettyContainerObjectName, new HashSet(), new HashSet(), new HashSet(), pojoWebServiceTemplate, webServiceBuilder, null, null);
    }

    public void testContents() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity1() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1A.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity2() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1B.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity3() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1C.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
//        System.out.println(jettyWebApp.xmlText());
    }

    public void testOldFormat() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan2.xml");
        assertTrue(resourcePlan!= null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testOldFormatExploded() throws Exception {
        URL war = classLoader.getResource("deployables/war5");
        assertTrue(war != null);
        UnpackedJarFile moduleFile = new UnpackedJarFile(new File(war.getFile()));
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void XtestOldFormatPackaged() throws Exception {
        URL war = classLoader.getResource("deployables/war6.war");
        assertTrue(war != null);
        JarFile moduleFile = new JarFile(new File(war.getFile()));
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, moduleFile, true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        JettyWebAppDocument jettyWebAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType webApp = jettyWebAppDoc.addNewWebApp();
        addEnvironment(webApp);
        webApp.setContextPriorityClassloader(false);
        GerResourceRefType ref = webApp.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(webApp);
        System.out.println(webApp.toString());
    }

    private void addEnvironment(JettyWebAppType webApp) {
        EnvironmentType environmentType = webApp.addNewEnvironment();
        ArtifactType configId = environmentType.addNewConfigId();
        configId.setGroupId("g");
        configId.setArtifactId("a");
        configId.setVersion("1");
        configId.setType("car");
        environmentType.addNewClassloader();
    }

    public void testContextPriorityClassloader() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan3.xml");
        assertTrue(resourcePlan != null);

        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, false, null, null);
        assertFalse(jettyWebApp.getContextPriorityClassloader());
    }

    public void testContextPriorityClassloaderTrue() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan4.xml");
        assertTrue(resourcePlan != null);

        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, false, null, null);
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
        addEnvironment(webApp);
        webApp.setContextRoot("myContextRoot");
        webApp.setContextPriorityClassloader(false);

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));

        webApp = builder.getJettyWebApp(webApp, dummyFile, true, null, null);

        assertEquals("myContextRoot", webApp.getContextRoot());

    }

    public void testContextRootWithoutPlanStandAlone() throws Exception {

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, true, null, null);

        assertEquals("war2", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanAndTargetPath() throws Exception {

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", null);

        assertEquals("myTargetPath", GerWebAppType.getContextRoot());

    }

    public void testContextRootWithoutPlanButWebApp() throws Exception {

        WebAppDocument webAppDocument = WebAppDocument.Factory.newInstance();
        WebAppType webAppType = webAppDocument.addNewWebApp();
        webAppType.setId("myId");

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, dummyFile, false, "myTargetPath", webAppType);

        assertEquals("myId", GerWebAppType.getContextRoot());

    }

    public void testParseSpecDD() {

    }

    public void testConvertToJettySchema() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan4.xml");
        assertTrue(resourcePlan != null);
        XmlObject rawPlan = XmlBeansUtil.parse(resourcePlan);
        XmlObject webPlan = new GenericToSpecificPlanConverter(GerJettyDocument.type.getDocumentElementName().getNamespaceURI(),
                JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "jetty").convertToSpecificPlan(rawPlan);
        URL ConvertedPlan = classLoader.getResource("plans/plan4-converted.xml");
        assertTrue(ConvertedPlan != null);
        XmlObject converted = XmlBeansUtil.parse(ConvertedPlan);
        XmlCursor c = converted.newCursor();
        SchemaConversionUtils.findNestedElement(c, JettyWebAppDocument.type.getDocumentElementName());
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
            QName actualName = test.getName();
            QName expectedName = expected.getName();
            if (!actualName.equals(expectedName)) {
                problems.add("Different elements at elementCount: " + elementCount + ", test: " + actualName + ", expected: " + expectedName);
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
