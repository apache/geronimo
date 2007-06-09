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
package org.apache.geronimo.security.deploy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.security.Principal;

import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.DomainPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class Role implements Serializable {

    private String roleName;
    private final Set<RealmPrincipalInfo> realmPrincipals = new HashSet<RealmPrincipalInfo>();
    private final Set<LoginDomainPrincipalInfo> domainPrincipals = new HashSet<LoginDomainPrincipalInfo>();
    private final Set<PrincipalInfo> principals = new HashSet<PrincipalInfo>();
    private final Set<DistinguishedName> distinguishedNames = new HashSet<DistinguishedName>();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Set<RealmPrincipalInfo> getRealmPrincipals() {
        return realmPrincipals;
    }

    public Set<LoginDomainPrincipalInfo> getLoginDomainPrincipals() {
        return domainPrincipals;
    }

    public Set<PrincipalInfo> getPrincipals() {
        return principals;
    }

    public Set<DistinguishedName> getDistinguishedNames() {
        return distinguishedNames;
    }
}
