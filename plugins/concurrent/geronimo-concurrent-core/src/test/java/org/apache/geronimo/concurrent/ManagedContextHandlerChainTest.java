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
package org.apache.geronimo.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ManagedContextHandlerChainTest extends TestCase {
    
    public void testOk() throws Exception {
        ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
        
        List calls = new ArrayList();
        
        chain.addManagedContextHandler(new DummyContextHandler(1, calls));
        chain.addManagedContextHandler(new DummyContextHandler(2, calls));
        chain.addManagedContextHandler(new DummyContextHandler(3, calls));
                
        Map<String, Object> threadContext = new HashMap<String, Object>();
        chain.saveContext(threadContext);
        try {
            chain.setContext(threadContext);
        } finally {
            chain.unsetContext(threadContext);
        }
        
        assertEquals(Arrays.asList("a1", "a2", "a3", "b1", "b2", "b3", "c3", "c2", "c1"), calls);
    }
    
    public void testFailSet() throws Exception {
        ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
        
        List calls = new ArrayList();
        
        chain.addManagedContextHandler(new DummyContextHandler(1, calls));
        chain.addManagedContextHandler(new DummyContextHandler(2, calls, "b"));
        chain.addManagedContextHandler(new DummyContextHandler(3, calls));
                
        Map<String, Object> threadContext = new HashMap<String, Object>();
        chain.saveContext(threadContext);
        try {
            chain.setContext(threadContext);
        } catch (Exception e) {
            // ignore exception
        } finally {
            chain.unsetContext(threadContext);
        }
        
        assertEquals(Arrays.asList("a1", "a2", "a3", "b1", "b2", "c2", "c1"), calls);
    }
    
    public void testFailUnset() throws Exception {
        ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
        
        List calls = new ArrayList();
        
        chain.addManagedContextHandler(new DummyContextHandler(1, calls));
        chain.addManagedContextHandler(new DummyContextHandler(2, calls, "c"));
        chain.addManagedContextHandler(new DummyContextHandler(3, calls));
                
        Map<String, Object> threadContext = new HashMap<String, Object>();
        chain.saveContext(threadContext);
        try {
            chain.setContext(threadContext);
        } finally {           
            chain.unsetContext(threadContext);         
        }
        
        assertEquals(Arrays.asList("a1", "a2", "a3", "b1", "b2", "b3", "c3", "c2", "c1"), calls);
    }
            
    private static class DummyContextHandler implements ManagedContextHandler {

        List calls;
        int id;
        String fail;
        
        public DummyContextHandler(int id, List calls) {
            this(id, calls, null);
        }
        
        public DummyContextHandler(int id, List calls, String fail) {
            this.id = id;
            this.calls = calls;
            this.fail = fail + id;
        }
        
        public void saveContext(Map<String, Object> context) {
            String name = "a" + this.id;
            this.calls.add(name); 
            if (name.equals(this.fail)) {
                throw new NullPointerException(name);
            }
        }

        public void setContext(Map<String, Object> threadContext) {
            String name = "b" + this.id;
            this.calls.add(name);    
            if (name.equals(this.fail)) {
                throw new NullPointerException(name);
            }
        }

        public void unsetContext(Map<String, Object> threadContext) {
            String name = "c" + this.id;
            this.calls.add(name);    
            if (name.equals(this.fail)) {
                throw new NullPointerException(name);
            } 
        }
    
    }
}
