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

import java.lang.reflect.Field;

/*
 * FinalFields that are sent across the wire .. how to unmarshall and recreate the object on the
 * receiving side?  We don't want to invoke the constructor since it would establish values for
 * final fields.  We have to recreate the final field exactly like it was on the sender side.
 *
 * The sun.misc.Unsafe does this for us.
 */

public class FinalFieldSetterJdk14 extends FinalFieldSetter
{
    private final long fieldOffset;
    private static final sun.misc.Unsafe unsafe; //Only available for Sun's JDK1.4+

    static 
    {
        sun.misc.Unsafe val = null;
        try
        {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            val = (sun.misc.Unsafe)unsafeField.get((java.lang.Object)null);
        }
        catch(Throwable e)
        {
        }
        unsafe = val;
    }

    public FinalFieldSetterJdk14(Field field)
    {
        if(unsafe != null)
        {
            fieldOffset = unsafe.objectFieldOffset(field);
        }
        else
        {
            fieldOffset = -1;
        }
    }

    public void setBoolean(Object that, boolean value)
    {
        unsafe.putBoolean(that, fieldOffset, value);
    }

    public void setByte(Object that, byte value)
    {
        unsafe.putByte(that, fieldOffset, value);
    }

    public void setChar(Object that, char value)
    {
        unsafe.putChar(that, fieldOffset, value);
    }

    public void setDouble(Object that, double value)
    {
        unsafe.putDouble(that, fieldOffset, value);
    }

    public void setFloat(Object that, float value)
    {
        unsafe.putFloat(that, fieldOffset, value);
    }

    public void setInt(Object that, int value)
    {
        unsafe.putInt(that, fieldOffset, value);
    }

    public void setLong(Object that, long value)
    {
        unsafe.putLong(that, fieldOffset, value);
    }

    public void setShort(Object that, short value)
    {
        unsafe.putShort(that, fieldOffset, value);
    }

    public void set(Object that, Object value)
    {
        unsafe.putObject(that, fieldOffset, value);
    }
}
