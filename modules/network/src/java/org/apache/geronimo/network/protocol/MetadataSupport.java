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

package org.apache.geronimo.network.protocol;

import javax.security.auth.Subject;

import java.io.ObjectStreamException;
import java.io.Serializable;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:13 $
 */
public class MetadataSupport implements Serializable {

    // Be careful here.  If you change the ordinals, this class must be changed on evey client.
    private static int MAX_ORDINAL = 1;
    private static final MetadataSupport[] values = new MetadataSupport[MAX_ORDINAL];
    private static final MetadataSupport SUBJECT = new MetadataSupport("SUBJECT", 0);

    public static Subject getSubject(UpPacket packet) {
        return (Subject) packet.getMetadata(SUBJECT);
    }

    public static void setSubject(UpPacket packet, Subject subject) {
        packet.setMetadata(SUBJECT, subject);
    }

    private final transient String name;
    private final int ordinal;

    private MetadataSupport(String name, int ordinal) {
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
