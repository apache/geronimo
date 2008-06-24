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
package org.apache.geronimo.concurrent.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedExecutorService;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.geronimo.concurrent.TestCallable;
import org.apache.geronimo.concurrent.TestContextHandler;
import org.apache.geronimo.concurrent.TestRunnable;

public abstract class BasicManagedExecutorServiceTest extends TestCase {
    
    public static final int TIMEOUT = 5 * 1000;
    
    public static final String NOT_EXPECTED = "unexpected data";
    
    protected ManagedExecutorService service;
    protected Object expected;
        
    private static int counter = 0;
    
    protected static Object setRandomContextData() {
        Object data = "random data " + counter++;
        TestContextHandler.setCurrentObject(data);
        return data;
    }
    
    public void tearDown() throws Exception {
        service.shutdown();
    }
    
    public void testExecute() throws Exception {       
        TestRunnable task = new TestRunnable();

        service.execute(task);
        
        Thread.sleep(1000 * 5);
        
        assertEquals(Arrays.asList(expected), task.getList());                 
    }
    
    public void testSubmit() throws Exception {
        // test1
        TestRunnable task1 = new TestRunnable();

        Future f1 = service.submit(task1);
        
        assertNull(f1.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task1.getList());
        
        // test2
        TestRunnable task2 = new TestRunnable();
        Integer value = new Integer(5);
        
        Future f2 = service.submit(task2, value);
        
        assertEquals(value, f2.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task2.getList());    
        
        // test3        
        TestCallable task3 = new TestCallable();
        
        Future f3 = service.submit(task3);
        
        assertEquals(task3, f3.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task3.getList());        
    }
    
