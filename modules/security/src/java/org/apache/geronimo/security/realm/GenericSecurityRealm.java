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
import java.util.Collections;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.spi.LoginModule;
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
    private Map autoMapPrincipals = new HashMap();
    private Principal defaultPrincipal;
    private Properties deploymentSupport;
    private Map deployment;
    private String[] domains;
    private boolean restrictPrincipalsToServer;

    public GenericSecurityRealm(String realmName, Kernel kernel, ServerInfo serverInfo, Properties loginModuleConfiguration, ClassLoader classLoader) throws MalformedObjectNameException {
        this.realmName = realmName;
        this.kernel = kernel;
        this.serverInfo = serverInfo;
        this.classLoader = classLoader;
        processConfiguration(loginModuleConfiguration);
        initializeDeployment();
    }

    public String getRealmName() {
        return realmName;
    }

    public JaasLoginModuleConfiguration[] getAppConfigurationEntries() {
        return config;
    }

    /**
     * Gets a helper that lists principals for the realm to help with
     * generating deployment descriptors.  May return null if the realm does
     * not support these features.
     */
    public DeploymentSupport getDeploymentSupport(String domain) throws GeronimoSecurityException {
        return (DeploymentSupport) deployment.get(domain);
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

    public Properties getDeploymentSupport() {
        return deploymentSupport;
    }

    public void setDeploymentSupport(Properties deploymentSupport) {
        this.deploymentSupport = deploymentSupport;
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
    public Set obtainRolePrincipalClasses(String loginDomain) {
        String[] list = (String[]) autoMapPrincipals.get(loginDomain);
        if(list == null) {
            return Collections.EMPTY_SET;
        }
        Set set = new HashSet();
        for (int i = 0; i < list.length; i++) {
            set.add(list[i]);
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

    /**
     * Should be of the form loginDomain=class,class,class...
     */
    public void setAutoMapPrincipalClasses(Properties props) {
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            String value = props.getProperty(key);
            autoMapPrincipals.put(key, value.split(","));
        }
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

    public void setRestrictPrincipalsToServer(boolean restrictPrincipalsToServer) {
        this.restrictPrincipalsToServer = restrictPrincipalsToServer;
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
                    if(module.getLoginDomainName() != null) {
                        if(domains.contains(module.getLoginDomainName())) {
                            throw new IllegalStateException("Error in "+realmName+": one security realm cannot contain multiple login modules for the same login domain");
                        } else {
                            domains.add(module.getLoginDomainName());
                        }
                    }
                    JaasLoginModuleConfiguration config = new JaasLoginModuleConfiguration(module.getLoginModuleClass(), flag, options, module.isServerSide(), module.getLoginDomainName());
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
        this.domains = (String[]) domains.toArray(new String[domains.size()]);
        config = (JaasLoginModuleConfiguration[]) list.toArray(new JaasLoginModuleConfiguration[list.size()]);
    }

    private void initializeDeployment() {
        deployment = new HashMap();
        for (int i = 0; i < config.length; i++) {
            if(config[i].getLoginDomainName() == null) {
                continue;
            }
            DeploymentSupport support = null;
            if(deploymentSupport != null && deploymentSupport.containsKey(config[i].getLoginDomainName())) {
                try {
                    //todo: how should this be configured?  Should it be a GBean?
                    support = (DeploymentSupport) classLoader.loadClass(deploymentSupport.getProperty(config[i].getLoginDomainName())).newInstance();
                } catch (Exception e) {
                    throw new GeronimoSecurityException("Unable to load deployment support class '"+deploymentSupport.getProperty(config[i].getLoginDomainName())+"'", e);
                }
            } else if(config[i].getLoginModule(classLoader) instanceof DeploymentSupport) {
                LoginModule module = config[i].getLoginModule(classLoader);
                module.initialize(null, null, null, config[i].getOptions());
                support = (DeploymentSupport) module;
            }
            if(support != null) {
                deployment.put(config[i].getLoginDomainName(), support);
                String[] auto = support.getAutoMapPrincipalClassNames();
                if(auto != null) {
                    autoMapPrincipals.put(config[i].getLoginDomainName(), auto);
                }
            }
        }
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
        infoFactory.addAttribute("deploymentSupport", Properties.class, true);
        infoFactory.addAttribute("restrictPrincipalsToServer", boolean.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.addOperation("getAppConfigurationEntries", new Class[0]);
        infoFactory.addOperation("obtainDefaultPrincipal", new Class[0]);
        infoFactory.addOperation("obtainRolePrincipalClasses", new Class[]{String.class});
        infoFactory.addOperation("getDeploymentSupport", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"realmName", "kernel", "ServerInfo", "loginModuleConfiguration", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
