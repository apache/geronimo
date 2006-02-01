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

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPolicyConfigurationManager implements GBeanLifecycle, RoleDesignateSource {

    private final Map contextIdToPolicyConfigurationMap = new HashMap();
    private final Map roleDesignates;
    private final PrincipalRoleMapper principalRoleMapper;

    public ApplicationPolicyConfigurationManager(Map contextIdToPermissionsMap, Map roleDesignates, ClassLoader cl, PrincipalRoleMapper principalRoleMapper) throws PolicyContextException, ClassNotFoundException {
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

        for (Iterator iterator = contextIdToPermissionsMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String contextID = (String) entry.getKey();
            ComponentPermissions componentPermissions = (ComponentPermissions) entry.getValue();

            PolicyConfiguration policyConfiguration = policyConfigurationFactory.getPolicyConfiguration(contextID, true);
            contextIdToPolicyConfigurationMap.put(contextID, policyConfiguration);
            policyConfiguration.addToExcludedPolicy(componentPermissions.getExcludedPermissions());
            policyConfiguration.addToUncheckedPolicy(componentPermissions.getUncheckedPermissions());
            for (Iterator roleIterator = componentPermissions.getRolePermissions().entrySet().iterator(); roleIterator.hasNext();) {
                Map.Entry roleEntry = (Map.Entry) roleIterator.next();
                String roleName = (String) roleEntry.getKey();
                PermissionCollection rolePermissions = (PermissionCollection) roleEntry.getValue();
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
        for (Iterator iterator = contextIdToPolicyConfigurationMap.values().iterator(); iterator.hasNext();) {
            PolicyConfiguration policyConfiguration = (PolicyConfiguration) iterator.next();
            for (Iterator iterator2 = contextIdToPolicyConfigurationMap.values().iterator(); iterator2.hasNext();) {
                PolicyConfiguration policyConfiguration2 = (PolicyConfiguration) iterator2.next();
                if (policyConfiguration != policyConfiguration2) {
                    policyConfiguration.linkConfiguration(policyConfiguration2);
                }
            }
        }

        //commit
        for (Iterator iterator = contextIdToPolicyConfigurationMap.values().iterator(); iterator.hasNext();) {
            PolicyConfiguration policyConfiguration = (PolicyConfiguration) iterator.next();
            policyConfiguration.commit();
        }

        //refresh policy
        Policy policy = Policy.getPolicy();
        policy.refresh();

        for (Iterator iterator = roleDesignates.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Subject roleDesignate = (Subject) entry.getValue();
            ContextManager.registerSubject(roleDesignate);
            SubjectId id = ContextManager.getSubjectId(roleDesignate);
            roleDesignate.getPrincipals().add(new IdentificationPrincipal(id));
        }
        this.roleDesignates = roleDesignates;
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        for (Iterator iterator = roleDesignates.entrySet().iterator(); iterator.hasNext();) {
             Map.Entry entry = (Map.Entry) iterator.next();
             Subject roleDesignate = (Subject) entry.getValue();
             ContextManager.unregisterSubject(roleDesignate);
         }

        if (principalRoleMapper != null) {
            principalRoleMapper.uninstall();
        }

        for (Iterator iterator = contextIdToPolicyConfigurationMap.values().iterator(); iterator.hasNext();) {
            PolicyConfiguration policyConfiguration = (PolicyConfiguration) iterator.next();
            policyConfiguration.delete();
        }
    }

    public void doFail() {

    }

    public Map getRoleDesignateMap() {
        return roleDesignates;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPolicyConfigurationManager.class, NameFactory.JACC_MANAGER);
        infoBuilder.addAttribute("contextIdToPermissionsMap", Map.class, true);
        infoBuilder.addAttribute("roleDesignates", Map.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addInterface(RoleDesignateSource.class);
        infoBuilder.addReference("PrincipalRoleMapper", PrincipalRoleMapper.class, NameFactory.JACC_MANAGER);
        infoBuilder.setConstructor(new String[] {"contextIdToPermissionsMap", "roleDesignates", "classLoader", "PrincipalRoleMapper"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
