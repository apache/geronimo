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

package org.apache.geronimo.security;

import java.security.Principal;


/**
 * @version $Revision: 1.5 $ $Date: 2004/06/13 16:52:53 $
 */
public class PrimaryRealmPrincipal extends RealmPrincipal {

    public PrimaryRealmPrincipal(String realm, Principal principal) {
        super(realm, principal);
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
        if (!(another instanceof PrimaryRealmPrincipal)) return false;

        PrimaryRealmPrincipal realmPrincipal = (PrimaryRealmPrincipal) another;

        return getRealm().equals(realmPrincipal.getRealm()) && getPrincipal().equals(realmPrincipal.getPrincipal());
    }
}
