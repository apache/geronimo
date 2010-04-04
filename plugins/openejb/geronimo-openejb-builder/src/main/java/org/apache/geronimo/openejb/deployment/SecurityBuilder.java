/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb.deployment;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ExcludeList;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;

public class SecurityBuilder {
    /**
     * Fill the container moduleBuilder with the security information that it needs
     * to create the proper interceptors.  A <code>SecurityConfiguration</code>
     * is also filled with permissions that need to be used to fill the JACC
     * policy configuration.
     *
     * @param defaultRole default role for otherwise unassigned permissions
     * @param notAssigned the set of all possible permissions.  These will be
     * culled so that all that are left are those that have
     * not been assigned roles.
     * @param assemblyDescriptor the assembly descriptor
     * @param ejbName the name of the EJB
     * @param securityRoleRefs the EJB's role references
     * @param componentPermissions the holder for the ejb's permissions
     * @throws DeploymentException if any constraints are violated
     */
    public void addComponentPermissions(String defaultRole,
            Collection<Permission> notAssigned,
            AssemblyDescriptor assemblyDescriptor,
            String ejbName,
            List<SecurityRoleRef> securityRoleRefs,
            ComponentPermissions componentPermissions) throws DeploymentException {

        PermissionCollection uncheckedPermissions = componentPermissions.getUncheckedPermissions();
        PermissionCollection excludedPermissions = componentPermissions.getExcludedPermissions();
        Map<String, PermissionCollection> rolePermissions = componentPermissions.getRolePermissions();
        Set<Permission> allExcludedPermissions = new HashSet<Permission>();

        //this can occur in an ear when one ejb module has security and one doesn't.  In this case we still need
        //to make the non-secure one completely unchecked.
        if (assemblyDescriptor != null) {
            /**
             * JACC v1.0 section 3.1.5.2
             */
            ExcludeList excludeList = assemblyDescriptor.getExcludeList();
            if (excludeList != null) {
                for (Method method : excludeList.getMethod()) {
                    if (!ejbName.equals(method.getEjbName())) {
                        continue;
                    }

                    // method name
                    String methodName = method.getMethodName();
                    // method interface
                    String methodIntf = method.getMethodIntf() == null? null: method.getMethodIntf().toString();

                    // method parameters
                    String[] methodParams;
                    if (method.getMethodParams() != null) {
                        List<String> paramList = method.getMethodParams().getMethodParam();
                        methodParams = paramList.toArray(new String[paramList.size()]);
                    } else {
                        methodParams = null;
                    }

                    // create the permission object
                    EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodIntf, methodParams);

                    excludedPermissions.add(permission);
                    allExcludedPermissions.addAll(intersectPermissions(notAssigned, permission, false));
                }
            }
            /**
             * JACC v1.0 section 3.1.5.1
             */
            for (MethodPermission methodPermission : assemblyDescriptor.getMethodPermission()) {
                List<String> roleNames = methodPermission.getRoleName();
                boolean unchecked = methodPermission.getUnchecked();

                for (Method method : methodPermission.getMethod()) {
                    if (!ejbName.equals(method.getEjbName())) {
                        continue;
                    }

                    // method name
                    String methodName = method.getMethodName();
                    if ("*".equals(methodName)) {
                        // jacc uses null instead of *
                        methodName = null;
                    }
                    // method interface
                    String methodIntf = method.getMethodIntf() == null? null: method.getMethodIntf().toString();

                    // method parameters
                    String[] methodParams;
                    if (method.getMethodParams() != null) {
                        List<String> paramList = method.getMethodParams().getMethodParam();
                        methodParams = paramList.toArray(new String[paramList.size()]);
                    } else {
                        methodParams = null;
                    }

                    // create the permission object
                    EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodIntf, methodParams);
                    Collection<Permission> culled = intersectPermissions(notAssigned, permission, true);
                    //does this intersect the excluded permissions?
                    int size = culled.size();
                    culled.removeAll(allExcludedPermissions);
                    if (size == culled.size()) {
                        //no intersection, just use actually specified permission
                        culled = Collections.<Permission>singletonList(permission);
                    }
                    //otherwise, use the individual permissions that are not excluded

                    // if this is unchecked, mark it as unchecked; otherwise assign the roles
                    if (unchecked) {
                        for (Permission p: culled) {
                            uncheckedPermissions.add(p);
                        }
                    } else if (culled.size() > 0) {
                        for (String roleName : roleNames) {
                            Permissions permissions = (Permissions) rolePermissions.get(roleName);
                            if (permissions == null) {
                                permissions = new Permissions();
                                rolePermissions.put(roleName, permissions);
                            }
                            for (Permission p: culled) {
                                permissions.add(p);
                            }
                        }
                    }
                }

            }


