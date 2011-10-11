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
package org.apache.geronimo.j2ee.deployment;

import java.util.jar.JarFile;

import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * EAR config builder tests for naked JAVAEE 5 unpacked.
 *
 * @version $Rev:386276 $ $Date$
 */
public class EARConfigBuilder5NakedUnpackedTest
    extends EARConfigBuilderTestSupport
{
    protected void setUp() throws Exception {
        super.setUp();

        earFile = JarUtils.createJarFile(resolveFile("target/test-ear-javaee_5-naked-unpacked.ear"));
        locations.put(null, new Artifact("org.apache.geronimo.testsupport", "test-ear-javaee_5", "3.0-TEST", "ear"));
        ejbConfigBuilder.ejbModule = new EJBModule(false, ejbModuleName, null, null, null, "ejb.jar/", null, null, null, jndiContext, parentModule);
        webConfigBuilder.contextRoot = contextRoot;
        webConfigBuilder.webModule = new WebModule(false, webModuleName, null, null, (JarFile)null, "war.war/", null, null, null, contextRoot, WEB_NAMESPACE, jndiContext, parentModule);
        connectorConfigBuilder.connectorModule = new ConnectorModule(false, raModuleName, null, null, null, "rar.rar/", null, null, null, jndiContext, parentModule);
    }

    protected void tearDown() throws Exception {
        JarUtils.close(earFile);
        close(ejbConfigBuilder.ejbModule);
        close(webConfigBuilder.webModule);
        close(connectorConfigBuilder.connectorModule);

        super.tearDown();
    }
}
