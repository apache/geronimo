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

package org.apache.geronimo.security.jaas;

import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.security.SecurityService;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.kernel.Kernel;


/**
 * A JAAS configuration mechanism (associating JAAS configuration names with
 * specific LoginModule configurations).  This is a drop-in replacement for the
 * normal file-reading JAAS configuration mechanism.  Instead of getting
 * its configuration from its file, it gets its configuration from other
 * GBeans running in Geronimo.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoLoginConfiguration extends Configuration implements GBeanLifecycle {

    private static Map entries = new Hashtable();
    private Configuration oldConfiguration;
    private static Kernel kernel; //todo: this restricts you to one Kernel per JVM

    public GeronimoLoginConfiguration(Kernel kernel) {
        this.kernel = kernel;
    }

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        AppConfigurationEntry entry = (AppConfigurationEntry) entries.get(name);

        if (entry == null) return null;

//        if(!entry.getOptions().containsKey("kernel")) {
//            entry.getOptions().put("kernel", kernel.getKernelName());
//        }

        return new AppConfigurationEntry[]{entry};
    }

    public void refresh() {
    }

    /**
     * Registers a single Geronimo LoginModule
     */
    public static void register(JaasLoginModuleConfiguration entry) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        if (entries.containsKey(entry.getName())) throw new java.lang.IllegalArgumentException("ConfigurationEntry already registered");

        entries.put(entry.getName(), getAppConfigurationEntry(entry));
    }

    private static AppConfigurationEntry getAppConfigurationEntry(JaasLoginModuleConfiguration config) {
        return new AppConfigurationEntry(config.getLoginModuleClassName(), config.getFlag().getFlag(), config.getOptions());
    }

    /**
     * Registers a wrapper configuration that will hit a Geronimo security
     * realm under the covers.
     */
    public static void register(SecurityRealm realm) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        if (entries.containsKey(realm.getRealmName())) throw new java.lang.IllegalArgumentException("ConfigurationEntry already registered");
        Map options = new HashMap();
        options.put("realm", realm.getRealmName());
        if(kernel != null) {
            options.put("kernel", kernel.getKernelName());
        }

        entries.put(realm.getRealmName(), new AppConfigurationEntry("org.apache.geronimo.security.jaas.JaasLoginCoordinator", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options));
    }

    public static void unRegister(String name) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityService.CONFIGURE);

        entries.remove(name);
    }

    public void doStart() throws WaitingException, Exception {
        try {
            oldConfiguration = Configuration.getConfiguration();
        } catch (SecurityException e) {
            oldConfiguration = null;
        }
        Configuration.setConfiguration(this);
    }

    public void doStop() throws WaitingException, Exception {
        Configuration.setConfiguration(oldConfiguration);
    }

    public void doFail() {
        Configuration.setConfiguration(oldConfiguration);
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GeronimoLoginConfiguration.class.getName());
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
