/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.NullConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;

import org.apache.geronimo.testsupport.TestSupport;

/**
 * EAR config builder tests for naked J2EE 1.4 unpacked.
 *
 * @version $Rev:386276 $ $Date$
 */
public class EARConfigBuilder14NakedUnpackedTest
    extends EARConfigBuilderTestSupport
{
    protected void setUp() throws Exception {
        super.setUp();
        
        earFile = DeploymentUtil.createJarFile(resolveFile("target/test-ear-j2ee_1.4-naked-unpacked.ear"));
        ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, "ejb.jar/", null, null, null);
        webConfigBuilder.contextRoot = contextRoot;
        webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, "war.war/", null, null, null, contextRoot, portMap, WEB_NAMESPACE);
        connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, "rar.rar/", null, null, null);
    }

    protected void tearDown() throws Exception {
        DeploymentUtil.close(earFile);
        close(ejbConfigBuilder.ejbModule);
        close(webConfigBuilder.webModule);
        close(connectorConfigBuilder.connectorModule);
        
        super.tearDown();
    }
}
