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

package org.apache.geronimo.messaging.util;

import java.io.IOException;
import java.net.InetAddress;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.geronimo.messaging.Request;
import org.apache.geronimo.messaging.Result;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class ProxyFactoryTest extends TestCase {

    private static final Object DEST_CONN = new Object();
    
    private RequestSender sender;
    private MockMsgOut out;
    private NodeInfo[] dests;
    private DummyTarget proxy;
    
    protected void setUp() throws Exception {
        sender = new RequestSender();
        out = new MockMsgOut();
        dests = new NodeInfo[]
            {new NodeInfo("test", InetAddress.getLocalHost(), 8080)};
        
        EndPointCallback endPointCallback = new EndPointCallback(sender);
        endPointCallback.setEndPointId(DEST_CONN);
        endPointCallback.setOut(out);
        endPointCallback.setTargets(dests);
        
        ProxyFactory factory =
            new ProxyFactory(new Class[] {DummyTarget.class},
                new Callback[] {endPointCallback},
                new Class[] {MethodInterceptor.class}, null);
        proxy = (DummyTarget) factory.getProxy();
    }
    
    public void testProxy1() throws Exception {
        Object rawObject = new Object();
        out.result = new Result(true, rawObject);

        proxy.sendRawObject(rawObject);
        
        Msg msg = out.msg;
        assertNotNull(msg);
        MsgHeader header = msg.getHeader();
        assertEquals(dests, header.getHeader(MsgHeaderConstants.DEST_NODES));
        assertEquals(DEST_CONN, header.getHeader(MsgHeaderConstants.DEST_ENDPOINT));
        Request request = (Request) msg.getBody().getContent();
        assertEquals("sendRawObject", request.getMethodName());
        assertEquals(rawObject, request.getParameters()[0]);
    }

    public void testProxy2() throws Exception {
        out.result = new Result(false, new IllegalStateException());

        try {
            proxy.raiseISException();
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
    }

    public void testProxy3() throws Exception {
        out.result = new Result(false, new IOException());

        try {
            proxy.raiseCheckedException();
            fail("Should throw an IOException.");
        } catch (IOException e) {
        }
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
    
    private interface DummyTarget {
        public void sendRawObject(Object anObject);
        public void raiseISException();
        public void raiseCheckedException() throws IOException;
    }
    
}
