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
 * @version $Revision: 1.2 $ $Date: 2004/02/17 04:30:29 $
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
