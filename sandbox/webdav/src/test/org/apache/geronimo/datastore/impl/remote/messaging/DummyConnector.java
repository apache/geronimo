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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:14 $
 */
public class DummyConnector implements Connector {

    private final String name;
    private final List received;
    private final NodeInfo[] targetNodes;
    private RequestSender sender;
    private MsgOutInterceptor out;
    protected ServerNodeContext serverNodeContext;
    
    public DummyConnector(String aName, NodeInfo[] aTargetNodes) {
        name = aName;
        targetNodes = aTargetNodes;
        received = new ArrayList();
    }
    
    public String getName() {
        return name;
    }

    public void raiseISException() {
        throw new IllegalStateException();
    }
    
    public void sendRawObject(Object anObject) {
        sender.sendSyncRequest(anObject,
            new HeaderOutInterceptor(
                MsgHeaderConstants.BODY_TYPE,
                MsgBody.Type.REQUEST,
                out), targetNodes);
    }

    public void setContext(ServerNodeContext aContext) {
        serverNodeContext = aContext;
        sender = aContext.getRequestSender();
        out = aContext.getOutput();
        if ( null != out ) {
            out = 
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    name,
                    out);
        }
    }

    public List getReceived() {
        return received;
    }
    
    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody.Type bodyType =
        (MsgBody.Type) header.getHeader(MsgHeaderConstants.BODY_TYPE);
        if ( bodyType.equals(MsgBody.Type.REQUEST) ) {
            handleRequest(aMsg);
        } else if ( bodyType.equals(MsgBody.Type.RESPONSE) ) {
            handleResponse(aMsg);
        }
    }
    
    public void handleRequest(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody body = aMsg.getBody();
        
        Object id = header.getHeader(MsgHeaderConstants.CORRELATION_ID);
        Object node = header.getHeader(MsgHeaderConstants.SRC_NODE);

        received.add(body.getContent());
        
        Msg msg = new Msg();
        body = msg.getBody();
        body.setContent(new CommandResult(true, null));
        MsgOutInterceptor reqOut =
            new HeaderOutInterceptor(
                MsgHeaderConstants.CORRELATION_ID,
                id,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_CONNECTOR,
                    getName(),
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.BODY_TYPE,
                        MsgBody.Type.RESPONSE,
                        new HeaderOutInterceptor(
                            MsgHeaderConstants.DEST_NODES,
                            node,
                            new HeaderOutInterceptor(
                                MsgHeaderConstants.BODY_TYPE,
                                MsgBody.Type.RESPONSE,
                                out)))));
        reqOut.push(msg);
    }

    private void handleResponse(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        CommandResult result;
        result = (CommandResult) body.getContent();
        sender.setResponse(
            header.getHeader(MsgHeaderConstants.CORRELATION_ID),
            result);
    }
    
}
