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
import org.apache.geronimo.interop.util.*;
import org.apache.geronimo.interop.IOP.*;
import org.apache.geronimo.interop.GIOP.*;
import org.omg.CORBA.TCKind;

/**
 ** CORBA 2.3 / GIOP 1.2 CDR OutputStream.
 **/
public class CdrOutputStream extends org.omg.CORBA_2_3.portable.OutputStream
{
    //public static final Component component = new Component(CdrOutputStream.class);

    public static CdrOutputStream getInstance()
    {
        CdrOutputStream output = new CdrOutputStream(); //(CdrOutputStream)component.getInstance();
        output.init(new byte[DEFAULT_BUFFER_LENGTH], 0);
        return output;
    }

    public static CdrOutputStream getInstance(byte[] buffer)
    {
        CdrOutputStream output = new CdrOutputStream(); //(CdrOutputStream)component.getInstance();
        output.init(buffer, 0);
        return output;
    }

    public static CdrOutputStream getInstance(byte[] buffer, int offset)
    {
        CdrOutputStream output = new CdrOutputStream(); //(CdrOutputStream)component.getInstance();
        output.init(buffer, offset);
        return output;
    }

    public static CdrOutputStream getInstanceForEncapsulation()
    {
        CdrOutputStream output = getInstance();
        output.write_boolean(false); // byte order: big endian
        return output;
    }

