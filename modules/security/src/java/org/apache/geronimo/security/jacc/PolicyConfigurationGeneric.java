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

package org.apache.geronimo.security.jacc;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.security.RealmPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class PolicyConfigurationGeneric implements GeronimoPolicyConfiguration {
    private final static int OPEN = 1;
    private final static int IN_SERVICE = 2;
    private final static int DELETED = 3;

    private final String contextID;
    private int state;
    private final HashMap rolePermissionsMap = new HashMap();
    private final HashMap principalRoleMapping = new HashMap();
    private Permissions unchecked = null;
    private Permissions excluded = null;

    private final HashMap principalPermissionsMap = new HashMap();

    PolicyConfigurationGeneric(String contextID) {
        this.contextID = contextID;
        this.state = OPEN;
    }

    public String getContextID() throws PolicyContextException {
        return contextID;
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {

        if (excluded != null && excluded.implies(permission)) return false;

        if (unchecked != null && unchecked.implies(permission)) return true;

        Principal[] principals = domain.getPrincipals();
        if (principals.length == 0) return false;

        for (int i = 0; i < principals.length; i++) {
            Principal principal = principals[i];
            if (!(principal instanceof RealmPrincipal)) continue;

            Permissions permissions = (Permissions) principalPermissionsMap.get(principal);

            if (permissions != null && permissions.implies(permission)) return true;
        }

        return false;
    }

    public void addRoleMapping(String role, Collection principals) throws PolicyContextException {
        Iterator iter = principals.iterator();
        while (iter.hasNext()) {
            Principal principal = (Principal) iter.next();

            if (!(principal instanceof RealmPrincipal)) throw new PolicyContextException("Principal not instance of RealmPrincipal");

            HashSet roles = (HashSet) principalRoleMapping.get(principal);
            if (roles == null) {
                roles = new HashSet();
                principalRoleMapping.put(principal, roles);
            }
            roles.add(role);
        }
    }

    public void addToRole(String roleName, PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToRole(roleName, (Permission) e.nextElement());
        }
    }

    public void addToRole(String roleName, Permission permission) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Permissions permissions = (Permissions) rolePermissionsMap.get(roleName);
        if (permissions == null) {
            permissions = new Permissions();
            rolePermissionsMap.put(roleName, permissions);
        }
        permissions.add(permission);
    }

    public void addToUncheckedPolicy(PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToUncheckedPolicy((Permission) e.nextElement());
        }
    }

    public void addToUncheckedPolicy(Permission permission) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        if (unchecked == null) unchecked = new Permissions();

        unchecked.add(permission);
    }

    public void addToExcludedPolicy(PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToExcludedPolicy((Permission) e.nextElement());
        }
    }

    public void addToExcludedPolicy(Permission permission) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        if (excluded == null) excluded = new Permissions();

        excluded.add(permission);
    }

    public void removeRole(String roleName) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        rolePermissionsMap.remove(roleName);
    }

    public void removeUncheckedPolicy() throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        unchecked = null;
    }

    public void removeExcludedPolicy() throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        excluded = null;
    }

    public void linkConfiguration(javax.security.jacc.PolicyConfiguration link) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        RoleMappingConfiguration configuration = (RoleMappingConfiguration) link;
        Iterator principals = principalRoleMapping.keySet().iterator();
        while (principals.hasNext()) {
            Principal principal = (Principal) principals.next();

            Iterator roles = ((HashSet) principalRoleMapping.get(principal)).iterator();
            while (roles.hasNext()) {
                configuration.addRoleMapping((String) roles.next(), Collections.singletonList(principal));
            }

        }
        configuration.linkConfiguration(this);
    }

    public void delete() throws PolicyContextException {
        state = DELETED;
    }

    public void commit() throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Iterator principals = principalRoleMapping.keySet().iterator();
        while (principals.hasNext()) {
            Principal principal = (Principal) principals.next();
            Permissions principalPermissions = (Permissions) principalPermissionsMap.get(principal);

            if (principalPermissions == null) {
                principalPermissions = new Permissions();
                principalPermissionsMap.put(principal, principalPermissions);
            }

            Iterator roles = ((HashSet) principalRoleMapping.get(principal)).iterator();
            while (roles.hasNext()) {
                Permissions permissions = (Permissions) rolePermissionsMap.get(roles.next());
                if (permissions == null) continue;
                Enumeration rolePermissions = permissions.elements();
                while (rolePermissions.hasMoreElements()) {
                    principalPermissions.add((Permission) rolePermissions.nextElement());
                }
            }

        }
        state = IN_SERVICE;
    }

    public boolean inService() throws PolicyContextException {
        return (state == IN_SERVICE);
    }

    //TODO I have no idea what side effects this might have, but it's needed in some form from GeronimoPolicyConfigurationFactory.
    //see JACC spec 1.0 section 3.1.1.1 discussion of in service and deleted.
    public void open(boolean remove) {
        if (remove) {
            rolePermissionsMap.clear();
            principalRoleMapping.clear();
            unchecked = null;
            excluded = null;
            principalPermissionsMap.clear();
        }
        state = OPEN;
    }
}
