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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;


class TypeCodeImpl extends TypeCode {

    public static class EquivalenceContext {

        Map eqmap = new IdentityHashMap();

        void add(TypeCode t1, TypeCode t2) {
            _add(t1, t2);
            _add(t2, t1);
        }

        private void _add(TypeCode t1, TypeCode t2) {
            Map eqset = (Map) eqmap.get(t1);
            if (eqset == null) {
                eqset = new IdentityHashMap();
                eqmap.put(t1, eqset);
            }

            Integer count = (Integer) eqset.get(t2);
            if (count == null) {
                count = new Integer(1);
                eqset.put(t2, count);
            } else {
                eqset.put(t2, new Integer(count.intValue() + 1));
            }
        }

        void remove(TypeCode t1, TypeCode t2) {

            _remove(t1, t2);
            _remove(t2, t1);

        }

        private void _remove(TypeCode t1, TypeCode t2) {
            Map eqset = (Map) eqmap.get(t1);

            if (eqset == null) return;

            Integer count = (Integer) eqset.get(t2);
            if (count == null) return;

            int value = count.intValue();
            if (value == 1) {
                eqset.remove(t2);
            } else {
                eqset.put(t2, new Integer(value - 1));
            }
        }

        boolean contains(TypeCode t1, TypeCode t2) {
            return _contains(t1, t2) || _contains(t2, t1);
        }

        private boolean _contains(TypeCode t1, TypeCode t2) {
            Map eqset = (Map) eqmap.get(t1);
            if (eqset.containsKey(t2)) {
                return true;
            } else {
                return false;
            }
        }
    }

    TCKind kind;

    // objref, struct, enum, alias, value, valuebox, native,
    // abstract_interface and except
    String id;
    String name;

    // struct, enum, value, except...
    String[] memberNames;
    TypeCodeImpl[] memberTypes;

    // union
    Any[] labels;
    TypeCodeImpl discriminatorType;

    // string, wstring, sequence, array
    int length;

    TypeCodeImpl contentType;

    // fixed
    short fixedDigits;
    short fixedScale;

    // value
    short[] memberVisibility;
    short typeModifier;
    TypeCodeImpl concreteBaseType;

    String recursiveId;
    // TypeCodeImpl recursiveResolved;

    //
    // Public API
    //
    public boolean equal(TypeCode tc) {

        if (tc == null)
            return false;

        if (tc == this)
            return true;

        return equal(tc, new EquivalenceContext());
    }


    //
    // Public API
    //
    public boolean equal(TypeCode tc, EquivalenceContext ctx) {

        if (tc == null)
            return false;

        if (tc == this)
            return true;

        if (ctx.contains(this, tc)) {
            return true;
        }

        if (kind != tc.kind())
            return false;

        try {
            ctx.add(this, tc);
            return __equal(tc, ctx);
        }
        catch (BadKind e) {
            return false;
        }
        catch (Bounds e) {
            return false;
        }
        finally {
            ctx.remove(this, tc);
        }
    }

