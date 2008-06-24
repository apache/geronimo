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
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.util.concurrent.ManagedExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedExecutorServiceServlet extends HttpServlet {

    private final static Log LOG = LogFactory.getLog(ManagedExecutorServiceServlet.class);
           
    @Resource
    ManagedExecutorService executorService;
        
    @Resource(name="ConcurrentPool")
    DataSource db;
    
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) 
        throws IOException {
        
        System.out.println(executorService);
        
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
       
    public void testBasic() throws Exception {            
        Future t1 = executorService.submit(new TestRunnable(1000 * 30));    
        Future t2 = executorService.submit(new TestIdentifiableRunnable(1000 * 60));
    }
    
    /*
     * Tests if lifecycle methods throws IllegalStateException on 
     * server-managed executor service.
     */
    public void testServerManagedLifecycleMethods() throws Exception {
        ManagedExecutorServiceTest test = new ManagedExecutorServiceTest(executorService);
        test.testServerManagedLifecycleMethods();
    }
      
    public void testBasicContextMigration() throws Exception {
        ManagedExecutorServiceTest test = new ManagedExecutorServiceTest(executorService);
        test.testContextMigration(false);
    }
    
    public void testSecurityContextMigration() throws Exception {
        ManagedExecutorServiceTest test = new ManagedExecutorServiceTest(executorService);
        test.testContextMigration(true);
    }
    
}
