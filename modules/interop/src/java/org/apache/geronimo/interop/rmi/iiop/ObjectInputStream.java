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

import java.io.IOException;
import java.io.NotActiveException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import org.omg.CORBA.TCKind;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.util.ArrayUtil;
import org.apache.geronimo.interop.util.IntegerCache;


public class ObjectInputStream extends java.io.ObjectInputStream {
    public static ObjectInputStream getInstance() {
        return getInstance(CdrInputStream.getInstance());
    }

    public static ObjectInputStream getInstance(org.apache.geronimo.interop.rmi.iiop.CdrInputStream cdrInput) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }

        input.init(cdrInput);
        return input;
    }

    public static ObjectInputStream getPooledInstance() {
        ObjectInputStream input = null; //(ObjectInputStream)_pool.get();
        if (input == null) {
            input = getInstance();
        }
        return input;
    }

    // -----------------------------------------------------------------------
    // inner classes
    // -----------------------------------------------------------------------

    protected static class StreamState {
        ValueType type;
        Object value;
        int offset;

        StreamState(ValueType type, Object value, int offset) {
            this.type = type;
            this.value = value;
            this.offset = offset;
        }
    }

    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public static final int MAXIMUM_BLOCK_LENGTH = 0x7fffff00;

    public CdrInputStream _cdrInput;

    public Object[] thisAsObjectArray;

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    //private static ThreadLocalInstancePool _pool = new ThreadLocalInstancePool(ObjectInputStream.class.getName());

    private int _blockLength = MAXIMUM_BLOCK_LENGTH;

    private int _endLevel = 0;

    private HashMap _indirection;

    private boolean _isChunked = false;

    private ArrayList _stack;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public ObjectInputStream() throws IOException {
    }

    public void $reset() {
        _cdrInput.reset();
        if (_indirection != null) {
            _indirection.clear();
        }
        if (_stack != null) {
            _stack.clear();
        }
        _blockLength = MAXIMUM_BLOCK_LENGTH;
        _endLevel = 0;
        _isChunked = false;
    }

    public void recycle() {
        $reset();
        //_pool.put(this);
    }

    // public methods from java.io.ObjectInputStream

    public boolean readBoolean() {
        return _cdrInput.read_boolean();
    }

    public char readChar() {
        return _cdrInput.read_wchar();
    }

    public byte readByte() {
        return _cdrInput.read_octet();
    }

    public short readShort() {
        return _cdrInput.read_short();
    }

    public int readInt() {
        return _cdrInput.read_long();
    }

    public long readLong() {
        return _cdrInput.read_longlong();
    }

    public float readFloat() {
        return _cdrInput.read_float();
    }

    public double readDouble() {
        return _cdrInput.read_double();
    }

    public Object readObjectOverride() {
        return readObject(ValueType.OBJECT_VALUE_TYPE, false);
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException, NotActiveException {
        StreamState state = top();
        readDeclaredFields(state.type, state.value);
    }

    // -----------------------------------------------------------------------
    // public methods used by generated and package-internal code
    // -----------------------------------------------------------------------

    public Exception readException(ValueType type) {
        return (Exception) readObject(type, false);
    }

    public Object readObject(ValueType type) {
        return readObject(type, false);
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(org.apache.geronimo.interop.rmi.iiop.CdrInputStream cdrInput) {
        _cdrInput = cdrInput;
        thisAsObjectArray = new Object[]{this};
    }

    protected void putIndirection(Integer key, Object value) {
        if (_indirection == null) {
            _indirection = new HashMap();
        }
        _indirection.put(key, value);
    }

    protected Object readObject(ValueType declaredType, boolean calledByCustomSerialization) {
        org.omg.CORBA.TypeCode tc = null;

        int tag = _cdrInput.read_ulong();
        int saveOffset = _cdrInput._offset - 4;
        Object value;

        if (tag == ValueType.INDIRECTION_TAG) {
            // Indirection to value already read (or cyclic value being read).
            saveOffset = _cdrInput._offset;
            int offset = _cdrInput.read_long();
            Integer key = IntegerCache.get(saveOffset + offset);
            if (_indirection != null) {
                value = _indirection.get(key);
                if (value != null) {
                    return value;
                }
            }
            throw new org.omg.CORBA.MARSHAL("invalid indirection offset = " + offset);
        } else {
            _cdrInput._offset = saveOffset;
        }

        if (calledByCustomSerialization) {
            boolean isObjectRef = _cdrInput.read_boolean();
            if (isObjectRef) {
                org.omg.CORBA.Object ref = _cdrInput.read_Object();
                endBlock();
                if (_blockLength == MAXIMUM_BLOCK_LENGTH) {
                    startBlock();
                }
                return ref;
            } else {
                _cdrInput._offset = saveOffset;
            }
        } else if (declaredType.isAnyOrObjectRefOrAbstractInterface) {
            boolean isObjectRef = false;
            if (declaredType.isObjectRef) {
                return _cdrInput.read_Object();
            } else if (declaredType.isAny) {
                tc = _cdrInput.read_TypeCode();
                int kind = tc.kind().value();
                if (kind == TCKind._tk_null) {
                    return null;
                }
                if (kind == TCKind._tk_objref) {
                    isObjectRef = true;
                } else if (kind == TCKind._tk_abstract_interface) {
                    isObjectRef = _cdrInput.read_boolean();
                }
                if (isObjectRef) {
                    saveOffset = _cdrInput._offset;
                    int checkValue = _cdrInput.read_ulong();
                    if (checkValue == 0) {
                        return null;
                    }

                    _cdrInput._offset = saveOffset;
                    return _cdrInput.read_Object();
                }
            } else if (declaredType.isAbstractInterface) {
                isObjectRef = _cdrInput.read_boolean();
                if (isObjectRef) {
                    return _cdrInput.read_Object();
                }
            } else {
                throw new IllegalStateException(declaredType.toString());
            }
        }

        saveOffset = _cdrInput._offset;
        tag = _cdrInput.read_long();
        if (tag == ValueType.NULL_VALUE_TAG) {
            return null;
        }
        if (tag == ValueType.INDIRECTION_TAG) {
            // Indirection to value already read (or cyclic value being read).
            saveOffset = _cdrInput._offset;
            int offset = _cdrInput.read_long();
            Integer key = IntegerCache.get(saveOffset + offset);
            if (_indirection != null) {
                value = _indirection.get(key);
                if (value != null) {
                    return value;
                }
            }
            throw new org.omg.CORBA.MARSHAL("invalid indirection offset = " + offset);
        }
        ValueType actualType;
        boolean saveIsChunked = _isChunked;
        _isChunked = (tag & 0x00000008) != 0;
        String codebaseURL = null;
        if ((tag & 0x00000001) == 1) {
            codebaseURL = readMetaString();
        }
        switch (tag & 0x00000006) {
            case 0: // NO_TYPE_VALUE_TAG
                {
                    actualType = declaredType;
                    if (tc != null) {
                        try {
                            String id = tc.id();
                            if (id != null) {
                                int kind = tc.kind().value();
                                if (kind == TCKind._tk_value_box) {
                                    kind = tc.content_type().kind().value();
                                    if (kind == TCKind._tk_wstring) {
                                        actualType = ValueType.STRING_VALUE_TYPE;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            throw new SystemException(ex);
                        }
                    }
                }
                break;
            case 2: // SINGLE_TYPE_VALUE_TAG
                {
                    String repositoryID = readMetaString();
// TODO                    requiresCustomSerialization = (Boolean)_specialCaseReposIds.get(repositoryID);
                    actualType = ValueType.getInstanceByID(repositoryID);
                }
                break;
            case 6: // TYPE_LIST_VALUE_TAG
                {
                    int n = _cdrInput.read_ulong();
                    if (n < 1) {
                        throw new org.omg.CORBA.MARSHAL("invalid type list length = " + n);
                    }
                    String repositoryID = readMetaString();
// TODO                    requiresCustomSerialization = (Boolean)_specialCaseReposIds.get(repositoryID);
                    actualType = ValueType.getInstanceByID(repositoryID);
                    for (int i = 1; i < n; i++) {
                        String ignore = readMetaString();
                    }
                }
                break;
            default:
                throw new org.omg.CORBA.MARSHAL("invalid value tag = " + tag);
        }
        if (actualType.isObjectRef) {
            value = actualType.helper.read(this);
            return value;
        }
        startBlock();
        if (_isChunked) {
            _endLevel--;
        }
        Integer key = new Integer(saveOffset);
        switch (actualType.readWriteCase) {
            case ValueType.CASE_ARRAY:
                value = readArray(actualType, key);
                break;
            case ValueType.CASE_CLASS:
                value = readClassDesc();
                putIndirection(key, value);
                break;
            case ValueType.CASE_IDL_ENTITY:
                value = actualType.helper.read(this);
                putIndirection(key, value);
                break;
                // case ValueType.CASE_IDL_OBJECT: // already handled above
            case ValueType.CASE_STRING:
                value = _cdrInput.read_wstring();
                putIndirection(key, value);
                break;
            default:
                value = actualType.newInstance();
                putIndirection(key, value);
                Object newValue = readObjectState(actualType, value, false); // requiresCustomSerialization);
                if (newValue != value) {
                    value = newValue;
                    putIndirection(key, value);
                }
        }
        endBlock();
        readEndTag();
        _isChunked = saveIsChunked;
        startBlock();
        return value;
    }

    protected String readMetaString() {
        String id;
        int saveOffset = _cdrInput._offset;
        int tag = _cdrInput.read_long();
        if (tag == ValueType.INDIRECTION_TAG) {
            saveOffset = _cdrInput._offset;
            int offset = _cdrInput.read_long();
            Integer key = IntegerCache.get(saveOffset + offset);
            id = _indirection == null ? null : (String) _indirection.get(key);
            if (id == null) {
                throw new org.omg.CORBA.MARSHAL("invalid indirection offset = " + offset);
            }
        } else {
            _cdrInput._offset = saveOffset;
            id = _cdrInput.read_string();
            putIndirection(IntegerCache.get(saveOffset), id);
        }
        return id;
    }

    protected Object readObjectState(ValueType valueType, Object value, boolean requiresCustomSerialization) {
        if (valueType.isExternalizable) {
            byte format = _cdrInput.read_octet();
            valueType.readExternal(value, this);
            return value;
        }
        if (valueType.hasParentState) {
            value = readObjectState(valueType.parent, value, false);
        }
        if (valueType.hasReadObject) {
            push(new StreamState(valueType, value, _cdrInput._offset));
            /* TODO
            if (repositoryID.equals(_SUN_JDK_BIG_DECIMAL_REPOSID))
            {
                // Sun's first field is an int
                int scale = readInt();
                // Sun's second field is a java.math.BigInteger
                java.math.BigInteger intVal = (java.math.BigInteger)readObject(java.math.BigInteger.class);
                // Create BigDecimal using scale and intVal
                value = new java.math.BigDecimal(intVal, scale);
            }
            else if (repositoryID.equals(_IBM_JDK_BIG_DECIMAL_REPOSID))
            {
                byte format = _cdrInput.read_octet();
                boolean defaultWriteObjectCalled = _cdrInput.read_boolean();
                // IBM's first field is a long
                long intLong = readLong();
                // IBM's second field is a int
                int scale = readInt();
                // IBM's third field is a java.math.BigInteger
                java.math.BigInteger intVal = (java.math.BigInteger)readObject(java.math.BigInteger.class);
                // We can ignore the long, since IBM doesn't use it
                // in the writeObject, to ensure backward's compatibility
                // with their previous versions which don't have the long.
                value = new java.math.BigDecimal(intVal, scale);
            }
            else
            */
            {
                if (valueType.hasWriteObject || requiresCustomSerialization) {
                    byte format = _cdrInput.read_octet();
                    boolean defaultWriteObjectCalled = _cdrInput.read_boolean();
                }
                valueType.readObject(value, this);
            }
            pop();
        } else {
            readDeclaredFields(valueType, value);
        }
        if (valueType.hasReadResolve) {
            value = valueType.readResolve(value);
        }
        return value;
    }

    protected void readDeclaredFields(ValueType valueType, Object value) {
        int n = valueType.fields.length;
        for (int f = 0; f < n; f++) {
            ValueTypeField field = valueType.fields[f];
            int primitive = field.primitive;
            if (primitive != 0) {
                readPrimitive(primitive, field, value);
            } else {
                field.set(value, readObject(field.type, false));
            }
        }
    }

    protected Object readClassDesc() {
        String codebase = (String) readObject(ValueType.STRING_VALUE_TYPE);
        String id = (String) readObject(ValueType.STRING_VALUE_TYPE);
        return ValueType.getInstanceByID(id)._class;
    }

    protected Object readArray(ValueType arrayType, Integer key) {
        Object value = null;
        int primitive = arrayType.primitiveArray;
        int n = _cdrInput.read_ulong();
        if (primitive != 0) {
            value = arrayType.helper.read(this);
            putIndirection(key, value);
        } else {
            Object[] array;
            try {
                array = n == 0 ? ArrayUtil.EMPTY_OBJECT_ARRAY : (Object[]) Array.newInstance(arrayType.element._class, n);
            } catch (Exception ex) {
                throw new SystemException(ex);
            }
            putIndirection(key, array);
            for (int i = 0; i < n; i++) {
                array[i] = readObject(arrayType.element, false);
            }
            value = array;
        }
        return value;
    }

    private void readPrimitive(int primitive, ValueTypeField field, Object value) {
        switch (primitive) {
            case PrimitiveType.BOOLEAN:
                field.setBoolean(value, _cdrInput.read_boolean());
                break;
            case PrimitiveType.BYTE:
                field.setByte(value, _cdrInput.read_octet());
                break;
            case PrimitiveType.CHAR:
                field.setChar(value, _cdrInput.read_wchar());
                break;
            case PrimitiveType.DOUBLE:
                field.setDouble(value, _cdrInput.read_double());
                break;
            case PrimitiveType.FLOAT:
                field.setFloat(value, _cdrInput.read_float());
                break;
            case PrimitiveType.INT:
                field.setInt(value, _cdrInput.read_long());
                break;
            case PrimitiveType.LONG:
                field.setLong(value, _cdrInput.read_longlong());
                break;
            case PrimitiveType.SHORT:
                field.setShort(value, _cdrInput.read_short());
                break;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * * This method handle end tag compaction.  Note that it is lazy in the
     * * sense that it always assumes a tag has been compacted if the end tag
     * * is not what it expected.
     */
    protected void readEndTag() {
        if (_isChunked) {
            int anEndTag = _cdrInput.read_long();
            if (anEndTag != _endLevel) {
                _cdrInput._offset -= 4;
            }
            _endLevel++;
        }
    }

    protected void startBlock() {
        if (!_isChunked) {
            return;
        }
        _blockLength = _cdrInput.read_long();
        if (_blockLength >= 0
            && _blockLength < MAXIMUM_BLOCK_LENGTH) {
            _blockLength += _cdrInput._offset;
        } else {
            // Not a chunk length field.
            _blockLength = MAXIMUM_BLOCK_LENGTH;
            _cdrInput._offset -= 4;
        }
    }

    protected void endBlock() {
        // If in a chunk, check for underflow or overflow.
        if (_blockLength != MAXIMUM_BLOCK_LENGTH) {
            if (_blockLength == _cdrInput._offset) {
                // Chunk ended correctly.
                _blockLength = MAXIMUM_BLOCK_LENGTH;
            }
        }
    }

    protected void push(StreamState state) {
        if (_stack == null) {
            _stack = new ArrayList();
        }
        _stack.add(state);
    }

    protected void pop() {
        int n = _stack.size();
        if (n == 0) {
            throw new SystemException("pop: state stack empty");
        }
        _stack.remove(n - 1);
    }

    protected StreamState top() {
        int n = _stack.size();
        if (n == 0) {
            throw new SystemException("top: state stack empty");
        }
        return (StreamState) _stack.get(n - 1);
    }
}
