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


public class FilterInputChannel extends InputChannel {

    protected final InputChannel ch;

    FilterInputChannel(InputChannel ch) {
        this.ch = ch;
    }

    public byte readByte() throws IOException {
        return ch.readByte();
    }

    public int readInt() throws IOException {
        return ch.readInt();
    }

    public long readLong() throws IOException {
        return ch.readLong();
    }

    public void skip(int count) throws IOException {
        ch.skip(count);
    }

    public boolean isClosed() {
        return ch.isClosed();
    }

    public int read(byte[] data, int off, int len) throws IOException {
        return ch.read(data, off, len);
    }

    public short readShort() throws IOException {
        return ch.readShort();
    }

    public void setOrder(ByteOrder order) {
        ch.setOrder(order);
    }

    public void relinquish() {
        ch.relinquish();
    }

	public ByteOrder getOrder() {
		return ch.getOrder();
	}

}
