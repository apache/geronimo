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

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/24 11:37:06 $
 */
public class DummyConnector
    extends AbstractConnector
    implements Connector, GBean {

    private final String name;
    private final List received;
    private final NodeInfo[] targetNodes;
    
    public DummyConnector(Node aNode,
        String aName, NodeInfo[] aTargetNodes) {
        super(aNode);
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == aTargetNodes ) {
            throw new IllegalArgumentException("Target nodes is required.");
        }
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

    public void setContext(NodeContext aContext) {
        super.setContext(aContext);
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
    
    protected void handleRequest(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody body = aMsg.getBody();
        
        Object id = header.getHeader(MsgHeaderConstants.CORRELATION_ID);
        Object srcNode = header.getHeader(MsgHeaderConstants.SRC_NODE);

        received.add(body.getContent());
        
        Msg msg = new Msg();
        body = msg.getBody();
        body.setContent(new CommandResult(true, null));
        MsgOutInterceptor reqOut =
            new HeaderOutInterceptor(
                MsgHeaderConstants.CORRELATION_ID,
                id,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.BODY_TYPE,
                    MsgBody.Type.RESPONSE,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_NODES,
                        srcNode,
                        new HeaderOutInterceptor(
                            MsgHeaderConstants.BODY_TYPE,
                            MsgBody.Type.RESPONSE,
                            out))));
        reqOut.push(msg);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(DummyConnector.class, AbstractConnector.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Node", "Name", "TargetNodes"},
            new Class[] {Node.class, String.class, NodeInfo[].class});
        factory.addAttribute(new GAttributeInfo("TargetNodes", true));
        factory.addAttribute(new GAttributeInfo("Received", false));
        factory.addOperation("raiseISException");
        factory.addOperation("sendRawObject", new Class[]{Object.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
