/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.security.jaas;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * A wrapper used by the Geronimo security system to make sure that the
 * principals that are put into the subject get copied into RealmPrincipals
 * which, in turn, also get placed into the subject.  It is these RealmPrincipals
 * that are used in the principal to role mapping.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */
public class LocalLoginModule implements LoginModule {
    private String realmName;
    private String kernelName;
    private Subject internalSubject = new Subject();
    private Subject externalSubject;
    private LoginModuleId loginModuleId;
    LoginServiceMBean loginService;
    private CallbackHandler callbackHandler;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        externalSubject = subject;
        this.callbackHandler = callbackHandler;
        realmName = (String) options.get("realm");
        kernelName = (String) options.get("kernel");
        try {
            Kernel kernel = Kernel.getKernel(kernelName);

            if (kernel == null) throw new GeronimoSecurityException("No kernel found by the name of " + kernelName);

            loginService = (LoginServiceMBean) MBeanProxyFactory.getProxy(LoginServiceMBean.class, kernel.getMBeanServer(), LoginService.LOGIN_SERVICE);

            this.loginModuleId = loginService.allocateLoginModule(realmName);
        } catch (Exception e) {
            throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString() + "\n").initCause(e);
        }
    }

    public boolean login() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        try {
            return tryLogin();
        } catch (ExpiredLoginModuleException ele) {
            try {
                loginModuleId = loginService.allocateLoginModule(realmName);
                return tryLogin();
            } catch (Exception e) {
                throw (LoginException) new LoginException().initCause(e);
            }
        } catch (Exception e) {
            throw (LoginException) new LoginException().initCause(e);
        }
    }

    public boolean commit() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        loginService.commit(loginModuleId);

        internalSubject = loginService.retrieveSubject(loginModuleId);

        externalSubject.getPrincipals().addAll(internalSubject.getPrincipals());
        externalSubject.getPrivateCredentials().addAll(internalSubject.getPrivateCredentials());
        externalSubject.getPublicCredentials().addAll(internalSubject.getPublicCredentials());

        return true;
    }

    public boolean abort() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        return loginService.abort(loginModuleId);
    }

    public boolean logout() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        Iterator pricipals = externalSubject.getPrincipals().iterator();
        while (pricipals.hasNext()) {
            Object o = pricipals.next();
            if (o instanceof RealmPrincipal)
                pricipals.remove();
            else if (internalSubject.getPrincipals().contains(o)) pricipals.remove();
        }

        Iterator privateCredentials = externalSubject.getPrivateCredentials().iterator();
        while (privateCredentials.hasNext()) {
            Object o = privateCredentials.next();
            if (internalSubject.getPrivateCredentials().contains(o)) privateCredentials.remove();
        }

        Iterator publicCredentials = externalSubject.getPublicCredentials().iterator();
        while (publicCredentials.hasNext()) {
            Object o = publicCredentials.next();
            if (internalSubject.getPublicCredentials().contains(o)) publicCredentials.remove();
        }

        return loginService.logout(loginModuleId);
    }

    /**
     * Simulate an upcall from the login module on the remote server
     *
     * @return
     * @throws LoginException
     */
    private boolean tryLogin() throws Exception {
        Collection collection = loginService.getCallbacks(loginModuleId);
        Callback[] callbacks = new Callback[0];

        callbacks = (Callback[]) collection.toArray(new Callback[]{});

        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }

        ArrayList list = new ArrayList();
        for (int i = 0; i < callbacks.length; i++) {
            list.add(callbacks[i]);
        }
        return loginService.login(loginModuleId, list);
    }
}
