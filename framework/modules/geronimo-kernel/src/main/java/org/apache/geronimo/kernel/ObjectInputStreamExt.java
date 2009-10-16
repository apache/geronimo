/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class ObjectInputStreamExt extends ObjectInputStream {

    private Bundle bundle;

    public ObjectInputStreamExt(InputStream in, Bundle bundle) throws IOException {
        super(in);
        this.bundle = bundle;
    }

    protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        return ClassLoading.loadClass(classDesc.getName(), bundle);
    }

//see http://www.osgi.org/blog/2008/08/classy-solutions-to-tricky-proxies.html
    //currently broken
    protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        Class[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++)
            cinterfaces[i] = bundle.loadClass(interfaces[i]);

        try {
            return Proxy.getProxyClass(null, cinterfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

}
