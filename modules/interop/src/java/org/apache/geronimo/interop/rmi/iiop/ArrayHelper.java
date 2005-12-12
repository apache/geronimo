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

import org.apache.geronimo.interop.util.ArrayUtil;

import java.lang.reflect.*;

public class ArrayHelper implements ObjectHelper
{
    private ValueType _element;

    private ObjectHelper _primitive;

    public ArrayHelper(Class elementClass)
    {
        if (elementClass.isPrimitive())
        {
            _primitive = PrimitiveType.getArrayHelper(elementClass);
        }
        else
        {
            _element = ValueType.getInstance(elementClass);
        }
    }

    public Object read(ObjectInputStream input)
    {
        if (_primitive != null)
        {
            return _primitive.read(input);
        }
        CdrInputStream cdrInput = input._cdrInput;
        int n = cdrInput.read_long();
        Object[] array = (Object[])Array.newInstance(_element._class, n);
        for (int i = 0; i < n; i++)
        {
            array[i] = input.readObject(_element);
        }
        return array;
    }

    public void write(ObjectOutputStream output, Object value)
    {
        if (_primitive != null)
        {
            _primitive.write(output, value);
            return;
        }
        CdrOutputStream cdrOutput = output._cdrOutput;
        Object[] array = (Object[])value;
        if (array == null)
        {
            array = ArrayUtil.EMPTY_OBJECT_ARRAY;
        }
        int n = array.length;
        cdrOutput.write_long(n);
        for (int i = 0; i < n; i++)
        {
            output.writeObject(_element, array[i]);
        }
    }
}
