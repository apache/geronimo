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

package org.apache.geronimo.console.securitymanager.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.console.util.StringUtils;

public class SERealmGroupHelper extends RealmHelper {

    private static final String GET_GROUPS_FUNCTION = "getGroups";

    private static final String ADD_GROUP_FUNCTION = "addGroupPrincipal";

    private static final String GROUP_EXISTS_FUNCTION = "groupExists";

    private static final String UPDATE_GROUP_FUNCTION = "updateGroupPrincipal";

    private static final String DELETE_GROUP_FUNCTION = "removeGroupPrincipal";

    private static final String GET_USERS_FUNCTION = "getGroupMembers";
    
    private static final String IS_AVAILABLE = "isAvailable";

    private static final String[] STRING = { "java.lang.String" };

    private static final String[] HASHTABLE = { "java.util.Hashtable" };

    private static final Kernel kernel = KernelRegistry.getSingleKernel();

    public static String[] getGroups() throws Exception {
        String[] groups = (String[]) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, GET_GROUPS_FUNCTION);
        return groups;
    }

    private static void refresh() {
        try {

            kernel.stopGBean(ObjectNameConstants.SE_REALM_MBEAN_NAME);
            kernel.startGBean(ObjectNameConstants.SE_REALM_MBEAN_NAME);
        } catch (Exception e) {
        }
    }

    public static void addGroup(String groupName, String[] userList)
            throws Exception {
        addGroup(groupName, StringUtils.convertToCommaDelimited(userList));
        refresh();
    }

    public static void updateGroup(String groupName, String[] userList)
            throws Exception {
        updateGroup(groupName, StringUtils.convertToCommaDelimited(userList));
        refresh();
    }

    public static boolean groupExists(String username) throws Exception {
        Boolean ret;
        String[] arg = {username};
        ret = (Boolean) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, GROUP_EXISTS_FUNCTION, arg, STRING);
        return ret.booleanValue();
    }

    public static void addGroup(String groupName, String userList)
            throws Exception {

        Hashtable props = new Hashtable();
        props.put("GroupName", groupName);
        props.put("Members", userList);
        Object[] args = {props};
        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, ADD_GROUP_FUNCTION, args, HASHTABLE);
    }

    public static void updateGroup(String groupName, String userList)
            throws Exception {
        Hashtable props = new Hashtable();
        props.put("GroupName", groupName);
        props.put("Members", userList);
        Object[] args = {props};

        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, UPDATE_GROUP_FUNCTION, args, HASHTABLE);
    }

    public static void deleteGroup(String groupName) throws Exception {
        String[] args = {groupName};
        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, DELETE_GROUP_FUNCTION, args, STRING);
        refresh();
    }

    public static Set getUsers(String groupName) throws Exception {
        Set ret = null;
        String[] arg = {groupName};
        ret = (Set) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, GET_USERS_FUNCTION, arg, STRING);
        return ret;
    }

    public static boolean isGroupMember(String groupName, String username)
            throws Exception {
        Collection users = getUsersAsCollection(groupName);
        return (users.contains(username));
    }
    
    private static Collection getUsersAsCollection(String groupName)
            throws Exception {
        return getUsers(groupName);
    }
    
    public static boolean isAvailable() throws Exception {
        return (Boolean) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, IS_AVAILABLE, new Object[0], new String[0]);
    }

}