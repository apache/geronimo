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

import javax.management.ObjectName;
import javax.security.jacc.PolicyContextException;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.security.jacc.ModuleConfiguration;
import org.apache.geronimo.security.jacc.PolicyContextHandlerContainerSubject;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.apache.geronimo.security.jacc.PolicyContextHandlerSOAPMessage;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:25 $
 */
public class SecurityService implements SecurityServiceMBean, GBean {

    private static final GBeanInfo GBEAN_INFO;

    /**
     * The JMX name of the SecurityService.
     */
    public static final ObjectName SECURITY = JMXUtil.getObjectName("geronimo.security:type=SecurityService");

    private final Log log = LogFactory.getLog(SecurityServiceMBean.class);

    private String policyConfigurationFactory;
    private Collection realms = Collections.EMPTY_SET;
    private Collection moduleConfigurations = Collections.EMPTY_SET;


    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    //deprecated, for geronimo mbean only
    public SecurityService() {
        this(null);
    }


    public SecurityService(String policyConfigurationFactory) {
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
        setPolicyConfigurationFactory(policyConfigurationFactory);
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

    public Collection getModuleConfigurations() {
        return moduleConfigurations;
    }

    public void setModuleConfigurations(Collection moduleConfigurations) {
        this.moduleConfigurations = moduleConfigurations;
    }


    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SecurityService.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("PolicyConfigurationFactory", true));
        infoFactory.addReference(new GReferenceInfo("Realms", SecurityRealm.class.getName()));
        infoFactory.addReference(new GReferenceInfo("ModuleConfigurations", ModuleConfiguration.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(new String[]{"PolicyConfigurationFactory"},
                                                        new Class[]{String.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
