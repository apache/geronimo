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
package org.apache.geronimo.console.configcreator.configData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.console.configcreator.AbstractHandler;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.xbeans.geronimo.security.GerDistinguishedNameType;
import org.apache.geronimo.xbeans.geronimo.security.GerLoginDomainPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmPrincipalType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

/**
 * @version $Rev$ $Date$
 */
@RemoteProxy
public class EARHelper {
    //private EARConfigData earConfig;
    public EARHelper() {
        //earConfig = (EARConfigData) WebContextFactory.get().getHttpServletRequest().getSession().getAttribute(
        //        AbstractHandler.EAR_CONFIG_DATA_ID);
    }

    private EARConfigData getEarConfigData(HttpServletRequest request) {
        return (EARConfigData) request.getSession().getAttribute(AbstractHandler.EAR_CONFIG_DATA_ID);
    }

    @DataTransferObject
    public static class EnvironmentJson implements Serializable {
        String groupId;
        String artifactId;
        String version;
        String type;
        String hiddenClasses;
        String nonOverridableClasses;
        List<String> inverseClassLoading = new ArrayList<String>();

        public EnvironmentJson() {
        }

        public EnvironmentJson(EnvironmentType environment) {
            ArtifactType moduleId = environment.getModuleId();
            groupId = moduleId.getGroupId();
            artifactId = moduleId.getArtifactId();
            version = moduleId.getVersion();
            type = moduleId.getType();
            if (environment.isSetHiddenClasses()) {
                hiddenClasses = mergeStrings(environment.getHiddenClasses().getFilterArray());
            }
            if (environment.isSetNonOverridableClasses()) {
                nonOverridableClasses = mergeStrings(environment.getNonOverridableClasses().getFilterArray());
            }
            if (environment.isSetInverseClassloading()) {
                inverseClassLoading.add("true");
            }
        }

        private String mergeStrings(String[] strArray) {
            StringBuilder str = new StringBuilder("");
            for (int i = 0; i < strArray.length; i++) {
                str.append(strArray[i] + ";");
            }
            return str.toString();
        }

        public void save(EnvironmentType environment) {
            ArtifactType moduleId = environment.getModuleId();
            moduleId.setArtifactId(artifactId);

            if (moduleId.isSetGroupId()) {
                moduleId.unsetGroupId();
            }
            if (!isEmpty(groupId)) {
                moduleId.setGroupId(groupId);
            }

            if (moduleId.isSetVersion()) {
                moduleId.unsetVersion();
            }
            if (!isEmpty(version)) {
                moduleId.setVersion(version);
            }

            if (moduleId.isSetType()) {
                moduleId.unsetType();
            }
            if (!isEmpty(type)) {
                moduleId.setType(type);
            }

            if (environment.isSetHiddenClasses()) {
                environment.unsetHiddenClasses();
            }
            if (!isEmpty(hiddenClasses)) {
                String[] splitStrings = getNonEmptyStrings(hiddenClasses.split(";"));
                if (splitStrings.length > 0) {
                    environment.addNewHiddenClasses().setFilterArray(splitStrings);
                }
            }
            if (environment.isSetNonOverridableClasses()) {
                environment.unsetNonOverridableClasses();
            }
            if (!isEmpty(nonOverridableClasses)) {
                String[] splitStrings = getNonEmptyStrings(nonOverridableClasses.split(";"));
                if (splitStrings.length > 0) {
                    environment.addNewNonOverridableClasses().setFilterArray(splitStrings);
                }
            }
            if (environment.isSetInverseClassloading()) {
                environment.unsetInverseClassloading();
            }
            if (inverseClassLoading.size() > 0 && "true".equalsIgnoreCase(inverseClassLoading.get(0))) {
                environment.addNewInverseClassloading();
            }
        }

