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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.outbound.security;

import java.io.IOException;
import java.util.Map;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/11 08:28:15 $
 *
 * */
public class PasswordCredentialLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private PasswordCredentialRealm passwordCredentialRealm;

    private String resourcePrincipalName;
    private String userName;
    private char[] password;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        passwordCredentialRealm = (PasswordCredentialRealm)options.get(PasswordCredentialRealm.REALM_INSTANCE);
        if (passwordCredentialRealm == null) {
            throw new IllegalArgumentException("No realm supplied in options");
        }
    }

    public boolean login() throws LoginException {
        if (passwordCredentialRealm == null || passwordCredentialRealm.managedConnectionFactory == null) {
            return false;
        }
        Callback[] callbacks = new Callback[3];

        callbacks[0] = new NameCallback("Resource Principal");
        callbacks[1] = new NameCallback("User name");
        callbacks[2] = new PasswordCallback("Password", false);
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        resourcePrincipalName = ((NameCallback) callbacks[0]).getName();
        userName = ((NameCallback) callbacks[1]).getName();
        password = ((PasswordCallback) callbacks[2]).getPassword();
        return resourcePrincipalName != null && userName != null && password != null;
    }

    public boolean commit() throws LoginException {
        subject.getPrincipals().add(new ResourcePrincipal(resourcePrincipalName));
        PasswordCredential passwordCredential = new PasswordCredential(userName, password);
        passwordCredential.setManagedConnectionFactory(passwordCredentialRealm.getManagedConnectionFactory());
        subject.getPrivateCredentials().add(passwordCredential);
        return true;
    }

    public boolean abort() throws LoginException {
        subject = null;
        userName = null;
        password = null;
        return true;
    }

    public boolean logout() throws LoginException {
        subject = null;
        userName = null;
        password = null;
        return true;
    }
}
