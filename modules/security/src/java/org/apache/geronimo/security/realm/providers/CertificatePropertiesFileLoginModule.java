/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Collection;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * An example LoginModule that reads a list of users and group from a file on disk.
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
 * @version $Rev$ $Date$
 */
public class CertificatePropertiesFileLoginModule implements LoginModule {
    public final static String USERS_URI = "usersURI";
    public final static String GROUPS_URI = "groupsURI";
    private static Log log = LogFactory.getLog(CertificatePropertiesFileLoginModule.class);
    private final Map users = new HashMap();
    final Map groups = new HashMap();

    Subject subject;
    CallbackHandler handler;
    X500Principal principal;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        try {
            Kernel kernel = KernelRegistry.getKernel((String)options.get(JaasLoginModuleUse.KERNEL_LM_OPTION));
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            URI usersURI = new URI((String)options.get(USERS_URI));
            URI groupsURI = new URI((String)options.get(GROUPS_URI));
            loadProperties(kernel, serverInfo, usersURI, groupsURI);
        } catch (Exception e) {
            log.error(e);
            throw new IllegalArgumentException("Unable to configure properties file login module: "+e);
        }
    }

    public void loadProperties(Kernel kernel, ServerInfo serverInfo, URI usersURI, URI groupURI) throws GeronimoSecurityException {
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


    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[1];

        callbacks[0] = new CertificateCallback();
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        assert callbacks.length == 1;
        X509Certificate certificate = ((CertificateCallback)callbacks[0]).getCertificate();
        if (certificate == null) {
            return false;
        }
        principal = certificate.getSubjectX500Principal();

        if(!users.containsKey(principal.getName())) {
            throw new FailedLoginException();
        }
        return true;
    }

    public boolean commit() throws LoginException {
        Set principals = subject.getPrincipals();

        principals.add(principal);
        String userName = (String) users.get(principal.getName());
        principals.add(new GeronimoUserPrincipal(userName));

        Iterator e = groups.keySet().iterator();
        while (e.hasNext()) {
            String groupName = (String) e.next();
            Set users = (Set) groups.get(groupName);
            Iterator iter = users.iterator();
            while (iter.hasNext()) {
                String user = (String) iter.next();
                if (userName.equals(user)) {
                    principals.add(new GeronimoGroupPrincipal(groupName));
                    break;
                }
            }
        }

        return true;
    }

    public boolean abort() throws LoginException {
        principal = null;

        return true;
    }

    public boolean logout() throws LoginException {
        principal = null;
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
        Collection s;
        if(className.equals(GeronimoGroupPrincipal.class.getName())) {
            s = groups.keySet();
        } else if(className.equals(GeronimoUserPrincipal.class.getName())) {
            s = users.values();
        } else if(className.equals(X500Principal.class.getName())) {
            s = users.keySet();
        } else {
            throw new IllegalArgumentException("No such principal class "+className);
        }
        return (String[]) s.toArray(new String[s.size()]);
    }
}
