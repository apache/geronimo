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

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.concurrent.thread.BasicManagedThreadFactory;
import org.apache.geronimo.concurrent.thread.ManagedThread;

public class GeronimoManagedThreadFactory extends BasicManagedThreadFactory {
    
    // gbean that manages this object
    private ManagedThreadFactoryGBean factoryGBean;
        
    private HungTaskMonitor hungTaskMonitor;
    private long hungTaskMonitorFrequency = 1000 * 60; // 1 minute
    
    public GeronimoManagedThreadFactory(ManagedThreadFactoryGBean gbean) {                                       
        this.factoryGBean = gbean;
    }
    
    public void setHungTaskMonitorFrequency(long newFrequency) {
        if (newFrequency <= 0) {
            throw new IllegalArgumentException("Frequency must be greater than 0");
        }
        this.hungTaskMonitorFrequency = newFrequency;
    }
    
    public long getHungTaskMonitorFrequency() {
        return this.hungTaskMonitorFrequency;
    }
    
    @Override
    protected ManagedThread createThread(ThreadGroup group, Runnable runnable, String name) {               
        GeronimoManagedThread managedThread =  
            new GeronimoManagedThread(group, runnable, name, this);
        
        // add gbean for the thread
        this.factoryGBean.addThreadGBean(managedThread);

        startHungTaskMonitor();
        
        return managedThread;
    }
    
    @Override
    public void threadStopped(Thread thread) {
        super.threadStopped(thread);
        
        // remove gbean fro the thread
        GeronimoManagedThread managedThread = (GeronimoManagedThread)thread;
        this.factoryGBean.removeThreadGBean(managedThread);
    }
    
    public String[] getThreads() {
        return getThreads(getThreadList());
    }
    
    public String[] getHungTaskThreads() {
        return getHungTaskThreads(getThreadList());
    }
    
    public static String[] getThreads(List<ManagedThread> threadList) {
        int size = threadList.size();
        String [] threadNames = new String[size];
        for (int i = 0; i < size; i++) {
            GeronimoManagedThread thread = (GeronimoManagedThread)threadList.get(i);
            threadNames[i] = thread.getGbean().getObjectName();
        }
        return threadNames;        
    }
    
    public static String[] getHungTaskThreads(List<ManagedThread> threadList) {
        int size = threadList.size();
        List<String> hungTaskThreads = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            GeronimoManagedThread thread = (GeronimoManagedThread)threadList.get(i);
            if (thread.isTaskHung()) {
                hungTaskThreads.add(thread.getGbean().getObjectName());
            }
        }
        return hungTaskThreads.toArray(new String[hungTaskThreads.size()]);
    }
    
    private synchronized void startHungTaskMonitor() {
        if (this.hungTaskMonitor == null) {
            this.hungTaskMonitor = new HungTaskMonitor();
            this.hungTaskMonitor.setFrequency(this.hungTaskMonitorFrequency);
            Thread thread = new Thread(this.threadGroup, 
                                       this.hungTaskMonitor, 
                                       "UpdateTaskStateThread");
            thread.start();
        }
    }
    
    private class HungTaskMonitor implements Runnable {
        private long frequency = 1000 * 60; // 1 minute default
        private boolean done = false;
        
        public void setFrequency(long newFrequency) {
            frequency = newFrequency;
        }        
        
        public long getFrequency() {
            return frequency;
        }
        
        public void stop() {
            done = true;
        }

        public void run() {
            while (!done) {
                try {
                    Thread.sleep(frequency);
                } catch (InterruptedException e) {
                    break;
                }
                updateStatus();
            }
        }
    }
    
}
