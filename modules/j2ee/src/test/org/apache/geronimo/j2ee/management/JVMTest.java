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

import java.net.InetAddress;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/02/22 05:19:10 $
 */
public class JVMTest extends Abstract77Test {
    private JVM jvm;
    private org.apache.geronimo.j2ee.management.geronimo.JVM jvmEx;
    private String node;
    private Runtime runtime;

    public void testStandardInterface() {
        assertEquals(JVM_NAME.toString(), jvm.getobjectName());
        assertEquals(System.getProperty("java.version"), jvm.getjavaVersion());
        assertEquals(System.getProperty("java.vendor"), jvm.getjavaVendor());
        assertEquals(node, jvm.getnode());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(JVM_NAME.toString(), mbServer.getAttribute(JVM_NAME, "objectName"));
        assertEquals(System.getProperty("java.version"), mbServer.getAttribute(JVM_NAME, "javaVersion"));
        assertEquals(System.getProperty("java.vendor"), mbServer.getAttribute(JVM_NAME, "javaVendor"));
        assertEquals(node, mbServer.getAttribute(JVM_NAME, "node"));
    }

    public void testGeronimoInterface() {
        assertEquals(runtime.availableProcessors(), jvmEx.getavailableProcessors());

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(runtime.freeMemory(), jvmEx.getfreeMemory());
        assertEquals(runtime.maxMemory(), jvmEx.getmaxMemory());
        assertEquals(runtime.totalMemory(), jvmEx.gettotalMemory());

    }

    public void testGeronimoAttributes() throws Exception {
        assertEquals(new Integer(runtime.availableProcessors()), mbServer.getAttribute(JVM_NAME, "availableProcessors"));

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(new Long(runtime.freeMemory()), mbServer.getAttribute(JVM_NAME, "freeMemory"));
        assertEquals(new Long(runtime.maxMemory()), mbServer.getAttribute(JVM_NAME, "maxMemory"));
        assertEquals(new Long(runtime.totalMemory()), mbServer.getAttribute(JVM_NAME, "totalMemory"));

    }

    protected void setUp() throws Exception {
        super.setUp();
        jvm = (JVM) MBeanProxyFactory.getProxy(JVM.class, mbServer, JVM_NAME);
        jvmEx = (org.apache.geronimo.j2ee.management.geronimo.JVM) MBeanProxyFactory.getProxy(org.apache.geronimo.j2ee.management.geronimo.JVM.class, mbServer, JVM_NAME);
        node = InetAddress.getLocalHost().toString();
        runtime = Runtime.getRuntime();
    }
}
