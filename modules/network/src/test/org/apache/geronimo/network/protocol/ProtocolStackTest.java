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
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import junit.framework.TestCase;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Rev$ $Date$
 */
public class ProtocolStackTest extends TestCase {

    protected final int COUNT = 5;
    protected CountDown completed;

    public void testNothing() {
    }

    public void test() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(1 * 1000);
        tp.setPoolSize(5);
        tp.setPoolName("TP");
        tp.doStart();

        ClockPool cp = new ClockPool();
        cp.setPoolName("CP");
        cp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.setTimeout(500);
        sm.doStart();

        AcceptableProtocolStack s = new AcceptableProtocolStack();

        SocketProtocol spt = new SocketProtocol();
        spt.setTimeout(10 * 1000);
        spt.setSelectorManager(sm);

        s.push(spt);

        TestProtocol test = new TestProtocol();
        test.setValue("SimpleTest");
        test.setThreadPool(tp);
        test.setClockPool(cp);
        test.setSelectorManager(sm);
        s.push(test);

        s.push(new TestCountingProtocol(completed));

        ProtocolFactory pf = new ProtocolFactory();
        pf.setClockPool(cp);
        pf.setMaxAge(Long.MAX_VALUE);
        pf.setMaxInactivity(1 * 60 * 60 * 1000);
        pf.setReclaimPeriod(10 * 1000);
        pf.setTemplate(s);

        ServerSocketAcceptor ssa = new ServerSocketAcceptor();
        ssa.setSelectorManager(sm);
        ssa.setTimeOut(5 * 1000);
        ssa.setUri(new URI("async://localhost:0/?tcp.nodelay=true&tcp.backlog=5#"));
        ssa.setAcceptorListener(pf);
        ssa.startup();

        ProtocolStack stack = new ProtocolStack();

        SocketProtocol sp = new SocketProtocol();
        sp.setTimeout(10 * 1000);
        sp.setInterface(new InetSocketAddress(ssa.getConnectURI().getHost(), 0));
        sp.setAddress(new InetSocketAddress(ssa.getConnectURI().getHost(), ssa.getConnectURI().getPort()));
        sp.setSelectorManager(sm);

        stack.push(sp);

        stack.setup();

        for (int i = 0; i < COUNT; i++) {
            stack.sendDown(getDatagramPacket());
        }

        if (!completed.attempt(60 * 1000)) {
            throw new IllegalStateException("TIMEOUT");
        }

        stack.drain();

        ssa.drain();

        pf.drain();

        spt.drain();

        sm.doStop();

        cp.doStop();

        tp.doStop();
    }

    public void setUp() throws Exception {
        completed = new CountDown(COUNT);
    }

    static volatile long id = 0;

    public DatagramDownPacket getDatagramPacket() {
        DatagramDownPacket packet = new DatagramDownPacket();
        ArrayList list = new ArrayList();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        CharBuffer b = buffer.asCharBuffer();
        b.put("Hello! " + id++);

        buffer.position(b.position() * 2);
        buffer.flip();
        list.add(buffer);

        packet.setBuffers(list);

        return packet;
    }
}
