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
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.security.Principal;
import java.security.PrivilegedActionException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * A wrapper used by the Geronimo security system to make sure that the
 * principals that are put into the subject get copied into RealmPrincipals
 * which, in turn, also get placed into the subject.  It is these RealmPrincipals
 * that are used in the principal to role mapping.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:39 $
 */
public class RemoteLoginModuleRemoteWrapper implements LoginModule {
    private String realm;
    private LoginModule module;
    private Subject internalSubject = new Subject();
    private Subject externalSubject;
    private static ClassLoader classLoader;

    static {
        classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

        externalSubject = subject;
        realm = (String) options.get(LoginModuleConstants.REALM_NAME);
        try {
            final String finalClass = (String) options.get(LoginModuleConstants.MODULE);
            module = (LoginModule) java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                    return Class.forName(finalClass, true, classLoader).newInstance();
                }
            });
            module.initialize(internalSubject, callbackHandler, sharedState, options);
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof InstantiationException) {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error:" + e.getCause().getMessage() + "\n").initCause(e.getCause());
            } else {
                throw (GeronimoSecurityException) new GeronimoSecurityException("Initialize error: " + e.toString() + "\n").initCause(e);
            }
        }
    }

    public boolean login() throws LoginException {
        return module.login();
    }

    public boolean commit() throws LoginException {

        if (!module.commit()) return false;

        RealmPrincipal principal;
        Set set = internalSubject.getPrincipals();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            principal = new RealmPrincipal(realm, (Principal) iter.next());
            externalSubject.getPrincipals().add(principal);
        }
        externalSubject.getPrincipals().addAll(internalSubject.getPrincipals());
        externalSubject.getPrivateCredentials().addAll(internalSubject.getPrivateCredentials());
        externalSubject.getPublicCredentials().addAll(internalSubject.getPublicCredentials());

        return true;
    }

    public boolean abort() throws LoginException {
        return module.abort();
    }

    public boolean logout() throws LoginException {
        Iterator pricipals = externalSubject.getPrincipals().iterator();
        while (pricipals.hasNext()) {
            Object o = pricipals.next();
            if (o instanceof RealmPrincipal) pricipals.remove();
            if (internalSubject.getPrincipals().contains(o)) pricipals.remove();
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
        return module.logout();
    }
}
