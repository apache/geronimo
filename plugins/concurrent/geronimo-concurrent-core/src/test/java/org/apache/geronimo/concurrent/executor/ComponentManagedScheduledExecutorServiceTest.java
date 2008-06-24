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

import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.ManagedContextHandlerChain;
import org.apache.geronimo.concurrent.TestContextHandler;
import org.apache.geronimo.concurrent.thread.BasicManagedThreadFactory;

public class ComponentManagedScheduledExecutorServiceTest extends BasicManagedScheduledExecutorServiceTest {
           
    public void setUp() throws Exception {
        expected = setRandomContextData();
        scheduledExecutor = new TestManagedScheduledExecutorService();
        service = scheduledExecutor;
        
        Object unexpected = setRandomContextData();
        assertTrue(expected != unexpected);
    }
      
    public void setTestRandomContextData() {
        setRandomContextData();
    }
    
    private static class TestManagedScheduledExecutorService extends ComponentManagedScheduledExecutorService {
        public TestManagedScheduledExecutorService() {
            super(50,
                  getManagedThreadFactory(),
                  getManagedContextHandler());
        }
        private static ManagedThreadFactory getManagedThreadFactory() {
            ManagedThreadFactory factory = new BasicManagedThreadFactory();
            return factory;
        }
        private static ManagedContextHandler getManagedContextHandler() {
            ManagedContextHandlerChain chain = new ManagedContextHandlerChain();
            chain.addManagedContextHandler(new TestContextHandler());
            return chain;
        }
    }
    
}
