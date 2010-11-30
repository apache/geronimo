/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.security.SecurityNames;
import org.osgi.framework.Bundle;


/**
 * Holds a reference to a login module and the control flag.  A linked list of these forms the list of login modules
 * in a GenericSecurityRealm.
 *
 * @version $Rev$ $Date$
 */
public class JaasLoginModuleUse implements JaasLoginModuleChain {
    // See also http://java.sun.com/j2se/1.4.2/docs/guide/security/jaas/JAASLMDevGuide.html for more standard login module option keys
    public final static String KERNEL_NAME_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.KERNEL";
    public final static String SERVERINFO_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.SERVERINFO";
    public final static String CLASSLOADER_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.CLASSLOADER";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(KERNEL_NAME_LM_OPTION, SERVERINFO_LM_OPTION, CLASSLOADER_LM_OPTION));

    private final LoginModuleSettings loginModule;
    private final JaasLoginModuleUse next;
    private LoginModuleControlFlag controlFlag;

    //for reference.
    public JaasLoginModuleUse() {
        loginModule = null;
        next = null;
        controlFlag = null;
    }

    public JaasLoginModuleUse(LoginModuleSettings loginModule, JaasLoginModuleUse next, LoginModuleControlFlag controlFlag) {
        this.loginModule = loginModule;
        this.next = next;
        this.controlFlag = controlFlag;
    }

    public LoginModuleSettings getLoginModule() {
        return loginModule;
    }

    public JaasLoginModuleChain getNext() {
        return next;
    }

    public LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(LoginModuleControlFlag controlFlag) {
        this.controlFlag = controlFlag;
    }

    public void configure(Set<String> domainNames, List<AppConfigurationEntry> loginModuleConfigurations, String realmName, Kernel kernel, ServerInfo serverInfo, Bundle bundle) throws ClassNotFoundException {
        Map<String, ?> suppliedOptions = loginModule.getOptions();
        Map<String, Object> options;
        if (suppliedOptions != null) {
            options = new HashMap<String, Object>(suppliedOptions);
        } else {
            options = new HashMap<String, Object>();
        }
        if (kernel != null && !options.containsKey(KERNEL_NAME_LM_OPTION)) {
            options.put(KERNEL_NAME_LM_OPTION, kernel.getKernelName());
        }
        if (serverInfo != null && !options.containsKey(SERVERINFO_LM_OPTION)) {
            options.put(SERVERINFO_LM_OPTION, serverInfo);
        }
        if (!options.containsKey(CLASSLOADER_LM_OPTION)) {
            options.put(CLASSLOADER_LM_OPTION, bundle);
        }
        AppConfigurationEntry entry;
        if (loginModule.isWrapPrincipals()) {
            Class loginModuleClass = bundle.loadClass(loginModule.getLoginModuleClass());
            options.put(WrappingLoginModule.CLASS_OPTION, loginModuleClass);
            options.put(WrappingLoginModule.DOMAIN_OPTION, loginModule.getLoginDomainName());
            options.put(WrappingLoginModule.REALM_OPTION, realmName);
            options.put(ProxyLoginModule.PROPERTY_MODULE, WrappingLoginModule.class.getName());
        } else {
            options.put(ProxyLoginModule.PROPERTY_MODULE, loginModule.getLoginModuleClass());
        }
        options.put(ProxyLoginModule.PROPERTY_BUNDLE, Long.toString(bundle.getBundleId()));
        entry = new AppConfigurationEntry(ProxyLoginModule.class.getName(), controlFlag.getFlag(), options);
        if (loginModule.getLoginDomainName() != null) {
            if (domainNames.contains(loginModule.getLoginDomainName())) {
                throw new IllegalStateException("Error in realm: one security realm cannot contain multiple login modules for the same login domain");
            } else {
                domainNames.add(loginModule.getLoginDomainName());
            }
        }
        loginModuleConfigurations.add(entry);

        if (next != null) {
            next.configure(domainNames, loginModuleConfigurations, realmName, kernel, serverInfo, bundle);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JaasLoginModuleUse.class, SecurityNames.LOGIN_MODULE_USE);
        infoBuilder.addAttribute("controlFlag", LoginModuleControlFlag.class, true);
        infoBuilder.addReference("LoginModule", LoginModuleSettings.class, SecurityNames.LOGIN_MODULE);
        infoBuilder.addReference("Next", JaasLoginModuleUse.class);

        infoBuilder.addInterface(JaasLoginModuleChain.class);
        infoBuilder.setConstructor(new String[]{"LoginModule", "Next", "controlFlag"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
