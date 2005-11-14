/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.core.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

public class PropertiesLoginModuleManager {

    private ServerInfo serverInfo;

    private LoginModuleGBean loginModule;

    private Properties users = new Properties();

    private Properties groups = new Properties();

    private static final String usersKey = "usersURI";

    private static final String groupsKey = "groupsURI";

    public PropertiesLoginModuleManager(ServerInfo serverInfo,
            LoginModuleGBean loginModule) {
        this.serverInfo = serverInfo;
        this.loginModule = loginModule;
    }

    private void refreshUsers() {
        users.clear();
        try {
            users.load(serverInfo.resolve(getUsersURI()).toURL().openStream());
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        }
    }

    private void refreshGroups() throws GeronimoSecurityException {
        groups.clear();
        try {
            groups
                    .load(serverInfo.resolve(getGroupsURI()).toURL()
                            .openStream());
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        }
    }

    public String[] getUsers() throws GeronimoSecurityException {
        users.clear();
        try {
            users.load(serverInfo.resolve(getUsersURI()).toURL().openStream());
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        }
        return (String[]) users.keySet().toArray(new String[0]);
    }

    public String[] getGroups() throws GeronimoSecurityException {
        groups.clear();
        try {
            groups
                    .load(serverInfo.resolve(getGroupsURI()).toURL()
                            .openStream());
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        }
        return (String[]) groups.keySet().toArray(new String[0]);
    }

    public void addUserPrincipal(Hashtable properties)
            throws GeronimoSecurityException {
        if (users.getProperty((String) properties.get("UserName")) != null) {
            throw new GeronimoSecurityException("User principal "
                    + (String) properties.get("UserName") + " already exists.");
        }
        try {
            refreshUsers();
            users.setProperty((String) properties.get("UserName"),
                    (String) properties.get("Password"));
            users.store(serverInfo.resolve(getUsersURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add user principal: "
                    + e.getMessage());
        }
    }

    public void removeUserPrincipal(String userPrincipal)
            throws GeronimoSecurityException {
        try {
            refreshUsers();
            users.remove(userPrincipal);
            users.store(serverInfo.resolve(getUsersURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot remove user principal "
                    + userPrincipal + ": " + e.getMessage());
        }
    }

    public void updateUserPrincipal(Hashtable properties)
            throws GeronimoSecurityException {
        //same as add pricipal overriding the property
        try {
            refreshUsers();
            users.setProperty((String) properties.get("UserName"),
                    (String) properties.get("Password"));
            users.store(serverInfo.resolve(getUsersURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add user principal: "
                    + e.getMessage());
        }
    }

    public void addGroupPrincipal(Hashtable properties)
            throws GeronimoSecurityException {
        refreshGroups();
        if (groups.getProperty((String) properties.get("GroupName")) != null) {
            throw new GeronimoSecurityException("Group "
                    + (String) properties.get("GroupName") + " already exists.");
        }
        try {
            groups.setProperty((String) properties.get("GroupName"),
                    (String) properties.get("Members"));
            groups.store(serverInfo.resolve(getGroupsURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add group principal: "
                    + e.getMessage());
        }
    }

    public void removeGroupPrincipal(String groupPrincipal)
            throws GeronimoSecurityException {
        refreshGroups();
        try {
            groups.remove(groupPrincipal);
            groups.store(serverInfo.resolve(getGroupsURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException(
                    "Cannot remove group principal: " + e.getMessage());
        }
    }

    public void updateGroupPrincipal(Hashtable properties)
            throws GeronimoSecurityException {
        //same as add group principal
        refreshGroups();
        try {
            groups.setProperty((String) properties.get("GroupName"),
                    (String) properties.get("Members"));
            groups.store(serverInfo.resolve(getGroupsURI()).toURL()
                    .openConnection().getOutputStream(), null);
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add group principal: "
                    + e.getMessage());
        }
    }

    public void addToGroup(String userPrincipal, String groupPrincipal)
            throws GeronimoSecurityException {
        throw new GeronimoSecurityException(
                "Not implemented for properties file security realm...");
    }

    public void removeFromGroup(String userPrincipal, String groupPrincipal)
            throws GeronimoSecurityException {
        throw new GeronimoSecurityException(
                "Not implemented for properties file security realm...");
    }

    public String getPassword(String userPrincipal)
            throws GeronimoSecurityException {
        refreshUsers();
        return users.getProperty(userPrincipal);
    }

    public Set getGroupMembers(String groupPrincipal)
            throws GeronimoSecurityException {
        Set memberSet = new HashSet();
        groups.clear();
        refreshGroups();
        if (groups.getProperty(groupPrincipal) == null) {
            return memberSet;
        }
        String[] members = ((String) groups.getProperty(groupPrincipal))
                .split(",");

        memberSet.addAll(Arrays.asList(members));
        return memberSet;
    }

    private String getUsersURI() {
        return loginModule.getOptions().getProperty(usersKey);
    }

    private String getGroupsURI() {
        return loginModule.getOptions().getProperty(groupsKey);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(
                "PropertiesLoginModuleManager",
                PropertiesLoginModuleManager.class);

        infoFactory.addOperation("addUserPrincipal",
                new Class[] { Hashtable.class });
        infoFactory.addOperation("removeUserPrincipal",
                new Class[] { String.class });
        infoFactory.addOperation("updateUserPrincipal",
                new Class[] { Hashtable.class });
        infoFactory.addOperation("getGroups");
        infoFactory.addOperation("getUsers");

        infoFactory.addOperation("updateUserPrincipal",
                new Class[] { Hashtable.class });

        infoFactory.addOperation("getPassword", new Class[] { String.class });
        infoFactory.addOperation("getGroupMembers",
                new Class[] { String.class });
        infoFactory.addOperation("addGroupPrincipal",
                new Class[] { Hashtable.class });
        infoFactory.addOperation("removeGroupPrincipal",
                new Class[] { String.class });
        infoFactory.addOperation("updateGroupPrincipal",
                new Class[] { Hashtable.class });
        infoFactory.addOperation("addToGroup", new Class[] { String.class,
                String.class });
        infoFactory.addOperation("removeFromGroup", new Class[] { String.class,
                String.class });

        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("LoginModule", LoginModuleGBean.class, NameFactory.LOGIN_MODULE);

        infoFactory
                .setConstructor(new String[] { "ServerInfo", "LoginModule" });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
