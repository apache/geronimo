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

import java.util.Hashtable;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.console.util.ObjectNameConstants;

public class SERealmUserHelper extends RealmHelper {

    private static final String GET_USERS_FUNCTION = "getUsers";

    private static final String ADD_USER_FUNCTION = "addUserPrincipal";

    private static final String USER_EXISTS_FUNCTION = "userExists";

    private static final String UPDATE_USER_FUNCTION = "updateUserPrincipal";

    private static final String DELETE_USER_FUNCTION = "removeUserPrincipal";

    private static final String GET_PASSWORD_FUNCTION = "getPassword";

    private static final String[] STRING = { "java.lang.String" };

    private static final String[] HASHTABLE = { "java.util.Hashtable" };

    private static final Kernel kernel = KernelRegistry.getSingleKernel();

    public static String[] getUsers() throws Exception {
        return (String[]) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, GET_USERS_FUNCTION);
    }

    private static void refresh() {
        try {

            kernel.stopGBean(ObjectNameConstants.SE_REALM_MBEAN_NAME);
            kernel.startGBean(ObjectNameConstants.SE_REALM_MBEAN_NAME);
//            kernel.stopGBean(ObjectNameConstants.SE_REALM_IMMUTABLE_MBEAN_NAME);
//            kernel.startGBean(ObjectNameConstants.SE_REALM_IMMUTABLE_MBEAN_NAME);

        } catch (Exception e) {
        }
    }

    public static String getPassword(String username) throws Exception {
        Object ret;
        String[] arg = {username};
        ret = invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, GET_PASSWORD_FUNCTION, arg, STRING);
        return (ret != null) ? ret.toString() : "";
    }

    public static boolean userExists(String username) throws Exception {
        Boolean ret;
        String[] arg = {username};
        ret = (Boolean) invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, USER_EXISTS_FUNCTION, arg, STRING);
        return ret.booleanValue();
    }

    public static void addUser(String username, String password)
            throws Exception {
        Hashtable props = new Hashtable();
        props.put("UserName", username);
        props.put("Password", password);
        Object[] args = {props};
        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, ADD_USER_FUNCTION, args, HASHTABLE);
        refresh();
    }

    public static void updateUser(String username, String password)
            throws Exception {
        Hashtable props = new Hashtable();
        props.put("UserName", username);
        props.put("Password", password);
        Object[] args = {props};
        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, UPDATE_USER_FUNCTION, args, HASHTABLE);
        refresh();
    }

    public static void deleteUser(String username) throws Exception {
        String[] args = {username};
        invoke(ObjectNameConstants.SE_REALM_MBEAN_NAME, DELETE_USER_FUNCTION, args, STRING);
        refresh();
    }


}