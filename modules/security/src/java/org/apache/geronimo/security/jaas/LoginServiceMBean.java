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

package org.apache.geronimo.security.jaas;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import java.util.Collection;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.GeronimoSecurityException;


/**
 * An MBean that maintains a list of security realms.
 *
 * A LoginModuleId represents the configuration of a single security realm,
 * which may in fact include multiple login modules under the covers, but
 * that is not really important here.
 *
 * @version $Rev$ $Date$
 */
public interface LoginServiceMBean {

    Collection getRealms() throws GeronimoSecurityException;

    void setRealms(Collection realms);

    SerializableACE[] getAppConfigurationEntries(String realmName);

    LoginModuleId allocateLoginModules(String realmName) throws LoginException;

    void removeLoginModules(LoginModuleId loginModuleId) throws ExpiredLoginModuleException;

    Collection getCallbacks(LoginModuleId loginModuleId) throws ExpiredLoginModuleException;

    boolean login(LoginModuleId loginModuleId, Collection callbacks) throws LoginException;

    boolean commit(LoginModuleId loginModuleId) throws LoginException;

    boolean abort(LoginModuleId loginModuleId) throws LoginException;

    boolean logout(LoginModuleId loginModuleId) throws LoginException;

    Subject retrieveSubject(LoginModuleId loginModuleId) throws LoginException;
}
