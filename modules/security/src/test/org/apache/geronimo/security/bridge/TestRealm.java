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

import javax.security.auth.login.AppConfigurationEntry;

import java.util.HashMap;
import java.util.Set;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.realm.providers.AbstractSecurityRealm;
import org.apache.regexp.RE;


/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:27 $
 */
public class TestRealm extends AbstractSecurityRealm {
    public final static String REALM_NAME = "bridge-realm";
    public final static String JAAS_NAME = "bridge";
    private static final GBeanInfo GBEAN_INFO;

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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TestRealm.class.getName(), AbstractSecurityRealm.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("debug", true));
        infoFactory.addOperation(new GOperationInfo("isLoginModuleLocal"));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"RealmName"},
                                                        new Class[]{String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
