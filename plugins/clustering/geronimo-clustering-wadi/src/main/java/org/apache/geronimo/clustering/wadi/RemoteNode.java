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

import org.apache.geronimo.clustering.AbstractNode;
import org.codehaus.wadi.group.Peer;


/**
 *
 * @version $Rev$ $Date$
 */
public class RemoteNode extends AbstractNode {
    private static final String ADAPTOR_KEY = "ADAPTOR_KEY";

    public static RemoteNode retrieveOptionalAdaptor(Peer peer) {
        return (RemoteNode) peer.getLocalStateMap().get(ADAPTOR_KEY);
    }

    public static RemoteNode retrieveAdaptor(Peer peer) {
        RemoteNode node = (RemoteNode) peer.getLocalStateMap().get(ADAPTOR_KEY);
        if (null == node) {
            throw new IllegalStateException("No registered adaptor");
        }
        return node;
    }
    
    private final Peer peer;
    private final NodeService nodeService;
    private NodeConnectionInfo connectionInfo;
    
    public RemoteNode(Peer peer, NodeService nodeService) {
        super(peer.getName());
        if (null == nodeService) {
            throw new IllegalArgumentException("nodeService is required");
        }
        this.peer = peer;
        this.nodeService = nodeService;
        
        peer.getLocalStateMap().put(ADAPTOR_KEY, this);
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    protected String getHost() {
        if (null == connectionInfo) {
            connectionInfo = nodeService.getConnectionInfo();
        }
        return connectionInfo.getHost();
    }
    
    @Override
    protected int getPort() {
        if (null == connectionInfo) {
            connectionInfo = nodeService.getConnectionInfo();
        }
        return connectionInfo.getPort();
    }

}
