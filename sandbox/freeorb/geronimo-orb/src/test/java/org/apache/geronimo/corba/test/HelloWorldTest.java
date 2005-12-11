/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.corba.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.geronimo.corba.testframework.RemoteTest;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantRetentionPolicyValue;

public abstract class HelloWorldTest extends RemoteTest {

    static protected final String ORBClassKey = "org.omg.CORBA.ORBClass";
    static protected final String ORBSingletonClassKey = "org.omg.CORBA.ORBSingletonClass";

	public void setUp() throws Exception {
		startTestAgent("server", true);
		startTestAgent("client", true);
	}
	
	ORB orb;

	HelloObjectOperations hello;

	public void serverBefore1() throws Exception {

		hello = new HelloObjectServant();

		orb = ORB.init(new String[0], getAgentProperties());

		//com.sun.corba.se.spi.orb.ORB o = (com.sun.corba.se.spi.orb.ORB) orb;
		//o.giopDebugFlag = true;
		//o.giopVersionDebugFlag = true;
		//o.transportDebugFlag = true;
		
        new Thread() {
            public void run() {
                orb.run();
            }
        }.start();

		POA root = (POA) orb.resolve_initial_references("RootPOA");

		List plist = new ArrayList();

		plist
				.add(root
						.create_id_assignment_policy(IdAssignmentPolicyValue.SYSTEM_ID));

		plist
				.add(root
						.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION));

		plist
				.add(root
						.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN));

		Policy[] policies = (Policy[]) plist.toArray(new Policy[0]);

		POA poa = root.create_POA("poa1", null, policies);

		org.omg.CORBA.Object ref = poa.servant_to_reference((Servant) hello);

		String ior = orb.object_to_string(ref);
		
		poa.the_POAManager().activate();

		putProperty("serverIOR", ior);
	}

	public void clientBefore1() throws Exception {
		orb = ORB.init(new String[0], getAgentProperties());
		

		String ior = getProperty("serverIOR");
		assertNotNull(ior);

		System.out.println("ior is: " + ior);

		System.out.println("ORB is "+orb);
		
		Object obj = orb.string_to_object(ior);
		assertNotNull(obj);
		
		hello = HelloObjectHelper.narrow(obj);
		
		assertNotNull(hello);

	}
	
	public void serverAfter1() throws Exception {
		orb.shutdown(false);
	}

	
	public void serverTest2() {
		System.out.println("hej far");
	}
	
	public void clientTest1() throws Exception {

		try {
		
		//System.out.println("ce1.1");
		//hello.hello11();

		System.out.println("ce1.2");
		System.out.flush();
		hello.hello1((short) 0xba);
		
		System.out.println("ce1.3");
		hello.hello2(0x01020304);

		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

}
