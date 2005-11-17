/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas.client;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.security.jaas.server.JaasSessionId;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;


/**
 * @version $Revision$ $Date$
 */
public class ServerLoginProxy extends LoginModuleProxy {
    CallbackHandler handler;
    Callback[] callbacks;
    private final int lmIndex;
    private final JaasLoginServiceMBean service;
    private final JaasSessionId sessionHandle;

    public ServerLoginProxy(LoginModuleControlFlag controlFlag, Subject subject, int lmIndex,
                            JaasLoginServiceMBean service, JaasSessionId sessionHandle)
    {
        super(controlFlag, subject);
        this.lmIndex = lmIndex;
        this.service = service;
        this.sessionHandle = sessionHandle;
    }

    public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options) {
        this.handler = handler;
    }

    /**
     * Perform a login on the server side.
     * <p/>
     * Here we get the Callbacks from the server side, pass them to the
     * local handler so that they may be filled.  We pass the resulting
     * set of Callbacks back to the server.
     *
     * @return true if the authentication succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws javax.security.auth.login.LoginException
     *          if the authentication fails
     */
    public boolean login() throws LoginException {
        try {
            callbacks = service.getServerLoginCallbacks(sessionHandle, lmIndex);
            if (handler != null) {
                handler.handle(callbacks);
            } else if (callbacks != null && callbacks.length > 0) {
                System.err.println("No callback handler available for " + callbacks.length + " callbacks!");
            }
            return service.performLogin(sessionHandle, lmIndex, callbacks);
        } catch (Exception e) {
            LoginException le = new LoginException("Error filling callback list");
            le.initCause(e);
            throw le;
        }
    }

    public boolean commit() throws LoginException {
        return service.performCommit(sessionHandle, lmIndex);
    }

    public boolean abort() throws LoginException {
        return false; // taken care of with a single call to the server
    }

    public boolean logout() throws LoginException {
        return false; // taken care of with a single call to the server
    }
}