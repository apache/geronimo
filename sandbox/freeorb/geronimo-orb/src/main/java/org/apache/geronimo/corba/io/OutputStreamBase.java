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
import java.io.Serializable;
import java.util.HashMap;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.TypeCodeUtil;


public abstract class OutputStreamBase extends OutputStream implements
                                                            org.omg.CORBA.DataOutputStream
{

    private CharConverter char_writer;

    private CharConverter wchar_writer;

    private ValueWriter value_writer;

    public abstract AbstractORB __orb();

    public ORB orb() {
        return __orb();
    }

    public InputStream create_input_stream() {
        throw new NO_RESOURCES();
    }

    public void write_boolean(boolean value) {
        write_octet(value ? (byte) 1 : (byte) 0);
    }

    public void write_char(char value) {
        CharConverter char_converter = __get_char_converter();
        char_converter.write_char(this, value);
    }

    private CharConverter __get_char_converter() {
        if (char_writer == null) {
            char_writer = __orb().get_char_converter(getGIOPVersion());
        }
        return char_writer;
    }

    private CharConverter __get_wchar_converter() {
        if (wchar_writer == null) {
            wchar_writer = __orb().get_char_converter(getGIOPVersion());
        }
        return wchar_writer;
    }

    private GIOPVersion getGIOPVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    public void write(int value) throws IOException {
        try {
            write_octet((byte) value);
        }
        catch (SystemException e) {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }

    public void write_wchar(char value) {
        CharConverter char_converter = __get_wchar_converter();
        char_converter.write_char(this, value);
    }

    public void write_ushort(short value) {
        write_short(value);
    }

    public void write_ulong(int value) {
        write_long(value);
    }

    public void write_ulonglong(long value) {
        write_longlong(value);
    }

    public void write_float(float value) {
        write_long(Float.floatToIntBits(value));
    }

    public void write_double(double value) {
        write_longlong(Double.doubleToLongBits(value));
    }

    public void write_string(String value) {
        __get_char_converter().write_string(this, value);
    }

    public void write_wstring(String value) {
        __get_wchar_converter().write_string(this, value);
    }

    public void write_boolean_array(boolean[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_boolean(value[offset + i]);
        }
    }

    public void write_char_array(char[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_char(value[i]);
        }
    }

    public void write_wchar_array(char[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_wchar(value[i]);
        }
    }

    public void write_octet_array(byte[] value, int offset, int length) {
        try {
            write(value, offset, length);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }

    public void write_short_array(short[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_short(value[i]);
        }
    }

    public void write_ushort_array(short[] value, int offset, int length) {
        write_short_array(value, offset, length);
    }

    public void write_long_array(int[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_long(value[i]);
        }
    }

    public void write_ulong_array(int[] value, int offset, int length) {
        write_long_array(value, offset, length);
    }

    public void write_longlong_array(long[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_longlong(value[i]);
        }
    }

    public void write_ulonglong_array(long[] value, int offset, int length) {
        write_longlong_array(value, offset, length);
    }

    public void write_float_array(float[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_float(value[i]);
        }
    }

    public void write_double_array(double[] value, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            write_double(value[i]);
        }
    }

    public void write_Object(Object value) {
        if (value == null) {
            // write null IOR
            write_string("");
            write_ulong(0);
        } else {
            if (value instanceof LocalObject)
                throw new org.omg.CORBA.MARSHAL("cannot marshal local object");

            ClientDelegate delegate = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) value)
                    ._get_delegate();

            delegate.getInternalIOR().write(this);
        }
    }

    public void write_TypeCode(TypeCode value) {

        if (value == null) {
            throw new org.omg.CORBA.BAD_TYPECODE("null typecode");
        }

        try {
            TypeCodeUtil.write(this, value, new HashMap());

        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
            throw new org.omg.CORBA.BAD_TYPECODE(ex.getMessage());

        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds ex) {
            throw new org.omg.CORBA.BAD_TYPECODE(ex.getMessage());
        }
    }

    public void write_any(Any value) {
        write_TypeCode(value.type());
        value.write_value(this);
    }

    protected SystemException translate_exception(IOException e) {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract int __stream_position();

    //
    // org.omg.CORBA_2_3.portable.OutputStream
    //

    ValueWriter getValueWriter() {
        if (value_writer == null) {
            value_writer = new ValueWriter(this);
        }
        return value_writer;
    }

    public void write_value(java.io.Serializable value) {
        getValueWriter().writeValue(value, (String) null);
    }

    public void write_value(java.io.Serializable value, String id) {
        getValueWriter().writeValue(value, id);
    }

    public void write_value(java.io.Serializable value, Class clz) {
        getValueWriter().writeValue(value, (String) null);
    }

    public void write_value(java.io.Serializable value,
                            org.omg.CORBA.portable.BoxedValueHelper helper)
    {
        getValueWriter().writeValue(value, helper);
    }

    public void write_abstract_interface(java.lang.Object value) {
        if (value == null) {
            write_boolean(false);
            write_long(0);

        } else if (value instanceof org.omg.CORBA.Object) {
            write_boolean(true);
            write_Object((org.omg.CORBA.Object) value);

        } else if (value instanceof java.io.Serializable) {

            //
            // We select on Serializable first, since CORBA stubs are
            // serializable, an that allows RMI/CORBA stubs to be
            // passed around without requiring narrow.
            //

            write_boolean(false);
            write_value((Serializable) value);

        } else {
            throw new org.omg.CORBA.MARSHAL("not a valid abstract object: "
                                            + value.getClass().getName());
        }
    }

    //
    // DataOutputStream
    //

    public String[] _truncatable_ids() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_Abstract(java.lang.Object value) {
        write_abstract_interface(value);
    }

    public void write_Value(java.io.Serializable value) {
        write_value(value);
    }

    public void write_any_array(org.omg.CORBA.Any[] anies, int off, int len) {
        for (int i = 0; i < len; i++) {
            write_any(anies[i + off]);
        }
    }

    // align output to the given size
    public abstract void align(int i);

    public void __fatal(String string) {
        __orb().fatal(string);
    }

    public javax.rmi.CORBA.ValueHandler getValueHandler() {
        return __orb().getValueHandler();
    }

    public EncapsulationOutputStream __open_encapsulation() {
        return new EncapsulationOutputStream(__orb());
    }

    public void __close_encapsulation(EncapsulationOutputStream eout) {
        try {
            eout.writeTo(this);
        }
        catch (IOException e) {
            throw translate_exception(e);
        }
    }
}
