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

public class ManagedScheduledExecutorServiceTest extends ConcurrentTest {
    
    public String getServletName() {
        return "ManagedScheduledExecutorServiceServlet";
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
        
        String name = "DefaultManagedScheduledExecutorService";
        
        Set<ObjectName> objectNameSet = 
            mbServerConn.queryNames(new ObjectName("*:j2eeType=ManagedExecutorService,name=" + name + ",*"), null);
        assertEquals(1, objectNameSet.size());
        
        ObjectName executorService = objectNameSet.iterator().next();
                
        invokeTest("testBasic");                         
    }
          
}