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

package org.apache.geronimo.messaging.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class StreamManagerImplTest
    extends TestCase
{

    public void testRetrieveLocalNext() throws Exception {
        int size = 1024*1024;
        MockNode node = new MockNode();
        InetAddress address = InetAddress.getLocalHost();
        node.setNodeInfo(new NodeInfo("dummy", address, 8081));
        
        StreamManager master = new StreamManagerImpl(node);
        InputStream inputStream = new DummyInputStream(size);
        Object streamID = master.register(inputStream);
        byte[] buffer;
        int curPos = 0;
        while ( StreamManagerImpl.NULL_READ !=
            (buffer = master.retrieveLocalNext(streamID)) ) {
            for (int i = 0; i < buffer.length; i++) {
                assertEquals(1, buffer[i]);
                curPos++;
            }
        }
        assertEquals(curPos, size);
    }
    
    public void testRetrieve() throws Exception {
        MockNode node = new MockNode();
        InetAddress address = InetAddress.getLocalHost();
        node.setNodeInfo(new NodeInfo("dummy", address, 8081));
        
        StreamManager master = new StreamManagerImpl(node);
        
        StreamManager slave = new StreamManagerImpl(node);

        MsgOutInterceptor out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE, node,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_ENDPOINT, "DUMMY",
                    slave.getMsgConsumerOut()));
        master.setMsgProducerOut(out);
        
        out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE, node,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_ENDPOINT, "DUMMY",
                    master.getMsgConsumerOut()));
        slave.setMsgProducerOut(out);

        int size = 1024*1024;
        InputStream inputStream = new DummyInputStream(size);
        Object streamID = master.register(inputStream);

        InputStream retrieved = slave.retrieve(streamID);
        int read;
        int curPos = 0;
        while ( -1 != (read = retrieved.read())) {
            assertEquals(1, read);
            curPos++;
        }
        assertEquals(curPos, size);
    }
    
    private static class DummyInputStream extends InputStream {
        private final int size;
        private int curPos = 0;
        private DummyInputStream(int aSize) {
            size = aSize;
        }
        public int read() throws IOException {
            if ( curPos++ < size ) {
                return 1;
            }
            return -1;
        }
    }
    
}
