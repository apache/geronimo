/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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

import org.apache.geronimo.core.util.ClassUtil;
import org.apache.geronimo.remoting.MarshalledObject;

/**
 * @version $Revision: 1.1 $ $Date: 2003/08/22 02:23:26 $
 */
public class BytesMarshalledObject implements MarshalledObject, Externalizable {

    public class ObjectInputStreamExt extends ObjectInputStream {

        private ClassLoader classloader;

        public ObjectInputStreamExt(InputStream in, ClassLoader loader) throws IOException {
            super(in);
            this.classloader = loader;
        }

        /**
         * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
         */
        protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
            return ClassUtil.resolveObjectStreamClass(classloader, classDesc.getName());
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

    }

    static class ObjectOutputStreamExt extends ObjectOutputStream {

        /**
         * @param out
         * @throws IOException
         */
        public ObjectOutputStreamExt(OutputStream out) throws IOException {
            super(out);
            // TODO Auto-generated constructor stub
        }

    }

    private byte data[];

    public BytesMarshalledObject() {
    }

    public BytesMarshalledObject(Object value) throws IOException {
        set(value);
    }

    /**
     * @see org.apache.geronimo.remoting.MarshalledObject#set(java.lang.Object)
     */
    public void set(Object value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStreamExt os = new ObjectOutputStreamExt(baos);
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
        ObjectInputStreamExt is = new ObjectInputStreamExt(bais, classloader);
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
