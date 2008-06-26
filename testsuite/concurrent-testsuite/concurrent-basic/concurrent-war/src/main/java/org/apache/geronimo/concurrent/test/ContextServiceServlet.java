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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.util.concurrent.ContextService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

public class ContextServiceServlet extends HttpServlet {

    private final static Log LOG = LogFactory.getLog(ContextServiceServlet.class);
    
    @Resource
    ContextService contextService;
    
    @Resource(name="ConcurrentPool")
    DataSource db;
        
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) 
        throws IOException {
        
        System.out.println(contextService);
                
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
        
    public void testTransactionDemarcation() throws Exception {
        InitialContext ctx = new InitialContext();
        UserTransaction userTransaction = 
            (UserTransaction)ctx.lookup("java:comp/UserTransaction");
        userTransaction.begin();
        Assert.assertEquals(Status.STATUS_ACTIVE, userTransaction.getStatus());
        
        // run the task on the same thread
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BasicTaskRunnable runnable = new BasicTaskRunnable(classLoader);
        runnable.setTestTransaction(true);
        
        Runnable task = (Runnable)contextService.createContextObject(runnable, new Class [] {Runnable.class});
        task.run();
        
        Assert.assertEquals(Status.STATUS_ACTIVE, userTransaction.getStatus());
        userTransaction.commit();
    }
    
    public void testUseParentTransaction() throws Exception {
        InitialContext ctx = new InitialContext();
        UserTransaction userTransaction = 
            (UserTransaction)ctx.lookup("java:comp/UserTransaction");
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, userTransaction.getStatus());
        
        // run the task on the same thread
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BasicTaskRunnable runnable = new BasicTaskRunnable(classLoader);
        runnable.setTestTransaction(true);
        
        Map<String, String> props = new HashMap<String, String>();
        props.put(ContextService.USE_PARENT_TRANSACTION, "true");
        Runnable task = (Runnable)contextService.createContextObject(runnable, new Class [] {Runnable.class}, props);
        task.run();
        
        Assert.assertEquals(Status.STATUS_ACTIVE, userTransaction.getStatus());
        userTransaction.commit();
    }
    
    public void testContextService() throws Exception {
        
        TestRunnable testRunnable = new TestRunnable(2 * 1000);
        Runnable r = (Runnable)contextService.createContextObject(testRunnable, new Class [] {Runnable.class});
        
        Thread t = new Thread(new LoopRunnable(r));
        t.start();
        
        Thread.sleep(5 * 1000);
    }
    
    private static class LoopRunnable implements Runnable {
        Runnable target;
        
        public LoopRunnable(Runnable target) {
            this.target = target;
        }
        
        public void run() {
            try {
                while(true) {
                    Thread.sleep(2 * 1000);
                    target.run();
                }
            } catch (Exception e) {                
                e.printStackTrace();
            }
        }
        
    }
    
}
