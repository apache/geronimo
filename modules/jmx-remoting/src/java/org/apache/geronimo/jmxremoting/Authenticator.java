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
 * @version $Rev$ $Date$
 */
public class Authenticator implements JMXAuthenticator {
    private final String configName;
    private final ClassLoader cl;

    /**
     * Constructor indicating which JAAS Application Configuration Entry to use.
     * @param configName the JAAS config name
     */
    public Authenticator(String configName, ClassLoader cl) {
        this.configName = configName;
        this.cl = cl;
    }

    public Subject authenticate(Object o) throws SecurityException {
        if (o instanceof String[] == false) {
            throw new IllegalArgumentException("Expected String[2], got " + o == null ? null : o.getClass().getName());
        }
        String[] params = (String[]) o;
        if (params.length != 2) {
            throw new IllegalArgumentException("Expected String[2] but length was " + params.length);
        }

        Thread thread = Thread.currentThread();
        ClassLoader oldCL = thread.getContextClassLoader();
        Credentials credentials = new Credentials(params[0], params[1]);
        try {
            thread.setContextClassLoader(cl);
            LoginContext context = new LoginContext(configName, credentials);
            context.login();
            return context.getSubject();
        } catch (LoginException e) {
            // do not propogate cause - we don't know what information is may contain
            throw new SecurityException("Invalid login");
        } finally {
            credentials.clear();
            thread.setContextClassLoader(oldCL);
        }
    }
}
