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
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
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
    private final static byte NULL = 0x00;
    
    /**
     * Compression based on the Topology shared knowledge.
     */
    private final static byte TOPOLOGY = 0x01;

    /**
     * Identifies a request.
     */
    private final static byte REQUEST = 0x01;
    
    /**
     * Identifies a response.
     */
    private final static byte RESPONSE = 0x02;

    public NodeTopology getTopology() {
        return topology;
    }

    public void setTopology(NodeTopology aTopology) {
        topology = aTopology;
    }
    
    public Object beforePop(StreamInputStream anIn)
        throws IOException {
        byte type = anIn.readByte(); 
        if ( type == NULL ) {
            return null;
        }
        if ( null == topology ) {
            throw new IllegalArgumentException("No topology is defined.");
        }
        Object[] result = new Object[5];
        int id = anIn.readInt();
        NodeInfo nodeInfo = topology.getNodeById(id);
        result[0] = nodeInfo;

        id = anIn.readInt();
        nodeInfo = topology.getNodeById(id);
        result[1] = nodeInfo;
        
        id = anIn.readInt();
        nodeInfo = topology.getNodeById(id);
        result[2] = nodeInfo;
        
        int bodyType = anIn.read();
        if ( REQUEST == bodyType ) {
            result[3] = MsgBody.Type.REQUEST;
        } else {
            result[3] = MsgBody.Type.RESPONSE;
        }
        
        int reqID = anIn.readInt();
        result[4] = new RequestSender.RequestID(new Integer(reqID));
        return result;
    }
    
    public void afterPop(StreamInputStream anIn, Msg aMsg, Object anOpaque)
        throws IOException {
        if ( null == anOpaque ) {
            return;
        }
        Object[] prePop = (Object[]) anOpaque;
        MsgHeader header = aMsg.getHeader();
        header.addHeader(MsgHeaderConstants.SRC_NODE, prePop[0]);
        header.addHeader(MsgHeaderConstants.DEST_NODE, prePop[1]);
        header.addHeader(MsgHeaderConstants.DEST_NODES, prePop[2]);
        header.addHeader(MsgHeaderConstants.BODY_TYPE, prePop[3]);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, prePop[4]);
    }
    
    public Object beforePush(StreamOutputStream anOut, Msg aMsg)
        throws IOException {
        if ( null == topology ) {
            anOut.writeByte(NULL);
            return null;
        }
        anOut.writeByte(TOPOLOGY);
        MsgHeader header = aMsg.getHeader();
        
        NodeInfo info =
            (NodeInfo) header.resetHeader(MsgHeaderConstants.SRC_NODE);
        anOut.writeInt(topology.getIDOfNode(info));
        
        info =
           (NodeInfo) header.resetHeader(MsgHeaderConstants.DEST_NODE);
        anOut.writeInt(topology.getIDOfNode(info));
        
        NodeInfo target =
            (NodeInfo) header.resetHeader(MsgHeaderConstants.DEST_NODES);
        anOut.writeInt(topology.getIDOfNode(target));
        
        MsgBody.Type type  = (MsgBody.Type)
        header.resetHeader(MsgHeaderConstants.BODY_TYPE);
        if ( type == MsgBody.Type.REQUEST ) {
            anOut.write(REQUEST);
        } else {
            anOut.write(RESPONSE);
        }
        
        RequestSender.RequestID reqID  = (RequestSender.RequestID)
            header.resetHeader(MsgHeaderConstants.CORRELATION_ID);
        anOut.writeInt(reqID.getID());
        
        return null;
    }
    
    public void afterPush(StreamOutputStream anOut, Msg aMsg,
        Object anOpaque) throws IOException {
    }

}