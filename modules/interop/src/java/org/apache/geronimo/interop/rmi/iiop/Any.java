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

import org.omg.CORBA.TCKind;

/**
 * * An implementation of CORBA 'any'.
 */
public class Any
    extends org.omg.CORBA.Any
{
    private org.omg.CORBA.TypeCode _type;

    private byte[] _data;

    /**
     * @param tk
     * @param what
     */
    private void extract_type(TCKind tk, String what)
    {
        if (_type.kind().value() != tk.value())
        {
            throw new org.omg.CORBA.BAD_TYPECODE("org.apache.geronimo.interop.rmi.iiop.Any.extract_"
                + what + ": type = " + _type);
        }
    }

    /**
     *
     */
    public Any()
    {
        _type = TypeCode.NULL;
    }

    /**
     * @param a
     * @return
     */
    public boolean equal(org.omg.CORBA.Any a)
    {
        if (!_type.equal(a.type()))
        {
            return false;
        }
        if (a instanceof org.apache.geronimo.interop.rmi.iiop.Any)
        {
            org.apache.geronimo.interop.rmi.iiop.Any _that = (org.apache.geronimo.interop.rmi.iiop.Any) a;
            String x = org.apache.geronimo.interop.util.Base16Binary.toString(this._data);
            String y = org.apache.geronimo.interop.util.Base16Binary.toString(_that._data);
            return x.equals(y);
        }
        else
        {
            // TODO: implement equality testing with other ORB's 'any' values
            throw new org.omg.CORBA.NO_IMPLEMENT("org.apache.geronimo.interop.rmi.iiop.Any.equal("
                + a.getClass().getName() + ")");
        }
    }

    /**
     * @return
     */
    public org.omg.CORBA.TypeCode type()
    {
        return _type;
    }

    /**
     * @param type
     */
    public void type(org.omg.CORBA.TypeCode type)
    {
        _type = type;
    }

    /**
     * @param input
     * @param type
     */
    public void read_value(org.omg.CORBA.portable.InputStream input,
                           org.omg.CORBA.TypeCode type)
    {
        byte[] buffer = ((CdrInputStream) input)._buffer;
        int length = ((CdrInputStream) input)._length;
        _type = type;
        _data = new byte[length];
        System.arraycopy(buffer, 0, _data, 0, length);
    }

    /**
     * @param output
     */
    public void write_value(org.omg.CORBA.portable.OutputStream output)
    {
        // A no-op in this implementation.
    }

    /**
     * @return
     */
    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        _data = null;
        return CdrOutputStream.getInstance();
    }

    /**
     * @return
     */
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        if (_data == null)
        {
            throw new org.omg.CORBA.BAD_OPERATION(
                "org.apache.geronimo.interop.rmi.Any.create_input_stream");
        }
        return CdrInputStream.getInstance();
    }

    /**
     * @return
     */
    public short extract_short()
    {
        extract_type(TCKind.tk_short, "short");
        return create_input_stream().read_short();
    }

    /**
     * @param value
     */
    public void insert_short(short value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_short(value);
        read_value(null, TypeCode.SHORT);
    }

    /**
     * @return
     */
    public int extract_long()
    {
        extract_type(TCKind.tk_long, "long");
        return create_input_stream().read_long();
    }

    /**
     * @param value
     */
    public void insert_long(int value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_long(value);
        read_value(null, TypeCode.LONG);
    }

    /**
     * @return
     */
    public long extract_longlong()
    {
        extract_type(TCKind.tk_longlong, "longlong");
        return create_input_stream().read_longlong();
    }

    /**
     * @param value
     */
    public void insert_longlong(long value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_longlong(value);
        read_value(null, TypeCode.LONGLONG);
    }

    /**
     * @return
     */
    public short extract_ushort()
    {
        extract_type(TCKind.tk_ushort, "ushort");
        return create_input_stream().read_ushort();
    }

    /**
     * @param value
     */
    public void insert_ushort(short value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_ushort(value);
        read_value(null, TypeCode.USHORT);
    }

    /**
     * @return
     */
    public int extract_ulong()
    {
        extract_type(TCKind.tk_ulong, "ulong");
        return create_input_stream().read_ulong();
    }

    /**
     * @param value
     */
    public void insert_ulong(int value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_ulong(value);
        read_value(null, TypeCode.ULONG);
    }

    /**
     * @return
     */
    public long extract_ulonglong()
    {
        extract_type(TCKind.tk_ulonglong, "ulonglong");
        return create_input_stream().read_ulonglong();
    }

    /**
     * @param value
     */
    public void insert_ulonglong(long value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_ulonglong(value);
        read_value(null, TypeCode.ULONGLONG);
    }

    /**
     * @return
     */
    public float extract_float()
    {
        extract_type(TCKind.tk_float, "float");
        return create_input_stream().read_float();
    }

    /**
     * @param value
     */
    public void insert_float(float value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_float(value);
        read_value(null, TypeCode.FLOAT);
    }

    /**
     * @return
     */
    public double extract_double()
    {
        extract_type(TCKind.tk_double, "double");
        return create_input_stream().read_double();
    }

    /**
     * @param value
     */
    public void insert_double(double value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_double(value);
        read_value(null, TypeCode.DOUBLE);
    }

    /**
     * @return
     */
    public boolean extract_boolean()
    {
        extract_type(TCKind.tk_boolean, "boolean");
        return create_input_stream().read_boolean();
    }

    /**
     * @param value
     */
    public void insert_boolean(boolean value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_boolean(value);
        read_value(null, TypeCode.BOOLEAN);
    }

    /**
     * @return
     */
    public char extract_char()
    {
        extract_type(TCKind.tk_char, "char");
        return create_input_stream().read_char();
    }

    /**
     * @param value
     */
    public void insert_char(char value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_char(value);
        read_value(null, TypeCode.CHAR);
    }

    /**
     * @return
     */
    public char extract_wchar()
    {
        extract_type(TCKind.tk_wchar, "wchar");
        return create_input_stream().read_wchar();
    }

    /**
     * @param value
     */
    public void insert_wchar(char value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_wchar(value);
        read_value(null, TypeCode.WCHAR);
    }

    /**
     * @return
     */
    public byte extract_octet()
    {
        extract_type(TCKind.tk_octet, "octet");
        return create_input_stream().read_octet();
    }

    /**
     * @param value
     */
    public void insert_octet(byte value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_octet(value);
        read_value(null, TypeCode.OCTET);
    }

    /**
     * @return
     */
    public org.omg.CORBA.Any extract_any()
    {
        extract_type(TCKind.tk_any, "any");
        return create_input_stream().read_any();
    }

    /**
     * @param value
     */
    public void insert_any(org.omg.CORBA.Any value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_any(value);
        read_value(null, TypeCode.ANY);
    }

    /**
     * @return
     */
    public org.omg.CORBA.Object extract_Object()
    {
        extract_type(TCKind.tk_objref, "Object");
        org.omg.CORBA.Object obj = create_input_stream().read_Object();
        return obj;
    }

    /**
     * @param value
     */
    public void insert_Object(org.omg.CORBA.Object value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_Object(value);
        read_value(null, TypeCode.OBJREF);
    }

    /**
     * @param value
     * @param type
     */
    public void insert_Object(org.omg.CORBA.Object value,
                              org.omg.CORBA.TypeCode type)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_Object(value);
        read_value(null, type);
    }

    /**
     * @return
     */
    public java.io.Serializable extract_Value()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @param v
     */
    public void insert_Value(java.io.Serializable v)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @param v
     * @param t
     */
    public void insert_Value(java.io.Serializable v, org.omg.CORBA.TypeCode t)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @return
     */
    public String extract_string()
    {
        extract_type(TCKind.tk_string, "string");
        return create_input_stream().read_string();
    }

    /**
     * @param value
     */
    public void insert_string(String value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_string(value);
        read_value(null, TypeCode.STRING);
    }

    /**
     * @return
     */
    public String extract_wstring()
    {
        extract_type(TCKind.tk_wstring, "wstring");
        return create_input_stream().read_wstring();
    }

    /**
     * @param value
     */
    public void insert_wstring(String value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_wstring(value);
        read_value(null, TypeCode.WSTRING);
    }

    /**
     * @return
     */
    public org.omg.CORBA.TypeCode extract_TypeCode()
    {
        extract_type(TCKind.tk_TypeCode, "TypeCode");
        return create_input_stream().read_TypeCode();
    }

    /**
     * @param value
     */
    public void insert_TypeCode(org.omg.CORBA.TypeCode value)
    {
        org.omg.CORBA.portable.OutputStream output = create_output_stream();
        output.write_TypeCode(value);
        read_value(null, TypeCode.TYPECODE);
    }

    /**
     * @return
     */
    public org.omg.CORBA.Principal extract_Principal()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(
            "org.apache.geronimo.interop.rmi.iiop.Any.extract_Principal");
    }

    public void insert_Principal(org.omg.CORBA.Principal value)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(
            "org.apache.geronimo.interop.rmi.iiop.Any.insert_Principal");
    }

    // Don't implement insert_Streamable and extract_Streamable since from
    // a TypeCode it appears to be impossible to determine the holder class
    // name (in the general case) in order to construct a Streamable object
    // for return from extract_Streamable.
    /**
     * @return
     * @throws org.omg.CORBA.BAD_INV_ORDER
     */
    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @param s
     */
    public void insert_Streamable(org.omg.CORBA.portable.Streamable s)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * * Construct an Any from a TypeCode and a String value
     * * (supported for boolean and numeric primitive IDL types only).
     */
    /**
     * @param type
     * @param value
     */
    public Any(org.omg.CORBA.TypeCode type, String value)
    {
        try
        {
            _type = type;
            switch (_type.kind().value())
            {
                case TCKind._tk_boolean:
                    if (value.equals("0"))
                    {
                        insert_boolean(false);
                    }
                    else if (value.equals("1"))
                    {
                        insert_boolean(true);
                    }
                    else
                    {
                        insert_boolean(Boolean.valueOf(value).booleanValue());
                    }
                    break;
                case TCKind._tk_octet:
                    // Don't use class Byte as it isn't present in JDK 1.0.2
                    insert_octet((byte) parse(value, 0, 255));
                    break;
                case TCKind._tk_short:
                    // Don't use class Short as it isn't present in JDK 1.0.2
                    insert_short((short) parse(value, -32768, 32767));
                    break;
                case TCKind._tk_ushort:
                    // Don't use class Short as it isn't present in JDK 1.0.2
                    insert_ushort((short) parse(value, 0, 65535));
                    break;
                case TCKind._tk_long:
                    insert_long((int) parse(value, -2147483648, 2147483647));
                    break;
                case TCKind._tk_ulong:
                    insert_ulong((int) parse(value, 0, 4294967295L));
                    break;
                case TCKind._tk_longlong:
                    insert_longlong(Long.parseLong(value));
                    break;
                case TCKind._tk_ulonglong:
                    // Note: doesn't handle "unsigned long" values >= 2^63. Fix
                    // this if reported by customers.
                    insert_ulonglong(Long.parseLong(value));
                    break;
                case TCKind._tk_float:
                    insert_float(Float.valueOf(value).floatValue());
                    break;
                case TCKind._tk_double:
                    insert_double(Double.valueOf(value).doubleValue());
                    break;
                default:
                    throw new org.omg.CORBA.BAD_PARAM(value);
            }
        }
        catch (NumberFormatException nfe)
        {
            throw new org.omg.CORBA.BAD_PARAM(value + " - " + nfe.toString());
        }
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return
     * @throws NumberFormatException
     */
    private long parse(String value, long min, long max)
        throws NumberFormatException
    {
        long n = Long.parseLong(value);
        if (n < min || n > max)
        {
            throw new NumberFormatException(value +
                " is not in range ["
                + min + ".." + max + "]");
        }
        return n;
    }

    /**
     * @return
     */
    public String toString()
    {
        switch (_type.kind().value())
        {
            case TCKind._tk_any:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_wchar:
            case TCKind._tk_octet:
            case TCKind._tk_short:
            case TCKind._tk_ushort:
            case TCKind._tk_long:
            case TCKind._tk_ulong:
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_string:
            case TCKind._tk_wstring:
            case TCKind._tk_objref:
                return value();
            default:
                // TODO: traverse structure to produce printable output
                return _type.toString();
        }
    }

    /**
     * @return
     */
    private String value()
    {
        switch (_type.kind().value())
        {
            case TCKind._tk_any:
                return "" + extract_any();
            case TCKind._tk_boolean:
                return extract_boolean() ? "TRUE" : "FALSE";
            case TCKind._tk_char:
                return "'" + extract_char() + "'";
            case TCKind._tk_wchar:
                return "'" + extract_wchar() + "'";
            case TCKind._tk_octet:
                return "" + extract_octet();
            case TCKind._tk_short:
                return "" + extract_short();
            case TCKind._tk_ushort:
                return "" + extract_ushort();
            case TCKind._tk_long:
                return "" + extract_long();
            case TCKind._tk_ulong:
                return "" + extract_ulong();
            case TCKind._tk_longlong:
                return "" + extract_longlong();
            case TCKind._tk_ulonglong:
                return "" + extract_ulonglong();
            case TCKind._tk_float:
                return "" + extract_float();
            case TCKind._tk_double:
                return "" + extract_double();
            case TCKind._tk_string:
                return "\"" + extract_string() + "\"";
            case TCKind._tk_wstring:
                return "\"" + extract_wstring() + "\"";
            case TCKind._tk_objref:
                return extract_Object().toString();
            case TCKind._tk_TypeCode:
                return "" + extract_TypeCode();
            default:
                return "?";
        }
    }
}
