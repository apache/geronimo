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

package org.apache.geronimo.messaging;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 * Based implementation for the EndPoint contracts.
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/02 11:29:24 $
 */
public abstract class AbstractEndPoint
    implements EndPoint, GBean
{

    /**
     * Node which has mounted this EndPoint. 
     */
    protected final Node node;

    /**
     * EndPoint identifier.
     */
    protected final Object id;
    
    /**
     * To send requests.
     */
    protected final RequestSender sender;

    /**
     * Used to communicate with remote EndPoints.
     */
    protected MsgOutInterceptor out;
    
    /**
     * Creates an EndPoint, which is mounted by the specified Node and having
     * the specified identifier.
     * 
     * @param aNode Node owning this connector.
     * @param anID EndPoint identifier.
     */
    public AbstractEndPoint(Node aNode, Object anID) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        } else if ( null == anID ) {
            throw new IllegalArgumentException("Identifier is required.");
        }
        node = aNode;
        id = anID;
        
        sender = new RequestSender();
    }
    
    public final Object getID() {
        return id;
    }
    
    public MsgOutInterceptor getMsgConsumerOut() {
        return new MsgDispatcher();
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        // When an EndPoint is unregistered by a Node, this latter resets its
        // MsgProducer output.
        if ( null == aMsgOut ) {
            out = null;
            return;
        }
        // Automatically adds the identifier of this EndPoint to produced Msgs.
        out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_ENDPOINT, id, aMsgOut);
    }
    
    /**
     * Handles a request Msg. A request Msg MUST contain a Request object.
     * 
     * @param aMsg Request Msg to be processed.
     */
    protected void handleRequest(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        Request command = (Request) body.getContent();
        command.setTarget(this);
        Result result = command.execute();
        Msg msg = aMsg.reply();
        msg.getBody().setContent(result);
        out.push(msg);
    }
    
    /**
     * Handles a response Msg. A response Msg MUST contain a Result object.
     * 
     * @param aMsg Response to be handled.
     */
    protected void handleResponse(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        Result result = (Result) body.getContent();
        sender.setResponse(
            header.getHeader(MsgHeaderConstants.CORRELATION_ID),
            result);
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        node.addEndPoint(this);
    }

    public void doStop() throws WaitingException, Exception {
        node.removeEndPoint(this);
    }

    public void doFail() {
        node.removeEndPoint(this);
    }

    /**
     * Dispatches Msgs delivered to this EndPoint. 
     */
    private class MsgDispatcher implements MsgOutInterceptor {

        public void push(Msg aMsg) {
            MsgHeader header = aMsg.getHeader();
            MsgBody.Type bodyType =
                (MsgBody.Type) header.getHeader(MsgHeaderConstants.BODY_TYPE);
            if ( MsgBody.Type.REQUEST == bodyType ) {
                handleRequest(aMsg);
            } else if ( MsgBody.Type.RESPONSE == bodyType ) {
                handleResponse(aMsg);
            } else {
                // This "should" neither happen as we are using a type-safe
                // enumeration. However, as Msgs are marshalled it is possible
                // to have an unknown bodyType. Just to be sure.
                throw new IllegalArgumentException("Unknown body type.");
            }
        }
        
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Abstract EndPoint", AbstractEndPoint.class.getName());
        infoFactory.addReference("Node", Node.class);
        infoFactory.addAttribute("ID", Object.class, true);
        infoFactory.addAttribute("MsgConsumerOut", MsgOutInterceptor.class, false);
        infoFactory.addAttribute("MsgProducerOut", MsgOutInterceptor.class, false);
        infoFactory.setConstructor(new String[]{"Node", "ID"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
