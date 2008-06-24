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

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.testng.annotations.Test;

public class ManagedExecutorServiceTest extends ConcurrentTest {
    
    public String getServletName() {
        return "ManagedExecutorServiceServlet";
    }
        
    @Test
    public void testLifecycleMethods() throws Exception {
        invokeTest("testServerManagedLifecycleMethods"); 
    }
    
    @Test
    public void testBasicContextMigration() throws Exception {
        invokeTest("testBasicContextMigration"); 
    }
    
    @Test
    public void testSecurityContextMigration() throws Exception {
        invokeSecureTest("testSecurityContextMigration"); 
    }
    
    @Test
    public void testThreadsField() throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        
        String name = "DefaultManagedExecutorService";
        
        Set<ObjectName> objectNameSet = 
            mbServerConn.queryNames(new ObjectName("*:j2eeType=ManagedExecutorService,name=" + name + ",*"), null);
        assertEquals(1, objectNameSet.size());
        
        ObjectName executorService = objectNameSet.iterator().next();
                
        invokeTest("testBasic");  
        
        ObjectName task1Thread = null;
        ObjectName task2Thread = null;
        
        long slept = 0;
        while (slept < TIMEOUT) {           
            String[] threadNames = (String[])mbServerConn.getAttribute(executorService, "threads");
            if (threadNames != null && threadNames.length > 0) {
                for (String threadName : threadNames) {
                    ObjectName threadObjectName = new ObjectName(threadName);
                    
                    String taskName = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityName");                   
                    String taskDesc = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityDescription");
                    
                    if ("TestRunnable".equals(taskName) && "TestRunnable".equals(taskDesc)) {
                        if (task1Thread == null) {
                            task1Thread = threadObjectName;
                        } else if (!task1Thread.equals(threadObjectName)) {
                            fail("Task1 was found on " + task1Thread + " and " + threadObjectName);
                        }
                    } else if ("TestIdentifiableRunnable Name".equals(taskName) && "TestIdentifiableRunnable Description".equals(taskDesc)) {
                        if (task2Thread == null) {
                            task2Thread = threadObjectName;
                        } else if (!task2Thread.equals(threadObjectName)) {
                            fail("Task2 was found on " + task2Thread + " and " + threadObjectName);
                        }
                    }
                }
                // found both, we're done
                if (task1Thread != null && task2Thread != null) {
                    break;
                }
            } 
            Thread.sleep(1000 * 10);
            slept += 1000 * 10;
        }
        
        assertTrue("task1 not found", task1Thread != null);
        assertTrue("task2 not found", task2Thread != null);
        
        System.out.println("Task1 found on " + task1Thread);
        System.out.println("Task2 found on " + task2Thread);
        
        assertTrue("task1 finished", pollThread(task1Thread));
        assertTrue("task2 finished", pollThread(task2Thread));        
    }
    
    private boolean pollThread(ObjectName threadObjectName) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        long slept = 0;
        while (slept < TIMEOUT) {
            String taskName = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityName");                   
            String taskDesc = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityDescription");
            System.out.println(taskName + " " + taskDesc);
            if (taskName == null && taskDesc == null) {
                return true;
            }
            Thread.sleep(1000 * 10);
            slept += 1000 * 10;
        }       
        return false;
    }
      
}