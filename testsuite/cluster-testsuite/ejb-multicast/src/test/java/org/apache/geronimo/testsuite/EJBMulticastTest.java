/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.testsuite;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ejb.EJB;
import org.superbiz.load.Load;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Unit test for simple App.
 */
public class EJBMulticastTest {
    
    /**
     * Rigourous Test :-)
     */
	@Test
    public void testEJBMulticast()
    {
        //Remote lookup
            Properties p = new Properties();
            p.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            Load loadRemote = null;           
            
            //Multicast
            p.setProperty(Context.PROVIDER_URL,"multicast://239.255.3.2:6142?group=cluster1");
            Context context1;
			try {
				context1 = new InitialContext(p);
				loadRemote =(Load) context1.lookup("LoadBeanRemote");
				Assert.assertEquals(p.getProperty(Context.PROVIDER_URL),"multicast://239.255.3.2:6142?group=cluster1");
				Assert.assertNotNull(loadRemote.getNodeName());
				Assert.assertEquals(loadRemote.add(1, 2), 3);          
				Assert.assertEquals(loadRemote.sum(1,2,1), 4);
			} catch (NamingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();			}
            
            
            p.setProperty(Context.PROVIDER_URL,"failover:ejbd://127.0.0.1:4202,ejbd://127.0.0.1:4211,multicast://239.255.3.2:6142");
            
            try {
            	Context context3 = new InitialContext(p);
				loadRemote =(Load) context3.lookup("LoadBeanRemote");
				Assert.assertEquals(p.getProperty(Context.PROVIDER_URL),"failover:ejbd://127.0.0.1:4202,ejbd://127.0.0.1:4211,multicast://239.255.3.2:6142");
				Assert.assertNotNull(loadRemote.getNodeName());				
				Assert.assertEquals(loadRemote.add(1, 2), 3);          
				Assert.assertEquals(loadRemote.sum(1,2,1), 4);
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
    }
}
