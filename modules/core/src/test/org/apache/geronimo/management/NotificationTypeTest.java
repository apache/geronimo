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

package org.apache.geronimo.management;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.apache.geronimo.kernel.management.NotificationType;

import junit.framework.TestCase;

/**
 * Unit test for org.apache.geronimo.common.State class
 *
 * @version $Rev$ $Date$
 */

public class NotificationTypeTest extends TestCase {
    private static final NotificationFilter filter = NotificationType.NOTIFICATION_FILTER;
    
    public void testName() {

        assertEquals("j2ee.object.created", NotificationType.OBJECT_CREATED);
        assertEquals("j2ee.object.deleted", NotificationType.OBJECT_DELETED);
        assertEquals("j2ee.state.starting", NotificationType.STATE_STARTING);
        assertEquals("j2ee.state.running", NotificationType.STATE_RUNNING);
        assertEquals("j2ee.state.stopping", NotificationType.STATE_STOPPING);
        assertEquals("j2ee.state.stopped", NotificationType.STATE_STOPPED);
        assertEquals("j2ee.state.failed", NotificationType.STATE_FAILED);
        assertEquals("j2ee.attribute.changed", NotificationType.ATTRIBUTE_CHANGED);
        
    }

    public void testNotificationFilter() {
        
        Object dummy = new Object();
        Notification notification = new Notification(NotificationType.OBJECT_CREATED,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        notification = new Notification(NotificationType.OBJECT_DELETED,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));

        notification = new Notification(NotificationType.STATE_STARTING,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        notification = new Notification(NotificationType.STATE_RUNNING,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        notification = new Notification(NotificationType.STATE_STOPPING,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        notification = new Notification(NotificationType.STATE_STOPPED,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        notification = new Notification(NotificationType.STATE_FAILED,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));

        notification = new Notification(NotificationType.ATTRIBUTE_CHANGED,dummy,1);
        assertTrue(filter.isNotificationEnabled(notification));
        
        notification = new Notification("hopefully unknown type",dummy,1);
        assertFalse(filter.isNotificationEnabled(notification));
    }

}
