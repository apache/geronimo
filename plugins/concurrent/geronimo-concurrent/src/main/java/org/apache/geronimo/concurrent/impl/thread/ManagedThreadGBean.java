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
package org.apache.geronimo.concurrent.impl.thread;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.impl.GBeanBuilder;
import org.apache.geronimo.concurrent.impl.NotificationHelper;
import org.apache.geronimo.concurrent.thread.ManagedThread;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.EventProvider;
import org.apache.geronimo.management.ManagedConstants;

public class ManagedThreadGBean implements GBeanLifecycle, EventProvider, org.apache.geronimo.management.ManagedThread {

    private final static Log LOG = LogFactory.getLog(ManagedThreadGBean.class);
    
    public static final GBeanInfo GBEAN_INFO;

    private AbstractName name;
    private ManagedThread thread;

    private NotificationHelper notificationHelper;
    
    public ManagedThreadGBean(Kernel kernel, 
                              ClassLoader classLoader, 
                              AbstractName name) {
        this.name = name;
        
        this.notificationHelper = new NotificationHelper(kernel, name);
    }
            
    protected void sendNotification(ManagedThread.TaskState state) {
        if (this.notificationHelper.isNotificationSupported()) {
            String type = null;
            if (state == ManagedThread.TaskState.CANCELLED) {
                type = ManagedConstants.TASK_CANCELLED_STATE;
            } else if (state == ManagedThread.TaskState.RELEASED) {
                type = ManagedConstants.TASK_RELEASED_STATE;                
            } else if (state == ManagedThread.TaskState.HUNG) {
                type = ManagedConstants.TASK_HUNG_STATE;
            } else {
                // no notifications on other states
                return;
            }
            
            // TODO: 1) might need to properly synchronize to get accurate info
            //       2) some info only set on certain events?
            
            Properties userData = new Properties();
            userData.setProperty("managedthread", 
                                 getObjectName());            
            userData.setProperty("managedthread.threadID", 
                                 String.valueOf(getThreadID()));
            userData.setProperty("managedthread.threadName", 
                                 getThreadName());
            userData.setProperty("managedthread.taskRunTime", 
                                 String.valueOf(getTaskRunTime()));
            String taskDescription = getTaskIdentityDescription();
            if (taskDescription != null) {
                userData.setProperty("managedthread.taskIdentityDescription",
                                     taskDescription);
            }
            String taskName = getTaskIdentityName();
            if (taskName != null) {
                userData.setProperty("managedthread.taskIdentityName", 
                                     taskName);
            }
                       
            this.notificationHelper.sendNotification(type, userData);
        }
    }
    
    protected void setManagedThread(ManagedThread thread) {
        this.thread = thread;
    }
    
    public void doFail() {
        this.thread = null;
    }

    public void doStart() throws Exception {     
        LOG.debug("Thread gbean started: " + this.thread);
    }

    public void doStop() throws Exception {
        LOG.debug("Thread gbean stopped: " + this.thread);
        this.thread = null;
    }
    
    public boolean cancel() {
        return (this.thread == null) ? false : this.thread.cancelTask();
    }

    public long getHungTaskThreshold() {
        return (this.thread == null) ? -1 : this.thread.getHungTaskThreshold();
    }
    
    public void setHungTaskThreshold(long threshold) {
        if (this.thread != null) {
            this.thread.setHungTaskThreshold(threshold);
        }
    }

    public String getTaskIdentityDescription() {
        return (this.thread == null) ? null : this.thread.getTaskIdentityDescription();
    }

    public String getTaskIdentityDescription(String locale) {
        return (this.thread == null) ? null : this.thread.getTaskIdentityDescription(locale);
    }

    public String getTaskIdentityName() {
        return (this.thread == null) ? null : this.thread.getTaskIdentityName();
    }

    public long getTaskRunTime() {
        return (this.thread == null) ? 0 : this.thread.getTaskRunTime();
    }

    public long getThreadID() {
        return (this.thread == null) ? -1 : this.thread.getThreadID();
    }

    public String getThreadName() {
        return (this.thread == null) ? null : this.thread.getThreadName();
    }

    public boolean isTaskCancelled() {
        return (this.thread == null) ? false : this.thread.isTaskCancelled();
    }

    public boolean isTaskHung() {
        return (this.thread == null) ? false : this.thread.isTaskHung();
    }

    public AbstractName getName() {
        return this.name;
    }
    
    public String getObjectName() {
        return this.name.getObjectName().getCanonicalName();
    }

    public String[] getEventTypes() {
        return new String[] { ManagedConstants.TASK_HUNG_STATE,
                              ManagedConstants.TASK_CANCELLED_STATE,
                              ManagedConstants.TASK_RELEASED_STATE };        
    }
    
    public boolean isEventProvider() {
        return true;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }
    
    protected void verifyObjectName() {
        GBeanBuilder.verifyObjectName(getObjectName(),  
                                      ManagedConstants.MANAGED_THREAD, 
                                      ManagedConstants.MANAGED_THREAD);
    }
    
    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ManagedThreadGBean.class, ManagedConstants.MANAGED_THREAD);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false, false);
        infoFactory.addAttribute("kernel", Kernel.class, false, false);

        infoFactory.addInterface(org.apache.geronimo.management.ManagedThread.class);

        infoFactory.setConstructor(new String[]{"kernel", 
                                                "classLoader", 
                                                "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
       
}
