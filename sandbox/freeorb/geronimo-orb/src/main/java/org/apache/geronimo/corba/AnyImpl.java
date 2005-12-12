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
package org.apache.geronimo.corba;

import java.io.IOException;

import org.omg.CORBA.Any;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;

import org.apache.geronimo.corba.TypeCodeImpl.EquivalenceContext;


public class AnyImpl extends org.omg.CORBA.Any {

    transient AbstractORB orb;

    org.omg.CORBA.TypeCode type;

    java.lang.Object _value;

    org.omg.CORBA.portable.OutputStream _out;

    Object getValue() {
        if (_value == null) {

            if (_out != null) {
                read_value(_out.create_input_stream(), type);
                _out = null;
            }
        }

        return _value;
    }

    void setValue(Object value) {
        _value = value;
        _out = null;
    }

    void setOut(org.omg.CORBA.portable.OutputStream out) {
        _value = null;
        _out = out;
    }

    org.omg.CORBA.ORB __get_orb() {
        if (orb == null) {
            orb = new SingletonORB();
        }

        return orb;
    }

    AnyImpl() {
    }

    public AnyImpl(AbstractORB orb) {
        this();
        this.orb = orb;
        set_primitive(TCKind.tk_null);
    }

    public String toString() {
        return "Any{type=" + type + ", value=" + getValue() + "}";
    }

    public TypeCode type() {
        return type;
    }

    public void type(TypeCode tc) {
        type = tc;
        _value = null;
    }

    private void set_primitive(TCKind kind) {
        type = TypeCodeUtil.get_primitive_tc(kind);
    }

    private void check_primitive(TCKind kind, boolean nullOK) {
        if (type.kind().value() != kind.value())
            throw new org.omg.CORBA.BAD_OPERATION("wrong kind");
        if (!nullOK && getValue() == null)
            throw new org.omg.CORBA.BAD_OPERATION("value is null");
    }

    public void read_value(org.omg.CORBA.portable.InputStream in,
                           org.omg.CORBA.TypeCode tc)
    {

        type = tc;
        int kind = tc.kind().value();

        if (_value != null
            && _value instanceof org.omg.CORBA.portable.Streamable
            && kind != TCKind._tk_value && kind != TCKind._tk_value_box
            && kind != TCKind._tk_abstract_interface)
        {
            ((org.omg.CORBA.portable.Streamable) _value)._read(in);
            return;
        }

        switch (kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
                setValue(null);
                return;

            case TCKind._tk_short:
                insert_short(in.read_short());
                return;

            case TCKind._tk_long:
                insert_long(in.read_long());
                return;

            case TCKind._tk_longlong:
                insert_longlong(in.read_longlong());
                return;

            case TCKind._tk_ushort:
                insert_ushort(in.read_ushort());
                return;

            case TCKind._tk_ulong:
                insert_ulong(in.read_ulong());
                return;

            case TCKind._tk_ulonglong:
                insert_ulonglong(in.read_ulonglong());
                return;

            case TCKind._tk_float:
                insert_float(in.read_float());
                return;

            case TCKind._tk_double:
                insert_double(in.read_double());
                return;

            case TCKind._tk_boolean:
                insert_boolean(in.read_boolean());
                return;

            case TCKind._tk_char:
                insert_char(in.read_char());
                return;

            case TCKind._tk_wchar:
                insert_wchar(in.read_wchar());
                return;

            case TCKind._tk_octet:
                insert_octet(in.read_octet());
                return;

            case TCKind._tk_any:
                insert_any(in.read_any());
                return;

            case TCKind._tk_TypeCode:
                insert_TypeCode(in.read_TypeCode());
                return;

            case TCKind._tk_objref:
                insert_Object(in.read_Object());
                return;

            case TCKind._tk_union:
            case TCKind._tk_array:
                throw new org.omg.CORBA.NO_IMPLEMENT();

            case TCKind._tk_except: {
                in.read_string();
            }
            // fall through

            case TCKind._tk_struct: {
                try {
                    int count = tc.member_count();
                    AnyImpl[] valuearr = new AnyImpl[count];
                    for (int i = 0; i < count; i++) {
                        TypeCode elem_tc = tc.member_type(i);
                        valuearr[i] = new AnyImpl(orb);
                        valuearr[i].read_value(in, elem_tc);
                    }
                    setValue(valuearr);

                }
                catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                    throw new org.omg.CORBA.BAD_PARAM("not struct?");

                }
                catch (org.omg.CORBA.TypeCodePackage.Bounds ex) {
                    throw new org.omg.CORBA.BAD_PARAM("concurrent mod");
                }
                return;
            }

            case TCKind._tk_sequence: {
                int len = in.read_ulong();
                TypeCode content = null;
                try {
                    content = tc.content_type();
                }
                catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                    throw new org.omg.CORBA.BAD_PARAM("not sequence?");
                }

                AnyImpl[] valuearr = new AnyImpl[len];
                for (int i = 0; i < len; i++) {
                    valuearr[i] = new AnyImpl(orb);
                    valuearr[i].read_value(in, content);
                }

                setValue(valuearr);
                return;
            }

