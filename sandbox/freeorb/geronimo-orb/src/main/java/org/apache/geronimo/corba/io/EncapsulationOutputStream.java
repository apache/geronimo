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
package org.apache.geronimo.corba.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.channel.MarkHandler;
import org.apache.geronimo.corba.channel.OutputChannelMarker;
import org.omg.CORBA.NO_IMPLEMENT;


public class EncapsulationOutputStream extends OutputStreamBase {

    private final AbstractORB orb;
    private ByteArrayOutputStream barr;
    private DataOutputStream dout;

    public EncapsulationOutputStream(AbstractORB orb) {
        this.orb = orb;
        this.barr = new java.io.ByteArrayOutputStream();
        this.dout = new java.io.DataOutputStream(barr);

        // big-edian marker
        write_boolean(false);
    }

    public AbstractORB __orb() {
        return orb;
    }

    public int __stream_position() {
        return dout.size();
    }

    public int computeAlignment(int pos, int align) {
        if (align > 1) {
            int incr = pos & (align - 1);
            if (incr != 0) {
                return align - incr;
            }
        }
        return 0;
    }

    public void align(int align) {

        try {
            int skip = computeAlignment(__stream_position(), align);
            if (skip != 0) {
                skip(skip);
            }
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public static final byte[] SKIP_BUFFER = new byte[8];

    private void skip(int skip) throws IOException {
        while (skip > 0) {
            int bytes = Math.min(SKIP_BUFFER.length, skip);
            write(SKIP_BUFFER, 0, bytes);
            skip -= bytes;
        }
    }

    public void write(byte[] data, int off, int len) throws IOException {
        dout.write(data, off, len);
    }

    public void write(int value) throws IOException {
        dout.write(value);
    }

    public void write_octet(byte value) {
        try {
            dout.writeByte(value);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public void write_short(short value) {
        try {
            align(2);
            dout.writeShort(value);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public void write_long(int value) {
        try {
            align(4);
            dout.writeInt(value);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public void write_longlong(long value) {
        try {
            align(8);
            dout.writeLong(value);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        barr.writeTo(out);
    }

	protected OutputChannelMarker mark(MarkHandler handler) {
		throw new NO_IMPLEMENT();
	}

	protected GIOPVersion getGIOPVersion() {
		return GIOPVersion.V1_0;
	}

}
