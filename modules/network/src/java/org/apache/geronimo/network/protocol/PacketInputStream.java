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
import java.io.InputStream;
import java.nio.ByteBuffer;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;


/**
 * @version $Revision: 1.5 $ $Date: 2004/08/01 13:03:43 $
 */
public class PacketInputStream extends InputStream {

    ProtocolBuffer buffer;
    private final Protocol up;
    private ByteBuffer currentBuffer;
    private boolean closed;


    public PacketInputStream(Protocol up) {
        this(up, (short) 1);
    }

    public PacketInputStream(Protocol up, short queueSize) {
        this.buffer = new ProtocolBuffer(queueSize);
        this.up = up;
        this.currentBuffer = ByteBuffer.allocate(0);
        this.closed = false;

        this.up.setUpProtocol(buffer);
        buffer.setDownProtocol(this.up);

    }

    public int read() throws IOException {
        if (closed) throw new IOException("Packet InputStream closed");

        check();

        return currentBuffer.get();
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }

        int length = len;
        while (length > 0) {
            check();
            int remaining = currentBuffer.remaining();
            int segment = Math.min(remaining, length);
            currentBuffer.get(b, off, segment);
            off += segment;
            length -= segment;
        }
        return len;
    }

    public long skip(long n) throws IOException {

        long length = n;
        while (length > 0) {
            int segment;
            if (length <= Integer.MAX_VALUE) {
                segment = Math.min(currentBuffer.remaining(), (int) length);
            } else {
                segment = Math.min(currentBuffer.remaining(), Integer.MAX_VALUE);
            }
            currentBuffer.position(currentBuffer.position() + segment);
            length -= segment;
            check();
        }

        return n;
    }

    public int available() throws IOException {
        return currentBuffer.remaining();
    }

    public void close() throws IOException {
        closed = true;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }

    private void check() throws IOException {
        if (!currentBuffer.hasRemaining()) {
            try {
                currentBuffer = buffer.getPacket().getBuffer();
            } catch (InterruptedException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }
    }

    private class ProtocolBuffer implements Protocol {

        BoundedLinkedQueue queue;
        Protocol down;

        ProtocolBuffer(short size) {
            queue = new BoundedLinkedQueue(size);
        }

        UpPacket getPacket() throws InterruptedException {
            return (UpPacket) queue.take();
        }

        public Protocol getUpProtocol() {
            throw new NoSuchMethodError("Socket protocol is at the bottom");
        }

        public void setUpProtocol(Protocol up) {
            throw new NoSuchMethodError("Socket protocol is at the bottom");
        }

        public Protocol getDownProtocol() {
            return down;
        }

        public void setDownProtocol(Protocol down) {
            this.down = down;
        }

        public void clearLinks() {
            down = null;
        }

        public Protocol cloneProtocol() throws CloneNotSupportedException {
            return (Protocol) super.clone();
        }

        public void setup() throws ProtocolException {
        }

        public void drain() throws ProtocolException {
        }

        public void teardown() throws ProtocolException {
        }

        public void sendUp(UpPacket packet) throws ProtocolException {
            try {
                queue.put(packet);
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            throw new UnsupportedOperationException("Method not implemented");
        }

        public void flush() throws ProtocolException {
        }
    }
}
