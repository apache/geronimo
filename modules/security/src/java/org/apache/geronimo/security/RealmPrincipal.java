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
 * Represents a principal in an realm.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:08 $
 */
public class RealmPrincipal implements Principal, Serializable {
    private final String realm;
    private final Principal principal;
    private transient String name = null;
    private transient long id;

    public RealmPrincipal(String realm, Principal principal) {
        if (realm == null) throw new IllegalArgumentException("realm == null");
        if (principal == null) throw new IllegalArgumentException("principal == null");

        this.realm = realm;
        this.principal = principal;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
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
        if (!(another instanceof RealmPrincipal)) return false;

        RealmPrincipal realmPrincipal = (RealmPrincipal) another;

        return realm.equals(realmPrincipal.realm) && principal.equals(realmPrincipal.principal);
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
            buffer.append(realm);
            buffer.append(":[");
            buffer.append(principal.getClass().getName());
            buffer.append(':');
            buffer.append(principal.getName());
            buffer.append("]");

            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns the principal that is associated with the realm.
     *
     * @return the principal that is associated with the realm.
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * Returns the realm that is associated with the principal.
     *
     * @return the realm that is associated with the principal.
     */
    public String getRealm() {
        return realm;
    }
}
