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
package org.apache.geronimo.security.realm.providers;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * ConstantLoginModule allows a single user and multiple group
 * principals to be added to an authenticated Subject via
 * configuration during application deployment. ConstantLoginModule
 * would normally be used along with a more conventional LoginModule. A
 * potential use case for ConstantLoginModule is a situation where you
 * want to associate a single user (or group) to an authenticated user,
 * but the authentication mechanism does not contain such a group.
 * For example, ConstantLoginModule could allow an "Authenticated" 
 * user principal to be added to the Subject.
 * <p>
 * To configure, add the following to the <login-config> of your geronimo deployment plan:
 * <code>
 *   <log:login-module control-flag="REQUIRED" wrap-principals="false">
 *       <log:login-domain-name>Constant</log:login-domain-name>
 *       <log:login-module-class>org.apache.geronimo.security.realm.providers.ConstantLoginModule</log:login-module-class>
 *       <log:option name="userName">authenticated</log:option>
 *       <log:option name="groupNames">group1,group2</log:option>
 *   </log:login-module>
 * </code>
 */
public class ConstantLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler handler;
    
    private String userName;
    private String groupNames;
    
    private static final String USER_NAME = "userName";
    private static final String GROUP_NAMES = "groupNames";
    
    public boolean abort() throws LoginException {
        return true;
    }

    /**
     * Add the user and group principals to the Subject. Group names are separated
     * by ',' characters.
     */
    public boolean commit() throws LoginException {
        if(userName != null) {
            subject.getPrincipals().add(new GeronimoUserPrincipal(userName));
        }
        
        if(groupNames != null) {
            for (String groupName : groupNames.split(",")) {
                subject.getPrincipals().add(new GeronimoGroupPrincipal(groupName));
            }
        }
        
        return true;
    }

    /**
     * Save the userName and groupNames settings for use during commit()
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {

        this.subject = subject;
        this.handler = callbackHandler;
        
        this.userName = (String)options.get(USER_NAME);
        this.groupNames = (String)options.get(GROUP_NAMES);
    }

    public boolean login() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }

}
