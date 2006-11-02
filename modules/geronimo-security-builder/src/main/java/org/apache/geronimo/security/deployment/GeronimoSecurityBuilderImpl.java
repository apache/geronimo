/**
 *
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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.RealmPrincipalInfo;
import org.apache.geronimo.security.deploy.LoginDomainPrincipalInfo;
import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.DistinguishedName;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;
import org.apache.geronimo.security.jacc.ApplicationPrincipalRoleConfigurationManager;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerDistinguishedNameType;
import org.apache.geronimo.xbeans.geronimo.security.GerDefaultPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerNamedUsernamePasswordCredentialType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityDocument;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.SecurityBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSecurityBuilderImpl implements SecurityBuilder {
    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private static final QNameSet SECURITY_QNAME_SET = QNameSet.singleton(SECURITY_QNAME);


    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        EARContext earContext = (EARContext) applicationContext;
        XmlObject[] items = container.selectChildren(SECURITY_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of security elements in geronimo plan " + items.length + " qnameset: " + SECURITY_QNAME_SET);
        }
        if (items.length == 1) {
            GerSecurityType securityType = (GerSecurityType) items[0].copy().changeType(GerSecurityType.type);
            Security security = buildSecurityConfig(securityType);
            ClassLoader classLoader = applicationContext.getClassLoader();
            SecurityConfiguration securityConfiguration = buildSecurityConfiguration(security, classLoader);
            earContext.setSecurityConfiguration(securityConfiguration);
        }
        //add the JACC gbean if there is a principal-role mapping and we are on the corect module
        if (earContext.getSecurityConfiguration() != null && applicationContext == moduleContext) {
            Naming naming = earContext.getNaming();
            GBeanData roleMapperData = configureRoleMapper(naming, earContext.getModuleName(), earContext.getSecurityConfiguration());
            try {
                earContext.addGBean(roleMapperData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Role mapper gbean already present", e);
            }
            GBeanData jaccBeanData = configureApplicationPolicyManager(naming, earContext.getModuleName(), earContext.getContextIDToPermissionsMap(), earContext.getSecurityConfiguration());
            jaccBeanData.setReferencePattern("PrincipalRoleMapper", roleMapperData.getAbstractName());
            try {
                earContext.addGBean(jaccBeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("JACC manager gbean already present", e);
            }
            earContext.setJaccManagerName(jaccBeanData.getAbstractName());
        }
    }

    public String getNamespace() {
        XmlBeansUtil.registerSubstitutionGroupElements(org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument.type.getDocumentElementName(), SECURITY_QNAME_SET);

        return GerSecurityDocument.type.getDocumentElementName().getLocalPart();
    }

    private static SecurityConfiguration buildSecurityConfiguration(Security security, ClassLoader classLoader) {
        Map roleDesignates = new HashMap();
        Map principalRoleMap = new HashMap();
        Map roleToPrincipalMap = new HashMap();
        GeronimoSecurityBuilderImpl.buildRolePrincipalMap(security, roleDesignates, roleToPrincipalMap, classLoader);
        GeronimoSecurityBuilderImpl.invertMap(roleToPrincipalMap, principalRoleMap);
        return new SecurityConfiguration(principalRoleMap, roleDesignates, security.getDefaultPrincipal(), security.getDefaultRole(), security.isDoAsCurrentCaller(), security.isUseContextHandler());
    }

    private static Map invertMap(Map roleToPrincipalMap, Map principalRoleMapping) {
        for (Iterator roles = roleToPrincipalMap.entrySet().iterator(); roles.hasNext();) {
            Map.Entry entry = (Map.Entry) roles.next();
            String role = (String) entry.getKey();
            Set principals = (Set) entry.getValue();
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                java.security.Principal principal = (java.security.Principal) iter.next();

                HashSet roleSet = (HashSet) principalRoleMapping.get(principal);
                if (roleSet == null) {
                    roleSet = new HashSet();
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
     * @param security
     * @param roleDesignates
     * @param roleToPrincipalMap
     * @param classLoader
     */
    public static void buildRolePrincipalMap(Security security, Map roleDesignates, Map roleToPrincipalMap, ClassLoader classLoader) {

        Iterator roleMappings = security.getRoleMappings().values().iterator();
        while (roleMappings.hasNext()) {
            Role role = (Role) roleMappings.next();

            String roleName = role.getRoleName();
            Subject roleDesignate = new Subject();
            Set principalSet = new HashSet();

            Iterator realmPrincipals = role.getRealmPrincipals().iterator();
            while (realmPrincipals.hasNext()) {
                RealmPrincipalInfo realmPrincipal = (RealmPrincipalInfo) realmPrincipals.next();
                java.security.Principal principal = ConfigurationUtil.generateRealmPrincipal(realmPrincipal.getRealm(), realmPrincipal.getDomain(), realmPrincipal, classLoader);

                principalSet.add(principal);
                if (realmPrincipal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            Iterator domainPrincipals = role.getLoginDomainPrincipals().iterator();
            while (domainPrincipals.hasNext()) {
                LoginDomainPrincipalInfo domainPrincipal = (LoginDomainPrincipalInfo) domainPrincipals.next();
                java.security.Principal principal = ConfigurationUtil.generateDomainPrincipal(domainPrincipal.getDomain(), domainPrincipal, classLoader);

                principalSet.add(principal);
                if (domainPrincipal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            Iterator principals = role.getPrincipals().iterator();
            while (principals.hasNext()) {
                PrincipalInfo plainPrincipalInfo = (PrincipalInfo) principals.next();
                java.security.Principal principal = ConfigurationUtil.generatePrincipal(plainPrincipalInfo, classLoader);

                principalSet.add(principal);
                if (plainPrincipalInfo.isDesignatedRunAs()) roleDesignate.getPrincipals().add(principal);
            }

            for (Iterator names = role.getDistinguishedNames().iterator(); names.hasNext();) {
                DistinguishedName dn = (DistinguishedName) names.next();

                X500Principal x500Principal = ConfigurationUtil.generateX500Principal(dn.getName());

                principalSet.add(x500Principal);
                if (dn.isDesignatedRunAs()) roleDesignate.getPrincipals().add(x500Principal);
            }

            Set roleMapping = (Set) roleToPrincipalMap.get(roleName);
            if (roleMapping == null) {
                roleMapping = new HashSet();
                roleToPrincipalMap.put(roleName, roleMapping);
            }
            roleMapping.addAll(principalSet);

            if (roleDesignate.getPrincipals().size() > 0) {
                roleDesignates.put(roleName, roleDesignate);
            }
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

                for (int j = 0; j < roleType.sizeOfRealmPrincipalArray(); j++) {
                    role.getRealmPrincipals().add(GeronimoSecurityBuilderImpl.buildRealmPrincipal(roleType.getRealmPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfLoginDomainPrincipalArray(); j++) {
                    role.getLoginDomainPrincipals().add(GeronimoSecurityBuilderImpl.buildDomainPrincipal(roleType.getLoginDomainPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfPrincipalArray(); j++) {
                    role.getPrincipals().add(buildPrincipal(roleType.getPrincipalArray(j)));
                }

                for (int j = 0; j < roleType.sizeOfDistinguishedNameArray(); j++) {
                    GerDistinguishedNameType dnType = roleType.getDistinguishedNameArray(j);

                    role.getDistinguishedNames().add(new DistinguishedName(dnType.getName().trim(), dnType.getDesignatedRunAs()));
                }

                security.getRoleMappings().put(roleName, role);
            }
        }

        security.setDefaultPrincipal(buildDefaultPrincipal(securityType.getDefaultPrincipal()));

        return security;
    }

    //used from app client builder
    public DefaultPrincipal buildDefaultPrincipal(XmlObject xmlObject) {
        GerDefaultPrincipalType defaultPrincipalType = (GerDefaultPrincipalType) xmlObject;
        DefaultPrincipal defaultPrincipal = new DefaultPrincipal();

        defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));
        GerNamedUsernamePasswordCredentialType[] namedCredentials = defaultPrincipalType.getNamedUsernamePasswordCredentialArray();
        if (namedCredentials.length > 0) {
            Set defaultCredentialSet = new HashSet();
            for (int i = 0; i < namedCredentials.length; i++) {
                GerNamedUsernamePasswordCredentialType namedCredentialType = namedCredentials[i];
                NamedUsernamePasswordCredential namedCredential = new NamedUsernamePasswordCredential(namedCredentialType.getUsername().trim(), namedCredentialType.getPassword().trim().toCharArray(), namedCredentialType.getName().trim());
                defaultCredentialSet.add(namedCredential);
            }
            defaultPrincipal.setNamedUserPasswordCredentials(defaultCredentialSet);
        }
        return defaultPrincipal;
    }

    private static RealmPrincipalInfo buildRealmPrincipal(GerRealmPrincipalType realmPrincipalType) {
        return new RealmPrincipalInfo(realmPrincipalType.getDomainName().trim(), realmPrincipalType.getRealmName().trim(), realmPrincipalType.getClass1().trim(), realmPrincipalType.getName().trim(), realmPrincipalType.isSetDesignatedRunAs());
    }

    private static LoginDomainPrincipalInfo buildDomainPrincipal(GerLoginDomainPrincipalType domainPrincipalType) {
        return new LoginDomainPrincipalInfo(domainPrincipalType.getDomainName().trim(), domainPrincipalType.getClass1().trim(), domainPrincipalType.getName().trim(), domainPrincipalType.isSetDesignatedRunAs());
    }

    //used from TSSConfigEditor
    public PrincipalInfo buildPrincipal(XmlObject xmlObject) {
        GerPrincipalType principalType = (GerPrincipalType) xmlObject;
        return new PrincipalInfo(principalType.getClass1().trim(), principalType.getName().trim(), principalType.isSetDesignatedRunAs());
    }

    public GBeanData configureRoleMapper(Naming naming, AbstractName moduleName, Object securityConfiguration) {
        AbstractName roleMapperName = naming.createChildName(moduleName, "RoleMapper", "RoleMapper");
        GBeanData roleMapperData = new GBeanData(roleMapperName, ApplicationPrincipalRoleConfigurationManager.GBEAN_INFO);
        roleMapperData.setAttribute("principalRoleMap", ((SecurityConfiguration) securityConfiguration).getPrincipalRoleMap());
        return roleMapperData;
    }

    public GBeanData configureApplicationPolicyManager(Naming naming, AbstractName moduleName, Map contextIDToPermissionsMap, Object securityConfiguration) {
        AbstractName jaccBeanName = naming.createChildName(moduleName, NameFactory.JACC_MANAGER, NameFactory.JACC_MANAGER);
        GBeanData jaccBeanData = new GBeanData(jaccBeanName, ApplicationPolicyConfigurationManager.GBEAN_INFO);
        jaccBeanData.setAttribute("contextIdToPermissionsMap", contextIDToPermissionsMap);
        jaccBeanData.setAttribute("roleDesignates", ((SecurityConfiguration) securityConfiguration).getRoleDesignates());
        return jaccBeanData;

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(GeronimoSecurityBuilderImpl.class, NameFactory.MODULE_BUILDER);

        infoFactory.addInterface(SecurityBuilder.class);


        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
