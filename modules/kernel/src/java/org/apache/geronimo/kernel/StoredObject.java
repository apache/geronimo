/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel;

import java.io.Serializable;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * @version $Rev$ $Date$
 */
public class StoredObject implements Serializable {

    private byte[] content;

    public StoredObject(Serializable object) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(object);
        out.flush();
        out.close();
        this.content = buffer.toByteArray();
        buffer.close();
    }

    public Object getObject(ClassLoader classLoader) throws IOException, ClassNotFoundException {
        ByteArrayInputStream buffer = new ByteArrayInputStream(this.content);
        ObjectInputStream in = new ObjectInputStreamExt(buffer, classLoader);
        Object obj = in.readObject();
        buffer.close();
        in.close();
        return obj;
    }
}
