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
package org.apache.geronimo.security.jacc.mappingprovider;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.security.Principal;

import javax.security.jacc.PolicyContextException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.deploy.SubjectInfo;

/**
 * @version $Rev$ $Date$
 */
public class ApplicationPrincipalRoleConfigurationManager implements PrincipalRoleMapper {

    private static PrincipalRoleConfigurationFactory principalRoleConfigurationFactory;
    private final Map<Principal, Set<String>> principalRoleMap;
    private final Map<String, Subject> roleDesignates = new HashMap<String, Subject>();
    private final Subject defaultSubject;

    public ApplicationPrincipalRoleConfigurationManager(Map<Principal, Set<String>> principalRoleMap, SubjectInfo defaultSubjectInfo, Map<String, SubjectInfo> roleDesignates, CredentialStore credentialStore) throws PolicyContextException, ClassNotFoundException, LoginException {
        if (credentialStore == null && (!roleDesignates.isEmpty() || defaultSubjectInfo != null)) {
            throw new NullPointerException("No CredentialStore supplied to resolve default and run-as subjects");
        }
        this.principalRoleMap = principalRoleMap;
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


    public static void setPrincipalRoleConfigurationFactory(PrincipalRoleConfigurationFactory principalRoleConfigurationFactory) {
        if (ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory != null) {
            throw new IllegalStateException("ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory already set");
        }
        ApplicationPrincipalRoleConfigurationManager.principalRoleConfigurationFactory = principalRoleConfigurationFactory;
    }

    public void install(Set<String> contextIds) throws PolicyContextException {
        if (principalRoleConfigurationFactory == null) {
            throw new IllegalStateException("Inconsistent security setup.  PrincipalRoleConfigurationFactory is not set");
        }

        for (String contextID : contextIds) {
            PrincipalRoleConfiguration principalRoleConfiguration = principalRoleConfigurationFactory.getPrincipalRoleConfiguration(contextID);
            principalRoleConfiguration.setPrincipalRoleMapping(principalRoleMap);
        }

    }


    public void uninstall(Set<String> contextIds) throws PolicyContextException {
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
        if (role == null) return null;
        Subject runAs = roleDesignates.get(role);
        if (runAs == null) throw new IllegalStateException("no run-as identity configured for role: " + role);
        return runAs;
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
    }

    public void doFail() {

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ApplicationPrincipalRoleConfigurationManager.class, SecurityNames.JACC_MANAGER);
        infoBuilder.addAttribute("principalRoleMap", Map.class, true);
        infoBuilder.addAttribute("defaultSubjectInfo", SubjectInfo.class, true);
        infoBuilder.addAttribute("roleDesignates", Map.class, true);
        infoBuilder.addReference("CredentialStore", CredentialStore.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.setConstructor(new String[] {"principalRoleMap", "defaultSubjectInfo", "roleDesignates", "CredentialStore"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
