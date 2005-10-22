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

import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.server.JaasLoginServiceMBean;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.client.JaasLoginCoordinator;


/**
 * Creates a LoginModule configuration that will connect a server-side
 * component to a security realm.  The same thing could be done with a
 * LoginModuleGBean and a DirectConfigurationEntry, but this method saves some
 * configuration effort.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ServerRealmConfigurationEntry implements ConfigurationEntryFactory {
    private final String applicationConfigName;
    private final String realmName;
    private final Kernel kernel;
    private final JaasLoginServiceMBean loginService;
    private boolean wrapPrincipals;

    public ServerRealmConfigurationEntry() {
        this.applicationConfigName = null;
        this.realmName = null;
        this.kernel = null;
        this.loginService = null;
    }

    public ServerRealmConfigurationEntry(String applicationConfigName, String realmName, Kernel kernel, JaasLoginServiceMBean loginService) {
        this.applicationConfigName = applicationConfigName;
        this.realmName = realmName;
        if (applicationConfigName == null || realmName == null) {
            throw new IllegalArgumentException("applicationConfigName and realmName are required");
        }
        if (applicationConfigName.equals(realmName)) {
            throw new IllegalArgumentException("applicationConfigName must be different than realmName (there's an automatic entry using the same name as the realm name, so you don't need a ServerRealmConfigurationEntry if you're just going to use that!)");
        }
        this.kernel = kernel;
        this.loginService = loginService;
    }

    public String getConfigurationName() {
        return applicationConfigName;
    }

    public boolean isWrapPrincipals() {
        return wrapPrincipals;
    }

    public void setWrapPrincipals(boolean wrapPrincipals) {
        this.wrapPrincipals = wrapPrincipals;
    }

    public JaasLoginModuleConfiguration generateConfiguration() {
        Properties options = new Properties();
        options.put(JaasLoginCoordinator.OPTION_REALM, realmName);
        options.put(JaasLoginCoordinator.OPTION_KERNEL, kernel.getKernelName());
        if (loginService != null) {
            options.put(JaasLoginCoordinator.OPTION_SERVICENAME, loginService.getObjectName());
        }

        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());

        return new JaasLoginModuleConfiguration(JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true, applicationConfigName, wrapPrincipals, JaasLoginCoordinator.class.getClassLoader());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServerRealmConfigurationEntry.class, NameFactory.CONFIGURATION_ENTRY);
        infoFactory.addInterface(ConfigurationEntryFactory.class);
        infoFactory.addAttribute("applicationConfigName", String.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("LoginService", JaasLoginServiceMBean.class, "JaasLoginService");
        infoFactory.addAttribute("wrapPrincipals", Boolean.TYPE, true);

        infoFactory.setConstructor(new String[]{"applicationConfigName", "realmName", "kernel", "LoginService"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
