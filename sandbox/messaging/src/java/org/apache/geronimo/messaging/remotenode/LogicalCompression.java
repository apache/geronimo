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

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgBody;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.io.PopSynchronization;
import org.apache.geronimo.messaging.io.PushSynchronization;
import org.apache.geronimo.messaging.io.StreamInputStream;
import org.apache.geronimo.messaging.io.StreamOutputStream;

/**
 * Logical compression of Msgs.
 * <BR>
 * Its goal is to compress Msgs to be sent to other nodes. The compression is
 * based on a shared knowledge such as a Topology.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/24 23:52:12 $
 */
public class LogicalCompression
    implements PopSynchronization, PushSynchronization
{

    /**
     * Topology shared knowledge.   
     */
    private NodeTopology topology;
    
    /**
     * No logical compression.
     */
    private final static byte NULL = (byte) 0;
    
    /**
     * Compression based on the Topology shared knowledge.
     */
    private final static byte TOPOLOGY = (byte) 1;

    /**
     * Identifies a request.
     */
    private final static byte REQUEST = (byte) 0;
    
    /**
     * Identifies a response.
     */
    private final static byte RESPONSE = (byte) 1;

    public NodeTopology getTopology() {
        return topology;
    }

    public void setTopology(NodeTopology aTopology) {
        topology = aTopology;
    }
    
    public Object beforePop(StreamInputStream anIn)
        throws IOException {
        List result = new ArrayList(5);
        int bodyType = anIn.readByte();
        if ( REQUEST == bodyType ) {
            result.add(MsgBody.Type.REQUEST);
        } else {
            result.add(MsgBody.Type.RESPONSE);
        }
        byte reqID = anIn.readByte();
        result.add(new RequestSender.RequestID(reqID));
        byte type = anIn.readByte(); 
        if ( type == NULL ) {
            return result;
        }
        if ( null == topology ) {
            throw new IllegalArgumentException("No topology is defined.");
        }
        int id = anIn.readInt();
        NodeInfo nodeInfo = topology.getNodeById(id);
        result.add(nodeInfo);

        id = anIn.readInt();
        nodeInfo = topology.getNodeById(id);
        result.add(nodeInfo);
        
        id = anIn.readInt();
        nodeInfo = topology.getNodeById(id);
        result.add(nodeInfo);
        return result.toArray();
    }
    
    public void afterPop(StreamInputStream anIn, Msg aMsg, Object anOpaque)
        throws IOException {
        List prePop = (List) anOpaque;
        MsgHeader header = aMsg.getHeader();
        header.addHeader(MsgHeaderConstants.BODY_TYPE, prePop.get(0));
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, prePop.get(1));
        if ( 5 != prePop.size() ) {
            return;
        }
        header.addHeader(MsgHeaderConstants.SRC_NODE, prePop.get(2));
        header.addHeader(MsgHeaderConstants.DEST_NODE, prePop.get(3));
        header.addHeader(MsgHeaderConstants.DEST_NODES, prePop.get(4));
    }
    
    public Object beforePush(StreamOutputStream anOut, Msg aMsg)
        throws IOException {
        MsgHeader header = aMsg.getHeader();
        MsgBody.Type type  = (MsgBody.Type)
            header.resetHeader(MsgHeaderConstants.BODY_TYPE);
        if ( type == MsgBody.Type.REQUEST ) {
            anOut.writeByte(REQUEST);
        } else {
            anOut.writeByte(RESPONSE);
        }
        RequestSender.RequestID reqID  = (RequestSender.RequestID)
            header.resetHeader(MsgHeaderConstants.CORRELATION_ID);
        anOut.writeByte(reqID.getID());
        if ( null == topology ) {
            anOut.writeByte(NULL);
            return null;
        }
        anOut.writeByte(TOPOLOGY);
        
        NodeInfo info =
            (NodeInfo) header.resetHeader(MsgHeaderConstants.SRC_NODE);
        anOut.writeInt(topology.getIDOfNode(info));
        
        info =
           (NodeInfo) header.resetHeader(MsgHeaderConstants.DEST_NODE);
        anOut.writeInt(topology.getIDOfNode(info));
        
        NodeInfo target =
            (NodeInfo) header.resetHeader(MsgHeaderConstants.DEST_NODES);
        anOut.writeInt(topology.getIDOfNode(target));
        return null;
    }
    
    public void afterPush(StreamOutputStream anOut, Msg aMsg,
        Object anOpaque) throws IOException {
    }

}