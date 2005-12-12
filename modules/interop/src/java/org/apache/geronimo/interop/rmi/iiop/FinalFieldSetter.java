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

import org.apache.geronimo.interop.util.SystemUtil;

import java.lang.reflect.Field;

public abstract class FinalFieldSetter
{
    private static final boolean JDK14 = SystemUtil.isJDK14();

    public static FinalFieldSetter getInstance(Field f)
    {
        if(JDK14)
        {
            return new FinalFieldSetterJdk14(f);
        }
        else
        {
            throw new RuntimeException("FinalFieldSetter is not implemented for jdk version: "  +
                SystemUtil.getVmVersion());
        }
    }

    public abstract void setBoolean(Object that, boolean value);
    public abstract void setByte(Object that, byte value);
    public abstract void setChar(Object that, char value);
    public abstract void setDouble(Object that, double value);
    public abstract void setFloat(Object that, float value);
    public abstract void setInt(Object that, int value);
    public abstract void setLong(Object that, long value);
    public abstract void setShort(Object that, short value);
    public abstract void set(Object that, Object value);
}
