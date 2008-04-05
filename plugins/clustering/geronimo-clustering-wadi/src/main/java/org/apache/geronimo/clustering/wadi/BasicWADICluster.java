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

import org.apache.geronimo.clustering.ClusterListener;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.codehaus.wadi.group.Peer;

/**
 * 
 * @version $Rev$ $Date$
 */
public class BasicWADICluster implements GBeanLifecycle, WADICluster {
    private final Node node;
    private final DispatcherHolder dispatcherHolder;
    private final NodeFactory nodeFactory;
    private final IdentityHashMap<ClusterListener, org.codehaus.wadi.group.ClusterListener> listenerToWADIListener;
    private final org.codehaus.wadi.group.ClusterListener wrapNodeAsPeerListener;
    
    private org.codehaus.wadi.group.Cluster cluster;
    
    public BasicWADICluster(@ParamReference(name=GBEAN_REF_NODE) Node node,
        @ParamReference(name=GBEAN_REF_DISPATCHER_HOLDER) DispatcherHolder dispatcherHolder) {
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
        
        listenerToWADIListener = new IdentityHashMap<ClusterListener, org.codehaus.wadi.group.ClusterListener>();
        
        wrapNodeAsPeerListener = new org.codehaus.wadi.group.ClusterListener() {
            public void onListenerRegistration(org.codehaus.wadi.group.Cluster cluster, Set existing) {
                wrapAsNode(existing);
            }
            
            public void onMembershipChanged(org.codehaus.wadi.group.Cluster cluster, Set joiners, Set leavers) {
                wrapAsNode(joiners);
            }
        };
    }
    
    public void doStart() throws Exception {
        cluster = dispatcherHolder.getDispatcher().getCluster();
        
        cluster.addClusterListener(wrapNodeAsPeerListener);
    }
    
    public void doStop() throws Exception {
        cluster.removeClusterListener(wrapNodeAsPeerListener);
        
        clearListeners();
    }

    public void doFail() {
        cluster.removeClusterListener(wrapNodeAsPeerListener);

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
        Set<Node> nodes = wrapAsNode(peers);
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
    
    protected Set<Node> wrapAsNode(Collection<Peer> peers) {
        Set<Node> nodes = new HashSet<Node>();
        for (Peer peer : peers) {
            Node node = wrapAsNode(peer);
            nodes.add(node);
        }
        return nodes;
    }

    protected Node wrapAsNode(Peer peer) {
        Node node = RemoteNode.retrieveOptionalAdaptor(peer);
        if (null == node) {
            node = newRemoteNode(peer);
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
            Set<Node> existingNodes = wrapAsNode(existing);
            listener.onListenerRegistration(BasicWADICluster.this, existingNodes);
        }
        
        public void onMembershipChanged(org.codehaus.wadi.group.Cluster cluster, Set joiners, Set leavers) {
            Set<Node> joinerNodes = wrapAsNode(joiners);
            Set<Node> leaverNodes = wrapAsNode(leavers);
            listener.onMembershipChanged(BasicWADICluster.this, joinerNodes, leaverNodes);
        }
        
    }
    
    public static final String GBEAN_REF_NODE = "Node";
    public static final String GBEAN_REF_DISPATCHER_HOLDER = "DispatcherHolder";
}
