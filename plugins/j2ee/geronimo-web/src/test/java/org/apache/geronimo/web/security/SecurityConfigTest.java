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

package org.apache.geronimo.web.security;

import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class SecurityConfigTest extends TestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();


    public void testNoSecConstraint() throws Exception {
        URL specDDUrl = classLoader.getResource("security/web-nosecurity.xml");
        InputStream in = specDDUrl.openStream();
        try {
            WebApp webApp = (WebApp) JaxbJavaee.unmarshal(WebApp.class, in);
            SpecSecurityBuilder builder = new SpecSecurityBuilder(webApp);
            ComponentPermissions componentPermissions = builder.buildSpecSecurityConfig();
        } finally {
            in.close();
        }
    }
}
