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
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.testng.Assert;

public class BasicTask {
    
    private ClassLoader expectedClassLoader;
    private boolean testEJBSecurity;
    private boolean testTransaction;
       
    public BasicTask(ClassLoader expectedClassLoader) {
        this.expectedClassLoader = expectedClassLoader;
    }
    
    public void setTestEjbSecurity(boolean testEjbSecurity) {
        this.testEJBSecurity = testEjbSecurity;
    }
    
    public void setTestTransaction(boolean testTransaction) {
        this.testTransaction = testTransaction;
    }
    
    public Object execute() throws Exception {
        System.out.println("Task starting");
        
        InitialContext ctx = new InitialContext();
        
        // lookup user env
        testEnvEntry(ctx);
        
        // lookup database pool
        testDataSource(ctx);
        
        // lookup user transaction
        testUserTransaction(ctx);
        
        // check class loader
        testClassLoader();
        
        // check ejb
        testEJB(ctx);
        
        System.out.println("Task stopping");
        return Boolean.TRUE;
    }
    
    private void testEnvEntry(InitialContext ctx) {
        try {
            String greeting = (String)ctx.lookup("java:comp/env/greeting");
            Assert.assertEquals(greeting, "Hello", "env-entry lookup");
        } catch (NamingException e) {
            Assert.fail("env-entry JNDI lookup failed", e);
        }
    }
    
    private void testDataSource(InitialContext ctx) {
        try {
            DataSource ds = (DataSource)ctx.lookup("java:comp/env/ConcurrentPool");
            Assert.assertNotNull(ds);
        } catch (NamingException e) {
            Assert.fail("DataSource JNDI lookup failed", e);
        }
    }
    
    private void testUserTransaction(InitialContext ctx) {
        try {
            UserTransaction userTransaction = 
                (UserTransaction)ctx.lookup("java:comp/UserTransaction");
            Assert.assertNotNull(userTransaction);
            if (this.testTransaction) {
                userTransaction.begin();
            }
        } catch (NamingException e) {
            Assert.fail("UserTransaction JNDI lookup failed", e);
        } catch (NotSupportedException e) {
            Assert.fail("Transaction failed", e);
        } catch (SystemException e) {
            Assert.fail("Transaction failed", e);
        } 
    }
    
    private void testEJB(InitialContext ctx) {
        try {
            CalculatorLocal calc = (CalculatorLocal)ctx.lookup("java:comp/env/ejb/Calculator");
            Assert.assertNotNull(calc);
            Assert.assertEquals(calc.sum(5, 2), 7, "add");
            if (this.testEJBSecurity) {
                // multiply() requires a specific role
                Assert.assertEquals(calc.multiply(5, 2), 10, "multiply");
            }
        } catch (NamingException e) {
            Assert.fail("EJB JNDI lookup failed", e);
        }
    }
        
    private void testClassLoader() {
        ClassLoader actaulClassLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertEquals(actaulClassLoader, expectedClassLoader, "ClassLoader");
    }
    
}
