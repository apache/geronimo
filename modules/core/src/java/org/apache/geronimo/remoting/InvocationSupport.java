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
package org.apache.geronimo.remoting;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;
import org.apache.geronimo.common.Invocation;
/**
 * @version $Revision: 1.3 $ $Date: 2003/08/24 06:07:36 $
 */
public final class InvocationSupport implements Serializable {

    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 3;
    private static final InvocationSupport[] values = new InvocationSupport[MAX_ORDINAL + 1];
    private static final InvocationSupport MARSAHLLED_VALUE = new InvocationSupport("MARSHALED_VALUE", 0);
    private static final InvocationSupport REMOTE_URI = new InvocationSupport("REMOTE_URI", 1);
    private static final InvocationSupport INVOCATION_TYPE = new InvocationSupport("INVOCATION_TYPE", 2);

    public static MarshalledObject getMarshaledValue(Invocation invocation) {
        return (MarshalledObject) invocation.getTransient(MARSAHLLED_VALUE);
    }
    public static void putMarshaledValue(Invocation invocation, MarshalledObject mo) {
        invocation.putTransient(MARSAHLLED_VALUE, mo);
    }
    public static URI getRemoteURI(Invocation invocation) {
        return (URI) invocation.getTransient(REMOTE_URI);
    }
    public static void putRemoteURI(Invocation invocation, URI remoteURI) {
        invocation.putTransient(REMOTE_URI, remoteURI);
    }
    public static InvocationType getInvocationType(Invocation invocation) {
        return (InvocationType) invocation.getTransient(INVOCATION_TYPE);
    }
    public static void putInvocationType(Invocation invocation, InvocationType type) {
        invocation.putTransient(INVOCATION_TYPE, type);
    }
    private final transient String name;
    private final int ordinal;

    private InvocationSupport(String name, int ordinal) {
        assert ordinal < MAX_ORDINAL;
        assert values[ordinal] == null;
        this.name = name;
        this.ordinal = ordinal;
        values[ordinal] = this;
    }

    public String toString() {
        return name;
    }

    Object readResolve() throws ObjectStreamException {
        return values[ordinal];
    }

    static public boolean isAncestor(ClassLoader parent, ClassLoader child) {
        // Root child? ancestor must be root too.
        if (child == null)
            return parent == null;
        // Root parent is the ancestor of all classloaders.
        if (parent == null)
            return true;

        while (child != null) {
            if (child.equals(parent))
                return true;
            child = child.getParent();
        }
        return false;
    }

}
