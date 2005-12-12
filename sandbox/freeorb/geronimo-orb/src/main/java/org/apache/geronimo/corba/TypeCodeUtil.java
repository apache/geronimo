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

import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.VM_ABSTRACT;
import org.omg.CORBA.ValueMember;

import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.EncapsulationOutputStream;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;


public class TypeCodeUtil {

    static TypeCodeImpl create_struct_tc(String id, String name,
                                         StructMember[] members)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_struct;
        tc.id = id;
        tc.name = name;

        tc.memberNames = new String[members.length];
        tc.memberTypes = new TypeCodeImpl[members.length];

        for (int i = 0; i < members.length; i++) {
            tc.memberNames[i] = members[i].name;
            tc.memberTypes[i] = (TypeCodeImpl) members[i].type;
        }

        tc.__resolveRecursive();
        return tc;
    }

    static TypeCodeImpl create_union_tc(String id, String name,
                                        TypeCode discriminator_type, UnionMember[] members)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_union;
        tc.id = id;
        tc.name = name;

        tc.discriminatorType = (TypeCodeImpl) discriminator_type;

        tc.memberNames = new String[members.length];
        tc.memberTypes = new TypeCodeImpl[members.length];
        tc.labels = new AnyImpl[members.length];

        for (int i = 0; i < members.length; i++) {
            tc.memberNames[i] = members[i].name;
            tc.memberTypes[i] = (TypeCodeImpl) members[i].type;
            tc.labels[i] = members[i].label;
        }

        tc.__resolveRecursive();
        return tc;
    }

    static TypeCodeImpl create_enum_tc(String id, String name, String[] members) {
        TypeCodeImpl tc = new TypeCodeImpl();

        tc.kind = TCKind.tk_enum;
        tc.id = id;
        tc.name = name;
        tc.memberNames = new String[members.length];
        for (int i = 0; i < members.length; i++) {
            tc.memberNames[i] = members[i];
        }

        return tc;
    }

    static TypeCodeImpl create_alias_tc(String id, String name,
                                        TypeCode original_type)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_alias;
        tc.id = id;
        tc.name = name;
        tc.contentType = (TypeCodeImpl) original_type;
        return tc;
    }

    static TypeCodeImpl create_exception_tc(String id, String name,
                                            StructMember[] members)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_except;
        tc.id = id;
        tc.name = name;

        tc.memberNames = new String[members.length];
        tc.memberTypes = new TypeCodeImpl[members.length];

        for (int i = 0; i < members.length; i++) {
            tc.memberNames[i] = members[i].name;
            tc.memberTypes[i] = (TypeCodeImpl) members[i].type;
        }

        tc.__resolveRecursive();
        return tc;
    }

    static TypeCodeImpl create_interface_tc(String id, String name) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_objref;
        tc.id = id;
        tc.name = name;
        return tc;
    }

    static TypeCodeImpl create_string_tc(int bound) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_string;
        tc.length = bound;
        return tc;
    }

    static TypeCodeImpl create_wstring_tc(int bound) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_wstring;
        tc.length = bound;
        return tc;
    }

    static TypeCodeImpl create_fixed_tc(short digits, short scale) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    static TypeCodeImpl create_sequence_tc(int bound, TypeCode element_type) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_sequence;
        tc.length = bound;
        tc.contentType = (TypeCodeImpl) element_type;
        return tc;
    }

    static TypeCodeImpl create_recursive_sequence_tc(int bound, int offset) {
        throw new org.omg.CORBA.NO_IMPLEMENT("deprecated");
    }

    static TypeCodeImpl create_array_tc(int length, TypeCode element_type) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    static TypeCodeImpl create_value_tc(String id, String name,
                                        short type_modifier, TypeCode concrete_base,
                                        org.omg.CORBA.ValueMember[] members)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_value;
        tc.id = id;
        tc.name = name;
        tc.typeModifier = type_modifier;
        tc.concreteBaseType = (TypeCodeImpl) concrete_base;
        int count = members.length;
        tc.memberNames = new String[count];
        tc.memberTypes = new TypeCodeImpl[count];
        tc.memberVisibility = new short[count];

        for (int i = 0; i < count; i++) {
            tc.memberNames[i] = members[i].name;
            tc.memberTypes[i] = (TypeCodeImpl) members[i].type;
            tc.memberVisibility[i] = members[i].access;
        }

        if (count > 0) {
            tc.__resolveRecursive();
        }

        return tc;
    }

    static TypeCodeImpl create_value_box_tc(String id, String name,
                                            TypeCode boxed_type)
    {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_value_box;
        tc.id = id;
        tc.name = name;
        tc.contentType = (TypeCodeImpl) boxed_type;

        return tc;
    }

    static TypeCodeImpl create_native_tc(String id, String name) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    static TypeCodeImpl create_recursive_tc(String id) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.recursiveId = id;

        return tc;
    }

    static TypeCodeImpl create_abstract_interface_tc(String id, String name) {
        TypeCodeImpl tc = new TypeCodeImpl();
        tc.kind = TCKind.tk_abstract_interface;

        tc.id = id;
        tc.name = name;

        return tc;
    }

    static TypeCodeImpl[] primitives = new TypeCodeImpl[TCKind._tk_abstract_interface + 1];

    static TypeCodeImpl get_primitive_tc(TCKind kind) {
        int value = kind.value();

        if (primitives[value] != null)
            return primitives[value];

        TypeCodeImpl tc = null;

        switch (value) {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_short:
            case TCKind._tk_long:
            case TCKind._tk_ushort:
            case TCKind._tk_ulong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_octet:
            case TCKind._tk_any:
            case TCKind._tk_TypeCode:
            case TCKind._tk_Principal:
            case TCKind._tk_string:
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_longdouble:
            case TCKind._tk_wchar:
            case TCKind._tk_wstring:
            case TCKind._tk_fixed:
                tc = new TypeCodeImpl();
                tc.kind = kind;
                break;

            case TCKind._tk_objref:
                tc = create_interface_tc("IDL:omg.org/CORBA/Object:1.0", "Object");
                break;

            case TCKind._tk_value:
                tc = create_value_tc("IDL:omg.org/CORBA/ValueBase:1.0",
                                     "ValueBase", VM_ABSTRACT.value, null, new ValueMember[0]);
                break;

            default:
                throw new org.omg.CORBA.BAD_TYPECODE();
        }

        primitives[value] = tc;

        return tc;
    }

    public static TypeCodeImpl read(InputStreamBase in,
                                    InputStreamBase toplevel, java.util.Map map)
    {
        TypeCodeImpl tc;
        int kind = in.read_ulong();
        int pos = toplevel.__stream_position() - 4;
        // boolean swap;

        if (kind == -1) {
            int offset = in.read_long();
            int position = pos + 4 + offset;

            position += (position & 3); // align ?

            tc = (TypeCodeImpl) map.get(new Integer(position));
            if (tc == null) {
                throw new org.omg.CORBA.MARSHAL("invalid TypeCode indirection");
            } else {
                map.put(new Integer(pos), tc);
                return tc;
            }
        }

        EncapsulationInputStream encap_in;

        switch (kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_short:
            case TCKind._tk_long:
            case TCKind._tk_longlong:
            case TCKind._tk_ushort:
            case TCKind._tk_ulong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_longdouble:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_wchar:
            case TCKind._tk_octet:
            case TCKind._tk_any:
            case TCKind._tk_TypeCode:
            case TCKind._tk_Principal:
                tc = get_primitive_tc(TCKind.from_int(kind));
                break;

            case TCKind._tk_fixed:
                tc = create_fixed_tc(in.read_ushort(), in.read_short());
                break;

            case TCKind._tk_objref:
                encap_in = in.__open_encapsulation();
                try {
                    tc = create_interface_tc(encap_in.read_string(), encap_in
                            .read_string());
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }
                break;

            case TCKind._tk_abstract_interface:
                encap_in = in.__open_encapsulation();
                try {
                    tc = create_abstract_interface_tc(encap_in.read_string(),
                                                      encap_in.read_string());
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }
                in.__close_encapsulation(encap_in);
                break;

            case TCKind._tk_native:
                encap_in = in.__open_encapsulation();
                try {
                    tc = create_native_tc(encap_in.read_string(), encap_in
                            .read_string());
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }
                break;

            case TCKind._tk_struct:
            case TCKind._tk_except: {
                encap_in = in.__open_encapsulation();
                try {

                    tc = new TypeCodeImpl();
                    map.put(new Integer(pos), tc);
                    tc.kind = TCKind.from_int(kind);
                    tc.id = encap_in.read_string();
                    tc.name = encap_in.read_string();
                    int count = encap_in.read_ulong();
                    tc.memberNames = new String[count];
                    tc.memberTypes = new TypeCodeImpl[count];
                    for (int i = 0; i < count; i++) {
                        tc.memberNames[i] = encap_in.read_string();
                        tc.memberTypes[i] = read(encap_in, toplevel, map);
                    }

                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;
            }

            case TCKind._tk_union: {
                encap_in = in.__open_encapsulation();
                try {

                    tc = new TypeCodeImpl();
                    map.put(new Integer(pos), tc);
                    tc.kind = TCKind.tk_union;
                    tc.id = encap_in.read_string();
                    tc.name = encap_in.read_string();
                    tc.discriminatorType = read(encap_in, toplevel, map);
                    int index = encap_in.read_long();
                    int count = encap_in.read_ulong();
                    tc.labels = new AnyImpl[count];
                    tc.memberNames = new String[count];
                    tc.memberTypes = new TypeCodeImpl[count];

                    for (int i = 0; i < count; i++) {
                        tc.labels[i] = new AnyImpl(encap_in.__orb());
                        if (i == index) {
                            AnyImpl scrap = new AnyImpl(encap_in.__orb());
                            scrap.read_value(encap_in, tc.discriminatorType);
                            tc.labels[i].insert_octet((byte) 0);
                        } else {
                            tc.labels[i].read_value(encap_in, tc.discriminatorType);
                        }

                        tc.memberNames[i] = encap_in.read_string();
                        tc.memberTypes[i] = read(encap_in, toplevel, map);
                    }

                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;
            }

            case TCKind._tk_enum: {
                encap_in = in.__open_encapsulation();
                try {
                    String id = encap_in.read_string();
                    String name = encap_in.read_string();
                    int count = encap_in.read_ulong();
                    String[] names = new String[count];
                    for (int i = 0; i < count; i++)
                        names[i] = encap_in.read_string();
                    tc = create_enum_tc(id, name, names);

                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;
            }

            case TCKind._tk_string:
                tc = create_string_tc(in.read_ulong());
                break;

            case TCKind._tk_wstring:
                tc = create_wstring_tc(in.read_ulong());
                break;

            case TCKind._tk_sequence:
            case TCKind._tk_array:

                encap_in = in.__open_encapsulation();
                try {
                    tc = new TypeCodeImpl();
                    map.put(new Integer(pos), tc);

                    tc.kind = TCKind.from_int(kind);
                    tc.contentType = read(encap_in, toplevel, map);
                    tc.length = encap_in.read_ulong();
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;

            case TCKind._tk_alias:
                encap_in = in.__open_encapsulation();
                try {
                    tc = create_alias_tc(encap_in.read_string(), encap_in
                            .read_string(), read(encap_in, toplevel, map));
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }
                // in.__swap (swap);
                break;

            case TCKind._tk_value:
                encap_in = in.__open_encapsulation();
                try {

                    tc = new TypeCodeImpl();
                    map.put(new Integer(pos), tc);
                    tc.kind = TCKind.tk_value;
                    tc.id = encap_in.read_string();
                    tc.name = encap_in.read_string();
                    tc.typeModifier = encap_in.read_short();
                    tc.concreteBaseType = read(encap_in, toplevel, map);
                    if (tc.concreteBaseType.kind() == TCKind.tk_null) {
                        tc.concreteBaseType = null;
                    }
                    int count = encap_in.read_ulong();
                    tc.memberNames = new String[count];
                    tc.memberTypes = new TypeCodeImpl[count];
                    tc.memberVisibility = new short[count];

                    for (int i = 0; i < count; i++) {
                        tc.memberNames[i] = encap_in.read_string();
                        tc.memberTypes[i] = read(encap_in, toplevel, map);
                        tc.memberVisibility[i] = encap_in.read_short();
                    }

                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;

            case TCKind._tk_value_box:
                encap_in = in.__open_encapsulation();
                try {

                    tc = create_value_box_tc(encap_in.read_string(), encap_in
                            .read_string(), read(encap_in, toplevel, map));
                }
                finally {
                    in.__close_encapsulation(encap_in);
                }

                break;

            default:
                throw new org.omg.CORBA.NO_IMPLEMENT("read_TypeCode kind=" + kind
                                                     + "(" + Integer.toHexString(kind) + ")");
        }

        map.put(new Integer(pos), tc);
        return tc;
    }

    static final int EMPTY_TC = 1;

    static final int SIMPLE_TC = 2;

    static final int COMPLEX_TC = 3;

    static final int[] typecode_type = new int[]{EMPTY_TC, // null
            EMPTY_TC, // void
            EMPTY_TC, // short
            EMPTY_TC, // long
            EMPTY_TC, // ushort
            EMPTY_TC, // ulong
            EMPTY_TC, // float
            EMPTY_TC, // double
            EMPTY_TC, // boolean
            EMPTY_TC, // char
            EMPTY_TC, // octet
            EMPTY_TC, // any
            EMPTY_TC, // TypeCode
            EMPTY_TC, // Principal

            COMPLEX_TC, // objref
            COMPLEX_TC, // struct
            COMPLEX_TC, // union

            COMPLEX_TC, // enum
            SIMPLE_TC, // string
            COMPLEX_TC, // sequence
            COMPLEX_TC, // array
            COMPLEX_TC, // alias
            COMPLEX_TC, // except

            EMPTY_TC, // longlong
            EMPTY_TC, // ulonglong
            EMPTY_TC, // longdouble
            EMPTY_TC, // wchar

            SIMPLE_TC, // wstring
            SIMPLE_TC, // fixed
            COMPLEX_TC, // value
            COMPLEX_TC, // value_box

            COMPLEX_TC, // abstract_interface
    };

    public static void write(OutputStreamBase out, TypeCode tc, java.util.Map map)
            throws org.omg.CORBA.TypeCodePackage.BadKind,
                   org.omg.CORBA.TypeCodePackage.Bounds
    {

        EncapsulationOutputStream eout;
        Integer pos = (Integer) map.get(tc);
        if (pos != null) {
            out.write_long(-1);
            out.write_long(pos.intValue() - out.__stream_position());
        } else {
        	    out.write_long(tc.kind().value());
            // org.omg.CORBA.TCKindHelper.write(out, tc.kind());
            pos = new Integer(out.__stream_position() - 4);

            switch (tc.kind().value()) {
                case TCKind._tk_null:
                case TCKind._tk_void:
                case TCKind._tk_short:
                case TCKind._tk_long:
                case TCKind._tk_longlong:
                case TCKind._tk_ushort:
                case TCKind._tk_ulong:
                case TCKind._tk_ulonglong:
                case TCKind._tk_float:
                case TCKind._tk_double:
                case TCKind._tk_longdouble:
                case TCKind._tk_boolean:
                case TCKind._tk_char:
                case TCKind._tk_wchar:
                case TCKind._tk_octet:
                case TCKind._tk_any:
                case TCKind._tk_TypeCode:
                case TCKind._tk_Principal:
                    break;

                case TCKind._tk_fixed:
                    map.put(tc, pos);
                    out.write_ushort(tc.fixed_digits());
                    out.write_short(tc.fixed_scale());
                    break;

                case TCKind._tk_objref:
                case TCKind._tk_abstract_interface:
                case TCKind._tk_native:
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();
                    eout.write_string(tc.id());
                    eout.write_string(tc.name());
                    out.__close_encapsulation(eout);
                    break;

                case TCKind._tk_struct:
                case TCKind._tk_except:
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();
                    eout.write_string(tc.id());
                    eout.write_string(tc.name());
                    eout.write_ulong(tc.member_count());
                    for (int i = 0; i < tc.member_count(); i++) {
                        eout.write_string(tc.member_name(i));
                        // recurse
                        write(eout, tc.member_type(i), map);
                    }
                    out.__close_encapsulation(eout);
                    break;

                case TCKind._tk_union: {
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();
                    eout.write_string(tc.id());
                    eout.write_string(tc.name());

                    // write discriminator
                    TypeCodeImpl disc = (TypeCodeImpl) tc.discriminator_type();
                    write(eout, disc, map);

                    int index = tc.default_index();
                    eout.write_long(index);
                    eout.write_ulong(tc.member_count());

                    for (int i = 0; i < tc.member_count(); i++) {

                        if (i == index) {
                            switch (disc.kind().value()) {
                                case TCKind._tk_short:
                                case TCKind._tk_ushort:
                                    eout.write_short((short) 0);
                                    break;

                                case TCKind._tk_long:
                                case TCKind._tk_ulong:
                                    eout.write_long(0);
                                    break;

                                case TCKind._tk_longlong:
                                case TCKind._tk_ulonglong:
                                    eout.write_longlong(0);
                                    break;

                                case TCKind._tk_boolean:
                                    eout.write_boolean(false);
                                    break;

                                case TCKind._tk_char:
                                    eout.write_char((char) 0);
                                    break;

                                case TCKind._tk_enum:
                                    eout.write_ulong(0);
                                    break;

                                default:
                                    throw new org.omg.CORBA.BAD_TYPECODE();

                            }
                        } else {
                            tc.member_label(i).write_value(eout);
                        }

                        eout.write_string(tc.member_name(i));
                        write(eout, tc.member_type(i), map);
                    }

                    out.__close_encapsulation(eout);
                    break;
                }

                case TCKind._tk_enum:
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();
                    eout.write_string(tc.id());
                    eout.write_string(tc.name());
                    eout.write_ulong(tc.member_count());
                    for (int i = 0; i < tc.member_count(); i++) {
                        eout.write_string(tc.member_name(i));
                    }
                    out.__close_encapsulation(eout);
                    break;

                case TCKind._tk_string:
                case TCKind._tk_wstring:
                    out.write_ulong(tc.length());
                    break;

                case TCKind._tk_sequence:
                case TCKind._tk_array:
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();
                    write(out, tc.content_type(), map);
                    out.write_ulong(tc.length());
                    out.__close_encapsulation(eout);
                    break;

                case TCKind._tk_value: {
                    map.put(tc, pos);

                    TypeCode base = tc.concrete_base_type();
                    if (base == null) {
                        base = get_primitive_tc(TCKind.tk_null);
                    }

                    eout = out.__open_encapsulation();

                    eout.write_string(tc.id());
                    eout.write_string(tc.name());
                    eout.write_short(tc.type_modifier());
                    write(eout, base, map);
                    eout.write_ulong(tc.member_count());
                    for (int i = 0; i < tc.member_count(); i++) {
                        eout.write_string(tc.member_name(i));
                        write(eout, tc.member_type(i), map);
                        eout.write_short(tc.member_visibility(i));
                    }
                    out.__close_encapsulation(eout);
                    break;
                }

                case TCKind._tk_value_box:
                    map.put(tc, pos);
                    eout = out.__open_encapsulation();

                    eout.write_string(tc.id());
                    eout.write_string(tc.name());

                    write(eout, tc.content_type(), map);

                    out.__close_encapsulation(eout);
                    break;

                default:
                    throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        }
    }
}
