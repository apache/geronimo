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
package org.apache.geronimo.jmxremoting;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * JMX Authenticator that checks the Credentials by logging in via JAAS.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/30 19:48:01 $
 */
public class Authenticator implements JMXAuthenticator {
    private final String configName;

    /**
     * Constructor indicating which JAAS Application Configuration Entry to use.
     * @param configName the JAAS config name
     */
    public Authenticator(String configName) {
        this.configName = configName;
    }

    public Subject authenticate(Object o) throws SecurityException {
        if (o instanceof Credentials == false) {
            throw new IllegalArgumentException("Expected Credentials, got " + o == null ? null : o.getClass().getName());
        }

        Credentials credentials = (Credentials) o;
        try {
            LoginContext context = new LoginContext(configName, credentials);
            context.login();
            return context.getSubject();
        } catch (LoginException e) {
            // do not propogate cause - we don't know what information is may contain
            throw new SecurityException("Invalid login");
        } finally {
            credentials.clear();
        }
    }
}
