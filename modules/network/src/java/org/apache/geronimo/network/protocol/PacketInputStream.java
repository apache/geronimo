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
 * @version $Revision: 1.3 $ $Date: 2004/04/19 16:29:31 $
 */
public class PacketInputStream extends InputStream {

    /**
     * Null AvailableCallBack.
     */
    private static final AvailableCallBack NULL_CALLBACK =
        new AvailableCallBack() {
            public void execute() {}
        };
    
    private final ProtocolBuffer buffer;
    private final Protocol up;
    private final AvailableCallBack callBack;
    private ByteBuffer currentBuffer;
    private boolean closed;


    public PacketInputStream(Protocol up) {
        this(up, (short) 1);
    }

    public PacketInputStream(Protocol up, short queueSize) {
        this(up, queueSize, null);
    }
    
    /**
     * Creates an InputStream on top of the provided protocol.
     * 
     * @param up Protocol.
     * @param queueSize Size of the queue used to buffer UpPackets coming from
     * up.
     * @param aCallBack Callback when an UpPacket is received from up.
     */
    public PacketInputStream(Protocol up, short queueSize,
        AvailableCallBack aCallBack) {
        this.buffer = new ProtocolBuffer(queueSize);
        if ( null == aCallBack ) {
            this.callBack = NULL_CALLBACK;
        } else {
            this.callBack = aCallBack;
        }
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
        while (length > 0 && 0 < available() ) {
            check();
            int remaining = currentBuffer.remaining();
            int segment = Math.min(remaining, length);
            currentBuffer.get(b, off, segment);
            off += segment;
            length -= segment;
        }
        return len - length;
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
        return currentBuffer.remaining() + buffer.available();
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
        volatile int available;

        ProtocolBuffer(short size) {
            queue = new BoundedLinkedQueue(size);
        }

        int available() {
            return available;
        }
        
        UpPacket getPacket() throws InterruptedException {
            UpPacket packet = (UpPacket) queue.take();
            available -= packet.getBuffer().remaining();
            return packet;
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
                available += packet.getBuffer().remaining();
                queue.put(packet);
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
            callBack.execute();
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
            throw new UnsupportedOperationException("Method not implemented");
        }

    }

    /**
     * When an UpPacket has been received by the protocol from which this
     * instance is reading, the execute method is called.
     * <BR>
     * It allows reading from the InputStream without having to poll it.
     */
    public interface AvailableCallBack {
        public void execute() throws ProtocolException;
    }
    
}
