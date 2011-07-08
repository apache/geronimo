/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.security.Principal;
import java.io.Serializable;


/**
 * Represents a principal in an realm.
 *
 * @version $Rev$ $Date$
 */
public class RealmPrincipal implements Principal, Serializable {
    private final String realm;
    private final String domain;
    private final Principal principal;
    private transient String name = null;

    public RealmPrincipal(String realm, String domain, Principal principal) {

        if (realm == null) throw new IllegalArgumentException("realm is null");
        if (domain == null) throw new IllegalArgumentException("domain is null");
        if (principal == null) throw new IllegalArgumentException("principal is null");

        this.realm = realm;
        this.domain = domain;
        this.principal = principal;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RealmPrincipal that = (RealmPrincipal) o;

        if (!domain.equals(that.domain)) return false;
        if (!principal.equals(that.principal)) return false;
        if (!realm.equals(that.realm)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = realm.hashCode();
        result = 29 * result + domain.hashCode();
        result = 29 * result + principal.hashCode();
        return result;
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
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        if (name == null) {
            StringBuilder buffer = new StringBuilder("");
            buffer.append(realm);
            buffer.append("::");
            buffer.append(domain);
            buffer.append("::");
            buffer.append(principal.getClass().getName());
            buffer.append(':');
            buffer.append(principal.getName());

            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns the realm that is associated with the principal.
     *
     * @return the realm that is associated with the principal.
     */
    public String getRealm() {
        return realm;
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
    public String getLoginDomain() {
        return domain;
    }
}
