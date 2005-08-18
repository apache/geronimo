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

import org.apache.geronimo.management.J2EEServer;


/**
 * @version $Rev$ $Date$
 */
public class ServerTest extends Abstract77Test {
    private J2EEServer server;
    private String version;

    public void testStandardInterface() throws Exception {
        assertEquals(SERVER_NAME.toString(), server.getObjectName());
        assertEquals(0, server.getDeployedObjects().length);
        assertEquals(0, server.getResources().length);
        assertObjectNamesEqual(new String[]{JVM_NAME.toString()}, server.getJavaVMs());
        assertEquals("The Apache Software Foundation", server.getServerVendor());
        assertEquals(version, server.getServerVersion());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(SERVER_NAME.toString(), kernel.getAttribute(SERVER_NAME, "objectName"));
        assertEquals(0, ((String[]) kernel.getAttribute(SERVER_NAME, "deployedObjects")).length);
        assertEquals(0, ((String[]) kernel.getAttribute(SERVER_NAME, "resources")).length);
        assertObjectNamesEqual(new String[]{JVM_NAME.toString()}, (String[]) kernel.getAttribute(SERVER_NAME, "javaVMs"));
        assertEquals("The Apache Software Foundation", kernel.getAttribute(SERVER_NAME, "serverVendor"));
        assertEquals(version, kernel.getAttribute(SERVER_NAME, "serverVersion"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        server = (J2EEServer) kernel.getProxyManager().createProxy(SERVER_NAME, J2EEServer.class);
        version = (String) kernel.getAttribute(SERVER_INFO_NAME, "version");
    }

    protected void tearDown() throws Exception {
        kernel.getProxyManager().destroyProxy(server);
        super.tearDown();
    }
}
