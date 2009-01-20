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
package org.apache.geronimo.jaxws.catalog;

import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.geronimo.jaxws.test.GreeterRemote;

import org.testng.annotations.Test;

public class EJBCatalogTest extends CatalogTest {

    protected String getTestServletContext() {
        return "/catalog-ejb";
    }
       
    @Test
    public void testClient() throws Exception {
        Properties p = new Properties();
        
        p.put("java.naming.factory.initial", 
              "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put("java.naming.provider.url", 
              "127.0.0.1:4201");   
        
        InitialContext ctx = new InitialContext(p);
        
        GreeterRemote greeter = (GreeterRemote)ctx.lookup("/GreeterClientRemote");
        
        String response = greeter.test();
        
        assertEquals("OK", response);
    }
}
