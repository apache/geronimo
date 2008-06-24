/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.concurrent.impl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.jmx.MBeanServerKernelBridge;

public class NotificationHelper {
    
    private final static Log LOG = LogFactory.getLog(NotificationHelper.class);
    
    private AtomicInteger notificationSequence = new AtomicInteger(1);
    private NotificationBroadcasterSupport notificationBroadcasterSupport;
    private String objectName;
    
    public NotificationHelper(Kernel kernel, AbstractName name) {
        this.notificationBroadcasterSupport = getNotificationBroadcasterSupport(kernel, name);
        this.objectName = name.getObjectName().getCanonicalName();
    }
        
    private static NotificationBroadcasterSupport getNotificationBroadcasterSupport(Kernel kernel,
                                                                                    AbstractName name) {
        try {
            MBeanServerKernelBridge bridge = 
                (MBeanServerKernelBridge)kernel.getGBean(MBeanServerKernelBridge.class);
            return bridge.getNotificationBroadcasterSupport(name);
        } catch (Exception e) {
            LOG.debug("Failed to lookup MBeanServerKernelBridge", e);
            return null;
        }
    }
               
    public boolean isNotificationSupported() {
        return (this.notificationBroadcasterSupport != null);        
    }
    
    public void sendNotification(String type, Object userData) {
        if (!isNotificationSupported()) {
            return;
        }
        Notification notification = new Notification(type,
                                                     this.objectName, 
                                                     this.notificationSequence.getAndIncrement());
        notification.setUserData(userData);
        
        this.notificationBroadcasterSupport.sendNotification(notification);
    }
}
