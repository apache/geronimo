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
package org.apache.geronimo.deployment;

import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;

import org.apache.geronimo.jmx.JMXUtil;
import org.apache.log4j.Logger;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:47:53 $
 */
public class DeploymentController implements MBeanRegistration, NotificationListener, DeploymentControllerMBean {
    private static final ObjectName DEFAULT_NAME = JMXUtil.getObjectName("geronimo.deployment:type=DeploymentController");
    private static final String MBEAN_REGISTERED = "JMX.mbean.registered";
    private static final String MBEAN_UNREGISTERED = "JMX.mbean.unregistered";

    private static final ObjectName DEPLOYER_PATTERN = JMXUtil.getObjectName("*:type=Deployer");
    private static final Logger log = Logger.getLogger(DeploymentController.class);

    private MBeanServer server;
    private final Set deployers = new HashSet();

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        server = mBeanServer;
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(MBEAN_REGISTERED);
        filter.enableType(MBEAN_UNREGISTERED);
        server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, filter, null);
        return objectName == null ? DEFAULT_NAME : objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
        server.removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
    }

    public void postDeregister() {
    }

    public void handleNotification(Notification notification, Object o) {
        List mbeans = (List) o;
        for (Iterator i = mbeans.iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
/* comment out until we get mx4j from maven fixed
            if (DEPLOYER_PATTERN.apply(name)) {
                if (MBEAN_REGISTERED.equals(notification.getType())) {
                    log.info("Adding Deployer "+name);
                    deployers.add(name);
                } else if (MBEAN_UNREGISTERED.equals(notification.getType())) {
                    log.info("Removing Deployer "+name);
                    deployers.remove(name);
                }
            }
*/
        }
    }
}
