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

package org.apache.geronimo.security.bridge;

import java.util.HashMap;
import java.util.Set;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.realm.providers.AbstractSecurityRealm;
import org.apache.regexp.RE;


/**
 * @version $Revision: 1.5 $ $Date: 2004/06/02 05:33:05 $
 */
public class TestRealm extends AbstractSecurityRealm {
    public final static String REALM_NAME = "bridge-realm";
    public final static String JAAS_NAME = "bridge";

    public TestRealm() {
    }

    public TestRealm(String realmName) {
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
        return new AppConfigurationEntry(TestLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                new HashMap());
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TestRealm.class, AbstractSecurityRealm.GBEAN_INFO);
        infoFactory.addAttribute("debug", boolean.class, true);
        infoFactory.addOperation("isLoginModuleLocal");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
