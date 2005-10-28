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

import java.io.IOException;
import java.nio.ByteOrder;


/**
 * @author Jeppe Sommer (jso@trifork.com)
 */
public abstract class RingBuffer {

    abstract protected void putByte(byte b) throws IOException;

    abstract protected void putShort(short s) throws IOException;

    abstract protected void putInt(int i) throws IOException;

    abstract protected void putLong(long l) throws IOException;

    abstract protected byte getByte() throws IOException;

    /**
     * returns -1 at EOF
     */
    abstract protected int get() throws IOException;

    abstract protected int getInt() throws IOException;

    abstract protected short getShort() throws IOException;

    abstract protected long getLong() throws IOException;

    abstract protected void putSkip(int amount) throws IOException;

    abstract protected void skipInput(int amount) throws IOException;

    abstract protected OutputChannelMarker setPutMark(MarkHandler handler);

    abstract protected void closePutEnd();

    abstract protected void close();

    OutputChannel outputView = new OutputChannel() {

        public void writeByte(byte b) throws IOException {
            putByte(b);
        }

        public void writeShort(short s) throws IOException {
            putShort(s);
        }

        public void writeInt(int i) throws IOException {
            putInt(i);
        }

        public void writeLong(long l) throws IOException {
            putLong(l);
        }

        public void skip(int count) throws IOException {
            putSkip(count);
        }

        public OutputChannelMarker mark(MarkHandler handler) {
            return setPutMark(handler);
        }

        public void write(byte[] data, int off, int len) throws IOException {
            RingBuffer.this.write(data, off, len);
        }

        public void flush() throws IOException {
            RingBuffer.this.flush();
        }

        public void close() {
            RingBuffer.this.closePutEnd();
        }

        public void relinquish() {
            RingBuffer.this.relinquishOutput();
        }

    };

    InputChannel inputView = new InputChannel() {

        public int read(byte[] data, int off, int len) throws IOException {
            return RingBuffer.this.get(data, off, len);
        }

        public int read() throws IOException {
            return get();
        }

        public short readShort() throws IOException {
            return getShort();
        }

        public byte readByte() throws IOException {
            return getByte();
        }

        public int readInt() throws IOException {
            return getInt();
        }

        public long readLong() throws IOException {
            return getLong();
        }

        public void skip(int count) throws IOException {
            skipInput(count);
        }

        public void close() {
            RingBuffer.this.close();
        }

        public boolean isClosed() {
            return RingBuffer.this.isEmpty() && RingBuffer.this.isClosed();
        }

        public int available() {
            return RingBuffer.this.availableForGet();
        }

        public void relinquish() {
            RingBuffer.this.relinquishInput();
        }

        public void setOrder(ByteOrder order) {
            RingBuffer.this.setByteOrderForGet(order);
        }

    };

    public OutputChannel getOutputChannel() {
        return outputView;
    }

    protected abstract void setByteOrderForGet(ByteOrder order);

    protected abstract void relinquishInput();

    protected abstract void relinquishOutput();

    protected abstract void write(byte[] data, int off, int len) throws IOException;

    public abstract int availableForGet();

    protected abstract boolean isClosed();

    protected abstract int get(byte[] data, int off, int len) throws IOException;

    protected abstract void flush() throws IOException;

    public InputChannel getInputChannel() {
        return inputView;
    }

    public abstract boolean isEmpty();

}
