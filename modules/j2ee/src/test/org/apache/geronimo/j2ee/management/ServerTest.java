/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.j2ee.management;

import java.util.Arrays;

import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/02/22 05:19:10 $
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
