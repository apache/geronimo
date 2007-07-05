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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.clustering.BasicNode;
import org.apache.geronimo.clustering.ClusterListener;
import org.apache.geronimo.clustering.Node;
import org.codehaus.wadi.group.Address;
import org.codehaus.wadi.group.LocalPeer;
import org.codehaus.wadi.group.Peer;

import com.agical.rmock.core.Action;
import com.agical.rmock.core.MethodHandle;
import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusterTest extends RMockTestCase {

    private org.codehaus.wadi.group.Cluster wadiCluster;
    private LocalPeer localPeer;
    private Peer peer1;
    private Peer peer2;
    private WADICluster cluster;
    private ClusterListener listener;
    
    @Override
    protected void setUp() throws Exception {
        DispatcherHolder dispatcherHolder = (DispatcherHolder) mock(DispatcherHolder.class);

        wadiCluster = dispatcherHolder.getDispatcher().getCluster();
        wadiCluster.getClusterName();
        modify().multiplicity(expect.from(0)).returnValue("name");

        localPeer = wadiCluster.getLocalPeer();
        modify().multiplicity(expect.from(0));
        localPeer.getName();
        modify().multiplicity(expect.from(0)).returnValue("localPeerName");
        
        Map<Address, Peer> remotePeers = new HashMap<Address, Peer>();
        peer1 = addPeer("peer1", remotePeers);
        peer2 = addPeer("peer2", remotePeers);
        
        wadiCluster.getRemotePeers();
        modify().multiplicity(expect.from(0)).returnValue(remotePeers);
        
        cluster = new WADICluster(dispatcherHolder);
        listener = (ClusterListener) mock(ClusterListener.class);
    }

    private Peer addPeer(String peerName, Map<Address, Peer> remotePeers) {
        Peer peer = (Peer) mock(Peer.class, peerName);

        Address address = peer.getAddress();
        modify().multiplicity(expect.from(0));
        peer.getName();
        modify().multiplicity(expect.from(0)).returnValue(peerName);
        
        remotePeers.put(address, peer);
        
        return peer;
    }
    
    public void testGetName() throws Exception {
        startVerification();
        cluster.doStart();
        
        assertEquals(wadiCluster.getClusterName(), cluster.getName());
    }
    
    public void testGetLocalNode() throws Exception {
        startVerification();
        cluster.doStart();
        
        assertEquals(localPeer.getName(), cluster.getLocalNode().getName());
    }
    
    public void testGetRemotePeers() throws Exception {
        startVerification();
        cluster.doStart();

        Set<Node> remoteNodes = cluster.getRemoteNodes();
        assertEquals(2, remoteNodes.size());
        assertTrue(remoteNodes.contains(new BasicNode(peer1.getName())));
        assertTrue(remoteNodes.contains(new BasicNode(peer2.getName())));
    }
    
    public void testAddClusterListener() throws Exception {
        wadiCluster.addClusterListener(null);
        modify().args(new AbstractExpression() {

            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }

            public boolean passes(Object arg0) {
                assertTrue(arg0 instanceof org.codehaus.wadi.group.ClusterListener);
                return true;
            }
            
        });
        
        startVerification();
        cluster.doStart();
        
        cluster.addClusterListener(listener);
    }
    
    public void testAddNullClusterListenerThrowsException() throws Exception {
        startVerification();
        cluster.doStart();
        
        try {
            cluster.addClusterListener(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testRemoveClusterListener() throws Exception {
        AbstractExpression assertSame = new AssertSameWADIListener();
        wadiCluster.addClusterListener(null);
        modify().args(assertSame);
        wadiCluster.removeClusterListener(null);
        modify().args(assertSame);
        
        startVerification();
        cluster.doStart();

        cluster.addClusterListener(listener);
        cluster.removeClusterListener(listener);
    }
    
    public void testRemoveUndefinedClusterListenerThrowsException() throws Exception {
        startVerification();
        cluster.doStart();

        try {
            cluster.removeClusterListener(listener);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testClusterListenerRegistrationCallback() throws Exception {
        Set<Node> existing = new HashSet<Node>();
        existing.add(new BasicNode("peer1"));
        listener.onListenerRegistration(cluster, existing);
        
        wadiCluster.addClusterListener(null);
        modify().args(is.ANYTHING).perform(new Action() {

            public Object invocation(Object[] arg0, MethodHandle arg1) throws Throwable {
                org.codehaus.wadi.group.ClusterListener wadiListener = (org.codehaus.wadi.group.ClusterListener) arg0[0];
                wadiListener.onListenerRegistration(wadiCluster, Collections.singleton(peer1));;
                return null;
            }
            
        });

        startVerification();
        cluster.doStart();
        
        cluster.addClusterListener(listener);
    }
    
    public void testClusterListenerMembershipChangeCallback() throws Exception {
        Set<Node> joiners = new HashSet<Node>();
        joiners.add(new BasicNode("peer1"));
        Set<Node> leavers = new HashSet<Node>();
        leavers.add(new BasicNode("peer2"));
        listener.onMembershipChanged(cluster, joiners, leavers);
        
        wadiCluster.addClusterListener(null);
        modify().args(is.ANYTHING).perform(new Action() {

            public Object invocation(Object[] arg0, MethodHandle arg1) throws Throwable {
                org.codehaus.wadi.group.ClusterListener wadiListener = (org.codehaus.wadi.group.ClusterListener) arg0[0];
                wadiListener.onMembershipChanged(wadiCluster, Collections.singleton(peer1), Collections.singleton(peer2));
                return null;
            }
            
        });

        startVerification();
        cluster.doStart();
        
        cluster.addClusterListener(listener);
    }
    
    public void testRemoveListenersOnStopOrFail() throws Exception {
        AbstractExpression assertSame = new AssertSameWADIListener();
        wadiCluster.addClusterListener(null);
        modify().args(assertSame);
        wadiCluster.removeClusterListener(null);
        modify().args(assertSame);
        
        startVerification();
        cluster.doStart();
        
        cluster.addClusterListener(listener);
        cluster.doStop();
    }
    
    private final class AssertSameWADIListener extends AbstractExpression {
        private org.codehaus.wadi.group.ClusterListener wadiListener;

        public void describeWith(ExpressionDescriber arg0) throws IOException {
        }

        public boolean passes(Object arg0) {
            if (null == wadiListener) {
                wadiListener = (org.codehaus.wadi.group.ClusterListener) arg0;
            } else {
                assertSame(wadiListener, arg0);
            }
            return true;
        }
    }

}
