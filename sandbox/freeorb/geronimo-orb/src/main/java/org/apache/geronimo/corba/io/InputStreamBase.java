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

import java.io.IOException;

import org.omg.CORBA.Any;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA_2_3.portable.ObjectImpl;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.AnyImpl;
import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.PlainObject;
import org.apache.geronimo.corba.TypeCodeUtil;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.util.IntegerToObjectHashMap;
import org.apache.geronimo.corba.util.IntegerToObjectMap;


public abstract class InputStreamBase extends org.omg.CORBA_2_3.portable.InputStream {

    /**
     * returns the logical current stream position
     */
    public abstract int __stream_position();

    /**
     * Return our ORB implementation
     */
    protected abstract AbstractORB __orb();

    public ORB orb() {
        return __orb();
    }

    private IntegerToObjectMap valueMap;
    private CharConverter char_converter;
    private CharConverter wchar_converter;

    IntegerToObjectMap __get_value_map() {
        if (valueMap == null) {
            valueMap = new IntegerToObjectHashMap();
        }
        return valueMap;
    }

    private void __register_value(int pos, Object value) {
        __get_value_map().put(pos, value);
    }

    private Object __lookup_value(int pos) {
        return __get_value_map().get(pos);
    }


    public short read_ushort() {
        return read_short();
    }

    public int read_ulong() {
        return read_long();
    }

    public final long read_ulonglong() {
        return read_longlong();
    }

    public final float read_float() {
        return Float.intBitsToFloat(read_long());
    }

    public final double read_double() {
        return Double.longBitsToDouble(read_longlong());
    }

    public final boolean read_boolean() {
        return read_octet() == 0 ? false : true;
    }

    public final char read_char() {
        CharConverter converter = __get_char_converter();
        return converter.read_char(this);
    }

    protected void __set_char_converter(CharConverter converter) {
        this.char_converter = converter;
    }

    protected void __set_wchar_converter(CharConverter converter) {
        this.wchar_converter = converter;
    }

    private CharConverter __get_char_converter() {
        if (char_converter == null) {
            char_converter = __orb().get_char_converter(getGIOPVersion());
        }

        return char_converter;
    }

    public final char read_wchar() {
        CharConverter converter = __get_wchar_converter();
        return converter.read_char(this);
    }

    private CharConverter __get_wchar_converter() {
        if (wchar_converter == null) {
            wchar_converter = __orb().get_wchar_converter(getGIOPVersion());
        }

        return wchar_converter;
    }

    public final String read_string() {
        int tag = read_long();
        int tag_position = __stream_position() - 4;
        if (tag == -1) {
            int off = read_long();
            int pos = tag_position + off;
            return (String) __lookup_value(pos);
        } else {
            int pos = tag_position;
            CharConverter converter = __get_char_converter();
            String value = converter.read_string(this, tag);
            __register_value(pos, value);
            return value;
        }
    }


    public final String read_wstring() {
        int tag = read_long();
        int tag_position = __stream_position() - 4;
        if (tag == -1) {
            int off = read_long();
            int pos = tag_position + off;
            return (String) __lookup_value(pos);
        } else {
            int pos = tag_position;
            CharConverter converter = __get_wchar_converter();
            String value = converter.read_string(this, tag);
            __register_value(pos, value);
            return value;
        }
    }

    public final void read_boolean_array(boolean[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_boolean();
        }
    }

    public final void read_char_array(char[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_char();
        }
    }

    public final void read_wchar_array(char[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_wchar();
        }
    }

    public final void read_short_array(short[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_short();
        }
    }

    public final void read_ushort_array(short[] value, int offset, int length) {
        read_short_array(value, offset, length);
    }

    public final void read_long_array(int[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_long();
        }
    }

    public final void read_ulong_array(int[] value, int offset, int length) {
        read_long_array(value, offset, length);
    }

    public final void read_longlong_array(long[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_longlong();
        }
    }

    public final void read_ulonglong_array(long[] value, int offset, int length) {
        read_longlong_array(value, offset, length);
    }

    public final void read_float_array(float[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_float();
        }
    }

    public final void read_double_array(double[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_double();
        }
    }

    public final void read_any_array(org.omg.CORBA.Any[] value, int offset,
                                     int length)
    {
        for (int i = 0; i < length; i++) {
            value[i + offset] = read_any();
        }
    }

    public final Any read_any() {
        org.omg.CORBA.Any any = new AnyImpl(__orb());
        any.read_value(this, read_TypeCode());
        return any;
    }

    public final TypeCode read_TypeCode() {
        return TypeCodeUtil.read(this, this, new java.util.HashMap());
    }

    public void read_octet_array(byte[] data, int off, int len) {
        for (int i = 0; i < len; i++) {
            data[i + off] = read_octet();
        }
    }

    public final org.omg.CORBA.Object read_Object() {
        InternalIOR ior = InternalIOR.read(__orb(), this);
        ClientDelegate del = new ClientDelegate(ior);
        ObjectImpl result = new PlainObject();
        result._set_delegate(del);
        return result;
    }

    public EncapsulationInputStream __open_encapsulation() {
        int len = read_long();
        byte[] data = new byte[len];
        read_octet_array(data, 0, len);
        return new EncapsulationInputStream(__orb(), data);
    }

    public void __close_encapsulation(EncapsulationInputStream encap) {

    }

    protected abstract GIOPVersion getGIOPVersion();

    protected SystemException translate_exception(IOException e) {
        SystemException result;

        result = new MARSHAL(e.getMessage());
        result.initCause(e);

        return result;
    }


}
