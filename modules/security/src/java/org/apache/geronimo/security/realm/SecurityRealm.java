/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.regexp.RE;


/**
 * @version $Rev$ $Date$
 */
public interface SecurityRealm {

    static final String BASE_OBJECT_NAME = "geronimo.security:type=SecurityRealm";

    public String getRealmName();

    public long getMaxLoginModuleAge();

    public AppConfigurationEntry[] getAppConfigurationEntries();

    public boolean isLoginModuleLocal();

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getGroupPrincipals() throws GeronimoSecurityException;

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException;

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getUserPrincipals() throws GeronimoSecurityException;

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException;

    /**
     * @deprecated Will be removed a future milestone release.  The refreshing should
     *             be done by a login module or editor when appropriate to its own
     *             needs.
     */
    public void refresh() throws GeronimoSecurityException;

}
