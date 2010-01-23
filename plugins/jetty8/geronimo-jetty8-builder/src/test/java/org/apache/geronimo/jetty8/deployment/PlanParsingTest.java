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
package org.apache.geronimo.jetty8.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.util.UnpackedJarFile;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.config.GerJettyDocument;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 */
public class PlanParsingTest extends XmlBeansTestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();

    private Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private AbstractName baseRootName = naming.createRootName(baseId, "root", NameFactory.SERVICE_MODULE);
    private AbstractNameQuery jettyContainerObjectName = new AbstractNameQuery(naming.createChildName(baseRootName, "jettyContainer", GBeanInfoBuilder.DEFAULT_J2EE_TYPE));
    private AbstractNameQuery credentialStoreName = new AbstractNameQuery(naming.createChildName(baseRootName, "CredentialStore", GBeanInfoBuilder.DEFAULT_J2EE_TYPE));
    private AbstractName pojoWebServiceTemplate = null;
    private WebServiceBuilder webServiceBuilder = null;
    private Environment defaultEnvironment = new Environment();
    private JettyModuleBuilder builder;
    private AtomicBoolean isDefault = new AtomicBoolean(false);

    protected void setUp() throws Exception {
        super.setUp();
        GeronimoSecurityBuilderImpl securityBuilder = new GeronimoSecurityBuilderImpl(null, null, null);
        builder = new JettyModuleBuilder(defaultEnvironment,
                new Integer(1800),
                null,
                jettyContainerObjectName,
                null, new HashSet(),
                new HashSet(),
                new HashSet(),
                null,
                null,
                pojoWebServiceTemplate,
                Collections.singleton(webServiceBuilder),
                null,
                Arrays.asList(new GBeanBuilder(null, null), securityBuilder),
                new NamingBuilderCollection(null),
                null,
                new MockResourceEnvironmentSetter(),
                null);
        builder.doStart();
        securityBuilder.doStart();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        builder.doStop();
    }

    public void testContents() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
//        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity1() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1A.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
//        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity2() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1B.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
//        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testMoveSecurity3() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan1C.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
//        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
//        log.debug(jettyWebApp.xmlText());
    }

    public void testOldFormat() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan2.xml");
        assertTrue(resourcePlan != null);
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(new File(resourcePlan.getFile()), null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
//        assertEquals(4, jettyWebApp.getSecurity().getRoleMappings().getRoleArray().length);
    }

    public void testConvertPlan() throws Exception {
        URL srcXml = classLoader.getResource("plans/plan-convert.xml");
        XmlObject rawPlan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());

        XmlObject webPlan = new GenericToSpecificPlanConverter(
                "http://geronimo.apache.org/xml/ns/web/jetty/config-1.0.1",
                "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.2",
                "jetty").convertToSpecificPlan(rawPlan);

        XmlObject p = webPlan.changeType(JettyWebAppType.type);
        XmlBeansUtil.validateDD(p);
    }

    public void testOldFormatExploded() throws Exception {
        URL war = classLoader.getResource("deployables/war5");
        assertTrue(war != null);
        UnpackedJarFile moduleFile = new UnpackedJarFile(new File(war.getFile()));
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, new DeployableJarFile(moduleFile), true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void XtestOldFormatPackaged() throws Exception {
        URL war = classLoader.getResource("deployables/war6.war");
        assertTrue(war != null);
        JarFile moduleFile = new JarFile(new File(war.getFile()));
        JettyWebAppType jettyWebApp = builder.getJettyWebApp(null, new DeployableJarFile(moduleFile), true, null, null);
        moduleFile.close();
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        JettyWebAppDocument jettyWebAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType webApp = jettyWebAppDoc.addNewWebApp();
        addEnvironment(webApp);
        GerResourceRefType ref = webApp.addNewResourceRef();
        ref.setRefName("ref");
        ref.setResourceLink("target");

        XmlBeansUtil.validateDD(webApp);
        log.debug(webApp.toString());
    }

    private void addEnvironment(JettyWebAppType webApp) {
        EnvironmentType environmentType = webApp.addNewEnvironment();
        ArtifactType configId = environmentType.addNewModuleId();
        configId.setGroupId("g");
        configId.setArtifactId("a");
        configId.setVersion("1");
        configId.setType("car");
    }

    /**
     * This test has 2 purposes: one the obvious one explicitly tested,
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

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));

        webApp = builder.getJettyWebApp(webApp, new DeployableJarFile(dummyFile), true, null, null);

        assertEquals("myContextRoot", webApp.getContextRoot());

    }

    public void testContextRootWithoutPlanStandAlone() throws Exception {

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, new DeployableJarFile(dummyFile), true, null, null);
        WebAppType webApp = getWebApp(dummyFile);
        String contextRoot = builder.getContextRoot(GerWebAppType, null, webApp, true, dummyFile, null);

        assertEquals("/war2", contextRoot);

    }

    public void testContextRootWithoutPlanAndTargetPath() throws Exception {

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, new DeployableJarFile(dummyFile), false, "myTargetPath", null);
        WebAppType webApp = getWebApp(dummyFile);
        String contextRoot = builder.getContextRoot(GerWebAppType, null, webApp, false, dummyFile, "myTargetPath");
        assertEquals("myTargetPath", contextRoot);

    }

    public void testContextRootWithoutPlanButWebApp() throws Exception {

        WebAppDocument webAppDocument = WebAppDocument.Factory.newInstance();
        WebAppType webAppType = webAppDocument.addNewWebApp();
        webAppType.setId("myId");

        URL war = classLoader.getResource("deployables/war2.war");
        assertTrue(war != null);
        JarFile dummyFile = new JarFile(new File(war.getFile()));
        JettyWebAppType GerWebAppType = builder.getJettyWebApp(null, new DeployableJarFile(dummyFile), false, "myTargetPath", webAppType);
//        WebAppType webApp = getWebApp(dummyFile);
        String contextRoot = builder.getContextRoot(GerWebAppType, null, webAppType, false, dummyFile, "myTargetPath");

        assertEquals("myId", contextRoot);

    }

    private WebAppType getWebApp(JarFile dummyFile) throws IOException, XmlException {
        URL specDDUrl = JarUtils.createJarURL(dummyFile, "WEB-INF/web.xml");
        XmlObject parsed = XmlBeansUtil.parse(specDDUrl, getClass().getClassLoader());
        WebAppDocument webAppDoc = (WebAppDocument) parsed.changeType(WebAppDocument.type);
        return webAppDoc.getWebApp();
    }

    public void testParseSpecDD() {

    }

    public void xtestConvertToJettySchema() throws Exception {
        URL resourcePlan = classLoader.getResource("plans/plan4.xml");
        assertTrue(resourcePlan != null);
        XmlObject rawPlan = XmlBeansUtil.parse(resourcePlan, getClass().getClassLoader());
        XmlObject webPlan = new GenericToSpecificPlanConverter(GerJettyDocument.type.getDocumentElementName().getNamespaceURI(),
                JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "jetty").convertToSpecificPlan(rawPlan);
        URL ConvertedPlan = classLoader.getResource("plans/plan4-converted.xml");
        assertTrue(ConvertedPlan != null);
        XmlObject converted = XmlBeansUtil.parse(ConvertedPlan, getClass().getClassLoader());
        XmlCursor c = converted.newCursor();
        SchemaConversionUtils.findNestedElement(c, JettyWebAppDocument.type.getDocumentElementName());
        c.toFirstChild();
        ArrayList problems = new ArrayList();
        compareXmlObjects(webPlan, c.getObject(), problems);
        assertEquals("problems: " + problems, 0, problems.size());
    }

}
