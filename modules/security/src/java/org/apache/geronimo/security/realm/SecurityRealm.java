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

package org.apache.geronimo.security.realm;

import javax.security.auth.login.AppConfigurationEntry;

import java.util.Set;

import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.regexp.RE;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */
public interface SecurityRealm {

    static final String BASE_OBJECT_NAME = "geronimo.security:type=SecurityRealm";

    public String getRealmName();

    public Set getGroupPrincipals() throws GeronimoSecurityException;

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException;

    public Set getUserPrincipals() throws GeronimoSecurityException;

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException;

    public void refresh() throws GeronimoSecurityException;

    public AppConfigurationEntry getAppConfigurationEntry();

    public boolean isLoginModuleLocal();

    public long getMaxLoginModuleAge();
}
