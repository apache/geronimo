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
package org.apache.geronimo.deployment.dependency;

import java.net.URL;
import java.io.File;
import javax.management.ObjectName;

import org.apache.management.j2ee.State;

import junit.framework.TestCase;
import mx4j.connector.RemoteMBeanServer;
import mx4j.connector.rmi.jrmp.JRMPConnector;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/20 03:26:06 $
 */
public class LifeCycleTest extends TestCase {
    private RemoteMBeanServer server;
    private final ObjectName deploymentControllerName = new ObjectName("geronimo.deployment:role=DeploymentController");
    private final ObjectName grandparentName = new ObjectName("family:role=Grandparent");
    private final ObjectName parentName = new ObjectName("family:role=Parent");
    private final ObjectName childName = new ObjectName("family:role=Child");
    private final URL deploymentURL = (new File("src/test/org/apache/geronimo/deployment/dependency/family-service.xml")).getAbsoluteFile().toURL();

    public LifeCycleTest() throws Exception {
    }

    public LifeCycleTest(String s) throws Exception {
        super(s);
    }

    public void testDoStartCalled() throws Exception {
        assertDoStartCalledOnAll();
    }

    public void testDoStopCalled() throws Exception {
        server.invoke(childName, "stop", null, null);
        Thread.sleep(500);
        boolean doStopCalled = ((Boolean) server.getAttribute(childName, "DoStopCalled")).booleanValue();
        assertTrue("doStop was not called", doStopCalled);
        server.invoke(childName, "reset", null, null);
        server.invoke(childName, "start", null, null);
    }

    public void testRecursiveRestart() throws Exception {
        // stopping the grandparent should stop parent and child becasue of dependency chain
        server.invoke(grandparentName, "stop", null, null);
        Thread.sleep(500);

        assertAllStopped();
        assertDoStopCalledOnAll();

        // reset the internal state, so we can check that doStart is called on the restart
        resetAll();

        // Start recursive from the parent down
        server.invoke(grandparentName, "startRecursive", null, null);

        assertAllRunning();
        assertDoStartCalledOnAll();
    }

    public void testOneByOneRestart() throws Exception {
        // stop the child first
        server.invoke(childName, "stop", null, null);
        Thread.sleep(500);

        // only the child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertRunning(parentName, "Parent");
        assertStopped(childName, "Child");
        assertDoStopCalled(childName, "Child");


        // stop the parent
        server.invoke(parentName, "stop", null, null);
        Thread.sleep(500);

        // now the parent and child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertStopped(childName, "Child");
        assertDoStopCalled(parentName, "Parent");


        // stop the grand parent
        server.invoke(grandparentName, "stop", null, null);
        Thread.sleep(500);

        assertAllStopped();
        assertDoStopCalled(grandparentName, "Grandparent");

        // reset the internal state, so we can check that doStart is called on the restart
        resetAll();

        // restart in reverse order
        server.invoke(grandparentName, "start", null, null);
        Thread.sleep(500);

        // now the parent and child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertStopped(childName, "Child");
        assertDoStartCalled(grandparentName, "Grandparent");


        // restart the parent
        server.invoke(parentName, "start", null, null);
        Thread.sleep(500);

        // only the child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertRunning(parentName, "Parent");
        assertStopped(childName, "Child");
        assertDoStartCalled(parentName, "Parent");


        // restart the child
        server.invoke(childName, "start", null, null);
        Thread.sleep(500);

        assertAllRunning();
        assertDoStartCalled(childName, "Child");
    }

    public void testMiddleStop() throws Exception {
        // stop the parent
        server.invoke(parentName, "stop", null, null);
        Thread.sleep(500);

        // now the parent and child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertStopped(childName, "Child");
        assertDoStopCalled(parentName, "Parent");
        assertDoStopCalled(childName, "Child");


        // reset the internal state, so we can check that doStart is called on the restart
        resetAll();

        // restart recursive the parent
        server.invoke(parentName, "startRecursive", null, null);
        Thread.sleep(500);

        assertAllRunning();
        assertDoStartCalled(parentName, "Parent");
        assertDoStartCalled(childName, "Child");
    }

    private void assertAllStopped() throws Exception {
        assertStopped(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertStopped(childName, "Child");
    }

    private void assertAllRunning() throws Exception {
        assertRunning(grandparentName, "Grandparent");
        assertRunning(parentName, "Parent");
        assertRunning(childName, "Child");
    }

    private void assertDoStartCalledOnAll() throws Exception {
        assertDoStartCalled(grandparentName, "Grandparent");
        assertDoStartCalled(parentName, "Parent");
        assertDoStartCalled(childName, "Child");
    }


    private void assertDoStopCalledOnAll() throws Exception {
        assertDoStopCalled(grandparentName, "Grandparent");
        assertDoStopCalled(parentName, "Parent");
        assertDoStopCalled(childName, "Child");
    }

    private void assertStopped(ObjectName objectName, String name) throws Exception {
        int state = ((Integer) server.getAttribute(objectName, "State")).intValue();
        assertEquals(name + " is not stopped", state, State.STOPPED_INDEX);
    }

    private void assertRunning(ObjectName objectName, String name) throws Exception {
        int state = ((Integer) server.getAttribute(objectName, "State")).intValue();
        assertEquals(name + " is not running", state, State.RUNNING_INDEX);
    }

    private void assertDoStopCalled(ObjectName objectName, String name) throws Exception {
        boolean doStopCalled = ((Boolean) server.getAttribute(objectName, "DoStopCalled")).booleanValue();
        assertTrue("doStop was not called on " + name, doStopCalled);
    }

    private void assertDoStartCalled(ObjectName objectName, String name) throws Exception {
        boolean doStartCalled = ((Boolean) server.getAttribute(objectName, "DoStartCalled")).booleanValue();
        assertTrue("doStart was not called on " + name, doStartCalled);
    }

    private void resetAll() throws Exception {
        server.invoke(grandparentName, "reset", null, null);
        server.invoke(parentName, "reset", null, null);
        server.invoke(childName, "reset", null, null);
    }

    // Deploys the family-service.xml file
    // after setup completes all MBeans should be freshly started
    protected void setUp() throws Exception {
        // Create a JRMPConnector
        JRMPConnector connector = new JRMPConnector();

        // Pass in the adaptor's JNDI name, no properties
        String jndiName = "jrmp";
        connector.connect(jndiName, null);

        // Get the remote MBeanServer from the connector
        // And use it as if it is an MBeanServer
        server = connector.getRemoteMBeanServer();

        server.invoke(deploymentControllerName,
                "deploy",
                new Object[]{deploymentURL},
                new String[]{"java.net.URL"});
    }

    protected void tearDown() throws Exception {
        server.invoke(deploymentControllerName,
                "undeploy",
                new Object[]{deploymentURL},
                new String[]{"java.net.URL"});
    }
}
