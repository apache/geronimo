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
import java.nio.ByteOrder;
import java.util.ArrayList;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.CountDown;
import junit.framework.TestCase;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;


/**
 * @version $Revision: 1.4 $ $Date: 2004/03/17 03:12:00 $
 */
public class SocketProtocolStressTest extends TestCase {

    protected ThreadPool tp;
    protected ClockPool cp;
    protected SelectorManager sm;
    protected SocketProtocol spt;
    protected ProtocolFactory pf;
    protected ServerSocketAcceptor ssa;
    protected SocketProtocol sp;
    protected volatile int count;

    public void testSimple() throws Exception {
        sp.sendDown(getDatagramPacket());
        sp.sendDown(getDatagramPacket());
        sp.sendDown(getDatagramPacket());

        DatagramDownPacket packet = getDatagramPacket();
        sp.sendDown(packet);
        sp.sendDown(packet);

        Thread.sleep(5 * 1000);
    }

    public void testConcurrentRequests() throws Exception {

        final int WORKERS = 100;
        final int MESSAGE_COUNT = 10;
        final CyclicBarrier barrier = new CyclicBarrier(WORKERS);
        final CountDown finished = new CountDown(WORKERS);

        for (int i = 0; i < WORKERS; i++) {

            new Thread() {
                /**
                 * @see java.lang.Thread#run()
                 */
                public void run() {
                    try {
                        barrier.barrier();

                        for (int i = 0; i < MESSAGE_COUNT; i++)
                            sp.sendDown(getDatagramPacket());


                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        finished.release();
                    }
                }
            }.start();
        }

        finished.acquire();

        Thread.sleep(5 * 1000);
        
        assertEquals(WORKERS * MESSAGE_COUNT, count);
    }

    public void setUp() throws Exception {
        count = 0;

        tp = new ThreadPool();
        tp.setKeepAliveTime(60 * 1000);
        tp.setMinimumPoolSize(5);
        tp.setMaximumPoolSize(25);
        tp.setPoolName("TP");
        tp.doStart();

        cp = new ClockPool();
        cp.setPoolName("CP");
        cp.doStart();

        sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.setTimeout(500);
        sm.doStart();

        spt = new SocketProtocol();
        spt.setUpProtocol(new Protocol() {
            public Protocol getUpProtocol() {
                throw new NoSuchMethodError();
            }

            public void setUpProtocol(Protocol up) {
                throw new NoSuchMethodError();
            }

            public Protocol getDownProtocol() {
                throw new NoSuchMethodError();
            }

            public void setDownProtocol(Protocol down) {
                throw new NoSuchMethodError();
            }

            public void clearLinks() {
            }

            public Protocol cloneProtocol() throws CloneNotSupportedException {
                return (Protocol) super.clone();
            }

            public void setup() {
            }

            public void drain() {
            }

            public void teardown() throws ProtocolException {
            }

            public void sendUp(UpPacket packet) {
                count++;
            }

            public void sendDown(DownPacket packet) {
            }

        });
        spt.setTimeout(10 * 1000);
        spt.setSelectorManager(sm);

        pf = new ProtocolFactory();
        pf.setClockPool(cp);
        pf.setMaxAge(Long.MAX_VALUE);
        pf.setMaxInactivity(1 * 60 * 60 * 1000);
        pf.setReclaimPeriod(10 * 1000);
        pf.setTemplate(spt);

        ssa = new ServerSocketAcceptor();
        ssa.setSelectorManager(sm);
        ssa.setTimeOut(5 * 1000);
        ssa.setUri(new URI("async://localhost:0/?tcp.nodelay=true&tcp.backlog=5#"));
        ssa.setAcceptorListener(pf);
        ssa.startup();

        sp = new SocketProtocol();
        sp.setUpProtocol(new Protocol() {
            public Protocol getUpProtocol() {
                throw new NoSuchMethodError();
            }

            public void setUpProtocol(Protocol up) {
                throw new NoSuchMethodError();
            }

            public Protocol getDownProtocol() {
                throw new NoSuchMethodError();
            }

            public void setDownProtocol(Protocol down) {
                throw new NoSuchMethodError();
            }

            public void clearLinks() {
            }

            public Protocol cloneProtocol() throws CloneNotSupportedException {
                return (Protocol) super.clone();
            }

            public void setup() {
            }

            public void drain() {
            }

            public void teardown() throws ProtocolException {
            }

            public void sendUp(UpPacket packet) {
            }

            public void sendDown(DownPacket packet) {
            }

        });

        sp.setTimeout(10 * 1000);
        sp.setInterface(new InetSocketAddress("localhost", 0));
        sp.setAddress(new InetSocketAddress(ssa.getConnectURI().getHost(), ssa.getConnectURI().getPort()));
        sp.setSelectorManager(sm);

        sp.setup();
    }

    public void tearDown() throws Exception {
        sp.drain();

        ssa.drain();

        pf.drain();

        spt.drain();

        sm.doStop();

        cp.doStop();

        tp.doStop();
    }

    public DatagramDownPacket getDatagramPacket() {
        DatagramDownPacket packet = new DatagramDownPacket();
        ArrayList list = new ArrayList();

        final int COUNT = 102400;
        ByteBuffer buffer = ByteBuffer.allocate(COUNT);
        for (int i = 0; i < COUNT; i++) {
            buffer.put((byte) 0xba);
        }
        buffer.flip();
        list.add(buffer);

        packet.setBuffers(list);

        return packet;
    }
}
