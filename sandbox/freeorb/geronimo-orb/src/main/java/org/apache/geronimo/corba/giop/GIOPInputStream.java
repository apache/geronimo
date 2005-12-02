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
package org.apache.geronimo.corba.giop;

import java.io.IOException;
import java.nio.ByteOrder;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.channel.InputChannel;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.omg.CORBA.INTERNAL;


public class GIOPInputStream extends InputStreamBase {

    int size_of_previous_fragments;
    int position;

    int limit;

    private final InputChannel ch;

    private InputController controller;

    private AbstractORB orb;

    private GIOPVersion version;
    private int message_start;

    protected GIOPInputStream(ORB orb, GIOPVersion version, InputController controller, InputChannel ch) {
    		this.orb = orb;
    		this.version = version;
        this.controller = controller;
        this.ch = ch;
    }

    public void limit(int limit) {
        this.limit = limit;
    }

    public void setMessageStart(int position) {
        this.message_start = position;
    }

    public int position() {
        return position;
    }

    /**
     * ensure that there are
     */
    private void check(int size) {
        if (position + size > limit) {
            size_of_previous_fragments += limit - message_start;
            controller.getNextFragment(this);
        }
    }

    private int availableHere() {
        return limit - position;
    }

    public void align(int align) throws IOException {
        if (align > 1) {
            int incr = position() & (align - 1);
            if (incr != 0)
                skip(align - incr);
        }
    }

    public int read(byte[] data, int off, int len) throws IOException {

        if (len == 0) {
            return 0;
        }

        // assert that there is at least one byte available
        check(1);

        int howmany = Math.min(len, availableHere());

        int read_bytes = ch.read(data, off, howmany);
        if (read_bytes != -1) {
            position += read_bytes;
        }

        return read_bytes;
    }

    public void read_octet_array(byte[] data, int off, int len) {
        do {
            int howmany;
            try {
                howmany = read(data, off, len);
            }
            catch (IOException e) {
                throw translate_exception(e);
            }
            off += howmany;
            len -= howmany;
        }
        while (len != 0);
    }

    public byte read_octet() {
        check(1);

        byte result;
        try {
            result = ch.readByte();
        }
        catch (IOException e) {
            throw translate_exception(e);
        }

        position += 1;
        return result;
    }

    public short read_short() {
        short result;
        try {
            align(2);
            check(2);
            result = ch.readShort();
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
        position += 2;
        return result;
    }

    public int read_long() {
        int result;
        try {
            align(4);
            check(4);
            result = ch.readInt();
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
        position += 4;
        return result;
    }

    public long read_longlong() {
        long result;
        try {
            align(8);
            result = ch.readLong();
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
        position += 8;
        return result;
    }

    public void skip(int count) throws IOException {
        ch.skip(count);
        position += count;
    }

    public boolean isClosed() {
        return ch.isClosed();
    }

    public void relinquish() {
        ch.relinquish();
    }

    public void setOrder(ByteOrder order) {
        ch.setOrder(order);
    }

    public void position(int i) {
        position = i;
    }

    public void controller(InputController controller) {
        this.controller = controller;
    }

    public int __stream_position() {
        return size_of_previous_fragments + position - message_start;
    }

    public AbstractORB __orb() {
        return orb;
    }

    public GIOPVersion getGIOPVersion() {
        return version;
    }

	public boolean __isLittleEndian() {
		return ch.getOrder() == ByteOrder.LITTLE_ENDIAN;
	}

	public void finishGIOPMessage() {
		// skip input at end of message, if any
		if (limit > position()) {
			try {
				ch.skip(limit - position());
			} catch (IOException e) {
				e.printStackTrace();
				throw new INTERNAL();
			}
		}
		ch.relinquish();
	}

	public void setGIOPVersion(GIOPVersion version2) {
		this.version = version2;
	}


}
