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

import java.security.AccessController;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.management.ObjectName;
import javax.security.auth.login.Configuration;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * An MBean that maintains a list of security realms.
 *
 * @version $Revision: 1.7 $ $Date: 2004/01/16 02:10:46 $
 */
public class SecurityService  {


    private final Log log = LogFactory.getLog(SecurityService.class);

    private String policyConfigurationFactory;
    private Collection realms = Collections.EMPTY_SET;
    private Collection moduleConfigurations = Collections.EMPTY_SET;


    /**
     * Permissions that protect access to sensitive security information
     */
    public static final GeronimoSecurityPermission CONFIGURE = new GeronimoSecurityPermission("configure");

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(SecurityService.class.getName());

        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getModuleConfiguration",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("contextID", String.class, ""),
                    new GeronimoParameterInfo("remove", Boolean.TYPE, "")},
                GeronimoOperationInfo.ACTION_INFO,
                "Get security configuration for module identified by contextID"));

        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PolicyConfigurationFactory",
                                                             true, true,
                                                             "The PolicyConfigurationFactory to use",
                                                             (Object)"org.apache.geronimo.security.GeronimoPolicyConfigurationFactory"));

        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("Realms", SecurityRealm.class, ObjectName.getInstance(SecurityRealm.BASE_OBJECT_NAME + ",*")));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("ModuleConfigurations", ModuleConfiguration.class, ObjectName.getInstance(AbstractModuleConfiguration.BASE_OBJECT_NAME + ",*")));

        return mbeanInfo;
    }


    public SecurityService() {
        AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        Configuration.setConfiguration(new GeronimoLoginConfiguration(SecurityService.this));
                        return null;
                    }
                });
        /**
         *  @see "JSR 115 4.6.1" Container Subject Policy Contact Handler
         */
        try {
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerContainerSubject(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerSOAPMessage(), true);
            ConfigurationUtil.registerPolicyContextHandler(new PolicyContextHandlerHttpServletRequest(), true);
        } catch (PolicyContextException pce) {
            log.error("Exception in doStart()", pce);

            throw (IllegalStateException)new IllegalStateException().initCause(pce);
        }
    }

    public String getPolicyConfigurationFactory() {
        return policyConfigurationFactory;
    }

    public void setPolicyConfigurationFactory(String policyConfigurationFactory) {
        this.policyConfigurationFactory = policyConfigurationFactory;

        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", policyConfigurationFactory);
    }

    /**
     *
     * This was once a managed operation...only used by GeronimoLoginConfiguration, which uses it directly.
     * Return type was a Set.  Changed to collection to work with GeronimoMBeanEndpoints.
     * @return
     * @throws GeronimoSecurityException
     */
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

    /**
     * <p>This method is used to obtain a web module configuration that corresponds to the identified policy context.
     * The methods of the <code>WebModuleConfigurationMBean</code> class are used to map deployment descriptor
     * information into policy statements needed by the identified policy context as well as the principal to role
     * mapping.</p>
     *
     * <p>If at the time of the call, the identified web module configuration does not exist, then the web module
     * configuration will be created and the JMX MBean that implements the context's
     * <code>WebModuleConfigurationMBean</code> interface will be returned. If the state of the identified context is
     * "deleted" or "inService" it will be transitioned to the "open" state as a result of the call. The states in the
     * lifecycle of a policy context are defined by the <code>WebModuleConfigurationMBean</code> interface.</p>
     *
     * <p>For a given value of policy context identifier, this method must always return the same instance of
     * <code>WebModuleConfigurationMBean</code> and there must be at most one actual instance of a
     * <code>WebModuleConfigurationMBean</code> with a given policy context identifier (during a process context).</p>
     *
     * <p>To preserve the invariant that there be at most one <code>WebModuleConfigurationMBean</code> object for a
     * given policy context, it may be necessary for this method to be thread safe.</p>
     *
     * @param contextID A String identifying the web module configuration to be returned. The value passed to this
     *                  parameter must not be null.
     * @param remove A boolean value that establishes whether or not the security configuration of an existing web
     *                  module is to be removed before its <code>WebModuleConfigurationMBean</code> object is returned.
     *                  If the value passed to this parameter is <code>true</code> the security configuration of an
     *                  existing web module will be removed. If the value is <code>false</code>, it will not be removed.
     * @return an MBean that implements the <code>WebModuleConfigurationMBean</code> Interface matched to the
     *                  identified policy context.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     *                  the <code>getModuleConfiguration</code> method signature.
     */
    public ModuleConfiguration getModuleConfiguration(String contextID, boolean remove) throws GeronimoSecurityException {
        assert contextID != null : "ContextID must be supplied!";
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CONFIGURE);
        }

        for (Iterator iterator = moduleConfigurations.iterator(); iterator.hasNext();) {
            ModuleConfiguration moduleConfiguration = (ModuleConfiguration) iterator.next();
            if (contextID.equals(moduleConfiguration.getContextID())) {
                if (remove) {
                    moduleConfiguration.delete();
                }
                return moduleConfiguration;
            }
        }
        return null;
    }

}
