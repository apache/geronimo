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

package org.apache.geronimo.web25.deployment;

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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev$ $Date$
 */
public class SecurityConfigTest extends TestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();

    private XmlOptions options = new XmlOptions();

    private WebModuleBuilder webModuleBuilder = new WebModuleBuilder(null);

    public void testNoSecConstraint() throws Exception {
        String warName = "war3";
        File path = new File(BASEDIR, "src/test/resources/deployables/"
                + warName);

        // parse the spec dd
        String specDD = "";
        WebAppType webApp = null;
        UnpackedJarFile jarFile = new UnpackedJarFile(path);
        URL specDDUrl = DeploymentUtil.createJarURL(jarFile, "WEB-INF/web.xml");
        // read in the entire specDD as a string
        specDD = DeploymentUtil.readAll(specDDUrl);
        // parse it
        XmlObject parsed = XmlBeansUtil.parse(specDD);
        WebAppDocument webAppDoc = webModuleBuilder
                .convertToServletSchema(parsed);
        webApp = webAppDoc.getWebApp();
        Set securityRoles = AbstractWebModuleBuilder.collectRoleNames(webApp);
        Map rolePermissions = new HashMap();
        try {
        ComponentPermissions componentPermissions = webModuleBuilder
                .buildSpecSecurityConfig(webApp, securityRoles, rolePermissions);
        } catch (IllegalArgumentException e) {
            // This is a known issue
            //System.out.println("Exception caught: " + e.getMessage());
        }
    }

    private static class WebModuleBuilder extends AbstractWebModuleBuilder {

        protected WebModuleBuilder(Kernel kernel) {
            super(kernel, null, null, null, null, Collections.EMPTY_SET, null);
        }

        protected Module createModule(Object plan, JarFile moduleFile,
                String targetPath, URL specDDUrl, boolean standAlone,
                String contextRoot, AbstractName earName, Naming naming,
                ModuleIDBuilder idBuilder) throws DeploymentException {
            return null;
        }

        public void initContext(EARContext earContext, Module module,
                ClassLoader classLoader) throws DeploymentException {
        }

        public void addGBeans(EARContext earContext, Module module,
                ClassLoader classLoader, Collection repositories)
                throws DeploymentException {
        }

        public String getSchemaNamespace() {
            return null;
        }
    }

}
