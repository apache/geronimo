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

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import java.util.Hashtable;
import java.util.Map;

import org.apache.geronimo.security.SecurityService;


/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:09 $
 */
public class GeronimoLoginConfiguration extends Configuration {

    private static Map entries = new Hashtable();

    public AppConfigurationEntry[] getAppConfigurationEntry(String JAASId) {
        ConfigurationEntry entry = (ConfigurationEntry) entries.get(JAASId);

        if (entry == null) return null;

        return entry.getAppConfigurationEntry();
    }

    public void refresh() {
    }

    public static void register(ConfigurationEntry entry) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        if (entries.containsKey(entry.getJAASId())) throw new java.lang.IllegalArgumentException("ConfigurationEntry already registered");

        entries.put(entry.getJAASId(), entry);
    }

    public static void unRegister(ConfigurationEntry entry) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        entries.remove(entry.getJAASId());
    }
}
