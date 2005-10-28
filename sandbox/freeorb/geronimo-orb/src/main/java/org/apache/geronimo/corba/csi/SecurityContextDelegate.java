/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.csi;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;


/**
 * Interface to the application server logic needed by CSI
 */

public interface SecurityContextDelegate {

    /**
     * get info needed to construct an out-bound IIOP request with CSIv2
     */
    AuthenticationInfo getAuthenticationInfo();

    /**
     * do a login
     */
    Subject login(String name, String realm, String password) throws LoginException;

    /**
     * do an anonymous login
     */
    Subject anonymousLogin() throws LoginException;

    /**
     * set the current system subject
     */
    void setAuthenticatedSubject(Subject subject);

    /**
     * establish user
     */
    Subject delegate(String user, String domain);

}
