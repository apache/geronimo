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
package org.apache.geronimo.security.deploy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @version $Rev$ $Date$
 */
public class Role implements Serializable {

    private String roleName;
    private Map realms = new HashMap();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Map getRealms() {
        return realms;
    }

    public void append(Realm realm) {
        if (realms.containsKey(realm.getRealmName())) {
            Realm existing = (Realm) realms.get(realm.getRealmName());
            existing.getPrincipals().addAll(realm.getPrincipals());
        } else {
            realms.put(realm.getRealmName(), realm);
        }
    }
}
