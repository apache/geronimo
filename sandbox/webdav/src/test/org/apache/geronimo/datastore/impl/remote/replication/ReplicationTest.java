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

package org.apache.geronimo.datastore.impl.remote.replication;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.ServerNode;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/03 15:27:32 $
 */
public class ReplicationTest extends TestCase {

    SimpleReplicatedMap replicant1;
    ReplicationMember replication1;
    ReplicationMember replication2;
    
    protected void setUp() throws Exception {
        replicant1 = new SimpleReplicatedMap();
        replication1 = new ReplicationMember("Replication1", new String[] {"Node2"});
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8080);
        ServerNode server1 = new ServerNode(nodeInfo1,
            Collections.singleton(replication1));
        server1.doStart();
        replication1.doStart();

        replication2 = new ReplicationMember("Replication1", new String[] {"Node1"});
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        ServerNode server2 = new ServerNode(nodeInfo2,
            Collections.singleton(replication2));
        server2.doStart();
        replication2.doStart();
        
        server2.join(nodeInfo1);
    }

    public void testUseCase() {
        replicant1.put("test1", "value1");
        replication1.registerReplicantCapable(replicant1);
        Object id = replicant1.getID();
        SimpleReplicatedMap replicant2 =
            (SimpleReplicatedMap) replication2.retrieveReplicantCapable(id);
        assertNotNull("Not been registered", replicant2);
        assertEquals("value1", replicant2.get("test1"));
        replicant1.put("test2", "value2");
        assertEquals("value2", replicant2.get("test2"));
        replicant1.remove("test1");
        assertNull(replicant2.get("test1"));
        Map tmp = new HashMap();
        tmp.put("test3", "value3");
        replicant1.putAll(tmp);
        assertEquals("value3", replicant2.get("test3"));
        replicant2.remove("test3");
        assertNull(replicant1.get("test3"));
    }
    
}