    public static CdrOutputStream getPooledInstance()
    {
        CdrOutputStream output = null; // (CdrOutputStream)_pool.get();
        if (output == null)
        {
            output = getInstance();
        }
        return output;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static final int DEFAULT_BUFFER_LENGTH = 64;

    private static final int MAXIMUM_POOLED_BUFFER_LENGTH = 1024;

    private static final boolean RMI_TRACE = true; //SystemProperties.rmiTrace();

    private static IOR NULL_IOR = new IOR("", new TaggedProfile[0]);

    private static char[] GIOP_MAGIC = { 'G', 'I', 'O', 'P' };

    private static ServiceContext[] EMPTY_SERVICE_CONTEXT_LIST = {};

    private static Version GIOP_VERSION_1_0 = new Version((byte)1, (byte)0);
    private static Version GIOP_VERSION_1_1 = new Version((byte)1, (byte)1);
    private static Version GIOP_VERSION_1_2 = new Version((byte)1, (byte)2);

    //private static ThreadLocalInstancePool _pool = new ThreadLocalInstancePool(CdrOutputStream.class.getName());

    private int _giopVersion = GiopVersion.VERSION_1_2;

    private boolean _unaligned;

    private byte[] _pooledBuffer;

    // -----------------------------------------------------------------------
    // package-private data
    // -----------------------------------------------------------------------

    byte[] _buffer;

    int _offset;

    int _length;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public void init(byte[] buffer, int offset)
    {
        _buffer = buffer;
        _offset = offset;
        _length = _buffer.length;
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
    }

    public void setUnaligned()
    {
        _unaligned = true;
    }

    public byte[] getBytes()
    {
        int n = _offset;
        byte[] bytes = new byte[n];
        System.arraycopy(_buffer, 0, bytes, 0, n);
        return bytes;
    }

    public byte[] getBuffer()
    {
        return _buffer;
    }

    public int getOffset()
    {
        return _offset;
    }

    public int getLength()
    {
        return _length;
    }

    public byte[] getEncapsulation()
    {
        byte[] data = new byte[_offset];
        System.arraycopy(_buffer, 0, data, 0, _offset);
        return data;
    }

    public void setGiopVersion(int version)
    {
        _giopVersion = version;
    }

    /**
     ** Align the buffer offset so the next item is written at an offset
     ** aligned according to <code>alignment</code>, which must be a
     ** power of 2 (and at least = 1).
     ** <p>The padding bytes are set to zero, to prevent the
     ** security problems inherent in uninitialised padding bytes.
     ** <p>Then we check if there is enough space left in the buffer for
     ** an item of <code>size</code> bytes; if not, we expand the buffer
     ** to make space.
     **/
    public final void write_align(int alignment, int size)
    {
        if (_unaligned)
        {
            alignment = 1;
        }
        int needLength = _offset + alignment + size;
        if (needLength > _length)
        {
            // We need to increase the buffer size. We allow for a bit
            // of future expansion (if possible).
            int factor8 = 32;
            for (int pass = 1; pass <= 7; pass++, factor8 /= 2)
            {
                // We try factors 5, 3, 2, 1.5, 1.25, 1.125, 1.
                try
                {
                    byte[] newBuffer = new byte[needLength + factor8 * needLength / 8];
                    // Copy old buffer contents into new buffer.
                    System.arraycopy(_buffer, 0, newBuffer, 0, _offset);
                    pool(_buffer);
                    _buffer = newBuffer;
                    _length = newBuffer.length;
                    break;
                }
                catch (OutOfMemoryError ignore)
                {
                    if (pass == 7)
                    {
                        throw new org.omg.CORBA.NO_MEMORY(needLength + " bytes");
                    }
                }
            }
        }
        int mask = alignment - 1;
        for (int i = (alignment - (_offset & mask)) & mask; i > 0; i--)
        {
            _buffer[_offset++] = 0;
        }
    }

    /**
     ** Convenience method needed in many places.
     **/
    public void write_octet_sequence(byte[] bytes)
    {
        if (bytes == null)
        {
            bytes = ArrayUtil.EMPTY_BYTE_ARRAY;
        }
        int n = bytes.length;
        write_long(n);
        write_octet_array(bytes, 0, n);
    }

    public void write_message(int messageType)
    {
        MessageHeader_1_1 header = new MessageHeader_1_1();
        header.magic = GIOP_MAGIC;
        switch (_giopVersion)
        {
          case GiopVersion.VERSION_1_0:
            header.GIOP_version = GIOP_VERSION_1_2;
            break;
          case GiopVersion.VERSION_1_1:
            header.GIOP_version = GIOP_VERSION_1_1;
            break;
          case GiopVersion.VERSION_1_2:
            header.GIOP_version = GIOP_VERSION_1_2;
            break;
          default:
            throw new IllegalStateException();
        }
        header.flags = 0;
        header.message_type = (byte)messageType;
        header.message_size = 0;
        // header.message_size is rewritten later
        MessageHeader_1_1Helper.write(this, header);
    }

    public void write_message_size()
    {
        int messageSize = _offset - 12;
        int saveOffset = _offset;
        _offset = 8;
        write_long(messageSize);
        _offset = saveOffset;
    }

    public void write_request(RequestHeader_1_2 request, CdrOutputStream parameters)
    {
        if (request.service_context == null)
        {
            // Avoid allocation of empty array by Helper.
            request.service_context = EMPTY_SERVICE_CONTEXT_LIST;
        }
        write_message(MsgType_1_1._Request);
        switch (_giopVersion)
        {
          case GiopVersion.VERSION_1_0:
            RequestHeader_1_0 req10 = new RequestHeader_1_0();
            req10.service_context = request.service_context;
            req10.request_id = request.request_id;
            req10.response_expected = (request.response_flags & 1) != 0;
            req10.operation = request.operation;
            req10.object_key = request.target.object_key();
            RequestHeader_1_0Helper.write(this, req10);
            break;
          case GiopVersion.VERSION_1_1:
            RequestHeader_1_1 req11 = new RequestHeader_1_1();
            req11.service_context = request.service_context;
            req11.request_id = request.request_id;
            req11.response_expected = (request.response_flags & 1) != 0;
            req11.operation = request.operation;
            req11.object_key = request.target.object_key();
            RequestHeader_1_1Helper.write(this, req11);
            break;
          case GiopVersion.VERSION_1_2:
            RequestHeader_1_2Helper.write(this, request);
            break;
          default:
            throw new IllegalStateException();
        }
        byte[] parametersBuffer = parameters.getBuffer();
        int parametersLength = parameters.getOffset();
        if (parametersLength > 0)
        {
            if (_giopVersion >= GiopVersion.VERSION_1_2)
            {
                write_align(8, 0); // parameters are 8-byte aligned
            }
            else
            {
                // TODO: should have padded service context earlier
            }
            write_octet_array(parametersBuffer, 0, parametersLength);
        }
        write_message_size();
    }

    public void write_reply(ReplyHeader_1_2 reply, CdrOutputStream results)
    {
        if (reply.service_context == null)
        {
            // Avoid allocation of empty array by Helper.
            reply.service_context = EMPTY_SERVICE_CONTEXT_LIST;
        }
        write_message(MsgType_1_1._Reply);
        switch (_giopVersion)
        {
          case GiopVersion.VERSION_1_0:
          case GiopVersion.VERSION_1_1:
            ReplyHeader_1_0 rep10 = new ReplyHeader_1_0();
            rep10.service_context = reply.service_context;
            rep10.request_id = reply.request_id;
            rep10.reply_status = reply.reply_status;
            ReplyHeader_1_0Helper.write(this, rep10);
            break;
          case GiopVersion.VERSION_1_2:
            ReplyHeader_1_2Helper.write(this, reply);
            break;
          default:
            throw new IllegalStateException();
        }
        byte[] resultsBuffer = results.getBuffer();
        int resultsLength = results.getOffset();
        if (resultsLength > 0)
        {
            if (_giopVersion >= GiopVersion.VERSION_1_2)
            {
                write_align(8, 0); // results are 8-byte aligned
            }
            else
            {
                // TODO: should have padded service context earlier
            }
            write_octet_array(resultsBuffer, 0, resultsLength);
        }
        write_message_size();
    }

    public void write_reply(LocateReplyHeader_1_2 reply)
    {
        write_message(MsgType_1_1._LocateReply);
        LocateReplyHeader_1_2Helper.write(this, reply);
        write_message_size();
    }

    public void write_SystemException(Exception ex, boolean withStackTrace)
    {
        String type = "UNKNOWN";
        if (ex instanceof org.omg.CORBA.SystemException)
        {
            type = JavaClass.getNameSuffix(ex.getClass().getName());
        }
        else if (ex instanceof UnsupportedOperationException)
        {
            type = "BAD_OPERATION";
        }
        else if (ex instanceof SecurityException)
        {
            type = "NO_PERMISSION";
        }
        else if (ex instanceof java.rmi.NoSuchObjectException)
        {
            type = "OBJECT_NOT_EXIST";
        }
        //else if (ex instanceof org.apache.geronimo.interop.transaction.TransactionRolledbackSystemException)
        //{
        //    type = "TRANSACTION_ROLLEDBACK";
        //}
        String id = "IDL:omg.org/CORBA/" + type + ":1.0";
        write_string(id);
        write_long(0); // minor (TODO: other values?)
        write_long(0); // completed (TODO: other values?)
        if (withStackTrace)
        {
            write_string(ExceptionUtil.getStackTrace(ex));
        }
    }

    public void send_message(java.io.OutputStream output, String url)
    {
        if (RMI_TRACE)
        {
            byte[] data = new byte[_offset];
            System.arraycopy(_buffer, 0, data, 0, _offset);
            RmiTrace.send(url, data);
        }
        try
        {
            output.write(_buffer, 0, _offset);
            output.flush();
        }
        catch (java.io.IOException ex)
        {
            throw new org.omg.CORBA.COMM_FAILURE(ex.toString());
        }
    }

    // -----------------------------------------------------------------------
    // public methods from org.omg.CORBA.portable.OutputStream
    // -----------------------------------------------------------------------

    public void write_boolean(boolean value)
    {
        write_align(1, 1);
        if (value)
        {
            _buffer[_offset++] = 1;
        }
        else
        {
            _buffer[_offset++] = 0;
        }
    }

    public void write_char(char value)
    {
        write_align(1, 1);
        if ((int)value > 255)
        {
            throw new org.omg.CORBA.MARSHAL("write_char: value = " + value);
        }
        _buffer[_offset++] = (byte)value;
    }

    public void write_wchar(char value)
    {
        write_octet((byte)2); // size of wchar is 2 bytes
        write_align(1, 2);
        write_ushort_no_align_big_endian((int)value);
    }

    public void write_octet(byte value)
    {
        write_align(1, 1);
        _buffer[_offset++] = value;
    }

    public void write_short(short value)
    {
        write_align(2, 2);
        int oldOffset = _offset;
        _offset += 2;
        BigEndian.setShort(_buffer, oldOffset, value);
    }

    public void write_ushort(short value)
    {
        write_short(value);
    }

    public void write_long(int value)
    {
        write_align(4, 4);
        int oldOffset = _offset;
        _offset += 4;
        BigEndian.setInt(_buffer, oldOffset, value);
    }

    public void write_ulong(int value)
    {
        write_long(value);
    }

    public void write_longlong(long value)
    {
        write_align(8, 8);
        int oldOffset = _offset;
        _offset += 8;
        BigEndian.setLong(_buffer, oldOffset, value);
    }

    public void write_ulonglong(long value)
    {
        write_longlong(value);
    }

    public void write_float(float value)
    {
        write_long(Float.floatToIntBits(value));
    }

    public void write_double(double value)
    {
        write_longlong(Double.doubleToLongBits(value));
    }

    public void write_string(String value)
    {
        if (value == null)
        {
            value = "";
        }
        write_align(4, 4);
        int size = UTF8.fromString(value, _buffer, _offset + 4, _length - 1);
        if (size == -1)
        {
            // No room to convert in-place, ok to allocate new byte array.
            byte[] bytes = UTF8.fromString(value);
            size = bytes.length;
            write_ulong(size + 1);
            write_octet_array(bytes, 0, size);
        }
        else
        {
            // Already converted already into _buffer.
            write_ulong(size + 1);
            _offset += size;
        }
        write_octet((byte)0);
    }

    public void write_wstring(String value)
    {
        if (value == null)
        {
            value = "";
        }
        int size = value.length();
        int numBytes = 2 * size;
        write_ulong(numBytes); // No terminating NUL
        write_align(1, numBytes);
        for (int i = 0; i < size; i++)
        {
            char c = value.charAt(i);
            BigEndian.setShort(_buffer, _offset, (short)c);
            _offset += 2;
        }
    }

    public void write_boolean_array(boolean[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_boolean(value[offset + i]);
        }
    }

    public void write_char_array(char[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_char(value[offset + i]);
        }
    }

    public void write_wchar_array(char[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_wchar(value[offset + i]);
        }
    }

    public void write_octet_array(byte[] value, int offset, int length)
    {
        write_align(1, length);
        System.arraycopy(value, offset, _buffer, _offset, length);
        _offset += length;
    }

    public void write_short_array(short[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_short(value[offset + i]);
        }
    }

    public void write_ushort_array(short[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_ushort(value[offset + i]);
        }
    }

    public void write_long_array(int[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_long(value[offset + i]);
        }
    }

    public void write_ulong_array(int[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_ulong(value[offset + i]);
        }
    }

    public void write_longlong_array(long[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_longlong(value[offset + i]);
        }
    }

    public void write_ulonglong_array(long[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_ulonglong(value[offset + i]);
        }
    }

    public void write_float_array(float[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_float(value[offset + i]);
        }
    }

    public void write_double_array(double[] value, int offset, int length)
    {
        for (int i = 0; i < length; i++)
        {
            write_double(value[offset + i]);
        }
    }

    public void write_Object(org.omg.CORBA.Object value)
    {
        if (value == null)
        {
            write_IOR(null);
        }
        else if (value instanceof ObjectRef)
        {
            ObjectRef ref = (ObjectRef)value;
            IOR ior = ref.$getIOR();
            write_IOR(ior);
        }
        else
        {
            throw new org.omg.CORBA.MARSHAL(value.getClass().getName());
        }
    }

    public void write_TypeCode(org.omg.CORBA.TypeCode tc)
    {
        write_TypeCode(tc, new java.util.HashMap());
    }

    public void write_Any(org.omg.CORBA.Any value)
    {
        org.omg.CORBA.TypeCode tc = value.type();
        write_TypeCode(tc);
        write_Any(value.create_input_stream(), tc);
    }

    // Sybase-internal
    public void write_Any(org.omg.CORBA.portable.InputStream is, org.omg.CORBA.TypeCode tc)
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
                    write_TypeCode(is.read_TypeCode());
                    break;
                case TCKind._tk_any:
                    write_Any(is.read_any());
                    break;
                case TCKind._tk_boolean:
                    write_boolean(is.read_boolean());
                    break;
                case TCKind._tk_char:
                    write_char(is.read_char());
                    break;
                case TCKind._tk_wchar:
                    write_wchar(is.read_wchar());
                    break;
                case TCKind._tk_octet:
                    write_octet(is.read_octet());
                    break;
                case TCKind._tk_short:
                    write_short(is.read_short());
                    break;
                case TCKind._tk_ushort:
                    write_ushort(is.read_ushort());
                    break;
                case TCKind._tk_long:
                    write_long(is.read_long());
                    break;
                case TCKind._tk_ulong:
                case TCKind._tk_enum:
                    write_ulong(is.read_ulong());
                    break;
                case TCKind._tk_longlong:
                    write_longlong(is.read_longlong());
                    break;
                case TCKind._tk_ulonglong:
                    write_ulonglong(is.read_ulonglong());
                    break;
                case TCKind._tk_float:
                    write_float(is.read_float());
                    break;
                case TCKind._tk_double:
                    write_double(is.read_double());
                    break;
                case TCKind._tk_string:
                    write_string(is.read_string());
                    break;
                case TCKind._tk_wstring:
                    write_wstring(is.read_wstring());
                    break;
                case TCKind._tk_objref:
                    write_Object(is.read_Object());
                    break;
                case TCKind._tk_alias:
                    write_Any(is, tc.content_type());
                    break;
                case TCKind._tk_array:
                    {
                        int n = tc.length();
                        org.omg.CORBA.TypeCode c = tc.content_type();
                        for (int i = 0; i < n; i++)
                        {
                            write_Any(is, c);
                        }
                    }
                    break;
                case TCKind._tk_sequence:
                    {
                        int n = is.read_ulong();
                        write_ulong(n);
                        org.omg.CORBA.TypeCode c = tc.content_type();
                        for (int i = 0; i < n; i++)
                        {
                            write_Any(is, c);
                        }
                    }
                    break;
                case TCKind._tk_struct:
                case TCKind._tk_except:
                    {
                        int n = tc.member_count();
                        for (int i = 0; i < n; i++)
                        {
                            write_Any(is, tc.member_type(i));
                        }
                    }
                    break;
                case TCKind._tk_union:
                    {
                        org.omg.CORBA.TypeCode dt = tc.discriminator_type();
                        org.omg.CORBA.Any disc = read_disc(is, dt);
                        write_Any(disc.create_input_stream(), dt);
                        int di = tc.default_index();
                        int i, n = tc.member_count();
                        for (i = 0; i < n; i++)
                        {
                            org.omg.CORBA.Any label = tc.member_label(i);
                            if (label.equal(disc))
                            {
                                write_Any(is, tc.member_type(i));
                            }
                        }
                        if (i == n && di >= 0)
                        {
                            write_Any(is, tc.member_type(di));
                        }
                    }
                    break;
                case TCKind._tk_fixed: // TODO
                case TCKind._tk_value: // TODO
                default:
                    throw new org.omg.CORBA.MARSHAL("write_Any: type = " + tc);
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
        {
            throw new org.omg.CORBA.MARSHAL("write_Any: " + ex.toString());
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds ex)
        {
            throw new org.omg.CORBA.MARSHAL("write_Any: " + ex.toString());
        }
    }

