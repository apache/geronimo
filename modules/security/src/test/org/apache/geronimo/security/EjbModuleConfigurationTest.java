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

import org.apache.geronimo.security.jacc.EJBModuleConfiguration;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;


/**
 * Unit test for EJB module configuration
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:10 $
 */
public class EjbModuleConfigurationTest extends AbstractLoaderUtilTest {
    private File docDir;
    EJBModuleConfiguration module;
    WebAppType client;

    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        docDir = new File("src/test-data/xml/deployment");
    }

    public void testRead() throws Exception {

        File f = new File(docDir, "geronimo-ejb-jar-testRead.xml");
        System.out.println("file at: " + f.getAbsolutePath());

        EjbJarType ejbJar = EjbJarDocument.Factory.parse(f).getEjbJar();

        File s = new File(docDir, "geronimo-security.xml");

        GerSecurityType security = GerSecurityDocument.Factory.parse(s).getSecurity();

        assertTrue(security.getDefaultPrincipal() != null);

        module = new EJBModuleConfiguration("pookie test", ejbJar, security);
        assertSame("pookie test", module.getContextID());
    }
}
