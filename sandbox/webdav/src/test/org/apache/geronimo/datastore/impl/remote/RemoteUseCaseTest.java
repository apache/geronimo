/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.datastore.impl.remote;

import java.io.File;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.Util;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.datastore.impl.local.AbstractUseCaseTest;
import org.apache.geronimo.datastore.impl.local.LocalGFileManager;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeImpl;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.network.NetworkTransportFactory;
import org.apache.geronimo.messaging.proxy.EndPointProxyInfo;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;

/**
 * This is a remote use-case.
 *
 * @version $Rev$ $Date$
 */
public class RemoteUseCaseTest extends AbstractUseCaseTest {

    private ProtocolContext ctx1;
    private Node node1;

    private ProtocolContext ctx2;
    private Node node2;
    
    /**
     * In this set-up one initializes two nodes, namely Node1 and Node2. A
     * local GFileManager is mounted by Node1. A client GFileManager is mounted
     * by Node2. Node2 joins Node1.
     */
    protected void setUp() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"),
            "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        
        LockManager lockManager = new LockManager();

        InetAddress address = InetAddress.getLocalHost();
        final NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8080);
        final NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);

        String fileSystem = "FileSystem";
        
        // Set-up the first Node.
        ctx1 = new ProtocolContext();
        ctx1.init("Node1");
        ctx1.start();
        node1 = new NodeImpl(nodeInfo1, ctx1.tp, ctx1.cp, ctx1.factoryTransport());
        GFileManager localManager =
            new LocalGFileManager(fileSystem, root, lockManager);
        GFileManagerProxy proxy =
            new GFileManagerProxy(node1, localManager);
        node1.doStart();
        proxy.doStart();
        localManager.doStart();
        
        // Set-up the second ServerNode.
        ctx2 = new ProtocolContext();
        ctx2.init("Node2");
        ctx2.start();
        node2 = new NodeImpl(nodeInfo2, ctx2.tp, ctx2.cp, ctx2.factoryTransport());
        EndPointProxyInfo proxyInfo =
            new EndPointProxyInfo(fileSystem,
                new Class[] {GFileManager.class},
                new NodeInfo[] {nodeInfo1});
        fileManager = (GFileManager) node2.factoryEndPointProxy(proxyInfo);
        node2.doStart();
        
        // Sets the topology.
        NodeTopology topology = new MockTopology(nodeInfo1, nodeInfo2);
        node1.setTopology(topology);
    }
    
    protected void tearDown() throws Exception {
        node2.doStop();
        ctx2.stop();
        
        node1.doStop();
        ctx1.stop();
    }
    
    private static class  ProtocolContext {
        private ThreadPool tp;
        private ClockPool cp;
        private SelectorManager sm;
        public MessagingTransportFactory factoryTransport() {
            return new NetworkTransportFactory(sm, cp);
        }
        public void init(String aName) throws Exception {
            tp = new ThreadPool();
            tp.setKeepAliveTime(1 * 1000);
            tp.setMinimumPoolSize(5);
            tp.setMaximumPoolSize(25);
            tp.setPoolName("TP " + aName);

            cp = new ClockPool();
            cp.setPoolName("CP " + aName);

            sm = new SelectorManager();
            sm.setThreadPool(tp);
            sm.setThreadName("SM " + aName);
            sm.setTimeout(500);
        }
        public void start() throws Exception {
            tp.doStart();
            cp.doStart();
            sm.doStart();
        }
        public void stop() throws Exception {
            sm.doStop();
            cp.doStop();
            tp.doStop();
        }
    }
    
    private static class MockTopology implements NodeTopology {

        private final NodeInfo nodeInfo1;
        private final NodeInfo nodeInfo2;

        public int getVersion() {
            return 0;
        }
        
        private MockTopology(NodeInfo aNodeInfo1, NodeInfo aNodeInfo2) {
            nodeInfo1 = aNodeInfo1;
            nodeInfo2 = aNodeInfo2;
        }
        
        public Set getNeighbours(NodeInfo aRoot) {
            Set result = new HashSet();
            if ( aRoot.equals(nodeInfo1) ) {
                result.add(nodeInfo2);
            } else if ( aRoot.equals(nodeInfo2) ) {
            } else {
                throw new IllegalArgumentException("Not expected");
            }
            return result;
        }
        public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
            if ( aSource.equals(nodeInfo1) && aTarget.equals(nodeInfo2) ) {
                return new NodeInfo[] {nodeInfo2};
            } else if ( aSource.equals(nodeInfo2) && aTarget.equals(nodeInfo1) ) {
                return new NodeInfo[] {nodeInfo1};
            }
            throw new IllegalArgumentException("Not expected");
        }
        public int getIDOfNode(NodeInfo aNodeInfo) {
            if ( aNodeInfo.equals(nodeInfo1) ) {
                return 1;
            } else if ( aNodeInfo.equals(nodeInfo2) ) {
                return 2;
            }
            throw new IllegalArgumentException("Not expected");
        }
        public NodeInfo getNodeById(int anId) {
            switch (anId) {
                case 1:
                    return nodeInfo1;
                case 2:
                    return nodeInfo2;
                default:
                    throw new IllegalArgumentException("Not expected");
            }
        }
        public Set getNodes() {
            throw new IllegalArgumentException("Not expected");
        }
        
    }
    
}
