/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.security;

import java.security.Policy;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.jacc.GeronimoPolicy;
import org.apache.geronimo.security.jacc.PolicyContextHandlerContainerSubject;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.geronimo.security.jacc.PolicyContextHandlerSOAPMessage;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * An MBean that registers the JACC factory and handlers.
 *
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl {

    private final Log log = LogFactory.getLog(SecurityServiceImpl.class);

    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    public SecurityServiceImpl(ClassLoader classLoader, String policyConfigurationFactory, String policyProvider) throws PolicyContextException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Context Handler
         */
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);

        if (policyConfigurationFactory != null) {
            System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", policyConfigurationFactory);
        }

        PolicyConfigurationFactory.getPolicyConfigurationFactory();

        if (policyProvider != null) {
            Policy customPolicy = (Policy) classLoader.loadClass(policyProvider).newInstance();
            Policy.setPolicy(customPolicy);
        } else {
            Policy.setPolicy(new GeronimoPolicy());
        }

        log.info("JACC factory registered");
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SecurityServiceImpl.class);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("policyConfigurationFactory", String.class, true);
        infoFactory.addAttribute("policyProvider", String.class, true);

        infoFactory.setConstructor(new String[]{"classLoader", "policyConfigurationFactory", "policyProvider"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
