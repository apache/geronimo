/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2ee;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.xmlbeans.XmlObject;
import org.openejb.deployment.OpenEJBModuleBuilder;

/**
 * <p>This test case show the infomation about openEJB that we assumed. And the
 * simmlier code code is used in the real code. As the OpenEJB is developing and 
 * rapidly changing this test case act as a notifier for saying things has chaged</p>   
 */
public class DynamicEJBDeploymentTest extends AbstractTestCase{
	private static final String j2eeDomainName = "openejb.server";
	private static final String j2eeServerName = "TestOpenEJBServer";
	private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
	private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");
	private Kernel kernel;

    /**
     * @param testName
     */
    public DynamicEJBDeploymentTest(String testName) {
        super(testName);
    }

	protected void setUp() throws Exception {
		super.setUp();
		String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
		if (str == null) {
			str = ":org.apache.geronimo.naming";
		} else {
			str = str + ":org.apache.geronimo.naming";
		}
		System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
		kernel = new Kernel("blah");
		kernel.boot();

		GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
		serverInfoGBean.setAttribute("baseDirectory", ".");
		ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ServerInfo");
		kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
		kernel.startGBean(serverInfoObjectName);
		assertRunning(kernel, serverInfoObjectName);

		GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
		j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
		ObjectName j2eeServerObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName);
		kernel.loadGBean(j2eeServerObjectName, j2eeServerGBean);
		kernel.startGBean(j2eeServerObjectName);
		assertRunning(kernel, j2eeServerObjectName);

		GBeanMBean tmGBean = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
		Set patterns = new HashSet();
		patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
		patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=ActivationSpec,*"));
		tmGBean.setReferencePatterns("ResourceManagers", patterns);
		kernel.loadGBean(transactionManagerObjectName, tmGBean);
		kernel.startGBean(transactionManagerObjectName);
		assertRunning(kernel, transactionManagerObjectName);

		GBeanMBean connectionTrackerGBean = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
		ObjectName connectionTrackerObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ConnectionTracker");
		kernel.loadGBean(connectionTrackerObjectName, connectionTrackerGBean);
		kernel.startGBean(connectionTrackerObjectName);
		assertRunning(kernel, connectionTrackerObjectName);

		//load mock resource adapter for mdb
//		DeploymentHelper.setUpResourceAdapter(kernel);

	}
	public void testEJBJarDeploy() throws Exception {
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(kernel);
	
		File earFile =  new File(outDir + "/echo-ewsimpl.jar");
		if(!earFile.exists()){
			GeronimoWsDeployContext deployContext =
				 new GeronimoWsDeployContext(
					 getTestFile("target/samples/echo.jar"),
					 outDir);
			Ws4J2ee ws4j2ee = new Ws4J2ee(deployContext, null);
					ws4j2ee.generate();
		}
	
	
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = new URLClassLoader(new URL[]{earFile.toURL()}, oldCl);
	
		Thread.currentThread().setContextClassLoader(cl);
	
		File carFile = File.createTempFile("OpenEJBTest", ".car");
		ObjectName j2eeServerObjectName = new ObjectName(j2eeDomainName 
				+ ":j2eeType=J2EEServer,name=" + j2eeServerName);
		try {
            EARConfigBuilder earConfigBuilder =
                    new EARConfigBuilder(new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                            transactionManagerObjectName,
                            connectionTrackerObjectName,
                            null,
                            null,
                            null,
                            moduleBuilder,
                            moduleBuilder,
                            null,
                            null,
                            null);
//			new EARConfigBuilder(	null,
//									null,
//									j2eeServerObjectName,
//									moduleBuilder,
//									null,	// web
//									null, //connector
//								transactionManagerObjectName, connectionTrackerObjectName);

			XmlObject plan = earConfigBuilder.getDeploymentPlan(earFile.toURL());
			earConfigBuilder.buildConfiguration(carFile, null, earFile, plan);
			File unpackedDir = new File(tempDir, "OpenEJBTest-ear-Unpacked");
			LocalConfigStore.unpack(unpackedDir, new FileInputStream(carFile));
		} finally {
			carFile.delete();
		}
	}


	protected void tearDown() throws Exception {
		kernel.stopGBean(connectionTrackerObjectName);
		kernel.stopGBean(transactionManagerObjectName);
		kernel.shutdown();
	}
	private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
		int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
		assertEquals(State.RUNNING_INDEX, state);
	}


}

