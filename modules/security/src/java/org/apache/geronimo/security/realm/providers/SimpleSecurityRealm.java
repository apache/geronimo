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
package org.apache.geronimo.security.realm.providers;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.regexp.RE;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.security.GeronimoSecurityException;


/**
 * @version $Revision: $ $Date: $
 */
public class SimpleSecurityRealm extends AbstractSecurityRealm {

    private static Log log = LogFactory.getLog(SimpleSecurityRealm.class);

    private boolean running = false;
    private String loginModuleName;
    private Properties options;


    public SimpleSecurityRealm(String realmName) {
        super(realmName);
    }

    public String getLoginModuleName() {
        return loginModuleName;
    }

    public void setLoginModuleName(String loginModuleName) {
        this.loginModuleName = loginModuleName;
    }

    public Properties getOptions() {
        return options;
    }

    public void setOptions(Properties options) {
        this.options = options;
    }

    public void doStart() {
        refresh();
        running = true;

        log.info("Simple Realm - " + getRealmName() + " - started");
    }

    public void doStop() {
        running = false;

        log.info("Simple Realm - " + getRealmName() + " - stopped");
    }

    public void doFail() {
        running = false;

        log.info("Simple Realm - " + getRealmName() + " - failed");
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        return null;
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        return null;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        return null;
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        return null;
    }

    public void refresh() throws GeronimoSecurityException {
        log.info("Simple Realm - " + getRealmName() + " - refresh");
    }

    public javax.security.auth.login.AppConfigurationEntry getAppConfigurationEntry() {

        AppConfigurationEntry entry = new AppConfigurationEntry(loginModuleName,
                                                                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                                                options);

        return entry;
    }

    public boolean isLoginModuleLocal() {
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SimpleSecurityRealm.class, AbstractSecurityRealm.GBEAN_INFO);

        infoFactory.addAttribute("loginModuleName", String.class, true);
        infoFactory.addAttribute("options", Properties.class, true);

        infoFactory.addOperation("isLoginModuleLocal");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
