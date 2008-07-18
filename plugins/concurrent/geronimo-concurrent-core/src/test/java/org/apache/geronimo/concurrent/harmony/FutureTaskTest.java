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
package org.apache.geronimo.concurrent.harmony;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.SkippedException;

import junit.framework.TestCase;

public class FutureTaskTest extends TestCase {
        
    private static int TIMEOUT = 60 * 2;
    
    private ExecutorService executor;
    
    public void setUp() {  
        this.executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, 
                                               new ArrayBlockingQueue<Runnable>(10));        
    }
    
    public void tearDown() {
        this.executor.shutdown();
    }
    
    public void testCancel() throws Exception {
        TestTask task = new TestTask(-1);
        final TestFutureTask future = new TestFutureTask(task, null);
        TestTaskThread thread = new TestTaskThread(future);
        
        this.executor.execute(thread);               
        this.executor.execute(new Runnable() {
           public void run() {
               try { Thread.sleep(1000 * 10); } catch (Exception e) {}
               future.cancel(true);
           }
        });
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (CancellationException e) {
            // that's what we expect
        }    
        
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertFalse(future.isSkipped());
    }   
    
    public void testException() throws Exception {
        TestTask task = new TestTask(3);
        TestFutureTask future = new TestFutureTask(task, null);
        TestTaskThread thread = new TestTaskThread(future);
        this.executor.execute(thread);
        
        try {
            future.get();
            fail("Did not throw exception");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NullPointerException) {
                // that's what we expect
            } else {
                fail("Unexpected root exception " +  e.getMessage());
            }
        }
        
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isSkipped());
    }
    
    public void testSkippAndContinue() throws Exception {
        TestTask task = new TestTask(6);
        TestFutureTask future = new TestFutureTask(task, null);
        TestTaskThreadSkip thread = new TestTaskThreadSkip(future);
        this.executor.execute(thread);
        
        try {
            future.get();
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        // try it one more time for fun
        Thread.sleep(1000 * 2);
        
        try {
            future.get();
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.isSkipped());
        
        // let the task run again
        thread.getLatch().countDown();
        Thread.sleep(1000 * 5);
        
        // should be running again
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isSkipped());
        
        try {
            future.get();
            fail("Did not throw exception");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NullPointerException) {
                // that's what we expect
            } else {
                fail("Unexpected root exception " +  e.getMessage());
            }
        }
        
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isSkipped());
    }
    
    public void testSkippAndCancel() throws Exception {
        TestTask task = new TestTask(-1);
        final TestFutureTask future = new TestFutureTask(task, null);
        TestTaskThreadSkip thread = new TestTaskThreadSkip(future);
        this.executor.execute(thread);
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        // try it one more time for fun
        Thread.sleep(1000 * 2);
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.isSkipped());
        
        // let the task run again
        thread.getLatch().countDown();
        Thread.sleep(1000 * 5);
        
        // should be running again
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isSkipped());
        
        // issue cancel
        this.executor.execute(new Runnable() {
            public void run() {
                try { Thread.sleep(1000 * 10); } catch (Exception e) {}
                future.cancel(true);
            }
         });
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (CancellationException e) {
            // that's what we expect
        }
        
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertFalse(future.isSkipped());
    }
    
    public void testCancelAtSkip() throws Exception {
        TestTask task = new TestTask(-1);
        final TestFutureTask future = new TestFutureTask(task, null);
        TestTaskThreadSkip thread = new TestTaskThreadSkip(future);
        this.executor.execute(thread);
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        // try it one more time for fun
        Thread.sleep(1000 * 2);
        
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.isSkipped());
        
        // cancel the task
        future.cancel(true);
        
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertFalse(future.isSkipped());
        
        // let the task run again
        thread.getLatch().countDown();
                
        try {
            future.get(TIMEOUT, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (CancellationException e) {
            // that's what we expect
        }
    }
    
    public void testCancelSkip() throws Exception {
        TestTask task = new TestTask(-1);
        final TestFutureTask future = new TestFutureTask(task, null);
       
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isSkipped());
        
        // set skipped state, should return true each time
        assertTrue(future.setSkipped());
        assertTrue(future.setSkipped());
        
        // cancel
        future.cancel(true);
        
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertFalse(future.isSkipped());
        
        // set skipped should return false, since future was cancelled
        assertFalse(future.setSkipped());
        
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertFalse(future.isSkipped());
    }
    
    private static class TestTask implements Runnable {
       
        private int count;
        private int maxCount;
        public TestTask(int maxCount) {
            this.maxCount = maxCount;
        }
        public void run() {
            count++;
            System.out.println("running: " + count);
            if (count == maxCount) {
                throw new NullPointerException("maxCount reached");
            }
        }
    }
    
    private static class TestFutureTask<V> extends FutureTask<V> {
        public TestFutureTask(Runnable runnable, V result) {
            super(runnable, result);
        }
        public boolean runAndReset() {
            return super.runAndReset();
        }
        public boolean setSkipped() {
            return super.setSkipped();
        }
    }
    
    
    private static class TestTaskThread implements Runnable {
        private TestFutureTask task;
        
        public TestTaskThread(TestFutureTask task) {
            this.task = task;
        }
        
        public void run() {
            try {
                while(task.runAndReset()) {
                    Thread.sleep(1000 * 5);                                      
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class TestTaskThreadSkip implements Runnable {
        private TestFutureTask task;
        private CountDownLatch latch = new CountDownLatch(1);
        
        public TestTaskThreadSkip(TestFutureTask task) {
            this.task = task;
        }
        
        public CountDownLatch getLatch() {
            return this.latch;
        }
        
        public void run() {
            try {
                for (int i = 0; i < 3; i++) {
                    task.runAndReset();
                    Thread.sleep(1000 * 5); 
                }
                System.out.println("set skipped state");
                System.out.println(task.setSkipped());
                System.out.println(task.setSkipped());
                
                latch.await();
                
                while(task.runAndReset()) {
                    Thread.sleep(1000 * 5);                                      
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
