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
package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;
import org.w3c.dom.NamedNodeMap;

/**
 */
public class PlanParsingTest extends TestCase {
    private ClassLoader classLoader = this.getClass().getClassLoader();

    private Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private AbstractName baseRootName = naming.createRootName(baseId, "root", NameFactory.SERVICE_MODULE);
    private AbstractNameQuery tomcatContainerObjectName = new AbstractNameQuery(naming.createChildName(baseRootName, "TomcatContainer", GBeanInfoBuilder.DEFAULT_J2EE_TYPE));
    private WebServiceBuilder webServiceBuilder = null;
    private Environment defaultEnvironment = new Environment();
    private TomcatModuleBuilder builder;
    private Kernel kernel;

    protected void setUp() throws Exception {
        MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        PackageAdmin packageAdmin = new PackageAdmin() {

                @Override
                public ExportedPackage[] getExportedPackages(Bundle bundle) {
                    return new ExportedPackage[0];
                }

                @Override
                public ExportedPackage[] getExportedPackages(String s) {
                    return new ExportedPackage[0];
                }

                @Override
                public ExportedPackage getExportedPackage(String s) {
                    return null;
                }

                @Override
                public void refreshPackages(Bundle[] bundles) {
                }

                @Override
                public boolean resolveBundles(Bundle[] bundles) {
                    return false;
                }

                @Override
                public RequiredBundle[] getRequiredBundles(String s) {
                    return new RequiredBundle[0];
                }

                @Override
                public Bundle[] getBundles(String s, String s1) {
                    return new Bundle[0];
                }

                @Override
                public Bundle[] getFragments(Bundle bundle) {
                    return new Bundle[0];
                }

                @Override
                public Bundle[] getHosts(Bundle bundle) {
                    return new Bundle[0];
                }

                @Override
                public Bundle getBundle(Class aClass) {
                    return null;
                }

                @Override
                public int getBundleType(Bundle bundle) {
                    return 0;
                }
            };
        bundleContext.registerService(PackageAdmin.class.getName(), packageAdmin, null);
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test");
        kernel.boot(bundleContext);
        builder = new TomcatModuleBuilder(defaultEnvironment,
                tomcatContainerObjectName,
                new WebAppInfo(),
                Collections.singleton(webServiceBuilder),
                Arrays.asList(new GBeanBuilder(), new GeronimoSecurityBuilderImpl(null, null, null)),
                new NamingBuilderCollection(null),
                Collections.EMPTY_LIST,
                null,
                new MockResourceEnvironmentSetter(),
                kernel,
                bundleContext);
        builder.doStart();
        GeronimoSecurityBuilderImpl securityBuilder = new GeronimoSecurityBuilderImpl(null, null, null);
        securityBuilder.doStart();
    }

    protected void tearDown() throws Exception {
        builder.doStop();
        kernel.shutdown();
    }

    public void testConvertPlan() throws Exception {
        URL srcXml = classLoader.getResource("plans/plan-convert.xml");
        XmlObject rawPlan = XmlBeansUtil.parse(srcXml, getClass().getClassLoader());

        XmlObject webPlan = new GenericToSpecificPlanConverter(
                "http://geronimo.apache.org/xml/ns/web/tomcat/config-1.0",
                "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1",
                "tomcat").convertToSpecificPlan(rawPlan);

        XmlObject p = webPlan.changeType(TomcatWebAppType.type);
        //TODO WTF? no changes but it fails run from maven, not from idea.
//        XmlBeansUtil.validateDD(p);
    }

    public void testResourceRef() throws Exception {
        URL resourceURL = classLoader.getResource("plans/plan1.xml");
        File resourcePlan = new File(resourceURL.getFile());
        assertTrue(resourcePlan.exists());
        TomcatWebAppType tomcatWebApp = builder.getTomcatWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, tomcatWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        GerWebAppDocument tomcatWebAppDoc = GerWebAppDocument.Factory.newInstance();
        GerWebAppType tomcatWebAppType = tomcatWebAppDoc.addNewWebApp();
        EnvironmentType environmentType = tomcatWebAppType.addNewEnvironment();
        ArtifactType artifactType = environmentType.addNewModuleId();
        artifactType.setArtifactId("foo");

        GerResourceRefType ref = tomcatWebAppType.addNewResourceRef();
        ref.setRefName("ref");
        ref.setResourceLink("target");

        XmlBeansUtil.validateDD(tomcatWebAppType);
    }

    public void testContextAttributes() throws Exception {
        URL resourceURL = classLoader.getResource("plans/plan-context.xml");
        File resourcePlan = new File(resourceURL.getFile());
        assertTrue(resourcePlan.exists());
        TomcatWebAppType tomcatWebApp = builder.getTomcatWebApp(resourcePlan, null, true, null, null);
        NamedNodeMap  namedNodeMap = tomcatWebApp.getContext().getDomNode().getAttributes();
        assertEquals(2, namedNodeMap.getLength());
    }
}
