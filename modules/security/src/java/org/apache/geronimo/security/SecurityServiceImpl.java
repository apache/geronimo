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

package org.apache.geronimo.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.security.Policy;
import javax.management.ObjectName;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyConfigurationFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jacc.ModuleConfiguration;
import org.apache.geronimo.security.jacc.PolicyContextHandlerContainerSubject;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.geronimo.security.jacc.PolicyContextHandlerSOAPMessage;
import org.apache.geronimo.security.jacc.GeronimoPolicy;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.realm.AutoMapAssistant;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl implements SecurityServiceMBean, GBeanLifecycle {
    /**
     * The JMX name of the SecurityServiceImpl.
     */
    public static final ObjectName SECURITY = JMXUtil.getObjectName("geronimo.security:type=SecurityServiceImpl");

    private final Log log = LogFactory.getLog(SecurityServiceMBean.class);

    private String policyConfigurationFactory;
    private Collection realms = Collections.EMPTY_SET;
    private Collection mappers = Collections.EMPTY_SET;
    private Collection moduleConfigurations = Collections.EMPTY_SET;


    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    //deprecated, for geronimo mbean only
    public SecurityServiceImpl() {
        this(null);
    }


    public SecurityServiceImpl(String policyConfigurationFactory) {
        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Contact Handler
         */
        try {
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);
        } catch (PolicyContextException pce) {
            log.error("Exception in doStart()", pce);

            throw (IllegalStateException) new IllegalStateException().initCause(pce);
        }
        
        this.policyConfigurationFactory = policyConfigurationFactory;
        //TODO remove this if wrapper when GeronimoMBean leaves.
        if (policyConfigurationFactory != null) {
            System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", policyConfigurationFactory);
        }
    }

    public String getPolicyConfigurationFactory() {
        return policyConfigurationFactory;
    }

    public void setPolicyConfigurationFactory(String policyConfigurationFactory) {
        this.policyConfigurationFactory = policyConfigurationFactory;
        //TODO remove this if wrapper when GeronimoMBean leaves.
        if (policyConfigurationFactory != null) {
            System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", policyConfigurationFactory);
        }
    }

    public Collection getRealms() throws GeronimoSecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);
        return realms;
    }


    public void setRealms(Collection realms) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);
        this.realms = realms;
    }

    public Collection getMappers() throws GeronimoSecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);
        return mappers;
    }


    public void setMappers(Collection mappers) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(CONFIGURE);
        this.mappers = mappers;
    }

    public Collection getModuleConfigurations() {
        return moduleConfigurations;
    }

    public void setModuleConfigurations(Collection moduleConfigurations) {
        this.moduleConfigurations = moduleConfigurations;
    }

    public SecurityRealm getRealm(String name) {
        for (Iterator iter = realms.iterator(); iter.hasNext();) {
            SecurityRealm realm = (SecurityRealm) iter.next();
            if (name.equals(realm.getRealmName())) {
                return realm;
            }
        }
        return null;
    }

    public AutoMapAssistant getMapper(String name) {
        for (Iterator iter = mappers.iterator(); iter.hasNext();) {
            AutoMapAssistant mapper = (AutoMapAssistant) iter.next();
            if (name.equals(mapper.getRealmName())) {
                return mapper;
            }
        }
        return null;
    }

    public void doStart() throws WaitingException, Exception {
        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        Policy.setPolicy(new GeronimoPolicy(factory));

        log.info("Security service started");
    }

    public void doStop() throws WaitingException, Exception {
        log.info("Security service stopped");
    }

    public void doFail() {
        log.info("Security service failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SecurityServiceImpl.class);

        infoFactory.addAttribute("policyConfigurationFactory", String.class, true);

        infoFactory.addReference("Realms", SecurityRealm.class);
        infoFactory.addReference("Mappers", AutoMapAssistant.class);
        infoFactory.addReference("ModuleConfigurations", ModuleConfiguration.class);
        infoFactory.addOperation("getRealm", new Class[]{String.class});
        infoFactory.addOperation("getMapper", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"policyConfigurationFactory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