            case TCKind._tk_value:
            case TCKind._tk_value_box:
                insert_Value(((org.omg.CORBA_2_3.portable.InputStream) in)
                        .read_value(), tc);
                return;

            case TCKind._tk_abstract_interface:
                type = tc;
                setValue(((org.omg.CORBA_2_3.portable.InputStream) in)
                        .read_abstract_interface());
                return;

            case TCKind._tk_enum:
                insert_ulong(in.read_ulong());
                type = tc;
                return;

            case TCKind._tk_string:
                insert_string(in.read_string());
                type = tc;
                return;

            case TCKind._tk_wstring:
                insert_wstring(in.read_wstring());
                type = tc;
                return;

            case TCKind._tk_alias:
                try {
                    read_value(in, tc.content_type());
                    type = tc;
                }
                catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                    throw new org.omg.CORBA.INTERNAL("not alias?");
                }
                return;

            default:
                throw new org.omg.CORBA.NO_IMPLEMENT(tc.toString());
        }
    }

    public void write_value(org.omg.CORBA.portable.OutputStream out) {
        int kind = type.kind().value();

        if (_value != null
            && _value instanceof org.omg.CORBA.portable.Streamable
            && kind != TCKind._tk_value && kind != TCKind._tk_value_box
            && kind != TCKind._tk_abstract_interface)
        {
            ((org.omg.CORBA.portable.Streamable) _value)._write(out);
            return;
        }

        TypeCode tc = type;
        while (kind == TCKind._tk_alias) {
            try {
                tc = tc.content_type();
                kind = tc.kind().value();
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                throw new org.omg.CORBA.INTERNAL("not alias");
            }
        }

        switch (kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
                return;

            case TCKind._tk_short:
                out.write_short(extract_short());
                return;

            case TCKind._tk_long:
                out.write_long(extract_long());
                return;

            case TCKind._tk_longlong:
                out.write_longlong(extract_longlong());
                return;

            case TCKind._tk_ushort:
                out.write_ushort(extract_ushort());
                return;

            case TCKind._tk_ulong:
                out.write_ulong(extract_ulong());
                return;

            case TCKind._tk_ulonglong:
                out.write_ulonglong(extract_ulonglong());
                return;

            case TCKind._tk_float:
                out.write_float(extract_float());
                return;

            case TCKind._tk_double:
                out.write_double(extract_double());
                return;

            case TCKind._tk_boolean:
                out.write_boolean(extract_boolean());
                return;

            case TCKind._tk_char:
                out.write_char(extract_char());
                return;

            case TCKind._tk_wchar:
                out.write_wchar(extract_wchar());
                return;

            case TCKind._tk_octet:
                out.write_octet(extract_octet());
                return;

            case TCKind._tk_any:
                out.write_any(extract_any());
                return;

            case TCKind._tk_TypeCode:
                out.write_TypeCode(extract_TypeCode());
                return;

            case TCKind._tk_objref:
                out.write_Object(extract_Object());
                return;

            case TCKind._tk_union:
            case TCKind._tk_array:
                throw new org.omg.CORBA.NO_IMPLEMENT();

            case TCKind._tk_sequence: {
                AnyImpl[] valuearr = (AnyImpl[]) getValue();
                out.write_ulong(valuearr.length);
            }
            // fall thorugh

            case TCKind._tk_struct: {
                AnyImpl[] valuearr = (AnyImpl[]) getValue();
                int count = valuearr.length;
                for (int i = 0; i < count; i++) {
                    valuearr[i].write_value(out);
                }
                return;
            }

            case TCKind._tk_except: {
                try {
                    out.write_string(tc.id());
                }
                catch (org.omg.CORBA.TypeCodePackage.BadKind ex) {
                    throw new org.omg.CORBA.BAD_PARAM("not exception?");
                }

                AnyImpl[] valuearr = (AnyImpl[]) getValue();
                int count = valuearr.length;

                for (int i = 0; i < count; i++) {
                    valuearr[i].write_value(out);
                }
                return;
            }

            case TCKind._tk_value:
            case TCKind._tk_value_box:
                ((org.omg.CORBA_2_3.portable.OutputStream) out)
                        .write_value((java.io.Serializable) getValue());
                return;

            case TCKind._tk_abstract_interface:
                ((org.omg.CORBA_2_3.portable.OutputStream) out)
                        .write_abstract_interface(getValue());
                return;

            case TCKind._tk_enum:
                out.write_ulong(((Integer) getValue()).intValue());
                return;

            case TCKind._tk_string:
                out.write_string(extract_string());
                return;

            case TCKind._tk_wstring:
                out.write_wstring(extract_wstring());
                return;

            default:
                throw new org.omg.CORBA.INTERNAL("unexpected TypeCode");
        }
    }

    /**
     * @deprecated
     */
    public void insert_Principal(org.omg.CORBA.Principal p) {
        set_primitive(TCKind.tk_Principal);
        setValue(p);
    }

    /**
     * @deprecated
     */
    public org.omg.CORBA.Principal extract_Principal() {
        check_primitive(TCKind.tk_Principal, false);
        return (org.omg.CORBA.Principal) getValue();
    }

    public void insert_TypeCode(org.omg.CORBA.TypeCode tc) {
        set_primitive(TCKind.tk_TypeCode);
        setValue(tc);
    }

    public org.omg.CORBA.TypeCode extract_TypeCode() {
        check_primitive(TCKind.tk_TypeCode, false);
        return (TypeCode) getValue();
    }

    public void insert_Object(org.omg.CORBA.Object o, org.omg.CORBA.TypeCode tc) {
        type = tc;
        setValue(o);
    }

    public void insert_Object(org.omg.CORBA.Object o) {
        insert_Object(o, TypeCodeUtil.get_primitive_tc(TCKind.tk_objref));
    }

    public org.omg.CORBA.Object extract_Object() {
        check_primitive(TCKind.tk_objref, true);
        return (org.omg.CORBA.Object) getValue();
    }

    public void insert_wstring(String v) {
        set_primitive(TCKind.tk_wstring);
        setValue(v);
    }

    public String extract_wstring() {
        check_primitive(TCKind.tk_wstring, true);
        return (String) getValue();
    }

    public void insert_string(String v) {
        set_primitive(TCKind.tk_string);
        setValue(v);
    }

    public String extract_string() {
        check_primitive(TCKind.tk_string, true);
        return (String) getValue();
    }

    public void insert_short(short v) {
        set_primitive(TCKind.tk_short);
        setValue(new Short(v));
    }

    public short extract_short() {
        check_primitive(TCKind.tk_short, false);
        return ((Short) getValue()).shortValue();
    }

    public void insert_ushort(short v) {
        set_primitive(TCKind.tk_ushort);
        setValue(new Short(v));
    }

    public short extract_ushort() {
        check_primitive(TCKind.tk_ushort, false);
        return ((Short) getValue()).shortValue();
    }

    public void insert_any(org.omg.CORBA.Any v) {
        set_primitive(TCKind.tk_any);
        setValue(v);
    }

    public org.omg.CORBA.Any extract_any() {
        check_primitive(TCKind.tk_any, false);
        return (AnyImpl) getValue();
    }

    public void insert_wchar(char v) {
        set_primitive(TCKind.tk_wchar);
        setValue(new Character(v));
    }

    public char extract_wchar() {
        check_primitive(TCKind.tk_wchar, false);
        return ((Character) getValue()).charValue();
    }

    public void insert_char(char v) {
        set_primitive(TCKind.tk_char);
        setValue(new Character(v));
    }

    public char extract_char() {
        check_primitive(TCKind.tk_char, false);
        return ((Character) getValue()).charValue();
    }

    public void insert_octet(byte v) {
        set_primitive(TCKind.tk_octet);
        setValue(new Byte(v));
    }

    public byte extract_octet() {
        check_primitive(TCKind.tk_octet, false);
        return ((Byte) getValue()).byteValue();
    }

    public void insert_boolean(boolean v) {
        set_primitive(TCKind.tk_boolean);
        setValue(new Boolean(v));
    }

    public boolean extract_boolean() {
        check_primitive(TCKind.tk_boolean, false);
        return ((Boolean) getValue()).booleanValue();
    }

    public void insert_double(double v) {
        set_primitive(TCKind.tk_double);
        setValue(new Double(v));
    }

    public double extract_double() {
        check_primitive(TCKind.tk_double, false);
        return ((Double) getValue()).doubleValue();
    }

    public void insert_float(float v) {
        set_primitive(TCKind.tk_float);
        setValue(new Float(v));
    }

    public float extract_float() {
        check_primitive(TCKind.tk_float, false);
        return ((Float) getValue()).floatValue();
    }

    public void insert_long(int v) {
        set_primitive(TCKind.tk_long);
        setValue(new Integer(v));
    }

    public int extract_long() {
        check_primitive(TCKind.tk_long, false);
        return ((Integer) getValue()).intValue();
    }

    public void insert_ulong(int v) {
        set_primitive(TCKind.tk_ulong);
        setValue(new Integer(v));
    }

    public int extract_ulong() {
        check_primitive(TCKind.tk_ulong, false);
        return ((Integer) getValue()).intValue();
    }

    public void insert_longlong(long v) {
        set_primitive(TCKind.tk_longlong);
        setValue(new Long(v));
    }

    public long extract_longlong() {
        check_primitive(TCKind.tk_longlong, false);
        return ((Number) getValue()).longValue();
    }

    public void insert_ulonglong(long v) {
        set_primitive(TCKind.tk_ulonglong);
        setValue(new Long(v));
    }

    public long extract_ulonglong() {
        check_primitive(TCKind.tk_ulonglong, false);
        return ((Long) getValue()).longValue();
    }

    public void insert_Streamable(org.omg.CORBA.portable.Streamable val) {
        type = val._type();
        setValue(val);
    }

    public void insert_Value(java.io.Serializable value) {
        insert_Value(value, TypeCodeUtil.get_primitive_tc(TCKind.tk_value));
    }

    public void insert_Value(java.io.Serializable val, TypeCode tc) {
        type = tc;
        setValue(val);
    }

    public java.io.Serializable extract_Value() {
        TCKind kind = type.kind();

        if (kind == TCKind.tk_value || kind == TCKind.tk_value_box
            || kind == TCKind.tk_abstract_interface)
        {
            return (java.io.Serializable) getValue();
        } else {
            throw new org.omg.CORBA.BAD_OPERATION("TypeCode is not value");
        }
    }

    public org.omg.CORBA.portable.InputStream create_input_stream() {
        if (_out != null) {
            return _out.create_input_stream();

        } else {
            // org.omg.CORBA.portable.OutputStream out = create_output_stream
            // ();

            org.omg.CORBA.portable.OutputStream out;

            if (orb instanceof ORB) {
                out = __get_orb().create_output_stream();
            } else {
                // out = new DynamicOutputStream();
                throw new NO_IMPLEMENT();
            }

            write_value(out);
            return out.create_input_stream();
        }
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        org.omg.CORBA.portable.OutputStream out;

        if (orb instanceof ORB) {
            out = __get_orb().create_output_stream();
        } else {
            // out = new DynamicOutputStream();
            throw new NO_IMPLEMENT();
        }

        setOut(out);

        return out;
    }

    /**
     * compare this Any to another for equality
     */
    public boolean equal(org.omg.CORBA.Any other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        return equal(other, new EquivalenceContext());
    }

    boolean equal(Any other, EquivalenceContext ctx) {

        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (type != other.type()) {

            if (type instanceof TypeCodeImpl) {
                if (!((TypeCodeImpl) type).equal(other.type(), ctx)) {
                    return false;
                }
            } else if (other.type() instanceof TypeCodeImpl) {
                if (!((TypeCodeImpl) other.type()).equal(type, ctx)) {
                    return false;
                }
            } else {
                if (!type.equal(other.type())) {
                    return false;
                }
            }

        }

        InputStream b1 = this.create_input_stream();
        InputStream b2 = other.create_input_stream();

        int available1 = 0;
        int available2 = 0;
        try {
            available1 = b1.available();
            available2 = b2.available();
        }
        catch (IOException e) {
            return false;
        }

        if (available1 != available2) {
            return false;
        }

        byte[] data1 = new byte[available1];
        b1.read_octet_array(data1, 0, data1.length);

        byte[] data2 = new byte[available2];
        b2.read_octet_array(data2, 0, data2.length);

        return java.util.Arrays.equals(data1, data2);
    }

}
