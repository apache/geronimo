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
package org.apache.geronimo.corba.testframework;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestAgentManager {
	
	final RemoteTest tst;

	public TestAgentManager(RemoteTest tst, String testClassName, String driverName)
			throws RemoteException {
		this.tst = tst;
		this.testClassName = testClassName;
		setTestDriver(new TestDriverImpl(this, driverName));
		setupRegistry();
		bindDriverInRegistry();
	}

	public void addAgent(String agentName, Properties props, boolean fork) throws IOException {
		TestAgent agent = new TestAgent(agentName, fork);
		getAgentsByName().put(agentName, agent);
		agentNames.add(agentName);
		agent.start(getTestDriver().getDriverName(), testClassName, props);
	}

	public String getClassName() {
		return testClassName;
	}

	public List getAgentNames() {
		return agentNames;
	}

	public void invokeOnAgent(String agentName, String methodName)
			throws Exception {
		
		TestAgent agent = ((TestAgent) getAgentsByName().get(agentName));
		VMController controller = agent.getController();
		controller.invoke(
				methodName);
	}

	public void shutdownAllAgents() {
		Iterator it = agentNames.iterator();
		while (it.hasNext()) {
			TestAgent agent = (TestAgent) agentsByName.get(it.next());
			try {
				agent.getController().exit(0);
			} catch (Exception e) {
			}
		}
	}

	private List agentNames = new ArrayList();

	private Map agentsByName = new HashMap();

	private TestDriverImpl testDriver;

	private Registry rmiRegistry;

	private String testClassName;

	private void setupRegistry() throws RemoteException {
		if (rmiRegistry == null) {
			try {
				rmiRegistry = LocateRegistry
						.createRegistry(Registry.REGISTRY_PORT);
			} catch (Exception e) {
				e.printStackTrace();
				rmiRegistry = LocateRegistry
						.getRegistry(Registry.REGISTRY_PORT);
			}
		}
	}

	private void bindDriverInRegistry() throws RemoteException {
		rmiRegistry.rebind(getTestDriver().getDriverName(), getTestDriver());
	}

	TestDriverImpl getTestDriver() {
		return testDriver;
	}

	private void setTestDriver(TestDriverImpl testDriver) {
		this.testDriver = testDriver;
	}

	public void setAgentsByName(Map agentsByName) {
		this.agentsByName = agentsByName;
	}

	public Map getAgentsByName() {
		return agentsByName;
	}

}
