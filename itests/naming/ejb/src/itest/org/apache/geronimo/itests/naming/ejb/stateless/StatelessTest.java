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
package org.apache.geronimo.itests.naming.ejb.stateless;

import java.util.Properties;
import javax.naming.InitialContext;

import junit.framework.TestCase;

/**
 * @version $Rev:  $ $Date:  $
 */
public class StatelessTest extends TestCase {

    public void testWebService() throws Exception {
        Properties props = new Properties();

        props.put("java.naming.factory.initial", "org.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "127.0.0.1:4201");
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");

        StatelessHome home = (StatelessHome) new InitialContext(props).lookup("ejb/StatelessHome");
        StatelessObject stateless = home.create();
        stateless.testWebService();
    }

}
