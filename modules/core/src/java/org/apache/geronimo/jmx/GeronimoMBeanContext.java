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
package org.apache.geronimo.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Notification;

/**
 * Context handle for Geronimo MBean targets which gives a target a reference to the MBean server, the object name
 * of the GeronimoMBean containing the target, allows the target to send MBean notifications.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/05 02:38:32 $
 */
public class GeronimoMBeanContext {
    private MBeanServer server;
    private GeronimoMBean geronimoMBean;
    private ObjectName objectName;

    /**
     * Creates a new context for a target.
     *
     * @param server a reference to the mbean server in which the Geronimo Mbean is registered
     * @param geronimoMBean the Geronimo Mbean the contains the target
     * @param objectName the registered name of the Geronimo MBean
     */
    public GeronimoMBeanContext(MBeanServer server, GeronimoMBean geronimoMBean, ObjectName objectName) {
        this.server = server;
        this.geronimoMBean = geronimoMBean;
        this.objectName = objectName;
    }

    /**
     * Gets a reference to the MBean server in which the Geronimo MBean is registered.
     * @return a reference to the MBean server in which the Geronimo MBean is registered
     */
    public MBeanServer getServer() {
        return server;
    }

    /**
     * Gets the registered name of the Geronimo MBean
     * @return the registered name of the Geronimo MBean
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     * Sends the specified notification in a javax.management.Notification.
     * The message must be declared in the a GeronimoNotificationInfo.
     * @param message the message to send
     */
    public void sendNotification(String message) {
        geronimoMBean.sendNotification(message);
    }

    /**
     * Sends the specified notification .
     * The norification must be declared in the a GeronimoNotificationInfo.
     * @param notification the notification to send
     */
    public void sendNotification(Notification notification) {
        geronimoMBean.sendNotification(notification);
    }
}
