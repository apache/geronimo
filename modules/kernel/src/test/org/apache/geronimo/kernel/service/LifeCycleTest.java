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
package org.apache.geronimo.kernel.service;

import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.management.State;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/06 20:01:53 $
 */
public class LifeCycleTest extends TestCase {
    private JMXKernel kernel;
    private MBeanServer server;

    private final ObjectName grandparentName = new ObjectName("family:role=Grandparent");
    private final ObjectName parentName = new ObjectName("family:role=Parent");
    private final ObjectName childName = new ObjectName("family:role=Child");

    public LifeCycleTest() throws Exception {
    }

    public LifeCycleTest(String s) throws Exception {
        super(s);
    }

    public void testDependencies() throws Exception {
        DependencyService2MBean dependencyService = (DependencyService2MBean) MBeanProxyFactory.getProxy(
                DependencyService2MBean.class,
                server,
                new ObjectName("geronimo.boot:role=DependencyService2"));

        Set children;
        children = dependencyService.getChildren(grandparentName);
        assertEquals(1, children.size());
        assertTrue(children.contains(parentName));

        children = dependencyService.getChildren(parentName);
        assertEquals(1, children.size());
        assertTrue(children.contains(childName));

        children = dependencyService.getChildren(childName);
        assertEquals(0, children.size());
    }

    public void testAllRunning() throws Exception {
        assertAllRunning();
    }

    public void testDoStartCalled() throws Exception {
        assertDoStartCalledOnAll();
    }

