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

import javax.naming.InitialContext;

import org.apache.geronimo.concurrent.test.thread.ManagedThreadFactoryRemote;
import org.testng.annotations.Test;

public class ManagedThreadFactoryTest extends EJBTestSupport {
    
    protected String getRemoteBeanName() {
        return "/ManagedThreadFactoryBeanRemote";        
    }
    
    private ManagedThreadFactoryRemote getBean() throws Exception {
        return getBean(null, null);
    }
    
    protected ManagedThreadFactoryRemote getBean(String username, String password) throws Exception {        
        InitialContext ctx = getInitialContext(username, password);       
        String beanName = getRemoteBeanName();
        System.out.println("Using: " + beanName);
        ManagedThreadFactoryRemote bean = (ManagedThreadFactoryRemote)ctx.lookup(beanName);
        return bean;
    }
    
    @Test
    public void testBasicContextMigration() throws Exception {
        ManagedThreadFactoryRemote bean = getBean();
        bean.testBasicContextMigration();
    }
    
    @Test
    public void testSecurityContextMigration() throws Exception {
        ManagedThreadFactoryRemote bean = getBean("system", "manager");
        bean.testSecurityContextMigration();
    }
    
}
