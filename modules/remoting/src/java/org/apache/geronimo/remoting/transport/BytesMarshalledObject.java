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

package org.apache.geronimo.remoting.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.reflect.Proxy;

import org.apache.geronimo.common.Classes;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.TransportContext;

/**
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:03 $
 */
public class BytesMarshalledObject implements MarshalledObject, Externalizable {

    public class ObjectInputStreamExt extends ObjectInputStream {

        private ClassLoader classloader;
        private TransportContext transportContext;

        public ObjectInputStreamExt(InputStream in, ClassLoader loader, TransportContext transportContext) throws IOException {
            super(in);
            this.transportContext = transportContext;
            this.classloader = loader;
            this.enableResolveObject(transportContext!=null);
        }

        /**
         * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
         */
        protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
            return Classes.loadClass(classDesc.getName(), classloader);
        }

        /**
         * @see java.io.ObjectInputStream#resolveProxyClass(java.lang.String[])
         */
        protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            Class[] cinterfaces = new Class[interfaces.length];
            for (int i = 0; i < interfaces.length; i++)
                cinterfaces[i] = classloader.loadClass(interfaces[i]);

            try {
                return Proxy.getProxyClass(classloader, cinterfaces);
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

    static class ObjectOutputStreamExt extends ObjectOutputStream {

        private TransportContext transportContext;

        /**
         * @param out
         * @throws IOException
         */
        public ObjectOutputStreamExt(OutputStream out, TransportContext transportContext) throws IOException {
            super(out);
            this.transportContext = transportContext;
            enableReplaceObject(transportContext!=null);
        }

        /**
         * @see java.io.ObjectOutputStream#replaceObject(java.lang.Object)
         */
        protected Object replaceObject(Object obj) throws IOException {
            return transportContext.writeReplace(obj);
        }
        
    }

    private byte data[];
    private TransportContext transportContext;

    public BytesMarshalledObject() {
    }

    public BytesMarshalledObject(Object value) throws IOException {
        set(value);
    }

    public BytesMarshalledObject(TransportContext transportContext) {
        this.transportContext = transportContext;
    }

    public BytesMarshalledObject(TransportContext transportContext, Object value) throws IOException {
        this.transportContext = transportContext;
        set(value);
    }

    /**
     * @see org.apache.geronimo.remoting.MarshalledObject#set(java.lang.Object)
     */
    public void set(Object value) throws IOException {
        // Set the transport context so that objects can write replace them selfs by
        // delegating to the TranportContext to create remote proxies.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStreamExt os = new ObjectOutputStreamExt(baos, transportContext);
        os.writeObject(value);
        os.close();
        data = baos.toByteArray();

    }

    public byte[] getBytes() {
        return data;
    }

    public void setBytes(byte[] data) {
        this.data = data;
    }

    public Object get() throws IOException, ClassNotFoundException {
        return get(Thread.currentThread().getContextClassLoader());
    }

    public Object get(ClassLoader classloader) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStreamExt is = new ObjectInputStreamExt(bais, classloader, transportContext);
        Object rc = is.readObject();
        is.close();
        return rc;
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        data = new byte[size];
        in.readFully(data);
    }

}
