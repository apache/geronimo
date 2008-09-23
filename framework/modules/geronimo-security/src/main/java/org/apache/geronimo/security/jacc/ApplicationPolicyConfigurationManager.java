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
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.deploy.SubjectInfo;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPolicyConfigurationManager implements GBeanLifecycle, RunAsSource {

    private final Map<String, PolicyConfiguration> contextIdToPolicyConfigurationMap = new HashMap<String, PolicyConfiguration>();
    private final Map<String, Subject> roleDesignates = new HashMap<String, Subject>();
    private final Subject defaultSubject;
    private final PrincipalRoleMapper principalRoleMapper;

    public ApplicationPolicyConfigurationManager(Map<String, ComponentPermissions> contextIdToPermissionsMap, SubjectInfo defaultSubjectInfo, Map<String, SubjectInfo> roleDesignates, ClassLoader cl, CredentialStore credentialStore, PrincipalRoleMapper principalRoleMapper) throws PolicyContextException, ClassNotFoundException, LoginException {
        if (credentialStore == null && (!roleDesignates.isEmpty() || defaultSubjectInfo != null)) {
            throw new NullPointerException("No CredentialStore supplied to resolve default and run-as subjects");
        }
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
                for (Enumeration permissions = rolePermissions.elements(); permissions.hasMoreElements();) {
                    Permission permission = (Permission) permissions.nextElement();
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

        if (defaultSubjectInfo == null) {
            defaultSubject = ContextManager.EMPTY;
        } else {
            defaultSubject = credentialStore.getSubject(defaultSubjectInfo.getRealm(), defaultSubjectInfo.getId());
            registerSubject(defaultSubject);
        }

        for (Map.Entry<String, SubjectInfo> entry : roleDesignates.entrySet()) {
            String role = entry.getKey();
            SubjectInfo subjectInfo = entry.getValue();
            if (subjectInfo == null || credentialStore == null) {
                throw new NullPointerException("No subjectInfo for role " + role);
            }
            Subject roleDesignate = credentialStore.getSubject(subjectInfo.getRealm(), subjectInfo.getId());
            registerSubject(roleDesignate);
            this.roleDesignates.put(role, roleDesignate);
        }
    }

    private void registerSubject(Subject subject) {
        ContextManager.registerSubject(subject);
        SubjectId id = ContextManager.getSubjectId(subject);
        subject.getPrincipals().add(new IdentificationPrincipal(id));
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public Subject getSubjectForRole(String role) {
        return roleDesignates.get(role);
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        for (Map.Entry<String, Subject> entry : roleDesignates.entrySet()) {
            Subject roleDesignate = entry.getValue();
            ContextManager.unregisterSubject(roleDesignate);
        }
        if (defaultSubject != ContextManager.EMPTY) {
            ContextManager.unregisterSubject(defaultSubject);
        }

        if (principalRoleMapper != null) {
            principalRoleMapper.uninstall(contextIdToPolicyConfigurationMap.keySet());
        }

        for (PolicyConfiguration policyConfiguration : contextIdToPolicyConfigurationMap.values()) {
            policyConfiguration.delete();
        }
    }

    public void doFail() {

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPolicyConfigurationManager.class, SecurityNames.JACC_MANAGER);
        infoBuilder.addAttribute("contextIdToPermissionsMap", Map.class, true);
        infoBuilder.addAttribute("defaultSubjectInfo", SubjectInfo.class, true);
        infoBuilder.addAttribute("roleDesignates", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addReference("CredentialStore", CredentialStore.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addReference("PrincipalRoleMapper", PrincipalRoleMapper.class, SecurityNames.JACC_MANAGER);
        infoBuilder.setConstructor(new String[] {"contextIdToPermissionsMap", "defaultSubjectInfo", "roleDesignates", "classLoader", "CredentialStore", "PrincipalRoleMapper"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
