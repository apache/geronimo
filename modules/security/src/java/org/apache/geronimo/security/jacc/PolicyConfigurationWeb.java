/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
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

import org.apache.geronimo.security.jacc.GeronimoPolicyConfiguration;
import org.apache.geronimo.security.RealmPrincipal;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 06:47:07 $
 */
public class PolicyConfigurationWeb implements GeronimoPolicyConfiguration {
    private final static int OPEN = 1;
    private final static int IN_SERVICE = 2;
    private final static int DELETED = 3;

    private final String contextID;
    private int state;
    private HashMap rolePermissionsMap = new HashMap();
    private HashMap principalRoleMapping = new HashMap();
    private Permissions unchecked = null;
    private Permissions excluded = null;

    private HashMap principalPermissionsMap = new HashMap();

    PolicyConfigurationWeb(String contextID) {
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

        Enumeration enum = permissions.elements();
        while (enum.hasMoreElements()) {
            addToRole(roleName, (Permission) enum.nextElement());
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

        Enumeration enum = permissions.elements();
        while (enum.hasMoreElements()) {
            addToUncheckedPolicy((Permission) enum.nextElement());
        }
    }

    public void addToUncheckedPolicy(Permission permission) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        if (unchecked == null) unchecked = new Permissions();

        unchecked.add(permission);
    }

    public void addToExcludedPolicy(PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) throw new UnsupportedOperationException("Not in an open state");

        Enumeration enum = permissions.elements();
        while (enum.hasMoreElements()) {
            addToUncheckedPolicy((Permission) enum.nextElement());
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
        if (state == DELETED) throw new UnsupportedOperationException("Not in an open state");

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
                Enumeration rolePermissions = ((Permissions) rolePermissionsMap.get(roles.next())).elements();
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
}
