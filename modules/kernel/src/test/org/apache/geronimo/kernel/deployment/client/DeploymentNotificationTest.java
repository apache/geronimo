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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment.client;

import org.apache.geronimo.kernel.deployment.ServerTarget;
import org.apache.geronimo.kernel.deployment.GeronimoTargetModule;

import javax.management.Notification;
import javax.enterprise.deploy.spi.TargetModuleID;

import junit.framework.TestCase;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/06 17:39:16 $
 */
public class DeploymentNotificationTest extends TestCase {
    private final static TargetModuleID TARGET = new GeronimoTargetModule(new ServerTarget("dummytarget"), "dummymodule");
    private final static String TYPE = "DummyType";
    private final static Object OBJECT = new Object();
    private final static long SEQ = 1;
    private final static int DEPLOYMENT = 2;
    private final static String MESSAGE = "DummyMessage";
    private final static long TIMESTAMP = System.currentTimeMillis();

    /**
     * Tests the constants defined in the {@link DeploymentNotification}.
     */
    public void testConstants() {
        assertEquals("DEPLOYMENT_COMPLETED is app.deploy.completed", DeploymentNotification.DEPLOYMENT_COMPLETED, "app.deploy.completed");
        assertEquals("DEPLOYMENT_FAILED is app.deploy.failure", DeploymentNotification.DEPLOYMENT_FAILED, "app.deploy.failure");
        assertEquals("DEPLOYMENT_UPDATE is app.deploy.update", DeploymentNotification.DEPLOYMENT_UPDATE, "app.deploy.update");
    }

    /**
     * Tests the {@link DeploymentNotification#DeploymentNotification(java.lang.String, java.lang.Object, long, int, TargetModuleID)
     * form of constructor.
     */
    public void testSimpleConstructor() {
        DeploymentNotification dn = new DeploymentNotification(TYPE, OBJECT, SEQ, DEPLOYMENT, TARGET);
        doDeploymentNotificationTest(dn);
    }

    /**
     * Tests the {@link DeploymentNotification#DeploymentNotification(java.lang.String, java.lang.Object, long, long, int, javax.enterprise.deploy.spi.TargetModuleID)
     * form of constructor
     */
    public void testTimestampConstructor() {
        DeploymentNotification dn = new DeploymentNotification(TYPE, OBJECT, SEQ, TIMESTAMP, DEPLOYMENT, TARGET);
        doDeploymentNotificationTest(dn);
        doNotificationTimestampTest(dn);
    }

    /**
     * Tests the {@link DeploymentNotification#DeploymentNotification(java.lang.String, java.lang.Object, long, java.lang.String, int, javax.enterprise.deploy.spi.TargetModuleID)
     * form of constructor
     */
    public void testMessageConstructor() {
        DeploymentNotification dn = new DeploymentNotification(TYPE, OBJECT, SEQ, MESSAGE, DEPLOYMENT, TARGET);
        doDeploymentNotificationTest(dn);
        doNotificationMessageTest(dn);
    }

    /**
     * Tests the {@link DeploymentNotification#DeploymentNotification(java.lang.String, java.lang.Object, long, long, java.lang.String, int, javax.enterprise.deploy.spi.TargetModuleID)
     * form of the constructor
     */
    public void testMessageTimestampConstructor() {
        DeploymentNotification dn = new DeploymentNotification(TYPE, OBJECT, SEQ, TIMESTAMP, MESSAGE, DEPLOYMENT, TARGET);
        doDeploymentNotificationTest(dn);
        doNotificationMessageTest(dn);
        doNotificationTimestampTest(dn);
    }

    /**
     * Tests {@link DeploymentNotification#setDeploymentID(int)} and {@link DeploymentNotification#setTargetModuleID(javax.enterprise.deploy.spi.TargetModuleID)
     * mutator methods
     */
    public void testMutators() {
        DeploymentNotification dn = new DeploymentNotification(TYPE, OBJECT, SEQ, DEPLOYMENT, TARGET);
        doDeploymentNotificationTest(dn);
        int secondDeployment = DEPLOYMENT + 1;
        dn.setDeploymentID(secondDeployment);
        assertEquals("Setter indeed changed the deploymentID", secondDeployment, dn.getDeploymentID());
        GeronimoTargetModule TARGET2 = new GeronimoTargetModule(new ServerTarget("dummytarget2"), "dummymodule2");
        dn.setTargetModuleID(TARGET2);
        assertSame("Setter indeed changed the target", TARGET2, dn.getTargetModuleID());
    }

    private void doNotificationTest(Notification notification) {
        assertEquals("Type returned is the one used on creation", TYPE, notification.getType());
        assertSame("Object returned is the one used on creation", OBJECT, notification.getSource());
        assertEquals("Sequence returned is the one used on creation", SEQ, notification.getSequenceNumber());

    }

    private void doNotificationTimestampTest(Notification notification) {
        assertEquals("Timestamp returned is the one used on creation", TIMESTAMP, notification.getTimeStamp());
    }

    private void doNotificationMessageTest(Notification notification) {
        assertEquals("Message returned is the one used on creation", MESSAGE, notification.getMessage());
    }

    private void doDeploymentNotificationTest(DeploymentNotification dn) {
        assertEquals("Deployment ID returned is the one used on creation", dn.getDeploymentID(), DEPLOYMENT);
        assertSame("TargetID returned is the one used on creation", dn.getTargetModuleID(), TARGET);
        doNotificationTest(dn);
    }
}
