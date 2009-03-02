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

package org.apache.geronimo.testsuite.security;

import java.rmi.AccessException;
import java.util.Hashtable;

import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class TestMEJBSecurity extends TestSupport {
       
    @Test
    public void testLogin() throws Exception {   
        Hashtable env = getEnvironment();
        env.put(Context.SECURITY_PRINCIPAL, "system");
        env.put(Context.SECURITY_CREDENTIALS, "manager");
        
        Management mgmt = getMEJB(env);
        System.out.println(mgmt.getDefaultDomain());
//        assertEquals("geronimo", mgmt.getDefaultDomain());
        assertEquals("DefaultDomain", mgmt.getDefaultDomain());        
    }

    @Test
    public void testBadPasswordLogin() throws Exception {   
        testFailure("system", "managerr");
    }
    
    @Test
    public void testBadUser() throws Exception {  
        testFailure("doesnotexist", "managerr");
    }
    
    @Test
    public void testNullPasswordLogin() throws Exception {        
        testFailure("system", null);
    }
    
    @Test
    public void testNullUserLogin() throws Exception {        
        testFailure(null, "manager");
    }
    
    @Test
    public void testNullCredentialsLogin() throws Exception {        
        testFailure(null, null);
    }
    
    @Test
    public void testEmptyCredentialsLogin() throws Exception {        
        testFailure("", "");
    }
            
    protected void testFailure(String username, String password) throws Exception {
        Hashtable env = getEnvironment();
        if (username != null) {
            env.put(Context.SECURITY_PRINCIPAL, username);
        }
        if (password != null) {
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        
        try {
            Management mgmt = getMEJB(env);
            fail("Did not throw security exception");
        } catch (AuthenticationException e) {
            // expected
            e.printStackTrace(System.out);
        } catch (AccessException e) {
            // expected
            e.printStackTrace(System.out);
        }
    }
        
    protected Hashtable getEnvironment() {
        Hashtable p = new Hashtable();
        
        p.put("java.naming.factory.initial", 
              "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put("java.naming.provider.url", 
              "127.0.0.1:4201");  
        
        // XXXX: this should not be necessary
        String realmName = getRealmName();
        if (realmName != null) {
            p.put("openejb.authentication.realmName", realmName);
        }
        
        return p;
    }
    
    protected Management getMEJB(Hashtable env) throws Exception { 
        String jndiName = "ejb/mgmt/MEJB";
        InitialContext ctx = new InitialContext(env);
        Object objref = ctx.lookup(jndiName);
        ManagementHome home = (ManagementHome)
            PortableRemoteObject.narrow(objref,ManagementHome.class);
        Management mejb = home.create();
        return mejb;
    }
    
    protected String getRealmName() {
        return "geronimo-admin";
    }
    
}
