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

package org.apache.geronimo.security.deployment;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.service.SingleGBeanBuilder;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.security.deploy.LoginDomainPrincipalInfo;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.RealmPrincipalInfo;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSecurityBuilderImpl implements NamespaceDrivenBuilder, GBeanLifecycle {
    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private static final QNameSet SECURITY_QNAME_SET = QNameSet.singleton(SECURITY_QNAME);
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.1", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.2", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security", "http://geronimo.apache.org/xml/ns/security-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.1", "http://geronimo.apache.org/xml/ns/security-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.2", "http://geronimo.apache.org/xml/ns/security-2.0");
    }

    private final AbstractNameQuery credentialStoreName;

    public GeronimoSecurityBuilderImpl(AbstractNameQuery credentialStoreName) {
        this.credentialStoreName = credentialStoreName;
    }

    public void doStart() {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        EARContext earContext = (EARContext) applicationContext;
        XmlObject[] items = container.selectChildren(SECURITY_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of security elements in geronimo plan " + items.length + " qnameset: " + SECURITY_QNAME_SET);
        }
        if (items.length == 1) {
            GerSecurityType securityType;
            try {
                securityType = (GerSecurityType) XmlBeansUtil.typedCopy(items[0], GerSecurityType.type);
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate security element", e);
            }
            Security security = buildSecurityConfig(securityType);
            ClassLoader classLoader = applicationContext.getClassLoader();
            SecurityConfiguration securityConfiguration = buildSecurityConfiguration(security, classLoader);
            earContext.setSecurityConfiguration(securityConfiguration);
            
            Naming naming = earContext.getNaming();
            GBeanData roleMapperData = configureRoleMapper(naming, earContext.getModuleName(), securityConfiguration);
            try {
                earContext.addGBean(roleMapperData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Role mapper gbean already present", e);
            }
            AbstractNameQuery credentialStoreName;
            if (securityType.isSetCredentialStoreRef()) {
                PatternType credentialStoreType = securityType.getCredentialStoreRef();
                credentialStoreName = SingleGBeanBuilder.buildAbstractNameQuery(credentialStoreType, NameFactory.GERONIMO_SERVICE, Collections.singleton(CredentialStore.class.getName()));
            } else {
                credentialStoreName = this.credentialStoreName;
            }
            GBeanData jaccBeanData = configureApplicationPolicyManager(naming, earContext.getModuleName(), earContext.getContextIDToPermissionsMap(), securityConfiguration, credentialStoreName);
            jaccBeanData.setReferencePattern("PrincipalRoleMapper", roleMapperData.getAbstractName());
            try {
                earContext.addGBean(jaccBeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("JACC manager gbean already present", e);
            }
            earContext.setJaccManagerName(jaccBeanData.getAbstractName());
        }
    }

    private static SecurityConfiguration buildSecurityConfiguration(Security security, ClassLoader classLoader) {
        Map<String, SubjectInfo> roleDesignates = security.getRoleSubjectMappings();
        Map<Principal, Set<String>> principalRoleMap = new HashMap<Principal, Set<String>>();
        Map<String, Set<Principal>> roleToPrincipalMap = new HashMap<String, Set<Principal>>();
        buildRolePrincipalMap(security, roleToPrincipalMap, classLoader);
        invertMap(roleToPrincipalMap, principalRoleMap);
        return new SecurityConfiguration(principalRoleMap, roleDesignates, security.getDefaultSubjectInfo(), security.getDefaultRole(), security.isDoAsCurrentCaller(), security.isUseContextHandler());
    }

    private static Map invertMap(Map<String, Set<Principal>> roleToPrincipalMap, Map<Principal, Set<String>> principalRoleMapping) {
        for (Map.Entry<String, Set<java.security.Principal>> entry : roleToPrincipalMap.entrySet()) {
            String role = entry.getKey();
            Set<Principal> principals = entry.getValue();
            for (Principal principal : principals) {

                Set<String> roleSet = principalRoleMapping.get(principal);
                if (roleSet == null) {
                    roleSet = new HashSet<String>();
                    principalRoleMapping.put(principal, roleSet);
                }
                roleSet.add(role);
            }
        }
        return principalRoleMapping;
    }

    /**
     * non-interface, used in some jetty/tomcat tests
     *
     * @param security Security object holding security info as it is extracted
     * @param roleToPrincipalMap role to set of Principals mapping
     * @param classLoader application classloader in case we need to load some principal classes.
     */
    public static void buildRolePrincipalMap(Security security, Map<String, Set<Principal>> roleToPrincipalMap, ClassLoader classLoader) {

        for (Object o : security.getRoleMappings().values()) {
            Role role = (Role) o;

            String roleName = role.getRoleName();
            Set<Principal> principalSet = new HashSet<Principal>();

            for (Object o1 : role.getRealmPrincipals()) {
                RealmPrincipalInfo realmPrincipal = (RealmPrincipalInfo) o1;
                Principal principal = ConfigurationUtil.generateRealmPrincipal(realmPrincipal.getRealm(), realmPrincipal.getDomain(), realmPrincipal, classLoader);

                principalSet.add(principal);
            }

            for (Object o2 : role.getLoginDomainPrincipals()) {
                LoginDomainPrincipalInfo domainPrincipal = (LoginDomainPrincipalInfo) o2;
                Principal principal = ConfigurationUtil.generateDomainPrincipal(domainPrincipal.getDomain(), domainPrincipal, classLoader);

                principalSet.add(principal);
            }

            for (Object o3 : role.getPrincipals()) {
                PrincipalInfo plainPrincipalInfo = (PrincipalInfo) o3;
                Principal principal = ConfigurationUtil.generatePrincipal(plainPrincipalInfo, classLoader);

                principalSet.add(principal);
            }

            Set<Principal> roleMapping = roleToPrincipalMap.get(roleName);
            if (roleMapping == null) {
                roleMapping = new HashSet<Principal>();
                roleToPrincipalMap.put(roleName, roleMapping);
            }
            roleMapping.addAll(principalSet);

        }
    }

    private Security buildSecurityConfig(GerSecurityType securityType) {
        Security security;

        if (securityType == null) {
            return null;
        }
        security = new Security();

        security.setDoAsCurrentCaller(securityType.getDoasCurrentCaller());
        security.setUseContextHandler(securityType.getUseContextHandler());
        if (securityType.isSetDefaultRole()) {
            security.setDefaultRole(securityType.getDefaultRole().trim());
        }

        if (securityType.isSetRoleMappings()) {
            GerRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                GerRoleType roleType = roleMappingsType.getRoleArray(i);
                Role role = new Role();

                String roleName = roleType.getRoleName().trim();
                role.setRoleName(roleName);

                if (roleType.isSetRunAsSubject()) {
                    SubjectInfo subjectInfo = buildSubjectInfo(roleType.getRunAsSubject());
                    security.getRoleSubjectMappings().put(roleName, subjectInfo);
                }

                for (int j = 0; j < roleType.sizeOfRealmPrincipalArray(); j++) {
                    role.getRealmPrincipals().add(GeronimoSecurityBuilderImpl.buildRealmPrincipal(roleType.getRealmPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfLoginDomainPrincipalArray(); j++) {
                    role.getLoginDomainPrincipals().add(GeronimoSecurityBuilderImpl.buildDomainPrincipal(roleType.getLoginDomainPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfPrincipalArray(); j++) {
                    role.getPrincipals().add(buildPrincipal(roleType.getPrincipalArray(j)));
                }

                security.getRoleMappings().put(roleName, role);
            }
        }

        security.setDefaultSubjectInfo(buildSubjectInfo(securityType.getDefaultSubject()));

        return security;
    }

    private SubjectInfo buildSubjectInfo(GerSubjectInfoType defaultSubject) {
        if (defaultSubject == null) {
            return null;
        }
        String realmName = defaultSubject.getRealm().trim();
        String id = defaultSubject.getId().trim();
        return new SubjectInfo(realmName, id);
    }

    private static RealmPrincipalInfo buildRealmPrincipal(GerRealmPrincipalType realmPrincipalType) {
        return new RealmPrincipalInfo(realmPrincipalType.getRealmName().trim(), realmPrincipalType.getDomainName().trim(), realmPrincipalType.getClass1().trim(), realmPrincipalType.getName().trim());
    }

    private static LoginDomainPrincipalInfo buildDomainPrincipal(GerLoginDomainPrincipalType domainPrincipalType) {
        return new LoginDomainPrincipalInfo(domainPrincipalType.getDomainName().trim(), domainPrincipalType.getClass1().trim(), domainPrincipalType.getName().trim());
    }

    //used from TSSConfigEditor
    public PrincipalInfo buildPrincipal(XmlObject xmlObject) {
        GerPrincipalType principalType = (GerPrincipalType) xmlObject;
        return new PrincipalInfo(principalType.getClass1().trim(), principalType.getName().trim());
    }

    protected GBeanData configureRoleMapper(Naming naming, AbstractName moduleName, SecurityConfiguration securityConfiguration) {
        AbstractName roleMapperName = naming.createChildName(moduleName, "RoleMapper", "RoleMapper");
        GBeanData roleMapperData = new GBeanData(roleMapperName, ApplicationPrincipalRoleConfigurationManager.GBEAN_INFO);
        roleMapperData.setAttribute("principalRoleMap", securityConfiguration.getPrincipalRoleMap());
        return roleMapperData;
    }

    protected GBeanData configureApplicationPolicyManager(Naming naming, AbstractName moduleName, Map contextIDToPermissionsMap, SecurityConfiguration securityConfiguration, AbstractNameQuery credentialStoreName) {
        AbstractName jaccBeanName = naming.createChildName(moduleName, NameFactory.JACC_MANAGER, NameFactory.JACC_MANAGER);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        Map<String, SubjectInfo> roleDesignates = securityConfiguration.getRoleDesignates();
        jaccBeanData.setAttribute("roleDesignates", roleDesignates);
        jaccBeanData.setAttribute("defaultSubjectInfo", securityConfiguration.getDefaultSubjectInfo());
        if ((roleDesignates != null && !roleDesignates.isEmpty()) || securityConfiguration.getDefaultSubjectInfo() != null) {
            jaccBeanData.setReferencePattern("CredentialStore", credentialStoreName);
        }
        return jaccBeanData;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return SECURITY_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(GeronimoSecurityBuilderImpl.class, NameFactory.MODULE_BUILDER);

        infoFactory.addAttribute("credentialStoreName", AbstractNameQuery.class, true, true);
        infoFactory.setConstructor(new String[] {"credentialStoreName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
