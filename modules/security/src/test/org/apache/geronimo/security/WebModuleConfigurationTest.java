/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import org.apache.geronimo.security.jacc.WebModuleConfiguration;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;


/**
 * Unit test for web module configuration
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:27 $
 */
public class WebModuleConfigurationTest extends AbstractLoaderUtilTest {
    private File docDir;
    WebModuleConfiguration module;
    WebAppType client;

    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        docDir = new File("src/test-data/xml/deployment");
    }

    public void testRead() throws Exception {


        File f = new File(docDir, "geronimo-web-app-testRead.xml");
        WebAppType webApp = WebAppDocument.Factory.parse(f).getWebApp();

        File s = new File(docDir, "geronimo-security.xml");
        GerSecurityType security = GerSecurityDocument.Factory.parse(s).getSecurity();

        module = new WebModuleConfiguration("pookie /test", webApp, security);
        assertSame("pookie /test", module.getContextID());

    }
}
