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

package org.apache.geronimo.messaging.remotenode.network;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.MockStreamManager;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.network.protocol.EchoUpProtocol;
import org.apache.geronimo.network.protocol.Protocol;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class ProtocolOutInterceptorTest extends TestCase
{

    private Protocol echoUp;
    private StreamManager manager;
    
    protected void setUp() throws Exception {
        echoUp = new EchoUpProtocol();
        manager = new MockStreamManager(); 
    }
    
    public void testPushDispatch() throws Exception {
        Integer expected = new Integer(1);
        
        ProtocolOutInterceptor out =
            new ProtocolOutInterceptor(echoUp, manager, null, null);
        DummyDispatcher dispatcher = new DummyDispatcher(echoUp);
        Msg msg = new Msg();
        msg.getBody().setContent(expected);
        out.push(msg);

        Msg received = dispatcher.received;
        assertNotNull(received);
        assertEquals(expected, received.getBody().getContent());
        dispatcher.received = null;
    }
    
    private class DummyDispatcher implements MsgOutInterceptor {
        private Msg received;
        public DummyDispatcher(Protocol aProtocol) throws IOException {
            ProtocolInDispatcher inDispatcher =
                new ProtocolInDispatcher(aProtocol, manager, null, null);
            inDispatcher.setMsgProducerOut(this);
        }
        public void push(Msg aMsg) {
            received = aMsg;
        }
    }
    
}
