/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.util;

import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.TypeCodePackage.Bounds;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class TypeCode    extends org.omg.CORBA.TypeCode
{
    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public static final TypeCode NULL = new TypeCode(TCKind.tk_null);

    public static final TypeCode VOID = new TypeCode(TCKind.tk_void);

    public static final TypeCode ANY = new TypeCode(TCKind.tk_any);

    public static final TypeCode BOOLEAN = new TypeCode(TCKind.tk_boolean);

    public static final TypeCode CHAR = new TypeCode(TCKind.tk_char);

    public static final TypeCode WCHAR = new TypeCode(TCKind.tk_wchar);

    public static final TypeCode OCTET = new TypeCode(TCKind.tk_octet);

    public static final TypeCode SHORT = new TypeCode(TCKind.tk_short);

    public static final TypeCode USHORT = new TypeCode(TCKind.tk_ushort);

    public static final TypeCode LONG = new TypeCode(TCKind.tk_long);

    public static final TypeCode ULONG = new TypeCode(TCKind.tk_ulong);

    public static final TypeCode LONGLONG = new TypeCode(TCKind.tk_longlong);

    public static final TypeCode ULONGLONG = new TypeCode(TCKind.tk_ulonglong);

    public static final TypeCode FLOAT = new TypeCode(TCKind.tk_float);

    public static final TypeCode DOUBLE = new TypeCode(TCKind.tk_double);

    public static final TypeCode LONGDOUBLE = new TypeCode(
        TCKind.tk_longdouble);

    public static final TypeCode STRING = new TypeCode(TCKind.tk_string);

    public static final TypeCode WSTRING = new TypeCode(TCKind.tk_wstring);

    public static final TypeCode OBJREF = new TypeCode(TCKind.tk_objref);

    public static final TypeCode TYPECODE = new TypeCode(TCKind.tk_TypeCode);

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private TCKind _kind;

    private String _name;

    private String _type;

    private String _id;

    private String _label;

    // content type, discriminator type, concrete base type,
    // or other TypeCode for indirection.
    private org.omg.CORBA.TypeCode _ref;

    private String[] _member_name;

    private org.omg.CORBA.TypeCode[] _member_type;

    private org.omg.CORBA.Any[] _member_label;

    private short[] _member_visibility;

    private int _default;

    private int _length;

    private short _digits;

    private short _scale;

    private short _type_modifier;

    private boolean _indirection;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------
    /**
     * @param kind
     */
    public TypeCode(TCKind kind)
    {
        _kind = kind;
        _default = -1;
        if (kind.value() == TCKind._tk_objref)
        {
            _type = "Object";
        }
    }

    public boolean equal(org.omg.CORBA.TypeCode tc)
    {
        if (_indirection)
        {
            return _ref.equal(tc);
        }
        try
        {
            int tk = _kind.value();
            if (tk != tc.kind().value())
            {
                return false;
            }
            // TODO: compare id()
            if (_member_name != null)
            {
                int n = _member_name.length;
                if (n != tc.member_count())
                {
                    return false;
                }
                for (int i = 0; i < n; i++)
                {
                    if (!equalIfNotEmpty(member_name(i), tc.member_name(i)))
                    {
                        return false;
                    }
                    if (!member_type(i).equal(tc.member_type(i)))
                    {
                        return false;
                    }
                }
            }
            if (tk == TCKind._tk_union)
            {
                if (!discriminator_type().equal(tc.discriminator_type()))
                {
                    return false;
                }
                int n = _member_name.length;
                for (int i = 0; i < n; i++)
                {
                    if (!member_label(i).equal(tc.member_label(i)))
                    {
                        return false;
                    }
                }
            }
            if (tk == TCKind._tk_array
                ||
                tk == TCKind._tk_sequence
                ||
                tk == TCKind._tk_string
                || tk == TCKind._tk_wstring)
            {
                if (length() != tc.length())
                {
                    return false;
                }
            }
            if (tk == TCKind._tk_alias
                ||
                tk == TCKind._tk_array
                || tk == TCKind._tk_sequence)
            {
                if (!content_type().equal(tc.content_type()))
                {
                    return false;
                }
            }
            return true;
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
        {
            throw (org.omg.CORBA.UNKNOWN)new org.omg.CORBA.UNKNOWN(ex.toString()).initCause(ex);
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds ex)
        {
            throw (org.omg.CORBA.UNKNOWN)new org.omg.CORBA.UNKNOWN(ex.toString()).initCause(ex);
        }
    }

    public boolean equivalent
        (org.omg.CORBA.TypeCode tc)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    private boolean equalIfNotEmpty(String a, String b)
    {
        if (a.length() == 0 || b.length() == 0)
        {
            return true;
        }
        else
        {
            return a.equals(b);
        }
    }

    public TCKind kind()
    {
        if (_indirection)
        {
            return _ref.kind();
        }
        return _kind;
    }

    /**
     * @return
     * @throws BadKind
     */
    public String id()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.id();
        }
        if (_id != null)
        {
            return _id;
        }
        if (_type != null && _type.equals("Object"))
        {
            return "";
        }
        return default_id();
    }

    // Sybase-internal
    /**
     * @param id
     */
    public void id(String id)
    {
        if (!id.equals(""))
        {
            _id = id;
            if (id.startsWith("IDL:") && id.endsWith(":1.0"))
            {
                // Infer _type field from standard IDL format _id
                id = id.substring(4, id.length() - 4);
                if (id.startsWith("omg.org/"))
                {
                    id = id.substring(8);
                }
                _type = "";
                for (; ;)
                {
                    int slash = id.indexOf('/');
                    if (slash == -1)
                    {
                        break;
                    }
                    _type = _type + id.substring(0, slash) + "::";
                    id = id.substring(slash + 1);
                }
                _type = _type + id;
            }
        }
    }

    /**
     * @return
     * @throws BadKind
     */
    public String name()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.name();
        }
        /* TODO?
        if (_name == null)
        {
            _name = (String)_names.get(new Integer(_kind.value()));
        }
        */
        if (_name == null)
        {
            throw new BadKind();
        }
        return _name;
    }

    // Sybase-internal
    /**
     * @param name
     */
    public void name(String name)
    {
        _name = name;
    }

    /**
     * @return
     * @throws BadKind
     */
    public int member_count()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.member_count();
        }
        if (_member_name == null)
        {
            throw new BadKind();
        }
        return _member_name.length;
    }

    // Sybase-internal
    /**
     * @param count
     */
    public void member_count(int count)
    {
        _member_name = new String[count];
        _member_type = new org.omg.CORBA.TypeCode[count];
        if (_kind.value() == TCKind._tk_union)
        {
            _member_label = new org.omg.CORBA.Any[count];
        }
        if (_kind.value() == TCKind._tk_value)
        {
            _member_visibility = new short[count];
        }
    }

    /**
     * @param index
     * @return
     * @throws BadKind
     * @throws Bounds
     */
    public String member_name(int index)
        throws BadKind, Bounds
    {
        if (_indirection)
        {
            return _ref.member_name(index);
        }
        if (_member_name == null)
        {
            throw new BadKind();
        }
        if (index < 0 || index >= _member_name.length)
        {
            throw new Bounds();
        }
        return _member_name[index];
    }

    // Sybase-internal
    /**
     * @param index
     * @param name
     */
    public void member_name(int index, String name)
    {
        _member_name[index] = name;
    }

    /**
     * @param index
     * @return
     * @throws BadKind
     * @throws Bounds
     */
    public org.omg.CORBA.TypeCode member_type(int index)
        throws BadKind, Bounds
    {
        if (_indirection)
        {
            return _ref.member_type(index);
        }
        if (_member_type == null)
        {
            throw new BadKind();
        }
        if (index < 0 || index >= _member_type.length)
        {
            throw new Bounds();
        }
        return _member_type[index];
    }

    // Sybase-internal
    /**
     * @param index
     * @param type
     */
    public void member_type(int index, org.omg.CORBA.TypeCode type)
    {
        _member_type[index] = type;
    }

    /**
     * @param index
     * @return
     * @throws BadKind
     * @throws Bounds
     */
    public org.omg.CORBA.Any member_label(int index)
        throws BadKind, Bounds
    {
        if (_indirection)
        {
            return _ref.member_label(index);
        }
        if (_member_label == null)
        {
            throw new BadKind();
        }
        if (index < 0 || index >= _member_label.length)
        {
            throw new Bounds();
        }
        return _member_label[index];
    }

    // Sybase-internal
    /**
     * @param index
     * @param label
     */
    public void member_label(int index, org.omg.CORBA.Any label)
    {
        _member_label[index] = label;
    }

    /**
     * @return
     * @throws BadKind
     */
    public org.omg.CORBA.TypeCode discriminator_type()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.discriminator_type();
        }
        if (_ref == null
            || _kind.value() != TCKind._tk_union)
        {
            throw new BadKind();
        }
        return _ref;
    }

    // Sybase-internal
    /**
     * @param disc
     */
    public void discriminator_type(org.omg.CORBA.TypeCode disc)
    {
        _ref = disc;
    }

    /**
     * @return
     * @throws BadKind
     */
    public int default_index()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.default_index();
        }
        if (_kind.value() != TCKind._tk_union)
        {
            throw new BadKind();
        }
        return _default;
    }

    /**
     * @return
     * @throws BadKind
     */
    public int length()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.length();
        }
        int tk = _kind.value();
        if (tk != TCKind._tk_string &&
            tk != TCKind._tk_wstring
            && tk != TCKind._tk_sequence && tk != TCKind._tk_array)
        {
            throw new BadKind();
        }
        return _length;
    }

    // Sybase-internal
    /**
     * @param length
     */
    public void length(int length)
    {
        _length = length;
    }

    /**
     * @return
     * @throws BadKind
     */
    public org.omg.CORBA.TypeCode content_type()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.content_type();
        }
        int tk = _kind.value();
        if (_ref == null
            || (tk != TCKind._tk_alias
            &&
            tk != TCKind._tk_array
            &&
            tk != TCKind._tk_sequence
            && tk != TCKind._tk_value_box))
        {
            throw new BadKind();
        }
        return _ref;
    }

    // Sybase-internal
    /**
     * @param type
     */
    public void content_type(org.omg.CORBA.TypeCode type)
    {
        _ref = type;
    }

    /**
     * @return
     * @throws BadKind
     */
    public short fixed_digits()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.fixed_digits();
        }
        int tk = _kind.value();
        if (tk != TCKind._tk_fixed)
        {
            throw new BadKind();
        }
        return _digits;
    }

    // Sybase-internal
    /**
     * @param digits
     */
    public void fixed_digits(short digits)
    {
        _digits = digits;
    }

    /**
     * @return
     * @throws BadKind
     */
    public short fixed_scale()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.fixed_scale();
        }
        int tk = _kind.value();
        if (tk != TCKind._tk_fixed)
        {
            throw new BadKind();
        }
        return _scale;
    }

    // Sybase-internal
    /**
     * @param scale
     */
    public void fixed_scale(short scale)
    {
        _scale = scale;
    }

    /**
     * @param index
     * @return
     * @throws BadKind
     * @throws Bounds
     */
    public short member_visibility
        (int index)
        throws BadKind, Bounds
    {
        if (_indirection)
        {
            return _ref.member_visibility(index);
        }
        if (_member_type == null)
        {
            throw new BadKind();
        }
        if (index < 0 || index >= _member_visibility.length)
        {
            throw new Bounds();
        }
        return _member_visibility[index];
    }

    // Sybase-internal
    /**
     * @param index
     * @param visibility
     */
    public void member_visibility(int index, short visibility)
    {
        _member_visibility[index] = visibility;
    }

    /**
     * @return
     * @throws BadKind
     */
    public short type_modifier()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.type_modifier();
        }
        int tk = _kind.value();
        if (tk != TCKind._tk_value)
        {
            throw new BadKind();
        }
        return _type_modifier;
    }

    // Sybase-internal
    /**
     * @param modifier
     */
    public void type_modifier(short modifier)
    {
        _type_modifier = modifier;
    }

    /**
     * @return
     * @throws BadKind
     */
    public org.omg.CORBA.TypeCode concrete_base_type()
        throws BadKind
    {
        if (_indirection)
        {
            return _ref.concrete_base_type();
        }
        int tk = _kind.value();
        if (tk != TCKind._tk_value)
        {
            throw new BadKind();
        }
        return _ref;
    }

    // Sybase-internal
    /**
     * @param base
     */
    public void concrete_base_type(org.omg.CORBA.TypeCode base)
    {
        _ref = base;
    }

    // Sybase-internal
    /**
     * @param ref
     */
    public void indirection(org.omg.CORBA.TypeCode ref)
    {
        _ref = ref;
        _indirection = true;
    }

    // Sybase-internal
    /**
     * @param id
     */
    public void recursive(String id)
    {
        _id = id;
        _ref = null;
        _indirection = true;
    }

    // Sybase-internal
    /**
     *
     */
    public void fix_recursive_members()
    {
        String id = _id == null ? default_id() : _id;
        int n = _member_type.length;
        for (int i = 0; i < n; i++)
        {
            TypeCode mt = (TypeCode) _member_type[i];
            if (mt._kind.value() == TCKind._tk_sequence)
            {
                TypeCode ct = (TypeCode) mt._ref;
                if (ct._indirection
                    &&
                    ct._ref == null
                    && ct._id.equals(id))
                {
                    ct._ref = this;
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // private methods
    // -----------------------------------------------------------------------
    private String default_id()
    {
        // Take _type, and generate _id, e.g.
        // if _type = "SessionManager::Manager",
        // then _id = "IDL:SessionManager/Manager:1.0".
        if (_type == null)
        {
            return "";
        }
        StringBuffer id = new StringBuffer(_type.length() + 10);
        id.append("IDL:");
        int n = _type.length();
        for (int i = 0; i < n; i++)
        {
            char c = _type.charAt(i);
            if (c == ':' && i + 1 < n && _type.charAt(i + 1) == ':')
            {
                i++;
            }
            id.append(c == ':' ? '/' : c);
        }
        id.append(":1.0");
        return id.toString();
    }
}
