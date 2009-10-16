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
package org.apache.geronimo.security.realm.providers;

import java.io.Serializable;
import java.security.Principal;

/**
 * A principal that represents a user for the login modules distributed
 * with Geronimo.  Custom login modules may use this if convenient or provide
 * their own Principal implementations -- it doesn't matter.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoUserPrincipal implements Principal, Serializable, GeronimoCallerPrincipal {
    private final String name;

    public GeronimoUserPrincipal(String name) {
        this.name = name;
    }

    /**
     * Compares this principal to the specified object.  Returns true
     * if the object passed in is a GeronimoUserPrincipal with the
     * same name.
     */
    public boolean equals(Object another) {
        if (!(another instanceof GeronimoUserPrincipal)) return false;

        return ((GeronimoUserPrincipal) another).name.equals(name);
    }

    /**
     * Returns a string representation of this principal.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns a hashcode for this principal.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the name of this principal.
     */
    public String getName() {
        return name;
    }
}