    public void testSubmitWithListener() throws Exception {
        // test1
        TestRunnable task1 = new TestRunnable();
        TestManagedTaskListener listener1 = new TestManagedTaskListener();

        Future f1 = service.submit(task1, listener1);
        
        assertNull(f1.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task1.getList());
        assertTrue("waiting for taskDone()", listener1.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener1.getCallbacks(f1));
        
        // test2
        TestRunnable task2 = new TestRunnable();
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        Integer value = new Integer(5);
        
        Future f2 = service.submit(task2, value, listener2);
        
        assertEquals(value, f2.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task2.getList());  
        assertTrue("waiting for taskDone()", listener2.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks2 = createCallbackInfo(f2);
        compareCallbacks(callbacks2, listener2.getCallbacks(f2));
        
        // test3        
        TestCallable task3 = new TestCallable();
        TestManagedTaskListener listener3 = new TestManagedTaskListener();
        
        Future f3 = service.submit(task3, listener3);
        
        assertEquals(task3, f3.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task3.getList());  
        assertTrue("waiting for taskDone()", listener3.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks3 = createCallbackInfo(f3);
        compareCallbacks(callbacks3, listener3.getCallbacks(f3));
    }
    
    public void testSubmitWithListenerFail() throws Exception {
        // test1
        TestRunnable task1 = new TestRunnable(true);
        TestManagedTaskListener listener1 = new TestManagedTaskListener();

        Future f1 = service.submit(task1, listener1);
        
        try {
            f1.get(5, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (ExecutionException e) {
            assertTrue("Unexpected exception " + e.getMessage(), 
                       e.getCause() instanceof IllegalStateException);
        }
        
        assertEquals(Arrays.asList(expected), task1.getList());        
        assertTrue("waiting for taskDone()", listener1.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener1.getCallbacks(f1));
                
        // test2
        TestRunnable task2 = new TestRunnable(true);
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        Integer value = new Integer(5);
        
        Future f2 = service.submit(task2, value, listener2);
        
        try {
            f2.get(5, TimeUnit.SECONDS);
            fail("Did not throw exception");
        } catch (ExecutionException e) {
            assertTrue("Unexpected exception " + e.getMessage(), 
                       e.getCause() instanceof IllegalStateException);
        }
        
        assertEquals(Arrays.asList(expected), task2.getList());  
        assertTrue("waiting for taskDone()", listener2.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks2 = createCallbackInfo(f2);
        compareCallbacks(callbacks2, listener2.getCallbacks(f2));
        
        // test3        
        TestCallable task3 = new TestCallable(0, true);
        TestManagedTaskListener listener3 = new TestManagedTaskListener();
        
        Future f3 = service.submit(task3, listener3);
        
        try {
            f3.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertTrue("Unexpected exception " + e.getMessage(), 
                       e.getCause() instanceof IllegalStateException);
        }
        
        assertEquals(Arrays.asList(expected), task3.getList());  
        assertTrue("waiting for taskDone()", listener3.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks3 = createCallbackInfo(f3);
        compareCallbacks(callbacks3, listener3.getCallbacks(f3));        
    }
    
    public void testInvokeAll() throws Exception {
        testInvokeAll(false);
    }
    
    public void testInvokeAllWithTimeout() throws Exception {
        testInvokeAll(true);
    }
    
    private void testInvokeAll(boolean withTimeout) throws Exception {
        TestCallable task1 = new TestCallable();
        TestCallable task2 = new TestCallable();
                       
        ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        tasks.add(task1);
        tasks.add(task2);
        
        List<Future<Object>> results = null;
        if (withTimeout) {
            results = service.invokeAll(tasks, 30, TimeUnit.SECONDS);
        } else {
            results = service.invokeAll(tasks);
        }

        assertEquals(task1, results.get(0).get(5, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(expected), task1.getList()); 
                
        assertEquals(task2, results.get(1).get(5, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(expected), task2.getList());            
    }
    
    public void testInvokeAllWithListener() throws Exception {
        testInvokeAllWithListener(false);
    }
    
    public void testInvokeAllWithListenerWithTimeout() throws Exception {
        testInvokeAllWithListener(true);
    }
    
    private void testInvokeAllWithListener(boolean withTimeout) throws Exception {
        TestCallable task1 = new TestCallable();
        TestCallable task2 = new TestCallable();
        TestManagedTaskListener listener = new TestManagedTaskListener();
                       
        ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        tasks.add(task1);
        tasks.add(task2);
        
        List<Future<Object>> results = null;
        if (withTimeout) {
            results = service.invokeAll(tasks, 30, TimeUnit.SECONDS, listener);
        } else {
            results = service.invokeAll(tasks, listener);
        }

        Future f1 = results.get(0);
        assertEquals(task1, f1.get(5, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(expected), task1.getList()); 
        
        Future f2 = results.get(1);
        assertEquals(task2, f2.get(5, TimeUnit.SECONDS));
        assertEquals(Arrays.asList(expected), task2.getList());
                
        assertTrue("waiting for taskDone()", listener.waitForDone(2, TIMEOUT));
        
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener.getCallbacks(f1));        
  
        List<TestManagedTaskListener.CallbackInfo> callbacks2 = createCallbackInfo(f2);
        compareCallbacks(callbacks2, listener.getCallbacks(f2));
    }
        
    public void testInvokeAny() throws Exception {
        testInvokeAny(false);
    }
    
    public void testInvokeAnyWithTimeout() throws Exception {
        testInvokeAny(true);
    }
    
    private void testInvokeAny(boolean withTimeout) throws Exception {
        TestCallable task1 = new TestCallable(1000 * 10);
        TestCallable task2 = new TestCallable(1000 * 5);
                       
        ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        tasks.add(task1);
        tasks.add(task2);
        
        Object result = null;
        if (withTimeout) {
            result = service.invokeAny(tasks, 30, TimeUnit.SECONDS);
        } else {
            result = service.invokeAny(tasks);
        }
                
        // task2 runs faster
        assertEquals(task2, result);
        assertEquals(Arrays.asList(expected), task2.getList());
        
        // task1 should get cancelled so list should be empty
        assertTrue(task1.getList().isEmpty());
    }
    
    public void testInvokeAnyWithListener() throws Exception {
        // XXX: Not sure how to test it since cannot quite connect listener callbacks
        // to the Future that represents the given task
    }
       
    public abstract void setTestRandomContextData();
        
    public void testSubmitWithMixedContext() throws Exception {
        // test1
        TestRunnable task1 = new TestRunnable();
        TestManagedTaskListener listener1 = new TestManagedTaskListener();

        setTestRandomContextData();        
        Future f1 = service.submit(task1, listener1);
        
        assertNull(f1.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task1.getList());
        assertTrue("waiting for taskDone()", listener1.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener1.getCallbacks(f1));
        
        // test2
        TestRunnable task2 = new TestRunnable();
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        Integer value = new Integer(5);
        
        setTestRandomContextData();
        Future f2 = service.submit(task2, value, listener2);
        
        assertEquals(value, f2.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task2.getList());  
        assertTrue("waiting for taskDone()", listener2.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks2 = createCallbackInfo(f2);
        compareCallbacks(callbacks2, listener2.getCallbacks(f2));
        
        // test3        
        TestCallable task3 = new TestCallable();
        TestManagedTaskListener listener3 = new TestManagedTaskListener();
        
        setTestRandomContextData();
        Future f3 = service.submit(task3, listener3);
        
        assertEquals(task3, f3.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task3.getList());  
        assertTrue("waiting for taskDone()", listener3.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks3 = createCallbackInfo(f3);
        compareCallbacks(callbacks3, listener3.getCallbacks(f3));
    }   
    
    
    public void testRejectSubmissionOnShutdown() throws Exception {
        service.shutdown();
        
        // test1
        TestRunnable task1 = new TestRunnable();
        TestManagedTaskListener listener1 = new TestManagedTaskListener();

        try {
            service.submit(task1, listener1);
            fail("Did not throw RejectedExecutionException");
        } catch (RejectedExecutionException e) {
            // that's what we expect
        }
               
        assertEquals(0, task1.getList().size());
        checkRejectedListener(listener1.getCallbacks());

        // test2
        TestRunnable task2 = new TestRunnable();
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        Integer value = new Integer(5);
        
        try {
            service.submit(task2, value, listener2);
        } catch (RejectedExecutionException e) {
            // that's what we expect
        }
               
        assertEquals(0, task2.getList().size());
        checkRejectedListener(listener2.getCallbacks());   
                
        // test3        
        TestCallable task3 = new TestCallable();
        TestManagedTaskListener listener3 = new TestManagedTaskListener();
        
        try {
            service.submit(task3, listener3);
        } catch (RejectedExecutionException e) {
            // that's what we expect
        }
               
        assertEquals(0, task3.getList().size());
        checkRejectedListener(listener3.getCallbacks());              
    }
    
    private void checkRejectedListener(List<TestManagedTaskListener.CallbackInfo> callbacks) throws Exception {
        assertEquals(2, callbacks.size());
        
        TestManagedTaskListener.CallbackInfo callback = null;
        
        callback = callbacks.get(0);
        assertEquals(TestManagedTaskListener.Callbacks.SUBMITTED, callback.getCallback());
        assertEquals(expected, callback.getData());
        assertEquals(service, callback.getManagedExecutorService());
        
        callback = callbacks.get(1);
        assertEquals(TestManagedTaskListener.Callbacks.DONE, callback.getCallback());
        assertEquals(expected, callback.getData());
        assertEquals(service, callback.getManagedExecutorService());
        assertTrue(callback.getException() instanceof RejectedExecutionException);
    }
    
    /**
     * Tests whether calling Future.get() from within ManagedTaskLister.taskDone() work 
     * as expected (i.e. does not cause a deadlock).
     */
    public void testFutureInListenerDone() throws Exception {
        TestCallable task = new TestCallable(2 * 1000);
        
        FutureDoneTastListener listener = new FutureDoneTastListener();
        
        Future f1 = service.submit(task, listener);
        
        assertEquals(task, f1.get());

        assertTrue("waiting for taskDone()", listener.waitForDone(TIMEOUT));
        assertEquals(Arrays.asList(expected), task.getList());       
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener.getCallbacks(f1));
                
        assertEquals(task, listener.getFutureResult());
        assertEquals(null, listener.getFutureException());               
    }
    
    /**
     * Tests whether calling Future.get() from within ManagedTaskLister.taskAbort() work 
     * as expected (i.e. does not cause a deadlock).
     */
    public void testFutureInListenerAbort() throws Exception {
        TestCallable task = new TestCallable(10 * 1000);
        
        FutureAbortTastListener listener = new FutureAbortTastListener();
        
        Future f1 = service.submit(task, listener);
        
        Thread.sleep(5 * 1000);
        f1.cancel(true);
        try {
            f1.get();
            fail("Did not throw exception");
        } catch (CancellationException e) {
            // expected
        }

        assertTrue("waiting for taskDone()", listener.waitForDone(TIMEOUT));
        assertTrue(task.getList().isEmpty()); // it should be empty as the task got cancelled       
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createAbortCallbackInfoV1(f1);
        try {
            compareCallbacks(callbacks1, listener.getCallbacks(f1));
        } catch (AssertionFailedError e) {
            callbacks1 = createAbortCallbackInfoV2(f1);
            compareCallbacks(callbacks1, listener.getCallbacks(f1));
        }
                
        assertEquals(null, listener.getFutureResult());       
        assertTrue(listener.getFutureException() instanceof  CancellationException);              
    }
    
    private static class FutureDoneTastListener extends TestManagedTaskListener {
        private Object result;
        private Throwable exception;
        
        public Object getFutureResult() {
            return this.result;
        }
        
        public Throwable getFutureException() {
            return this.exception;
        }
        
        @Override
        public void taskDone(Future<?> arg0, ManagedExecutorService arg1, Throwable arg2) {
            try {
                this.result = arg0.get();
            } catch (Throwable e) {
                this.exception = e;
            } 
            super.taskDone(arg0, arg1, arg2);         
        }
    }
    
    private static class FutureAbortTastListener extends TestManagedTaskListener {
        private Object result;
        private Throwable exception;
        
        public Object getFutureResult() {
            return this.result;
        }
        
        public Throwable getFutureException() {
            return this.exception;
        }
        
        @Override
        public void taskAborted(Future<?> arg0, ManagedExecutorService arg1, Throwable arg2) {
            try {
                this.result = arg0.get();
            } catch (Throwable e) {
                this.exception = e;
            }
            super.taskAborted(arg0, arg1, arg2);          
        }
    }
    
    
    // common functions    
    
    protected void checkListenerBasics(Future expectedFuture,
                                       List<TestManagedTaskListener.CallbackInfo> callbacks) throws Exception {
        for (int i = 0; i < callbacks.size(); i++) {
            TestManagedTaskListener.CallbackInfo callback = callbacks.get(i);
            
            assertEquals(expectedFuture, callback.getFuture());
            assertEquals(service, callback.getManagedExecutorService());
            assertEquals(expected, callback.getData());
        }
    }
    
    protected void checkData(List list) throws Exception {
        for (Object data : list) {
            assertEquals(expected, data);
        }
    }
    
    protected void compareCallbacks(List<TestManagedTaskListener.CallbackInfo> expectedCallbacks,
                                    List<TestManagedTaskListener.CallbackInfo> actualCallbacks) throws Exception {
        //System.out.println("Expected");
        //System.out.println(expectedCallbacks);
        //System.out.println("Actual");
        //System.out.println(actualCallbacks);
        
        assertEquals(expectedCallbacks.size(), actualCallbacks.size());
        for (int i = 0; i < expectedCallbacks.size(); i++) {
            TestManagedTaskListener.CallbackInfo expectedCallbackInfo = expectedCallbacks.get(i);
            TestManagedTaskListener.CallbackInfo actaulCallbackInfo = actualCallbacks.get(i);
          
            System.out.println("Expected : " + expectedCallbackInfo);
            System.out.println("Actual   : " + actaulCallbackInfo);
            
            assertEquals(expectedCallbackInfo.getCallback(), 
                         actaulCallbackInfo.getCallback());
            assertEquals(expectedCallbackInfo.getFuture(), 
                         actaulCallbackInfo.getFuture());
            assertEquals(expectedCallbackInfo.getManagedExecutorService(), 
                         actaulCallbackInfo.getManagedExecutorService());
            assertEquals(expectedCallbackInfo.getData(), 
                         actaulCallbackInfo.getData());
        }
    }
            
    protected List<TestManagedTaskListener.CallbackInfo> createCallbackInfo(Future future) {
        return createCallbackInfo(future, 1);
    }
    
    protected List<TestManagedTaskListener.CallbackInfo> createCallbackInfo(Future future, int times) {
        List<TestManagedTaskListener.CallbackInfo> callbacks = 
            new ArrayList<TestManagedTaskListener.CallbackInfo>();
        for (int i=0;i<times;i++) {
            addNormalCallbackInfo(future, callbacks);
        }
        return callbacks;
    }
    
    protected void addNormalCallbackInfo(Future future, List<TestManagedTaskListener.CallbackInfo> callbacks) {
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.SUBMITTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.STARTING, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.DONE, future, service, null, expected));
    }
    
    protected List<TestManagedTaskListener.CallbackInfo> createCancelCallbackInfo(Future future) {
        List<TestManagedTaskListener.CallbackInfo> callbacks = 
            new ArrayList<TestManagedTaskListener.CallbackInfo>();
        addCancelCallbackInfo(future, callbacks);
        return callbacks;
    }
    
    protected List<TestManagedTaskListener.CallbackInfo> createAbortCallbackInfoV1(Future future) {
        List<TestManagedTaskListener.CallbackInfo> callbacks = 
            new ArrayList<TestManagedTaskListener.CallbackInfo>();
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.SUBMITTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.STARTING, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.ABORTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.DONE, future, service, null, expected));
        return callbacks;
    }
    
    protected List<TestManagedTaskListener.CallbackInfo> createAbortCallbackInfoV2(Future future) {
        List<TestManagedTaskListener.CallbackInfo> callbacks = 
            new ArrayList<TestManagedTaskListener.CallbackInfo>();
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.SUBMITTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.STARTING, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.DONE, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.ABORTED, future, service, null, expected));
        return callbacks;
    }
    
    protected void addCancelCallbackInfo(Future future, List<TestManagedTaskListener.CallbackInfo> callbacks) {
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.SUBMITTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.ABORTED, future, service, new CancellationException(), expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.DONE, future, service, null, expected));
    }
    
    protected void addSkippedCallbackInfo(Future future, List<TestManagedTaskListener.CallbackInfo> callbacks) {       
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.SUBMITTED, future, service, null, expected));
        callbacks.add(new TestManagedTaskListener.CallbackInfo(TestManagedTaskListener.Callbacks.DONE, future, service, null, expected));
    }
}
