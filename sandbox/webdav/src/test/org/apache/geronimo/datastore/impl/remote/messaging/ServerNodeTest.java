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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.datastore.impl.remote.messaging.Topology.NodePath;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.PathWeight;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:13 $
 */
public class ServerNodeTest
    extends TestCase
{

    ServerNode server1;
    ServerNode server2;
    ServerNode server3;
    ServerNode server4;
    
    DummyConnector connector1;
    DummyConnector connector11;
    DummyConnector connector2;
    DummyConnector connector21;
    DummyConnector connector4;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        NodeInfo nodeInfo3 = new NodeInfo("Node3", address, 8083);
        NodeInfo nodeInfo4 = new NodeInfo("Node4", address, 8084);
        
        connector1 =
            new DummyConnector("dummy1", new NodeInfo[] {nodeInfo2, nodeInfo4});
        connector11 =
            new DummyConnector("dummy11", new NodeInfo[] {nodeInfo2});
        Collection connectors = new ArrayList();
        connectors.add(connector1);
        connectors.add(connector11);
        server1 = new ServerNode(nodeInfo1, connectors);
        server1.doStart();

        connector2 =
            new DummyConnector("dummy1", new NodeInfo[] {nodeInfo1, nodeInfo4});
        connector21 =
            new DummyConnector("dummy11", new NodeInfo[] {nodeInfo1});
        connectors = new ArrayList();
        connectors.add(connector2);
        connectors.add(connector21);
        server2 = new ServerNode(nodeInfo2, connectors);
        server2.doStart();
        server2.join(nodeInfo1);
        
        server3 = new ServerNode(nodeInfo3, Collections.EMPTY_LIST);
        server3.doStart();
        server3.join(nodeInfo2);

        connector4 =
            new DummyConnector("dummy1", new NodeInfo[] {nodeInfo1, nodeInfo2});
        server4 = new ServerNode(nodeInfo4, Collections.singleton(connector4));
        server4.doStart();
        server4.join(nodeInfo3);
        
        Topology topology = new Topology();
        PathWeight weight = new PathWeight(10);
        NodePath path = new NodePath(nodeInfo1, nodeInfo2, weight, weight);
        topology.addPath(path);
        path = new NodePath(nodeInfo2, nodeInfo3, weight, weight);
        topology.addPath(path);
        path = new NodePath(nodeInfo3, nodeInfo4, weight, weight);
        topology.addPath(path);
        server1.setTopology(topology);
        server2.setTopology(topology);
        server3.setTopology(topology);
        server4.setTopology(topology);
    }

    protected void tearDown() throws Exception {
        server4.doStop();
        server3.doStop();
        server2.doStop();
        server1.doStop();
    }
    
    public void testMulticast() throws Exception {
        connector1.sendRawObject("Test1");
        List list = connector2.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
        list = connector4.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
    }

    public static void main(String[] args) throws Exception {
        ServerNodeTest test = new ServerNodeTest();
        test.setUp();
        while ( true ) {
            test.testInputStreamPerformance();
        }
    }
    
    public void testSendRawPerformance() throws Exception {
        List list = connector21.getReceived();
        int iter = 1000;
        long start = System.currentTimeMillis();
        for(int i = 0; i < iter; i++) {
            connector11.sendRawObject(null);
            assertEquals(1, list.size());
            list.remove(0);
        }
        long end = System.currentTimeMillis();
        System.out.println("#calls={" + iter + "}; Time={" + (end-start) + "}");
        // TODO update when compression is implemented.
        assertTrue((end - start) < 3000);
    }
    
    public void testInputStreamPerformance() throws Exception {
        long nbBytes = 1024 * 1024;
        InputStream in = new DummyInputStream(nbBytes);
        long baseLine = timeRead(in);
        
        in = new DummyInputStream(nbBytes);
        connector11.sendRawObject(in);
        List list = connector21.getReceived();
        assertEquals(1, list.size());
        in = (InputStream) list.remove(0);
        long time = timeRead(in);
        System.out.println("#bytes={" + nbBytes +
            "}; Baseline={" + baseLine + "}; Time={" + time + "}");
        // TODO update when compression is implemented.
        assertTrue(baseLine * 100 > time);
    }

    private long timeRead(InputStream anIn)
        throws Exception {
        int read;
        long start = System.currentTimeMillis();
        while ( -1 != (read = anIn.read() ) ) {}
        return System.currentTimeMillis() - start;
    }
    
    private static class DummyInputStream extends InputStream {
        private final long size;
        private long curPos = 0;
        private DummyInputStream(long aSize) {
            size = aSize;
        }
        public int read() throws IOException {
            if ( curPos++ < size ) {
                return 1;
            }
            return -1;
        }
    }
    
}
