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
package org.apache.geronimo.security.deployment;

import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.geronimo.security.GerDefaultPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;


/**
 * @version $Rev:  $ $Date:  $
 */
public class SecurityBuilder {

    public static Security buildSecurityConfig(GerSecurityType securityType, Set roleNames) throws DeploymentException {
        Security security = null;

        if (securityType == null) {
            return null;
        }
        security = new Security();

        security.setDoAsCurrentCaller(securityType.getDoasCurrentCaller());
        security.setUseContextHandler(securityType.getUseContextHandler());
        if (securityType.isSetDefaultRole()) {
            security.setDefaultRole(securityType.getDefaultRole().trim());
        }

        GerRoleMappingsType roleMappingsType = securityType.getRoleMappings();
        Set allRealms = new HashSet();
        if (roleMappingsType != null) {
            for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                GerRoleType roleType = roleMappingsType.getRoleArray(i);
                Role role = new Role();

                String roleName = roleType.getRoleName().trim();
                role.setRoleName(roleName);

                for (int j = 0; j < roleType.sizeOfRealmArray(); j++) {
                    GerRealmType realmType = roleType.getRealmArray(j);
                    String realmName = realmType.getRealmName().trim();
                    allRealms.add(realmName);
                    Realm realm = new Realm();

                    realm.setRealmName(realmName);

                    for (int k = 0; k < realmType.sizeOfPrincipalArray(); k++) {
                        realm.getPrincipals().add(buildPrincipal(realmType.getPrincipalArray(k)));
                    }

                    role.getRealms().put(realmName, realm);
                }

                security.getRoleMappings().put(roleName, role);
            }
        }

        security.getRoleNames().addAll(roleNames);

        DefaultPrincipal defaultPrincipal = new DefaultPrincipal();
        if (securityType.isSetDefaultPrincipal()) {
            GerDefaultPrincipalType defaultPrincipalType = securityType.getDefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName().trim());
            defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));

        } else {
            throw new DeploymentException("No default principal configured");
        }
        security.setDefaultPrincipal(defaultPrincipal);

        return security;
    }

    public static Principal buildPrincipal(GerPrincipalType principalType) {
        Principal principal = new Principal();

        principal.setClassName(principalType.getClass1());
        principal.setPrincipalName(principalType.getName());
        principal.setDesignatedRunAs(principalType.isSetDesignatedRunAs());

        return principal;
    }
}
