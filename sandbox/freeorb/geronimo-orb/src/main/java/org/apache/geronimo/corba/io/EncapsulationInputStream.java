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

import org.omg.CORBA.MARSHAL;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.codeset.CharConverter;


public class EncapsulationInputStream extends InputStreamBase {

    private int pos;

    private final boolean little_endian;

    private final GIOPVersion version;

    private final AbstractORB orb;

    private final byte[] data;

    private final int off;

    private final int len;

    public EncapsulationInputStream(AbstractORB orb, GIOPVersion version,
                                    CharConverter charConverter,
                                    CharConverter wCharConverter, byte[] data, int off, int len)
    {
        this.orb = orb;
        this.version = version;
        this.__set_char_converter(charConverter);
        this.__set_wchar_converter(wCharConverter);
        this.data = data;
        this.off = off;
        this.len = len;

        this.little_endian = read_boolean();
    }

    public EncapsulationInputStream(AbstractORB orb, byte[] data) {
        this(orb, data, 0, data.length);
    }

    public EncapsulationInputStream(AbstractORB orb, byte[] data, int off, int len) {
        this(orb, GIOPVersion.V1_0, orb.get_char_converter(GIOPVersion.V1_0),
             orb.get_wchar_converter(GIOPVersion.V1_0), data, off, len);
    }

    public int __stream_position() {
        return pos;
    }

    public AbstractORB __orb() {
        return orb;
    }

    protected void __check(int size, int align) {
        int padding = computeAlignment(pos, align);
        if (pos + padding + size > len) {
            throw new MARSHAL();
        }
        pos += padding;
    }

    public int available() {
        return pos - len;
    }

    public GIOPVersion getGIOPVersion() {
        return version;
    }

    public byte read_octet() {
        __check(1, 1);
        return data[off + pos++];
    }

    public short read_short() {
        __check(2, 2);

        return read2();
    }

    private short read2() {
        short value;
        if (little_endian) {
            value = (short) (read1() | (read1() << 8));
        } else {
            value = (short) ((read1() << 8) | read1());
        }

        return value;
    }

    private int read1() {
        return (data[off + pos++] & 0xff);
    }

    public int read_long() {
        __check(4, 4);

        return read4();
    }

    private int read4() {
        int value;
        if (little_endian) {
            value = read1() | (read1() << 8) | (read1() << 16)
                    | (read1() << 24);
        } else {
            value = (read1() << 24) | (read1() << 16) | (read1() << 8)
                    | read1();
        }

        return value;
    }

    public long read_longlong() {

        __check(8, 8);

        long value;
        if (little_endian) {
            value = read4() | (((long) read4()) << 32);
        } else {
            value = (((long) read4()) << 32) | read4();
        }

        return value;

    }

    public EncapsulationInputStream __open_encapsulation() {
        int encap_len = read_long();
        return new EncapsulationInputStream(__orb(), data, pos, encap_len);
    }

    public boolean __isLittleEndian() {
    	return little_endian;
    }
}
