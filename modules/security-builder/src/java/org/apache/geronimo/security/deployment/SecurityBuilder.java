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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.DistinguishedName;
import org.apache.geronimo.security.deploy.LoginDomainPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.RealmPrincipal;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.xbeans.geronimo.security.GerDefaultPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerDistinguishedNameType;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerNamedUsernamePasswordCredentialType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;


/**
 * @version $Rev:  $ $Date:  $
 */
public class SecurityBuilder {

    public static SecurityConfiguration buildSecurityConfiguration(GerSecurityType securityType) {
        Security security = buildSecurityConfig(securityType);
        return buildSecurityConfiguration(security);
    }

    public static SecurityConfiguration buildSecurityConfiguration(Security security) {
        Map roleDesignates = new HashMap();
        Map principalRoleMap = new HashMap();
        Map roleToPrincipalMap = new HashMap();
        buildRolePrincipalMap(security, roleDesignates, roleToPrincipalMap);
        invertMap(roleToPrincipalMap, principalRoleMap);
        return new SecurityConfiguration(principalRoleMap, roleDesignates, security.getDefaultPrincipal(), security.getDefaultRole(), security.isDoAsCurrentCaller(), security.isUseContextHandler());
    }

    private static Map invertMap(Map roleToPrincipalMap, Map principalRoleMapping) {
        for (Iterator roles = roleToPrincipalMap.entrySet().iterator(); roles.hasNext();) {
            Map.Entry entry = (Map.Entry) roles.next();
            String role = (String) entry.getKey();
            Set principals = (Set) entry.getValue();
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                java.security.Principal principal = (java.security.Principal) iter.next();

                HashSet roleSet = (HashSet) principalRoleMapping.get(principal);
                if (roleSet == null) {
                    roleSet = new HashSet();
                    principalRoleMapping.put(principal, roleSet);
                }
                roleSet.add(role);
            }
        }
        return principalRoleMapping;
    }

    public static void buildRolePrincipalMap(Security security, Map roleDesignates, Map roleToPrincipalMap) {

        Iterator roleMappings = security.getRoleMappings().values().iterator();
        while (roleMappings.hasNext()) {
            Role role = (Role) roleMappings.next();

            String roleName = role.getRoleName();
            Subject roleDesignate = new Subject();
            Set principalSet = new HashSet();

            Iterator realmPrincipals = role.getRealmPrincipals().iterator();
            while (realmPrincipals.hasNext()) {
                RealmPrincipal realmPrincipal = (RealmPrincipal) realmPrincipals.next();
                java.security.Principal principal = ConfigurationUtil.generateRealmPrincipal(realmPrincipal.getRealm(), realmPrincipal.getDomain(), realmPrincipal);

                principalSet.add(principal);
                if (realmPrincipal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            Iterator domainPrincipals = role.getLoginDomainPrincipals().iterator();
            while (domainPrincipals.hasNext()) {
                LoginDomainPrincipal domainPrincipal = (LoginDomainPrincipal) domainPrincipals.next();
                java.security.Principal principal = ConfigurationUtil.generateDomainPrincipal(domainPrincipal.getDomain(), domainPrincipal);

                principalSet.add(principal);
                if (domainPrincipal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            Iterator principals = role.getPrincipals().iterator();
            while (principals.hasNext()) {
                Principal plainPrincipal = (Principal) principals.next();
                java.security.Principal principal = ConfigurationUtil.generatePrincipal(plainPrincipal);

                principalSet.add(principal);
                if (plainPrincipal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            for (Iterator names = role.getDistinguishedNames().iterator(); names.hasNext();) {
                DistinguishedName dn = (DistinguishedName) names.next();

                X500Principal x500Principal = ConfigurationUtil.generateX500Principal(dn.getName());

                principalSet.add(x500Principal);
                if (dn.isDesignatedRunAs()) roleDesignate.getPrincipals().add(x500Principal);
            }

            Set roleMapping = (Set) roleToPrincipalMap.get(roleName);
            if (roleMapping == null) {
                roleMapping = new HashSet();
                roleToPrincipalMap.put(roleName, roleMapping);
            }
            roleMapping.addAll(principalSet);

            if (roleDesignate.getPrincipals().size() > 0) {
                roleDesignates.put(roleName, roleDesignate);
            }
        }
    }

    private static Security buildSecurityConfig(GerSecurityType securityType) {
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

        if (securityType.isSetRoleMappings()) {
            GerRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                GerRoleType roleType = roleMappingsType.getRoleArray(i);
                Role role = new Role();

                String roleName = roleType.getRoleName().trim();
                role.setRoleName(roleName);

                for (int j = 0; j < roleType.sizeOfRealmPrincipalArray(); j++) {
                    role.getRealmPrincipals().add(buildRealmPrincipal(roleType.getRealmPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfLoginDomainPrincipalArray(); j++) {
                    role.getLoginDomainPrincipals().add(buildDomainPrincipal(roleType.getLoginDomainPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfPrincipalArray(); j++) {
                    role.getPrincipals().add(buildPrincipal(roleType.getPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfDistinguishedNameArray(); j++) {
                    GerDistinguishedNameType dnType = roleType.getDistinguishedNameArray(j);

                    role.getDistinguishedNames().add(new DistinguishedName(dnType.getName(), dnType.getDesignatedRunAs()));
                }

                security.getRoleMappings().put(roleName, role);
            }
        }

        security.setDefaultPrincipal(buildDefaultPrincipal(securityType.getDefaultPrincipal()));

        return security;
    }

    //used from app client builder
    public static DefaultPrincipal buildDefaultPrincipal(GerDefaultPrincipalType defaultPrincipalType) {
        DefaultPrincipal defaultPrincipal = new DefaultPrincipal();

        defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));
        GerNamedUsernamePasswordCredentialType[] namedCredentials = defaultPrincipalType.getNamedUsernamePasswordCredentialArray();
        if (namedCredentials.length > 0) {
            Set defaultCredentialSet = new HashSet();
            for (int i = 0; i < namedCredentials.length; i++) {
                GerNamedUsernamePasswordCredentialType namedCredentialType = namedCredentials[i];
                NamedUsernamePasswordCredential namedCredential = new NamedUsernamePasswordCredential(namedCredentialType.getUsername(), namedCredentialType.getPassword().toCharArray(), namedCredentialType.getName());
                defaultCredentialSet.add(namedCredential);
            }
            defaultPrincipal.setNamedUserPasswordCredentials(defaultCredentialSet);
        }
        return defaultPrincipal;
    }

    //used from TSSConfigEditor
    public static RealmPrincipal buildRealmPrincipal(GerRealmPrincipalType realmPrincipalType) {
        return new RealmPrincipal(realmPrincipalType.getDomainName(), realmPrincipalType.getRealmName(), realmPrincipalType.getClass1(), realmPrincipalType.getName(), realmPrincipalType.isSetDesignatedRunAs());
    }

    public static LoginDomainPrincipal buildDomainPrincipal(GerLoginDomainPrincipalType domainPrincipalType) {
        return new LoginDomainPrincipal(domainPrincipalType.getDomainName(), domainPrincipalType.getClass1(), domainPrincipalType.getName(), domainPrincipalType.isSetDesignatedRunAs());
    }

    public static Principal buildPrincipal(GerPrincipalType principalType) {
        return new Principal(principalType.getClass1(), principalType.getName(), principalType.isSetDesignatedRunAs());
    }

    public static GBeanData configureApplicationPolicyManager(ObjectName name, Map contextIDToPermissionsMap, SecurityConfiguration securityConfiguration) {
        GBeanData jaccBeanData = new GBeanData(name, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("principalRoleMap", securityConfiguration.getPrincipalRoleMap());
        jaccBeanData.setAttribute("roleDesignates", securityConfiguration.getRoleDesignates());
        return jaccBeanData;
    }

}
