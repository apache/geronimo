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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.jar.JarFile;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.SingleGBeanBuilder;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
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
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jacc.mappingprovider.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.jacc.PrincipalRoleMapper;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityRefDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityRefType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class GeronimoSecurityBuilderImpl implements NamespaceDrivenBuilder, ModuleBuilderExtension, GBeanLifecycle {
    private static final QName BASE_SECURITY_QNAME = org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument.type.getDocumentElementName();
    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private static final QName SECURITY_REF_QNAME = GerSecurityRefDocument.type.getDocumentElementName();
    private static final QNameSet SECURITY_QNAME_SET = QNameSet.forArray(new QName[]{SECURITY_QNAME, SECURITY_REF_QNAME});
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
    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
    }


    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        XmlObject[] items = container.selectChildren(SECURITY_QNAME);
        for (XmlObject item : items) {
            GerSecurityType securityType;
            try {
                securityType = (GerSecurityType) XmlBeansUtil.typedCopy(item, GerSecurityType.type);
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate security element", e);
            }
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
        XmlObject[] refs = container.selectChildren(SECURITY_REF_QNAME);
        if (refs.length > 1) {
            throw new DeploymentException("Unexpected count of security-ref elements in geronimo plan " + refs.length + " qname: " + SECURITY_REF_QNAME);
        }
        if (refs.length == 1) {
            GerSecurityRefType ref;
            try {
                ref = (GerSecurityRefType) XmlBeansUtil.typedCopy(refs[0], GerSecurityRefType.type);
                if (ref.isSetName()) {
                    String name = ref.getName().trim();
                    AbstractNameQuery roleMapperDataName = new AbstractNameQuery(null, Collections.singletonMap("name", name), PrincipalRoleMapper.class.getName());
                    setRoleMapperName(applicationContext, roleMapperDataName);
                } else {
                    PatternType SecurityRefType = ref.getRef();
                    AbstractNameQuery roleMapperDataName = SingleGBeanBuilder.buildAbstractNameQuery(SecurityRefType, GBeanInfoBuilder.DEFAULT_J2EE_TYPE, Collections.singleton(CredentialStore.class.getName()));
                    setRoleMapperName(applicationContext, roleMapperDataName);
                }
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate security element", e);
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
            AbstractName applicationPolicyManagerName = earContext.getNaming().createChildName(earContext.getModuleName(), SecurityNames.JACC_MANAGER, SecurityNames.JACC_MANAGER);
            //TODO A better way to avoid the multiple invocation on securityBuilder.addGBeans ?
            try {
                if (earContext.getGBeanInstance(applicationPolicyManagerName) != null) {
                    return;
                }
            } catch (GBeanNotFoundException e1) {
            }
            AbstractNameQuery roleMapperDataName = (AbstractNameQuery)earContext.getGeneralData().get(ROLE_MAPPER_DATA_NAME);
            if (roleMapperDataName == null) {
                roleMapperDataName = defaultRoleMappingName;
                EnvironmentBuilder.mergeEnvironments(earContext.getConfiguration().getEnvironment(), defaultEnvironment);
            }
            GBeanData jaccBeanData = configureApplicationPolicyManager(applicationPolicyManagerName, earContext.getContextIDToPermissionsMap());
            jaccBeanData.setReferencePattern("PrincipalRoleMapper", roleMapperDataName);
            try {
                earContext.addGBean(jaccBeanData);
                earContext.getGeneralData().put(EARContext.JACC_MANAGER_NAME_KEY, jaccBeanData.getAbstractName());
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("JACC manager gbean already present", e);
            }
        }
    }

    private SecurityConfiguration buildSecurityConfig(GerSecurityType securityType) {

        if (securityType == null) {
            return null;
        }

        boolean doAsCurrentCaller = securityType.getDoasCurrentCaller();
        boolean useContextHandler = securityType.getUseContextHandler();
        String defaultRole = securityType.isSetDefaultRole() ? securityType.getDefaultRole().trim() : null;

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

    private SubjectInfo buildSubjectInfo(GerSubjectInfoType defaultSubject) {
        if (defaultSubject == null) {
            return null;
        }
        String realmName = defaultSubject.getRealm().trim();
        String id = defaultSubject.getId().trim();
        return new SubjectInfo(realmName, id);
    }

    private static Principal buildRealmPrincipal(GerRealmPrincipalType realmPrincipalType, Bundle bundle) {
        return ConfigurationUtil.generateRealmPrincipal(realmPrincipalType.getRealmName().trim(), realmPrincipalType.getDomainName().trim(), realmPrincipalType.getClass1().trim(), realmPrincipalType.getName().trim(), bundle);
    }

    private static Principal buildDomainPrincipal(GerLoginDomainPrincipalType domainPrincipalType, Bundle bundle) {
        return ConfigurationUtil.generateDomainPrincipal(domainPrincipalType.getDomainName().trim(), domainPrincipalType.getClass1().trim(), domainPrincipalType.getName().trim(), bundle);
    }

    private static Principal buildPrincipal(GerPrincipalType principalType, Bundle bundle) {
        return ConfigurationUtil.generatePrincipal(principalType.getClass1().trim(), principalType.getName().trim(), bundle);
    }

    //used from TSSConfigEditor
    public PrincipalInfo buildPrincipal(XmlObject xmlObject) {
        GerPrincipalType principalType = (GerPrincipalType) xmlObject;
        return new PrincipalInfo(principalType.getClass1().trim(), principalType.getName().trim());
    }

    protected AbstractNameQuery configureRoleMapper(DeploymentContext deploymentContext, GerSecurityType securityType, Bundle bundle) throws DeploymentException {
        Map<String, SubjectInfo> roleDesignates = new HashMap<String, SubjectInfo>();
        Map<Principal, Set<String>> principalRoleMap = new HashMap<Principal, Set<String>>();
        if (securityType.isSetRoleMappings()) {
            GerRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                GerRoleType roleType = roleMappingsType.getRoleArray(i);

                String roleName = roleType.getRoleName().trim();
                if (roleType.isSetRunAsSubject()) {
                    SubjectInfo subjectInfo = buildSubjectInfo(roleType.getRunAsSubject());
                    roleDesignates.put(roleName, subjectInfo);
                }

                for (int j = 0; j < roleType.sizeOfRealmPrincipalArray(); j++) {
                    Principal principal = buildRealmPrincipal(roleType.getRealmPrincipalArray(j), bundle);
                    add(roleName, principal, principalRoleMap);
                }

                for (int j = 0; j < roleType.sizeOfLoginDomainPrincipalArray(); j++) {
                    Principal principal = buildDomainPrincipal(roleType.getLoginDomainPrincipalArray(j), bundle);
                    add(roleName, principal, principalRoleMap);
                }

                for (int j = 0; j < roleType.sizeOfPrincipalArray(); j++) {
                    Principal principal = buildPrincipal(roleType.getPrincipalArray(j), bundle);
                    add(roleName, principal, principalRoleMap);
                }

            }
        }

        SubjectInfo defaultSubjectInfo = buildSubjectInfo(securityType.getDefaultSubject());
        AbstractNameQuery credentialStoreName;
        if (securityType.isSetCredentialStoreRef()) {
            PatternType credentialStoreType = securityType.getCredentialStoreRef();
            credentialStoreName = SingleGBeanBuilder.buildAbstractNameQuery(credentialStoreType, GBeanInfoBuilder.DEFAULT_J2EE_TYPE, Collections.singleton(CredentialStore.class.getName()));
        } else {
            credentialStoreName = this.defaultCredentialStoreName;
        }
        Naming naming = deploymentContext.getNaming();
        String name = securityType.isSetName() ? securityType.getName() : "RoleMapper";
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

    protected GBeanData configureApplicationPolicyManager(AbstractName applicationPolicyManagerName, Map<String, ComponentPermissions> contextIDToPermissionsMap) {        
        GBeanData jaccBeanData = new GBeanData(applicationPolicyManagerName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        return jaccBeanData;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return SECURITY_QNAME_SET;
    }

    public QName getBaseQName() {
        return BASE_SECURITY_QNAME;
    }


}
