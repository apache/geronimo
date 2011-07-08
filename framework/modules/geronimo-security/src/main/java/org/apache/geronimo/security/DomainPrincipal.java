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

import java.io.Serializable;
import java.security.Principal;

/**
 * Represents a principal in an realm.
 *
 * @version $Rev$ $Date$
 */
public class DomainPrincipal implements Principal, Serializable {
    private final String domain;
    private final Principal principal;
    private transient String name = null;

    public DomainPrincipal(String domain, Principal principal) {
        if (domain == null) throw new IllegalArgumentException("domain is null");
        if (principal == null) throw new IllegalArgumentException("principal is null");

        this.domain = domain;
        this.principal = principal;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DomainPrincipal that = (DomainPrincipal) o;

        if (!domain.equals(that.domain)) return false;
        if (!principal.equals(that.principal)) return false;

        return true;
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
        int result;
        result = domain.hashCode();
        result = 29 * result + principal.hashCode();
        return result;
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        if (name == null) {

            StringBuilder buffer = new StringBuilder("");
            buffer.append(domain);
            buffer.append("::");
            buffer.append(principal.getClass().getName());
            buffer.append(':');
            buffer.append(principal.getName());
            buffer.append("]");

            name = buffer.toString();
        }
        return name;
//        return principal.getName();
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
    public String getDomain() {
        return domain;
    }
}
