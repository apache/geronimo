/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.naming.reference;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;

/**
 * @version $Rev:  $ $Date:  $
 */
public class DeserializingReference extends SimpleAwareReference {

    private final byte[] bytes;
    private transient Object content;

    public DeserializingReference(byte[] bytes) {
        this.bytes = bytes;
    }

    public Object getContent() {
        return content;
    }

    public void setClassLoader(ClassLoader classLoader) {
        super.setClassLoader(classLoader);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream is = new ConfigInputStream(bais, classLoader);
            try {
                content = is.readObject();
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize content", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not deserialize content", e);
        }
    }

    private static class ConfigInputStream extends ObjectInputStream {
        private final ClassLoader cl;

        public ConfigInputStream(InputStream in, ClassLoader cl) throws IOException {
            super(in);
            this.cl = cl;
        }

        protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            try {
                return cl.loadClass(desc.getName());
            } catch (ClassNotFoundException e) {
                // let the parent try
                return super.resolveClass(desc);
            }
        }
    }
}
