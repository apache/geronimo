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

import org.apache.geronimo.interop.util.JavaObject;

public class SimpleObjectInputStream extends ObjectInputStream
{
    public static ObjectInputStream getInstance()
    {
        ObjectInputStream ois = null;
        try {
            ois = new SimpleObjectInputStream();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return ois;
    }

    public static ObjectInputStream getInstance(byte[] bytes)
    {
        return getInstance(CdrInputStream.getInstance(bytes));
    }

    public static ObjectInputStream getInstance(org.apache.geronimo.interop.rmi.iiop.CdrInputStream cdrInput)
    {
        ObjectInputStream input = getInstance();
        input.init(cdrInput);
        return input;
    }

    public static ObjectInputStream getPooledInstance()
    {
        ObjectInputStream input = null;
        if (input == null)
        {
            input = getInstance();
        }
        return input;
    }

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public SimpleObjectInputStream() throws IOException
    {
        super();
    }

    public void $reset()
    {
        _cdrInput.reset();
    }

    public void recycle()
    {
        $reset();
    }

    public Exception readException(ValueType type)
    {
        return (Exception)readObject(type);
    }

    public Object readObject(ValueType type)
    {
        ObjectHelper h = type.helper;
        if (h != null)
        {
            return h.read(this);
        }
        byte[] bytes = _cdrInput.read_octet_sequence();
        Object value = bytes.length == 0 ? null : JavaObject.fromByteArray(bytes);
        return value;
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(org.apache.geronimo.interop.rmi.iiop.CdrInputStream cdrInput)
    {
        super.init(cdrInput);
    }
}
