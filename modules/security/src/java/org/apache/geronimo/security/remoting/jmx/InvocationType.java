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
/**
 * @version $Rev$ $Date$
 */
public final class InvocationType implements Serializable {


    private static final long serialVersionUID = 4049360807479227955L;
    
    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 2;
    private static final InvocationType[] values = new InvocationType[MAX_ORDINAL + 1];
    public static final InvocationType REQUEST = new InvocationType("REQUEST", 0);
    public static final InvocationType DATAGRAM = new InvocationType("DATAGRAM", 1);

    private final transient String name;
    private final int ordinal;

    private InvocationType(String name, int ordinal) {
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
}
