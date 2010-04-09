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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleChain;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.SingleLoginConfiguration;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.Bundle;


/**
 * A security realm that can be configured for one or more login modules.  It
 * can handle a combination of client-side and server-side login modules for
 * the case of remote clients, and it can auto-role-mapping for its login
 * modules (though you must configure it for that).
 * <p/>
 * This realm populates a number of special login module options for the
 * benefit of Geronimo login modules (though some of them are only available to
 * server-side login modules, marked as not Serializable below):
 * <pre>
 * Option                                      Type                   Serializable
 * JaasLoginModuleUse.KERNEL_LM_OPTION       String (Kernel name)        Yes
 * JaasLoginModuleUse.SERVERINFO_LM_OPTION   ServerInfo                  No
 * JaasLoginModuleUse.CLASSLOADER_LM_OPTION  ClassLoader                 No
 * </pre>
 * These options can be safely ignored by login modules that don't need them
 * (such as any custom LoginModules you may already have lying around).
 *
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = SecurityNames.SECURITY_REALM)
public class GenericSecurityRealm implements SecurityRealm, ConfigurationEntryFactory, ConfigurationFactory {

    private final String realmName;
    private AppConfigurationEntry[] config;

    private String[] domains;
    private final boolean wrapPrincipals;
    private final JaasLoginModuleUse loginModuleUse;

    private final boolean global;
    private final ServerInfo serverInfo;
    private final Bundle bundle;
    private final Kernel kernel;
    private final Configuration configuration;

    public GenericSecurityRealm(@ParamAttribute(name="realmName") String realmName,
                                @ParamReference(name="LoginModuleConfiguration", namingType = "LoginModuleUse")JaasLoginModuleUse loginModuleUse,
                                @ParamAttribute(name="wrapPrincipals")boolean wrapPrincipals,
                                @ParamAttribute(name="global")boolean global,
                                @ParamReference(name="ServerInfo")ServerInfo serverInfo,
                                @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel
    ) throws ClassNotFoundException {
        this.realmName = realmName;
        this.wrapPrincipals = wrapPrincipals;
        this.loginModuleUse = loginModuleUse;
        this.global = global;
        this.serverInfo = serverInfo;
        this.bundle = bundle;
        this.kernel = kernel;

        refresh();
        configuration = new SingleLoginConfiguration(this);
    }

    public String getRealmName() {
        return realmName;
    }

    public AppConfigurationEntry[] getAppConfigurationEntries() {
        return config;
    }

    public JaasLoginModuleChain getLoginModuleChain() {
        return loginModuleUse;
    }

    /**
     * Gets a list of the login domains that make up this security realm.  A
     * particular LoginModule represents 0 or 1 login domains, and a realm is
     * composed of a number of login modules, so the realm may cover any
     * number of login domains, though typically that number will be 1.
     */
    public String[] getLoginDomains() {
        return domains;
    }

    /**
     * If this attribute is true, then the principals will be wrapped in
     * realm principals.
     */
    public boolean isWrapPrincipals() {
        return wrapPrincipals;
    }

    public String getConfigurationName() {
        return realmName;
    }

    public boolean isGlobal() {
        return global;
    }

    public void refresh() {
        Set<String> domainNames = new HashSet<String>();
        List<AppConfigurationEntry> loginModuleConfigurations = new ArrayList<AppConfigurationEntry>();

        if (loginModuleUse != null) {
            try {
                loginModuleUse.configure(domainNames, loginModuleConfigurations, realmName, kernel, serverInfo, bundle);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("can not configure realm", e);
            }
        }

        domains = domainNames.toArray(new String[domainNames.size()]);
        config = loginModuleConfigurations.toArray(new AppConfigurationEntry[loginModuleConfigurations.size()]);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
