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
package org.apache.geronimo.client.builder;

import java.io.File;
import java.util.Collections;

import org.apache.geronimo.testsupport.TestSupport;

import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;

/**
 */
public class PlanParsingTest extends TestSupport {

    private ArtifactResolver clientArtifactResolver = null;
    private AppClientModuleBuilder builder;

    protected void setUp() throws Exception {
        super.setUp();
        builder = new AppClientModuleBuilder(new Environment(), null, null, null, null, null, null, Collections.<Repository>emptyList(), null, null, null, Collections.<ModuleBuilderExtension>emptyList(), clientArtifactResolver,"localhost",4021);
        builder.doStart();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        builder.doStop();
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(BASEDIR, "src/test/resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        GerApplicationClientType appClient = builder.getGeronimoAppClient(resourcePlan, null, true, null, null, null);
        assertEquals(1, appClient.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        GerApplicationClientDocument appClientDoc = GerApplicationClientDocument.Factory.newInstance();
        GerApplicationClientType appClient = appClientDoc.addNewApplicationClient();
        EnvironmentType clientEnvironmentType = appClient.addNewClientEnvironment();
        ArtifactType clientId = clientEnvironmentType.addNewModuleId();
        clientId.setGroupId("group");
        clientId.setArtifactId("artifact");
        EnvironmentType serverEnvironmentType = appClient.addNewServerEnvironment();
        serverEnvironmentType.setModuleId(clientId);

        GerResourceRefType ref = appClient.addNewResourceRef();
        ref.setRefName("ref");
        ref.setResourceLink("target");

        XmlBeansUtil.validateDD(appClient);
        // System.out.println(appClient.toString());
    }

    public void testConnectorInclude() throws Exception {
        File resourcePlan = new File(BASEDIR, "src/test/resources/plans/plan2.xml");
        assertTrue(resourcePlan.exists());
        GerApplicationClientType appClient = builder.getGeronimoAppClient(resourcePlan, null, true, null, null, null);
        assertEquals(1, appClient.getResourceRefArray().length);
        assertEquals(1, appClient.getResourceArray().length);
    }
}
