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
package org.apache.geronimo.network.protocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import junit.framework.TestCase;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Rev$ $Date$
 */
public class DatagramProtocolTest extends TestCase {

    protected final int COUNT = 5;
    protected CountDown completed;

    public void test() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(100);
        tp.setPoolSize(5);
        tp.setPoolName("TP");
        tp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.doStart();

        ProtocolStack stack = new ProtocolStack();
        DatagramProtocol dgp = new DatagramProtocol();
        dgp.setDestinationInterface(new InetSocketAddress("localhost", 0));
        dgp.setSourceAddress(new InetSocketAddress("localhost", 0));
        dgp.setSelectorManager(sm);

        stack.push(dgp);
        stack.push(new TopProtocol());
        stack.push(new TestCountingProtocol(completed));

        stack.setup();

        DatagramDownPacket packet = new DatagramDownPacket();
        packet.setAddress(new InetSocketAddress(dgp.getConnectURI().getHost(), dgp.getConnectURI().getPort()));
        packet.setBuffers(getByteBuffer());

        for (int i = 0; i < COUNT; i++) {
            stack.sendDown(packet);
        }

        if (!completed.attempt(60 * 1000)) {
            throw new IllegalStateException("TIMEOUT");
        }

        stack.drain();

        sm.doStop();

        tp.doStop();
    }

    public void testClone() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(100);
        tp.setPoolSize(5);
        tp.setPoolName("TP");
        tp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.doStart();

        ProtocolStack stack = new ProtocolStack();
        DatagramProtocol dgp = new DatagramProtocol();
        dgp.setDestinationInterface(new InetSocketAddress("localhost", 0));
        dgp.setSourceAddress(new InetSocketAddress("localhost", 8081));
        dgp.setSelectorManager(sm);

        stack.push(dgp);
        stack.push(new TopProtocol());
        stack.push(new TestCountingProtocol(completed));

        ProtocolStack stack_copy = (ProtocolStack) stack.cloneProtocol();

        stack_copy.setup();

        DatagramDownPacket packet = new DatagramDownPacket();
        packet.setAddress(new InetSocketAddress("localhost", 8081));
        packet.setBuffers(getByteBuffer());

        for (int i = 0; i < COUNT; i++) {
            stack_copy.sendDown(packet);
        }

        if (!completed.attempt(60 * 1000)) {
            throw new IllegalStateException("TIMEOUT");
        }

        stack_copy.drain();

        sm.doStop();

        tp.doStop();
    }

    public void setUp() throws Exception {
        completed = new CountDown(COUNT);
    }

    public Collection getByteBuffer() {
        ArrayList list = new ArrayList();

        ByteBuffer byteBuffer = ByteBuffer.allocate(6).order(ByteOrder.BIG_ENDIAN);

        // Load the ByteBuffer with some bytes
        byteBuffer.put(0, (byte) 'H');
        byteBuffer.put(1, (byte) 'e');
        byteBuffer.put(2, (byte) 'l');
        byteBuffer.put(3, (byte) 'l');
        byteBuffer.put(4, (byte) 'o');
        byteBuffer.put(5, (byte) '!');

        list.add(byteBuffer);

        return list;
    }

    class TopProtocol extends AbstractProtocol {

        public void setup() {
        }

        public void drain() {
        }

        public void teardown() {
        }

        public void sendUp(UpPacket packet) throws ProtocolException {
            DatagramUpPacket datgramPacket = (DatagramUpPacket) packet;
            System.out.println("FOO " + datgramPacket.getAddress());
            getUpProtocol().sendUp(packet);
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            getDownProtocol().sendDown(packet);
        }

        public void flush() throws ProtocolException {
        }
    }
}
