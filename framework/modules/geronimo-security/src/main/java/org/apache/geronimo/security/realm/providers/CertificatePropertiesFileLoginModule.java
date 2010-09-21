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
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * An example LoginModule that reads a list of credentials and group from a file on disk.
 * Authentication is provided by the SSL layer supplying the client certificate.
 * All we check is that it is present.  The
 * file should be formatted using standard Java properties syntax.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 *
 * The usersURI property file should have lines of the form token=certificatename
 * where certificate name is X509Certificate.getSubjectX500Principal().getName()
 *
 * The groupsURI property file should have lines of the form group=token1,token2,...
 * where the tokens were associated to the certificate names in the usersURI properties file.
 *
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class CertificatePropertiesFileLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(CertificatePropertiesFileLoginModule.class);
    public final static String USERS_URI = "usersURI";
    public final static String GROUPS_URI = "groupsURI";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(USERS_URI, GROUPS_URI));
            
    private final Map users = new HashMap();
    final Map groups = new HashMap();

    private Subject subject;
    private CallbackHandler handler;
    private X500Principal principal;
    private boolean loginSucceeded;
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
            URI usersURI = new URI((String)options.get(USERS_URI));
            URI groupsURI = new URI((String)options.get(GROUPS_URI));
            loadProperties(serverInfo, usersURI, groupsURI);
        } catch (Exception e) {
            log.error("Failed to load properties", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: "+e.getMessage(), e);
        }
    }

    public void loadProperties(ServerInfo serverInfo, URI usersURI, URI groupURI) throws GeronimoSecurityException {
        try {
            URI userFile = serverInfo.resolve(usersURI);
            URI groupFile = serverInfo.resolve(groupURI);
            InputStream stream = userFile.toURL().openStream();
            Properties tmpUsers = new Properties();
            tmpUsers.load(stream);
            stream.close();

            for (Iterator iterator = tmpUsers.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                users.put(entry.getValue(), entry.getKey());
            }

            Properties temp = new Properties();
            stream = groupFile.toURL().openStream();
            temp.load(stream);
            stream.close();

            Enumeration e = temp.keys();
            while (e.hasMoreElements()) {
                String groupName = (String) e.nextElement();
                String[] userList = ((String) temp.get(groupName)).split(",");

                Set userset = (Set) groups.get(groupName);
                if (userset == null) {
                    userset = new HashSet();
                    groups.put(groupName, userset);
                }

                for (int i = 0; i < userList.length; i++) {
                    String userName = userList[i];
                    userset.add(userName);
                }
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

        callbacks[0] = new CertificateCallback();
        try {
            handler.handle(callbacks);
            assert callbacks.length == 1;
            X509Certificate certificate = ((CertificateCallback)callbacks[0]).getCertificate();
            if (certificate == null) {
                throw new FailedLoginException();
            }
            principal = certificate.getSubjectX500Principal();

            if(!users.containsKey(principal.getName())) {
                // Clear out the private state
                principal = null;
                throw new FailedLoginException();
            }

            loginSucceeded = true;
            return true;
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            //try username/pw callbacks
            callbacks[0] = new NameCallback("User name");
            try {
                handler.handle(callbacks);
                assert callbacks.length == 1;
                String name = ((NameCallback)callbacks[0]).getName();
                if (name == null) {
                    throw new FailedLoginException();
                }
                principal = new X500Principal(name);
                //this normalizes the name by removing spaces
                name = principal.getName();
                if(!users.containsKey(name)) {
                    // Clear out the private state
                    principal = null;
                    throw new FailedLoginException();
                }
                principal = new X500Principal(name);
                loginSucceeded = true;
                return true;
            } catch (IOException ioe) {
                throw (LoginException) new LoginException().initCause(ioe);
            } catch (UnsupportedCallbackException uce2) {
                //fall through
            }
            throw (LoginException) new LoginException().initCause(uce);
        }
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     *
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if(loginSucceeded) {
            allPrincipals.add(principal);
            String userName = (String) users.get(principal.getName());
            allPrincipals.add(new GeronimoUserPrincipal(userName));

            Iterator e = groups.keySet().iterator();
            while (e.hasNext()) {
                String groupName = (String) e.next();
                Set users = (Set) groups.get(groupName);
                Iterator iter = users.iterator();
                while (iter.hasNext()) {
                    String user = (String) iter.next();
                    if (userName.equals(user)) {
                        allPrincipals.add(new GeronimoGroupPrincipal(groupName));
                        break;
                    }
                }
            }
            subject.getPrincipals().addAll(allPrincipals);
        }
        // Clear out the private state
        principal = null;
        
        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if(loginSucceeded) {
            // Clear out the private state
            principal = null;
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        principal = null;
        if(!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

}
