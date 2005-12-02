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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class TestDriverImpl extends UnicastRemoteObject implements
TestDriver {
    /**
     * 
     */
    private final TestAgentManager testAgentManager;
    private Map properties = new HashMap();

    public TestDriverImpl(TestAgentManager manager, String driverName) throws RemoteException {
        testAgentManager = manager;
        this.driverName = driverName;
    }
    
    public void agentReady(String agentName, VMController controller)
    throws RemoteException {
        System.out.println("AgentReady: " + agentName);
        TestAgent testAgent = (TestAgent) testAgentManager.getAgentsByName().get(agentName);
        testAgent.agentReady(controller);
    }

    public String getDriverName() { return driverName; }

    String driverName;
    
    protected TestDriverImpl(TestAgentManager manager) throws RemoteException {
        super();
        testAgentManager = manager;
        System.out.println("Constructing TestDriverImpl");
    }

    public synchronized void putProperty(String id, String value) throws RemoteException {
        properties.put(id, value);
        
    }

    public synchronized String getProperty(String id) throws RemoteException {
        return (String) properties.get(id);
    }

	public Properties getAgentProperties(String agentName) throws RemoteException {
		return testAgentManager.tst.getAgentProperties(agentName);
	}
}
