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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.SkippedException;
import javax.util.concurrent.Trigger;

import org.apache.geronimo.concurrent.TestCallable;
import org.apache.geronimo.concurrent.TestRunnable;

public abstract class BasicManagedScheduledExecutorServiceTest extends BasicManagedExecutorServiceTest {
    
    protected ManagedScheduledExecutorService scheduledExecutor;
    
    public void testSchedule() throws Exception {
        // test schedule Runnable
        TestRunnable task1 = new TestRunnable();

        Future f1 = scheduledExecutor.schedule(task1, 2, TimeUnit.SECONDS);
        
        assertNull(f1.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task1.getList());
                   
        // test schedule Callable
        TestCallable task2 = new TestCallable();
        
        Future f2 = scheduledExecutor.schedule(task2, 2, TimeUnit.SECONDS);
        
        assertEquals(task2, f2.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task2.getList());
    }
    
    public void testScheduleWithListener() throws Exception {
        // test schedule Runnable
        TestRunnable task1 = new TestRunnable();
        TestManagedTaskListener listener1 = new TestManagedTaskListener();

        Future f1 = scheduledExecutor.schedule(task1, 2, TimeUnit.SECONDS, listener1);
        
        assertNull(f1.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task1.getList());
        assertTrue("waiting for taskDone()", listener1.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks1 = createCallbackInfo(f1);
        compareCallbacks(callbacks1, listener1.getCallbacks(f1));
                   
        // test schedule Callable
        TestCallable task2 = new TestCallable();
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        
        Future f2 = scheduledExecutor.schedule(task2, 2, TimeUnit.SECONDS, listener2);
        
        assertEquals(task2, f2.get(5, TimeUnit.SECONDS));        
        assertEquals(Arrays.asList(expected), task2.getList());
        assertTrue("waiting for taskDone()", listener2.waitForDone(TIMEOUT));
        List<TestManagedTaskListener.CallbackInfo> callbacks2 = createCallbackInfo(f2);
        compareCallbacks(callbacks2, listener2.getCallbacks(f2));
    }
        
    public void testPeriodic() throws Exception {
        // test scheduleAtFixedRate
        TestRunnable task1 = new TestRunnable();
        assertTrue(task1.getList().size() == 0);
        
        Future f1 = scheduledExecutor.scheduleAtFixedRate(task1, 0, 2, TimeUnit.SECONDS);
        
        Thread.sleep(1000 * 5);
        f1.cancel(true);
        
        assertTrue(task1.getList().size() > 1);
        checkData(task1.getList());
        
        // test scheduleWithFixedDelay
        TestRunnable task2 = new TestRunnable();
        assertTrue(task2.getList().size() == 0);
        
        Future f2 = scheduledExecutor.scheduleWithFixedDelay(task2, 0, 2, TimeUnit.SECONDS);
        
        Thread.sleep(1000 * 5);
        f2.cancel(true);
        
        assertTrue(task2.getList().size() > 1);
        checkData(task2.getList());
    }
    
    public void testPeriodicWithListener() throws Exception {
        // test scheduleAtFixedRate
        TestRunnable task1 = new TestRunnable();
        assertTrue(task1.getList().size() == 0);
        TestManagedTaskListener listener1 = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.scheduleAtFixedRate(task1, 0, 2, TimeUnit.SECONDS, listener1);
        
        Thread.sleep(1000 * 5);
        f1.cancel(true);
        
        assertTrue(task1.getList().size() > 1);
        checkData(task1.getList());
        assertTrue(listener1.getCallbacks().size() >= 3);
        checkListenerBasics(f1, listener1.getCallbacks());
        
        // test scheduleWithFixedDelay
        TestRunnable task2 = new TestRunnable();
        assertTrue(task2.getList().size() == 0);
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        
        Future f2 = scheduledExecutor.scheduleWithFixedDelay(task2, 0, 2, TimeUnit.SECONDS, listener2);
        
        Thread.sleep(1000 * 5);
        f2.cancel(true);
        
        assertTrue(task2.getList().size() > 1);
        checkData(task2.getList());
        assertTrue(listener2.getCallbacks().size() >= 3);
        checkListenerBasics(f2, listener2.getCallbacks());
    }
    
    public void testScheduleWithTrigger() throws Exception {
        // test schedule Runnable
        TestRunnable task1 = new TestRunnable();
        TestTrigger trigger1 = new TestTrigger();
        TestManagedTaskListener listener1 = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.schedule(task1, trigger1, listener1);
        
        Thread.sleep(1000 * 8);
        f1.cancel(true);
               
        assertTrue(task1.getList().size() >= 1);
        checkData(task1.getList());
        assertTrue(trigger1.getCallbacks().size() >= 3);
        checkTriggerBasics(f1, trigger1.getCallbacks());
        assertTrue(listener1.getCallbacks().size() >= 3);
        checkListenerBasics(f1, listener1.getCallbacks());
                   
        // test schedule Callable
        TestCallable task2 = new TestCallable();
        TestTrigger trigger2 = new TestTrigger();
        TestManagedTaskListener listener2 = new TestManagedTaskListener();
        
        Future f2 = scheduledExecutor.schedule(task2, trigger2, listener2);
        
        Thread.sleep(1000 * 8);
        f2.cancel(true);
               
        assertTrue(task2.getList().size() >= 1);
        checkData(task2.getList());
        assertTrue(trigger2.getCallbacks().size() >= 3);
        checkTriggerBasics(f2, trigger2.getCallbacks());
        assertTrue(listener2.getCallbacks().size() >= 3);
        checkListenerBasics(f2, listener2.getCallbacks());
    }
       
    public void testCancelPeriodicBeforeRun() throws Exception {  
        TestRunnable task = new TestRunnable();
        TestManagedTaskListener listener = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.scheduleWithFixedDelay(task, 5, 5, TimeUnit.SECONDS, listener);
        
        Thread.sleep(1000 * 2);
        
        f1.cancel(true);
        
        assertTrue("waiting for taskDone()", listener.waitForDone(TIMEOUT));
                
        List<TestManagedTaskListener.CallbackInfo> callbacks = createCancelCallbackInfo(f1);
        compareCallbacks(callbacks, listener.getCallbacks());              
    }
    
    public void testCancelTriggerBeforeRun() throws Exception {  
        TestRunnable task = new TestRunnable();
        TestTrigger trigger = new TestTrigger();
        TestManagedTaskListener listener = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.schedule(task, trigger, listener);
        
        Thread.sleep(1000 * 2);
        
        f1.cancel(true);
        
        assertTrue("waiting for taskDone()", listener.waitForDone(TIMEOUT));
                
        List<TestManagedTaskListener.CallbackInfo> callbacks = createCancelCallbackInfo(f1);
        compareCallbacks(callbacks, listener.getCallbacks());
        
        List<TestTrigger.CallbackInfo> triggerCallbacks = trigger.getCallbacks();
        assertEquals(1, triggerCallbacks.size());
        TestTrigger.CallbackInfo triggerCallback = triggerCallbacks.get(0);
        assertEquals(TestTrigger.Callbacks.GET_NEXT_RUN_TIME, triggerCallback.getCallback());
        assertEquals(f1, triggerCallback.getFuture());
        assertEquals(expected, triggerCallback.getData());
    }
    
    public void testTriggerSkipAndCancel() throws Exception {  
        TestRunnable task = new TestRunnable();
        TestTrigger trigger = new TestTrigger(3, -1);
        TestManagedTaskListener listener = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.schedule(task, trigger, listener);
        
        try {
            f1.get(30, TimeUnit.SECONDS);
            fail("Did not throw SkippedException");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        assertTrue("waiting for taskDone()", listener.waitForDone(3, TIMEOUT));
        
        assertFalse(f1.isDone());
        assertFalse(f1.isCancelled());
        
        f1.cancel(true);
        
        try {
            f1.get(30, TimeUnit.SECONDS);
        } catch (CancellationException e) {
            // that's what we expect
        }   
        
        assertTrue(f1.isDone());
        assertTrue(f1.isCancelled());
                
        assertTrue("waiting for taskDone()", listener.waitForDone(2 * TIMEOUT));
        
        List<TestManagedTaskListener.CallbackInfo> callbacks = createCallbackInfo(f1, 2);
        addSkippedCallbackInfo(f1, callbacks);
        addCancelCallbackInfo(f1, callbacks);
        compareCallbacks(callbacks, listener.getCallbacks());
    }
    
    /* 
     * The behavior on Future.get() when getNextRunTime() returns null
     * is not quite defined yet.
     */
    /*
    public void testTriggerSkipAndDone() throws Exception {  
        TestRunnable task = new TestRunnable();
        TestTrigger trigger = new TestTrigger(3, 5);
        TestManagedTaskListener listener = new TestManagedTaskListener();
        
        Future f1 = scheduledExecutor.schedule(task, trigger, listener);
        
        try {
            f1.get(30, TimeUnit.SECONDS);
            fail("Did not throw SkippedException");
        } catch (SkippedException e) {
            // that's what we expect
        }
        
        assertFalse(f1.isDone());
        assertFalse(f1.isCancelled());
        
        Thread.sleep(1000 * 8);
        
        f1.get(30, TimeUnit.SECONDS);    
    }
    */
    
    public void testTriggerNull() throws Exception {        
        NullTrigger trigger = new NullTrigger();
        TestManagedTaskListener listener = new TestManagedTaskListener();
        
        TestRunnable task1 = new TestRunnable(true);
        try {
            Future f1 = scheduledExecutor.schedule(task1, trigger, listener);
            fail("Did not throw exception");
        } catch (RejectedExecutionException ex) {
            // expected
        }
        
        TestCallable task2 = new TestCallable(0, true);
        try {
            Future f2 = scheduledExecutor.schedule(task2, trigger, listener);
            fail("Did not throw exception");
        } catch (RejectedExecutionException ex) {
            // expected
        }
    }
    
    private static class NullTrigger implements Trigger {
        public Date getNextRunTime(Future<?> arg0, Date arg1, Date arg2, Date arg3, Date arg4) {
            return null;
        }
        public boolean skipRun(Future<?> arg0, Date arg1) {
            return false;
        }        
    }
    
    protected void checkTriggerBasics(Future expectedFuture,
                                      List<TestTrigger.CallbackInfo> callbacks) throws Exception {
        for (int i = 0; i < callbacks.size(); i++) {
            TestTrigger.CallbackInfo callback = callbacks.get(i);
            
            assertEquals(expectedFuture, callback.getFuture());
            assertEquals(expected, callback.getData());
        }
    }
    
}
