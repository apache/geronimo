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

package org.apache.geronimo.security.realm.providers;

import java.io.Serializable;
import java.security.Principal;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */
public class PropertiesFileUserPrincipal implements Principal, Serializable {
    private final String name;

    public PropertiesFileUserPrincipal(String name) {
        this.name = name;
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
        if (!(another instanceof PropertiesFileUserPrincipal)) return false;

        return ((PropertiesFileUserPrincipal) another).name.equals(name);
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return a string representation of this principal.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return a hashcode for this principal.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        return name;
    }
}
