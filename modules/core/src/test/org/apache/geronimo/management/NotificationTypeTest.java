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

package org.apache.geronimo.management;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.apache.geronimo.kernel.management.NotificationType;

import junit.framework.TestCase;

/**
 * Unit test for org.apache.geronimo.common.State class
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/08 04:36:32 $
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
