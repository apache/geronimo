/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:39 $
 */
public class RemoteLoginModuleLocalWrapper implements LoginModule {
    private String realmName;
    private Long loginModuleId;
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
