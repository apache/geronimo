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

package org.apache.geronimo.connector.outbound.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.realm.providers.AbstractSecurityRealm;
import org.apache.regexp.RE;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class PasswordCredentialRealm extends AbstractSecurityRealm implements SecurityRealm, ManagedConnectionFactoryListener {

    private static final GBeanInfo GBEAN_INFO;

    ManagedConnectionFactory managedConnectionFactory;

    static final String REALM_INSTANCE = "org.apache.connector.outbound.security.PasswordCredentialRealm";

    public PasswordCredentialRealm(String realmName) {
        super(realmName);
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        return null;
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        return null;
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null;
    }

    public void refresh() throws GeronimoSecurityException {
    }

    public AppConfigurationEntry getAppConfigurationEntry() {
        Map options = new HashMap();

        // TODO: This can be a bad thing, passing a reference to a realm to the login module
        // since the SerializableACE can be sent remotely
        options.put(REALM_INSTANCE, this);
        AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry(PasswordCredentialLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                options);
        return appConfigurationEntry;
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) {
        this.managedConnectionFactory = managedConnectionFactory;
    }

    ManagedConnectionFactory getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(PasswordCredentialRealm.class, AbstractSecurityRealm.getGBeanInfo());
        infoFactory.addInterface(ManagedConnectionFactoryListener.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
