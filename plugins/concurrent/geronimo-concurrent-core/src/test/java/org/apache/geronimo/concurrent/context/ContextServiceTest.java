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
package org.apache.geronimo.concurrent.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ContextService;

import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.TestContextHandler;
import org.apache.geronimo.concurrent.context.BasicContextService;

import junit.framework.TestCase;

public class ContextServiceTest extends TestCase {
    
    private ContextService getContextService() {
        return new TestContextService();    
    }
    
    public void testProxing() throws Exception {
        ContextService service = getContextService();
        
        MyTask task1 = new MyTask();   
                
        Object expected = "foo123bar";
        TestContextHandler.setCurrentObject(expected);
        
        Runnable task2 = (Runnable)service.createContextObject(task1, new Class[] {Runnable.class, Callable.class});
        
        assertEquals(expected, TestContextHandler.getCurrentObject());
        TestContextHandler.setCurrentObject(null);
        assertNull(TestContextHandler.getCurrentObject());
                
        task2.run();
        task2.equals(task2);
        task2.hashCode();
        task2.toString();
        ((Callable)task2).call();
        
        assertEquals(Arrays.asList(expected, null, null, null, expected), task1.getList());
    }
       
    public void testBadProxyInterfaces() throws Exception {
        ContextService service = getContextService();
        
        MyTask task1 = new MyTask();
        
        try {
            service.createContextObject(task1, null);
            fail("Did not throw exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        try {
            service.createContextObject(task1, new Class[] {} );
            fail("Did not throw exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        try {
            service.createContextObject(task1, new Class[] {null} );
            fail("Did not throw exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        try {
            service.createContextObject(task1, new Class[] {Serializable.class, null} );
            fail("Did not throw exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        try {
            service.createContextObject(task1, new Class[] {Runnable.class, null} );
            fail("Did not throw exception");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
      
    /*
     * Follows the example in the spec.
     */
    public void testWithExecutorService() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = 
            new ThreadPoolExecutor(5, 10, 5, TimeUnit.SECONDS,
                                   new ArrayBlockingQueue<Runnable>(10));
        
        ExecutorCompletionService threadPool = 
            new ExecutorCompletionService(threadPoolExecutor);
        
        ContextService service = getContextService();
                
        MyTask task1 = new MyTask();   
        
        String expected = "hello123World";
        TestContextHandler.setCurrentObject(expected);
        
        Callable task2 = (Callable)service.createContextObject(task1, new Class[] {Callable.class});
        
        assertEquals(expected, TestContextHandler.getCurrentObject());
        TestContextHandler.setCurrentObject(null);
        assertNull(TestContextHandler.getCurrentObject());
        
        threadPool.submit(task2);
        
        assertEquals(expected, threadPool.take().get());
        
        threadPoolExecutor.shutdown();
    }
      
    public void testProxyingProperties1() throws Exception {
        ContextService service = getContextService();
                
        MyTask task1 = new MyTask();   
        
        Object expected = "foo123bar";
        TestContextHandler.setCurrentObject(expected);
        
        Runnable task2 = (Runnable)service.createContextObject(task1, new Class[] {Runnable.class});
        
        assertEquals(expected, TestContextHandler.getCurrentObject());
        TestContextHandler.setCurrentObject(null);
        assertNull(TestContextHandler.getCurrentObject());
                
        task2.run();
        
        assertEquals(Arrays.asList(expected), task1.getList());
         
        Map<String, String> props = null;
        
        props = new HashMap<String, String>(); 
        props.put(TestContextHandler.SKIP, "true");
        
        service.setProperties(task2, props);
        
        task1.getList().clear();
        task2.run();
        
        assertEquals(Arrays.asList("skipped"), task1.getList()); 
        
        props = new HashMap<String, String>(); 
        props.put(TestContextHandler.SKIP, "false");
        
        service.setProperties(task2, props);
        
        task1.getList().clear();
        task2.run();
        
        assertEquals(Arrays.asList(expected), task1.getList()); 
    }
    
    public void testProxyingProperties2() throws Exception {
        ContextService service = getContextService();
                
        Map<String, String> props = null;
                        
        MyTask task1 = new MyTask();   
        
        Object expected = "foo123bar";
        TestContextHandler.setCurrentObject(expected);
        
        props = new HashMap<String, String>(); 
        props.put(TestContextHandler.SKIP, "true");
        
        Runnable task2 = (Runnable)service.createContextObject(task1, new Class[] {Runnable.class}, props);
        
        props = new HashMap<String, String>(); 
        props.put(TestContextHandler.SKIP, "false");
        
        Runnable task3 = (Runnable)service.createContextObject(task1, new Class[] {Runnable.class}, props);
        
        assertEquals(expected, TestContextHandler.getCurrentObject());
        TestContextHandler.setCurrentObject(null);
        assertNull(TestContextHandler.getCurrentObject());
           
        task1.getList().clear();
        task2.run();
        assertEquals(Arrays.asList("skipped"), task1.getList());
        
        task1.getList().clear();
        task3.run();
        assertEquals(Arrays.asList(expected), task1.getList());        
    }
    
    public void testGetSetProperties() throws Exception {
        ContextService service = getContextService();
        
        Map<String, String> props1 = new HashMap<String, String>();
        props1.put("foo", "bar");
        
        MyTask task1 = new MyTask();                  
        Runnable task2 = (Runnable)service.createContextObject(task1, new Class[] {Runnable.class}, props1);
        
        assertEquals(props1, service.getProperties(task2));
        
        Map<String, String> props2 = new HashMap<String, String>();       
        props2.put("hello", "world");
        
        service.setProperties(task2, props2);
        
        assertEquals(props2, service.getProperties(task2));   
    }
    
    public void testBadGetSetProperties() throws Exception {
        ContextService service = getContextService();
        
        MyTask task1 = new MyTask();   
        
        // get Properties on non-proxied object
        try {
            service.getProperties(task1);
            fail("did not throw exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }
        
        // set Properties on non-proxied object
        try {
            service.setProperties(task1, new HashMap<String, String>());
            fail("did not throw exception");
        } catch (IllegalArgumentException e) {
            // ignore
        }        
    }
    
    private static class TestContextService extends BasicContextService {  
        public TestContextService() {
            super(createManagedContextHandler());
        }
        private static ManagedContextHandler createManagedContextHandler() {
            ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
            chain.addManagedContextHandler(new TestContextHandler());
            return chain;
        }
    }
    
    private static class MyTask implements Runnable, Callable {
        
        private List list = new ArrayList();
               
        public List getList() {
            return this.list;
        }
        
        public boolean equals(Object other) {
            list.add(TestContextHandler.getCurrentObject());
            return super.equals(other);
        }
        
        public String toString() {
            list.add(TestContextHandler.getCurrentObject());
            return "MyTask" + super.hashCode();            
        }
        
        public int hashCode() {
            list.add(TestContextHandler.getCurrentObject());
            return super.hashCode();            
        }
              
        public void run() {
            list.add(TestContextHandler.getCurrentObject());
        }

        public Object call() throws Exception {           
            list.add(TestContextHandler.getCurrentObject());
            return TestContextHandler.getCurrentObject();
        }
        
    }
            
}
