/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop;

public abstract class StringSeqHelper
{
    public static java.lang.String[] clone
        (java.lang.String[] _value)
    {
        if (_value == null)
        {
            return null;
        }
        int _16 = _value.length;
        java.lang.String[] _clone = new java.lang.String[_16];
        for (int _17 = 0; _17 < _16; _17++)
        {
            _clone[_17] = _value[_17];
        }
        return _clone;
    }

    public static java.lang.String[] read
        (org.omg.CORBA.portable.InputStream _input)
    {
        int _18 = _input.read_ulong();
        java.lang.String[] value = new java.lang.String[_18];
        for (int _19 = 0; _19 < _18; _19++)
        {
            value[_19] = _input.read_string();
        }
        return value;
    }

    public static void write
        (org.omg.CORBA.portable.OutputStream _output,
        java.lang.String[] value)
    {
        if (value == null)
        {
            value = new java.lang.String[0];
        }
        int _20 = value.length;
        _output.write_ulong(_20);
        for (int _21 = 0; _21 < _20; _21++)
        {
            _output.write_string(value[_21]);
        }
    }

    public static org.omg.CORBA.TypeCode _type;

    public static org.omg.CORBA.TypeCode type()
    {
        if (_type == null)
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
            _type = orb.create_sequence_tc(0, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
        }
        return _type;
    }

    public static void insert
        (org.omg.CORBA.Any any,
        java.lang.String[] value)
    {
        org.omg.CORBA.portable.OutputStream output = any.create_output_stream();
        write(output, value);
        any.read_value(output.create_input_stream(), type());
    }

    public static java.lang.String[] extract
        (org.omg.CORBA.Any any)
    {
        if (! any.type().equal(type()))
        {
            throw new org.omg.CORBA.BAD_OPERATION();
        }
        return read(any.create_input_stream());
    }

    public static java.lang.String id()
    {
        return "IDL:org/apache/geronimo/interop/rmi/iiop/StringSeq:1.0";
    }
}