            /**
             * JACC v1.0 section 3.1.5.3
             */
            for (SecurityRoleRef securityRoleRef : securityRoleRefs) {

                String roleLink = securityRoleRef.getRoleLink() == null? securityRoleRef.getRoleName(): securityRoleRef.getRoleLink();

                PermissionCollection roleLinks = rolePermissions.get(roleLink);
                if (roleLinks == null) {
                    roleLinks = new Permissions();
                    rolePermissions.put(roleLink, roleLinks);

                }
                roleLinks.add(new EJBRoleRefPermission(ejbName, securityRoleRef.getRoleName()));
            }
        }

        /**
         * EJB v2.1 section 21.3.2
         * <p/>
         * It is possible that some methods are not assigned to any security
         * roles nor contained in the <code>exclude-list</code> element. In
         * this case, it is the responsibility of the Deployer to assign method
         * permissions for all of the unspecified methods, either by assigning
         * them to security roles, or by marking them as <code>unchecked</code>.
         */
        PermissionCollection permissions;
        if (defaultRole == null) {
            permissions = uncheckedPermissions;
        } else {
            permissions = rolePermissions.get(defaultRole);
            if (permissions == null) {
                permissions = new Permissions();
                rolePermissions.put(defaultRole, permissions);
            }
        }

        notAssigned.removeAll(allExcludedPermissions);
        for (Permission p: notAssigned) {
            permissions.add(p);
        }

    }

    /**
     * Generate all the possible permissions for a bean's interface.
     * <p/>
     * Method permissions are defined in the deployment descriptor as a binary
     * relation from the set of security roles to the set of methods of the
     * home, component, and/or web service endpoint interfaces of session and
     * entity beans, including all their superinterfaces (including the methods
     * of the <code>EJBHome</code> and <code>EJBObject</code> interfaces and/or
     * <code>EJBLocalHome</code> and <code>EJBLocalObject</code> interfaces).
     *
     * @param permissions the permission set to be extended
     * @param ejbName the name of the EJB
     * @param methodInterface the EJB method interface
     * @param interfaceClass the class name of the interface to be used to generate the permissions
     * @param classLoader the class loader to be used in obtaining the interface class
     * @throws org.apache.geronimo.common.DeploymentException in case a class could not be found
     */
    public void addToPermissions(Collection<Permission> permissions,
            String ejbName,
            String methodInterface,
            String interfaceClass,
            ClassLoader classLoader) throws DeploymentException {

        if (interfaceClass == null) {
            return;
        }

        try {
            Class clazz = Class.forName(interfaceClass, false, classLoader);
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                permissions.add(new EJBMethodPermission(ejbName, methodInterface, method));
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        }

    }

    /**
     * Removes permissions from <code>toBeChecked</code> that are implied by
     * <code>permission</code>.
     *
     * @param toBeChecked the permissions that are to be checked and possibly culled
     * @param permission the permission that is to be used for culling
     * @param remove  whether to remove the matched permission
     * @return the set of permissions that are implied by <code>permission</code>
     */
    private Collection<Permission> intersectPermissions(Collection<Permission> toBeChecked, Permission permission, boolean remove) {
        Collection<Permission> result = new ArrayList<Permission>();
        for (Iterator<Permission> it = toBeChecked.iterator(); it.hasNext();) {
            Permission test = it.next();
            if (permission.implies(test)) {
                if (remove) {
                    it.remove();
                }
                result.add(test);
            }
        }

        return result;
    }

    public void addEjbTimeout(EnterpriseBean remoteBean, EjbModule ejbModule, Collection<Permission> permissions) throws DeploymentException {
        NamedMethod timeout = null;
        if (remoteBean instanceof SessionBean) {
            timeout = ((SessionBean) remoteBean).getTimeoutMethod();
        } else if (remoteBean instanceof MessageDrivenBean) {
            timeout = ((MessageDrivenBean) remoteBean).getTimeoutMethod();
        }
        if (timeout != null) {
            permissions.add(new EJBMethodPermission(remoteBean.getEjbName(), timeout.getMethodName(), null, new String[]{Timer.class.getName()}));
        } else {
            try {
                Class ejbClass = ejbModule.getClassLoader().loadClass(remoteBean.getEjbClass());
                if (TimedObject.class.isAssignableFrom(ejbClass)) {
                    permissions.add(new EJBMethodPermission(remoteBean.getEjbName(), "ejbTimeout", null, new String[]{Timer.class.getName()}));
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not figure out timer method", e);
            }
        }
    }
    
}
