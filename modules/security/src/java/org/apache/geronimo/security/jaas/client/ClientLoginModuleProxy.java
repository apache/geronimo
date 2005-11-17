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
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.security.jaas.LoginModuleControlFlag;


/**
 * @version $Revision$ $Date$
 */
public class ClientLoginModuleProxy extends LoginModuleProxy
{
    private final LoginModule source;

    public ClientLoginModuleProxy(LoginModuleControlFlag controlFlag, Subject subject, LoginModule source)
    {
        super(controlFlag, subject);
        this.source = source;
    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
    {
        source.initialize(subject, callbackHandler, sharedState, options);
    }

    public boolean login() throws LoginException
    {
        return source.login();
    }

    public boolean commit() throws LoginException
    {
        return source.commit();
    }

    public boolean abort() throws LoginException
    {
        return source.abort();
    }

    public boolean logout() throws LoginException
    {
        return source.logout();
    }
}