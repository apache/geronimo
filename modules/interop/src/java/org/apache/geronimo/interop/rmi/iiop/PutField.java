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
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Method;

/**
 ** An implementation of java.io.ObjectOutputStream.PutField
 ** Provide programatic access to the persistent fields to be written
 ** to ObjectOutput.
 **/

public class PutField extends java.io.ObjectOutputStream.PutField
{
    /** class descriptor describing serializable fields */
    private final ObjectStreamClass desc;
    /** primitive field values */
    private final byte[] primVals;
    /** object field values */
    private final Object[] objVals;

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
     * Creates PutField object for writing fields defined in given
     * class descriptor.
     */
    PutField(ObjectStreamClass desc)
    {
        this.desc = desc;
        computeOffsets();
        primVals = new byte[primDataSize];
        objVals = new Object[numObjFields];
    }

    /**
     * Put the value of the named boolean field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, boolean val)
    {
        Bits.putBoolean(primVals, getFieldOffset(name, Boolean.TYPE), val);
    }

    /**
     * Put the value of the named byte field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, byte val)
    {
        primVals[getFieldOffset(name, Byte.TYPE)] = val;
    }

    /**
     * Put the value of the named char field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, char val)
    {
        Bits.putChar(primVals, getFieldOffset(name, Character.TYPE), val);
    }

    /**
     * Put the value of the named short field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, short val)
    {
        Bits.putShort(primVals, getFieldOffset(name, Short.TYPE), val);
    }

    /**
     * Put the value of the named int field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, int val)
    {
        Bits.putInt(primVals, getFieldOffset(name, Integer.TYPE), val);
    }

    /**
     * Put the value of the named long field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, long val)
    {
        Bits.putLong(primVals, getFieldOffset(name, Long.TYPE), val);
    }

    /**
     * Put the value of the named float field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, float val)
    {
        Bits.putFloat(primVals, getFieldOffset(name, Float.TYPE), val);
    }

    /**
     * Put the value of the named double field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, double val)
    {
        Bits.putDouble(primVals, getFieldOffset(name, Double.TYPE), val);
    }

    /**
     * Put the value of the named Object field into the persistent field.
     *
     * @param  name the name of the serializable field
     * @param  val the value to assign to the field
     */
    public void put(String name, Object val)
    {
        objVals[getFieldOffset(name, Object.class)] = val;
    }

    /**
     * Write the data and fields to the specified ObjectOutput stream.
     *
     * @param  out the stream to write the data and fields to
     * @throws IOException if I/O errors occur while writing to the
     *        underlying stream
     * @deprecated This method does not write the values contained by this
     *        <code>PutField</code> object in a proper format, and may
     *        result in corruption of the serialization stream.  The
     *        correct way to write <code>PutField</code> data is by
     *        calling the {@link java.io.ObjectOutputStream#writeFields()}
     *        method.
     */
    public void write(ObjectOutput out) throws IOException
    {
        /*
         * Applications should *not* use this method to write PutField
         * data, as it will lead to stream corruption if the PutField
         * object writes any primitive data (since block data mode is not
         * unset/set properly, as is done in OOS.writeFields()).  This
         * broken implementation is being retained solely for behavioral
         * compatibility, in order to support applications which use
         * OOS.PutField.write() for writing only non-primitive data.
         *
         * Serialization of unshared objects is not implemented here since
         * it is not necessary for backwards compatibility; also, unshared
         * semantics may not be supported by the given ObjectOutput
         * instance.  Applications which write unshared objects using the
         * PutField API must use OOS.writeFields().
         */
        throw new IOException("PutField.write(ObjectOutput) - not supported for RMI/IIOP");
    }

    /**
     * Writes buffered primitive data and object fields to stream.
     */
    void writeFields(ObjectOutputStream o) throws IOException
    {
        org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream out = (org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream)o;

        out._cdrOutput.write_align(4, 4); // write any necessary padding

        //Write out the primitive values first
        for(int i = 0; i < primVals.length; i++)
        {
            out.writeByte(primVals[i]);
        }

        //Write out the object fields
        java.io.ObjectStreamField[] fields = desc.getFields();
        int numPrimFields = fields.length - objVals.length;
        for (int i = 0; i < objVals.length; i++)
        {
            out.writeObject(ValueType.getInstance(objVals[i].getClass()), objVals[i]);
        }
    }

    /**
     * Returns offset of field with given name and type.  A specified type
     * of null matches all types, Object.class matches all non-primitive
     * types, and any other non-null type matches assignable types only.
     * Throws IllegalArgumentException if no matching field found.
     */
    private int getFieldOffset(String name, Class type)
    {
        ObjectStreamField field = getField(name, type);
        if (field == null)
        {
            throw new IllegalArgumentException("no such field");
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