    protected boolean __equal(TypeCode tc, EquivalenceContext ctx) throws BadKind, Bounds {

        switch (kind.value()) {
            case TCKind._tk_objref:
            case TCKind._tk_struct:
            case TCKind._tk_union:
            case TCKind._tk_enum:
            case TCKind._tk_alias:
            case TCKind._tk_value:
            case TCKind._tk_value_box:
            case TCKind._tk_native:
            case TCKind._tk_abstract_interface:
            case TCKind._tk_except:
                if (id.length() != 0 || tc.id().length() != 0) {
                    return id.equals(tc.id());

                } else if (name.length() != 0 || tc.name().length() != 0) {
                    if (!name.equals(tc.name()))
                        return false;
                }
        }

        switch (kind.value()) {
            case TCKind._tk_struct:
            case TCKind._tk_union:
            case TCKind._tk_value:
            case TCKind._tk_except:
            case TCKind._tk_enum:
                if (memberNames.length != tc.member_count())
                    return false;

                for (int i = 0; i < memberNames.length; i++) {
                    String name1 = memberNames[i];
                    String name2 = tc.member_name(i);

                    if (name1.length() != 0 || name2.length() != 0) {
                        if (!name1.equals(name2))
                            return false;
                    }
                }
        }


        switch (kind.value()) {
            case TCKind._tk_struct:
            case TCKind._tk_union:
            case TCKind._tk_value:
            case TCKind._tk_except:
                if (memberTypes.length != tc.member_count())
                    return false;

                for (int i = 0; i < memberTypes.length; i++) {
                    TypeCodeImpl type1 = memberTypes[i];
                    TypeCode type2 = tc.member_type(i);

                    if (!type1.equal(type2, ctx))
                        return false;
                }
        }

        if (kind.value() == TCKind._tk_union) {
            if (labels.length != tc.member_count())
                return false;

            for (int i = 0; i < labels.length; i++) {

                if (labels[i] instanceof AnyImpl) {

                    if (! ((AnyImpl) labels[i]).equal(tc.member_label(i), ctx))
                        return false;

                } else {

                    if (! labels[i].equal(tc.member_label(i)))
                        return false;
                }
            }

            if (! discriminatorType.equal(tc.discriminator_type(), ctx))
                return false;
        }

        switch (kind.value()) {
            case TCKind._tk_string:
            case TCKind._tk_wstring:
            case TCKind._tk_sequence:
            case TCKind._tk_array:
                if (length != tc.length())
                    return false;
        }

        switch (kind.value()) {
            case TCKind._tk_sequence:
            case TCKind._tk_array:
            case TCKind._tk_value_box:
            case TCKind._tk_alias:
                if (!contentType.equal(tc.content_type()))
                    return false;
        }

        if (kind.value() == TCKind._tk_fixed) {
            if (fixedScale != tc.fixed_scale()
                || fixedDigits != tc.fixed_digits())
            {
                return false;
            }
        }

        if (kind.value() == TCKind._tk_value) {
            if (memberVisibility.length != tc.member_count())
                return false;

            for (int i = 0; i < memberVisibility.length; i++) {
                if (memberVisibility[i] != tc.member_visibility(i)) {
                    return false;
                }
            }

            if (typeModifier != tc.type_modifier())
                return false;

            if (concreteBaseType != null || tc.concrete_base_type() != null) {

                if (concreteBaseType == null || tc.concrete_base_type() == null) {
                    return false;
                }

                if (!concreteBaseType.equal(tc.concrete_base_type()))
                    return false;
            }
        }

        return true;
    }

    public TypeCode content_type()
            throws BadKind
    {
        if (!(kind == org.omg.CORBA.TCKind.tk_sequence ||
              kind == org.omg.CORBA.TCKind.tk_array ||
              kind == org.omg.CORBA.TCKind.tk_value_box ||
              kind == org.omg.CORBA.TCKind.tk_alias))
            throw new BadKind();

        return contentType;
    }

    public String name()
            throws BadKind
    {
        return name;
    }

    public String id() {
        // objref, struct, unionm enum, alias, value, valuebox, native,
        // abstract_interface, except
        return id;
    }

    public int member_count()
            throws BadKind
    {
        return memberNames.length;
    }

    public String member_name(int idx)
            throws BadKind, Bounds
    {
        // struct, union, value, except

        try {
            return memberNames[idx];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new Bounds();
        }
    }

    public TypeCode member_type(int idx)
            throws BadKind, Bounds
    {
        try {
            return memberTypes[idx];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new Bounds();
        }
    }

    public org.omg.CORBA.Any member_label(int idx)
            throws BadKind, Bounds
    {
        if (kind != TCKind.tk_union)
            throw new BadKind();

        try {
            return labels[idx];
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new Bounds();
        }
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
            throws BadKind
    {
        if (kind != TCKind.tk_value)
            throw new BadKind();

        return concreteBaseType;
    }

    public short type_modifier()
            throws BadKind
    {
        if (kind != TCKind.tk_value)
            throw new BadKind();

        return typeModifier;
    }

    public short member_visibility(int index)
            throws BadKind, Bounds
    {
        if (kind != TCKind.tk_value)
            throw new BadKind();

        if (index < 0 || index > memberVisibility.length)
            throw new Bounds();

        return memberVisibility[index];
    }

    public int length()
            throws BadKind
    {
        return length;
    }

    public int default_index()
            throws BadKind
    {
        if (kind != TCKind.tk_union)
            throw new BadKind();

        for (int i = 0; i < labels.length; i++) {
            TypeCode tc = labels[i].type();
            if (tc.kind() == TCKind.tk_octet)
                return i;
        }

        return -1;
    }

    public TypeCode discriminator_type()
            throws BadKind
    {
        if (kind != TCKind.tk_union)
            throw new BadKind();

        return discriminatorType;
    }

    public TCKind kind() {
        return kind;
    }

    void __resolveRecursive() {
        Map codes = new HashMap();
        __resolveRecursive(codes);
    }

    private TypeCodeImpl __resolveRecursive(Map codes) {

        if (recursiveId == null && id != null && !id.equals("")) {
            codes.put(id, this);
        }

        if (memberTypes != null) {
            for (int i = 0; i < memberTypes.length; i++)
                memberTypes[i] = memberTypes[i].__resolveRecursive(codes);
        }

        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                TypeCodeImpl tc = (TypeCodeImpl) labels[i].type();
                if (tc != null)
                    labels[i].type(tc.__resolveRecursive(codes));
            }
        }

