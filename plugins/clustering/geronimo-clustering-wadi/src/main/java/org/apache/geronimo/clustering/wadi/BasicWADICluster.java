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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import org.apache.geronimo.clustering.Cluster;
import org.apache.geronimo.clustering.ClusterListener;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.group.Peer;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicWADICluster implements GBeanLifecycle, WADICluster {
    private final Node node;
    private final DispatcherHolder dispatcherHolder;
    private final NodeFactory nodeFactory;
    private final IdentityHashMap<Peer, Node> peerToNode;
    private final IdentityHashMap<ClusterListener, org.codehaus.wadi.group.ClusterListener> listenerToWADIListener;
    
    private org.codehaus.wadi.group.Cluster cluster;
    
    public BasicWADICluster(Node node, DispatcherHolder dispatcherHolder) {
        this(node, dispatcherHolder, new NodeProxyFactory());
    }

    public BasicWADICluster(Node node, DispatcherHolder dispatcherHolder, NodeFactory nodeFactory) {
        if (null == node) {
            throw new IllegalArgumentException("node is required");
        } else if (null == dispatcherHolder) {
            throw new IllegalArgumentException("dispatcherHolder is required");
        }
        this.node = node;
        this.dispatcherHolder = dispatcherHolder;
        this.nodeFactory = nodeFactory;
        
        peerToNode = new IdentityHashMap<Peer, Node>();
        listenerToWADIListener = new IdentityHashMap<ClusterListener, org.codehaus.wadi.group.ClusterListener>();
    }
    
    public void doStart() throws Exception {
        cluster = dispatcherHolder.getDispatcher().getCluster();
    }
    
    public void doStop() throws Exception {
        clearListeners();
    }

    public void doFail() {
        clearListeners();
    }

    public org.codehaus.wadi.group.Cluster getCluster() {
        return cluster;
    }
    
    public String getName() {
        return cluster.getClusterName();
    }

    public Node getLocalNode() {
        return node;
    }
    
    public Set<Node> getRemoteNodes() {
        Collection<Peer> peers = cluster.getRemotePeers().values();
        Set<Node> nodes = wrapAsNode(peers, false);
        return nodes;
    }

    public void addClusterListener(ClusterListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener is required");
        }
        GeronimoClusterListenerAdaptor wadiListener = new GeronimoClusterListenerAdaptor(listener);
        listenerToWADIListener.put(listener, wadiListener);
        cluster.addClusterListener(wadiListener);
    }
    
    public void removeClusterListener(ClusterListener listener) {
        org.codehaus.wadi.group.ClusterListener wadiListener = listenerToWADIListener.remove(listener);
        if (null == wadiListener) {
            throw new IllegalArgumentException(listener + " is not registered");
        }
        cluster.removeClusterListener(wadiListener);
    }
    
    protected void clearListeners() {
        for (org.codehaus.wadi.group.ClusterListener wadiListener : listenerToWADIListener.values()) {
            cluster.removeClusterListener(wadiListener);
        }
        
        listenerToWADIListener.clear();
    }
    
    protected Set<Node> wrapAsNode(Collection<Peer> peers, boolean remove) {
        Set<Node> nodes = new HashSet<Node>();
        for (Peer peer : peers) {
            Node node = wrapAsNode(peer, remove);
            nodes.add(node);
        }
        return nodes;
    }

    protected Node wrapAsNode(Peer peer, boolean remove) {
        Node node;
        synchronized (peerToNode) {
            if (remove) {
                node = peerToNode.remove(peer);
                if (null == node) {
                    throw new AssertionError("no node mapped to peer");
                }
            } else {
                node = peerToNode.get(peer);
                if (null == node) {
                    node = newRemoteNode(peer);
                    peerToNode.put(peer, node);
                }
            }
        }
        return node;
    }

    protected Node newRemoteNode(Peer peer) {
        return nodeFactory.newNode(cluster, peer);
    }

    protected class GeronimoClusterListenerAdaptor implements org.codehaus.wadi.group.ClusterListener {
        private final ClusterListener listener;

        public GeronimoClusterListenerAdaptor(ClusterListener listener) {
            this.listener = listener;
        }

        public void onListenerRegistration(org.codehaus.wadi.group.Cluster cluster, Set existing) {
            Set<Node> existingNodes = wrapAsNode(existing, false);
            listener.onListenerRegistration(BasicWADICluster.this, existingNodes);
        }
        
        public void onMembershipChanged(org.codehaus.wadi.group.Cluster cluster, Set joiners, Set leavers) {
            Set<Node> joinerNodes = wrapAsNode(joiners, false);
            Set<Node> leaverNodes = wrapAsNode(leavers, true);
            listener.onMembershipChanged(BasicWADICluster.this, joinerNodes, leaverNodes);
        }
        
    }
    
    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_NODE = "Node";
    public static final String GBEAN_REF_DISPATCHER_HOLDER = "DispatcherHolder";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Cluster",
            BasicWADICluster.class,
            NameFactory.GERONIMO_SERVICE);

        infoBuilder.addReference(GBEAN_REF_NODE, Node.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference(GBEAN_REF_DISPATCHER_HOLDER, DispatcherHolder.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(Cluster.class);

        infoBuilder.setConstructor(new String[] { GBEAN_REF_NODE, GBEAN_REF_DISPATCHER_HOLDER });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
