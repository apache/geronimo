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

package org.apache.geronimo.clustering.wadi;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManagerListener;
import org.codehaus.wadi.core.assembler.StackContext;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.core.manager.SessionAlreadyExistException;
import org.codehaus.wadi.core.manager.SessionMonitor;
import org.codehaus.wadi.core.session.Session;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.codehaus.wadi.servicespace.LifecycleState;
import org.codehaus.wadi.servicespace.ServiceAlreadyRegisteredException;
import org.codehaus.wadi.servicespace.ServiceName;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;
import org.codehaus.wadi.servicespace.ServiceSpaceLifecycleEvent;
import org.codehaus.wadi.servicespace.ServiceSpaceListener;
import org.codehaus.wadi.servicespace.ServiceSpaceName;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicWADISessionManagerTest extends RMockTestCase {

    private ServiceSpace serviceSpace;
    private SessionMonitor sessionMonitor;
    private Manager wadiManager;
    private WADICluster cluster;
    private BackingStrategyFactory backingStrategyFactory;
    private BasicWADISessionManager manager;

    private org.codehaus.wadi.core.manager.SessionListener wadiSessionListener;
    private ServiceSpaceListener serviceSpaceListener;

    @Override
    protected void setUp() throws Exception {
        cluster = (WADICluster) mock(WADICluster.class);
        backingStrategyFactory = (BackingStrategyFactory) mock(BackingStrategyFactory.class);
        serviceSpace = (ServiceSpace) mock(ServiceSpace.class);
        wadiManager = (Manager) mock(Manager.class);
        sessionMonitor = (SessionMonitor) mock(SessionMonitor.class);
    }

    private BasicWADISessionManager newManager(Collection<ClusteredServiceHolder> serviceHolders) throws Exception {
        return new BasicWADISessionManager(getClass().getClassLoader(),
            new WADISessionManagerConfigInfo(new URI("uri"), 60, 12, 60, false, false),
            cluster,
            backingStrategyFactory,
            serviceHolders) {
            @Override
            protected StackContext newStackContext(Dispatcher underlyingDisp, ServiceSpaceName serviceSpaceName) {
                return new StackContext(cl,
                    serviceSpaceName,
                    underlyingDisp,
                    configInfo.getSessionTimeoutSeconds(),
                    configInfo.getNumPartitions(),
                    configInfo.getSweepInterval(),
                    backingStrategyFactory) {
                    @Override
                    public void build() throws ServiceAlreadyRegisteredException {
                    }
                    @Override
                    public ServiceSpace getServiceSpace() {
                        return BasicWADISessionManagerTest.this.serviceSpace;
                    }
                    @Override
                    public Manager getManager() {
                        return BasicWADISessionManagerTest.this.wadiManager;
                    }
                    @Override
                    public SessionMonitor getSessionMonitor() {
                        return BasicWADISessionManagerTest.this.sessionMonitor;
                    }
                };
            }
        };
    }
    
    public void testBuildStackContextAndStartServiceSpaceUponStart() throws Exception {
        recordDoStart();
        
        startVerification();
        
        manager = newManager(Collections.EMPTY_SET);
        manager.doStart();
    }

    public void testRegisterServiceHoldersPriorToStart() throws Exception {
        cluster.getCluster().getDispatcher();

        sessionMonitor.addSessionListener(null);
        modify().args(is.NOT_NULL);
        
        ClusteredServiceHolder serviceHolder = (ClusteredServiceHolder) mock(ClusteredServiceHolder.class);
        
        serviceHolder.getService();
        Object service = new Object();
        modify().returnValue(service);
        
        serviceHolder.getServiceName();
        ServiceName serviceName = new ServiceName("name");
        modify().returnValue(serviceName);

        beginSection(s.ordered("Register service then start"));

        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        serviceRegistry.register(serviceName, service);
        
        serviceSpace.start();
        endSection();
        
        startVerification();
        
        manager = newManager(Collections.singleton(serviceHolder));
        manager.doStart();
    }

    public void testCreateSessionOK() throws Exception {
        recordDoStart();
        
        String sessionId = "sessionId";
        recordCreateSession(sessionId);
        
        startVerification();
        
        manager = newManager(Collections.EMPTY_SET);
        manager.doStart();
        manager.createSession(sessionId);
    }

    public void testCreateSessionFailsWhenSessionIdAlreadyExists() throws Exception {
        recordDoStart();
        
        String sessionId = "sessionId";
        wadiManager.createWithName(sessionId);
        modify().throwException(new SessionAlreadyExistException());
        
        startVerification();
        
        manager = newManager(Collections.EMPTY_SET);
        manager.doStart();
        try {
            manager.createSession(sessionId);
            fail();
        } catch (org.apache.geronimo.clustering.SessionAlreadyExistException e) {
        }
    }
    
    public void testSessionDestructionCallbackPropagatesToSessionListener() throws Exception {
        new SessionListenerTestTemplate() {
            @Override
            protected void executeSessionListenerCallback(SessionListener sessionListener) {
                sessionListener.notifySessionDestruction(null);
            }
            @Override
            protected void executeWADISessionListenerCallback(Session wadiSession) {
                wadiSessionListener.onSessionDestruction(wadiSession);
            }
        }.executeTest();
    }
    
    public void testInboundSessionMigrationCallbackPropagatesToSessionListener() throws Exception {
        new SessionListenerTestTemplate() {
            @Override
            protected void executeSessionListenerCallback(SessionListener sessionListener) {
                sessionListener.notifyInboundSessionMigration(null);
            }
            @Override
            protected void executeWADISessionListenerCallback(Session wadiSession) {
                wadiSessionListener.onInboundSessionMigration(wadiSession);
            }
        }.executeTest();
    }
    
    public void testOutboundSessionMigrationCallbackPropagatesToSessionListener() throws Exception {
        new SessionListenerTestTemplate() {
            @Override
            protected void executeSessionListenerCallback(SessionListener sessionListener) {
                sessionListener.notifyOutboundSessionMigration(null);
            }
            @Override
            protected void executeWADISessionListenerCallback(Session wadiSession) {
                wadiSessionListener.onOutbountSessionMigration(wadiSession);
            }
        }.executeTest();
    }
    
    public void testServiceSpaceListenerStartedEventPropagatesToSessionManagerListener() throws Exception {
        new SessionManagerListenerTestTemplate() {
            @Override
            protected void executeManagerListenerCallback(SessionManagerListener managerListener) {
                managerListener.onJoin(null, null);
            }
        }.executeTest(LifecycleState.STARTED);
    }
    
    public void testServiceSpaceListenerFailedEventPropagatesToSessionManagerListener() throws Exception {
        new SessionManagerListenerTestTemplate() {
            @Override
            protected void executeManagerListenerCallback(SessionManagerListener managerListener) {
                managerListener.onLeave(null, null);
            }
        }.executeTest(LifecycleState.FAILED);
    }
    
    public void testServiceSpaceListenerStoppedEventPropagatesToSessionManagerListener() throws Exception {
        new SessionManagerListenerTestTemplate() {
            @Override
            protected void executeManagerListenerCallback(SessionManagerListener managerListener) {
                managerListener.onLeave(null, null);
            }
        }.executeTest(LifecycleState.STOPPED);
    }
    
    public void testSessionManagerUnregistration() throws Exception {
        recordDoStart();
        
        SessionManagerListener managerListener = (SessionManagerListener) mock(SessionManagerListener.class);
        serviceSpace.addServiceSpaceListener(null);
        modify().args(is.NOT_NULL);

        serviceSpace.removeServiceSpaceListener(null);
        modify().args(is.NOT_NULL);

        startVerification();

        manager = newManager(Collections.EMPTY_SET);
        manager.doStart();
        manager.registerSessionManagerListener(managerListener);
        manager.unregisterSessionManagerListener(managerListener);
    }
    
    public void testGetRemoteNodes() throws Exception {
        recordDoStart();
        
        NodeService nodeService = (NodeService) mock(NodeService.class);
        
        Peer peer = (Peer) mock(Peer.class);
        peer.getName();
        modify().returnValue("peer");
        peer.getLocalStateMap();
        modify().multiplicity(expect.from(0)).returnValue(new HashMap());

        HashSet<Peer> peers = new HashSet<Peer>();
        peers.add(peer);
        serviceSpace.getHostingPeers();
        modify().returnValue(peers);

        startVerification();
        RemoteNode remoteNode = new RemoteNode(peer, nodeService);

        manager = newManager(Collections.EMPTY_SET);
        manager.doStart();
        Set<Node> remoteNodes = manager.getRemoteNodes();
        assertEquals(1, remoteNodes.size());
        assertTrue(remoteNodes.contains(remoteNode));
    }
    
    protected abstract class SessionManagerListenerTestTemplate {
        public void executeTest(LifecycleState lifecycleState) throws Exception {
            recordDoStart();
            
            SessionManagerListener managerListener = (SessionManagerListener) mock(SessionManagerListener.class);
            
            serviceSpace.addServiceSpaceListener(null);
            modify().args(new AbstractExpression() {
                public void describeWith(ExpressionDescriber arg0) throws IOException {
                }
                public boolean passes(Object arg0) {
                    serviceSpaceListener = (ServiceSpaceListener) arg0;
                    return true;
                }
            });
            
            NodeService nodeService = (NodeService) mock(NodeService.class);
            
            final Peer peer = (Peer) mock(Peer.class);
            peer.getName();
            modify().returnValue("peer");
            peer.getLocalStateMap();
            modify().multiplicity(expect.from(0)).returnValue(new HashMap());

            HashSet<Peer> peers = new HashSet<Peer>();
            peers.add(peer);
            
            executeManagerListenerCallback(managerListener);
            modify().args(new AbstractExpression() {
                public void describeWith(ExpressionDescriber arg0) throws IOException {
                }

                public boolean passes(Object arg0) {
                    RemoteNode remoteNode = (RemoteNode) arg0;
                    assertSame(peer, remoteNode.getPeer());
                    return true;
                }
            }, new AbstractExpression() {
                public void describeWith(ExpressionDescriber arg0) throws IOException {
                }

                public boolean passes(Object arg0) {
                    Set<RemoteNode> remoteNodes = (Set<RemoteNode>) arg0;
                    assertEquals(1, remoteNodes.size());
                    RemoteNode remoteNode = remoteNodes.iterator().next();
                    assertSame(peer, remoteNode.getPeer());
                    return true;
                }
            });
            
            startVerification();
            RemoteNode remoteNode = new RemoteNode(peer, nodeService);

            manager = newManager(Collections.EMPTY_SET);
            manager.doStart();
            manager.registerSessionManagerListener(managerListener);
            serviceSpaceListener.receive(new ServiceSpaceLifecycleEvent(new ServiceSpaceName(new URI("name")),
                peer,
                lifecycleState),
                peers);
        }

        protected abstract void executeManagerListenerCallback(SessionManagerListener managerListener);
    }
    
    protected abstract class SessionListenerTestTemplate {
        public void executeTest() throws Exception {
            recordDoStart();
            
            String sessionId = "sessionId";
            org.codehaus.wadi.core.session.Session wadiSession = recordCreateSession(sessionId);
            
            SessionListener sessionListener = (SessionListener) mock(SessionListener.class);
            executeSessionListenerCallback(sessionListener);
            modify().args(is.NOT_NULL);
            
            startVerification();
            
            manager = newManager(Collections.EMPTY_SET);
            manager.doStart();
            manager.registerListener(sessionListener);
            manager.createSession(sessionId);
            executeWADISessionListenerCallback(wadiSession);
        }
        
        protected abstract void executeWADISessionListenerCallback(Session wadiSession);

        protected abstract void executeSessionListenerCallback(SessionListener sessionListener);
    }

    private void recordDoStart() throws Exception {
        cluster.getCluster().getDispatcher();
        
        serviceSpace.getServiceRegistry();
        
        sessionMonitor.addSessionListener(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }

            public boolean passes(Object arg0) {
                wadiSessionListener = (org.codehaus.wadi.core.manager.SessionListener) arg0;
                return true;
            }
        });
        
        serviceSpace.start();
    }

    private org.codehaus.wadi.core.session.Session recordCreateSession(String sessionId)
            throws SessionAlreadyExistException {
        org.codehaus.wadi.core.session.Session wadiSession = wadiManager.createWithName(sessionId);
        wadiSession.getLocalStateMap();
        modify().multiplicity(expect.from(0)).returnValue(new HashMap());
        return wadiSession;
    }

}
