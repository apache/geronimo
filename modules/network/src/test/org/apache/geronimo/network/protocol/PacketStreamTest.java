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

import junit.framework.TestCase;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/18 04:05:51 $
 */
public class PacketStreamTest extends TestCase {

    EchoUpProtocol eup = new EchoUpProtocol();
    boolean failed;

    public void testStream() throws Exception {
        new Thread(new WriterThread((short) 1024), "Test Writer").start();

        PacketInputStream in = new PacketInputStream(eup);
        ObjectInputStream objIn = new ObjectInputStream(in);
        String msg = (String) objIn.readObject();

        assertEquals(msg, "Hello World!");
        assertFalse("Writer thread failed", failed);
    }

    public void testStreamManyPackets() throws Exception {
        new Thread(new WriterThread((short) 2), "Test Writer").start();

        PacketInputStream in = new PacketInputStream(eup);
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
                PacketOutputStream out = new PacketOutputStream(eup, packetSize);
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                objOut.writeObject(new String("Hello World!"));
                objOut.flush();
            } catch (IOException e) {
                failed = true;
            }
        }
    }


    public void setUp() throws Exception {
        eup = new EchoUpProtocol();
        failed = false;
    }

}
