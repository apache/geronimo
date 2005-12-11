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
import javax.security.auth.x500.X500Principal;


public interface AuthenticationInfo {

    void setX500Principal(X500Principal principal);

    X500Principal getX500Principal();

    void setPrincipalName(String name);

    String getPrincipalName();

    void setPassword(String name);

    String getPassword();

    void setRealm(String realm);

    String getRealm();

    void setAnonymous(boolean value);

    boolean isAnonymous();

    void setSubject(Subject subject);

    Subject getSubject();

}
