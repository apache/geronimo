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

package org.apache.geronimo.network.protocol.control;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.CountingProtocol;
import org.apache.geronimo.network.protocol.DatagramDownPacket;
import org.apache.geronimo.network.protocol.ProtocolFactory;
import org.apache.geronimo.network.protocol.ServerSocketAcceptor;
import org.apache.geronimo.network.protocol.SocketProtocol;
import org.apache.geronimo.network.protocol.TestProtocol;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;


/**
 * @version $Revision: 1.9 $ $Date: 2004/07/08 22:07:54 $
 */
public class ControlProtocolTest extends TestCase {

    final static private Log log = LogFactory.getLog(ControlProtocolTest.class);

    public void testDummy() throws Exception { }

    public void test() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(100 * 1000);
        tp.setPoolSize(5);
        tp.setPoolName("TP");
        tp.doStart();

        ClockPool cp = new ClockPool();
        cp.setPoolName("CP");
        cp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.doStart();

        ControlServerProtocolStack templateStack = new ControlServerProtocolStack();

        SocketProtocol spt = new SocketProtocol();
        spt.setTimeout(10 * 1000);
        spt.setSelectorManager(sm);

        templateStack.push(spt);

        ControlServerProtocol csp = new ControlServerProtocol();
        csp.setTimeout(1 * 1000);
        csp.setThreadPool(tp);
        csp.setClockPool(cp);
        csp.setSelectorManager(sm);
        csp.setControlServerListener(new ControlServerListener() {
            public void shutdown() {
                log.trace("SERVER SIDE SHUTDOWN_REQ");
            }
        });

        templateStack.push(csp);

        ControlServerProtocolWaiter waiter = new ControlServerProtocolWaiter();

        waiter.push(new CountingProtocol());

        TestProtocol test = new TestProtocol();
        test.setValue("SimpleTest");
        test.setThreadPool(tp);
        test.setClockPool(cp);
        test.setSelectorManager(sm);

        waiter.push(test);

        templateStack.push(waiter);

        ProtocolFactory pf = new ProtocolFactory();
        pf.setClockPool(cp);
        pf.setMaxAge(Long.MAX_VALUE);
        pf.setMaxInactivity(1 * 60 * 60 * 1000);
        pf.setReclaimPeriod(10 * 1000);
        pf.setTemplate(templateStack);

        ServerSocketAcceptor ssa = new ServerSocketAcceptor();
        ssa.setSelectorManager(sm);
        ssa.setTimeOut(5 * 1000);
        ssa.setUri(new URI("async://localhost:0/?tcp.nodelay=true&tcp.backlog=5#"));
        ssa.setAcceptorListener(pf);
        ssa.startup();

        ControlClientProtocolStack clientStack = new ControlClientProtocolStack();
        clientStack.setClassLoader(Thread.currentThread().getContextClassLoader());
        clientStack.setThreadPool(tp);
        clientStack.setClockPool(cp);
        clientStack.setSelectorManager(sm);

        SocketProtocol sp = new SocketProtocol();
        sp.setTimeout(15 * 1000);
        sp.setInterface(new InetSocketAddress(ssa.getConnectURI().getHost(), 0));
        sp.setAddress(new InetSocketAddress(ssa.getConnectURI().getHost(), ssa.getConnectURI().getPort()));
        sp.setSelectorManager(sm);

        clientStack.push(sp);

        ControlClientProtocol ccp = new ControlClientProtocol();
        ccp.setTimeout(15 * 1000);

        clientStack.push(ccp);

        clientStack.setup();

        clientStack.sendDown(getDatagramPacket());
        clientStack.sendDown(getDatagramPacket());
        clientStack.sendDown(getDatagramPacket());

        Thread.sleep(5 * 1000);

        clientStack.drain();

        Thread.sleep(5 * 1000);

        ssa.drain();

        pf.drain();

        sm.doStop();

        cp.doStop();

        tp.doStop();
    }

    static volatile long id = 0;

    protected DatagramDownPacket getDatagramPacket() {
        DatagramDownPacket packet = new DatagramDownPacket();
        ArrayList list = new ArrayList();

        final int COUNT = 1024000;
        ByteBuffer buffer = ByteBuffer.allocate(COUNT);
        for (int i = 0; i < COUNT; i++) {
            buffer.put((byte) 0x0b);
        }
//        CharBuffer b = buffer.asCharBuffer();
//        b.put("Hello! " + id++);
//
//        buffer.position(b.position()*2);
        buffer.flip();
        list.add(buffer);

        packet.setBuffers(list);

        return packet;
    }

}
