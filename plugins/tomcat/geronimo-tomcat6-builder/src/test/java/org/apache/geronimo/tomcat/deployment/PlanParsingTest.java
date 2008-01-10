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
import java.util.Collections;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;

/**
 */
public class PlanParsingTest extends TestCase {
    private ClassLoader classLoader = this.getClass().getClassLoader();

    private Naming naming = new Jsr77Naming();
    private Artifact baseId = new Artifact("test", "base", "1", "car");
    private AbstractName baseRootName = naming.createRootName(baseId, "root", NameFactory.SERVICE_MODULE);
    private AbstractNameQuery tomcatContainerObjectName = new AbstractNameQuery(naming.createChildName(baseRootName, "TomcatContainer", NameFactory.GERONIMO_SERVICE));
    private WebServiceBuilder webServiceBuilder = null;
    private Environment defaultEnvironment = new Environment();
    private TomcatModuleBuilder builder;

    protected void setUp() throws Exception {
        builder = new TomcatModuleBuilder(defaultEnvironment,
            tomcatContainerObjectName,
            Collections.singleton(webServiceBuilder),
            Collections.singleton(new GeronimoSecurityBuilderImpl(null)),
            Collections.singleton(new GBeanBuilder(null, null)),
            new NamingBuilderCollection(null, null),
            Collections.EMPTY_LIST,
            null,
            new MockResourceEnvironmentSetter(),
            null);
        builder.doStart();
    }

    protected void tearDown() throws Exception {
        builder.doStop();
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

}
