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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.clustering.SessionManagerListener;
import org.apache.geronimo.clustering.wadi.RemoteNode;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.openejb.EjbDeploymentIdAccessor;
import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.servicespace.InvocationMetaData;
import org.codehaus.wadi.servicespace.ServiceNotAvailableException;
import org.codehaus.wadi.servicespace.ServiceNotFoundException;
import org.codehaus.wadi.servicespace.ServiceProxy;
import org.codehaus.wadi.servicespace.ServiceProxyFactory;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class NetworkConnectorMonitor implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(NetworkConnectorMonitor.class);
    
    private final Collection<NetworkConnector> connectors;
    private final Collection<EjbDeploymentIdAccessor> idAccessors;
    private final WADISessionManager sessionManager;
    private final String nodeName;
    private final Set<URI> locations;
    private NetworkConnectorTracker localTracker;
    private ServiceProxyFactory trackerProxyFactory;
    
    public NetworkConnectorMonitor(Collection<NetworkConnector> connectors,
        Collection<EjbDeploymentIdAccessor> idAccessors,
        WADISessionManager sessionManager) {
        if (null == connectors && !(connectors instanceof ReferenceCollection)) {
            throw new IllegalArgumentException("connectors must be a [" + ReferenceCollection.class + "]");
        } else if (null == idAccessors && !(idAccessors instanceof ReferenceCollection)) {
            throw new IllegalArgumentException("idAccessors must be a [" + ReferenceCollection.class + "]");
        } else if (null == sessionManager) {
            throw new IllegalArgumentException("sessionManager is required");
        }
        this.connectors = connectors;
        this.idAccessors = idAccessors;
        this.sessionManager = sessionManager;
        
        nodeName = sessionManager.getCluster().getLocalNode().getName();
        
        locations = new HashSet<URI>();

        registerListenerForMembershipUpdates(sessionManager);
        registerListenerForConnectorUpdates((ReferenceCollection) connectors);
        registerListenerForDeploymentUpdates((ReferenceCollection) idAccessors);
    }

    protected void registerListenerForMembershipUpdates(WADISessionManager sessionManager) {
        sessionManager.registerSessionManagerListener(new SessionManagerListener() {
            public void onJoin(Node joiningNode, Set<Node> newHostingNodes) {
                Set<URI> clonedLocations;
                synchronized (locations) {
                    clonedLocations = new HashSet<URI>(locations);
                }
                for (EjbDeploymentIdAccessor deploymentIdAccessor : NetworkConnectorMonitor.this.idAccessors) {
                    String deploymentId = deploymentIdAccessor.getDeploymentId();
                    ServiceProxy proxy = trackerProxyFactory.getProxy();
                    RemoteNode remoteNode = (RemoteNode) joiningNode;
                    proxy.getInvocationMetaData().setTargets(new Peer[] {remoteNode.getPeer()});
                    NetworkConnectorTracker tracker = (NetworkConnectorTracker)  proxy;
                    tracker.registerNetworkConnectorLocations(deploymentId, nodeName, clonedLocations);
                }
            }

            public void onLeave(Node leavingNode, Set<Node> newHostingNodes) {
                String leavingNodeName = leavingNode.getName();
                localTracker.unregisterNetworkConnectorLocations(leavingNodeName);
            }
        });
    }

    protected void registerListenerForDeploymentUpdates(ReferenceCollection deploymentIdAccessors) {
        deploymentIdAccessors.addReferenceCollectionListener(new ReferenceCollectionListener() {
            public void memberAdded(ReferenceCollectionEvent event) {
                EjbDeploymentIdAccessor idAccessor = (EjbDeploymentIdAccessor) event.getMember();
                String deploymentId = idAccessor.getDeploymentId();
                updateTracker(deploymentId);
            }

            public void memberRemoved(ReferenceCollectionEvent event) {
                EjbDeploymentIdAccessor idAccessor = (EjbDeploymentIdAccessor) event.getMember();
                String deploymentId = idAccessor.getDeploymentId();
                removeTracker(deploymentId);
            }
        });
    }

    protected void registerListenerForConnectorUpdates(ReferenceCollection connectors) {
        connectors.addReferenceCollectionListener(new ReferenceCollectionListener() {
            public void memberAdded(ReferenceCollectionEvent event) {
                URI uri = buildURI(event);
                synchronized (locations) {
                    locations.add(uri);
                }
                Set<URI> uris = new HashSet<URI>();
                uris.add(uri);
                for (EjbDeploymentIdAccessor deploymentIdAccessor : idAccessors) {
                    String deploymentId = deploymentIdAccessor.getDeploymentId();
                    updateTracker(deploymentId, uris);
                }
            }

            public void memberRemoved(ReferenceCollectionEvent event) {
                URI uri = buildURI(event);
                synchronized (locations) {
                    locations.remove(uri);
                }
                Set<URI> uris = new HashSet<URI>();
                uris.add(uri);
                for (EjbDeploymentIdAccessor deploymentIdAccessor : idAccessors) {
                    String deploymentId = deploymentIdAccessor.getDeploymentId();
                    removeTracker(deploymentId, uris);
                }
            }

            protected URI buildURI(ReferenceCollectionEvent event) {
                NetworkConnector connector = (NetworkConnector) event.getMember();
                return NetworkConnectorMonitor.this.buildURI(connector);
            }
        });
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            log.error("See nested", e);
        }
    }

    public void doStart() throws Exception {
        trackerProxyFactory = newNetworkConnectorTrackerProxy();
        
        localTracker = locateLocalNetworkConnectorTracker();

        for (NetworkConnector connector : connectors) {
            URI uri = buildURI(connector);
            synchronized (locations) {
                locations.add(uri);
            }
        }

        for (EjbDeploymentIdAccessor deploymentIdAccessor : idAccessors) {
            String deploymentId = deploymentIdAccessor.getDeploymentId();
            updateTracker(deploymentId);
        }
    }

    public void doStop() throws Exception {
        synchronized (locations) {
            locations.clear();
        }
    }

    protected NetworkConnectorTracker locateLocalNetworkConnectorTracker() {
        ServiceSpace serviceSpace = sessionManager.getServiceSpace();
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        try {
            return (NetworkConnectorTracker) serviceRegistry.getStartedService(NetworkConnectorTracker.NAME);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    protected URI buildURI(NetworkConnector connector) {
        String uriAsString = connector.getProtocol() + "://" + connector.getHost() + ":" + connector.getPort();
        URI uri;
        try {
            uri = new URI(uriAsString);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("URI [" + uriAsString + "] is invalid", e);
        }
        return uri;
    }

    protected ServiceProxyFactory newNetworkConnectorTrackerProxy() {
        ServiceSpace serviceSpace = sessionManager.getServiceSpace();
        ServiceProxyFactory proxyFactory = serviceSpace.getServiceProxyFactory(NetworkConnectorTracker.NAME,
            new Class[] { NetworkConnectorTracker.class });
        InvocationMetaData invocationMetaData = proxyFactory.getInvocationMetaData();
        invocationMetaData.setOneWay(true);
        return proxyFactory;
    }

    protected void updateTrackerForAllDeploymentIds() {
        for (EjbDeploymentIdAccessor deploymentIdAccessor : idAccessors) {
            String deploymentId = deploymentIdAccessor.getDeploymentId();
            updateTracker(deploymentId);
        }
    }

    protected void updateTracker(Object deploymentId) {
        Set<URI> clonedLocations;
        synchronized (locations) {
            clonedLocations = new HashSet<URI>(locations);
        }
        updateTracker(deploymentId, clonedLocations);
    }
    
    protected void updateTracker(Object deploymentId, Set<URI> locationsToRegister) {
        NetworkConnectorTracker tracker = (NetworkConnectorTracker) trackerProxyFactory.getProxy();
        tracker.registerNetworkConnectorLocations(deploymentId, nodeName, locationsToRegister);
    }
    
    protected void removeTracker(Object deploymentId) {
        Set<URI> clonedLocations;
        synchronized (locations) {
            clonedLocations = new HashSet<URI>(locations);
        }
        removeTracker(deploymentId, clonedLocations);
    }

    protected void removeTracker(Object deploymentId, Set<URI> locationsToUnregister) {
        NetworkConnectorTracker tracker = (NetworkConnectorTracker) trackerProxyFactory.getProxy();
        tracker.unregisterNetworkConnectorLocations(deploymentId, nodeName, locationsToUnregister);
    }
    
    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_NETWORK_CONNECTORS = "NetworkConnectors";
    public static final String GBEAN_REF_EJB_DEP_ID_ACCESSOR = "EjbDeploymentIdAccessor";
    public static final String GBEAN_REF_WADI_SESSION_MANAGER = "WADISessionManager";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("NetworkConnectorMonitor",
            NetworkConnectorMonitor.class,
            GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        infoBuilder.addReference(GBEAN_REF_NETWORK_CONNECTORS, NetworkConnector.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addReference(GBEAN_REF_EJB_DEP_ID_ACCESSOR, EjbDeploymentIdAccessor.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addReference(GBEAN_REF_WADI_SESSION_MANAGER, WADISessionManager.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        infoBuilder.setConstructor(new String[] {GBEAN_REF_NETWORK_CONNECTORS,
            GBEAN_REF_EJB_DEP_ID_ACCESSOR,
            GBEAN_REF_WADI_SESSION_MANAGER});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}