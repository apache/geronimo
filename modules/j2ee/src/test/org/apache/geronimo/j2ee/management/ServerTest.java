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

package org.apache.geronimo.j2ee.management;

import java.util.Arrays;

import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:52 $
 */
public class ServerTest extends Abstract77Test {
    private J2EEServer server;
    private String version;

    public void testStandardInterface() {
        assertEquals(SERVER_NAME.toString(), server.getobjectName());
        assertEquals(0, server.getdeployedObjects().length);
        assertEquals(0, server.getresources().length);
        assertTrue(Arrays.equals(new String[]{JVM_NAME.toString()}, server.getjavaVMs()));
        assertEquals("The Apache Software Foundation", server.getserverVendor());
        assertEquals(version, server.getserverVersion());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(SERVER_NAME.toString(), mbServer.getAttribute(SERVER_NAME, "objectName"));
        assertEquals(0, ((String[])mbServer.getAttribute(SERVER_NAME, "deployedObjects")).length);
        assertEquals(0, ((String[])mbServer.getAttribute(SERVER_NAME, "resources")).length);
        assertTrue(Arrays.equals(new String[]{JVM_NAME.toString()}, (String[])mbServer.getAttribute(SERVER_NAME, "javaVMs")));
        assertEquals("The Apache Software Foundation", mbServer.getAttribute(SERVER_NAME, "serverVendor"));
        assertEquals(version, mbServer.getAttribute(SERVER_NAME, "serverVersion"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        server = (J2EEServer) MBeanProxyFactory.getProxy(J2EEServer.class, mbServer, SERVER_NAME);
        version = (String) mbServer.getAttribute(SERVER_INFO_NAME, "Version");
    }
}
