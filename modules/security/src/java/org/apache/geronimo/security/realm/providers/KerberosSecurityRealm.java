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

import java.util.HashMap;
import java.util.Set;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.regexp.RE;


/**
 * @version $Rev$ $Date$
 */
public class KerberosSecurityRealm extends AbstractSecurityRealm {
    private static Log log = LogFactory.getLog(KerberosSecurityRealm.class);

    private boolean running = false;
    private boolean debug;
    private boolean storeKey;
    private boolean useTicketCache;
    private boolean useKeyTab;
    private boolean doNotPrompt;
    private String ticketCache;
    private String keyTab;
    private boolean refreshKrb5Config;
    private String principal;
    private boolean tryFirstPass;
    private boolean useFirstPass;
    private boolean storePass;
    private boolean clearPass;

    //deprecated for geronimombeans only
    public KerberosSecurityRealm() {
    }

    public KerberosSecurityRealm(String realmName) {
        super(realmName);
    }

    public void doStart() {
        refresh();
        running = true;

        log.info("Kerberos Realm - " + getRealmName() + " - started");
    }

    public void doStop() {
        running = false;

        log.info("Kerberos Realm - " + getRealmName() + " - stopped");
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isStoreKey() {
        return storeKey;
    }

    public void setStoreKey(boolean storeKey) {
        this.storeKey = storeKey;
    }

    public boolean isUseTicketCache() {
        return useTicketCache;
    }

    public void setUseTicketCache(boolean useTicketCache) {
        this.useTicketCache = useTicketCache;
    }

    public boolean isUseKeyTab() {
        return useKeyTab;
    }

    public void setUseKeyTab(boolean useKeyTab) {
        this.useKeyTab = useKeyTab;
    }

    public boolean isDoNotPrompt() {
        return doNotPrompt;
    }

    public void setDoNotPrompt(boolean doNotPrompt) {
        this.doNotPrompt = doNotPrompt;
    }

    public String getTicketCache() {
        return ticketCache;
    }

    public void setTicketCache(String ticketCache) {
        this.ticketCache = ticketCache;
    }

    public String getKeyTab() {
        return keyTab;
    }

    public void setKeyTab(String keyTab) {
        this.keyTab = keyTab;
    }

    public boolean isRefreshKrb5Config() {
        return refreshKrb5Config;
    }

    public void setRefreshKrb5Config(boolean refreshKrb5Config) {
        this.refreshKrb5Config = refreshKrb5Config;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public boolean isTryFirstPass() {
        return tryFirstPass;
    }

    public void setTryFirstPass(boolean tryFirstPass) {
        this.tryFirstPass = tryFirstPass;
    }

    public boolean isUseFirstPass() {
        return useFirstPass;
    }

    public void setUseFirstPass(boolean useFirstPass) {
        this.useFirstPass = useFirstPass;
    }

    public boolean isStorePass() {
        return storePass;
    }

    public void setStorePass(boolean storePass) {
        this.storePass = storePass;
    }

    public boolean isClearPass() {
        return clearPass;
    }

    public void setClearPass(boolean clearPass) {
        this.clearPass = clearPass;
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
        log.info("Kerberos Realm - " + getRealmName() + " - refresh");
    }

    public javax.security.auth.login.AppConfigurationEntry[] getAppConfigurationEntries() {
        HashMap options = new HashMap();

        options.put("debug", (debug ? "true" : "false"));
        options.put("storeKey", (storeKey ? "true" : "false"));
        options.put("useTicketCache", (useTicketCache ? "true" : "false"));
        options.put("useKeyTab", (useKeyTab ? "true" : "false"));
        options.put("doNotPrompt", (doNotPrompt ? "true" : "false"));
        options.put("ticketCache", ticketCache);
        options.put("keyTab", keyTab);
        options.put("refreshKrb5Config", (refreshKrb5Config ? "true" : "false"));
        options.put("principal", principal);
        options.put("tryFirstPass", (tryFirstPass ? "true" : "false"));
        options.put("useFirstPass", (useFirstPass ? "true" : "false"));
        options.put("storePass", (storePass ? "true" : "false"));
        options.put("clearPass", (clearPass ? "true" : "false"));

        AppConfigurationEntry entry = new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                                                                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                                                options);

        return new AppConfigurationEntry[]{entry};
    }

    public boolean isLoginModuleLocal() {
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(KerberosSecurityRealm.class, AbstractSecurityRealm.GBEAN_INFO);

        infoFactory.addAttribute("debug", boolean.class, true);
        infoFactory.addAttribute("storeKey", boolean.class, true);
        infoFactory.addAttribute("useTicketCache", boolean.class, true);
        infoFactory.addAttribute("useKeyTab", boolean.class, true);
        infoFactory.addAttribute("doNotPrompt", boolean.class, true);
        infoFactory.addAttribute("ticketCache", String.class, true);
        infoFactory.addAttribute("keyTab", String.class, true);
        infoFactory.addAttribute("refreshKrb5Config", boolean.class, true);
        infoFactory.addAttribute("principal", String.class, true);
        infoFactory.addAttribute("tryFirstPass", boolean.class, true);
        infoFactory.addAttribute("useFirstPass", boolean.class, true);
        infoFactory.addAttribute("storePass", boolean.class, true);
        infoFactory.addAttribute("clearPass", boolean.class, true);

        infoFactory.addOperation("isLoginModuleLocal");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