    public void write_any(org.omg.CORBA.Any value) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_any: NOT IMPLMENTED YET");
    }

    public void write_Principal(org.omg.CORBA.Principal value) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_Principal: NOT IMPLMENTED YET");
    }

    public void write(int value) throws java.io.IOException {
        throw new org.omg.CORBA.NO_IMPLEMENT("write: NOT IMPLMENTED YET");
    }

    public void write_fixed(java.math.BigDecimal value) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_fixed: NOT IMPLMENTED YET");
    }

    public void write_Context(org.omg.CORBA.Context context, org.omg.CORBA.ContextList list) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_Context: NOT IMPLMENTED YET");
    }

    public org.omg.CORBA.ORB orb() {
        throw new org.omg.CORBA.NO_IMPLEMENT("orb: NOT IMPLMENTED YET");
    }


    // -----------------------------------------------------------------------
    // public methods from org.omg.CORBA_2_3.portable.OutputStream
    // -----------------------------------------------------------------------

    public void write_value(java.io.Serializable value) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_value: NOT IMPLMENTED YET");
    }

    public void write_value(java.io.Serializable value, Class _class) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_value: NOT IMPLMENTED YET");
    }

    public void write_value(java.io.Serializable value, String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_value: NOT IMPLMENTED YET");
    }

    public void write_value(java.io.Serializable value, org.omg.CORBA.portable.BoxedValueHelper helper) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_value: NOT IMPLMENTED YET");
    }

    public void write_abstract_interface(java.lang.Object value) {
        throw new org.omg.CORBA.NO_IMPLEMENT("write_abstract_interface: NOT IMPLMENTED YET");
    }


    // doing this specifically to handle Any. This implementation 
    // could be worng but will work for us
    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        CdrInputStream is = CdrInputStream.getInstance();
        is._buffer = new byte[_buffer.length];
        System.arraycopy(_buffer,0,is._buffer,0,_buffer.length);
        is._length = _buffer.length;
        is._offset = 0;
        return is;
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

    protected final void write_ushort_no_align_big_endian(int value)
    {
        int oldOffset = _offset;
        _offset += 2;
        BigEndian.setShort(_buffer, oldOffset, (short)value);
    }

    protected void write_IOR(IOR ior)
    {
        if (ior == null)
        {
            ior = NULL_IOR;
        }
        IORHelper.write(this, ior);
    }

    public int begin()
    {
        write_ulong(0);
        int saveOffset = _offset;
        write_boolean(false);
        return saveOffset;
    }

    public void end(int saveOffset)
    {
        int endOffset = _offset;
        _offset = saveOffset - 4;
        write_ulong(endOffset - saveOffset);
        _offset = endOffset;
    }

    private void write_TypeCode(org.omg.CORBA.TypeCode tc, java.util.HashMap table)
    {
        try
        {
            int tk = tc.kind().value();
            // Check if we need to write an indirection
            switch (tk)
            {
                case TCKind._tk_struct:
                case TCKind._tk_union:
                case TCKind._tk_value:
                    String id = tc.id();
                    if (! id.equals(""))
                    {
                        Integer key = (Integer)table.get(id);
                        if (key != null)
                        {
                            write_ulong(0xffffffff);
                            write_long(key.intValue() - _offset);
                            return;
                        }
                        table.put(id, new Integer(_offset));
                    }
            }
            write_ulong(tk);
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
                    write_ushort(tc.fixed_digits());
                    write_short(tc.fixed_scale());
                    break;
                case TCKind._tk_string:
                case TCKind._tk_wstring:
                    write_ulong(tc.length());
                    break;
                case TCKind._tk_objref:
                case TCKind._tk_native:
                case TCKind._tk_abstract_interface:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_alias:
                case TCKind._tk_value_box:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        write_TypeCode(tc.content_type(), table);
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_sequence:
                case TCKind._tk_array:
                    {
                        int saveOffset = begin();
                        write_TypeCode(tc.content_type(), table);
                        write_ulong(tc.length());
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_enum:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        int count = tc.member_count();
                        write_ulong(count);
                        for (int i = 0; i < count; i++)
                        {
                            write_string(tc.member_name(i));
                        }
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_struct:
                case TCKind._tk_except:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        int count = tc.member_count();
                        write_ulong(count);
                        for (int i = 0; i < count; i++)
                        {
                            write_string(tc.member_name(i));
                            write_TypeCode(tc.member_type(i), table);
                        }
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_union:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        org.omg.CORBA.TypeCode dt = tc.discriminator_type();
                        write_TypeCode(dt, table);
                        int di = tc.default_index();
                        write_ulong(di);
                        int count = tc.member_count();
                        write_ulong(count);
                        for (int i = 0; i < count; i++)
                        {
                            write_Any(tc.member_label(i).create_input_stream(), dt);
                            write_string(tc.member_name(i));
                            write_TypeCode(tc.member_type(i), table);
                        }
                        end(saveOffset);
                    }
                    break;
                case TCKind._tk_value:
                    {
                        int saveOffset = begin();
                        write_string(tc.id());
                        write_string(tc.name());
                        write_short(tc.type_modifier());
                        write_TypeCode(tc.concrete_base_type(), table);
                        int count = tc.member_count();
                        write_ulong(count);
                        for (int i = 0; i < count; i++)
                        {
                            write_string(tc.member_name(i));
                            write_TypeCode(tc.member_type(i), table);
                            write_short(tc.member_visibility(i));
                        }
                        end(saveOffset);
                    }
                    break;
                default:
                    throw new org.omg.CORBA.MARSHAL("write_TypeCode: kind = " + tk);
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
        {
            throw new org.omg.CORBA.MARSHAL(ex.toString());
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds ex)
        {
            throw new org.omg.CORBA.MARSHAL(ex.toString());
        }
    }

    private org.omg.CORBA.Any read_disc(org.omg.CORBA.portable.InputStream is, org.omg.CORBA.TypeCode dt)
    {
        int tk = dt.kind().value();
        if (tk == TCKind._tk_alias)
        {
            try
            {
                return read_disc(is, dt.content_type());
            }
            catch (org.omg.CORBA.TypeCodePackage.BadKind ex)
            {
                throw new org.omg.CORBA.MARSHAL("read_disc: " + ex.toString());
            }
        }
        org.omg.CORBA.Any disc = new org.apache.geronimo.interop.rmi.iiop.Any();
        switch (tk)
        {
            case TCKind._tk_boolean:
                disc.insert_boolean(is.read_boolean());
                break;
            case TCKind._tk_octet:
                disc.insert_octet(is.read_octet());
                break;
            case TCKind._tk_short:
                disc.insert_short(is.read_short());
                break;
            case TCKind._tk_ushort:
                disc.insert_ushort(is.read_ushort());
                break;
            case TCKind._tk_long:
                disc.insert_long(is.read_long());
                break;
            case TCKind._tk_ulong:
            case TCKind._tk_enum:
                disc.insert_ulong(is.read_ulong());
                break;
            case TCKind._tk_longlong:
                disc.insert_longlong(is.read_longlong());
                break;
            case TCKind._tk_ulonglong:
                disc.insert_ulonglong(is.read_ulonglong());
                break;
            default:
                throw new org.omg.CORBA.MARSHAL("read_disc: type = " + dt);
        }
        return disc;
    }
}
