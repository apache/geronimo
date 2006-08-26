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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
public class CertificateChainLoginModule implements LoginModule {
    private static Log log = LogFactory.getLog(CertificateChainLoginModule.class);

    Subject subject;
    CallbackHandler handler;
    X500Principal principal;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
//        try {
//            Kernel kernel = KernelRegistry.getKernel((String)options.get(JaasLoginModuleUse.KERNEL_LM_OPTION));
//            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
//            URI usersURI = new URI((String)options.get(USERS_URI));
//            URI groupsURI = new URI((String)options.get(GROUPS_URI));
//            loadProperties(kernel, serverInfo, usersURI, groupsURI);
//        } catch (Exception e) {
//            log.error(e);
//            throw new IllegalArgumentException("Unable to configure properties file login module: "+e);
//        }
    }



    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[1];

        callbacks[0] = new CertificateChainCallback();
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        assert callbacks.length == 1;
        Certificate[] certificateChain = ((CertificateChainCallback)callbacks[0]).getCertificateChain();
        if (certificateChain == null || certificateChain.length == 0) {
            return false;
        }
        if (!(certificateChain[0] instanceof X509Certificate)) {
            return false;
        }
        //TODO actually validate chain
        principal = ((X509Certificate)certificateChain[0]).getSubjectX500Principal();

        return true;
    }

    public boolean commit() throws LoginException {
        Set principals = subject.getPrincipals();

        principals.add(principal);
        principals.add(new GeronimoUserPrincipal(principal.getName()));

        return true;
    }

    public boolean abort() throws LoginException {
        principal = null;

        return true;
    }

    public boolean logout() throws LoginException {
        principal = null;

        return true;
    }

    /**
     * Gets the names of all principal classes that may be populated into
     * a Subject.
     */
    public String[] getPrincipalClassNames() {
        return new String[]{GeronimoUserPrincipal.class.getName()};
    }

}
