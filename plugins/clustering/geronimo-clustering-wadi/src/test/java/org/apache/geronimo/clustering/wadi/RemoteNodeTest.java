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

import java.util.HashMap;

import org.codehaus.wadi.group.Peer;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * 
 * @version $Rev$ $Date$
 */
public class RemoteNodeTest extends RMockTestCase {

    private Peer peer;
    private NodeService nodeService;
    private NodeConnectionInfo connectionInfo;

    @Override
    protected void setUp() throws Exception {
        peer = (Peer) mock(Peer.class);
        peer.getName();
        modify().multiplicity(expect.from(0)).returnValue("name");
        
        peer.getLocalStateMap();
        modify().returnValue(new HashMap());
        
        nodeService = (NodeService) mock(NodeService.class);

        connectionInfo = new NodeConnectionInfo("host", 1);
    }
    
    public void testGetName() throws Exception {
        startVerification();
        RemoteNode remoteNode = new RemoteNode(peer, nodeService);
        
        assertEquals(peer.getName(), remoteNode.getName());
    }
    
    public void testGetHost() throws Exception {
        nodeService.getConnectionInfo();
        modify().returnValue(connectionInfo);
        
        startVerification();
        RemoteNode remoteNode = new RemoteNode(peer, nodeService);
        
        assertEquals(connectionInfo.getHost(), remoteNode.getHost());
    }

    public void testGetPort() throws Exception {
        nodeService.getConnectionInfo();
        modify().returnValue(connectionInfo);
        
        startVerification();
        RemoteNode remoteNode = new RemoteNode(peer, nodeService);
        
        assertEquals(connectionInfo.getPort(), remoteNode.getPort());
    }
    
    public void testConnectionInfoIsInitializedOnlyOnce() throws Exception {
        nodeService.getConnectionInfo();
        modify().returnValue(connectionInfo);
        
        startVerification();
        RemoteNode remoteNode = new RemoteNode(peer, nodeService);
        
        assertEquals(connectionInfo.getHost(), remoteNode.getHost());
        assertEquals(connectionInfo.getPort(), remoteNode.getPort());
    }
    
}
