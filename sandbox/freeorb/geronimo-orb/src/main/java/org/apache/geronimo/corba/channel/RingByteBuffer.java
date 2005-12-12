/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.channel;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.corba.concurrency.IOSemaphoreClosedException;
import org.apache.geronimo.corba.util.HexUtil;


/**
 * @version $Revision$ $Date$
 */
public abstract class RingByteBuffer extends RingBuffer {

    int capacity;

    ByteBuffer byteBuffer;

    IOSemaphore putSpace;

    IOSemaphore getSpace;

    // byte[] buf;
    // int bufStart;

    List putMarks = new LinkedList();

    ByteBuffer b0, b1, b2;

    ByteBuffer[] bb0, bb1, bb2;

    int nextPutPos;

    private int nextGetPos;

    private String name;

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("RingBuffer[").append(getName()).append(" ");
        sb.append("put=").append(nextPutPos).append("; ");
        sb.append("putAvailable=").append(availableForPut()).append("; ");
        sb.append("get=").append(nextGetPos).append("; ");
        sb.append("getAvailable=").append(availableForGet());
        sb.append("]");

        return sb.toString();
    }

    public RingByteBuffer(int capacity, boolean direct) {
        this.capacity = capacity;
        byteBuffer = direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer
                .allocate(capacity);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        // buf = byteBuffer.array(); // May throw UnsupportedOperationException
        // bufStart = byteBuffer.arrayOffset();

        putSpace = new IOSemaphore(capacity - 1);
        getSpace = new IOSemaphore(0);

        b1 = byteBuffer.slice();
        b2 = byteBuffer.slice();
        b0 = byteBuffer.slice(); // used for get(byte[])

        // bb0 is used for empty I/O
        bb0 = new ByteBuffer[]{ByteBuffer.allocateDirect(0)};

        // bb1 is used for I/O where the contents fits in a contiguous buffer
        bb1 = new ByteBuffer[]{b1};

        // bb2 is for where we need to split the ring-buffer contents across two
        // buffers
        bb2 = new ByteBuffer[]{b1, b2};
    }

    public void flush() throws IOException {
        flushMarks();
        bufferFullHook("flush");
    }

    /**
     * this is a hint to the implementation, that writing should commence
     *
     * @throws IOException
     */
    abstract protected void bufferFullHook(String how) throws IOException;

    /**
     * This is a hint to the implementation, that reading should commence. If
     * the buffer is in a synchroneous context, this should simply read
     * something into the buffer by calling readFrom, in an asycnhroneous
     * context this should register interest in reading with an underlying
     * selector.
     *
     * @throws IOException
     */
    abstract protected void bufferEmptyHook(String how) throws IOException;

    /** */
    abstract protected void readEOFHook();

    /**
     * put a single byte to the buffer
     */
    protected void putByte(byte b) throws IOException {

        ensurePutSpace(1);

        byteBuffer.put(nextPutPos, b);
        nextPutPos = incr(nextPutPos, 1);

        increaseGetSpace(1);
    }

    protected void putShort(short i) throws IOException {

        ensurePutSpace(2);

        // byteBuffer.putInt(put, i)
        if (nextPutPos + 2 > byteBuffer.limit()) {
            byteBuffer.put(nextPutPos, (byte) ((i >> 8) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((i >> 0) & 0xff));
            nextPutPos = incr(nextPutPos, 1);
        } else {
            byteBuffer.putShort(nextPutPos, i);
            nextPutPos = incr(nextPutPos, 2);
        }

        increaseGetSpace(2);
    }

    protected void putInt(int i) throws IOException {

        ensurePutSpace(4);

        // byteBuffer.putInt(put, i)
        if (nextPutPos + 4 > byteBuffer.limit()) {
            byteBuffer.put(nextPutPos, (byte) ((i >> 24) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((i >> 16) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((i >> 8) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((i >> 0) & 0xff));
            nextPutPos = incr(nextPutPos, 1);
        } else {
            byteBuffer.putInt(nextPutPos, i);
            nextPutPos = incr(nextPutPos, 4);
        }

        increaseGetSpace(4);
    }

    protected void putLong(long l) throws IOException {

        ensurePutSpace(8);

        // byteBuffer.putInt(put, i)
        if (nextPutPos + 8 > byteBuffer.limit()) {
            byteBuffer.put(nextPutPos, (byte) ((l >> 56) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 48) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 40) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 32) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 24) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 16) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 8) & 0xff));
            byteBuffer.put(nextPutPos = incr(nextPutPos, 1), (byte) ((l >> 0) & 0xff));
            nextPutPos = incr(nextPutPos, 1);
        } else {
            byteBuffer.putLong(nextPutPos, l);
            nextPutPos = incr(nextPutPos, 8);
        }

        increaseGetSpace(8);
    }

    private void ensurePutSpace(int amount) throws IOException {
        ensureSomePutSpace(amount, amount);
    }

    private int ensureSomePutSpace(int min, int max) throws IOException {

        while (!putMarks.isEmpty()
               && availableForPut() + availableForGet() < min)
        {

            // No room in buffer and we're not going to free enough
            // space
            // by flushing non-marked bytes.
            flushOneMark();
        }

        if (availableForPut() <= max) {
            bufferFullHook("ensurePutSpace");
        }

        return putSpace.acquireSome(1, max, 0L);
    }

    private void flushOneMark() throws IOException {
        if (putMarks.isEmpty()) {
            throw new IllegalStateException();
        }
        AsyncMarkState state = (AsyncMarkState) putMarks.remove(0);

        if (!state.isReleased) {
            state.handler.bufferFull(state);
            if (!state.isReleased) {
                state.release();
            }
        }
    }

    private int availableForPut() {
        return putSpace.availablePermits();
    }

    private void flushMarks() throws IOException {
        AsyncMarkState[] markStates = new AsyncMarkState[putMarks.size()];
        putMarks.toArray(markStates);
        putMarks.clear();

        for (int i = markStates.length - 1; i >= 0; i--) {
            markStates[i].handler.bufferFull(markStates[i]);
            if (!markStates[i].isReleased) {
                markStates[i].release();
            }
        }
    }

    private void increaseGetSpace(int amount) {
        AsyncMarkState mark = lastMark();
        if (mark != null) {
            // A mark has been set, so don't release permits until mark is
            // released.
            mark.permits += amount;
        } else {
            // Release permits immediatly
            getSpace.releaseIfNotClosed(amount);
        }
    }

    private AsyncMarkState lastMark() {
        if (putMarks.isEmpty()) {
            return null;
        }
        return (AsyncMarkState) putMarks.get(putMarks.size() - 1);
    }

    private AsyncMarkState firstMark() {
        if (putMarks.isEmpty()) {
            return null;
        }
        return (AsyncMarkState) putMarks.get(0);
    }

    private void ensureGetSpace(int amount) throws IOException {
        ensureSomeGetSpace(amount, amount);
    }

    public int availableForGet() {
        return getSpace.availablePermits();
    }

    private int ensureSomeGetSpace(int min, int max) throws IOException {

        // this operation will make the buffer empty?
        if (availableForGet() <= min) {
            bufferEmptyHook("ensureSomeGetSpace(" + min + "," + max + ")");
        }

        try {
            return getSpace.acquireSome(min, max, 0L);
        }
        catch (IOSemaphoreClosedException ex) {

            // hook method to signal that EOF has been read
            readEOFHook();
            throw ex;
        }
    }

    private void increasePutSpace(int amount) {
        putSpace.releaseIfNotClosed(amount);
    }

    private int incr(int val, int incr) {
        return (val + incr) % capacity;
    }

    protected int get() throws IOException {
        try {
            return getByte();
        }
        catch (EOFException e) {
            return -1;
        }
    }

    protected void write(byte[] data, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }

        do {
            int bytes = writeSome(data, off, len);
            off += bytes;
            len -= bytes;
        }
        while (len != 0);
    }

    private int writeSome(byte[] data, int off, int len) throws IOException {

        System.out.println("WRITE SOME");

        int some = ensureSomePutSpace(1, len);

        if (nextPutPos + some > capacity) {

            int size1 = capacity - nextPutPos;
            b0.position(nextPutPos);
            b0.put(data, off, size1);

            b0.position(0);
            b0.put(data, off + size1, some - size1);

        } else {

            b0.position(nextPutPos);
            b0.put(data, off, some);

        }

        nextPutPos = incr(nextPutPos, some);

        increaseGetSpace(some);
        return some;
    }

    protected int get(byte[] data, int off, int len) throws IOException {
        if (len == 0) {
            if (isEmpty() && isClosed()) {
                return -1;
            }
            return 0;
        }

        int occupied;

        try {
            occupied = ensureSomeGetSpace(1, len);
        }
        catch (EOFException e) {
            return -1;
        }

        if (nextGetPos + occupied > capacity) {
            b0.position(nextGetPos);

            int size1 = capacity - nextGetPos;
            b0.get(data, off, size1);

            int size2 = occupied - size1;
            b0.position(0);
            b0.get(data, off + size1, size2);

        } else {
            b0.position(nextGetPos);
            b0.get(data, off, occupied);
        }

        nextGetPos = incr(nextGetPos, occupied);

        increasePutSpace(occupied);
        return occupied;
    }

    protected byte getByte() throws IOException {
        ensureGetSpace(1);

        if ((nextGetPos < 0) || (nextGetPos >= byteBuffer.limit())) {
            System.out.println("bad");
        }

        byte result = byteBuffer.get(nextGetPos);

        incrGet();
        increasePutSpace(1);

        return result;
    }

    protected void setByteOrderForGet(ByteOrder order) {
        byteBuffer.order(order);
    }

    protected ByteOrder getByteOrderForGet() {
    		return byteBuffer.order();
    }
    
    protected int getInt() throws IOException {
        ensureGetSpace(4);
        int result;

        if (nextGetPos + 4 < capacity) {
            result = byteBuffer.getInt(nextGetPos);
            nextGetPos = incr(nextGetPos, 4);

        } else if (byteBuffer.order() == ByteOrder.BIG_ENDIAN) {

            result = (0xff & byteBuffer.get(nextGetPos)) << 24;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 16;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 8;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 0;
            incrGet();

        } else {

            result = (0xff & byteBuffer.get(nextGetPos)) << 0;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 8;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 16;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 24;
            incrGet();

        }

        increasePutSpace(4);

        return result;
    }

    protected short getShort() throws IOException {
        ensureGetSpace(2);
        short result;

        if (nextGetPos + 2 < capacity) {
            result = byteBuffer.getShort(nextGetPos);
            nextGetPos = incr(nextGetPos, 2);

        } else if (byteBuffer.order() == ByteOrder.BIG_ENDIAN) {

            result = (short) ((0xff & byteBuffer.get(nextGetPos)) << 8);
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 0;
            incrGet();

        } else {

            result = (short) ((0xff & byteBuffer.get(nextGetPos)) << 0);
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 8;
            incrGet();

        }

        increasePutSpace(2);

        return result;
    }

    protected long getLong() throws IOException {
        ensureGetSpace(8);
        long result;

        if (nextGetPos + 8 < capacity) {
            result = byteBuffer.getLong(nextGetPos);
            nextGetPos = incr(nextGetPos, 8);

        } else if (byteBuffer.order() == ByteOrder.BIG_ENDIAN) {

            result = (0xffL & byteBuffer.get(nextGetPos)) << 56;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 48;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 40;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 32;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 24;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 16;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 8;
            incrGet();
            result |= (0xffL & byteBuffer.get(nextGetPos)) << 0;
            incrGet();

        } else {

            result = (0xff & byteBuffer.get(nextGetPos)) << 0;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 8;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 16;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 24;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 32;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 40;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 48;
            incrGet();
            result |= (0xff & byteBuffer.get(nextGetPos)) << 56;
            incrGet();

        }

        increasePutSpace(8);

        return result;
    }

    private void incrGet() {
        this.nextGetPos = incr(nextGetPos, 1);
    }

    protected OutputChannelMarker setPutMark(MarkHandler handler) {
        AsyncMarkState state = new AsyncMarkState(handler);

        putMarks.add(state);

        return state;
    }

    private ByteBuffer[] exposePutSpace() {
        return expose(nextPutPos, availableForPut());
    }

    private ByteBuffer[] exposeGetSpace() {
        return expose(nextGetPos, availableForGet());
    }

    /**
     * expose the ring buffer's content
     */
    private ByteBuffer[] expose(int start, int len) {

        if (len == 0) {
            return bb0;
        }

        if (start > capacity || start < 0) {
            throw new IllegalArgumentException();
        }

        if (start + len <= capacity) {
            b1.position(0);
            b1.limit(start + len);
            b1.position(start);

            if (b1.remaining() != len) {
                throw new InternalError();
            }

            return bb1;
        } else {
            b1.position(0); // ensure position for next
            b1.limit(capacity);
            b1.position(start);

            b2.position(0);
            b2.limit(len - b1.remaining());

            if (b1.remaining() + b2.remaining() != len) {
                throw new InternalError();
            }

            return bb2;
        }

    }

    public boolean isEmpty() {
        return availableForGet() <= 0;
    }

    /**
     * @param count
     * @throws IOException
     */
    public void putSkip(int count) throws IOException {

        ensurePutSpace(count);
        nextPutPos = incr(nextPutPos, count);
        increaseGetSpace(count);
    }

    public void skipInput(int count) throws IOException {

        ensureGetSpace(count);

        this.nextGetPos = incr(nextGetPos, count);

        increasePutSpace(count);
    }

    class AsyncMarkState extends OutputChannelMarker {

        int permits;

        int position;

        private final MarkHandler handler;

        private boolean isReleased = false;

        public AsyncMarkState(MarkHandler handler) {
            this.handler = handler;
            this.position = nextPutPos;
        }

        public void release() {
            if (isReleased) {
                throw new IllegalStateException();
            }

            AsyncMarkState prev = getPrevious();
            if (prev == null) {
                getSpace.releaseIfNotClosed(permits);
            } else {
                prev.permits += permits;
            }

            putMarks.remove(this);
            isReleased = true;
        }

        private AsyncMarkState getPrevious() {
            int idx = putMarks.indexOf(this);
            if (idx == -1 || idx == 0) {
                return null;
            } else {
                return (AsyncMarkState) putMarks.get(idx - 1);
            }
        }

        public void putByte(int idx, byte b) throws IOException {
            int oldPut = nextPutPos;
            nextPutPos = incr(position, idx);
            RingByteBuffer.this.putByte(b);
            nextPutPos = oldPut;
            permits -= 1;
        }

        public void putInt(int idx, int b) throws IOException {
            int oldPut = nextPutPos;
            nextPutPos = incr(position, idx);
            RingByteBuffer.this.putInt(b);
            nextPutPos = oldPut;
            permits -= 4;
        }

        public void putLong(int idx, long b) throws IOException {
            int oldPut = nextPutPos;
            nextPutPos = incr(position, idx);
            RingByteBuffer.this.putLong(b);
            nextPutPos = oldPut;
            permits -= 8;
        }
    }

    public boolean isClosedForPut() {
        return getSpace.isClosed();
    }

    public void closePutEnd() {
        getSpace.close();
    }

    public boolean isClosed() {
        return putSpace.isClosed();
    }

    /**
     * mark the receiving end as closed
     */
    public void close() {
        putSpace.close();
    }

    /** */
    public int writeTo(Socket sock) throws IOException {

        SocketChannel chan = sock.getChannel();
        if (chan == null) {
            return writeTo(sock.getOutputStream());
        } else {
            return writeTo(chan);
        }
    }

    public int writeTo(OutputStream out) throws IOException {

        ByteBuffer[] buffers = exposeGetSpace();

        int count;

        try {
            count = bbwrite(out, buffers);

        }
        catch (InterruptedIOException ex) {
            count = ex.bytesTransferred;
        }
        catch (IOException ex) {
            close();
            throw ex;
        }

        if (count > 0) {
            skipInput(count);
            return count;
        } else {
            return 0;
        }
    }

    public int writeTo(GatheringByteChannel chan) throws IOException {

        ByteBuffer[] buffers = exposeGetSpace();

        int count;

        HexUtil.printHex(System.out, "S: ", buffers);
        
        try {
            if (buffers.length == 1) {
                count = chan.write(buffers[0]);
            } else {
                count = (int) chan.write(buffers);
            }
        } catch (ChannelClosedException ex) {
            close();
            count = 0;
        } catch (InterruptedIOException ex) {
            count = ex.bytesTransferred;
        }

        if (count > 0) {
            skipInput(count);
            return count;
        } else {
            return 0;
        }

    }

    //
    // Utility
    //

    private int bbread(InputStream in, ByteBuffer[] buffers) throws IOException {
        int total = 0;
        for (int i = 0; i < buffers.length; i++) {
            int expected = buffers[i].remaining();
            if (expected == 0) {
                continue;
            }

            int bytes = bbread(in, buffers[i]);
            if (bytes == -1) {
                if (total == 0) {
                    return -1;
                } else {
                    return total;
                }
            }

            total += bytes;
            if (bytes != expected) {
                return total;
            }
        }

        return total;
    }

    private int bbread(InputStream in, ByteBuffer buffer) throws IOException {

        byte[] data = buffer.array();
        int off = buffer.arrayOffset();
        int start = buffer.position();
        int len = buffer.remaining();

        if (len != 0) {

            int bytes;

            try {
                bytes = in.read(data, off + start, len);
            }
            catch (InterruptedIOException ex) {
                bytes = ex.bytesTransferred;
            }

            return bytes;
        }

        return len;
    }

    private int bbwrite(OutputStream out, ByteBuffer[] buffers)
            throws IOException
    {
        int written = 0;

        for (int i = 0; i < buffers.length; i++) {
            int size = buffers[i].remaining();
            if (size != 0) {

                int bytes;

                try {
                    bytes = bbwrite(out, buffers[i]);

                }
                catch (IOException ex) {
                    if (written == 0) {
                        throw ex;
                    } else {
                        return written;
                    }
                }

                if (bytes == -1) {

                    if (written == 0) {
                        return -1;
                    } else {
                        return written;
                    }
                }

                written += bytes;

                if (bytes != size) {
                    return written;
                }
            }

        }

        return written;
    }

    private int bbwrite(OutputStream out, ByteBuffer buffer) throws IOException {
        byte[] data = buffer.array();
        int off = buffer.arrayOffset();

        int start = buffer.position();
        int len = buffer.remaining();

        try {
            out.write(data, off + start, len);
            buffer.position(start + len);
            return len;

        }
        catch (InterruptedIOException ex) {
            buffer.position(start + ex.bytesTransferred);
            return ex.bytesTransferred;

        }
        catch (EOFException ex) {
            return -1;
        }
    }

    public boolean readFrom(Socket sock) throws IOException
    {
        SocketChannel ch = sock.getChannel();
        if (ch == null) {
            return readFrom(sock.getInputStream());
        } else {
            return readFrom(ch);
        }
    }

    public boolean readFrom(InputStream inputStream) throws IOException {

        ByteBuffer[] buffers = exposePutSpace();
        int count;

        long before = System.currentTimeMillis();
        System.out.println("" + new Date() + " will read");

        int bufsize = 0;
        try {
            if (buffers.length == 1) {
                bufsize = buffers[0].remaining();
                count = bbread(inputStream, buffers[0]);
            } else {
                bufsize = buffers[0].remaining() + buffers[1].remaining();
                count = bbread(inputStream, buffers);
            }
        }
        catch (SocketException ex) {
            count = -1;
        }

        long after = System.currentTimeMillis();
        System.out.println("" + new Date() + " did read " + count
                           + " bytes OF " + bufsize + " TOOK " + (after - before) + " ms");

        if (count == -1) {
            closePutEnd();
            return true;
        } else if (count == 0) {
            return false;
        } else {
            putSkip(count);
            return true;
        }

    }

    public boolean readFrom(ScatteringByteChannel chan) throws IOException
    {
        ByteBuffer[] buffers = exposePutSpace();
        int count;

        long before = System.currentTimeMillis();
        System.out.println("" + new Date() + " will read");

        int bufsize = 0;
        try {
            if (buffers.length == 1) {
                bufsize = buffers[0].remaining();
                count = chan.read(buffers[0]);
            } else {
                bufsize = buffers[0].remaining() + buffers[1].remaining();
                count = (int) chan.read(buffers);
            }
        }
        catch (ClosedChannelException ex) {
            count = -1;
        }

        long after = System.currentTimeMillis();
        System.out.println("" + new Date() + " did read " + count
                           + " bytes OF " + bufsize + " TOOK " + (after - before) + " ms");
/*
        int length = count;
        for (int i = buffers.length-1; i >- 0; i--) {
        		int bufsz = buffers[i].position();
        		if (length > bufsz) {
        			buffers[i].position(0);
        			length -= bufsz;
        		} else {
        			buffers[i].position(bufsz-length);
        			break;
        		}
        }
        
        HexUtil.printHex(System.out, "R:", buffers);
*/
        
        if (count == -1) {
            closePutEnd();
            return true;
        } else if (count == 0) {
            return false;
        } else {
            putSkip(count);
            return true;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
