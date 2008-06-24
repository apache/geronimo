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
package org.apache.geronimo.concurrent.impl.executor;

import java.util.Properties;

import org.apache.geronimo.concurrent.impl.NotificationHelper;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThread;
import org.apache.geronimo.concurrent.impl.thread.GeronimoManagedThreadFactory;
import org.apache.geronimo.concurrent.impl.thread.ManagedThreadGBean;
import org.apache.geronimo.concurrent.impl.thread.TrackingManagedThreadFactory;
import org.apache.geronimo.management.ManagedConstants;

/**
 * ThreadFactory to be used within 
 * ServerManagedExecutorService or ServerManagedScheduledExecutorService.
 */
public class ServerManagedThreadFactory extends TrackingManagedThreadFactory {

    private NotificationHelper notificationHelper;
        
    public ServerManagedThreadFactory(GeronimoManagedThreadFactory factory,
                                      NotificationHelper notificationHelper) { 
        super(factory);
        this.notificationHelper = notificationHelper;
    }
    
    private void sendNotification(ManagedThreadGBean threadGBean) {
        if (this.notificationHelper.isNotificationSupported()) {
            Properties userData = new Properties();
            userData.setProperty("managedthread", threadGBean.getObjectName());
                        
            this.notificationHelper.sendNotification(ManagedConstants.NEW_THREAD_EVENT, userData);
        }
    }
    
    @Override
    public Thread newThread(Runnable runnable) {        
        GeronimoManagedThread thread = (GeronimoManagedThread)super.newThread(runnable); 
                                
        // send JMX notification
        sendNotification(thread.getGbean());
        
        return thread;
    }
                    
    public String[] getEventTypes() {
        return new String[] { ManagedConstants.NEW_THREAD_EVENT };
    }   
    
    public String[] getThreads() {
        return GeronimoManagedThreadFactory.getThreads(getThreadList());
    }
    
    public String[] getHungTaskThreads() {
        return GeronimoManagedThreadFactory.getHungTaskThreads(getThreadList());
    }

}
