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

package org.apache.geronimo.messaging.cluster;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanLifecycleController;
import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.pool.ClockPool;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/17 03:44:19 $
 */
public class ClusterHBTest
    extends TestCase
{

    private ClockPool cp;
    private long delay;
    private int nbMissed;
    
    private MockNode node1;
    private ClusterHBSender sender1;

    private MockNode node2;
    private ClusterHBSender sender2;
    
    private MockCluster cluster;
    
    private ClusterHBReceiver receiver;
    
    protected void setUp() throws Exception {
        cp = new ClockPool();
        cp.setPoolName("CP");
        cp.doStart();

        delay = 500;
        nbMissed = 2;

        InetAddress groupAddress = InetAddress.getByName("235.0.0.1");
        int port = 6667;

        InetAddress localhost = InetAddress.getLocalHost();
        
        ClusterInfo clusterInfo = new ClusterInfo(groupAddress, port);
        node1 = new MockNode();
        node1.setNodeInfo(new NodeInfo("node1", localhost, 1234));
        sender1 =
            new ClusterHBSender(node1, new MockCluster(clusterInfo), cp, delay,
                new MyMockController());
        
        node2 = new MockNode();
        node2.setNodeInfo(new NodeInfo("node2", localhost, 1234));
        sender2 =
            new ClusterHBSender(node2, new MockCluster(clusterInfo), cp, delay,
                new MyMockController());
        
        cluster = new MockCluster(clusterInfo);
        receiver = new ClusterHBReceiver(cluster, cp, nbMissed);
    }

    protected void tearDown() throws Exception {
        receiver.doStop();
        cp.doStop();
    }
    
    public void testRegister() throws Exception {
        sender1.doStart();
        receiver.doStart();

        Thread.sleep(delay * 2);
        assertEquals(1, cluster.nodes.size());
        assertEquals(node1.getNodeInfo(), cluster.nodes.get(0));

        sender2.doStart();

        Thread.sleep(delay * 2);
        assertEquals(2, cluster.nodes.size());
        assertEquals(node2.getNodeInfo(), cluster.nodes.get(1));

        sender2.doStop();
        sender1.doStop();
    }
    
    public void testUnregister() throws Exception {
        sender1.doStart();
        sender2.doStart();
        receiver.doStart();

        Thread.sleep(delay * 2);

        assertEquals(2, cluster.nodes.size());

        sender1.doStop();

        Thread.sleep(delay * (nbMissed + 1) );
        assertEquals(1, cluster.nodes.size());
        
        sender2.doStop();
        
        Thread.sleep(delay * (nbMissed + 1) );
        assertEquals(0, cluster.nodes.size());
    }
    
    private class MyMockController implements GBeanLifecycleController {
        public int getState() {
            return 0;
        }
        public void start() throws Exception {
        }
        public void stop() throws Exception {
        }
        public void fail() {
        }
    }
    
    private class MockCluster implements Cluster {
        private List nodes = Collections.synchronizedList(new ArrayList());
        private final ClusterInfo info;
        private MockCluster(ClusterInfo anInfo) {
            info = anInfo;
        }
        public Set getMembers() {
            return new HashSet(nodes);
        }
        public void addMember(NodeInfo aNode) {
            nodes.add(aNode);
        }
        public void removeMember(NodeInfo aNode) {
            nodes.remove(aNode);
        }
        public void addListener(ClusterEventListener aListener) {
        }
        public void removeListener(ClusterEventListener aListener) {
        }
        public ClusterInfo getClusterInfo() {
            return info;
        }
    }
    
}
