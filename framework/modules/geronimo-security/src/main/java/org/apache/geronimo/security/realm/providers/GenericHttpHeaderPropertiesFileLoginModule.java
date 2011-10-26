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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;
import org.apache.geronimo.system.serverinfo.ServerInfo;

public class GenericHttpHeaderPropertiesFileLoginModule extends GenericHttpHeaderLoginmodule implements LoginModule {
    private final static String GROUPS_URI = "groupsURI";
    private final static String HEADER_NAMES = "headerNames";
    private final static String AUTHENTICATION_AUTHORITY = "authenticationAuthority";

    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(GROUPS_URI,
            HEADER_NAMES, AUTHENTICATION_AUTHORITY));
    private static Log log = LogFactory.getLog(PropertiesFileLoginModule.class);
    final Map<String, Set<String>> roleUsersMap = new HashMap<String, Set<String>>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        headerNames = (String) options.get(HEADER_NAMES);
        authenticationAuthority = (String) options.get(AUTHENTICATION_AUTHORITY);

        for (Object option : options.keySet()) {
            if (!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: " + option + ". Not supported.");
            }
        }
        try {
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            final String groups = (String) options.get(GROUPS_URI);

            if (groups == null) {
                throw new IllegalArgumentException(GROUPS_URI + " must be provided!");
            }
            URI groupsURI = new URI(groups);
            loadProperties(serverInfo, groupsURI);
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: " + e.getMessage(), e);
        }
    }

    public void loadProperties(ServerInfo serverInfo, URI groupURI) throws GeronimoSecurityException {
        try {
            URI groupFile = serverInfo.resolveServer(groupURI);
            Properties temp = new Properties();
            InputStream stream = groupFile.toURL().openStream();
            temp.load(stream);
            stream.close();

            Enumeration e = temp.keys();
            while (e.hasMoreElements()) {
                String groupName = (String) e.nextElement();
                String[] userList = ((String) temp.get(groupName)).split(",");

                Set<String> userset = roleUsersMap.get(groupName);
                if (userset == null) {
                    userset = new HashSet<String>();
                    roleUsersMap.put(groupName, userset);
                }
                for (String user : userList) {
                    userset.add(user);
                }
            }

        } catch (Exception e) {
            log.error("Generic HTTP Header Properties File Login Module - data load failed", e);
            throw new GeronimoSecurityException(e);
        }
    }

    public boolean login() throws LoginException {
        Map<String, String> headerMap = null;
        loginSucceeded = false;
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new RequestCallback();
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        httpRequest = ((RequestCallback) callbacks[0]).getRequest();
        String[] headers = headerNames.split(",");
        try {
            headerMap = matchHeaders(httpRequest, headers);
        } catch (HeaderMismatchException e) {
            throw (LoginException) new LoginException("Header Mistmatch error").initCause(e);
        }

        if (headerMap.isEmpty()) {
            throw new FailedLoginException();
        }

        if (authenticationAuthority.equalsIgnoreCase("Siteminder")) {
            HeaderHandler headerHandler = new SiteminderHeaderHandler();
            username = headerHandler.getUser(headerMap);
        } else if (authenticationAuthority.equalsIgnoreCase("Datapower")) {
            /* To be Done */
        }
        if (username == null || username.equals("")) {
            username = null;
            throw new FailedLoginException();
        }

        if (username != null) {
            for (Map.Entry<String, Set<String>> entry : roleUsersMap.entrySet()) {
                String groupName = entry.getKey();
                Set<String> users = entry.getValue();
                for (String user : users) {
                    if (username.equals(user)) {
                        groups.add(groupName);
                        break;
                    }
                }
            }
        }

        if (groups.isEmpty()) {
            log.error("No roles associated with user " + username);
            loginSucceeded = false;
            throw new FailedLoginException();
        } else
            loginSucceeded = true;
        return loginSucceeded;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     * 
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            if (username != null) {
                super.commitHelper();
            }
        }

        // Clear out the private state
        username = null;
        roleUsersMap.clear();
        groups.clear();

        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            username = null;
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        username = null;
        if (!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }
}
