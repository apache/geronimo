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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class MockRemoteNode implements RemoteNode
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

    public void setTopology(NodeTopology aTopology) {
    }

    public void connect() throws IOException, CommunicationException {
    }

    public void leave() throws IOException, CommunicationException {
    }

    public void addConnection(RemoteNodeConnection aConnection) {
    }

    public void removeConnection(RemoteNodeConnection aConnection) {
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
