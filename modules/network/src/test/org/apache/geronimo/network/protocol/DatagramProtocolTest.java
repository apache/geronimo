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

import junit.framework.TestCase;

import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ThreadPool;


/**
 * @version $Revision: 1.3 $ $Date: 2004/03/14 00:11:29 $
 */
public class DatagramProtocolTest extends TestCase {

    public void test() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(100);
        tp.setMinimumPoolSize(5);
        tp.setMaximumPoolSize(25);
        tp.setPoolName("TP");
        tp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.setTimeout(500);
        sm.doStart();

        DatagramProtocol dgp = new DatagramProtocol();
        dgp.setUp(new Protocol() {
            public Protocol getUp() {
                throw new NoSuchMethodError();
            }

            public void setUp(Protocol up) {
                throw new NoSuchMethodError();
            }

            public Protocol getDown() {
                throw new NoSuchMethodError();
            }

            public void setDown(Protocol down) {
                throw new NoSuchMethodError();
            }

            public void clearLinks() {
            }

            public Protocol cloneProtocol() throws CloneNotSupportedException {
                return (Protocol) super.clone();
            }

            public void doStart() {
            }

            public void doStop() {
            }

            public void sendUp(UpPacket packet) {
                DatagramUpPacket datgramPacket = (DatagramUpPacket) packet;
                System.out.println("FOO " + datgramPacket.getAddress());
            }

            public void sendDown(DownPacket packet) {
            }

        });

        dgp.setDestinationInterface(new InetSocketAddress("localhost", 0));
        dgp.setSourceAddress(new InetSocketAddress("localhost", 0));
        dgp.setSelectorManager(sm);

        dgp.doStart();

        DatagramDownPacket packet = new DatagramDownPacket();
        packet.setAddress(new InetSocketAddress(dgp.getConnectURI().getHost(), dgp.getConnectURI().getPort()));
        packet.setBuffers(getByteBuffer());

        dgp.sendDown(packet);
        dgp.sendDown(packet);
        dgp.sendDown(packet);

        Thread.sleep(1 * 1000);

        dgp.doStop();

        sm.doStop();

        tp.doStop();
    }

    public void testClone() throws Exception {
        ThreadPool tp = new ThreadPool();
        tp.setKeepAliveTime(100);
        tp.setMinimumPoolSize(5);
        tp.setMaximumPoolSize(25);
        tp.setPoolName("TP");
        tp.doStart();

        SelectorManager sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.doStart();

        DatagramProtocol dgp = new DatagramProtocol();
        dgp.setUp(new Protocol() {
            public Protocol getUp() {
                throw new NoSuchMethodError();
            }

            public void setUp(Protocol up) {
                throw new NoSuchMethodError();
            }

            public Protocol getDown() {
                throw new NoSuchMethodError();
            }

            public void setDown(Protocol down) {
                throw new NoSuchMethodError();
            }

            public void clearLinks() {
            }

            public Protocol cloneProtocol() throws CloneNotSupportedException {
                return (Protocol) super.clone();
            }

            public void doStart() {
            }

            public void doStop() {
            }

            public void sendUp(UpPacket packet) {
                DatagramUpPacket datgramPacket = (DatagramUpPacket) packet;
                System.out.println("FOO " + datgramPacket.getAddress());
            }

            public void sendDown(DownPacket packet) {
            }

        });

        dgp.setDestinationInterface(new InetSocketAddress("localhost", 0));
        dgp.setSourceAddress(new InetSocketAddress("localhost", 8081));
        dgp.setSelectorManager(sm);

        DatagramProtocol dgp2 = (DatagramProtocol) dgp.cloneProtocol();

        dgp2.doStart();

        DatagramDownPacket packet = new DatagramDownPacket();
        packet.setAddress(new InetSocketAddress("localhost", 8081));
        packet.setBuffers(getByteBuffer());

        dgp2.sendDown(packet);
        dgp2.sendDown(packet);
        dgp2.sendDown(packet);

        Thread.sleep(5 * 1000);

        dgp2.doStop();

        sm.doStop();

        tp.doStop();
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
}
