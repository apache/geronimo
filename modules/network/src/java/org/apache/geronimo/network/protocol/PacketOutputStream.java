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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/20 20:39:11 $
 */
public class PacketOutputStream extends OutputStream {

    private final Protocol down;
    private ByteBuffer currentBuffer;
    private short packetSize;
    private boolean closed;


    public PacketOutputStream(Protocol down) {
        this(down, (short) 1024);
    }

    public PacketOutputStream(Protocol down, short packetSize) {
        this.down = down;
        this.packetSize = packetSize;
        this.currentBuffer = ByteBuffer.allocate(packetSize);
        this.closed = false;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public void write(int b) throws IOException {
        if (closed) throw new IOException("PacketOutputStream closed");

        currentBuffer.put((byte) b);
        if (!currentBuffer.hasRemaining()) flush();
    }

    public void write(byte b[]) throws IOException {
        if (closed) throw new IOException("PacketOutputStream closed");

        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (closed) throw new IOException("PacketOutputStream closed");

        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }

        while (len > 0) {
            int remaining = currentBuffer.remaining();
            int segment = Math.min(remaining, len);
            currentBuffer.put(b, off, segment);
            off += segment;
            len -= remaining;
            if (!currentBuffer.hasRemaining()) flush();
        }
    }

    public void flush() throws IOException {
        if (closed) throw new IOException("PacketOutputStream closed");

        currentBuffer.flip();

        if (currentBuffer.remaining() > 0) {
            PlainDownPacket packet = new PlainDownPacket();
            packet.setBuffers(Collections.singleton(currentBuffer));

            try {
                down.sendDown(packet);
            } catch (ProtocolException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }

        currentBuffer = ByteBuffer.allocate(packetSize);
    }

    public void close() throws IOException {
        flush();
        closed = true;
    }
}
