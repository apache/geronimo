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

package org.apache.geronimo.security.jacc;

import java.util.Collection;
import java.util.HashSet;

import org.apache.geronimo.common.GeronimoSecurityException;


/**
 *
 * @version $Rev$ $Date$
 */
public interface ModuleConfiguration {


    /**
     * This method returns this modules's policy context identifier.
     * @return this object's policy context identifier.
     */
    public String getContextID();

    /**
     * This method returns the module's set of roles.
     * @return the set of roles that are being used for this module.
     */
    public HashSet getRoles();

    /**
     * Add a mapping from a module's security roles to physical principals.  Mapping principals to the same role twice
     * will cause a <code>PolicyContextException</code> to be thrown.
     * @param role The role that is to be mapped to a set of principals.
     * @param principals The set of principals that are to be mapped to to role.
     * @throws org.apache.geronimo.common.GeronimoSecurityException if the mapping principals to the same role twice occurs.
     */
    public void addRoleMapping(String role, Collection principals) throws GeronimoSecurityException;

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
     * @throws org.apache.geronimo.common.GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the linkConfiguration method signature. The exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void linkConfiguration(ModuleConfiguration link) throws GeronimoSecurityException;

    /**
     * <p>Causes all policy statements to be deleted from this <code>PolicyConfiguration</code> and sets its internal
     * state such that calling any method, other than <code>delete</code>, <code>getContextID</code>, or
     * <code>inService</code> on the <code>PolicyConfiguration</code> will be rejected and cause an
     * <code>UnsupportedOperationException</code> to be thrown.</p>
     *
     * <p>This operation has no affect on any linked <code>PolicyConfigurations</code> other than removing any links
     * involving the deleted <code>PolicyConfiguration<code>.</p>
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws org.apache.geronimo.common.GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the delete method signature. The exception thrown by the implementation class will be encapsulated (during
     * construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void delete() throws GeronimoSecurityException;

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
     * @throws org.apache.geronimo.common.GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by
     * the commit method signature. The exception thrown by the implementation class will be encapsulated (during
     * construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public void commit() throws GeronimoSecurityException;

    /**
     * This method is used to determine if the policy context whose interface is this <code>PolicyConfiguration</code>
     * Object is in the "inService" state.
     * @return <code>true</code> if the state of the associated policy context is "inService"; <code>false</code>
     * otherwise.
     * @throws java.lang.SecurityException if called by an <code>AccessControlContext</code> that has not been granted
     * the "setPolicy" <code>SecurityPermission</code>.
     * @throws org.apache.geronimo.common.GeronimoSecurityException if the implementation throws a checked exception that has not been accounted for by the
     * <code>inService</code> method signature. The exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown <code>GeronimoSecurityException</code>.
     */
    public boolean inService() throws GeronimoSecurityException;
}
