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
package org.apache.management.j2ee;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * Static constants class which contains all of the J2EE notification types from the
 * J2EE management specification.
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/18 23:13:46 $
 */
public final class NotificationType {
    private NotificationType() {
    }

    /**
     * A new managed object was created.
     */
    public static final String OBJECT_CREATED = "j2ee.object.created";

    /**
     * A managed object was deleted
     */
    public static final String OBJECT_DELETED = "j2ee.object.deleted";

    /**
     * A state manageable object entered the starting state
     */
    public static final String STATE_STARTING = "j2ee.state.starting";

    /**
     * A state manageable object entered the running state
     */
    public static final String STATE_RUNNING = "j2ee.state.running";

    /**
     * A state manageable object entered the stopping state
     */
    public static final String STATE_STOPPING = "j2ee.state.stopping";

    /**
     * A state manageable object entered the stopped state.
     */
    public static final String STATE_STOPPED = "j2ee.state.stopped";

    /**
     * A state manageable object entered the failed state
     */
    public static final String STATE_FAILED = "j2ee.state.failed";

    /**
     * An attribute has change value
     */
    public static final String ATTRIBUTE_CHANGED = "j2ee.attribute.changed";

    /**
     * An array containg all of the know J2EE notification types
     */
    public static final String[] TYPES = new String[]{
        OBJECT_CREATED, OBJECT_DELETED,
        STATE_STARTING, STATE_RUNNING, STATE_STOPPING, STATE_STOPPED, STATE_FAILED,
        ATTRIBUTE_CHANGED
    };

    /**
     * A notification filter which lets all J2EE notifications pass
     */
    public static final NotificationFilter NOTIFICATION_FILTER = new J2EENotificationFilter();

    private static final class J2EENotificationFilter implements NotificationFilter {
        private J2EENotificationFilter() {
        }

        public boolean isNotificationEnabled(Notification notification) {
            String type = notification.getType();
            for (int i = 0; i < TYPES.length; i++) {
                if(TYPES[i].equals(type)) {
                    return true;
                }
            }
            return false;
        }
    };
}
