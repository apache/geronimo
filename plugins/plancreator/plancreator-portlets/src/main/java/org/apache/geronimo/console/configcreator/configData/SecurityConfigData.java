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
package org.apache.geronimo.console.configcreator.configData;

import java.util.HashSet;
import java.util.Map;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.geronimo.deployment.xbeans.PatternType;

import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.SecurityRole;

import org.apache.geronimo.xbeans.geronimo.security.GerDistinguishedNameType;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;

/**
 * 
 * @version $Rev$ $Date$
 */
public class SecurityConfigData {
    private GerSecurityType security = null;

    private HashSet<String> dependenciesSet = new HashSet<String>();

    public void parseWebDD(JndiConsumer annotatedWebAppDD) {
        if (annotatedWebAppDD instanceof WebApp) {
            WebApp webAppDD = (WebApp) annotatedWebAppDD;
            List<SecurityRole> securityRoles = webAppDD.getSecurityRole();
            if (securityRoles.size() > 0) {
                security = GerSecurityType.Factory.newInstance();
                GerRoleMappingsType roleMappings = security.addNewRoleMappings();
                for (SecurityRole securityRole: securityRoles) {
                    String roleName = securityRole.getRoleName();
                    roleMappings.addNewRole().setRoleName(roleName);
                }
            }
        }
    }

    public void readSecurityData(PortletRequest request) {
        dependenciesSet.clear();
        Map map = request.getParameterMap();
        boolean processAdvancedSettings = false;
        if (map.containsKey("security.advancedSettings.isPresent")
                && "true".equalsIgnoreCase(request.getParameter("security.advancedSettings.isPresent"))) {
            processAdvancedSettings = true;
        }
        GerRoleType[] roles = security.getRoleMappings().getRoleArray();
        for (int index = 0; index < roles.length; index++) {
            String prefix1 = "security.roleMappings" + "." + index + ".";
            GerRoleType role = roles[index];

            for (int i = role.sizeOfPrincipalArray() - 1; i >= 0; i--) {
                role.removePrincipal(i);
            }
            int lastIndex = Integer.parseInt(request.getParameter(prefix1 + "principal.lastIndex"));
            for (int i = 0; i < lastIndex; i++) {
                String prefix2 = prefix1 + "principal" + "." + i + ".";
                if (!map.containsKey(prefix2 + "name")) {
                    continue;
                }
                GerPrincipalType principal = role.addNewPrincipal();
                principal.setName(request.getParameter(prefix2 + "name"));
                principal.setClass1(request.getParameter(prefix2 + "class"));
            }

            for (int i = role.sizeOfLoginDomainPrincipalArray() - 1; i >= 0; i--) {
                role.removeLoginDomainPrincipal(i);
            }
            lastIndex = Integer.parseInt(request.getParameter(prefix1 + "loginDomainPrincipal.lastIndex"));
            for (int i = 0; i < lastIndex; i++) {
                String prefix2 = prefix1 + "loginDomainPrincipal" + "." + i + ".";
                if (!map.containsKey(prefix2 + "name")) {
                    continue;
                }
                GerLoginDomainPrincipalType loginDomainPrincipal = role.addNewLoginDomainPrincipal();
                loginDomainPrincipal.setName(request.getParameter(prefix2 + "name"));
                loginDomainPrincipal.setClass1(request.getParameter(prefix2 + "class"));
                loginDomainPrincipal.setDomainName(request.getParameter(prefix2 + "domainName"));
            }

            for (int i = role.sizeOfRealmPrincipalArray() - 1; i >= 0; i--) {
                role.removeRealmPrincipal(i);
            }
            lastIndex = Integer.parseInt(request.getParameter(prefix1 + "realmPrincipal.lastIndex"));
            for (int i = 0; i < lastIndex; i++) {
                String prefix2 = prefix1 + "realmPrincipal" + "." + i + ".";
                if (!map.containsKey(prefix2 + "name")) {
                    continue;
                }
                GerRealmPrincipalType realmPrincipal = role.addNewRealmPrincipal();
                realmPrincipal.setName(request.getParameter(prefix2 + "name"));
                realmPrincipal.setClass1(request.getParameter(prefix2 + "class"));
                realmPrincipal.setDomainName(request.getParameter(prefix2 + "domainName"));
                realmPrincipal.setRealmName(request.getParameter(prefix2 + "realmName"));
            }

            for (int i = role.sizeOfDistinguishedNameArray() - 1; i >= 0; i--) {
                role.removeDistinguishedName(i);
            }
            lastIndex = Integer.parseInt(request.getParameter(prefix1 + "distinguishedName.lastIndex"));
            for (int i = 0; i < lastIndex; i++) {
                String prefix2 = prefix1 + "distinguishedName" + "." + i + ".";
                if (!map.containsKey(prefix2 + "name")) {
                    continue;
                }
                GerDistinguishedNameType distinguishedName = role.addNewDistinguishedName();
                distinguishedName.setName(request.getParameter(prefix2 + "name"));
            }

            if (processAdvancedSettings) {
                String prefix2 = prefix1 + "runAsSubject" + ".";
                if (map.containsKey(prefix2 + "realm")) {
                    if (role.isSetRunAsSubject()) {
                        role.unsetRunAsSubject();
                    }
                    String realm = request.getParameter(prefix2 + "realm");
                    String id = request.getParameter(prefix2 + "id");
                    if (!isEmpty(realm) && !isEmpty(id)) {
                        GerSubjectInfoType runAsSubject = role.addNewRunAsSubject();
                        runAsSubject.setRealm(realm);
                        runAsSubject.setId(id);
                    }
                }
            }
        }
        if (processAdvancedSettings) {
            String parameterName = "security" + "." + "credentialStoreRef";
            if (map.containsKey(parameterName)) {
                String patternString = request.getParameter(parameterName);
                String[] elements = patternString.split("/", 6);
                PatternType pattern = PatternType.Factory.newInstance();
                pattern.setGroupId(elements[0]);
                pattern.setArtifactId(elements[1]);
                // pattern.setVersion(elements[2]);
                // pattern.setType(elements[3]);
                // pattern.setModule(elements[4]);
                pattern.setName(elements[5]);
                security.setCredentialStoreRef(pattern);
                dependenciesSet.add(JndiRefsConfigData.getDependencyString(patternString));
            }
            String prefix = "security" + "." + "defaultSubject" + ".";
            if (map.containsKey(prefix + "realm")) {
                if (security.isSetDefaultSubject()) {
                    security.unsetDefaultSubject();
                }
                String realm = request.getParameter(prefix + "realm");
                String id = request.getParameter(prefix + "id");
                if (!isEmpty(realm) && !isEmpty(id)) {
                    GerSubjectInfoType runAsSubject = security.addNewDefaultSubject();
                    runAsSubject.setRealm(realm);
                    runAsSubject.setId(id);
                }
            }
            parameterName = "security" + "." + "doasCurrentCaller";
            if ("true".equalsIgnoreCase(request.getParameter(parameterName))) {
                security.setDoasCurrentCaller(true);
            }
            parameterName = "security" + "." + "useContextHandler";
            if ("true".equalsIgnoreCase(request.getParameter(parameterName))) {
                security.setUseContextHandler(true);
            }
            String defaultRole = request.getParameter("security" + "." + "defaultRole");
            if (!isEmpty(defaultRole)) {
                security.setDefaultRole(defaultRole);
            }
        }
    }

    public GerSecurityType getSecurity() {
        return security;
    }

    public HashSet<String> getDependenciesSet() {
        return dependenciesSet;
    }

    protected static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
