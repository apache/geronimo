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

import org.apache.geronimo.interop.rmi.*;
import org.apache.geronimo.interop.rmi.iiop.client.*;
import org.apache.geronimo.interop.rmi.iiop.compiler.StubFactory;
import org.apache.geronimo.interop.util.*;
import org.apache.geronimo.interop.IOP.*;
import org.apache.geronimo.interop.GIOP.*;
import org.omg.CORBA.TCKind;

/**
 ** CORBA 2.3 / GIOP 1.2 CDR InputStream.
 **/
public class CdrInputStream extends org.omg.CORBA_2_3.portable.InputStream
{
    //public static final Component component = new Component(CdrInputStream.class);

    public static CdrInputStream getInstance()
    {
        CdrInputStream input = new CdrInputStream(); //(CdrInputStream)component.getInstance();
        input.init(new byte[64], 0, DEFAULT_BUFFER_LENGTH, false);
        return input;
    }

    public static CdrInputStream getInstance(byte[] buffer)
    {
        CdrInputStream input = new CdrInputStream(); //(CdrInputStream)component.getInstance();
        input.init(buffer, 0, buffer.length, false);
        return input;
    }

    public static CdrInputStream getInstance(byte[] buffer, int offset, int length, boolean little)
    {
        CdrInputStream input = new CdrInputStream(); //(CdrInputStream)component.getInstance();
        input.init(buffer, offset, length, little);
        return input;
    }

    public static CdrInputStream getInstanceForEncapsulation()
    {
        return getInstance();
    }

