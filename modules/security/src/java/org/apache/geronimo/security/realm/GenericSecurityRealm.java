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
package org.apache.geronimo.security.realm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.JaasLoginCoordinator;
import org.apache.geronimo.security.jaas.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.JaasLoginServiceMBean;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * A security realm that can be configured for one or more login modules.  It
 * can handle a combination of client-side and server-side login modules for
 * the case of remote clients, and it can auto-role-mapping for its login
 * modules (though you must configure it for that).
 * <p/>
 * To configure the list of LoginModules, set the loginModuleConfiguration
 * to a Properties object with syntax like this:
 * <pre>
 * LoginModule.1.REQUIRED=ObjectName1
 * LoginModule.2.SUFFICIENT=ObjectName2
 * ...
 * </pre>
 * Each ObjectName should identify a LoginModuleGBean in the server
 * configuration.  Each LoginModuleGBean has the configuration options for its
 * login module, and knows whether it should run on the client side or server
 * side.
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
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class GenericSecurityRealm implements SecurityRealm, ConfigurationEntryFactory {

    private final JaasLoginServiceMBean loginService;
    private final String realmName;
    private JaasLoginModuleConfiguration[] config;
    private final Kernel kernel;

    private final Principal defaultPrincipal;

    private String[] domains;
    private boolean restrictPrincipalsToServer;


    public GenericSecurityRealm(String realmName,
                                JaasLoginModuleUse loginModuleUse,
                                boolean restrictPrincipalsToServer,
                                Principal defaultPrincipal,
                                ServerInfo serverInfo,
                                ClassLoader classLoader,
                                Kernel kernel,
                                JaasLoginServiceMBean loginService) {
        this.realmName = realmName;
        this.kernel = kernel;
        this.restrictPrincipalsToServer = restrictPrincipalsToServer;
        this.defaultPrincipal = defaultPrincipal;
        this.loginService = loginService;

        Set domainNames = new HashSet();
        List loginModuleConfigurations = new ArrayList();

        if (loginModuleUse != null) {
            loginModuleUse.configure(domainNames,  loginModuleConfigurations,  kernel, serverInfo, classLoader);
        }

        domains = (String[]) domainNames.toArray(new String[domainNames.size()]);
        config = (JaasLoginModuleConfiguration[]) loginModuleConfigurations.toArray(new JaasLoginModuleConfiguration[loginModuleConfigurations.size()]);

    }

    public String getRealmName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration[] getAppConfigurationEntries() {
        return config;
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
     * Provides the default principal to be used when an unauthenticated
     * subject uses a container.
     *
     * @return the default principal
     */
    public Principal getDefaultPrincipal() {
        return defaultPrincipal;
    }

    /**
     * A GBean property.  If set to true, the login service will not return
     * principals generated by this realm to clients.  If set to false (the
     * default), the client will get a copy of all principals (except realm
     * principals generated strictly for use within Geronimo).
     */
    public boolean isRestrictPrincipalsToServer() {
        return restrictPrincipalsToServer;
    }

    public String getConfigurationName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration generateConfiguration() {
        Map options = new HashMap();
        options.put(JaasLoginCoordinator.OPTION_REALM, realmName);
        options.put(JaasLoginCoordinator.OPTION_KERNEL, kernel.getKernelName());
        if (loginService != null) {
            options.put(JaasLoginCoordinator.OPTION_SERVICENAME, loginService.getObjectName());
        }

        return new JaasLoginModuleConfiguration(JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true, realmName);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GenericSecurityRealm.class, NameFactory.SECURITY_REALM);

        infoFactory.addInterface(SecurityRealm.class);
        infoFactory.addInterface(ConfigurationEntryFactory.class);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("defaultPrincipal", Principal.class, true);
        infoFactory.addAttribute("deploymentSupport", Properties.class, true);
        infoFactory.addAttribute("restrictPrincipalsToServer", boolean.class, true);

        infoFactory.addReference("LoginModuleConfiguration", JaasLoginModuleUse.class, "LoginModuleUse");
        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("LoginService", JaasLoginServiceMBean.class, "JaasLoginService");

        infoFactory.addOperation("getAppConfigurationEntries", new Class[0]);

        infoFactory.setConstructor(new String[]{"realmName",
                                                "LoginModuleConfiguration",
                                                "restrictPrincipalsToServer",
                                                "defaultPrincipal",
                                                "ServerInfo",
                                                "classLoader",
                                                "kernel",
                                                "LoginService"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
