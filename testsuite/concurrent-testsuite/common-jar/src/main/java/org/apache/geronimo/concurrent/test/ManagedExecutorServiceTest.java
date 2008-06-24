/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.concurrent.test;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedExecutorService;

import org.testng.Assert;

public class ManagedExecutorServiceTest {
    
    ManagedExecutorService executorService;
    
    public ManagedExecutorServiceTest(ManagedExecutorService executorService) {
        this.executorService = executorService;
    }
        
    public void testContextMigration(boolean testEjbSecurity) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        
        // test callable
        BasicTaskCallable callable = new BasicTaskCallable(classLoader);
        callable.setTestEjbSecurity(testEjbSecurity);
        Future futureCallable = executorService.submit(callable);
        Object callableResult = futureCallable.get(30, TimeUnit.SECONDS);        
        Assert.assertEquals(callableResult, Boolean.TRUE, "Callable");
        
        // test runnable
        BasicTaskRunnable runnable = new BasicTaskRunnable(classLoader);
        runnable.setTestEjbSecurity(testEjbSecurity);
        Future futureRunnable = executorService.submit(runnable);
        futureRunnable.get(30, TimeUnit.SECONDS);        
        Object runnableResult = runnable.getResult();
        Assert.assertEquals(runnableResult, Boolean.TRUE, "Runnable");        
    }
    
    /*
     * Tests if lifecycle methods throws IllegalStateException on 
     * server-managed executor service.
     */
    public void testServerManagedLifecycleMethods() throws Exception {
        try {
            executorService.shutdown();
            Assert.fail("shutdown() did not throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        try {
            executorService.shutdownNow();
            Assert.fail("shutdownNow() did not throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
            Assert.fail("awaitTermination() did not throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        try {
            executorService.isShutdown();
            Assert.fail("isShutdown() did not throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
        
        try {
            executorService.isTerminated();
            Assert.fail("isTerminated() did not throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
    }
      
}
