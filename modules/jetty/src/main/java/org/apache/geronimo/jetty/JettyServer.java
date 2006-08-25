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
package org.apache.geronimo.jetty;

import org.mortbay.http.UserRealm;
import org.mortbay.jetty.Server;


/**
 * JettyServer extends the base Jetty Server class to prevent managing any user realm information by the web.xml realm name
 * which is only relevant for basic and digest authentication and should not be tied to any
 * actual information about which security realm is in use.
 * 
 * @version $Rev$ $Date$
 */
public class JettyServer extends Server {

    public UserRealm addRealm(UserRealm realm) {
        throw new IllegalArgumentException("You must supply a security-realm-name to every web module using security features");
    }

    public UserRealm getRealm(String realmName) {
        throw new IllegalArgumentException("You must supply a security-realm-name to every web module using security features");
    }

    public synchronized void removeRealm(UserRealm realm) {
        throw new IllegalArgumentException("You must supply a security-realm-name to every web module using security features");
    }

}
