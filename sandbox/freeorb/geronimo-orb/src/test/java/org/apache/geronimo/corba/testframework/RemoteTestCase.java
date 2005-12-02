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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

class RemoteTestCase extends TestCase {

    public RemoteTestCase(Object testObject, TestAgentManager testAgentManager, String testName) {
        super(testObject.getClass().getName() + " :: " + testName);
        this.testAgentManager = testAgentManager;
        methodCalls = new ArrayList();
        Method[] methods = testObject.getClass().getMethods();
        String[] testPhases = new String[] {"Before", RemoteTest.testKeyword, "After" };
        for(int phaseIndex = 0; phaseIndex < testPhases.length; phaseIndex++) {
            Iterator agentNameIter = testAgentManager.getAgentNames().iterator();
            while(agentNameIter.hasNext()) {
                String agentName = (String) agentNameIter.next();
                for(int agentIndex = 0; agentIndex < methods.length; agentIndex++) {
                    String methodName = methods[agentIndex].getName();
                    if(methodName.equals(agentName + testPhases[phaseIndex] + testName)) {
                        AgentMethodPair pair = new AgentMethodPair(agentName, methods[agentIndex].getName());
                        methodCalls.add(pair);
                    }
                }            
            }
        }
        
    }
    
    private class AgentMethodPair {
        public AgentMethodPair(String agentName, String methodName) {
            this.agentName = agentName;
            this.methodName = methodName;
        }
        public String agentName;
        public String methodName;
    }
    
    private List methodCalls;

    private TestAgentManager testAgentManager;

    protected void runTest() throws Throwable {
        Iterator it = methodCalls.iterator();
        while(it.hasNext()) {
            AgentMethodPair pair = (AgentMethodPair) it.next();
            try {
                testAgentManager.invokeOnAgent(pair.agentName, pair.methodName);
            }
            catch(InvocationTargetException e) {
            		e.printStackTrace();
                throw e.getTargetException();
            }
        }
    }
}

