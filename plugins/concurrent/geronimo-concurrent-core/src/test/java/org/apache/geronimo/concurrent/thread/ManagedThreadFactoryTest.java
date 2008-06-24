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
package org.apache.geronimo.concurrent.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.util.concurrent.Identifiable;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.TestContextHandler;
import org.apache.geronimo.concurrent.thread.ManagedThread;
import org.apache.geronimo.concurrent.thread.ManagedThreadFactoryUtils;

import junit.framework.TestCase;

public class ManagedThreadFactoryTest extends TestCase {
    
    private ManagedThreadFactory getManagedThreadFactory() {
        ManagedThreadFactory factory = new BasicManagedThreadFactory();
        ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
        chain.addManagedContextHandler(new TestContextHandler());
        return ManagedThreadFactoryUtils.createStandaloneThreadFactory(factory, chain);    
    }
              
    public void testCancel() throws Exception {
        BasicTask task = new BasicTask();
        
        ManagedThreadFactory threadFactory = getManagedThreadFactory();
        
        ManagedThread thread = 
            (ManagedThread)threadFactory.newThread(task);
            
        thread.setHungTaskThreshold(1000 * 5);
        
        assertFalse(thread.isTaskHung());
        
        thread.start();
        
        Thread.sleep(1000 * 10);
        thread.updateState();
        
        assertTrue(thread.isTaskHung());
        
        assertTrue(thread.cancelTask());
        
        thread.join();
                
        assertTrue(task.getException() != null);
        assertFalse(thread.isTaskHung());
    }
    
    public void testRunnableProperties() throws Exception {
        BasicTask task = new BasicTask();
        testExpectedProperties(task, 
                               task.toString(), 
                               task.toString());
    }
    
    public void testIdentifiableProperties() throws Exception {
        BasicIdentifiableTask task = new BasicIdentifiableTask();
        testExpectedProperties(task, 
                               task.getIdentityName(), 
                               task.getIdentityDescription(Locale.getDefault()));
    }
    
    private void testExpectedProperties(Runnable task, String expectedName, String expectedDescription) throws Exception {
        ManagedThreadFactory threadFactory = getManagedThreadFactory();
        
        ManagedThread thread = 
            (ManagedThread)threadFactory.newThread(task);
        
        assertNull(thread.getTaskIdentityDescription());
        assertNull(thread.getTaskIdentityDescription(Locale.getDefault().toString()));
        assertNull(thread.getTaskIdentityName());
        assertEquals(0, thread.getTaskRunTime());
        assertEquals(thread.getId(), thread.getThreadID());
        assertEquals(thread.getName(), thread.getThreadName());
        assertFalse(thread.isTaskHung());
        
        thread.start();
        
        Thread.sleep(1000 * 10);
                
        assertEquals(expectedDescription, thread.getTaskIdentityDescription());
        assertEquals(expectedDescription, thread.getTaskIdentityDescription(Locale.getDefault().toString()));
        assertEquals(expectedName, thread.getTaskIdentityName());
        assertTrue(thread.getTaskRunTime() > 0);
        assertEquals(thread.getId(), thread.getThreadID());
        assertEquals(thread.getName(), thread.getThreadName()); 
        assertFalse(thread.isTaskHung());
        
        thread.join();
        
        assertNull(thread.getTaskIdentityDescription());
        assertNull(thread.getTaskIdentityDescription(Locale.getDefault().toString()));
        assertNull(thread.getTaskIdentityName());
        assertEquals(0, thread.getTaskRunTime());
        assertEquals(thread.getId(), thread.getThreadID());
        assertEquals(thread.getName(), thread.getThreadName()); 
        assertFalse(thread.isTaskHung());
    }
    
    /*
     * Follows the example in the spec.
     */
    public void testThreadFactory() throws Exception {        
        MyTask task = new MyTask();   
        
        String expected = "hello123ThreadFactory";
        TestContextHandler.setCurrentObject(expected);
        
        ManagedThreadFactory threadFactory = getManagedThreadFactory();
                        
        assertEquals(expected, TestContextHandler.getCurrentObject());
        TestContextHandler.setCurrentObject(null);
        assertNull(TestContextHandler.getCurrentObject());
        
        Thread thread = threadFactory.newThread(task);        
        thread.start();
        thread.join();
        
        assertEquals(Arrays.asList(expected), task.getList());        
    }
       
    private static class MyTask implements Runnable {
        
        private List list = new ArrayList();
               
        public List getList() {
            return this.list;
        }
                      
        public void run() {
            list.add(TestContextHandler.getCurrentObject());
        }
        
    }
    
    private static class BasicTask implements Runnable {
        private Exception exception;
        public void run() {
            try {
                Thread.sleep(1000 * 20);
            } catch (Exception e) {
                exception = e;
            }
        }
        public Exception getException() {
            return exception;
        }
    }
    
    private static class BasicIdentifiableTask extends BasicTask implements Identifiable {

        public String getIdentityDescription(Locale arg0) {
            return "BasicIdentifiableTask Description";
        }

        public String getIdentityName() {
            return "BasicIdentifiableTask";
        }

    }
            
}
