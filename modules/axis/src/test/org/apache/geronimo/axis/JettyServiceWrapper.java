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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.JettyContainerImpl;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.GeronimoTransactionManager;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hemapani@opensource.lk
 */
public class JettyServiceWrapper {
	private ObjectName containerName;
	private Set containerPatterns;
	private ObjectName tmName;
	private ObjectName tcaName;
	private ObjectName connectorName;
	
	private final MBeanServer mbServer;
	
	public JettyServiceWrapper(Kernel kernel)throws MalformedObjectNameException{
            this.mbServer = kernel.getMBeanServer();
            
            containerName = new ObjectName(AxisGeronimoConstants.WEB_CONTANER_NAME);
            containerPatterns = Collections.singleton(containerName);
            connectorName = new ObjectName(AxisGeronimoConstants.WEB_CONNECTOR_NAME);
            tmName = new ObjectName(AxisGeronimoConstants.TRANSACTION_MANAGER_NAME);
            tcaName = 
            	new ObjectName(AxisGeronimoConstants.CONNTECTION_TRACKING_COORDINATOR);
	}

	public void doStart() throws Exception {
		GBeanMBean connector;
		GBeanMBean tm;
		GBeanMBean ctc;
		GBeanMBean container;
		container = new GBeanMBean(JettyContainerImpl.GBEAN_INFO);
		connector = new GBeanMBean(HTTPConnector.GBEAN_INFO);
		connector.setAttribute("port", new Integer(AxisGeronimoConstants.AXIS_SERVICE_PORT));
		connector.setReferencePatterns("JettyContainer", containerPatterns);
		start(containerName, container);
		start(connectorName, connector);
		tm = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
		Set patterns = new HashSet();
		patterns.add(
				ObjectName.getInstance(
						"geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
		tm.setReferencePatterns("resourceManagers", patterns);
		start(tmName, tm);
		ctc = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
		start(tcaName, ctc);
	}
	
	public void doStop() throws Exception{
		stop(tcaName);
		stop(tmName);
		stop(connectorName);
		stop(containerName);
	}
	
	
	private void start(ObjectName name, Object instance) throws Exception {
		 mbServer.registerMBean(instance, name);
		 mbServer.invoke(name, "start", null, null);
	 }

	 private void stop(ObjectName name) throws Exception {
		 mbServer.invoke(name, "stop", null, null);
		 mbServer.unregisterMBean(name);
	 }

}
