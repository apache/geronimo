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

package org.apache.geronimo.messaging.replication;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.EndPointUtil;
import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.NodeInfo;

/**
 *
 * @version $Rev$ $Date$
 */
public class ReplicationMemberImplTest
    extends TestCase
{

    private ReplicationMember primary;
    private ReplicationMember secondary;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo dummy = new NodeInfo("dummy", address, 8081);
        NodeInfo[] targets = new NodeInfo[] {dummy};
        
        primary = new ReplicationMemberImpl(new MockNode(), "Group", targets);
        secondary = new ReplicationMemberImpl(new MockNode(), "Group", targets);
        
        EndPointUtil.interConnect(primary, secondary);
    }
    
    public void testRegisterReplicantCapable() {
        SimpleReplicatedMap primaryMap = new SimpleReplicatedMap();
        primary.registerReplicantCapable(primaryMap);
        Object replicantID = primaryMap.getID();
        
        SimpleReplicatedMap secondaryMap = (SimpleReplicatedMap)
            secondary.retrieveReplicantCapable(replicantID);
        assertNotNull(secondaryMap);
    }
    
    public void testMergeWithUpdate() {
        SimpleReplicatedMap primaryMap = new SimpleReplicatedMap();
        primary.registerReplicantCapable(primaryMap);
        Object replicantID = primaryMap.getID();
        
        SimpleReplicatedMap secondaryMap = (SimpleReplicatedMap)
            secondary.retrieveReplicantCapable(replicantID);
        
        Integer key = new Integer(1);
        Object value = new Object();
        primaryMap.put(key, value);
        
        assertTrue(secondaryMap.containsKey(key));
        assertEquals(value, secondaryMap.get(key));
    }
    
    
}
