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

package org.apache.geronimo.messaging.reference;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.Result;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class ReferenceFactoryTest extends TestCase
{

    private RequestSender sender;
    private MockMsgOut out;
    private NodeInfo nodeInfo;
    private Object endPointID;
    private ReferenceableInfo info;
    
    protected void setUp() throws Exception {
        sender = new RequestSender();
        out = new MockMsgOut();
        
        InetAddress address = InetAddress.getLocalHost();
        nodeInfo = new NodeInfo("Node1", address, 8081);
        endPointID = new Integer(1);
    }

    private Object factory() {
        ReferenceFactory factory = new ReferenceFactory(sender, out);
        info = new ReferenceableInfo(nodeInfo, endPointID,
            new Class[] {MockReferenceable.class, Referenceable.class}, 1);
        return factory.factory(info);
    }
    
    public void testTypes() throws Exception {
        Object opaque = factory();
        assertTrue(opaque instanceof MockReferenceable);
        assertTrue(opaque instanceof Reference);
    }
    
    public void testEquals() throws Exception {
        Object obj1 = factory();
        Object obj2 = factory();
        assertTrue(obj1.equals(obj2));
    }
    
    private class MockMsgOut implements MsgOutInterceptor {
        private Msg msg;
        private Result result;
        public void push(Msg aMsg) {
            msg = aMsg;
            Object id =
            msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID);
            sender.setResponse(id, result);
        }
    }
    
}
