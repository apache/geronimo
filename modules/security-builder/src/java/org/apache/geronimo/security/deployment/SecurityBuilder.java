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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.xbeans.geronimo.security.GerAutoMapRolesType;
import org.apache.geronimo.xbeans.geronimo.security.GerClassOverrideType;
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

    public static Security buildSecurityConfig(Set loginDomainNames, GerSecurityType securityType, Set roleNames, Map localSecurityRealms, Kernel kernel) throws MalformedObjectNameException, DeploymentException {
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

        GerAutoMapRolesType autoMapRolesType = securityType.getAutoMapRoles();
        String autoMapRealmName = null;
        Set autoMapClassOverrides = null;
        if (autoMapRolesType != null) {

            autoMapRealmName = autoMapRolesType.getSecurityRealm().trim();

            GerClassOverrideType[] classOverrideArray = autoMapRolesType.getClassOverrideArray();
            if (classOverrideArray.length > 0) {
                autoMapClassOverrides = new HashSet();
            }
            for (int i = 0; i < classOverrideArray.length; i++) {
                autoMapClassOverrides.add(classOverrideArray[i].getClass1().trim());
            }

        }

        security.getRoleNames().addAll(roleNames);

        DefaultPrincipal defaultPrincipal = new DefaultPrincipal();
        if (securityType.isSetDefaultPrincipal()) {
            GerDefaultPrincipalType defaultPrincipalType = securityType.getDefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName().trim());
            defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));

        } else {
            if (autoMapRealmName == null) {
                throw new DeploymentException("No default principal configured, and no automap realm specific for default principal source");
            }
            Principal principal;
            GBeanData realmData = (GBeanData) localSecurityRealms.get(autoMapRealmName);
            if (realmData != null) {
                principal = (Principal) realmData.getAttribute("defaultPrincipal");
            } else {
                ObjectName realmObjectName = NameFactory.getSecurityRealmName(autoMapRealmName);

                try {
                    principal = (Principal) kernel.getAttribute(realmObjectName, "defaultPrincipal");
                } catch (GBeanNotFoundException e) {
                    throw new DeploymentException("No realm with supplied name: " + autoMapRealmName, e);
                } catch (NoSuchAttributeException e) {
                    throw new DeploymentException("Realm " + autoMapRealmName + " is not able to supply default principal", e);
                } catch (Exception e) {
                    throw new DeploymentException("Could not retrieve attribute autoMapPrincipalClasses from realm with supplied name: " + autoMapRealmName, e);
                }
            }
            defaultPrincipal = new DefaultPrincipal();
            defaultPrincipal.setPrincipal(principal);
            defaultPrincipal.setRealmName(autoMapRealmName);

        }
        security.setDefaultPrincipal(defaultPrincipal);

        for (Iterator realmNames = allRealms.iterator(); realmNames.hasNext();) {
            String realmName = (String) realmNames.next();

            Map autoMapPrincipalClassesMap;
            GBeanData realmData = (GBeanData) localSecurityRealms.get(realmName);
            if (realmData != null) {
                autoMapPrincipalClassesMap = (Map) realmData.getAttribute("autoMapPrincipalClasses");
            } else {
                ObjectName realmObjectName = NameFactory.getSecurityRealmName(realmName);
                try {
                    autoMapPrincipalClassesMap = (Map) kernel.getAttribute(realmObjectName, "autoMapPrincipalClasses");

                } catch (GBeanNotFoundException e) {
                    throw new DeploymentException("No realm with supplied name: " + realmName, e);
                } catch (NoSuchAttributeException e) {
                    //its not an automapper
                    break;
                } catch (Exception e) {
                    throw new DeploymentException("Could not retrieve attribute autoMapPrincipalClasses from realm with supplied name: " + realmName, e);
                }
            }
            for (Iterator iterator = loginDomainNames.iterator(); iterator.hasNext();) {
                String loginDomainName = (String) iterator.next();
                Set autoMapPrincipalClasses;
                if (realmName.equals(autoMapRealmName)) {
                    autoMapPrincipalClasses = autoMapClassOverrides;
                }
                autoMapPrincipalClasses = (Set) autoMapPrincipalClassesMap.get(loginDomainName);

                security.autoGenerate(loginDomainName, realmName, autoMapPrincipalClasses);
            }
        }
        return security;
    }

    private static Principal buildPrincipal(GerPrincipalType principalType) {
        Principal principal = new Principal();

        principal.setClassName(principalType.getClass1());
        principal.setPrincipalName(principalType.getName());
        principal.setDesignatedRunAs(principalType.isSetDesignatedRunAs());

        return principal;
    }
}
