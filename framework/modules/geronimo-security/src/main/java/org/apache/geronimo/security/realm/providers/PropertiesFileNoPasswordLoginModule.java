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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
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
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.crypto.encoders.HexTranslator;


/**
 * This login module should only be used if the user is already authenticated, such as from client cert or openid,
 * and the only remaining task is to add group information.
 *
 * A LoginModule that reads groups from a file on disk.  The
 * file should be formatted using standard Java properties syntax.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 * <p/>
 * This login module does not check security credentials so the lifecycle methods must return true to indicate success
 * or throw a LoginException if the user is not known or supplied in the callback.
 *
 * @version $Rev$ $Date$
 */
public class PropertiesFileNoPasswordLoginModule implements LoginModule {
    public final static String GROUPS_URI = "groupsURI";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(GROUPS_URI));

    private static final Logger log = LoggerFactory.getLogger(PropertiesFileNoPasswordLoginModule.class);

    final Map<String, Set<String>> groups = new HashMap<String, Set<String>>();

    private boolean loginSucceeded;
    private Subject subject;
    private CallbackHandler handler;
    private String username;
    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
        try {
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            final String groups = (String) options.get(GROUPS_URI);
            URI groupsURI = new URI(groups);
            loadProperties(serverInfo, groupsURI);
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: " + e.getMessage(),
                    e);
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

                Set<String> userset = groups.get(groupName);
                if (userset == null) {
                    userset = new HashSet<String>();
                    groups.put(groupName, userset);
                }
                userset.addAll(Arrays.asList(userList));
            }

        } catch (Exception e) {
            log.error("Properties File Login Module - data load failed", e);
            throw new GeronimoSecurityException(e);
        }
    }


    /**
     * This LoginModule is not to be ignored.  So, this method should never return false.
     * @return true if authentication succeeds, or throw a LoginException such as FailedLoginException
     *         if authentication fails
     */
    public boolean login() throws LoginException {
        loginSucceeded = false;
        Callback[] callbacks = new Callback[1];

        callbacks[0] = new NameCallback("User name");
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            return false;
        }
        assert callbacks.length == 1;
        username = ((NameCallback) callbacks[0]).getName();
        if (username == null || username.equals("")) {
            // Clear out the private state
            username = null;
            throw new FailedLoginException();
        }

        loginSucceeded = true;
        return true;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     *
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if(loginSucceeded) {
            if(username != null) {
                allPrincipals.add(new GeronimoUserPrincipal(username));
            }
            for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                String groupName = entry.getKey();
                Set<String> users = entry.getValue();
                for (String user : users) {
                    if (username.equals(user)) {
                        allPrincipals.add(new GeronimoGroupPrincipal(groupName));
                        break;
                    }
                }
            }
            subject.getPrincipals().addAll(allPrincipals);
        }
        // Clear out the private state
        username = null;

        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if(loginSucceeded) {
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
        if(!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

}
