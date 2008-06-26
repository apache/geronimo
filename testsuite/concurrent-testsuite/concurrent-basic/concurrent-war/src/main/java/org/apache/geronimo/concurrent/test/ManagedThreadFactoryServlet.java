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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedThreadFactoryServlet extends HttpServlet {

    private final static Log LOG = LogFactory.getLog(ManagedThreadFactoryServlet.class);
    
    @Resource
    ManagedThreadFactory threadFactory;
    
    @Resource(name="ConcurrentPool")
    DataSource db;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) 
        throws IOException {
        
        System.out.println(threadFactory);
        
        String testName = (String)request.getParameter("testName");
        System.out.println(testName);
        
        if (!testName.startsWith("test")) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid test name");
        }
        
        try {
            Method method = getClass().getMethod(testName, new Class [] {});
            method.invoke(this, new Object [] {});
        } catch (NoSuchMethodException e) {
            LOG.error("No such test: " + testName);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No such test");
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Throwable ex = e.getTargetException();
            LOG.error("Test " + testName + " failed", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Test failed");
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
            
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
    
    public void testLongTask() throws Exception {
        Thread t3 = threadFactory.newThread(new TestRunnable("TestRunnable long", 1000 * 60 * 2));
        t3.start();
    }
    
    public void testStartRunnable() throws Exception {
        Thread t1 = threadFactory.newThread(new TestRunnable(1000 * 30));
        t1.start();
    }
    
    public void testStartIdentifiableRunnable() throws Exception {
        Thread t2 = threadFactory.newThread(new TestIdentifiableRunnable(1000 * 30));
        t2.start(); 
    }
    
    public void testModuleShutdown() throws Exception {
        Thread t3 = threadFactory.newThread(new ThreadCreator());
        t3.start();
    }
    
    private class ThreadCreator implements Runnable {
        public void run() {
            try {
                while(true) {
                    threadFactory.newThread(new TestRunnable(1000 * 5)).start();
                    System.out.println("created thread");
                    Thread.sleep(2 * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
