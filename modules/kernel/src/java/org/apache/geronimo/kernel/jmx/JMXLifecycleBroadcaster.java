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
package org.apache.geronimo.kernel.jmx;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * @version $Rev$ $Date$
 */
public class JMXLifecycleBroadcaster implements LifecycleBroadcaster {
    private final NotificationBroadcasterSupport notificationBroadcaster = new NotificationBroadcasterSupport();
    private final ObjectName objectName;
    private final LifecycleBroadcaster lifecycleBroadcaster;
    private long sequence;

    public JMXLifecycleBroadcaster(ObjectName objectName, LifecycleBroadcaster lifecycleBroadcaster) {
        this.objectName = objectName;
        this.lifecycleBroadcaster = lifecycleBroadcaster;
    }

    public void fireLoadedEvent() {
        lifecycleBroadcaster.fireLoadedEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.OBJECT_CREATED, objectName, nextSequence()));
    }

    public void fireStartingEvent() {
        lifecycleBroadcaster.fireStartingEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STARTING, objectName, nextSequence()));
    }

    public void fireRunningEvent() {
        lifecycleBroadcaster.fireRunningEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_RUNNING, objectName, nextSequence()));
    }

    public void fireStoppingEvent() {
        lifecycleBroadcaster.fireStoppingEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPING, objectName, nextSequence()));
    }

    public void fireStoppedEvent() {
        lifecycleBroadcaster.fireStoppedEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPED, objectName, nextSequence()));
    }

    public void fireFailedEvent() {
        lifecycleBroadcaster.fireFailedEvent();
        notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_FAILED, objectName, nextSequence()));
    }

    public void fireUnloadedEvent() {
        lifecycleBroadcaster.fireUnloadedEvent();
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
