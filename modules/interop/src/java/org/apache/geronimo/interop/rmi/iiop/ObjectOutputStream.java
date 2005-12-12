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

import org.apache.geronimo.interop.*;
import org.apache.geronimo.interop.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class ObjectOutputStream extends java.io.ObjectOutputStream
{

    public static ObjectOutputStream getInstance()
    {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            oos = null;
        }
        return oos;
    }

    public static ObjectOutputStream getInstance(CdrOutputStream cdrOutput)
    {
        ObjectOutputStream output = getInstance();
        output.init(cdrOutput);
        return output;
    }

    public static ObjectOutputStream getPooledInstance()
    {
        ObjectOutputStream output = null;
        if (output == null)
        {
            output = getInstance();
        }
        return output;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    protected static class StreamState
    {
        ValueType type;
        Object value;
        int offset;
        org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.PutField putField;

        StreamState(ValueType type, Object value, int offset)
        {
            this.type = type;
            this.value = value;
            this.offset = offset;
        }
    }

    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public CdrOutputStream _cdrOutput;

    public boolean _hasException;

    public Object[] thisAsObjectArray;

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static ValueType OBJECT_VALUE_TYPE = ValueType.getInstance(java.lang.Object.class);

    private static boolean OBJECT_VALUE_TYPE_INIT = false;

    private ArrayList _stack = null;

    private SimpleIdentityHashMap _indirection;

    private int _blockSizeIndex = -1;

    private int _endLevel;

    private int _endTagIndex;

    private boolean _inBlock = false;

    private boolean _isChunked = false;

    private int _booleanIndex = -1;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public ObjectOutputStream() throws IOException
    {
        super();
    }

    public void $reset()
    {
        _cdrOutput.reset();
        if (_indirection != null)
        {
            _indirection.clear();
        }
        if (_stack != null)
        {
            _stack.clear();
        }
        _blockSizeIndex = -1;
        _endLevel = 0;
        _endTagIndex = 0;
        _inBlock = false;
        _isChunked = false;
        _booleanIndex = -1;
    }

    public void recycle()
    {
        $reset();
    }

    // -----------------------------------------------------------------------
    // public methods from java.io.ObjectOutputStream
    // -----------------------------------------------------------------------

    public void writeBoolean(boolean value)
    {
        _cdrOutput.write_boolean(value);
    }

    public void writeChar(char value)
    {
        _cdrOutput.write_wchar(value);
    }

    public void writeByte(byte value)
    {
        _cdrOutput.write_octet(value);
    }

    public void writeShort(short value)
    {
        _cdrOutput.write_short(value);
    }

    public void writeInt(int value)
    {
        _cdrOutput.write_long(value);
    }

    public void writeLong(long value)
    {
        _cdrOutput.write_longlong(value);
    }

    public void writeFloat(float value)
    {
        _cdrOutput.write_float(value);
    }

    public void writeDouble(double value)
    {
        _cdrOutput.write_double(value);
    }

    public void writeObjectOverride(Object value)
    {
        writeObject(OBJECT_VALUE_TYPE, value, true);
    }

    public void defaultWriteObject() throws IOException
    {
        StreamState state = top();
        // TODO: check this
        int saveOffset = _cdrOutput._offset;
        _cdrOutput._offset = _booleanIndex;
        _cdrOutput.write_boolean(true);
        _cdrOutput._offset = saveOffset;
        writeDeclaredFields(state.type, state.value);
    }

    // -----------------------------------------------------------------------
    // public methods used by generated and package-internal code
    // -----------------------------------------------------------------------

    public boolean hasException()
    {
        return _hasException;
    }

    public void writeException(ValueType type, Exception value)
    {
        String className = type._class.getName();
        String exType = StringUtil.removeSuffix(className, "Exception") + "Ex";
        String repositoryID = "IDL:" + exType.replace('.', '/') + ":1.0";
        _cdrOutput.write_string(repositoryID);
        writeObject(type, value);
        _hasException = true;
    }

    public void writeObject(ValueType type, Object value)
    {
        writeObject(type, value, false);
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(CdrOutputStream cdrOutput)
    {
        _cdrOutput = cdrOutput;
        thisAsObjectArray = new Object[] { this };
    }

    protected void putIndirection(Object value, Integer ref)
    {
        if (_indirection == null)
        {
            _indirection = new SimpleIdentityHashMap(8);
        }
        _indirection.put(value, ref);
    }

    protected void writeObject(ValueType declaredType, Object value, boolean calledFromCustomSerialization)
    {
        ValueType actualType = declaredType;
        while (value != null)
        {
            Class vc = value.getClass();
            if (vc != declaredType._class)
            {
                actualType = ValueType.getInstance(vc);
            }
            if (actualType.hasWriteReplace)
            {
                value = actualType.writeReplace(value);
            }
            else
            {
                break;
            }
        }
        boolean saveIsChunked = _isChunked;
        if (_inBlock)
        {
            if (actualType != null)
            {
                if (!declaredType.isAny || calledFromCustomSerialization)
                {
                    endBlock();
                }
            }
        }
        if (value == null)
        {
            if (calledFromCustomSerialization)
            {
                _cdrOutput.write_boolean(actualType.isObjectRef);
                if(actualType.isObjectRef)
                {
                    writeObjectRef(value);
                    endBlock();
                }
                else
                {
                    _cdrOutput.write_long(ValueType.NULL_VALUE_TAG);
                }
                return;
            }
            if (declaredType.isAny)
            {
                _cdrOutput.write_TypeCode(ValueType.TC_ABSTRACT_BASE);
                _cdrOutput.write_boolean(false);
            }
            if (declaredType.isObjectRef)
            {
                writeObjectRef(value);
            }
            else
            {
                if (declaredType.isAbstractInterface)
                {
                    _cdrOutput.write_boolean(false);
                }
                _cdrOutput.write_long(ValueType.NULL_VALUE_TAG);
            }
            return;
        }
        if (declaredType.isAny && ! calledFromCustomSerialization)
        {
            org.omg.CORBA.TypeCode tc = actualType.tc;
            _cdrOutput.write_TypeCode(tc);
            if (!actualType.isAny)
            {
                endBlock();
            }
        }
        else if (declaredType.isAbstractInterface || calledFromCustomSerialization)
        {
            _cdrOutput.write_boolean(actualType.isObjectRef);
            if (actualType.isObjectRef)
            {
                writeObjectRef(value);
                return;
            }
        }
        if (actualType.isObjectRef)
        {
            writeObjectRef(value);
            return;
        }
        Integer ref = _indirection == null ? null : (Integer)_indirection.get(value);
        if (ref != null)
        {
            _cdrOutput.write_long(ValueType.INDIRECTION_TAG);
            _cdrOutput.write_long(ref.intValue() - _cdrOutput._offset);
            return;
        }
        else
        {
            _cdrOutput.write_align(4, 4); // write any necessary padding
            ref = IntegerCache.get(_cdrOutput._offset);
            putIndirection(value, ref);
        }
        if (saveIsChunked || actualType.requiresCustomSerialization)
        {
            _cdrOutput.write_long(ValueType.TRUNCATABLE_SINGLE_TYPE_VALUE_TAG);
            _isChunked = true;
        }
        else
        {
            _cdrOutput.write_long(ValueType.SINGLE_TYPE_VALUE_TAG);
            _isChunked = false;
        }

        writeMetaString(actualType.id);
        startBlock();
        switch (actualType.readWriteCase)
        {
            case ValueType.CASE_ARRAY:
                writeArray(actualType, value);
                break;
            case ValueType.CASE_CLASS:
                writeClassDesc((java.lang.Class)value);
                break;
            case ValueType.CASE_IDL_ENTITY:
                actualType.helper.write(this, value);
                break;
            // case ValueType.IDL_OBJECT: // already handled above
            case ValueType.CASE_STRING:
                _cdrOutput.write_wstring((String)value);
                break;
            default:
                writeObjectState(actualType, value);
        }
        endBlock();
        writeEndTag(declaredType, actualType, calledFromCustomSerialization);
        _isChunked = saveIsChunked;
    }

    protected void writeMetaString(String ms)
    {
        Integer ref = (Integer)_indirection.get(ms);
        if (ref != null)
        {
            _cdrOutput.write_long(ValueType.INDIRECTION_TAG);
            _cdrOutput.write_long(ref.intValue() - _cdrOutput._offset);
        }
        else
        {
            ref = IntegerCache.get(_cdrOutput._offset);
            _cdrOutput.write_string(ms);
            putIndirection(ms, ref);
        }
    }

    protected void writeObjectState(ValueType type, Object value)
    {
        if (type.isExternalizable)
        {
            _cdrOutput.write_octet((byte)1);
            type.writeExternal(value, this);
            return;
        }
        if (type.hasParentState)
        {
            writeObjectState(type.parent, value);
        }
        if (type.hasWriteObject)
        {
            push(new StreamState(type, value, _cdrOutput._offset));
            if (type.skipCustomFlags)
            {
                _booleanIndex = _cdrOutput._offset;
            }
            else
            {
                _cdrOutput.write_octet((byte)1);
                _cdrOutput.write_boolean(false);
                _booleanIndex = _cdrOutput._offset - 1;
            }
            type.writeObject(value, this);
            pop();
        }
        else
        {
            writeDeclaredFields(type, value);
        }
    }

    protected void writeDeclaredFields(ValueType type, Object value)
    {
        int n = type.fields.length;
        for (int f = 0; f < n; f++)
        {
            ValueTypeField field = type.fields[f];
            int primitive = field.primitive;
            if (primitive != 0)
            {
                writePrimitive(primitive, field, value);
            }
            else
            {
                writeObject(field.type, field.get(value), false);
            }
        }
    }

    protected void writeClassDesc(Class theClass)
    {
        writeObject(ValueType.STRING_VALUE_TYPE, null); // codebase URL
        writeObject(ValueType.STRING_VALUE_TYPE, ValueType.getInstance(theClass).id);
    }

    protected void writeArray(ValueType arrayType, Object value)
    {
        int primitive = arrayType.primitiveArray;
        if (primitive != 0)
        {
            arrayType.helper.write(this, value);
        }
        else
        {
            Object[] array = (Object[])value;
            int n = array.length;
            _cdrOutput.write_ulong(n);
            for (int i = 0; i < n; i++)
            {
                writeObject(arrayType.element, array[i], false);
            }
        }
    }

    protected void writePrimitive(int primitive, ValueTypeField field, Object value)
    {
        switch (primitive)
        {
            case PrimitiveType.BOOLEAN:
                _cdrOutput.write_boolean(field.getBoolean(value));
                break;
            case PrimitiveType.BYTE:
                _cdrOutput.write_octet(field.getByte(value));
                break;
            case PrimitiveType.CHAR:
                _cdrOutput.write_wchar(field.getChar(value));
                break;
            case PrimitiveType.DOUBLE:
                _cdrOutput.write_double(field.getDouble(value));
                break;
            case PrimitiveType.FLOAT:
                _cdrOutput.write_float(field.getFloat(value));
                break;
            case PrimitiveType.INT:
                _cdrOutput.write_long(field.getInt(value));
                break;
            case PrimitiveType.LONG:
                _cdrOutput.write_longlong(field.getLong(value));
                break;
            case PrimitiveType.SHORT:
                _cdrOutput.write_short(field.getShort(value));
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void startBlock()
    {
        if (! _isChunked)
        {
            return;
        }
        _endLevel--;
        _cdrOutput.write_long(0);
        _inBlock = true;
        _blockSizeIndex = _cdrOutput._offset - 4;
    }

    public void endBlock()
    {
        if (! _inBlock)
        {
            return;
        }
        _inBlock = false;
        int oldSize = _cdrOutput._offset;
        _cdrOutput._offset = _blockSizeIndex;
        _cdrOutput.write_long(oldSize - _blockSizeIndex - 4);
        _cdrOutput._offset = oldSize;
        _blockSizeIndex = -1;
    }

    protected void writeEndTag()
    {
        if (_isChunked)
        {
            if (_endTagIndex == _cdrOutput._offset - 8)
            {
                _cdrOutput._offset -= 8;
            }
            _cdrOutput.write_long(_endLevel);
            _endTagIndex = _cdrOutput._offset - 4;
            if (_endLevel != -1)
            {
                _cdrOutput.write_long(1);
            }
            else // _endLevel == -1
            {
                _cdrOutput._offset -=4;
                _cdrOutput.write_long(-1);
                _isChunked = false;
            }
            _endLevel++;
        }
    }

    protected void writeEndTag(ValueType declaredType, ValueType actualType, 
                               boolean calledFromCustomSerialization)
    {
        if (_isChunked)
        {
            if (_endTagIndex == _cdrOutput._offset - 8)
            {
                _cdrOutput._offset -= 8;
            }
            _cdrOutput.write_long(_endLevel);
            _endTagIndex = _cdrOutput._offset - 4;
            if (_endLevel != -1)
            {
                if(declaredType.isAny && !actualType.isAny && !calledFromCustomSerialization)
                {
                    startBlock();
                    _endLevel++;
                }
                else
                {
                    _cdrOutput.write_long(1);
                }
            }
            else // _endLevel == -1
            {
                _cdrOutput._offset -=4;
                _cdrOutput.write_long(-1);
                _isChunked = false;
            }
            _endLevel++;
        }
    }

    private void writeObjectRef(java.lang.Object value)
    {
        if(value instanceof org.apache.geronimo.interop.rmi.iiop.ObjectRef || value == null)
        {
            _cdrOutput.write_Object((org.omg.CORBA.Object)value);
        }
        else if (value instanceof RemoteInterface)
        {
            ObjectRef objectRef = ((RemoteInterface)value).getObjectRef();
            value = objectRef;
            _cdrOutput.write_Object((org.omg.CORBA.Object)value);
        }
        else 
        {
            writeForeignObjectRef(value);
        }
    }

    private void writeForeignObjectRef(java.lang.Object value)
    {
        if (value instanceof java.rmi.Remote)
        {
            try
            {
                value = (org.omg.CORBA.Object)javax.rmi.PortableRemoteObject.toStub((java.rmi.Remote)value);
            }
            catch (java.rmi.NoSuchObjectException ex)
            {
                throw new org.omg.CORBA.MARSHAL(ExceptionUtil.causedBy(ex));
            }
        }
                
        if (value instanceof org.omg.CORBA.Object)
        {
            try
            {
                org.omg.CORBA.Object object = (org.omg.CORBA.Object)value;
                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
                orb.create_output_stream().write_Object(object);
                String ior = orb.object_to_string(object);
                org.apache.geronimo.interop.rmi.iiop.ObjectRef objectRef = org.apache.geronimo.interop.rmi.iiop.ObjectRef.$getObjectFromIOR(ior);
                _cdrOutput.write_Object((org.omg.CORBA.Object)objectRef);
            }
            catch (Exception ex)
            {
                throw new org.omg.CORBA.MARSHAL(ExceptionUtil.causedBy(ex));
            }
        }
        else
        {
            throw new org.omg.CORBA.MARSHAL("writeObjectRef: " + value.getClass().getName());
        }
    }

    public java.io.ObjectOutputStream.PutField putFields() throws IOException
    {
        StreamState state = top();

        Class currentClass = state.type.getTheClass();
        if(currentClass == null)
        {
            throw new IOException("putFields: class from ValueType is null");
        }

        java.io.ObjectStreamClass osc = ObjectStreamClass.lookup(currentClass);
        if(osc == null)
        {
            throw new IOException("putFields: ObjectSteamClass is null");
        }
        
        org.apache.geronimo.interop.rmi.iiop.PutField pf = new org.apache.geronimo.interop.rmi.iiop.PutField(osc);
        state.putField = pf;
        return pf;
    }

    public void writeFields() throws IOException
    {
        StreamState state = top();
        if(state.putField == null)
        {
            throw new IOException("writeFields: PutField object is null");
        }

        org.apache.geronimo.interop.rmi.iiop.PutField pf = (org.apache.geronimo.interop.rmi.iiop.PutField)state.putField;
        pf.writeFields(this);
    }

    protected void push(StreamState state)
    {
        if (_stack == null)
        {
            _stack = new ArrayList();
        }
        _stack.add(state);
    }

    protected void pop()
    {
        int n = _stack.size();
        if (n == 0)
        {
            throw new SystemException("pop: state stack empty");
        }
        _stack.remove(n - 1);
    }

    private StreamState top()
    {
        int n = _stack.size();
        if (n == 0)
        {
            throw new SystemException("top: state stack empty");
        }
        return (StreamState)_stack.get(n - 1);
    }
}
