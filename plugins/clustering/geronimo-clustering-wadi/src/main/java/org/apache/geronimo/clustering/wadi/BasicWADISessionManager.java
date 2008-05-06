/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering.wadi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.clustering.Cluster;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.Session;
import org.apache.geronimo.clustering.SessionAlreadyExistException;
import org.apache.geronimo.clustering.SessionListener;
import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.clustering.SessionManagerListener;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.codehaus.wadi.aop.replication.AOPStackContext;
import org.codehaus.wadi.core.assembler.StackContext;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.core.manager.SessionMonitor;
import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.codehaus.wadi.servicespace.LifecycleState;
import org.codehaus.wadi.servicespace.ServiceAlreadyRegisteredException;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;
import org.codehaus.wadi.servicespace.ServiceSpaceLifecycleEvent;
import org.codehaus.wadi.servicespace.ServiceSpaceListener;
import org.codehaus.wadi.servicespace.ServiceSpaceName;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicWADISessionManager implements GBeanLifecycle, SessionManager, WADISessionManager {
    private static final Logger log = LoggerFactory.getLogger(BasicWADISessionManager.class);

    protected final ClassLoader cl;
    private final WADICluster cluster;
    protected final WADISessionManagerConfigInfo configInfo;
    protected final BackingStrategyFactory backingStrategyFactory;
    private final Collection<ClusteredServiceHolder> serviceHolders;
    private final CopyOnWriteArrayList<SessionListener> listeners;
    private final Map<SessionManagerListener, ServiceSpaceListener> sessionManagerListenerToAdapter;

    private Manager manager;
    private SessionMonitor sessionMonitor;
    private ServiceSpace serviceSpace;
    
    public BasicWADISessionManager(@ParamSpecial(type=SpecialAttributeType.classLoader) ClassLoader cl,
        @ParamAttribute(name=GBEAN_ATTR_WADI_CONFIG_INFO) WADISessionManagerConfigInfo configInfo,
        @ParamReference(name=GBEAN_REF_CLUSTER) WADICluster cluster,
        @ParamReference(name=GBEAN_REF_BACKING_STRATEGY_FACTORY) BackingStrategyFactory backingStrategyFactory,
        @ParamReference(name=GBEAN_REF_SERVICE_HOLDERS) Collection<ClusteredServiceHolder> serviceHolders) {
        if (null == cl) {
            throw new IllegalArgumentException("cl is required");
        } else if (null == configInfo) {
            throw new IllegalArgumentException("configInfo is required");
        } else if (null == cluster) {
            throw new IllegalArgumentException("cluster is required");
        } else if (null == backingStrategyFactory) {
            throw new IllegalArgumentException("backingStrategyFactory is required");
        }
        this.cl = cl;
        this.configInfo = configInfo;
        this.cluster = cluster;
        this.backingStrategyFactory = backingStrategyFactory;
        
        if (null == serviceHolders) {
            serviceHolders = Collections.emptySet();
        }
        this.serviceHolders = serviceHolders;

        listeners = new CopyOnWriteArrayList<SessionListener>();
        sessionManagerListenerToAdapter = new HashMap<SessionManagerListener, ServiceSpaceListener>();
    }

    public void doStart() throws Exception {
        Dispatcher underlyingDisp = cluster.getCluster().getDispatcher();
        
        ServiceSpaceName serviceSpaceName = new ServiceSpaceName(configInfo.getServiceSpaceURI());
        StackContext stackContext;
        if (configInfo.isDeltaReplication()) {
            stackContext = newAOPStackContext(underlyingDisp, serviceSpaceName);
        } else {
            stackContext = newStackContext(underlyingDisp, serviceSpaceName);
        }
        stackContext.setDisableReplication(configInfo.isDisableReplication());
        stackContext.build();

        serviceSpace = stackContext.getServiceSpace();
        
        manager = stackContext.getManager();

        sessionMonitor = stackContext.getSessionMonitor();
        sessionMonitor.addSessionListener(new SessionListenerAdapter());
        
        registerClusteredServices();

        serviceSpace.start();
    }

    public void doStop() throws Exception {
        serviceSpace.stop();
    }

    public void doFail() {
        try {
            serviceSpace.stop();
        } catch (Exception e) {
            log.error("Failed to stop", e);
        }
    }

    public Session createSession(String sessionId) throws SessionAlreadyExistException {
        org.codehaus.wadi.core.session.Session session;
        try {
            session = manager.createWithName(sessionId);
        } catch (org.codehaus.wadi.core.manager.SessionAlreadyExistException e) {
            throw new SessionAlreadyExistException("Session " + sessionId + " already exists", e);
        }
        return new WADISessionAdaptor(session);
    }

    public Manager getManager() {
        return manager;
    }

    public Cluster getCluster() {
        return cluster;
    }
    
    public Node getNode() {
        return cluster.getLocalNode();
    }

    public Set<Node> getRemoteNodes() {
        Set<Peer> peers = serviceSpace.getHostingPeers();
        return mapToNodes(peers);
    }

    public void registerListener(SessionListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(SessionListener listener) {
        listeners.remove(listener);
    }

    public void registerSessionManagerListener(SessionManagerListener listener) {
        ServiceSpaceListener adapter = new ServiceSpaceListenerAdapter(listener);
        serviceSpace.addServiceSpaceListener(adapter);
        synchronized (sessionManagerListenerToAdapter) {
            sessionManagerListenerToAdapter.put(listener, adapter);
        }
    }

    public void unregisterSessionManagerListener(SessionManagerListener listener) {
        ServiceSpaceListener adapter;
        synchronized (sessionManagerListenerToAdapter) {
            adapter = sessionManagerListenerToAdapter.remove(listener);
        }
        if (null == adapter) {
            throw new IllegalArgumentException("Listener [" + listener + "] is not registered");
        }
        serviceSpace.removeServiceSpaceListener(adapter);
    }

    public ServiceSpace getServiceSpace() {
        return serviceSpace;
    }

    protected StackContext newStackContext(Dispatcher underlyingDisp, ServiceSpaceName serviceSpaceName) {
        return new StackContext(cl,
            serviceSpaceName,
            underlyingDisp,
            configInfo.getSessionTimeoutSeconds(),
            configInfo.getNumPartitions(),
            configInfo.getSweepInterval(),
            backingStrategyFactory);
    }

    protected AOPStackContext newAOPStackContext(Dispatcher underlyingDisp, ServiceSpaceName serviceSpaceName) {
        return new AOPStackContext(cl,
            serviceSpaceName,
            underlyingDisp,
            configInfo.getSessionTimeoutSeconds(),
            configInfo.getNumPartitions(),
            configInfo.getSweepInterval(),
            backingStrategyFactory);
    }
    
    protected void registerClusteredServices() throws ServiceAlreadyRegisteredException {
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        for (ClusteredServiceHolder serviceHolder : serviceHolders) {
            serviceRegistry.register(serviceHolder.getServiceName(), serviceHolder.getService());
        }
    }

    protected Set<Node> mapToNodes(Set<Peer> peers) throws AssertionError {
        Set<Node> nodes = new HashSet<Node>();
        for (Peer peer : peers) {
            RemoteNode remoteNode = RemoteNode.retrieveAdaptor(peer);
            nodes.add(remoteNode);
        }
        return nodes;
    }

    protected Node mapToNode(Peer peer) throws AssertionError {
        return RemoteNode.retrieveAdaptor(peer);
    }

    protected void notifyInboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
        for (SessionListener listener : listeners) {
            listener.notifyInboundSessionMigration(new WADISessionAdaptor(session));
        }
    }

    protected void notifyOutboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
        for (SessionListener listener : listeners) {
            WADISessionAdaptor adaptor = WADISessionAdaptor.retrieveAdaptor(session);
            listener.notifyOutboundSessionMigration(adaptor);
        }
    }

    protected void notifySessionDestruction(org.codehaus.wadi.core.session.Session session) {
        for (SessionListener listener : listeners) {
            WADISessionAdaptor adaptor = WADISessionAdaptor.retrieveAdaptor(session);
            listener.notifySessionDestruction(adaptor);
        }
    }

    protected class SessionListenerAdapter implements org.codehaus.wadi.core.manager.SessionListener {

        public void onSessionCreation(org.codehaus.wadi.core.session.Session session) {
        }

        public void onSessionDestruction(org.codehaus.wadi.core.session.Session session) {
            notifySessionDestruction(session);
        }
        
        public void onInboundSessionMigration(org.codehaus.wadi.core.session.Session session) {
            notifyInboundSessionMigration(session);
        }
        
        public void onOutbountSessionMigration(org.codehaus.wadi.core.session.Session session) {
            notifyOutboundSessionMigration(session);
        }
        
    }

    protected class ServiceSpaceListenerAdapter implements ServiceSpaceListener {
        private final SessionManagerListener listener;
        
        public ServiceSpaceListenerAdapter(SessionManagerListener listener) {
            this.listener = listener;
        }

        public void receive(ServiceSpaceLifecycleEvent event, Set<Peer> newHostingPeers) {
            LifecycleState state = event.getState();
            if (state == LifecycleState.STARTED) {
                Set<Node> newHostingNodes = mapToNodes(newHostingPeers);
                Node joiningNode = mapToNode(event.getHostingPeer());
                listener.onJoin(joiningNode, newHostingNodes);
            } else if (state == LifecycleState.STOPPED || state == LifecycleState.FAILED) {
                Set<Node> newHostingNodes = mapToNodes(newHostingPeers);
                Node leavingNode = mapToNode(event.getHostingPeer());
                listener.onLeave(leavingNode, newHostingNodes);
            }
        }
    }
    
    public static final String GBEAN_ATTR_WADI_CONFIG_INFO = "wadiConfigInfo";
    public static final String GBEAN_REF_CLUSTER = "Cluster";
    public static final String GBEAN_REF_BACKING_STRATEGY_FACTORY = "BackingStrategyFactory";
    public static final String GBEAN_REF_SERVICE_HOLDERS = "ServiceHolders";
}
