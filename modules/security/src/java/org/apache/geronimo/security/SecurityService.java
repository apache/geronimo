/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.3 $ $Date: 2004/02/17 00:05:39 $
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
