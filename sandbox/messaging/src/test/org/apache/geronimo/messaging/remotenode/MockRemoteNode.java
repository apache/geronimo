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

package org.apache.geronimo.messaging.remotenode;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Rev$ $Date$
 */
public class MockRemoteNode
    implements RemoteNode
{

    private NodeInfo nodeInfo;
    private List msgs = new ArrayList();
    
    public List getPushedMsg() {
        return msgs;
    }
    
    public void setNodeInfo(NodeInfo aNodeInfo) {
        nodeInfo = aNodeInfo;
    }
    
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setManager(RemoteNodeManager aManager) {
    }
    
    public void leave() {
    }
    
    public void join() throws NodeException {
    }

    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        return new DummyOutput();
    }

    private class DummyOutput implements MsgOutInterceptor {
        public void push(Msg aMsg) {
            msgs.add(aMsg);
        }
    }
    
}
