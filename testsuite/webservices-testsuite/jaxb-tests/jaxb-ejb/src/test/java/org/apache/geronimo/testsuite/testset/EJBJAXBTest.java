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
package org.apache.geronimo.testsuite.testset;

import java.util.Properties;
import java.util.List;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.test.JAXBHome;
import org.apache.geronimo.test.JAXBObject;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class EJBJAXBTest extends TestSupport {

    @Test
    public void testEJB() throws Exception {
        Properties p = new Properties();
    
        p.put("java.naming.factory.initial", 
              "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put("java.naming.provider.url", 
              "127.0.0.1:4201");
        
        InitialContext ctx = new InitialContext(p);
        
        Object obj = ctx.lookup("/JAXB");

        JAXBHome ejbHome = 
            (JAXBHome)PortableRemoteObject.narrow(obj, JAXBHome.class);

        JAXBObject ejbObject = ejbHome.create();
    
        List factoryList = ejbObject.testJAXB();

        assertEquals(1, factoryList.size());
        assertEquals("Implementation", 
                     "com.sun.xml.bind.v2.runtime.JAXBContextImpl",
                     factoryList.get(0));

        System.out.println( factoryList );
    }
    
}
