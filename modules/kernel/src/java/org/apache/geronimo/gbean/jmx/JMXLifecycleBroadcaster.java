/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean.jmx;

import javax.management.ObjectName;
import javax.management.NotificationBroadcasterSupport;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.ListenerNotFoundException;

import org.apache.geronimo.kernel.LifecycleListener;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * @version $Rev$ $Date$
 */
public class JMXLifecycleBroadcaster implements LifecycleListener {
    private final NotificationBroadcasterSupport notificationBroadcaster = new NotificationBroadcasterSupport();
    private long sequence;

    public void loaded(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.OBJECT_CREATED, objectName, nextSequence()));
    }

    public void starting(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STARTING, objectName, nextSequence()));
    }

    public void running(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_RUNNING, objectName, nextSequence()));
    }

    public void stopping(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPING, objectName, nextSequence()));
    }

    public void stopped(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPED, objectName, nextSequence()));
    }

    public void failed(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_FAILED, objectName, nextSequence()));
    }

    public void unloaded(ObjectName objectName) {
        notificationBroadcaster.sendNotification(new Notification(NotificationType.OBJECT_DELETED, objectName, nextSequence()));
    }

    void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
        notificationBroadcaster.addNotificationListener(listener, filter, handback);
    }

    void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener);
    }

    void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener, filter, handback);
    }

    private synchronized long nextSequence() {
        return sequence++;
    }
}
