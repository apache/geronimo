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

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 ** An implementation of java.io.ObjectInputStream.GetField
 ** Provide access to the persistent fields read from the input stream.
 **/

public class GetField extends java.io.ObjectInputStream.GetField
{
    /** class descriptor describing serializable fields */
    private final ObjectStreamClass desc;
    /** primitive field values */
    private final byte[] primVals;
    /** object field values */
    private final Object[] objVals;
    /** object field value handles */
    private final int[] objHandles;

    private int primDataSize = 0;
    private int numObjFields = 0;
    private ObjectStreamField[] _fields = null;

    private static Method setOffsetMethod;

    static
    {
        try
        {
            Class osFieldClass = java.io.ObjectStreamField.class;
            Class[] params = new Class[1];
            params[0] = int.class;
            setOffsetMethod = osFieldClass.getDeclaredMethod("setOffset", params);
            setOffsetMethod.setAccessible(true);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    /**
     * Creates GetFieldImpl object for reading fields defined in given
     * class descriptor.
     */
    GetField(ObjectStreamClass desc)
    {
        this.desc = desc;
        computeOffsets();
        primVals = new byte[primDataSize];
        objVals = new Object[numObjFields];
        objHandles = new int[objVals.length];
    }
    
    /**
    * Get the ObjectStreamClass that describes the fields in the stream.
    *
    * @return  the descriptor class that describes the serializable fields
    */
    public ObjectStreamClass getObjectStreamClass()
    {
        return desc;
    }
 
    /**
    * Return true if the named field is defaulted and has no value in this
    * stream.
    *
    * @param  name the name of the field
    * @return true, if and only if the named field is defaulted
    * @throws IOException if there are I/O errors while reading from
    *         the underlying <code>InputStream</code>
    * @throws IllegalArgumentException if <code>name</code> does not
    *         correspond to a serializable field
    */
    public boolean defaulted(String name) throws IOException
    {
        int fieldOffset = getFieldOffset(name, null);
        boolean result;
        if(fieldOffset < 0)
        {
            result = false;
        }
        else
        {
            result = true;
        }
        return result;
    }
 
    /**
     * Get the value of the named boolean field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>boolean</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public boolean get(String name, boolean val) throws IOException
    {
        int off = getFieldOffset(name, Boolean.TYPE);
        return (off >= 0) ? Bits.getBoolean(primVals, off) : val;
    }
    
    /**
     * Get the value of the named byte field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>byte</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public byte get(String name, byte val) throws IOException
    {
        int off = getFieldOffset(name, Byte.TYPE);
        return (off >= 0) ? primVals[off] : val;
    }
 
    /**
     * Get the value of the named char field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>char</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public char get(String name, char val) throws IOException
    {
        int off = getFieldOffset(name, Character.TYPE);
        return (off >= 0) ? Bits.getChar(primVals, off) : val;
    }
 
    /**
     * Get the value of the named short field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>short</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public short get(String name, short val) throws IOException
    {
        int off = getFieldOffset(name, Short.TYPE);
        return (off >= 0) ? Bits.getShort(primVals, off) : val;
    }
 
    /**
     * Get the value of the named int field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>int</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public int get(String name, int val) throws IOException
    {
        int off = getFieldOffset(name, Integer.TYPE);
        return (off >= 0) ? Bits.getInt(primVals, off) : val;
    }
 
    /**
     * Get the value of the named long field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>long</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public long get(String name, long val) throws IOException
    {
        int off = getFieldOffset(name, Long.TYPE);
        return (off >= 0) ? Bits.getLong(primVals, off) : val;
    }
    
    /**
     * Get the value of the named float field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>float</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public float get(String name, float val) throws IOException
    {
        int off = getFieldOffset(name, Float.TYPE);
        return (off >= 0) ? Bits.getFloat(primVals, off) : val;
    }
 
    /**
     * Get the value of the named double field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>double</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public double get(String name, double val) throws IOException
    {
        int off = getFieldOffset(name, Double.TYPE);
        return (off >= 0) ? Bits.getDouble(primVals, off) : val;
    }
 
    /**
     * Get the value of the named Object field from the persistent field.
     *
     * @param  name the name of the field
     * @param  val the default value to use if <code>name</code> does not
     *        have a value
     * @return the value of the named <code>Object</code> field
     * @throws IOException if there are I/O errors while reading from the
     *        underlying <code>InputStream</code>
     * @throws IllegalArgumentException if type of <code>name</code> is
     *        not serializable or if the field type is incorrect
     */
    public Object get(String name, Object val) throws IOException
    {
        int off = getFieldOffset(name, Object.class);
        if (off >= 0)
        {
            return objVals[off];
        }
        else
        {
            return val;
        }
    }


    /**
     * Reads primitive and object field values from stream.
     */
    void readFields(ObjectInputStream oi) throws IOException
    {
        org.apache.geronimo.interop.rmi.iiop.ObjectInputStream in =
            (org.apache.geronimo.interop.rmi.iiop.ObjectInputStream)oi;

        in._cdrInput.read_align(4, 4);

        //Read in primitive values first
        for(int i = 0; i < primVals.length; i++)
        {
            primVals[i] = in.readByte();
        }
        
        //Read in the object fields
        java.io.ObjectStreamField[] fields = desc.getFields();
        int numPrimFields = fields.length - objVals.length;
        for (int i = 0; i < objVals.length; i++)
        {
            objVals[i] = in.readObject(ValueType.getInstance(fields[numPrimFields + i].getType()));
        }
    }

    private int getFieldOffset(String name, Class type)
    {
        ObjectStreamField field = getField(name, type);
        if (field == null)
        {
            throw new IllegalArgumentException("no such field: " + name  + " of type: " + type.getName());
        }
        return field.getOffset();
    }

    private ObjectStreamField getField(String name, Class type)
    {
        if(type == null)
        {
            //Return match by name
            for(int i = 0; i < _fields.length; i++)
            {
                if(_fields[i].getName().equals(name))
                {
                    return _fields[i];
                }
            }
            return (ObjectStreamField)null;
        }
        else if(type == java.lang.Object.class)
        {
            //Return match for name, and any non-primitive type
            for(int i = 0; i < _fields.length; i++)
            {
                if(_fields[i].getName().equals(name) && !_fields[i].getType().isPrimitive())
                {
                    return _fields[i];
                }
            }
            return (ObjectStreamField)null;
        }
        else
        {
            for(int i = 0; i < _fields.length; i++)
            {
                if(_fields[i].getName().equals(name) && _fields[i].getType().equals(type))
                {
                    return _fields[i];
                }
            }
            return (ObjectStreamField)null;
       }
    }

    private void computeOffsets()
    {
        try
        {
            computeFieldOffsets();
        }
        catch(Exception e)
        {
            throw new RuntimeException(org.apache.geronimo.interop.util.ExceptionUtil.causedBy(e));
        }
    }
    
    private void computeFieldOffsets() throws Exception
    {
        primDataSize = 0;
        numObjFields = 0;
        int firstObjIndex = -1;
        java.io.ObjectStreamField[] fields = desc.getFields();
        _fields = new ObjectStreamField[fields.length];
        Object[] args = new Object[1];

        for (int i = 0; i < fields.length; i++)
        {
            java.io.ObjectStreamField f = fields[i];
            _fields[i] = new ObjectStreamField(fields[i].getName(), fields[i].getType());
            ObjectStreamField _f = _fields[i];
            
            switch (f.getTypeCode())
            {
                case 'Z':
                case 'B':
                    args[0] = new Integer(primDataSize++);
                    setOffsetMethod.invoke(_f, args);
                    break;

                case 'C':
                case 'S':
                    args[0] = new Integer(primDataSize);
                    setOffsetMethod.invoke(_f, args);
                    primDataSize += 2;
                    break;

                case 'I':
                case 'F':
                    args[0] = new Integer(primDataSize);
                    setOffsetMethod.invoke(_f, args);
                    primDataSize += 4;
                    break;

                case 'J':
                case 'D':
                    args[0] = new Integer(primDataSize);
                    setOffsetMethod.invoke(_f, args);
                    primDataSize += 8;
                    break;

                case '[':
                case 'L':
                    args[0] = new Integer(numObjFields++);
                    setOffsetMethod.invoke(_f, args);
                    if (firstObjIndex == -1)
                    {
                        firstObjIndex = i;
                    }
                    break;

                default:
                    break;
            }
        }
        if (firstObjIndex != -1 && firstObjIndex + numObjFields != fields.length)
        {   
            //throw new InvalidClassException(name, "illegal field order");
        }
    }
}
