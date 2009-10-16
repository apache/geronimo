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

package org.apache.geronimo.security;

import java.security.Policy;

import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.security.jacc.PolicyContextHandlerContainerSubject;
import org.apache.geronimo.security.jacc.PolicyContextHandlerEjbArguments;
import org.apache.geronimo.security.jacc.PolicyContextHandlerEnterpriseBean;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.geronimo.security.jacc.PolicyContextHandlerSOAPMessage;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An MBean that registers the JACC factory and handlers.
 *
 * @version $Rev$ $Date$
 */

@GBean
public class SecurityServiceImpl implements SecurityService {

    public static boolean POLICY_INSTALLED = false;

    private static final Logger log = LoggerFactory.getLogger(SecurityServiceImpl.class);

    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    public SecurityServiceImpl(@ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                               @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                               @ParamAttribute(name = "policyConfigurationFactory") String policyConfigurationFactory,
                               @ParamAttribute(name = "policyProvider") String policyProvider,
                               @ParamAttribute(name = "keyStore") String keyStore,
                               @ParamAttribute(name = "keyStorePassword") String keyStorePassword,
                               @ParamAttribute(name = "trustStore") String trustStore,
                               @ParamAttribute(name = "trustStorePassword") String trustStorePassword)
            throws PolicyContextException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Context Handler
         */
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerEnterpriseBean(), true);
        ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerEjbArguments(), true);

        if (!POLICY_INSTALLED) {
            policyProvider = sysOverRide(policyProvider, POLICY_PROVIDER);

            if (policyProvider != null) {
                Policy policy = (Policy) classLoader.loadClass(policyProvider).newInstance();
                policy.refresh();
                Policy.setPolicy(policy);
            }

            POLICY_INSTALLED = true;
        }

        policyConfigurationFactory = sysOverRide(policyConfigurationFactory, POLICY_CONFIG_FACTORY);
        if (policyConfigurationFactory != null) {
            Thread currentThread = Thread.currentThread();
            ClassLoader oldClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(classLoader);
            try {
                PolicyConfigurationFactory.getPolicyConfigurationFactory();
            } finally {
                currentThread.setContextClassLoader(oldClassLoader);
            }
        }
        if (keyStore != null) sysOverRide(serverInfo.resolveServerPath(keyStore), KEYSTORE);
        if (keyStorePassword != null) sysOverRide(keyStorePassword, KEYSTORE_PASSWORD);

        if (trustStore != null) sysOverRide(serverInfo.resolveServerPath(trustStore), TRUSTSTORE);
        if (trustStorePassword != null) sysOverRide(trustStorePassword, TRUSTSTORE_PASSWORD);

        log.debug(KEYSTORE + ": " + System.getProperty(KEYSTORE));
        log.debug(TRUSTSTORE + ": " + System.getProperty(TRUSTSTORE));

        log.debug("JACC factory registered");
    }

    private String sysOverRide(String attribute, String sysVar) {

        String sysValue = System.getProperty(sysVar);

        /**
         * System variable gets highest priority
         */
        if (sysValue != null)
            return sysValue;

        if (attribute != null) {
            System.setProperty(sysVar, attribute);
        }

        return attribute;

    }

}
