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

import java.util.Arrays;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;

/**
 * Based implementation for the Connector contracts.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/24 11:37:05 $
 */
public abstract class AbstractConnector implements Connector, GBean {

    /**
     * Node owning this Connector. 
     */
    protected Node node;
    
    /**
     * Context of the ServerNode which has mounted this instance.
     */
    protected NodeContext serverNodeContext;
    
    /**
     * To send requests.
     */
    protected RequestSender sender;

    /**
     * Used to communicate with remote Connectors.
     */
    protected MsgOutInterceptor out;
    
    /**
     * Creates a Connector, which is mounted by the specified node.
     * 
     * @param aNode Node owning this connector.
     */
    public AbstractConnector(Node aNode) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        node = aNode;
    }
    
    public void setContext(NodeContext aContext) {
        serverNodeContext = aContext;
        sender = aContext.getRequestSender();
        out = aContext.getOutput();
    }

    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody.Type bodyType =
        (MsgBody.Type) header.getHeader(MsgHeaderConstants.BODY_TYPE);
        if ( MsgBody.Type.REQUEST == bodyType ) {
            handleRequest(aMsg);
        } else if ( MsgBody.Type.RESPONSE == bodyType ) {
            handleResponse(aMsg);
        }
    }

    /**
     * Handles a request Msg.
     * 
     * @param aMsg Request Msg to be handled.
     */
    protected abstract void handleRequest(Msg aMsg);
    
    /**
     * Handles a response Msg.
     * 
     * @param aMsg Response to be handled.
     */
    protected void handleResponse(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        CommandResult result;
        result = (CommandResult) body.getContent();
        sender.setResponse(
            header.getHeader(MsgHeaderConstants.CORRELATION_ID),
            result);
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        node.addConnector(this);
    }

    public void doStop() throws WaitingException, Exception {
        node.removeConnector(this);
    }

    public void doFail() {
        node.removeConnector(this);
    }
    
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Abstract Connector", AbstractConnector.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Name", true));
        infoFactory.addAttribute(new GAttributeInfo("Context", false));
        infoFactory.addReference("Node", Node.class);
        infoFactory.addOperation("deliver", new Class[] {Msg.class});
        infoFactory.setConstructor(new GConstructorInfo(
            Arrays.asList(new Object[]{"Node"}),
            Arrays.asList(new Object[]{Node.class})));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
