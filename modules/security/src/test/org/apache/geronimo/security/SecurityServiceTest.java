/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.security;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.security.jacc.EJBModuleConfiguration;
import org.apache.geronimo.security.jacc.WebModuleConfiguration;
import org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;

/**
 * Unit test for web module configuration
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:10 $
 */
public class SecurityServiceTest extends TestCase {
    SecurityService securityService;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        securityService = new SecurityService();

        PropertiesFileSecurityRealm securityRealm = new PropertiesFileSecurityRealm("Foo",
                (new File(new File("."), "src/test-data/data/users.properties")).toURI(),
                (new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        securityRealm.doStart();

        securityService.setRealms(Collections.singleton(securityRealm));
        EjbJarType ejbJar = EjbJarType.Factory.newInstance();
        ejbJar.addNewEnterpriseBeans();
        AssemblyDescriptorType assemblyDescriptor = ejbJar.addNewAssemblyDescriptor();
        assemblyDescriptor.addNewExcludeList();
        GerSecurityType security = GerSecurityType.Factory.newInstance();
        WebAppType webApp = WebAppType.Factory.newInstance();

        securityService.setModuleConfigurations(Arrays.asList(new Object[] {new EJBModuleConfiguration("Foo", ejbJar, security),new WebModuleConfiguration("Bar", webApp, security)}));
    }

    public void tearDown() throws Exception {
    }

    public void testConfig() throws Exception {
    }
}
