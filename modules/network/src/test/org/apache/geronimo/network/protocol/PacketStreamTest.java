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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.TestCase;


/**
 * @version $Rev$ $Date$
 */
public class PacketStreamTest extends TestCase {

    EchoUpProtocol eup;
    Latch startLatch;
    boolean failed;

    public void testDummy() throws Exception { }

    public void testStream() throws Exception {
        new Thread(new WriterThread((short) 1024), "Test Writer").start();

        PacketInputStream in = new PacketInputStream(eup);

        startLatch.release();

        ObjectInputStream objIn = new ObjectInputStream(in);
        String msg = (String) objIn.readObject();

        assertEquals(msg, "Hello World!");
        assertFalse("Writer thread failed", failed);
    }

    public void testStreamManyPackets() throws Exception {
        new Thread(new WriterThread((short) 2), "Test Writer").start();

        PacketInputStream in = new PacketInputStream(eup);

        startLatch.release();

        ObjectInputStream objIn = new ObjectInputStream(in);
        String msg = (String) objIn.readObject();

        assertEquals(msg, "Hello World!");
        assertFalse("Writer thread failed", failed);
    }

    class WriterThread implements Runnable {

        short packetSize;

        WriterThread(short packetSize) {
            this.packetSize = packetSize;
        }

        public void run() {
            try {
                startLatch.acquire();

                PacketOutputStream out = new PacketOutputStream(eup, packetSize);
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                objOut.writeObject(new String("Hello World!"));
                objOut.flush();
            } catch (IOException e) {
                failed = true;
            } catch (InterruptedException e) {
                failed = true;
            }
        }
    }

    public void setUp() throws Exception {
        eup = new EchoUpProtocol();
        startLatch = new Latch();
        failed = false;
    }

}
