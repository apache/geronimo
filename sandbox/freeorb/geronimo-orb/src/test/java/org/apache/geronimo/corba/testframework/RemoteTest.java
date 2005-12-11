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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.omg.CORBA.Object;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

public class RemoteTest extends Assert implements Test {
	public RemoteTest() {
	}

	public void setUp() throws Exception {
	}

	protected void startTestAgent(String agentName, boolean fork)
			throws Exception {
		testAgentManager.addAgent(agentName, getAgentProperties(agentName),
				fork);
	}

	protected void startTestAgent(String agentName) throws Exception {
		startTestAgent(agentName, true);
	}

	protected Properties getAgentProperties() throws Exception {
		if (driver == null) {
			return System.getProperties();
		} else {
			return driver.getAgentProperties(agentName);
		}
	}

	protected Properties getAgentProperties(String agentName) {
		Properties props = new Properties();
		addIfNonNull(props, "java.ext.dirs", System.getProperties());
		return props;
	}

	private void addIfNonNull(Properties props, String string,
			Properties properties) {
		String val = properties.getProperty(string);
		if (val != null) {
			props.setProperty(string, val);
		}
	}

	public void putProperty(String id, String value) {
		try {
			driver.putProperty(id, value);
		} catch (RemoteException e) {
			throw new Error(e);
		}
	}

	public String getProperty(String id) {
		try {
			return (String) driver.getProperty(id);
		} catch (RemoteException e) {
			throw new Error(e);
		}
	}

	protected static final String testKeyword = "Test";

	private void ensureTstMethods() {
		if (testMethods == null) {
			Method[] methods = this.getClass().getMethods();
			testMethods = new HashSet();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals("countTestCases"))
					continue;

				int testIndex = methods[i].getName().indexOf(testKeyword);
				if (testIndex > 0) {
					String testName = methods[i].getName().substring(
							testIndex + testKeyword.length());
					testMethods.add(testName);
				}
			}
		}
	}

	public int countTestCases() {
		ensureTstMethods();
		return testMethods.size();
	}

	// public void testIt() {}

	private Set testMethods;

	private TestAgentManager testAgentManager;

	public void run(TestResult result) {
		ensureTstMethods();
		try {
			testAgentManager = new TestAgentManager(this, this.getClass()
					.getName(), generateDriverName());
			setTstDriver(testAgentManager.getTestDriver());
			System.out.println("BEGIN setUp : "+getClass().getName());
			setUp();
			System.out.println("END setUp : "+getClass().getName());
			Iterator i = testMethods.iterator();
			while (i.hasNext()) {
				String testName = (String) i.next();
				RemoteTestCase testCase = new RemoteTestCase(this,
						testAgentManager, testName);
				testCase.run(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}
		testAgentManager.shutdownAllAgents();
	}

	private String generateDriverName() {
		return "testDriver." + this.getClass().getName() + "."
				+ Math.rint(Integer.MAX_VALUE);

	}

	private TestDriver driver;

	private String agentName;

	protected void setTstDriver(TestDriver driver) {
		this.driver = driver;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
}
