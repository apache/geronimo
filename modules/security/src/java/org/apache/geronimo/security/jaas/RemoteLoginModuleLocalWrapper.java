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

import org.apache.geronimo.security.RealmPrincipal;


/**
 * A wrapper used by the Geronimo security system to make sure that the
 * principals that are put into the subject get copied into RealmPrincipals
 * which, in turn, also get placed into the subject.  It is these RealmPrincipals
 * that are used in the principal to role mapping.
 *
 * @version $Rev$ $Date$
 */
public class RemoteLoginModuleLocalWrapper implements LoginModule {
    private String realmName;
    private LoginModuleId loginModuleId;
    private Subject internalSubject;
    private Subject externalSubject;
    private LoginServiceMBean remoteLoginService;
    private CallbackHandler callbackHandler;


    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.externalSubject = subject;
        this.callbackHandler = callbackHandler;
        this.realmName = (String) options.get(LoginModuleConstants.REALM_NAME);
        this.remoteLoginService = (LoginServiceMBean) options.get(RemoteLoginModule.LOGIN_SERVICE);
        try {
            this.loginModuleId = remoteLoginService.allocateLoginModule(realmName);
        } catch (LoginException e) {
        }
    }

    /**
     * Login using a remote login module.
     * <p/>
     * <p>There is a possibility that the remote login module could have been
     * reclaimed on the server side.  This can occur if a long time passes
     * between the call to <code>initialize()</code> and the call to
     * <code>login()</code>.   If this is the case, we will receive a
     * <code>ExpiredLoginModuleException</code>.  When this happens, we will
     * reallocate a new login module and attempt to login again.
     *
     * @return true if the authentication was successful
     * @throws LoginException if authentication fails
     * @see ExpiredLoginModuleException
     */
    public boolean login() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        try {
            return tryLogin();
        } catch (ExpiredLoginModuleException ele) {
            try {
                loginModuleId = remoteLoginService.allocateLoginModule(realmName);
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

        remoteLoginService.commit(loginModuleId);

        internalSubject = remoteLoginService.retrieveSubject(loginModuleId);

        externalSubject.getPrincipals().addAll(internalSubject.getPrincipals());

        return true;
    }

    public boolean abort() throws LoginException {
        if (loginModuleId == null) throw new LoginException("No login module registered");

        return remoteLoginService.abort(loginModuleId);
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

        return remoteLoginService.logout(loginModuleId);
    }

    /**
     * Simulate an upcall from the login module on the remote server
     *
     * @return
     * @throws LoginException
     */
    private boolean tryLogin() throws Exception {
        Collection collection = remoteLoginService.getCallbacks(loginModuleId);
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
        return remoteLoginService.login(loginModuleId, list);
    }

    /**
     * Tell the server that the allocated login module is no longer needed.
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        if (loginModuleId != null) {
            remoteLoginService.removeLoginModule(loginModuleId);
        }
    }
}