        private String[] getNonEmptyStrings(String[] strings) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].trim().length() > 0)
                    list.add(strings[i].trim());
            }
            return list.toArray(new String[list.size()]);
        }

        private boolean isEmpty(String s) {
            return s == null || s.trim().equals("");
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHiddenClasses() {
            return hiddenClasses;
        }

        public void setHiddenClasses(String hiddenClasses) {
            this.hiddenClasses = hiddenClasses;
        }

        public String getNonOverridableClasses() {
            return nonOverridableClasses;
        }

        public void setNonOverridableClasses(String nonOverridableClasses) {
            this.nonOverridableClasses = nonOverridableClasses;
        }

        public List<String> getInverseClassLoading() {
            return inverseClassLoading;
        }

        public void setInverseClassLoading(List<String> inverseClassLoading) {
            this.inverseClassLoading = inverseClassLoading;
        }
    }

    @RemoteMethod 
    public EnvironmentJson getEnvironmentJson(HttpServletRequest request) {
        return new EnvironmentJson(getEarConfigData(request).getEnvironmentConfig().getEnvironment());
    }

    @RemoteMethod
    public void saveEnvironmentJson(HttpServletRequest request, EnvironmentJson envJson){
        envJson.save(getEarConfigData(request).getEnvironmentConfig().getEnvironment());
    }

    @DataTransferObject
    public static class DependencyItem implements Serializable {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @DataTransferObject
    public static class DependenciesJsonTree implements Serializable {
        String identifier = "name";
        String label = "name";
        List<DependencyItem> items = new ArrayList<DependencyItem>();

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<DependencyItem> getItems() {
            return items;
        }

        public void setItems(List<DependencyItem> items) {
            this.items = items;
        }

        public DependenciesJsonTree() {
        }

        public DependenciesJsonTree(EnvironmentConfigData environmentConfig) {
            Iterator<String> iter = environmentConfig.getDependenciesSet().iterator();
            while (iter.hasNext()) {
                String depString = iter.next();
                DependencyItem item = new DependencyItem();
                item.setName(depString);
                items.add(item);
            }
        }

        public void save(HashSet<String> dependenciesSet) {
            dependenciesSet.clear();
            for (int i = 0; i < items.size(); i++) {
                String depString = items.get(i).getName();
                dependenciesSet.add(depString);
            }
        }
    }

    @RemoteMethod
    public DependenciesJsonTree getDependenciesJsonTree(HttpServletRequest request) {
        return new DependenciesJsonTree(getEarConfigData(request).getEnvironmentConfig());
    }

    @RemoteMethod
    public void saveDependenciesJsonTree(HttpServletRequest request, DependenciesJsonTree dependenciesJsonTree){
        dependenciesJsonTree.save(getEarConfigData(request).getEnvironmentConfig().getDependenciesSet());
    }

    @DataTransferObject
    public static class SecurityPrincipalJson implements Serializable {
        private String name;
        private String principalName;
        private String type;
        private String className;
        private String domainName;
        private String realmName;

        public SecurityPrincipalJson() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public String getRealmName() {
            return realmName;
        }

        public void setRealmName(String realmName) {
            this.realmName = realmName;
        }
    }

    @DataTransferObject
    public static class SecurityRoleJson implements Serializable {
        private String roleName;
        private SecurityPrincipalJson[] children;

        public SecurityRoleJson() {
        }

        public String getName() {
            return "role = '" + roleName + "'";
        }

        public void setName(String name) {
            int beg = name.indexOf('\'');
            this.roleName = name.substring(beg, name.indexOf('\'', beg + 1));
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public SecurityPrincipalJson[] getChildren() {
            return children;
        }

        public void setChildren(SecurityPrincipalJson[] children) {
            this.children = children;
        }
    }

    @DataTransferObject
    public static class ModuleSecurityJsonTree implements Serializable {
        SecurityRoleJson[] items;

        public String getIdentifier() {
            return "name";
        }

        public void setIdentifier(String name) {
        }

        public String getLabel() {
            return "name";
        }

        public void setLabel(String label) {
        }

        public SecurityRoleJson[] getItems() {
            return items;
        }

        public void setItems(SecurityRoleJson[] items) {
            this.items = items;
        }

        public ModuleSecurityJsonTree() {
        }

        public ModuleSecurityJsonTree(WARConfigData warConfig) {
            GerRoleType[] roles = warConfig.getSecurity().getRoleMappings().getRoleArray();
            items = new SecurityRoleJson[roles.length];

            for (int i = 0; i < roles.length; i++) {
                SecurityRoleJson jRole = new SecurityRoleJson();
                GerRoleType role = roles[i];
                String roleName = role.getRoleName();
                jRole.setRoleName(roleName);

                GerPrincipalType[] principals = role.getPrincipalArray();
                GerLoginDomainPrincipalType[] loginDomainPrincipals = role.getLoginDomainPrincipalArray();
                GerRealmPrincipalType[] realmPrincipals = role.getRealmPrincipalArray();
                GerDistinguishedNameType[] distinguishedNames = role.getDistinguishedNameArray();

                int jLength = principals.length + loginDomainPrincipals.length + realmPrincipals.length + distinguishedNames.length;

                if (jLength > 0) {
                    SecurityPrincipalJson[] jPrincipals = new SecurityPrincipalJson[jLength];
                    int jIndex;

                    for (int j = 0; j < principals.length; j++) {
                        SecurityPrincipalJson jPrincipal = new SecurityPrincipalJson();
                        jPrincipal.setPrincipalName(principals[j].getName());
                        jPrincipal.setClassName(principals[j].getClass1());
                        jPrincipal.setType("Principal");
                        jPrincipal.setName(roleName + ".principal" + (1 + j));
                        jPrincipals[j] = jPrincipal;
                    }
                    jIndex = principals.length;

                    for (int j = 0; j < loginDomainPrincipals.length; j++) {
                        SecurityPrincipalJson jPrincipal = new SecurityPrincipalJson();
                        jPrincipal.setPrincipalName(loginDomainPrincipals[j].getName());
                        jPrincipal.setClassName(loginDomainPrincipals[j].getClass1());
                        jPrincipal.setDomainName(loginDomainPrincipals[j].getDomainName());
                        jPrincipal.setType("LoginDomainPrincipal");
                        jPrincipal.setName(roleName + ".principal" + (1 + j + jIndex));
                        jPrincipals[j + jIndex] = jPrincipal;
                    }
                    jIndex += loginDomainPrincipals.length;

                    for (int j = 0; j < realmPrincipals.length; j++) {
                        SecurityPrincipalJson jPrincipal = new SecurityPrincipalJson();
                        jPrincipal.setPrincipalName(realmPrincipals[j].getName());
                        jPrincipal.setClassName(realmPrincipals[j].getClass1());
                        jPrincipal.setDomainName(realmPrincipals[j].getDomainName());
                        jPrincipal.setRealmName(realmPrincipals[j].getRealmName());
                        jPrincipal.setType("RealmPrincipal");
                        jPrincipal.setName(roleName + ".principal" + (1 + j + jIndex));
                        jPrincipals[j + jIndex] = jPrincipal;
                    }
                    jIndex += realmPrincipals.length;

                    for (int j = 0; j < distinguishedNames.length; j++) {
                        SecurityPrincipalJson jPrincipal = new SecurityPrincipalJson();
                        jPrincipal.setPrincipalName(distinguishedNames[j].getName());
                        jPrincipal.setType("DistinguishedName");
                        jPrincipal.setName(roleName + ".principal" + (1 + j + jIndex));
                        jPrincipals[j + jIndex] = jPrincipal;
                    }

                    jRole.setChildren(jPrincipals);
                }
                items[i] = jRole;
            }
        }

        public void save(WARConfigData warConfig, Hashtable<String, Subject> runAsSubjects) {
            if (warConfig.getSecurity().isSetRoleMappings())
                warConfig.getSecurity().unsetRoleMappings();
            GerRoleMappingsType roleMappings = warConfig.getSecurity().addNewRoleMappings();

            for (int i = 0; i < items.length; i++) {
                SecurityPrincipalJson[] jPrincipals = items[i].getChildren();
                SecurityRoleJson item = items[i];

                GerRoleType role = roleMappings.addNewRole();
                role.setRoleName(item.getRoleName());

                for (int j = 0; j < jPrincipals.length; j++) {
                    SecurityPrincipalJson jPrincipal = jPrincipals[j];
                    String type = jPrincipal.getType();

                    if (type.equals("Principal")) {
                        GerPrincipalType principal = role.addNewPrincipal();
                        principal.setName(jPrincipal.getPrincipalName());
                        principal.setClass1(jPrincipal.getClassName());
                    } else if (type.equals("LoginDomainPrincipal")) {
                        GerLoginDomainPrincipalType principal = role.addNewLoginDomainPrincipal();
                        principal.setName(jPrincipal.getPrincipalName());
                        principal.setClass1(jPrincipal.getClassName());
                        principal.setDomainName(jPrincipal.getDomainName());
                    } else if (type.equals("RealmPrincipal")) {
                        GerRealmPrincipalType principal = role.addNewRealmPrincipal();
                        principal.setName(jPrincipal.getPrincipalName());
                        principal.setClass1(jPrincipal.getClassName());
                        principal.setDomainName(jPrincipal.getDomainName());
                        principal.setRealmName(jPrincipal.getRealmName());
                    } else if (type.equals("DistinguishedName")) {
                        GerDistinguishedNameType principal = role.addNewDistinguishedName();
                        principal.setName(jPrincipal.getPrincipalName());
                    }
                }

                if (runAsSubjects != null
                        && runAsSubjects.containsKey(item.getRoleName())) {
                    GerSubjectInfoType gerRunAsSubject = role.addNewRunAsSubject();
                    Subject runAsSubject = runAsSubjects.get(item.getRoleName());
                    gerRunAsSubject.setId(runAsSubject.getId());
                    gerRunAsSubject.setRealm(runAsSubject.getRealm());
                }
            }
        }
    }

    @DataTransferObject
    public static class Subject implements Serializable {
        String realm;
        String id;

        public Subject() {
        }

        public Subject(String id, String realm) {
            this.id = id;
            this.realm = realm;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @DataTransferObject
    public static class CredentialStoreRef implements Serializable {
        String groupId;
        String artifactId;
        String version;
        String type;
        String module;
        String name;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CredentialStoreRef() {
        }

        public CredentialStoreRef(String groupId, String artifactId,
                String version, String type, String module, String name) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.type = type;
            this.module = module;
            this.name = name;
        }

        public CredentialStoreRef(String combined) {
            String[] values = combined.split("/", 6);
            groupId = values[0];
            artifactId = values[1];
            version = values[2];
            type = values[3];
            module = values[4];
            name = values[5];
        }

        public void save(PatternType credentialStoreRef) {
            credentialStoreRef.setGroupId(groupId);
            credentialStoreRef.setArtifactId(artifactId);
            credentialStoreRef.setVersion(version);
            credentialStoreRef.setType(type);
            credentialStoreRef.setModule(module);
            credentialStoreRef.setName(name);
        }

        public String toString() {
            String combined = groupId;
            if (artifactId != null)
                combined = combined + '/' + artifactId;
            if (version != null)
                combined = combined + '/' + version;
            if (type != null)
                combined = combined + '/' + type;
            if (module != null)
                combined = combined + '/' + module;
            if (name != null)
                combined = combined + '/' + name;

            return combined;
        }
    }

    @DataTransferObject
    public static class ModuleSecurityConfig implements Serializable {
        private ModuleSecurityJsonTree roleMappings;
        private String securityRealmName;
        private Hashtable<String, Subject> runAsSubjects;
        private String defaultSubjectRealm, defaultSubjectId;
        private CredentialStoreRef credentialStoreRef;
        private boolean doasCurrentCaller;
        private boolean useContextHandler;

        public ModuleSecurityConfig() {
        }

        public ModuleSecurityConfig(WARConfigData warConfig) {
            roleMappings = new ModuleSecurityJsonTree(warConfig);
            setSecurityRealmName(warConfig.getWebApp().getSecurityRealmName());

            GerSecurityType security = warConfig.getSecurity();

            GerRoleType[] roles = security.getRoleMappings().getRoleArray();
            runAsSubjects = new Hashtable<String, Subject>();
            for (int i = 0; i < roles.length; i++) {
                GerSubjectInfoType runAsSubject = roles[i].getRunAsSubject();
                if (runAsSubject != null && runAsSubject.getId() != null && runAsSubject.getRealm() != null) {
                    runAsSubjects.put(roles[i].getRoleName(), new Subject(runAsSubject.getId(), runAsSubject.getRealm()));
                }
            }

            GerSubjectInfoType gerDefaultSubject = security.getDefaultSubject();
            if (gerDefaultSubject != null && gerDefaultSubject.getId() != null && gerDefaultSubject.getRealm() != null) {
                defaultSubjectId = gerDefaultSubject.getId();
                defaultSubjectRealm = gerDefaultSubject.getRealm();
            }

            if (security.isSetCredentialStoreRef()) {
                PatternType c = security.getCredentialStoreRef();
                credentialStoreRef = new CredentialStoreRef(c.getGroupId(), c.getArtifactId(), c.getVersion(), c.getType(), c.getModule(), c.getName());
            }
            doasCurrentCaller = security.getDoasCurrentCaller();
            useContextHandler = security.getUseContextHandler();
        }

        public void save(WARConfigData warConfig) {
            roleMappings.save(warConfig, runAsSubjects);
            warConfig.getWebApp().setSecurityRealmName(securityRealmName);

            GerSecurityType security = warConfig.getSecurity();

            if (security.isSetDefaultSubject())
                security.unsetDefaultSubject();
            if (defaultSubjectRealm != null && defaultSubjectId != null) {
                GerSubjectInfoType gerDefaultSubject = security
                        .addNewDefaultSubject();
                gerDefaultSubject.setId(defaultSubjectId);
                gerDefaultSubject.setRealm(defaultSubjectRealm);
            }

            if (security.isSetCredentialStoreRef())
                security.unsetCredentialStoreRef();
            if (credentialStoreRef != null)
                credentialStoreRef.save(security.addNewCredentialStoreRef());

            if (security.isSetDoasCurrentCaller())
                security.unsetDoasCurrentCaller();
            if (doasCurrentCaller)
                security.setDoasCurrentCaller(doasCurrentCaller);

            if (security.isSetUseContextHandler())
                security.unsetUseContextHandler();
            if (useContextHandler)
                security.setUseContextHandler(useContextHandler);
        }

        public String getSecurityRealmName() {
            return securityRealmName;
        }

        public void setSecurityRealmName(String securityRealmName) {
            this.securityRealmName = securityRealmName;
        }

        public ModuleSecurityJsonTree getRoleMappings() {
            return roleMappings;
        }

        public void setRoleMappings(ModuleSecurityJsonTree roleMappings) {
            this.roleMappings = roleMappings;
        }

        public Hashtable<String, Subject> getRunAsSubjects() {
            return runAsSubjects;
        }

        public void setRunAsSubjects(Hashtable<String, Subject> runAsSubjects) {
            this.runAsSubjects = runAsSubjects;
        }

        public boolean isDoasCurrentCaller() {
            return doasCurrentCaller;
        }

        public void setDoasCurrentCaller(boolean doasCurrentCaller) {
            this.doasCurrentCaller = doasCurrentCaller;
        }

        public boolean isUseContextHandler() {
            return useContextHandler;
        }

        public void setUseContextHandler(boolean useContextHandler) {
            this.useContextHandler = useContextHandler;
        }

        public String getCredentialStoreRef() {
            if (credentialStoreRef != null)
                return credentialStoreRef.toString();
            return "";
        }

        public void setCredentialStoreRef(String credentialStoreRef) {
            if (credentialStoreRef.trim().length() == 0) {
                this.credentialStoreRef = null;
            } else {
                this.credentialStoreRef = new CredentialStoreRef(credentialStoreRef);
            }
        }

        public String getDefaultSubjectRealm() {
            return defaultSubjectRealm;
        }

        public void setDefaultSubjectRealm(String defaultSubjectRealm) {
            this.defaultSubjectRealm = defaultSubjectRealm;
        }

        public String getDefaultSubjectId() {
            return defaultSubjectId;
        }

        public void setDefaultSubjectId(String defaultSubjectId) {
            this.defaultSubjectId = defaultSubjectId;
        }
    }

    @DataTransferObject
    public static class SecurityJson implements Serializable {
        private Hashtable<String, ModuleSecurityConfig> webModules = new Hashtable<String, ModuleSecurityConfig>();

        // TODO EJB Modules
        // private Hashtable<String, ModuleSecurityConfig> ejbModules = new
        // Hashtable<String, ModuleSecurityConfig>();
        public SecurityJson() {
        }

        public SecurityJson(EARConfigData earConfig) {
            Hashtable<String, WARConfigData> webConfigs = earConfig.getWebModules();

            Enumeration keys = webConfigs.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                webModules.put(key, new ModuleSecurityConfig(webConfigs.get(key)));
            }
        }

        public Hashtable<String, ModuleSecurityConfig> getWebModules() {
            return webModules;
        }

        public void setWebModules(Hashtable<String, ModuleSecurityConfig> webModules) {
            this.webModules = webModules;
        }

        public void save(EARConfigData earConfig) {
            Hashtable<String, WARConfigData> webConfigs = earConfig
                    .getWebModules();

            Enumeration keys = webConfigs.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                webModules.get(key).save(webConfigs.get(key));
            }
        }
    }

    @RemoteMethod
    public SecurityJson getSecurityJson(HttpServletRequest request) {
        return new SecurityJson(getEarConfigData(request));
    }

    @RemoteMethod
    public void saveSecurityJson(HttpServletRequest request, SecurityJson securityData) {
        securityData.save(getEarConfigData(request));
    }

    @RemoteMethod
    public String getGeneratedPlan(HttpServletRequest request) {
        return getEarConfigData(request).getDeploymentPlan();
    }

    @RemoteMethod
    public String saveGeneratedPlan(HttpServletRequest request, String plan) {
        return getEarConfigData(request).setDeploymentPlan(plan);
    }
    /*@RemoteMethod
    public String[] getWebModules() {
        return null;
    }

    @RemoteMethod
    public String[] getEjbModules() {
        return null;
    }

    @RemoteMethod
    public String[] getSessionBeans(String ejbModuleName) {
        return null;
    }

    @RemoteMethod
    public String[] getMDBs(String ejbModuleName) {
        return null;
    }*/
}
