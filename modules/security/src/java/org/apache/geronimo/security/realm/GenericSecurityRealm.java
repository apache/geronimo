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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.regexp.RE;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
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
public class GenericSecurityRealm implements SecurityRealm, ConfigurationEntryFactory, AutoMapAssistant {

    public final static String KERNEL_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.KERNEL";
    public final static String SERVERINFO_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.SERVERINFO";
    public final static String CLASSLOADER_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.CLASSLOADER";
    private String realmName;
    private JaasLoginModuleConfiguration[] config;
    private Kernel kernel;
    private ServerInfo serverInfo;
    private ClassLoader classLoader;
    private String[] autoMapPrincipals;
    private Principal defaultPrincipal;

    public GenericSecurityRealm(String realmName, Kernel kernel, ServerInfo serverInfo, Properties loginModuleConfiguration, ClassLoader classLoader) throws MalformedObjectNameException {
        this.realmName = realmName;
        this.kernel = kernel;
        this.serverInfo = serverInfo;
        this.classLoader = classLoader;
        processConfiguration(loginModuleConfiguration);
    }

    public String getRealmName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration[] getAppConfigurationEntries() {
        return config;
    }

    /**
     * Provides the default principal to be used when an unauthenticated
     * subject uses a container.
     *
     * @return the default principal
     */
    public Principal obtainDefaultPrincipal() {
        return defaultPrincipal;
    }

    /**
     * Provides a set of principal class names to be used when automatically
     * mapping principals to roles.
     *
     * @return a set of principal class names
     */
    public Set obtainRolePrincipalClasses() {
        Set set = new HashSet();
        for (int i = 0; i < autoMapPrincipals.length; i++) {
            set.add(autoMapPrincipals[i]);
        }
        return set;
    }

    public void setDefaultPrincipal(String code) {
        if (code != null) {
            String[] parts = code.split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Default Principal should have the form 'name=class'");
            }
            defaultPrincipal = new Principal();
            defaultPrincipal.setPrincipalName(parts[0]);
            defaultPrincipal.setClassName(parts[1]);
        }
    }

    public void setAutoMapPrincipalClasses(String classes) {
        if (classes != null) {
            autoMapPrincipals = classes.split(",");
        } else {
            autoMapPrincipals = new String[0];
        }
    }

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getGroupPrincipals() throws GeronimoSecurityException {
        return null; //todo
    }

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null; //todo
    }

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getUserPrincipals() throws GeronimoSecurityException {
        return null; //todo
    }

    /**
     * @deprecated Will be removed in favor of (some kind of realm editor object) in
     *             a future milestone release.
     */
    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null; //todo
    }

    public String getConfigurationName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration generateConfiguration() {
        Map options = new HashMap();
        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());

        return new JaasLoginModuleConfiguration(realmName, JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true);
    }

    private void processConfiguration(Properties props) throws MalformedObjectNameException {
        int i = 1;
        List list = new ArrayList();
        LoginModuleControlFlagEditor editor = new LoginModuleControlFlagEditor();
        while (true) {
            boolean found = false;
            String prefix = "LoginModule." + i + ".";
            for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
                String key = (String) en.nextElement();
                if (key.startsWith(prefix)) {
                    String flagName = key.substring(prefix.length()).toUpperCase();
                    editor.setAsText(flagName);
                    LoginModuleControlFlag flag = (LoginModuleControlFlag) editor.getValue();
                    LoginModuleGBean module = (LoginModuleGBean) MBeanProxyFactory.getProxy(LoginModuleGBean.class, kernel.getMBeanServer(), new ObjectName(props.getProperty(key)));
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
                    JaasLoginModuleConfiguration config = new JaasLoginModuleConfiguration(module.getObjectName(), module.getLoginModuleClass(), flag, options, module.isServerSide());
                    list.add(config);
                    ++i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        config = (JaasLoginModuleConfiguration[]) list.toArray(new JaasLoginModuleConfiguration[list.size()]);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GenericSecurityRealm.class);

        infoFactory.addInterface(SecurityRealm.class);
        infoFactory.addInterface(ConfigurationEntryFactory.class);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("loginModuleConfiguration", Properties.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("autoMapPrincipalClasses", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", String.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.addOperation("getAppConfigurationEntries", new Class[0]);
        infoFactory.addOperation("obtainDefaultPrincipal", new Class[0]);
        infoFactory.addOperation("obtainRolePrincipalClasses", new Class[0]);

        infoFactory.setConstructor(new String[]{"realmName", "kernel", "ServerInfo", "loginModuleConfiguration", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
