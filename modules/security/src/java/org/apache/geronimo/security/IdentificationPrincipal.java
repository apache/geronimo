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

package org.apache.geronimo.security;

import java.io.Serializable;
import java.security.Principal;


/**
 * @version $Revision: 1.4 $ $Date: 2004/06/13 16:52:29 $
 */
public class IdentificationPrincipal implements Principal, Serializable {
    private final SubjectId id;
    private transient String name;

    public IdentificationPrincipal(SubjectId id) {
        this.id = id;
    }

    public SubjectId getId() {
        return id;
    }

    /**
     * Compares this principal to the specified object.  Returns true
     * if the object passed in matches the principal represented by
     * the implementation of this interface.
     *
     * @param another principal to compare with.
     * @return true if the principal passed in is the same as that
     *         encapsulated by this principal, and false otherwise.
     */
    public boolean equals(Object another) {
        if (!(another instanceof IdentificationPrincipal)) return false;

        IdentificationPrincipal idPrincipal = (IdentificationPrincipal) another;

        return id.equals(idPrincipal.id);
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return a string representation of this principal.
     */
    public String toString() {
        return getName();
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return a hashcode for this principal.
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        if (name == null) {

            StringBuffer buffer = new StringBuffer("");
            buffer.append(getClass().getName());
            buffer.append("[");
            buffer.append(id);
            buffer.append("]");

            name = buffer.toString();
        }
        return name;
    }
}
