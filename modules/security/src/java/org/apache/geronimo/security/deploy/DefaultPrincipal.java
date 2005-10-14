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
package org.apache.geronimo.security.deploy;

import java.io.Serializable;
import java.util.Set;


/**
 * @version $Rev$ $Date$
 */
public class DefaultPrincipal implements Serializable {
    private String realmName;
    private String loginDomain;
    private Principal principal;

    private Set namedUserPasswordCredentials;

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getLoginDomain() {
        return loginDomain;
    }

    public void setLoginDomain(String loginDomain) {
        this.loginDomain = loginDomain;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public Set getNamedUserPasswordCredentials() {
        return namedUserPasswordCredentials;
    }

    public void setNamedUserPasswordCredentials(Set namedUserPasswordCredentials) {
        this.namedUserPasswordCredentials = namedUserPasswordCredentials;
    }
}
