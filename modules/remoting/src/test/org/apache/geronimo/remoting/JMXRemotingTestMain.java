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

package org.apache.geronimo.remoting;

import java.io.Serializable;
import java.rmi.Remote;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

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
        for (int i = 0; i < strings.length; i++) {
            System.out.println("domain: " + strings[i]);
        }
    }

    class MyListner implements NotificationListener, Remote {
        private int lookingForID = -1;

        public void handleNotification(Notification not, Object handback) {
            if (not instanceof DeploymentNotification) {
                int id = ((DeploymentNotification) not).getDeploymentID();
                if (lookingForID == -1) {
                    lookingForID = id;
                } else {
                    if (id != lookingForID) {
                        System.err.println("FAILED: should not get notification for ID " + id + " (expecting " + lookingForID + ")");
                        return;
                    }
                }
            }
            System.out.println("Got notification: " + not.getType());
            eventLatch.release();
        }

    }

    static class MyFilter implements NotificationFilter, Serializable {
        public boolean isNotificationEnabled(Notification notification) {
            System.err.println("Filtering a notification: " + notification.getType());
            return true;
        }

        public int hashCode() {
            return 1;
        }

        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj.getClass() != getClass())
                return false;
            return true;
        }
    }

    public void testNotificationListner() throws Exception {
        System.out.println("adding listner..");
        MBeanServer server = RemoteMBeanServerFactory.create("localhost");
        ObjectName name = ObjectName.getInstance("geronimo.deployment:role=DeploymentController");
        MyListner listner = new MyListner();
        MyFilter filter = new MyFilter();
        server.addNotificationListener(name, listner, filter, null);
        eventLatch.acquire();
        System.out.println("Event received.");
        server.removeNotificationListener(name, listner, filter, null);
        System.out.println("Notifications removed.");
        Thread.sleep(1000 * 60);
    }

    public static void main(String[] args) throws Exception {
        new JMXRemotingTestMain().testNotificationListner();
    }
}
