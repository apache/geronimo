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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.SecurityService;


/**
 * @version $Rev$ $Date$
 */
public class Security implements Serializable {

    private boolean doAsCurrentCaller;
    private boolean useContextHandler;
    private String defaultRole;
    private DefaultPrincipal defaultPrincipal;
    private Map roleMappings = new HashMap();
    private Set roleNames = new HashSet();
    private AutoMapAssistant assistant;

    public Security() {
    }

    public boolean isDoAsCurrentCaller() {
        return doAsCurrentCaller;
    }

    public void setDoAsCurrentCaller(boolean doAsCurrentCaller) {
        this.doAsCurrentCaller = doAsCurrentCaller;
    }

    public boolean isUseContextHandler() {
        return useContextHandler;
    }

    public void setUseContextHandler(boolean useContextHandler) {
        this.useContextHandler = useContextHandler;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public DefaultPrincipal getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        this.defaultPrincipal = defaultPrincipal;
    }

    public Map getRoleMappings() {
        return roleMappings;
    }

    public Set getRoleNames() {
        return roleNames;
    }

    public AutoMapAssistant getAssistant() {
        return assistant;
    }

    public void setAssistant(AutoMapAssistant assistant) {
        this.assistant = assistant;
    }

    public void append(Role role) {
        if (roleMappings.containsKey(role.getRoleName())) {
            Role existing = (Role) roleMappings.get(role.getRoleName());
            for (Iterator iter = role.getRealms().keySet().iterator(); iter.hasNext();) {
                existing.append((Realm) role.getRealms().get(iter.next()));
            }
        } else {
            roleMappings.put(role.getRoleName(), role);
        }
    }

    /**
     * Automatically generate role mappings and add them to the existing role mappings.
     * <p/>
     * NOTE: This method should be called during deployment.
     *
     * @param securityService used to obtain the configured auto map assistant.
     */
    public void autoGenerate(SecurityService securityService) {
        if (securityService == null) return;
        if (assistant == null) return;

        String realmName = assistant.getSecurityRealm();
        org.apache.geronimo.security.realm.AutoMapAssistant autoMapAssistant = securityService.getMapper(realmName);
        if (autoMapAssistant == null) return;

        /**
         * Append roles
         */
        for (Iterator iter = roleNames.iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            Role role = new Role();

            role.setRoleName(roleName);

            Realm realm = new Realm();

            realm.setRealmName(assistant.getSecurityRealm());

            //todo: the usage of the realm name in the next call instead of the login domain name is an error!
            for (Iterator principalClasses = autoMapAssistant.obtainRolePrincipalClasses(realmName).iterator(); principalClasses.hasNext();) {
                Principal principal = new Principal();
                //todo: Principal class needs to handle login domain as well
                principal.setClassName((String) principalClasses.next());
                principal.setPrincipalName(roleName);
                principal.setDesignatedRunAs(false);

                realm.getPrincipals().add(principal);
            }
            role.append(realm);

            append(role);
        }

        /**
         * Add default principal
         */
        if (defaultPrincipal != null) return;

        defaultPrincipal = new DefaultPrincipal();

        defaultPrincipal.setPrincipal(autoMapAssistant.obtainDefaultPrincipal());
        defaultPrincipal.setRealmName(realmName);
    }
}
