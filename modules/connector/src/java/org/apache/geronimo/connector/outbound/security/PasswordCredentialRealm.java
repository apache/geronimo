/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound.security;

import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.ManagedConnectionFactory;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.JaasLoginCoordinator;
import org.apache.geronimo.security.jaas.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.realm.SecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public class PasswordCredentialRealm implements SecurityRealm, ConfigurationEntryFactory, ManagedConnectionFactoryListener {

    ManagedConnectionFactory managedConnectionFactory;
    private final Kernel kernel;
    private final String realmName;

    static final String REALM_INSTANCE = "org.apache.connector.outbound.security.PasswordCredentialRealm";

    public PasswordCredentialRealm(Kernel kernel, String realmName) {
        this.kernel = kernel;
        this.realmName = realmName;
    }

    public String getRealmName() {
        return realmName;
    }

    public boolean isRestrictPrincipalsToServer() {
        return true;
    }

    public String[] getLoginDomains() {
        return new String[]{realmName};
    }

    public JaasLoginModuleConfiguration[] getAppConfigurationEntries() {
        Map options = new HashMap();

        // TODO: This can be a bad thing, passing a reference to a realm to the login module
        // since the SerializableACE can be sent remotely
        options.put(REALM_INSTANCE, this);
        JaasLoginModuleConfiguration config = new JaasLoginModuleConfiguration(PasswordCredentialLoginModule.class.getName(),
                                                                               LoginModuleControlFlag.REQUISITE, options, true, getRealmName());
        return new JaasLoginModuleConfiguration[]{config};
    }

    public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) {
        this.managedConnectionFactory = managedConnectionFactory;
    }

    ManagedConnectionFactory getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    public String getConfigurationName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration generateConfiguration() {
        Map options = new HashMap();
        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());

        return new JaasLoginModuleConfiguration(JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true, realmName);
    }

}
