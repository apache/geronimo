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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;


/**
 * Holds a reference to a login module and the control flag.  A linked list of these forms the list of login modules
 * in a GenericSecurityRealm.
 *
 * @version $Rev:  $ $Date:  $
 */
public class JaasLoginModuleUse {
    public final static String KERNEL_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.KERNEL";
    public final static String SERVERINFO_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.SERVERINFO";
    public final static String CLASSLOADER_LM_OPTION = "org.apache.geronimo.security.realm.GenericSecurityRealm.CLASSLOADER";

    private final LoginModuleGBean loginModule;
    private final JaasLoginModuleUse next;
    private final LoginModuleControlFlag controlFlag;

    //for reference.
    public JaasLoginModuleUse() {
        loginModule = null;
        next = null;
        controlFlag = null;
    }

    public JaasLoginModuleUse(LoginModuleGBean loginModule, JaasLoginModuleUse next, String controlFlag) {
        this.loginModule = loginModule;
        this.next = next;
        LoginModuleControlFlagEditor editor = new LoginModuleControlFlagEditor();
        editor.setAsText(controlFlag);
        this.controlFlag = (LoginModuleControlFlag) editor.getValue();
    }

    public LoginModuleGBean getLoginModule() {
        return loginModule;
    }

    public JaasLoginModuleUse getNext() {
        return next;
    }

    public String getControlFlag() {
        return controlFlag.toString();
    }

    public void configure(Set domainNames, List loginModuleConfigurations, Kernel kernel, ServerInfo serverInfo, ClassLoader classLoader) {
        Map options = loginModule.getOptions();
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
        if (loginModule.getLoginDomainName() != null) {
            if (domainNames.contains(loginModule.getLoginDomainName())) {
                throw new IllegalStateException("Error in realm: one security realm cannot contain multiple login modules for the same login domain");
            } else {
                domainNames.add(loginModule.getLoginDomainName());
            }
        }
        JaasLoginModuleConfiguration config = new JaasLoginModuleConfiguration(loginModule.getLoginModuleClass(), controlFlag, options, loginModule.isServerSide(), loginModule.getLoginDomainName(), loginModule.isWrapPrincipals());
        loginModuleConfigurations.add(config);

        if (next != null) {
            next.configure(domainNames, loginModuleConfigurations, kernel, serverInfo, classLoader);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JaasLoginModuleUse.class, "LoginModuleUse");
        infoBuilder.addAttribute("controlFlag", String.class, true);
        infoBuilder.addReference("LoginModule", LoginModuleGBean.class, NameFactory.LOGIN_MODULE);
        infoBuilder.addReference("Next", JaasLoginModuleUse.class);

        infoBuilder.addOperation("configure", new Class[]{Set.class, List.class, Kernel.class, ServerInfo.class, ClassLoader.class});

        infoBuilder.setConstructor(new String[]{"LoginModule", "Next", "controlFlag"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
