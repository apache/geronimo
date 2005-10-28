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
package org.apache.geronimo.corba.cdr;

import java.nio.ByteBuffer;

import org.omg.CORBA_2_3.portable.InputStream;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.io.GIOPVersion;


public abstract class CDRInputStream extends InputStream {

    private ByteBuffer buf;

    private GIOPVersion giop_version;

    private ORB orb;

    public CDRInputStream(ORB orb, ByteBuffer buf, GIOPVersion version,
                          InputStreamController ctrl)
    {
        this.giop_version = version;
        this.orb = orb;
        this.buf = buf;
    }

    protected final int computeAlignment(int align) {
        if (align > 1) {
            int incr = buf.position() & (align - 1);
            if (incr != 0)
                return align - incr;
        }

        return 0;
    }

    public int getSize() {
        return buf.position();
    }

    void alignAndCheck(int size, int align) {
        int a = computeAlignment(align);
        buf.position(buf.position() + a);

        if (buf.remaining() < size) {
            grow(size);
        }
    }

    public int read_long() {
        alignAndCheck(4, 4);
        return buf.getInt();
    }

    public void __readEndian() {
        // TODO Auto-generated method stub

    }

    /**
     * Create an inputstream that reads data from the given byte array
     */
    public static CDRInputStream create(byte[] data) {
        return create(ByteBuffer.wrap(data));
    }

    public static CDRInputStream create(ByteBuffer buf) {
        // TODO Auto-generated method stub
        return null;
    }

    public int __get_input_position() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long __beginEncapsulation() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void __endEncapsulation(long encap_state) {
        // TODO Auto-generated method stub

    }

    public AbstractORB __get_orb() {
        // TODO Auto-generated method stub
        return null;
    }

}
