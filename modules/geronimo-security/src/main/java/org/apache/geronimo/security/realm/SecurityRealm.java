/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginModuleChain;


/**
 * @version $Rev$ $Date$
 */
public interface SecurityRealm extends org.apache.geronimo.management.geronimo.SecurityRealm {

    static final String BASE_OBJECT_NAME = "geronimo.security:type=SecurityRealm";

    /**
     * Gets the JAAS configuration for this security realm.
     */
    public JaasLoginModuleConfiguration[] getAppConfigurationEntries();

    /**
     * If this attribute is true, the login service will not return
     * principals generated by this realm to clients.  If set to false (the
     * default), the client will get a copy of all principals (except realm
     * principals generated strictly for use within Geronimo).
     */
    public boolean isRestrictPrincipalsToServer();

    /**
     * If this attribute is true, then the principals will be wrapped in
     * realm principals.
     */
    public boolean isWrapPrincipals();

    /**
     * Gets a list of the login domains that make up this security realm.  A
     * particular LoginModule represents 0 or 1 login domains, and a realm is
     * composed of a number of login modules, so the realm may cover any
     * number of login domains, though typically that number will be 1.
     */
    public String[] getLoginDomains();

    /**
     * Gets the first JaasLoginModuleChain node in the chain of LoginModules
     * for this realm.
     */
    public JaasLoginModuleChain getLoginModuleChain();
}
