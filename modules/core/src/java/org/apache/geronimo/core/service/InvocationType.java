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

package org.apache.geronimo.core.service;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationKey;

/**
 *
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:42 $
 */
public final class InvocationType implements Serializable, InvocationKey {
    private static final StringInvocationKey INVOCATION_TYPE_KEY = new StringInvocationKey("INVOCATION_TYPE_KEY", false);

    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 3;
    private static final InvocationType[] values = new InvocationType[MAX_ORDINAL + 1];
    public static final InvocationType REMOTE = new InvocationType("REMOTE", 0, false, false);
    public static final InvocationType HOME = new InvocationType("HOME", 1, false, true);
    public static final InvocationType LOCAL = new InvocationType("LOCAL", 2, true, false);
    public static final InvocationType LOCALHOME = new InvocationType("LOCALHOME", 3, false, false);

    public static InvocationType getType(Invocation invocation) {
        return (InvocationType) invocation.get(INVOCATION_TYPE_KEY);
    }

    public static void putType(Invocation invocation, InvocationType type) {
        invocation.put(INVOCATION_TYPE_KEY, type);
    }

    private final transient String name;
    private final transient boolean local;
    private final transient boolean home;
    private final int ordinal;

    private InvocationType(String name, int ordinal, boolean local, boolean home) {
        assert ordinal <= MAX_ORDINAL;
        assert values[ordinal] == null;
        this.name = name;
        this.local = local;
        this.home = home;
        this.ordinal = ordinal;
        values[ordinal] = this;
    }
    
    /**
     * @see org.apache.geronimo.core.service.InvocationKey#isTransient()
     */
    public boolean isTransient() {
        return false;
    }
    

    public boolean isRemoteInvocation() {
        return !local;
    }

    public boolean isLocalInvocation() {
        return local;
    }

    public boolean isHomeInvocation() {
        return home;
    }

    public boolean isBeanInvocation() {
        return !home;
    }

    public String toString() {
        return name;
    }

    Object readResolve() throws ObjectStreamException {
        return values[ordinal];
    }

}
