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
package org.apache.geronimo.security.jacc;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.security.SecurityNames;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPolicyConfigurationManager implements GBeanLifecycle, RunAsSource {

    private final Map<String, PolicyConfiguration> contextIdToPolicyConfigurationMap = new HashMap<String, PolicyConfiguration>();
    private final PrincipalRoleMapper principalRoleMapper;
    private ClassLoader classLoader;

    public ApplicationPolicyConfigurationManager(Map<String, ComponentPermissions> contextIdToPermissionsMap, PrincipalRoleMapper principalRoleMapper, ClassLoader cl) throws PolicyContextException, ClassNotFoundException, LoginException {
        this.classLoader = cl;
        this.principalRoleMapper = principalRoleMapper;
        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);
        PolicyConfigurationFactory policyConfigurationFactory;
        try {
            policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }

        for (Map.Entry<String, ComponentPermissions> entry : contextIdToPermissionsMap.entrySet()) {
            String contextID = entry.getKey();
            ComponentPermissions componentPermissions = entry.getValue();

            PolicyConfiguration policyConfiguration = policyConfigurationFactory.getPolicyConfiguration(contextID, true);
            contextIdToPolicyConfigurationMap.put(contextID, policyConfiguration);
            policyConfiguration.addToExcludedPolicy(componentPermissions.getExcludedPermissions());
            policyConfiguration.addToUncheckedPolicy(componentPermissions.getUncheckedPermissions());
            for (Map.Entry<String, PermissionCollection> roleEntry : componentPermissions.getRolePermissions().entrySet()) {
                String roleName = roleEntry.getKey();
                PermissionCollection rolePermissions = roleEntry.getValue();
                for (Enumeration<Permission> permissions = rolePermissions.elements(); permissions.hasMoreElements();) {
                    Permission permission = permissions.nextElement();
                    policyConfiguration.addToRole(roleName, permission);

                }
            }
        }

        if (principalRoleMapper != null) {
            principalRoleMapper.install(contextIdToPermissionsMap.keySet());
        }

        //link everything together
        for (PolicyConfiguration policyConfiguration : contextIdToPolicyConfigurationMap.values()) {
            for (PolicyConfiguration policyConfiguration2 : contextIdToPolicyConfigurationMap.values()) {
                if (policyConfiguration != policyConfiguration2) {
                    policyConfiguration.linkConfiguration(policyConfiguration2);
                }
            }
        }

        //commit
        for (PolicyConfiguration policyConfiguration : contextIdToPolicyConfigurationMap.values()) {
            policyConfiguration.commit();
        }

        //refresh policy
        Policy policy = Policy.getPolicy();
        policy.refresh();

    }

    public Subject getDefaultSubject() {
        return principalRoleMapper.getDefaultSubject();
    }

    public Subject getSubjectForRole(String role) {
        return principalRoleMapper.getSubjectForRole(role);
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        if (principalRoleMapper != null) {
            principalRoleMapper.uninstall(contextIdToPolicyConfigurationMap.keySet());
        }

        for (PolicyConfiguration policyConfiguration : contextIdToPolicyConfigurationMap.values()) {
            policyConfiguration.delete();
        }
    }

    public void doFail() {

    }

    public void updateApplicationPolicyConfiguration(Map<String, ComponentPermissions> contextIdToPermissionsMap) throws PolicyContextException, ClassNotFoundException, LoginException {
        PolicyConfigurationFactory policyConfigurationFactory;
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        //Remove those contextId that we want to update, is this required ?
        for (String contextId : contextIdToPermissionsMap.keySet()) {
            contextIdToPolicyConfigurationMap.remove(contextId);
        }
        if (principalRoleMapper != null) {
            principalRoleMapper.uninstall(contextIdToPermissionsMap.keySet());
        }
        //
        for (Map.Entry<String, ComponentPermissions> entry : contextIdToPermissionsMap.entrySet()) {
            String contextID = entry.getKey();
            ComponentPermissions componentPermissions = entry.getValue();
            //Clean existing PolicyConfiguration and set its state to "OPEN"
            PolicyConfiguration policyConfiguration = policyConfigurationFactory.getPolicyConfiguration(contextID, true);
            contextIdToPolicyConfigurationMap.put(contextID, policyConfiguration);
            policyConfiguration.addToExcludedPolicy(componentPermissions.getExcludedPermissions());
            policyConfiguration.addToUncheckedPolicy(componentPermissions.getUncheckedPermissions());
            for (Map.Entry<String, PermissionCollection> roleEntry : componentPermissions.getRolePermissions().entrySet()) {
                String roleName = roleEntry.getKey();
                PermissionCollection rolePermissions = roleEntry.getValue();
                for (Enumeration<Permission> permissions = rolePermissions.elements(); permissions.hasMoreElements();) {
                    Permission permission = permissions.nextElement();
                    policyConfiguration.addToRole(roleName, permission);
                }
            }
        }
        if (principalRoleMapper != null) {
            principalRoleMapper.install(contextIdToPermissionsMap.keySet());
        }
        //link everything together, seems that we do nothing in the linkConfiguration method
        /*
        for (PolicyConfiguration policyConfiguration : contextIdToPolicyConfigurationMap.values()) {
            for (PolicyConfiguration policyConfiguration2 : contextIdToPolicyConfigurationMap.values()) {
                if (policyConfiguration != policyConfiguration2) {
                    policyConfiguration.linkConfiguration(policyConfiguration2);
                }
            }
        }
        */
        //commit
        for (String contextId : contextIdToPermissionsMap.keySet()) {
            contextIdToPolicyConfigurationMap.get(contextId).commit();
        }
        //refresh policy
        Policy policy = Policy.getPolicy();
        policy.refresh();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPolicyConfigurationManager.class, SecurityNames.JACC_MANAGER);
        infoBuilder.addAttribute("contextIdToPermissionsMap", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addReference("PrincipalRoleMapper", PrincipalRoleMapper.class, SecurityNames.JACC_MANAGER);
        infoBuilder.setConstructor(new String[] {"contextIdToPermissionsMap", "PrincipalRoleMapper", "classLoader"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