    public void testDoStopCalled() throws Exception {
        server.invoke(childName, "stop", null, null);
        Thread.sleep(500);
        assertDoStopCalled(childName, "child");
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

    public void testDoFailCalled() throws Exception {
        server.invoke(childName, "fail", null, null);
        assertFailed(childName, "child");
        assertDoFailCalled(childName, "child");

        server.invoke(childName, "reset", null, null);
        server.invoke(childName, "stop", null, null);
        assertStopped(childName, "child");
        assertDoStopCalled(childName, "child");

        server.invoke(childName, "reset", null, null);
        server.invoke(childName, "start", null, null);
        assertRunning(childName, "child");
        assertDoStartCalled(childName, "child");
    }

    public void testCascadeFail() throws Exception {
        // stopping the grandparent should stop parent and child becasue of dependency chain
        server.invoke(grandparentName, "fail", null, null);
        Thread.sleep(500);

        assertAllFailed();
        assertDoFailCalledOnAll();

        // reset the internal state, so we can check that doStop is called on the restart
        resetAll();

        // stop the grandparent... parent and child should still be failed
        server.invoke(grandparentName, "stop", null, null);
        assertStopped(grandparentName, "Grandparent");
        assertFailed(parentName, "Parent");
        assertFailed(childName, "Child");

        // stop the parent... child should still be failed
        server.invoke(parentName, "stop", null, null);
        assertStopped(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertFailed(childName, "Child");

        // stop the child... all should be stopped
        server.invoke(childName, "stop", null, null);
        assertAllStopped();

        // Start recursive from the grandparent down
        resetAll();
        server.invoke(grandparentName, "startRecursive", null, null);

        assertAllRunning();
        assertDoStartCalledOnAll();
    }

    public void testMiddleFail() throws Exception {
        // stop the parent
        server.invoke(parentName, "fail", null, null);

        // now the parent and child should be stopped
        assertRunning(grandparentName, "Grandparent");
        assertFailed(parentName, "Parent");
        assertFailed(childName, "Child");
        assertDoFailCalled(parentName, "Parent");
        assertDoFailCalled(childName, "Child");

        // stop the parent and child
        server.invoke(parentName, "stop", null, null);
        server.invoke(childName, "stop", null, null);

        // reset the internal state, so we can check that doStart is called on the restart
        resetAll();

        // restart recursive the parent
        server.invoke(parentName, "startRecursive", null, null);

        assertAllRunning();
        assertDoStartCalled(parentName, "Parent");
        assertDoStartCalled(childName, "Child");
    }

    private void assertAllStopped() throws Exception {
        assertStopped(grandparentName, "Grandparent");
        assertStopped(parentName, "Parent");
        assertStopped(childName, "Child");
    }

    private void assertAllFailed() throws Exception {
        assertFailed(grandparentName, "Grandparent");
        assertFailed(parentName, "Parent");
        assertFailed(childName, "Child");
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


    private void assertDoFailCalledOnAll() throws Exception {
        assertDoFailCalled(grandparentName, "Grandparent");
        assertDoFailCalled(parentName, "Parent");
        assertDoFailCalled(childName, "Child");
    }

    private void assertStopped(ObjectName objectName, String name) throws Exception {
        int state = ((Integer) server.getAttribute(objectName, "state")).intValue();
        assertEquals(name + " is not stopped", state, State.STOPPED_INDEX);
    }

    private void assertFailed(ObjectName objectName, String name) throws Exception {
        int state = ((Integer) server.getAttribute(objectName, "state")).intValue();
        assertEquals(name + " is not failed", state, State.FAILED_INDEX);
    }

    private void assertRunning(ObjectName objectName, String name) throws Exception {
        int state = ((Integer) server.getAttribute(objectName, "state")).intValue();
        assertEquals(name + " is not running", state, State.RUNNING_INDEX);
    }

    private void assertDoStopCalled(ObjectName objectName, String name) throws Exception {
        boolean doStopCalled = ((Boolean) server.getAttribute(objectName, "doStopCalled")).booleanValue();
        assertTrue("doStop was not called on " + name, doStopCalled);
    }

    private void assertDoFailCalled(ObjectName objectName, String name) throws Exception {
        boolean doFailCalled = ((Boolean) server.getAttribute(objectName, "doFailCalled")).booleanValue();
        assertTrue("doFail was not called on " + name, doFailCalled);
    }

    private void assertDoStartCalled(ObjectName objectName, String name) throws Exception {
        boolean doStartCalled = ((Boolean) server.getAttribute(objectName, "doStartCalled")).booleanValue();
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
        kernel = new JMXKernel("geronimo");
        server = kernel.getMBeanServer();

        server.createMBean("org.apache.geronimo.kernel.service.DependencyService2", new ObjectName("geronimo.boot:role=DependencyService2"));

        GeronimoMBean mbean;

        mbean = new GeronimoMBean(createPersonMBeanInfo(null));
        server.registerMBean(mbean, grandparentName);
        server.setAttribute(grandparentName, new Attribute("name", "Grandparent"));

        mbean = new GeronimoMBean(createPersonMBeanInfo(grandparentName));
        server.registerMBean(mbean, parentName);
        server.setAttribute(parentName, new Attribute("name", "Parent"));

        mbean = new GeronimoMBean(createPersonMBeanInfo(parentName));
        server.registerMBean(mbean, childName);
        server.setAttribute(childName, new Attribute("name", "Child"));

        server.invoke(grandparentName, "start", null, null);
        server.invoke(parentName, "start", null, null);
        server.invoke(childName, "start", null, null);
    }

    private GeronimoMBeanInfo createPersonMBeanInfo(ObjectName parent) {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass("org.apache.geronimo.kernel.service.PersonImpl");
        mbeanInfo.setName("PersonImpl");
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("doStartCalled", true, false));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("doStopCalled", true, false));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("doFailCalled", true, false));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("reset"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("fail"));

        if(parent != null) {
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("Parent", "org.apache.geronimo.kernel.service.Person", parent, true));
        }
        return mbeanInfo;
    }

    protected void tearDown() throws Exception {
        server.unregisterMBean(childName);
        server.unregisterMBean(parentName);
        server.unregisterMBean(grandparentName);
        kernel.release();
    }

    public void testNothing() {
    }
}
