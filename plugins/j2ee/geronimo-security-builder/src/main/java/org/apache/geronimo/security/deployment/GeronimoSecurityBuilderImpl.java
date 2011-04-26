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

import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.schema.ElementConverter;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.schema.SecurityElementConverter;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.deployment.model.security.LoginDomainPrincipalType;
import org.apache.geronimo.security.deployment.model.security.PrincipalType;
import org.apache.geronimo.security.deployment.model.security.RealmPrincipalType;
import org.apache.geronimo.security.deployment.model.security.RoleMappingsType;
import org.apache.geronimo.security.deployment.model.security.RoleType;
import org.apache.geronimo.security.deployment.model.security.SecurityRefType;
import org.apache.geronimo.security.deployment.model.security.SecurityType;
import org.apache.geronimo.security.deployment.model.security.SubjectInfoType;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class GeronimoSecurityBuilderImpl implements NamespaceDrivenBuilder, ModuleBuilderExtension, GBeanLifecycle {
//    private static final QName BASE_SECURITY_QNAME = org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument.type.getDocumentElementName();
//    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
//    private static final QName SECURITY_REF_QNAME = GerSecurityRefDocument.type.getDocumentElementName();
//    private static final QNameSet SECURITY_QNAME_SET = QNameSet.forArray(new QName[]{SECURITY_QNAME, SECURITY_REF_QNAME});
    public static final String GERONIMO_SECURITY_NAMESPACE = "http://geronimo.apache.org/xml/ns/security-2.0";
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();

    public static final EARContext.Key<AbstractNameQuery> ROLE_MAPPER_DATA_NAME = new EARContext.Key<AbstractNameQuery>() {

        @Override
        public AbstractNameQuery get(Map<EARContext.Key, Object> context) {
            return (AbstractNameQuery) context.get(this);
        }
    };

    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.1", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/loginconfig-1.2", "http://geronimo.apache.org/xml/ns/loginconfig-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security", "http://geronimo.apache.org/xml/ns/security-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.1", "http://geronimo.apache.org/xml/ns/security-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/security-1.2", "http://geronimo.apache.org/xml/ns/security-2.0");
    }

    private static final Map<String, ElementConverter> GERONIMO_SCHEMA_CONVERSIONS = new HashMap<String, ElementConverter>();

    static {
        GERONIMO_SCHEMA_CONVERSIONS.put("security", new SecurityElementConverter());
        GERONIMO_SCHEMA_CONVERSIONS.put("security-ref", new NamespaceElementConverter(GERONIMO_SECURITY_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("default-subject", new NamespaceElementConverter(GERONIMO_SECURITY_NAMESPACE));
    }

    private final AbstractNameQuery defaultCredentialStoreName;
    private final AbstractNameQuery defaultRoleMappingName;
    private final Environment defaultEnvironment;

    public GeronimoSecurityBuilderImpl(@ParamAttribute(name = "credentialStoreName")AbstractNameQuery credentialStoreName,
                                       @ParamAttribute(name = "defaultRoleMappingName")AbstractNameQuery defaultRoleMappingName,
                                       @ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment) {
        this.defaultCredentialStoreName = credentialStoreName;
        this.defaultRoleMappingName = defaultRoleMappingName;
        this.defaultEnvironment = defaultEnvironment;
    }

    public void doStart() {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
        SchemaConversionUtils.registerNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
        SchemaConversionUtils.unregisterNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
    }

    public void doFail() {
        doStop();
    }

    //MBE methods
    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        buildJaccManager(earContext);
    }

    //NamespaceDrivenBuilder methods
    public void buildEnvironment(Object container, Environment environment) throws DeploymentException {
    }


    public void build(Object container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        if (container instanceof SecurityType) {
            SecurityType securityType = (SecurityType) container;
            Bundle bundle = applicationContext.getDeploymentBundle();

            if (applicationContext instanceof EARContext) {
                SecurityConfiguration securityConfiguration = buildSecurityConfig(securityType);
                ((EARContext)applicationContext).setSecurityConfiguration(securityConfiguration);
            }

            AbstractNameQuery roleMapperDataName = configureRoleMapper(applicationContext, securityType, bundle);
            if (applicationContext instanceof EARContext) {
                setRoleMapperName(applicationContext, roleMapperDataName);
            }
        }
        if (container instanceof SecurityRefType) {
            SecurityRefType ref = (SecurityRefType) container;
                if (ref.getName() != null) {
                    String name = ref.getName().trim();
                    //TODO its an osgi filter expression
                    AbstractNameQuery roleMapperDataName = new AbstractNameQuery(null, Collections.singletonMap("name", name), PrincipalRoleMapper.class.getName());
                    setRoleMapperName(applicationContext, roleMapperDataName);
                } else {
                    String filter = ref.getRef();
                    //TODO its an osgi filter expression
                    AbstractNameQuery roleMapperDataName = new AbstractNameQuery(null, Collections.singletonMap("name", filter), PrincipalRoleMapper.class.getName());
//                    AbstractNameQuery roleMapperDataName = SingleGBeanBuilder.buildAbstractNameQuery(SecurityRefType, GBeanInfoBuilder.DEFAULT_J2EE_TYPE, Collections.singleton(CredentialStore.class.getName()));
                    setRoleMapperName(applicationContext, roleMapperDataName);
                }
        }
    }

    private void setRoleMapperName(DeploymentContext applicationContext, AbstractNameQuery roleMapperDataName) throws DeploymentException {
        EARContext earContext = (EARContext) applicationContext;
        if (earContext.getGeneralData().put(ROLE_MAPPER_DATA_NAME, roleMapperDataName) != null) {
            throw new DeploymentException("Only one role mapping or role mapping reference can be present in an ear");
        }
    }

    private void buildJaccManager(EARContext earContext) throws DeploymentException {
        if (earContext.isHasSecurity()) {
            //Be sure to only set once per app
            earContext.setHasSecurity(false);
            AbstractNameQuery roleMapperDataName = ROLE_MAPPER_DATA_NAME.get(earContext.getGeneralData());
            if (roleMapperDataName == null) {
                roleMapperDataName = defaultRoleMappingName;
                EnvironmentBuilder.mergeEnvironments(earContext.getConfiguration().getEnvironment(), defaultEnvironment);
            }
            Naming naming = earContext.getNaming();
            GBeanData jaccBeanData = configureApplicationPolicyManager(naming, earContext.getModuleName(), earContext.getContextIDToPermissionsMap());
            jaccBeanData.setReferencePattern("PrincipalRoleMapper", roleMapperDataName);
            try {
                earContext.addGBean(jaccBeanData);
                earContext.getGeneralData().put(EARContext.JACC_MANAGER_NAME_KEY, jaccBeanData.getAbstractName());
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("JACC manager gbean already present", e);
            }
        }
    }

    private SecurityConfiguration buildSecurityConfig(SecurityType securityType) {

        if (securityType == null) {
            return null;
        }

        boolean doAsCurrentCaller = securityType.isDoasCurrentCaller();
        boolean useContextHandler = securityType.isUseContextHandler();
        String defaultRole = securityType.getDefaultRole();

        return new SecurityConfiguration(defaultRole, doAsCurrentCaller, useContextHandler);
    }

    private void add(String roleName, Principal principal, Map<Principal, Set<String>> principalRoleMap) {
        Set<String> roles = principalRoleMap.get(principal);
        if (roles == null) {
            roles = new HashSet<String>();
            principalRoleMap.put(principal, roles);
        }
        roles.add(roleName);
    }

    private SubjectInfo buildSubjectInfo(SubjectInfoType defaultSubject) {
        if (defaultSubject == null) {
            return null;
        }
        String realmName = defaultSubject.getRealm().trim();
        String id = defaultSubject.getId().trim();
        return new SubjectInfo(realmName, id);
    }

    private static Principal buildRealmPrincipal(RealmPrincipalType realmPrincipalType, Bundle bundle) {
        return ConfigurationUtil.generateRealmPrincipal(realmPrincipalType.getRealmName().trim(), realmPrincipalType.getDomainName().trim(), realmPrincipalType.getClazz().trim(), realmPrincipalType.getName().trim(), bundle);
    }

    private static Principal buildDomainPrincipal(LoginDomainPrincipalType domainPrincipalType, Bundle bundle) {
        return ConfigurationUtil.generateDomainPrincipal(domainPrincipalType.getDomainName().trim(), domainPrincipalType.getClazz().trim(), domainPrincipalType.getName().trim(), bundle);
    }

    private static Principal buildPrincipal(PrincipalType principalType, Bundle bundle) {
        return ConfigurationUtil.generatePrincipal(principalType.getClazz().trim(), principalType.getName().trim(), bundle);
    }

    //used from TSSConfigEditor
    public PrincipalInfo buildPrincipal(PrincipalType principalType) {
        return new PrincipalInfo(principalType.getClazz().trim(), principalType.getName().trim());
    }

    protected AbstractNameQuery configureRoleMapper(DeploymentContext deploymentContext, SecurityType securityType, Bundle bundle) throws DeploymentException {
        Map<String, SubjectInfo> roleDesignates = new HashMap<String, SubjectInfo>();
        Map<Principal, Set<String>> principalRoleMap = new HashMap<Principal, Set<String>>();
        if (securityType.getRoleMappings() != null) {
            RoleMappingsType roleMappingsType = securityType.getRoleMappings();
            for (RoleType roleType : roleMappingsType.getRole()) {

                String roleName = roleType.getRoleName().trim();
                if (roleType.getRunAsSubject() != null) {
                    SubjectInfo subjectInfo = buildSubjectInfo(roleType.getRunAsSubject());
                    roleDesignates.put(roleName, subjectInfo);
                }

                for (RealmPrincipalType realmPrincipalType: roleType.getRealmPrincipal()) {
                    Principal principal = buildRealmPrincipal(realmPrincipalType, bundle);
                    add(roleName, principal, principalRoleMap);
                }

                for (LoginDomainPrincipalType loginDomainPrincipalType: roleType.getLoginDomainPrincipal()) {
                    Principal principal = buildDomainPrincipal(loginDomainPrincipalType, bundle);
                    add(roleName, principal, principalRoleMap);
                }

                for (PrincipalType principalType: roleType.getPrincipal()) {
                    Principal principal = buildPrincipal(principalType, bundle);
                    add(roleName, principal, principalRoleMap);
                }

            }
        }

        SubjectInfo defaultSubjectInfo = buildSubjectInfo(securityType.getDefaultSubject());
        AbstractNameQuery credentialStoreName;
        if (securityType.getCredentialStoreRef() != null) {
            String credentialStoreType = securityType.getCredentialStoreRef();
            //TODO use osgi filter
            credentialStoreName = new AbstractNameQuery(null, Collections.singletonMap("name", credentialStoreType), CredentialStore.class.getName());
//            credentialStoreName = SingleGBeanBuilder.buildAbstractNameQuery(credentialStoreType, GBeanInfoBuilder.DEFAULT_J2EE_TYPE, Collections.singleton(CredentialStore.class.getName()));
        } else {
            credentialStoreName = this.defaultCredentialStoreName;
        }
        Naming naming = deploymentContext.getNaming();
        String name = securityType.getName() != null ? securityType.getName() : "RoleMapper";
        AbstractName roleMapperName = naming.createChildName(deploymentContext.getModuleName(), "RoleMapper", name);
        GBeanData roleMapperData = new GBeanData(roleMapperName, ApplicationPrincipalRoleConfigurationManager.GBEAN_INFO);
        roleMapperData.setAttribute("principalRoleMap", principalRoleMap);
        roleMapperData.setAttribute("roleDesignates", roleDesignates);
        roleMapperData.setAttribute("defaultSubjectInfo", defaultSubjectInfo);
        if ((roleDesignates != null && !roleDesignates.isEmpty()) || defaultSubjectInfo != null) {
            roleMapperData.setReferencePattern("CredentialStore", credentialStoreName);
        }
        try {
            deploymentContext.addGBean(roleMapperData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Role mapper gbean already present", e);
        }
        return new AbstractNameQuery(roleMapperData.getAbstractName());
    }

    protected GBeanData configureApplicationPolicyManager(Naming naming, AbstractName moduleName, Map<String, ComponentPermissions> contextIDToPermissionsMap) {
        AbstractName jaccBeanName = naming.createChildName(moduleName, SecurityNames.JACC_MANAGER, SecurityNames.JACC_MANAGER);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        return jaccBeanData;
    }


}
