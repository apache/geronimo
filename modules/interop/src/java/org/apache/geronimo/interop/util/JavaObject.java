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
package org.apache.geronimo.interop.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.geronimo.interop.SystemException;


public abstract class JavaObject {
    public static byte[] toByteArray(Object object) {
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bs);
            os.writeObject(object);
            // Ensure last byte is not NUL. Avoids truncation of values when
            // stored in Sybase ASE databases.
            os.writeByte((byte) '.');
            os.flush();
            byte[] buffer = bs.toByteArray();
            os.close();
            bs.close();
            return buffer;
        } catch (Exception ex) {
            throw new SystemException("JavaObject.toByteArray", ex);
        }
    }

    public static java.lang.Object fromByteArray(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        try {
            ByteArrayInputStream bs = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(bs);
            Object object = is.readObject();
            is.close();
            bs.close();
            return object;
        } catch (Exception ex) {
            throw new SystemException("JavaObject.fromByteArray", ex);
        }
    }
}
