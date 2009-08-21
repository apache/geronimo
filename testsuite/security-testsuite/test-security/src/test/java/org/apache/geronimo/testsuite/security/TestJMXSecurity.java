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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class TestJMXSecurity extends TestSupport {

    @Test
    public void testLogin() throws Exception {        
        Map environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, new String[] {"system", "manager"});
        
        MBeanServerConnection conn = getConnection(environment);
        System.out.println(conn.getDefaultDomain());
//        assertEquals("geronimo", conn.getDefaultDomain());        
        assertEquals("DefaultDomain", conn.getDefaultDomain());
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
            
    private void testFailure(String username, String password) throws Exception {
        Map environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, new String[] {username, password});
        try {
            MBeanServerConnection conn = getConnection(environment);
            fail("Did not throw security exception");
        } catch (SecurityException e) {
            // expected
        }
    }
    
    @Test
    public void testNoCredentialsLogin() throws Exception {        
        Map environment = new HashMap();
        
        try {
            MBeanServerConnection conn = getConnection(environment);
            fail("Did not throw exception");
        } catch (Exception e) {
            // expected
        }
    }
    
    private MBeanServerConnection getConnection(Map<String, ?> env) throws IOException {     
        String hostname = "localhost";       
        String port = "9999";
        String url = "service:jmx:rmi:///jndi/rmi://" + hostname + "/JMXConnector";
        JMXServiceURL serviceURL = new JMXServiceURL(url);
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL, env);
        return connector.getMBeanServerConnection();
    }
    
}