    public static CdrInputStream getPooledInstance()
    {
        CdrInputStream input = null; // (CdrInputStream)_pool.get();
        if (input == null)
        {
            input = getInstance();
        }
        return input;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static final int DEFAULT_BUFFER_LENGTH = 64;

    private static final int MAXIMUM_POOLED_BUFFER_LENGTH = 1024;

    private static boolean RMI_TRACE = true; //SystemProperties.rmiTrace();

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    //private static ThreadLocalInstancePool _pool = new ThreadLocalInstancePool(CdrInputStream.class.getName());

    private GiopMessage _giopMessage;

    private ClientNamingContext _namingContext;

    private boolean _unaligned;

    private byte[] _pooledBuffer;

    // -----------------------------------------------------------------------
    // package-private data
    // -----------------------------------------------------------------------

    byte[] _buffer;

    int _offset;

    int _length;

    boolean _little;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public void init(byte[] buffer, int offset, int length, boolean little)
    {
        _buffer = buffer;
        _offset = offset;
        _length = length;
        _little = little;
    }

    public void recycle()
    {
        reset();
        //_pool.put(this);
    }

    public void reset()
    {
        _offset = 0;
        if (_buffer.length > MAXIMUM_POOLED_BUFFER_LENGTH)
        {
            _buffer = _pooledBuffer;
            _pooledBuffer = null;
            if (_buffer == null)
            {
                _buffer = new byte[DEFAULT_BUFFER_LENGTH];
            }
        }
        _length = _buffer.length;
        _little = false;
        _namingContext = null;
    }

    public void setUnaligned()
    {
        _unaligned = true;
    }

    public byte[] getBytes()
    {
        return ArrayUtil.getBytes(_buffer, 0, _length);
    }

    public byte[] getBuffer()
    {
        return _buffer;
    }

    public int getOffset()
    {
        return _offset;
    }

    public int getGiopVersion()
    {
        GiopMessage message = _giopMessage;
        if (message == null)
        {
            throw new IllegalStateException();
        }
        return message.giopVersion;
    }

    public void setLength(int length)
    {
        if (_buffer.length < length)
        {
            byte[] newBuffer = new byte[length];
            System.arraycopy(_buffer, 0, newBuffer, 0, 12);
            pool(_buffer);
            _buffer = newBuffer;
        }
        _length = length;
    }

    public ClientNamingContext getNamingContext()
    {
        return _namingContext;
    }

    public void setNamingContext(ClientNamingContext namingContext)
    {
        _namingContext = namingContext;
    }

    public void setEncapsulation(byte[] data)
    {
        _buffer = data;
        _offset = 0;
        _length = data.length;
        _little = read_boolean();
    }

    public boolean hasMoreData()
    {
        return _offset < _length;
    }

    /**
     ** Align the buffer offset so the next item is read from at an offset
     ** aligned according to <code>alignment</code>, which must be a
     ** power of 2 (and at least = 1).
     ** <p>Then we check if there is enough space left in the buffer for
     ** an item of <code>size</code> bytes; if not, we throw an
     ** exception.
     **/
    public final void read_align(int alignment, int size)
    {
        if (_unaligned)
        {
            alignment = 1;
        }
        int mask = alignment - 1;
        _offset += (alignment - (_offset & mask)) & mask;
        if (_offset + size <= _length)
        {
            return;
        }
        else
        {
            throw new org.omg.CORBA.MARSHAL("offset (" + _offset + ") + size ("
                + size + ") > buffer length (" + _length + ")");
        }
    }

    /**
     ** Convenience method needed in many places.
     **/
    public byte[] read_octet_sequence()
    {
        int n = read_long();
        if (n == 0)
        {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[n];
        read_octet_array(bytes, 0, n);
        return bytes;
    }

    public GiopMessage read_message()
    {
        return receive_message(null, null);
    }

    public GiopMessage receive_message(java.io.InputStream input, String url)
    {
        GiopMessage message = _giopMessage;
        if (message == null)
        {
            message = _giopMessage = new GiopMessage();
        }
        if (input != null)
        {
            read(input, _buffer, 0, 12);
        }
        int m1 = read_octet();
        int m2 = read_octet();
        int m3 = read_octet();
        int m4 = read_octet();
        if (m1 != 'G' || m2 != 'I' || m3 != 'O' || m4 != 'P')
        {
            throw new BadMagicException(m1 + "," + m2 + "," + m3 + "," + m4);
        }
        int v1 = read_octet();
        int v2 = read_octet();
        if (v1 != 1 || (v2 < 0 || v2 > 2)) // support GIOP 1.0, 1.1, 1.2
        {
            throw new UnsupportedProtocolVersionException(v1 + "." + v2);
        }
        int giopVersion;
        message.giopVersion = giopVersion = v2;
        int flags = read_octet();
        _little = (flags & 1) != 0;
        boolean fragmented = (flags & 2) != 0;
        int messageType = message.type = read_octet();
        int messageSize = message.size = read_ulong();
        if (fragmented && messageSize % 8 != 0)
        {
            throw new org.omg.CORBA.MARSHAL("GIOP Fragment: bad message size (not divisible by 8) = " + messageSize);
        }
        _length = 12 + messageSize;
        if (messageSize > 0 && input != null)
        {
            if (_buffer.length < _length)
            {
                byte[] newBuffer = new byte[_length];
                System.arraycopy(_buffer, 0, newBuffer, 0, 12);
                pool(_buffer);
                _buffer = newBuffer;
            }
            read(input, _buffer, 12, _length);
        }
        if (RMI_TRACE && url != null)
        {
            byte[] data = new byte[_length];
            System.arraycopy(_buffer, 0, data, 0, _length);
            RmiTrace.receive(url, data);
        }
        switch (messageType)
        {
          case MsgType_1_1._Request:
            switch (giopVersion)
            {
              case GiopVersion.VERSION_1_0:
                {
                    RequestHeader_1_0 req10 = RequestHeader_1_0Helper.read(this);
                    RequestHeader_1_2 req12 = new RequestHeader_1_2();
                    req12.service_context = req10.service_context;
                    req12.request_id = req10.request_id;
                    req12.response_flags = (byte)(req10.response_expected ? 3 : 0);
                    req12.operation = req10.operation;
                    (req12.target = new TargetAddress()).object_key(req10.object_key);
                    message.request = req12;
                }
                break;
              case GiopVersion.VERSION_1_1:
                {
                    RequestHeader_1_1 req11 = RequestHeader_1_1Helper.read(this);
                    RequestHeader_1_2 req12 = new RequestHeader_1_2();
                    req12.service_context = req11.service_context;
                    req12.request_id = req11.request_id;
                    req12.response_flags = (byte)(req11.response_expected ? 3 : 0);
                    req12.operation = req11.operation;
                    (req12.target = new TargetAddress()).object_key(req11.object_key);
                    message.request = req12;
                }
                break;
              case GiopVersion.VERSION_1_2:
                message.request = RequestHeader_1_2Helper.read(this);
                if (_length > _offset)
                {
                    read_align(8, 0); // parameters are 8-byte aligned (if present)
                }
                break;
            }
            break;
          case MsgType_1_1._Reply:
            message.reply = ReplyHeader_1_2Helper.read(this);
            if (giopVersion >= GiopVersion.VERSION_1_2)
            {
                if (_length > _offset)
                {
                    read_align(8, 0); // results are 8-byte aligned (if present)
                }
            }
            break;
          case MsgType_1_1._LocateRequest:
            switch (giopVersion)
            {
              case GiopVersion.VERSION_1_0:
              case GiopVersion.VERSION_1_1:
                {
                    LocateRequestHeader_1_0 req10 = LocateRequestHeader_1_0Helper.read(this);
                    LocateRequestHeader_1_2 req12 = new LocateRequestHeader_1_2();
                    req12.request_id = req10.request_id;
                    (req12.target = new TargetAddress()).object_key(req10.object_key);
                    message.locateRequest = req12;
                }
                break;
              default:
                message.locateRequest = LocateRequestHeader_1_2Helper.read(this);
            }
            break;
          case MsgType_1_1._LocateReply:
            // We never send LocateRequest, so this is unexpected.
            throw new org.omg.CORBA.MARSHAL("GIOP LocateReply: unexpected");
          // TODO: CloseConnection messages etc...
          default:
            throw new org.omg.CORBA.NO_IMPLEMENT("TODO: message type = " + messageType);
        }
        return message;
    }

    // -----------------------------------------------------------------------
    // public methods from org.omg.CORBA.portable.InputStream
    // -----------------------------------------------------------------------

    public boolean read_boolean()
    {
        read_align(1, 1);
        int b = _buffer[_offset++];
        if (b == 0)
        {
            return false;
        }
        else if (b == 1)
        {
            return true;
        }
        else
        {
            throw new org.omg.CORBA.MARSHAL("read_boolean: value = " + b);
        }
    }

    public char read_char()
    {
        read_align(1, 1);
        return (char)_buffer[_offset++];
    }

    public char read_wchar()
    {
        read_align(1, 3);
        int size = (int)read_wchar_size();
        int value = (char)read_ushort_no_align_big_endian();
        boolean littleEndian = ((value & 0xffff) == 0xFFFE);
        boolean bigEndian = ((value & 0xffff) == 0xFEFF);
        boolean bomPresent = (littleEndian || bigEndian);
        if ((bomPresent && size != 4) || (! bomPresent && size != 2))
        {
            throw new org.omg.CORBA.MARSHAL("wchar size = " + size
                + (bomPresent ? " (BOM present)" : " (BOM absent)"));
        }
        if (littleEndian)
        {
            read_align(1, 2);
            return (char)read_ushort_no_align_little_endian();
        }
        else if (bigEndian)
        {
            read_align(1, 2);
            return (char)read_ushort_no_align_big_endian();
        }
        else
        {
            // no BOM, big endian
            return (char)value;
        }
    }

    public byte read_octet()
    {
        read_align(1, 1);
        return _buffer[_offset++];
    }

    public short read_short()
    {
        read_align(2, 2);
        int oldOffset = _offset;
        _offset += 2;
        if (_little)
        {
            return LittleEndian.getShort(_buffer, oldOffset);
        }
        else
        {
            return BigEndian.getShort(_buffer, oldOffset);
        }
    }

    public short read_ushort()
    {
        return read_short();
    }

    public int read_long()
    {
        read_align(4, 4);
        int oldOffset = _offset;
        _offset += 4;
        if (_little)
        {
            return LittleEndian.getInt(_buffer, oldOffset);
        }
        else
        {
            return BigEndian.getInt(_buffer, oldOffset);
        }
    }

    public int read_ulong()
    {
        return read_long();
    }

    public long read_longlong()
    {
        read_align(8, 8);
        int oldOffset = _offset;
        _offset += 8;
        if (_little)
        {
            return LittleEndian.getLong(_buffer, oldOffset);
        }
        else
        {
            return BigEndian.getLong(_buffer, oldOffset);
        }
    }

    public long read_ulonglong()
    {
        return read_longlong();
    }

    public float read_float()
    {
        return Float.intBitsToFloat(read_ulong());
    }

    public double read_double()
    {
        return Double.longBitsToDouble(read_ulonglong());
    }

    public java.lang.String read_string()
    {
        int size = read_ulong();
        if (size < 1) // Zero or negative due to unsigned value > 2Gb
        {
            throw new org.omg.CORBA.MARSHAL("read_string: size = " + size);
        }
        read_align(1, size);
        size--;
        if (_buffer[_offset + size] != 0)
        {
            throw new org.omg.CORBA.MARSHAL("read_string: missing NUL");
        }
        // Assume transmission code set is UTF-8
        String value = size == 0 ? "" : UTF8.toString(_buffer, _offset, size);
        _offset += size + 1; // 1 for NUL
        return value;
    }

    public java.lang.String read_wstring()
    {
        int size = read_long();
        if (size == 0)
        {
            return "";
        }
        read_align(2, size);
        int numChars = size / 2;
        boolean littleEndian = false;
        read_align(1, 2);
        int firstChar = (char)read_ushort_no_align_big_endian();
        _offset+=2;
        char[] result;
        int index = 0;
        if (firstChar == 0xFEFF)
        {
            // big endian
            result = new char[--numChars];
        }
        else if (firstChar == 0xFFFE)
        {
            // little endian
            result = new char[--numChars];
            littleEndian = true;
        }
        else
        {
            // no BOM, big endian
            result = new char[numChars--];
            result[index++] = (char)firstChar;
        }
        read_align(1, 2 * numChars);
        if (littleEndian)
        {
            for (int i = 0; i < numChars; i++)
            {
                result[index++] = (char)read_ushort_no_align_little_endian();
                _offset+=2;
            }
        }
        else
        {
            for (int i = 0; i < numChars; i++)
            {
                result[index++] = (char)read_ushort_no_align_big_endian();
                _offset+=2;
            }
        }
        return new String(result);
    }

    public void read_boolean_array(boolean[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_boolean();
        }
    }

    public void read_char_array(char[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[i] = read_char();
        }
    }

    public void read_wchar_array(char[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_wchar();
        }
    }

    public void read_octet_array(byte[] value, int offset, int length)
    {
        read_align(1, length);
        System.arraycopy(_buffer, _offset, value, offset, length);
        _offset += length;
    }

    public void read_short_array(short[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_short();
        }
    }

    public void read_ushort_array(short[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_ushort();
        }
    }

    public void read_long_array(int[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_long();
        }
    }

    public void read_ulong_array(int[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_ulong();
        }
    }

    public void read_longlong_array(long[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_longlong();
        }
    }

    public void read_ulonglong_array(long[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_ulonglong();
        }
    }

    public void read_float_array(float[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_float();
        }
    }

    public void read_double_array(double[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            value[offset + i] = read_double();
        }
    }

    public org.omg.CORBA.Object read_Object()
    {
        IOR ior = IORHelper.read(this);
        if (ior.profiles.length == 0)
        {
            return null;
        }
        ObjectRef stub = null;
        if (ior.type_id.length() != 0)
        {
            String interfaceName = "";
            if (ior.type_id.startsWith("RMI:") && ior.type_id.endsWith(":0000000000000000"))
            {
                interfaceName = ior.type_id.substring(4, ior.type_id.length() - 17);
            }
            else if (ior.type_id.startsWith("IDL:") && ior.type_id.endsWith(":1.0"))
            {
                interfaceName = ior.type_id.substring(4, ior.type_id.length() - 4).replace('/', '.');
            }

            // Return instance of appropriate stub class
            if(interfaceName.startsWith("omg.org", 0))
            {
                interfaceName = "org.apache.geronimo.interop" + interfaceName.substring(7);
            }
            //if(interfaceName.equals("SessionManager.Factory"))
            //{
            //    ObjectRef homeRef =
            //        StubFactory.getInstance().getStub(org.apache.geronimo.interop.rmi.iiop.J40Home.class);
            //    homeRef.$setIOR(ior);
            //    homeRef.$setNamingContext(_namingContext);
            //    org.apache.geronimo.interop.rmi.iiop.J40Home home = (org.apache.geronimo.interop.rmi.iiop.J40Home)homeRef;
            //    org.apache.geronimo.interop.rmi.iiop.J40MetaData md = home.getJ40MetaData();
            //    interfaceName = md.ejbHomeInterfaceClass;
            //}
            Class remoteInterface = ThreadContext.loadClass(interfaceName);
            stub = StubFactory.getInstance().getStub(remoteInterface);
        }
        if (stub == null)
        {
            stub = ObjectRef._getInstance();
        }
        stub.$setIOR(ior);
        stub.$setNamingContext(_namingContext);
        return stub;
    }

    public org.omg.CORBA.TypeCode read_TypeCode()
    {
        return read_TypeCode(new java.util.HashMap());
    }

    private org.omg.CORBA.TypeCode read_TypeCode(java.util.HashMap table)
    {
        int beforeKindOffset = _offset;
        int tk = read_ulong();
        int afterKindOffset = _offset;
        org.apache.geronimo.interop.rmi.iiop.TypeCode tc;
        if (tk == 0xffffffff)
        {
            // an indirection to another TypeCode we have seen.
            int offset = read_long();
            Integer key = new Integer(afterKindOffset + offset);
            org.omg.CORBA.TypeCode ref = (org.omg.CORBA.TypeCode)table.get(key);
            if (ref == null)
            {
                throw new org.omg.CORBA.MARSHAL("read_TypeCode: bad indirection: offset = " + offset);
            }
            tc = new org.apache.geronimo.interop.rmi.iiop.TypeCode(TCKind.tk_null);
            tc.indirection(ref);
            return tc;
        }
        tc = new org.apache.geronimo.interop.rmi.iiop.TypeCode(TCKind.from_int(tk));
        table.put(new Integer(beforeKindOffset), tc);
        switch (tk)
        {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_TypeCode:
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
            case TCKind._tk_longdouble:
                // All these require only a TCKind.
                break;
            case TCKind._tk_fixed:
                tc.fixed_digits(read_ushort());
                tc.fixed_scale(read_short());
                break;
            case TCKind._tk_string:
            case TCKind._tk_wstring:
                tc.length(read_ulong());
                break;
            case TCKind._tk_objref:
            case TCKind._tk_native:
            case TCKind._tk_abstract_interface:
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    end(saveLittle);
                }
                break;
            case TCKind._tk_alias:
                // Change Here
            case TCKind._tk_value_box:
                // End Change Here
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    tc.content_type(read_TypeCode(table));
                    end(saveLittle);
                }
                break;
            case TCKind._tk_sequence:
            case TCKind._tk_array:
                {
                    boolean saveLittle = begin();
                    tc.content_type(read_TypeCode(table));
                    tc.length(read_ulong());
                    end(saveLittle);
                }
                break;
            case TCKind._tk_enum:
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    int count = read_ulong();
                    tc.member_count(count);
                    for (int i = 0; i < count; i++)
                    {
                        tc.member_name(i, read_string());
                    }
                    end(saveLittle);
                }
                break;
            case TCKind._tk_struct:
            case TCKind._tk_except:
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    int count = read_ulong();
                    tc.member_count(count);
                    for (int i = 0; i < count; i++)
                    {
                        tc.member_name(i, read_string());
                        tc.member_type(i, read_TypeCode(table));
                    }
                    end(saveLittle);
                }
                break;
            case TCKind._tk_union:
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    org.omg.CORBA.TypeCode dt = read_TypeCode(table);
                    tc.discriminator_type(dt);
                    int di = read_ulong();
                    int count = read_ulong();
                    tc.member_count(count);
                    for (int i = 0; i < count; i++)
                    {
                        org.omg.CORBA.Any label = new org.apache.geronimo.interop.rmi.iiop.Any();
                        read_Any(label.create_output_stream(), dt);
                        label.read_value(null, dt);
                        tc.member_label(i, label);
                        tc.member_name(i, read_string());
                        tc.member_type(i, read_TypeCode(table));
                    }
                    tc.default_index(di); // must call after setting members
                    end(saveLittle);
                }
                break;
            case TCKind._tk_value:
                {
                    boolean saveLittle = begin();
                    tc.id(read_string());
                    tc.name(read_string());
                    tc.type_modifier(read_short());
                    tc.concrete_base_type(read_TypeCode(table));
                    int count = read_ulong();
                    tc.member_count(count);
                    for (int i = 0; i < count; i++)
                    {
                        tc.member_name(i, read_string());
                        tc.member_type(i, read_TypeCode(table));
                        tc.member_visibility(i, read_short());
                    }
                    end(saveLittle);
                }
                break;
            default:
                throw new org.omg.CORBA.MARSHAL("read_TypeCode: kind = " + tk);
        }
        return tc;
    }

    public org.omg.CORBA.Any read_Any()
    {
        org.omg.CORBA.TypeCode tc = read_TypeCode();
        org.omg.CORBA.Any value = new org.apache.geronimo.interop.rmi.iiop.Any();
        org.omg.CORBA.portable.OutputStream os = value.create_output_stream();
        read_Any(os, tc);
        value.read_value(os.create_input_stream(), tc);
        return value;
    }

    // Sybase-internal
    public void read_Any(org.omg.CORBA.portable.OutputStream os, org.omg.CORBA.TypeCode tc)
    {
        try
        {
            int tk = tc.kind().value();
            switch (tk)
            {
                case TCKind._tk_null:
                case TCKind._tk_void:
                    break;
                case TCKind._tk_TypeCode:
                    os.write_TypeCode(read_TypeCode());
                    break;
                case TCKind._tk_any:
                    os.write_any(read_Any());
                    break;
                case TCKind._tk_boolean:
                    os.write_boolean(read_boolean());
                    break;
                case TCKind._tk_char:
                    os.write_char(read_char());
                    break;
                case TCKind._tk_wchar:
                    os.write_wchar(read_wchar());
                    break;
                case TCKind._tk_octet:
                    os.write_octet(read_octet());
                    break;
                case TCKind._tk_short:
                    os.write_short(read_short());
                    break;
                case TCKind._tk_ushort:
                    os.write_ushort(read_ushort());
                    break;
                case TCKind._tk_long:
                    os.write_long(read_long());
                    break;
                case TCKind._tk_ulong:
                case TCKind._tk_enum:
                    os.write_ulong(read_ulong());
                    break;
                case TCKind._tk_longlong:
                    os.write_longlong(read_longlong());
                    break;
                case TCKind._tk_ulonglong:
                    os.write_ulonglong(read_ulonglong());
                    break;
                case TCKind._tk_float:
                    os.write_float(read_float());
                    break;
                case TCKind._tk_double:
                    os.write_double(read_double());
                    break;
                case TCKind._tk_string:
                    os.write_string(read_string());
                    break;
                case TCKind._tk_wstring:
                    os.write_wstring(read_wstring());
                    break;
                case TCKind._tk_objref:
                    os.write_Object(read_Object());
                    break;
                case TCKind._tk_alias:
                    read_Any(os, tc.content_type());
                    break;
                case TCKind._tk_array:
                    {
                        int n = tc.length();
                        org.omg.CORBA.TypeCode c = tc.content_type();
                        for (int i = 0; i < n; i++)
                        {
                            read_Any(os, c);
                        }
                    }
                    break;
                case TCKind._tk_sequence:
                    {
                        int n = read_ulong();
                        os.write_ulong(n);
                        org.omg.CORBA.TypeCode c = tc.content_type();
                        for (int i = 0; i < n; i++)
                        {
                            read_Any(os, c);
                        }
                    }
                    break;
                case TCKind._tk_struct:
                case TCKind._tk_except:
                    {
                        int n = tc.member_count();
                        for (int i = 0; i < n; i++)
                        {
                            read_Any(os, tc.member_type(i));
                        }
                    }
                    break;
                case TCKind._tk_union:
                    {
                        org.omg.CORBA.TypeCode dt = tc.discriminator_type();
                        org.omg.CORBA.Any disc = new org.apache.geronimo.interop.rmi.iiop.Any();
                        read_Any(disc.create_output_stream(), dt);
                        disc.read_value(null, dt);
                        write_disc(disc, os, dt);
                        int di = tc.default_index();
                        int i, n = tc.member_count();
                        for (i = 0; i < n; i++)
                        {
                            org.omg.CORBA.Any label = tc.member_label(i);
                            if (label.equal(disc))
                            {
                                read_Any(os, tc.member_type(i));
                                break;
                            }
                        }
                        if (i == n && di >= 0)
                        {
                            read_Any(os, tc.member_type(di));
                        }
                    }
                    break;
                case TCKind._tk_fixed: // TODO
                case TCKind._tk_value: // TODO
                default:
                    throw new org.omg.CORBA.MARSHAL("read_Any: type = " + tc);
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
        {
            throw new org.omg.CORBA.MARSHAL("read_Any: " + ex.toString());
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds ex)
        {
            throw new org.omg.CORBA.MARSHAL("read_Any: " + ex.toString());
        }
    }

    public org.omg.CORBA.Any read_any() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_any: NOT YET IMPLMENTED");
    }

    public org.omg.CORBA.Principal read_Principal() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_Principal: NOT YET IMPLMENTED");
    }

    public int read() throws java.io.IOException {
        throw new org.omg.CORBA.NO_IMPLEMENT("read: NOT YET IMPLMENTED");
    }

    public java.math.BigDecimal read_fixed() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_fixed: NOT YET IMPLMENTED");
    }

    public org.omg.CORBA.Context read_Context() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_Context: NOT YET IMPLMENTED");
    }

    public org.omg.CORBA.Object read_Object(Class _class) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_Object: NOT YET IMPLMENTED");
    }

    public org.omg.CORBA.ORB orb() {
        throw new org.omg.CORBA.NO_IMPLEMENT("orb: NOT YET IMPLMENTED");
    }


    // -----------------------------------------------------------------------
    // public methods from org.omg.CORBA_2_3.portable.InputStream
    // -----------------------------------------------------------------------

    public java.io.Serializable read_value() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_value: NOT YET IMPLMENTED");
    }

    public java.io.Serializable read_value(Class _class) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_value: NOT YET IMPLMENTED");
    }

    public java.io.Serializable read_value(org.omg.CORBA.portable.BoxedValueHelper helper) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_value: NOT YET IMPLMENTED");
    }

    public java.io.Serializable read_value(java.lang.String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_value: NOT YET IMPLMENTED");
    }

    public java.io.Serializable read_value(java.io.Serializable todo) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_value: NOT YET IMPLMENTED");
    }

    public java.lang.Object read_abstract_interface() {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_abstract_interface: NOT YET IMPLMENTED");
    }

    public java.lang.Object read_abstract_interface(Class _class) {
        throw new org.omg.CORBA.NO_IMPLEMENT("read_abstract_interface: NOT YET IMPLMENTED");
    }


    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void pool(byte[] oldBuffer)
    {
        if (oldBuffer.length <= MAXIMUM_POOLED_BUFFER_LENGTH)
        {
            _pooledBuffer = oldBuffer;
        }
    }

    protected int read_ushort_no_align_big_endian()
    {
        return UnsignedShort.intValue(BigEndian.getShort(_buffer, _offset));
    }

    protected int read_ushort_no_align_little_endian()
    {
        return UnsignedShort.intValue(LittleEndian.getShort(_buffer, _offset));
    }

    protected int read_wchar_size()
    {
        read_align(1, 1);
        int size =  _buffer[_offset++];
        if (size < 2)
        {
            throw new org.omg.CORBA.MARSHAL("wchar size = " + size);
        }
        return size;
    }

    protected void read(java.io.InputStream input, byte[] buffer, int offset, int length)
    {
        try
        {
            while (offset < length)
            {
                int needLength = length - offset;
                int readLength = input.read(buffer, offset, needLength);
                if (readLength == -1)
                {
                    throw new org.omg.CORBA.MARSHAL("read: EOF");
                }
                offset += readLength;
            }
        }
        catch (java.io.IOException ex)
        {
            throw new org.omg.CORBA.COMM_FAILURE(ex.toString());
        }
    }

    public boolean begin()
    {
        int length = read_ulong(); // encapsulation length
        boolean saveLittle = _little;
        _little = read_boolean();
        return saveLittle;
    }

    public void end(boolean saveLittle)
    {
        _little = saveLittle;
    }

    private void write_disc(org.omg.CORBA.Any disc, org.omg.CORBA.portable.OutputStream os, org.omg.CORBA.TypeCode dt)
    {
        int tk = dt.kind().value();
        if (tk == TCKind._tk_alias)
        {
            try
            {
                write_disc(disc, os, dt.content_type());
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
            {
                throw new org.omg.CORBA.MARSHAL("write_disc: " + ex.toString());
            }
        }
        switch (tk)
        {
            case TCKind._tk_boolean:
                os.write_boolean(disc.extract_boolean());
                break;
            case TCKind._tk_octet:
                os.write_octet(disc.extract_octet());
                break;
            case TCKind._tk_short:
                os.write_short(disc.extract_short());
                break;
            case TCKind._tk_ushort:
                os.write_ushort(disc.extract_ushort());
                break;
            case TCKind._tk_long:
                os.write_long(disc.extract_long());
                break;
            case TCKind._tk_ulong:
            case TCKind._tk_enum:
                os.write_ulong(disc.extract_ulong());
                break;
            case TCKind._tk_longlong:
                os.write_longlong(disc.extract_longlong());
                break;
            case TCKind._tk_ulonglong:
                os.write_ulonglong(disc.extract_ulonglong());
                break;
            default:
                throw new org.omg.CORBA.MARSHAL("write_disc: type = " + dt);
        }
    }

}
