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

import org.apache.geronimo.clustering.BasicNode;
import org.apache.geronimo.clustering.Cluster;
import org.apache.geronimo.clustering.ClusterListener;
import org.apache.geronimo.clustering.Node;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.group.LocalPeer;
import org.codehaus.wadi.group.Peer;

/**
 * 
 * @version $Rev$ $Date$
 */
public class WADICluster implements GBeanLifecycle, Cluster {
    private final DispatcherHolder dispatcherHolder;
    private org.codehaus.wadi.group.Cluster cluster;
    private final IdentityHashMap<ClusterListener, org.codehaus.wadi.group.ClusterListener> listenerToWADIListener;
    
    public WADICluster(DispatcherHolder dispatcherHolder) {
        if (null == dispatcherHolder) {
            throw new IllegalArgumentException("dispatcherHolder is required");
        }
        this.dispatcherHolder = dispatcherHolder;
        
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

    public String getName() {
        return cluster.getClusterName();
    }

    public Node getLocalNode() {
        LocalPeer localPeer = cluster.getLocalPeer();
        return new BasicNode(localPeer.getName());
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
            nodes.add(new BasicNode(peer.getName()));
        }
        return nodes;
    }

    protected class GeronimoClusterListenerAdaptor implements org.codehaus.wadi.group.ClusterListener {
        private final ClusterListener listener;

        public GeronimoClusterListenerAdaptor(ClusterListener listener) {
            this.listener = listener;
        }

        public void onListenerRegistration(org.codehaus.wadi.group.Cluster cluster, Set existing) {
            Set<Node> existingNodes = wrapAsNode(existing);
            listener.onListenerRegistration(WADICluster.this, existingNodes);
        }
        
        public void onMembershipChanged(org.codehaus.wadi.group.Cluster cluster, Set joiners, Set leavers) {
            Set<Node> joinerNodes = wrapAsNode(joiners);
            Set<Node> leaverNodes = wrapAsNode(leavers);
            listener.onMembershipChanged(WADICluster.this, joinerNodes, leaverNodes);
        }
        
    }
    
    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_DISPATCHER_HOLDER = "DispatcherHolder";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Cluster",
            WADICluster.class,
            NameFactory.GERONIMO_SERVICE);

        infoBuilder.addReference(GBEAN_REF_DISPATCHER_HOLDER, DispatcherHolder.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addInterface(Cluster.class);

        infoBuilder.setConstructor(new String[] {GBEAN_REF_DISPATCHER_HOLDER});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
