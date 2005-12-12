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

package org.apache.geronimo.security.remoting.jmx;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationKey;
/**
 * @version $Rev$ $Date$
 */
public final class InvocationSupport implements Serializable, InvocationKey {

    private static final long serialVersionUID = 3690191053600796981L;

    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 2;
    private static final InvocationSupport[] values = new InvocationSupport[MAX_ORDINAL + 1];
    private static final InvocationSupport REMOTE_URI = new InvocationSupport("REMOTE_URI", 0);
    private static final InvocationSupport INVOCATION_TYPE = new InvocationSupport("INVOCATION_TYPE", 1);

    public static URI getRemoteURI(Invocation invocation) {
        return (URI) invocation.get(REMOTE_URI);
    }
    public static void putRemoteURI(Invocation invocation, URI remoteURI) {
        invocation.put(REMOTE_URI, remoteURI);
    }
    public static InvocationType getInvocationType(Invocation invocation) {
        return (InvocationType) invocation.get(INVOCATION_TYPE);
    }
    public static void putInvocationType(Invocation invocation, InvocationType type) {
        invocation.put(INVOCATION_TYPE, type);
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
    
    /**
     * @see org.apache.geronimo.core.service.InvocationKey#isTransient()
     */
    public boolean isTransient() {
        return true;
    }

}
