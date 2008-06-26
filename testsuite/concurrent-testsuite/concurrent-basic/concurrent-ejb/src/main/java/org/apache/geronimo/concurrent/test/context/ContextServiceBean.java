/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.concurrent.test.context;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import javax.util.concurrent.ContextService;

import org.apache.geronimo.concurrent.test.BasicTaskRunnable;
import org.apache.geronimo.concurrent.test.RunnableWrapper;

@Stateless(name="ContextServiceBean")
public class ContextServiceBean 
    implements ContextServiceLocal, 
               ContextServiceRemote {

    @Resource
    ContextService contextService;
            
    @Resource(name="ConcurrentPool")
    DataSource db;
    
    public void testBasicContextMigration() throws Exception {
        testContextMigration(false);
    }
    
    public void testSecurityContextMigration() throws Exception {
        testContextMigration(true);
    }
    
    private void testContextMigration(boolean testEjbSecurity) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BasicTaskRunnable runnable = new BasicTaskRunnable(classLoader);
        runnable.setTestEjbSecurity(testEjbSecurity);
        
        Runnable task = (Runnable)contextService.createContextObject(runnable, new Class [] {Runnable.class});
        RunnableWrapper wrapper = new RunnableWrapper(task);        
        Thread t = new Thread(wrapper);        
        t.start();
        t.join();
        
        Throwable throwable = wrapper.getThrowable();
        if (throwable instanceof Exception) {
            throw (Exception)throwable;
        } else if (throwable instanceof Error) {
            throw (Error)throwable;
        }
    }
        
}
