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
import java.lang.reflect.*;

public class ValueTypeField
{
    public final Field javaField;

    public final int primitive;

    public final ValueType type;

    private final boolean isFinal;

    private FinalFieldSetter finalFieldSetter;

    public ValueTypeField(Field field)
    {
        javaField = field;
        if (field.getType().isPrimitive())
        {
            primitive = PrimitiveType.get(field.getType());
            type = null;
        }
        else
        {
            primitive = 0;
            type = ValueType.getInstance(field.getType());
        }
        if(Modifier.isFinal(field.getModifiers()))
        {
            isFinal = true;
            finalFieldSetter = FinalFieldSetter.getInstance(field);
        }
        else
        {
            isFinal = false;
        }
    }

    public boolean getBoolean(Object that)
    {
        try
        {
            return javaField.getBoolean(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public byte getByte(Object that)
    {
        try
        {
            return javaField.getByte(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public char getChar(Object that)
    {
        try
        {
            return javaField.getChar(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public double getDouble(Object that)
    {
        try
        {
            return javaField.getDouble(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public float getFloat(Object that)
    {
        try
        {
            return javaField.getFloat(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public int getInt(Object that)
    {
        try
        {
            return javaField.getInt(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public long getLong(Object that)
    {
        try
        {
            return javaField.getLong(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public short getShort(Object that)
    {
        try
        {
            return javaField.getShort(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public Object get(Object that)
    {
        try
        {
            return javaField.get(that);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setBoolean(Object that, boolean value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setBoolean(that, value);
            }
            else
            {
                javaField.setBoolean(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setByte(Object that, byte value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setByte(that, value);
            }
            else
            {
                javaField.setByte(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setChar(Object that, char value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setChar(that, value);            
            }
            else
            {
                javaField.setChar(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setDouble(Object that, double value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setDouble(that, value);
            }
            else
            {
                javaField.setDouble(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setFloat(Object that, float value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setFloat(that, value);
            }
            else
            {
                javaField.setFloat(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setInt(Object that, int value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setInt(that, value);
            }
            else
            {
                javaField.setInt(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setLong(Object that, long value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setLong(that, value);
            }
            else
            {
                javaField.setLong(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void setShort(Object that, short value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.setShort(that, value);
            }
            else
            {
                javaField.setShort(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }

    public void set(Object that, Object value)
    {
        try
        {
            if(isFinal)
            {
                finalFieldSetter.set(that, value);
            }
            else
            {
                javaField.set(that, value);
            }
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
    }
}
