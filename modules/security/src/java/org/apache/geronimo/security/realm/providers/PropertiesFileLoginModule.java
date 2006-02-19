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

package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * A LoginModule that reads a list of users and group from files on disk.  The
 * files should be formatted using standard Java properties syntax.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 *
 * @version $Rev$ $Date$
 */
public class PropertiesFileLoginModule implements LoginModule {
    public final static String USERS_URI = "usersURI";
    public final static String GROUPS_URI = "groupsURI";
    private static Log log = LogFactory.getLog(PropertiesFileLoginModule.class);
    final Properties users = new Properties();
    final Map groups = new HashMap();

    Subject subject;
    CallbackHandler handler;
    String username;
    String password;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        try {
            Kernel kernel = KernelRegistry.getKernel((String)options.get(JaasLoginModuleUse.KERNEL_NAME_LM_OPTION));
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            final String users = (String)options.get(USERS_URI);
            final String groups = (String)options.get(GROUPS_URI);
            if(users == null || groups == null) {
                throw new IllegalArgumentException("Both "+USERS_URI+" and "+GROUPS_URI+" must be provided!");
            }
            URI usersURI = new URI(users);
            URI groupsURI = new URI(groups);
            loadProperties(kernel, serverInfo, usersURI, groupsURI);
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: "+e.getMessage());
        }
    }

    public void loadProperties(Kernel kernel, ServerInfo serverInfo, URI userURI, URI groupURI) throws GeronimoSecurityException {
        try {
            URI userFile = serverInfo.resolveServer(userURI);
            URI groupFile = serverInfo.resolveServer(groupURI);
            InputStream stream = userFile.toURL().openStream();
            users.load(stream);
            stream.close();

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
                    userset.add(userList[i]);
                }
            }

        } catch (Exception e) {
            log.error("Properties File Login Module - data load failed", e);
            throw new GeronimoSecurityException(e);
        }
    }


    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("User name");
        callbacks[1] = new PasswordCallback("Password", false);
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        assert callbacks.length == 2;
        username = ((NameCallback) callbacks[0]).getName();
        if(username == null || username.equals("")) {
            return false;
        }
        String realPassword = users.getProperty(username);
        char[] entered = ((PasswordCallback) callbacks[1]).getPassword();
        password = entered == null ? null : new String(entered);
        boolean result = (realPassword == null && password == null) ||
                (realPassword != null && password != null && realPassword.equals(password));
        if(!result) {
            throw new FailedLoginException();
        }
        return true;
    }

    public boolean commit() throws LoginException {
        Set principals = subject.getPrincipals();

        principals.add(new GeronimoUserPrincipal(username));

        Iterator e = groups.keySet().iterator();
        while (e.hasNext()) {
            String groupName = (String) e.next();
            Set users = (Set) groups.get(groupName);
            Iterator iter = users.iterator();
            while (iter.hasNext()) {
                String user = (String) iter.next();
                if (username.equals(user)) {
                    principals.add(new GeronimoGroupPrincipal(groupName));
                    break;
                }
            }
        }

        return true;
    }

    public boolean abort() throws LoginException {
        username = null;
        password = null;

        return true;
    }

    public boolean logout() throws LoginException {
        username = null;
        password = null;
        //todo: should remove principals added by commit
        return true;
    }

    /**
     * Gets the names of all principal classes that may be populated into
     * a Subject.
     */
    public String[] getPrincipalClassNames() {
        return new String[]{GeronimoUserPrincipal.class.getName(), GeronimoGroupPrincipal.class.getName()};
    }

    /**
     * Gets a list of all the principals of a particular type (identified by
     * the principal class).  These are available for manual role mapping.
     */
    public String[] getPrincipalsOfClass(String className) {
        Set s;
        if(className.equals(GeronimoGroupPrincipal.class.getName())) {
            s = groups.keySet();
        } else if(className.equals(GeronimoUserPrincipal.class.getName())) {
            s = users.keySet();
        } else {
            throw new IllegalArgumentException("No such principal class "+className);
        }
        return (String[]) s.toArray(new String[s.size()]);
    }
}
