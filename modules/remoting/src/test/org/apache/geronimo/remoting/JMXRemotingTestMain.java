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

package org.apache.geronimo.remoting;

import java.rmi.Remote;
import java.io.Serializable;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.NotificationFilter;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.deployment.client.DeploymentNotification;
import org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * this test needs for a geronimo instance to be running and
 * so I guess this is really a IntegrationTest and not a Unit test.
 * This should move into the Integration test suite once it exists.
 *
 * It also needs the classes in this package to be added to the
 * server classpath so that the serializable NotificationListener
 * can be resolved by the server.
 */
public class JMXRemotingTestMain {

    Latch eventLatch = new Latch(); 
    
    public void XtestCheckClassLoaders() throws Exception {
        MBeanServer server = RemoteMBeanServerFactory.create("localhost");
        String[] strings = server.getDomains();
        for(int i=0; i < strings.length; i++){
            System.out.println("domain: "+strings[i]);
        }
    }
    
    class MyListner implements NotificationListener, Remote {
        private int lookingForID = -1;

        /**
         * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
         */
        public void handleNotification(Notification not, Object handback) {
            if(not instanceof DeploymentNotification) {
                int id = ((DeploymentNotification)not).getDeploymentID();
                if(lookingForID == -1) {
                    lookingForID = id;
                } else {
                    if(id != lookingForID) {
                        System.err.println("FAILED: should not get notification for ID "+id+" (expecting "+lookingForID+")");
                        return;
                    }
                }
            }
            System.out.println("Got notification: "+not.getType());
            eventLatch.release();
        }
        
    }

    static class MyFilter implements NotificationFilter, Serializable {
        public boolean isNotificationEnabled(Notification notification) {
            System.err.println("Filtering a notification: "+notification.getType());
            return true;
        }
    }

    public void testNotificationListner() throws Exception {
        System.out.println("adding listner..");
        MBeanServer server = RemoteMBeanServerFactory.create("localhost");
        ObjectName name = JMXUtil.getObjectName("geronimo.deployment:role=DeploymentController");
        MyListner listner = new MyListner();
        MyFilter filter = new MyFilter();
        server.addNotificationListener(name,listner,filter,null);
        eventLatch.acquire();
        System.out.println("Event received.");
        server.removeNotificationListener(name, listner, filter, null);
        System.out.println("Notifications removed.");
        Thread.sleep(1000*60);
    }

    public static void main(String[] args) throws Exception {
        new JMXRemotingTestMain().testNotificationListner();
    }
}
