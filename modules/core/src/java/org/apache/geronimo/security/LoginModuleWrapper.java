/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
package org.apache.geronimo.security;

import org.apache.geronimo.security.util.ContextManager;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.AccessControlContext;
import java.security.AccessController;


/**
 * A wrapper used by the Geronimo security system to make sure that the
 * principals that are put into the subject get copied into RealmPrincipals
 * which, in turn, also get placed into the subject.  It is these RealmPrincipals
 * that are used in the principal to role mapping.
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/12 04:30:35 $
 */
public class LoginModuleWrapper implements LoginModule {
    private String realm;
    private LoginModule module;
    private Subject internalSubject = new Subject();
    private Subject externalSubject;
    private static ClassLoader classLoader;

    static {
        classLoader = (ClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
    }

    public final static String REALM = "org.apache.geronimo.security.LoginModuleWrapper.REALM";
    public final static String MODULE = "org.apache.geronimo.security.LoginModuleWrapper.MODULE";
    public final static String LOADER = "org.apache.geronimo.security.LoginModuleWrapper.LOADER";


    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

        externalSubject = subject;
        realm = (String) options.get(REALM);
        try {
            final String finalClass = (String) options.get(MODULE);
            module = (LoginModule) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction() {
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

        Set set = internalSubject.getPrincipals();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            externalSubject.getPrincipals().add(new RealmPrincipal(realm, (Principal) iter.next()));
        }
        externalSubject.getPrincipals().addAll(internalSubject.getPrincipals());
        externalSubject.getPrivateCredentials().addAll(internalSubject.getPrivateCredentials());
        externalSubject.getPublicCredentials().addAll(internalSubject.getPublicCredentials());

        AccessControlContext context = (AccessControlContext)Subject.doAsPrivileged(externalSubject, new java.security.PrivilegedAction() {
            public Object run() {
                return AccessController.getContext();
            }
        }, null);
        externalSubject.getPrivateCredentials().add(new AccessControlContextCredential(context));
        ContextManager.registerContext(externalSubject, context);

        return true;
    }

    public boolean abort() throws LoginException {
        return module.abort();
    }

    public boolean logout() throws LoginException {
        ContextManager.unregisterContext(externalSubject);

        return module.logout();
    }
}
