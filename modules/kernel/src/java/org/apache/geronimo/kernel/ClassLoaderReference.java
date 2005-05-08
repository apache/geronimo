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

import java.io.IOException;
import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class ClassLoaderReference extends ClassLoader implements Serializable {
    private ClassLoader classloader;

    public ClassLoaderReference(ClassLoader parent) {
        super(parent);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        ObjectInputStreamExt objectInputStreamExt = (ObjectInputStreamExt)in;
        classloader = objectInputStreamExt.getClassloader();
    }

    private Object readResolve() {
        return classloader;
    }
}
