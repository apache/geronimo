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
package org.apache.geronimo.jetty.interceptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 * @version $Rev:  $ $Date:  $
 */
public class SecurityContextBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int policyContextIDIndex;
    private final int webAppContextIndex;
    private final String policyContextID;
    private final static ThreadLocal currentWebAppContext = new ThreadLocal();
    private final Map roleDesignates = new HashMap();

    public SecurityContextBeforeAfter(BeforeAfter next, int policyContextIDIndex, int webAppContextIndex, String policyContextID) {
        this.next = next;
        this.policyContextIDIndex = policyContextIDIndex;
        this.webAppContextIndex = webAppContextIndex;
        this.policyContextID = policyContextID;
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        context[policyContextIDIndex] = PolicyContext.getContextID();
        context[webAppContextIndex] = getCurrentSecurityInterceptor();

        PolicyContext.setContextID(policyContextID);
        setCurrentSecurityInterceptor(this);

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        setCurrentSecurityInterceptor((SecurityContextBeforeAfter) context[webAppContextIndex]);
        PolicyContext.setContextID((String) context[policyContextIDIndex]);
    }

    private static void setCurrentSecurityInterceptor(SecurityContextBeforeAfter context) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.SET_CONTEXT);

        currentWebAppContext.set(context);
    }

    private static SecurityContextBeforeAfter getCurrentSecurityInterceptor() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.GET_CONTEXT);

        return (SecurityContextBeforeAfter) currentWebAppContext.get();
    }

    public static Subject getCurrentRoleDesignate(String role) {
        return getCurrentSecurityInterceptor().getRoleDesignate(role);
    }

    private Subject getRoleDesignate(String roleName) {
        return (Subject) roleDesignates.get(roleName);
    }

    private void setRoleDesignate(String roleName, Subject subject) {
        roleDesignates.put(roleName, subject);
    }

    public void addRoleMappings(Set securityRoles, String loginDomainName, Security security, RoleMappingConfiguration roleMapper) throws PolicyContextException, GeronimoSecurityException {

        for (Iterator roleMappings = security.getRoleMappings().values().iterator(); roleMappings.hasNext();) {
            Role role = (Role) roleMappings.next();
            String roleName = role.getRoleName();
            Set principalSet = new HashSet();

            if (!securityRoles.contains(roleName)) {
                throw new GeronimoSecurityException("Role does not exist in this configuration");
            }

            Subject roleDesignate = new Subject();

            for (Iterator realms = role.getRealms().values().iterator(); realms.hasNext();) {
                Realm realm = (Realm) realms.next();

                for (Iterator principals = realm.getPrincipals().iterator(); principals.hasNext();) {
                    org.apache.geronimo.security.deploy.Principal principal = (org.apache.geronimo.security.deploy.Principal) principals.next();

                    RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal, loginDomainName, realm.getRealmName());
                    if (realmPrincipal == null) {
                        throw new GeronimoSecurityException("Unable to create realm principal");
                    }

                    principalSet.add(realmPrincipal);
                    if (principal.isDesignatedRunAs()) {
                        roleDesignate.getPrincipals().add(realmPrincipal);
                    }
                }
            }
            roleMapper.addRoleMapping(roleName, principalSet);

            if (roleDesignate.getPrincipals().size() > 0) {
                setRoleDesignate(roleName, roleDesignate);
            }
        }

        /**
         * Register the role designates with the context manager.
         *
         * THIS MUST BE RUN AFTER JettyXMLConfiguration.configure()
         */
        for (Iterator iter = roleDesignates.keySet().iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.registerSubject(roleDesignate);
            SubjectId id = ContextManager.getSubjectId(roleDesignate);
            roleDesignate.getPrincipals().add(new IdentificationPrincipal(id));

//            log.debug("Role designate " + id + " for role '" + roleName + "' for JACC policy '" + policyContextID + "' registered.");
        }

    }

    public void stop() {
        for (Iterator iter = roleDesignates.keySet().iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.unregisterSubject(roleDesignate);
//            log.debug("Role designate " + ContextManager.getSubjectId(roleDesignate) + " for role '" + roleName + "' for JACC policy '" + policyContextID + "' unregistered.");
        }
    }
}
