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

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import java.util.Collection;
import java.util.HashSet;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/18 05:17:17 $
 */
public abstract class AbstractModuleConfiguration implements ModuleConfigurationMBean {
    private String contextId;
    private GeronimoMBeanContext context;
    private MBeanServer server;
    private ObjectName objectName;
    private PolicyConfigurationFactory factory;
    private PolicyConfiguration policyConfiguration;
    private boolean configured = false;
    private HashSet roleNames = new HashSet();

    public AbstractModuleConfiguration(String contextId, String objectName) throws GeronimoSecurityException {
        this.contextId = contextId;
        this.objectName = JMXUtil.getObjectName(objectName);

        try {
            factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
            policyConfiguration = factory.getPolicyConfiguration(contextId, false);
        } catch (ClassNotFoundException e) {
            throw new GeronimoSecurityException("Unable to find PolicyConfigurationFactory", e);
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException("Unable to find policy configuration with that id", e);
        }
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        server = mBeanServer;

        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public void doStart() {

    }

    public void doStop() {

    }

    public void doFail() {

    }

    public boolean canStart() {
        return true;
    }

    public boolean canStop() {
        return true;
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    /**
     * This method returns this MBean's object name.
     * @return this MBean's object name.
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     * This method returns this object's policy context identifier.
     * @return this module's policy context identifier.
     */
    public String getContextID() {
        return contextId;
    }

    /**
     * Used to notify the abstract base class that the concrete implementation has completed adding all the role names.
     * @param configured the state of the configuration
     */
    protected void setConfigured(boolean configured) {
        this.configured = configured;
    }

    /**
     * This method returns the policy configuration that this bean is configuring.
     * @return this object's policy configuration, <code>PolicyConfiguration</code>.
     */
    protected PolicyConfiguration getPolicyConfiguration() {
        return policyConfiguration;
    }

    /**
     * This method returns the module's set of roles.
     * @return the set of roles that are being used for this module.
     */
    public HashSet getRoles() {
        return roleNames;
    }

    /**
     * Add a mapping from a module's security roles to physical principals.  Mapping principals to the same role twice
     * will cause a <code>PolicyContextException</code> to be thrown.
     * @param role The role that is to be mapped to a set of principals.
     * @param principals The set of principals that are to be mapped to to role.
     * @throws GeronimoSecurityException if the mapping principals to the same role twice occurs.
     */
    public void addRollMapping(String role, Collection principals) throws GeronimoSecurityException {
        if (!configured) throw new GeronimoSecurityException("Must call configure() first");

        try {
            RoleMappingConfiguration roleMapper = (RoleMappingConfiguration) policyConfiguration;

            if (!roleNames.contains(role)) throw new GeronimoSecurityException("Role does not exist in this configuration");

            roleMapper.addRoleMapping(role, principals);
        } catch (ClassCastException cce) {
            throw new GeronimoSecurityException("Policy configuration object does not implement RoleMappingConfiguration", cce.getCause());
        } catch (PolicyContextException pe) {
            throw new GeronimoSecurityException("Method addRoleMapping threw an exception", pe.getCause());
        }
    }

    /**
     * <p>Creates a relationship between this configuration and another such that they share the same principal-to-role
     * mappings. <code>PolicyConfigurations</code> are linked to apply a common principal-to-role mapping to multiple
     * seperately manageable <code>PolicyConfigurations</code>, as is required when an application is composed of
     * multiple modules.</p>
     *
     * <p>Note that the policy statements which comprise a role, or comprise the excluded or unchecked policy
     * collections in a <code>PolicyConfiguration</code> are unaffected by the configuration being linked to
     * another.</p>
     * @param link a reference to a different PolicyConfiguration than this <code>PolicyConfiguration</code>.
     * <p>The relationship formed by this method is symetric, transitive and idempotent. If the argument
     * <code>PolicyConfiguration</code> does not have a different Policy context identifier than this
     * <code>PolicyConfiguration</code> no relationship is formed, and an exception, as described below, is thrown.
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * <code>EjbModuleConfigurationMBean</code> Object is "deleted" or "inService" when this method is called.
     * @throws java.lang.IllegalArgumentException if called with an argument <code>EjbModuleConfigurationMBean</code>
     * whose Policy context is equivalent to that of this <code>EjbModuleConfigurationMBean</code>.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the linkConfiguration method signature. The exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void linkConfiguration(ModuleConfigurationMBean link) throws GeronimoSecurityException {
        PolicyConfiguration other;

        try {
            other = factory.getPolicyConfiguration(link.getContextID(), false);
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException("Unable to find policy configuration with that id", e);
        }

        if (other != null) {
            try {
                policyConfiguration.linkConfiguration(other);
            } catch (PolicyContextException e) {
                throw new GeronimoSecurityException("Unable to link configuration", e.getCause());
            }

        }
    }

    /**
     * <p>Causes all policy statements to be deleted from this <code>PolicyConfiguration</code> and sets its internal
     * state such that calling any method, other than <code>delete</code>, <code>getContextID</code>, or
     * <code>inService</code> on the <code>PolicyConfiguration</code> will be rejected and cause an
     * <code>UnsupportedOperationException</code> to be thrown.</p>
     *
     * <p> This operation has no affect on any linked <code>PolicyConfigurations</code> other than removing any links
     * involving the deleted <code>PolicyConfiguration<code>.</p>
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the delete method signature. The exception thrown by the implementation class will be encapsulated (during
     * construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void delete() throws GeronimoSecurityException {
        try {
            server.unregisterMBean(objectName);
        } catch (InstanceNotFoundException e) {
            throw new GeronimoSecurityException("Already deleted", e);
        } catch (MBeanRegistrationException e) {
        } finally {
            try {
                policyConfiguration.delete();
            } catch (PolicyContextException e) {
                throw new GeronimoSecurityException("Unable to delete configuration", e.getCause());
            }
        }
    }

    /**
     * <p>This method is used to set to "inService" the state of the policy context whose interface is this
     * <code>PolicyConfiguration</code> Object. Only those policy contexts whose state is "inService" will be included
     * in the policy contexts processed by the <code>Policy.refresh</code> method. A policy context whose state is
     * "inService" may be returned to the "open" state by calling the <code>getPolicyConfiguration</code> method of the
     * <code>PolicyConfiguration</code> factory with the policy context identifier of the policy context.</p>
     *
     * <p> When the state of a policy context is "inService", calling any method other than <code>commit</code>,
     * <code>delete</code>, <code>getContextID</code>, or <code>inService</code> on its <code>PolicyConfiguration</code>
     * Object will cause an <code>UnsupportedOperationException</code> to be thrown.</p>
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * <code>PolicyConfiguration</code> Object is "deleted" when this method is called.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the commit method signature. The exception thrown by the implementation class will be encapsulated (during
     * construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void commit() throws GeronimoSecurityException {
        try {
            policyConfiguration.commit();
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException("Unable to commit configuration", e.getCause());
        }
    }

    /**
     * This method is used to determine if the policy context whose interface is this <code>PolicyConfiguration</code>
     * Object is in the "inService" state.
     * @return <code>true</code> if the state of the associated policy context is "inService"; <code>false</code>
     * otherwise.
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by the
     * <code>inService</code> method signature. The exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public boolean inService() throws GeronimoSecurityException {
        try {
            return policyConfiguration.inService();
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException("Unable to obtain inService state", e.getCause());
        }
    }
}
