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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.JaasLoginCoordinator;
import org.apache.geronimo.security.jaas.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginModuleControlFlagEditor;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
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
 * GenericSecurityRealm.KERNEL_LM_OPTION       String (Kernel name)        Yes
 * GenericSecurityRealm.SERVERINFO_LM_OPTION   ServerInfo                  No
 * GenericSecurityRealm.CLASSLOADER_LM_OPTION  ClassLoader                 No
 * </pre>
 * These options can be safely ignored by login modules that don't need them
 * (such as any custom LoginModules you may already have lying around).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class GenericSecurityRealm implements SecurityRealm, ConfigurationEntryFactory {

    public final static String KERNEL_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.KERNEL";
    public final static String SERVERINFO_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.SERVERINFO";
    public final static String CLASSLOADER_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.CLASSLOADER";
    private final String realmName;
    private JaasLoginModuleConfiguration[] config;
    private final Kernel kernel;
    private final ServerInfo serverInfo;
    private final ClassLoader classLoader;

    private final Principal defaultPrincipal;

    private String[] domains;
    private boolean restrictPrincipalsToServer;

    public GenericSecurityRealm(String realmName,
                                Properties loginModuleConfiguration,
                                boolean restrictPrincipalsToServer,
                                Principal defaultPrincipal,
                                ServerInfo serverInfo,
                                ClassLoader classLoader,
                                Kernel kernel) throws MalformedObjectNameException {
        this.realmName = realmName;
        this.kernel = kernel;
        this.serverInfo = serverInfo;
        this.classLoader = classLoader;
        this.restrictPrincipalsToServer = restrictPrincipalsToServer;
        this.defaultPrincipal = defaultPrincipal;

        processConfiguration(loginModuleConfiguration);
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
        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());

        return new JaasLoginModuleConfiguration(JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true, realmName);
    }

    private void processConfiguration(Properties props) throws MalformedObjectNameException {
        int i = 1;
        Set domains = new HashSet();
        List list = new ArrayList();
        LoginModuleControlFlagEditor editor = new LoginModuleControlFlagEditor();
        ProxyManager proxyManager = kernel.getProxyManager();
        while (true) {
            boolean found = false;
            String prefix = "LoginModule." + i + ".";
            for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                if (key.startsWith(prefix)) {
                    String flagName = key.substring(prefix.length()).toUpperCase();
                    editor.setAsText(flagName);
                    LoginModuleControlFlag flag = (LoginModuleControlFlag) editor.getValue();
                    LoginModuleGBean module = null;
                    try {
                        module = (LoginModuleGBean) proxyManager.createProxy(new ObjectName(props.getProperty(key)), LoginModuleGBean.class);
                        Map options = module.getOptions();
                        if (options != null) {
                            options = new HashMap(options);
                        } else {
                            options = new HashMap();
                        }
                        if (kernel != null && !options.containsKey(KERNEL_LM_OPTION)) {
                            options.put(KERNEL_LM_OPTION, kernel.getKernelName());
                        }
                        if (serverInfo != null && !options.containsKey(SERVERINFO_LM_OPTION)) {
                            options.put(SERVERINFO_LM_OPTION, serverInfo);
                        }
                        if (classLoader != null && !options.containsKey(CLASSLOADER_LM_OPTION)) {
                            options.put(CLASSLOADER_LM_OPTION, classLoader);
                        }
                        if (module.getLoginDomainName() != null) {
                            if (domains.contains(module.getLoginDomainName())) {
                                throw new IllegalStateException("Error in " + realmName + ": one security realm cannot contain multiple login modules for the same login domain");
                            } else {
                                domains.add(module.getLoginDomainName());
                            }
                        }
                        JaasLoginModuleConfiguration config = new JaasLoginModuleConfiguration(module.getLoginModuleClass(), flag, options, module.isServerSide(), module.getLoginDomainName());
                        list.add(config);
                    } finally {
                        proxyManager.destroyProxy(module);
                    }
                    ++i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        this.domains = (String[]) domains.toArray(new String[domains.size()]);
        config = (JaasLoginModuleConfiguration[]) list.toArray(new JaasLoginModuleConfiguration[list.size()]);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GenericSecurityRealm.class, NameFactory.SECURITY_REALM);

        infoFactory.addInterface(SecurityRealm.class);
        infoFactory.addInterface(ConfigurationEntryFactory.class);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("loginModuleConfiguration", Properties.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("defaultPrincipal", Principal.class, true);
        infoFactory.addAttribute("deploymentSupport", Properties.class, true);
        infoFactory.addAttribute("restrictPrincipalsToServer", boolean.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addOperation("getAppConfigurationEntries", new Class[0]);

        infoFactory.setConstructor(new String[]{"realmName",
                                                "loginModuleConfiguration",
                                                "restrictPrincipalsToServer",
                                                "defaultPrincipal",
                                                "ServerInfo",
                                                "classLoader",
                                                "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