        if (discriminatorType != null) {
            discriminatorType = discriminatorType.__resolveRecursive(codes);
        }

        if (contentType != null) {
            contentType = contentType.__resolveRecursive(codes);
        }

        if (concreteBaseType != null) {
            concreteBaseType = concreteBaseType.__resolveRecursive(codes);
        }

        // now resolve...
        if (recursiveId != null) {
            TypeCodeImpl recursiveResolved = (TypeCodeImpl) codes.get(recursiveId);

            if (recursiveResolved == null)
                throw new org.omg.CORBA.BAD_TYPECODE
                        ("cannot resolve recursive typecode " + recursiveId);

            return recursiveResolved;
        } else {
            return this;
        }
    }

    public short fixed_digits()
            throws BadKind
    {
        if (kind.value() != TCKind._tk_fixed) {
            throw new BadKind();
        }

        return fixedDigits;
    }

    public short fixed_scale()
            throws BadKind
    {
        if (kind.value() != TCKind._tk_fixed) {
            throw new BadKind();
        }

        return fixedScale;
    }

    public String toString() {
        switch (kind.value()) {
            case TCKind._tk_null:
                return "null";
            case TCKind._tk_void:
                return "void";
            case TCKind._tk_short:
                return "short";
            case TCKind._tk_long:
                return "long";
            case TCKind._tk_longlong:
                return "longlong";
            case TCKind._tk_ushort:
                return "ushort";
            case TCKind._tk_ulong:
                return "ulong";
            case TCKind._tk_ulonglong:
                return "ulonglong";
            case TCKind._tk_float:
                return "float";
            case TCKind._tk_double:
                return "double";
            case TCKind._tk_longdouble:
                return "longdouble";
            case TCKind._tk_boolean:
                return "boolean";
            case TCKind._tk_char:
                return "char";
            case TCKind._tk_wchar:
                return "wchar";
            case TCKind._tk_octet:
                return "octet";
            case TCKind._tk_any:
                return "any";
            case TCKind._tk_TypeCode:
                return "TypeCode";
            case TCKind._tk_Principal:
                return "Principal";
            case TCKind._tk_fixed:
                return "fixed{digits=" + fixedDigits + "; scale=" + fixedScale + "}";

            case TCKind._tk_objref:
                return "objref{id=" + id + "; name=" + name + "}";

            case TCKind._tk_abstract_interface:
                return "abstract_interface{id=" + id + "; name=" + name + "}";

            case TCKind._tk_native:
                return "native{id=" + id + "; name=" + name + "}";

            case TCKind._tk_struct:
                return "struct{id=" + id + "; name=" + name + "}";

            case TCKind._tk_except:
                return "except{id=" + id + "; name=" + name + "}";

            case TCKind._tk_union:
                return "union{id=" + id + "; name=" + name + "}";

            case TCKind._tk_enum:
                return "enum{id=" + id + "; name=" + name + "}";

            case TCKind._tk_value:
                return "value{id=" + id + "; name=" + name + "}";

            case TCKind._tk_value_box:
                return "valuebox{id=" + id + "; name=" + name + "}";

            case TCKind._tk_string:
                return "string{length=" + length + "}";

            case TCKind._tk_wstring:
                return "wstring{length=" + length + "}";

            case TCKind._tk_sequence:
                return "sequence{length=" + length + "; type" + contentType + "}";

            case TCKind._tk_array:
                return "array{length=" + length + "; type" + contentType + "}";

            default:
                throw new org.omg.CORBA.NO_IMPLEMENT();
        }

    }

    public TypeCode get_compact_typecode() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean equivalent(TypeCode tc) {
        return equals(tc);
    }
}

