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
package org.apache.geronimo.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

import org.apache.geronimo.common.Classes;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:27 $
 */
public class ResolvingObjectInputStream extends ObjectInputStream {

    private ClassLoader classLoader;
    private TransportContext transportContext;

    public ResolvingObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        this(in, classLoader, null);
    }

    public ResolvingObjectInputStream(InputStream in, ClassLoader classLoader, TransportContext transportContext) throws IOException {
        super(in);
        this.classLoader = classLoader;
        this.transportContext = transportContext;
        this.enableResolveObject(transportContext != null);
    }

    /**
     * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
     */
    protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        return Classes.loadClass(classDesc.getName(), classLoader);
    }

    /**
     * @see java.io.ObjectInputStream#resolveProxyClass(java.lang.String[])
     */
    protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        Class[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++)
            cinterfaces[i] = classLoader.loadClass(interfaces[i]);

        try {
            return Proxy.getProxyClass(classLoader, cinterfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    /**
     * @see java.io.ObjectInputStream#resolveObject(java.lang.Object)
     */
    protected Object resolveObject(Object obj) throws IOException {
        return transportContext.readReplace(obj);
    }
}
