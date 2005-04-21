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

import org.apache.geronimo.interop.util.ArrayUtil;
import org.apache.geronimo.interop.util.JavaObject;

public class SimpleObjectOutputStream extends ObjectOutputStream
{
    public static ObjectOutputStream getInstance()
    {
        ObjectOutputStream oos = null;
        try {
            oos = new SimpleObjectOutputStream();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public SimpleObjectOutputStream() throws IOException
    {
        super();
    }

    public void $reset()
    {
        _cdrOutput.reset();
    }

    public void recycle()
    {
        $reset();
    }

    public void writeException(ValueType type, Exception value)
    {
        String repositoryID = "IDL:" + type._class.getName().replace('.', '/') + ":1.0";
        _cdrOutput.write_string(repositoryID);
        writeObject(type, value);
        _hasException = true;
    }

    public void writeObject(ValueType type, Object value)
    {
        ObjectHelper h = type.helper;
        if (h != null)
        {
            h.write(this, value);
            return;
        }
        byte[] bytes = JavaObject.toByteArray(value);
        if (bytes == null) bytes = ArrayUtil.EMPTY_BYTE_ARRAY;
        _cdrOutput.write_octet_sequence(bytes);
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(CdrOutputStream cdrOutput)
    {
        super.init(cdrOutput);
    }
}
