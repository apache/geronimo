/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.security;

import java.util.HashMap;

import org.apache.geronimo.interop.properties.StringProperty;


public class Role {
    public static Role getInstance(String rolename) {
        Role role = (Role) _roleMap.get(rolename);
        if (role == null) {
            synchronized (_roleMap) {
                role = (Role) _roleMap.get(rolename);
                if (role == null) {
                    role = new Role();
                    role.init(rolename);
                    _roleMap.put(rolename, role);
                }
            }
        }
        return role;
    }

    public static Role getExistingInstance(String rolename) {
        return (Role) _roleMap.get(rolename);
    }

    // properties

    public static final StringProperty assignedRolesProperty =
            new StringProperty(Role.class, "assignedRoles")
            .displayName("Assigned Roles")
            .consoleHelp("Names of roles which have been explicitly assigned to this role.")
            .list()
            .sortOrder(1);

    public static final StringProperty excludedRolesProperty =
            new StringProperty(Role.class, "excludedRoles")
            .displayName("Excluded Roles")
            .consoleHelp("Names of roles which must be excluded from this role.")
            .list()
            .sortOrder(2);

    public static final StringProperty excludedUsersProperty =
            new StringProperty(Role.class, "excludedUsers")
            .displayName("Excluded Users")
            .consoleHelp("Names of users who must be excluded from this role.")
            .list()
            .sortOrder(3);

    public static final StringProperty inheritedRolesProperty =
            new StringProperty(Role.class, "inheritedRoles")
            .displayName("Inherited Roles")
            .consoleHelp("Names of roles which have been inherited from this role's assigned roles. This list is read only. It is derived from the assigned roles.")
            .list()
            .readOnly()
            .sortOrder(4);

    public static final StringProperty roleMembersProperty =
            new StringProperty(Role.class, "roleMembers")
            .displayName("Role Members")
            .consoleHelp("Names of users who have been assigned this role, or who have inherited it from an assigned role. This list is read only. To add a user to a role, please edit the user's properties")
            .list()
            .readOnly()
            .sortOrder(5);

    // public constants

    public static final String CLIENT = "[client]";

    public static final String SYSTEM = "[system]";

    // private data

    private static HashMap _roleMap = new HashMap();

    private String _name;

    // internal methods

    protected void init(String rolename) {
        _name = rolename;
    }

    // public methods

    public String getName() {
        return _name;
    }

    public String toString() {
        return super.toString() + ":" + _name;
    }
}
