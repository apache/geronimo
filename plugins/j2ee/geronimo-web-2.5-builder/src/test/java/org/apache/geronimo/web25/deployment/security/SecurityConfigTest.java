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

package org.apache.geronimo.web25.deployment.security;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev$ $Date$
 */
public class SecurityConfigTest extends TestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();

    private XmlOptions options = new XmlOptions();

    public void testNoSecConstraint() throws Exception {
        URL srcXml = classLoader.getResource("deployables/war3/WEB-INF/web.xml");
        WebAppDocument webAppDoc = WebAppDocument.Factory.parse(srcXml, options);
        WebAppType webApp = webAppDoc.getWebApp();
        SpecSecurityBuilder builder = new SpecSecurityBuilder();
        ComponentPermissions componentPermissions = builder.buildSpecSecurityConfig(webApp);
    }


}
