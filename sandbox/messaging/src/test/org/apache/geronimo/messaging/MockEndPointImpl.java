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

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class MockEndPointImpl
    extends AbstractEndPoint
    implements EndPoint, GBean, MockEndPoint {

    private final List received;
    private final NodeInfo[] targetNodes;

    public MockEndPointImpl(Node aNode,
        Object anID, NodeInfo[] aTargetNodes) {
        super(aNode, anID);
        if ( null == aTargetNodes ) {
            throw new IllegalArgumentException("Target nodes is required.");
        }
        targetNodes = aTargetNodes;
        received = new ArrayList();
    }
    
    public void sendRawObject(Object anObject) {
        sender.sendSyncRequest(anObject,
            new HeaderOutInterceptor(
                MsgHeaderConstants.BODY_TYPE,
                MsgBody.Type.REQUEST,
                out), getID(), targetNodes);
    }

    public List getReceived() {
        return received;
    }
    
    protected void handleRequest(Msg aMsg) {
        received.add(aMsg.getBody().getContent());
        Msg msg = aMsg.reply();
        msg.getBody().setContent(new Result(true, null));
        out.push(msg);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(MockEndPointImpl.class, AbstractEndPoint.GBEAN_INFO);
        factory.setConstructor(
            new String[] {"Node", "ID", "TargetNodes"},
            new Class[] {Node.class, Object.class, NodeInfo[].class});
        factory.addAttribute(new GAttributeInfo("TargetNodes", true));
        factory.addAttribute(new GAttributeInfo("Received", false));
        factory.addOperation("sendRawObject", new Class[]{Object.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
