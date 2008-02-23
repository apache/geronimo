/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.cluster.infra;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.SessionManagerListener;
import org.apache.geronimo.clustering.wadi.NodeService;
import org.apache.geronimo.clustering.wadi.RemoteNode;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.openejb.EjbDeploymentIdAccessor;
import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.servicespace.InvocationMetaData;
import org.codehaus.wadi.servicespace.ServiceProxy;
import org.codehaus.wadi.servicespace.ServiceProxyFactory;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class NetworkConnectorMonitorTest extends RMockTestCase {

    private NetworkConnectorMonitor monitor;
    private ReferenceCollection connectors;
    private ReferenceCollectionListener connectorListener;
    private ReferenceCollection deployments;
    private ReferenceCollectionListener deploymentListener;
    private WADISessionManager manager;
    private SessionManagerListener managerListener;
    private ServiceProxyFactory trackerProxyFactory;
    private NetworkConnectorTrackerProxy tracker;
    private Collection<EjbDeploymentIdAccessor> mockStartingDeployments;
    private Collection<NetworkConnector> mockStartingConnectors;
    private String nodeName;
    private NetworkConnectorTracker localTracker;

    @Override
    protected void setUp() throws Exception {
        connectors = (ReferenceCollection) mock(ReferenceCollection.class, "connectors");
        connectors.addReferenceCollectionListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }
            public boolean passes(Object arg0) {
                connectorListener = (ReferenceCollectionListener) arg0;
                return true;
            }
        });
        
        deployments = (ReferenceCollection) mock(ReferenceCollection.class, "deployments");
        deployments.addReferenceCollectionListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }
            public boolean passes(Object arg0) {
                deploymentListener = (ReferenceCollectionListener) arg0;
                return true;
            }
        });
        
        manager = (WADISessionManager) mock(WADISessionManager.class);
        manager.registerSessionManagerListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }

            public boolean passes(Object arg0) {
                managerListener = (SessionManagerListener) arg0;
                return true;
            }
        });
        
        manager.getCluster().getLocalNode().getName();
        nodeName = "nodeName";
        modify().returnValue(nodeName);
        
        trackerProxyFactory = (ServiceProxyFactory) mock(ServiceProxyFactory.class);
        
        tracker = (NetworkConnectorTrackerProxy) mock(NetworkConnectorTrackerProxy.class);
        trackerProxyFactory.getProxy();
        modify().returnValue(tracker);
    }
    
    public void testDeploymentLocationsAreRegisteredUponStart() throws Exception {
        recordStart();
        
        startVerificationAndDoStartMonitor();
    }

    public void testAddConnectorEventTriggersTrackerRegistration() throws Exception {
        new ExecuteConnectorEventTest() {
            @Override
            protected void executeListenerCallback(ReferenceCollectionEvent event) {
                connectorListener.memberAdded(event);
            }
            @Override
            protected void executeTrackerOperation(String deploymentId, Set<URI> locations) {
                tracker.registerNetworkConnectorLocations(deploymentId, nodeName, locations);
            }
        }.executeTest();
    }

    public void testRemoveConnectorEventTriggersTrackerUnregistration() throws Exception {
        new ExecuteConnectorEventTest() {
            @Override
            protected void executeListenerCallback(ReferenceCollectionEvent event) {
                connectorListener.memberRemoved(event);
            }
            @Override
            protected void executeTrackerOperation(String deploymentId, Set<URI> locations) {
                tracker.unregisterNetworkConnectorLocations(deploymentId, nodeName, locations);
            }
        }.executeTest();
    }
    
    public void testAddDeploymentEventTriggersTrackerRegistration() throws Exception {
        new ExecuteDeploymentEventTest() {
            @Override
            protected void executeListenerCallback(ReferenceCollectionEvent event) {
                deploymentListener.memberAdded(event);
            }
            @Override
            protected void executeTrackerOperation(String deploymentId, Set<URI> locations) {
                tracker.registerNetworkConnectorLocations(deploymentId, nodeName, locations);
            }
        }.executeTest();
    }
    
    public void testRemoveDeploymentEventTriggersTrackerUnregistration() throws Exception {
        new ExecuteDeploymentEventTest() {
            @Override
            protected void executeListenerCallback(ReferenceCollectionEvent event) {
                deploymentListener.memberRemoved(event);
            }
            @Override
            protected void executeTrackerOperation(String deploymentId, Set<URI> locations) {
                tracker.unregisterNetworkConnectorLocations(deploymentId, nodeName, locations);
            }
        }.executeTest();
    }

    public void testJoiningNodeTriggersTrackerRegistrationForJoiningNode() throws Exception {
        recordStart();
        
        deployments.iterator();
        modify().returnValue(mockStartingDeployments.iterator());

        Peer joiningPeer = (Peer) mock(Peer.class);
        joiningPeer.getLocalStateMap();
        modify().returnValue(new HashMap());
        joiningPeer.getName();
        modify().returnValue("joiningPeer");
        
        NodeService nodeService = (NodeService) mock(NodeService.class);

        NetworkConnectorTrackerProxy proxy = (NetworkConnectorTrackerProxy) mock(NetworkConnectorTrackerProxy.class,
            "NetworkConnectorTrackerProxy2");
        trackerProxyFactory.getProxy();
        modify().returnValue(proxy);
        
        InvocationMetaData invocationMetaData = (InvocationMetaData) mock(InvocationMetaData.class);
        proxy.getInvocationMetaData();
        modify().returnValue(invocationMetaData);
        invocationMetaData.setTargets(new Peer[] {joiningPeer});
        
        proxy.registerNetworkConnectorLocations("deploymentId", nodeName, Collections.singleton(new URI("ejbd://host:1")));
        
        startVerificationAndDoStartMonitor();
        
        RemoteNode joiningNode = new RemoteNode(joiningPeer, nodeService);
        
        managerListener.onJoin(joiningNode, null);
    }

    public void testLeavingNodeTriggersLocalTrackerUnRegistration() throws Exception {
        recordStart();
        
        Node leavingNode = (Node) mock(Node.class);
        leavingNode.getName();
        String leavingNodeName = "leavingNode";
        modify().returnValue(leavingNodeName);

        localTracker.unregisterNetworkConnectorLocations(leavingNodeName);
        
        startVerificationAndDoStartMonitor();

        managerListener.onLeave(leavingNode, null);
    }
    
    protected void startVerificationAndDoStartMonitor() throws Exception {
        startVerification();
        
        monitor = newMonitor();
        monitor.doStart();
    }
    
    protected abstract class ExecuteDeploymentEventTest {
        public void executeTest() throws Exception {
            recordStart();

            String deploymentId2 = "deploymentId2";
            EjbDeploymentIdAccessor idAccessor2 = newDeploymentIdAccessor(deploymentId2);

            trackerProxyFactory.getProxy();
            modify().returnValue(tracker);
            executeTrackerOperation(deploymentId2, Collections.singleton(new URI("ejbd://host:1")));

            startVerificationAndDoStartMonitor();
            
            ReferenceCollectionEvent event = new ReferenceCollectionEvent("name", idAccessor2);
            executeListenerCallback(event);
        }
        
        protected abstract void executeTrackerOperation(String deploymentId, Set<URI> locations);
        
        protected abstract void executeListenerCallback(ReferenceCollectionEvent event);
    }
    
    protected abstract class ExecuteConnectorEventTest {
        public void executeTest() throws Exception {
            recordStart();

            deployments.iterator();
            modify().returnValue(mockStartingDeployments.iterator());

            NetworkConnector connector = newConnector(2);

            trackerProxyFactory.getProxy();
            modify().returnValue(tracker);
            executeTrackerOperation("deploymentId", Collections.singleton(new URI("ejbd://host:2")));

            startVerificationAndDoStartMonitor();
            
            ReferenceCollectionEvent event = new ReferenceCollectionEvent("name", connector);
            executeListenerCallback(event);
        }
        
        protected abstract void executeTrackerOperation(String deploymentId, Set<URI> locations);
        
        protected abstract void executeListenerCallback(ReferenceCollectionEvent event);
    }
    
    private NetworkConnectorMonitor newMonitor() {
        return new NetworkConnectorMonitor(connectors, deployments, manager) {
            @Override
            protected ServiceProxyFactory newNetworkConnectorTrackerProxy() {
                return trackerProxyFactory;
            }
        };
    }

    protected void recordStart() throws Exception {
        localTracker = (NetworkConnectorTracker) mock(NetworkConnectorTracker.class);
        manager.getServiceSpace().getServiceRegistry().getStartedService(NetworkConnectorTracker.NAME);
        modify().returnValue(localTracker);
        
        mockStartingDeployments = new ArrayList<EjbDeploymentIdAccessor>();
        String deploymentId = "deploymentId";
        EjbDeploymentIdAccessor idAccessor = newDeploymentIdAccessor(deploymentId);
        mockStartingDeployments.add(idAccessor);
        deployments.iterator();
        modify().returnValue(mockStartingDeployments.iterator());

        mockStartingConnectors = new ArrayList<NetworkConnector>();
        NetworkConnector connector = newConnector(1);
        mockStartingConnectors.add(connector);
        connectors.iterator();
        modify().returnValue(mockStartingConnectors.iterator());
        
        tracker.registerNetworkConnectorLocations(deploymentId, nodeName, Collections.singleton(new URI("ejbd://host:1")));
    }

    private EjbDeploymentIdAccessor newDeploymentIdAccessor(String deploymentId) {
        EjbDeploymentIdAccessor idAccessor = (EjbDeploymentIdAccessor) mock(EjbDeploymentIdAccessor.class,
            "EjbDeploymentIdAccessor" + deploymentId);
        idAccessor.getDeploymentId();
        modify().multiplicity(expect.from(0)).returnValue(deploymentId);
        return idAccessor;
    }

    private NetworkConnector newConnector(int port) {
        NetworkConnector connector = (NetworkConnector) mock(NetworkConnector.class, "NetworkConnector" + port);
        connector.getHost();
        modify().returnValue("host");
        connector.getPort();
        modify().returnValue(port);
        connector.getProtocol();
        modify().returnValue("ejbd");
        return connector;
    }

    public interface NetworkConnectorTrackerProxy extends NetworkConnectorTracker, ServiceProxy {
    }

}
