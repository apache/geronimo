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
package org.apache.geronimo.concurrent.test.thread;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.test.BasicTaskRunnable;
import org.apache.geronimo.concurrent.test.RunnableWrapper;

@Stateless(name="ManagedThreadFactoryBean")
public class ManagedThreadFactoryBean 
    implements ManagedThreadFactoryLocal, 
               ManagedThreadFactoryRemote {

    @Resource
    ManagedThreadFactory threadFactory;
            
    @Resource(name="ConcurrentPool")
    DataSource db;
    
    public void testBasicContextMigration() throws Exception {
        testContextMigration(false);
    }
    
    public void testSecurityContextMigration() throws Exception {
        testContextMigration(true);
    }
    
    public void testContextMigration(boolean testEjbSecurity) throws Exception {
        // since ee context is captured on jndi lookup for ManagedThreadFactory
        // lookup ThreadFactory here to get the right security context
        ManagedThreadFactory threadFactory = getThreadFactory();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BasicTaskRunnable runnable = new BasicTaskRunnable(classLoader);
        runnable.setTestEjbSecurity(testEjbSecurity);
        RunnableWrapper wrapper = new RunnableWrapper(runnable); 
        Thread t = threadFactory.newThread(wrapper);
        t.start();
        t.join();
        
        Throwable throwable = wrapper.getThrowable();
        if (throwable instanceof Exception) {
            throw (Exception)throwable;
        } else if (throwable instanceof Error) {
            throw (Error)throwable;
        }
    }
    
    private ManagedThreadFactory getThreadFactory() throws NamingException {
        InitialContext ctx = new InitialContext();
        ManagedThreadFactory threadFactory = 
            (ManagedThreadFactory)ctx.lookup("java:comp/env/" + getClass().getName() + "/threadFactory");
        return threadFactory;
    }
}
