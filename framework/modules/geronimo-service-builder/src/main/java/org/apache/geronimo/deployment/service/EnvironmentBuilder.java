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

package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.DependenciesType;
import org.apache.geronimo.deployment.xbeans.EnvironmentDocument;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ImportType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentBuilder extends PropertyEditorSupport implements XmlAttributeBuilder {
    private final static QName QNAME = EnvironmentDocument.type.getDocumentElementName();
    private final static String NAMESPACE = QNAME.getNamespaceURI();

    public static Environment buildEnvironment(EnvironmentType environmentType) {
        Environment environment = new Environment();
        if (environmentType != null) {
            if (environmentType.isSetModuleId()) {
                environment.setConfigId(toArtifact(environmentType.getModuleId(), null));
            }

            if (environmentType.isSetDependencies()) {
                DependencyType[] dependencyArray = environmentType.getDependencies().getDependencyArray();
                LinkedHashSet dependencies = toDependencies(dependencyArray);
                environment.setDependencies(dependencies);
            }
            if (environmentType.isSetBundleActivator()) {
                environment.setBundleActivator(trim(environmentType.getBundleActivator()));
            }
            
            for (String bundleClassPath :environmentType.getBundleClassPathArray()){
                environment.addToBundleClassPath(bundleClassPath);
            }
            for (String importPackage: environmentType.getImportPackageArray()) {
                environment.addImportPackage(trim(importPackage));
            }
            for (String exportPackage: environmentType.getExportPackageArray()) {
                environment.addExportPackage(trim(exportPackage));
            }
            for (String requireBundle : environmentType.getRequireBundleArray()) {
                environment.addRequireBundle(requireBundle);
            }
            for (String dynamicImportPackage: environmentType.getDynamicImportPackageArray()) {
                environment.addDynamicImportPackage(trim(dynamicImportPackage));
            }
                        
            environment.setSuppressDefaultEnvironment(environmentType.isSetSuppressDefaultEnvironment());
            
            ClassLoadingRulesUtil.configureRules(environment.getClassLoadingRules(), environmentType);
        }

        return environment;
    }

    public static void mergeEnvironments(Environment environment, Environment additionalEnvironment) {
        if (additionalEnvironment != null) {
            //TODO merge configIds??
            if (environment.getConfigId() == null) {
                environment.setConfigId(additionalEnvironment.getConfigId());
            }
            environment.addDependencies(additionalEnvironment.getDependencies());
            environment.addToBundleClassPath(additionalEnvironment.getBundleClassPath());
            environment.addImportPackages(additionalEnvironment.getImportPackages());
            environment.addExportPackages(additionalEnvironment.getExportPackages());
            environment.addRequireBundles(additionalEnvironment.getRequireBundles());
            environment.addDynamicImportPackages(additionalEnvironment.getDynamicImportPackages());
            if (environment.getBundleActivator() == null && additionalEnvironment.getBundleActivator() != null) {
                environment.setBundleActivator(additionalEnvironment.getBundleActivator());
            }
            
            environment.setSuppressDefaultEnvironment(environment.isSuppressDefaultEnvironment() || additionalEnvironment.isSuppressDefaultEnvironment());
            
            ClassLoadingRules classLoadingRules = environment.getClassLoadingRules();
            ClassLoadingRules additionalClassLoadingRules = additionalEnvironment.getClassLoadingRules();
            classLoadingRules.merge(additionalClassLoadingRules);
        }
    }

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = buildEnvironment(environmentType);
        if (!environment.isSuppressDefaultEnvironment()) {
            mergeEnvironments(environment, defaultEnvironment);
        }
        return environment;
    }

    public static EnvironmentType buildEnvironmentType(Environment environment) {
        EnvironmentType environmentType = EnvironmentType.Factory.newInstance();
        if (environment.getConfigId() != null) {
            ArtifactType configId = toArtifactType(environment.getConfigId());
            environmentType.setModuleId(configId);
        }

        List<DependencyType> dependencies = toDependencyTypes(environment.getDependencies());
        DependencyType[] dependencyTypes = dependencies.toArray(new DependencyType[dependencies.size()]);
        DependenciesType dependenciesType = environmentType.addNewDependencies();
        dependenciesType.setDependencyArray(dependencyTypes);
        
        if (environment.getBundleActivator() != null) {
            environmentType.setBundleActivator(environment.getBundleActivator());
        }
        for (String bundleClassPath: environment.getBundleClassPath()) {
            environmentType.addBundleClassPath(bundleClassPath);
        }
        for (String importPackage: environment.getImportPackages()) {
            environmentType.addImportPackage(importPackage);
        }
        for (String exportPackage: environment.getExportPackages()) {
            environmentType.addExportPackage(exportPackage);
        }
        for (String requireBundle : environment.getRequireBundles()) {
            environmentType.addRequireBundle(requireBundle);
        }
        for (String dynamicImportPackage: environment.getDynamicImportPackages()) {
            environmentType.addDynamicImportPackage(dynamicImportPackage);
        }
        
        ClassLoadingRules classLoadingRules = environment.getClassLoadingRules();
        if (classLoadingRules.isInverseClassLoading()) {
            environmentType.addNewInverseClassloading();
        }
        
        if (environment.isSuppressDefaultEnvironment()) {
            environmentType.addNewSuppressDefaultEnvironment();
        }
        
        ClassLoadingRule classLoadingRule = classLoadingRules.getHiddenRule();
        environmentType.setHiddenClasses(toFilterType(classLoadingRule.getClassPrefixes()));
        
        classLoadingRule = classLoadingRules.getNonOverrideableRule();
        environmentType.setNonOverridableClasses(toFilterType(classLoadingRule.getClassPrefixes()));

        classLoadingRule = classLoadingRules.getPrivateRule();
        environmentType.setPrivateClasses(toFilterType(classLoadingRule.getClassPrefixes()));
        
        return environmentType;
    }

    private static ClassFilterType toFilterType(Set filters) {
        String[] classFilters = (String[]) filters.toArray(new String[filters.size()]);
        ClassFilterType classFilter = ClassFilterType.Factory.newInstance();
        classFilter.setFilterArray(classFilters);
        return classFilter;
    }

    private static List<DependencyType> toDependencyTypes(Collection artifacts) {
        List<DependencyType> dependencies = new ArrayList<DependencyType>();
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Dependency dependency = (Dependency) iterator.next();
            DependencyType artifactType = toDependencyType(dependency);
            dependencies.add(artifactType);
        }
        return dependencies;
    }

    private static ArtifactType toArtifactType(Artifact artifact) {
        ArtifactType artifactType = ArtifactType.Factory.newInstance();
        fillArtifactType(artifact, artifactType);
        return artifactType;
    }

    private static void fillArtifactType(Artifact artifact, ArtifactType artifactType) {
        if (artifact.getGroupId() != null) {
            artifactType.setGroupId(artifact.getGroupId());
        }
        if (artifact.getArtifactId() != null) {
            artifactType.setArtifactId(artifact.getArtifactId());
        }
        if (artifact.getVersion() != null) {
            artifactType.setVersion(artifact.getVersion().toString());
        }
        if (artifact.getType() != null) {
            artifactType.setType(artifact.getType());
        }
    }

    private static DependencyType toDependencyType(Dependency dependency) {
        DependencyType dependencyType = DependencyType.Factory.newInstance();
        fillArtifactType(dependency.getArtifact(), dependencyType);

        org.apache.geronimo.kernel.repository.ImportType importType = dependency.getImportType();
        if (importType == org.apache.geronimo.kernel.repository.ImportType.CLASSES) {
            dependencyType.setImport(ImportType.CLASSES);
        } else if (importType == org.apache.geronimo.kernel.repository.ImportType.SERVICES) {
            dependencyType.setImport(ImportType.SERVICES);
        }

        return dependencyType;
    }

    private static Set toFilters(ClassFilterType filterType) {
        Set filters = new HashSet();
        if (filterType != null) {
            String[] filterArray = filterType.getFilterArray();
            for (int i = 0; i < filterArray.length; i++) {
                String filter = filterArray[i].trim();
                filters.add(filter);
            }
        }
        return filters;
    }

    //package level for testing
    static LinkedHashSet toArtifacts(ArtifactType[] artifactTypes) {
        LinkedHashSet artifacts = new LinkedHashSet();
        for (int i = 0; i < artifactTypes.length; i++) {
            ArtifactType artifactType = artifactTypes[i];
            Artifact artifact = toArtifact(artifactType, "jar");
            artifacts.add(artifact);
        }
        return artifacts;
    }

    private static LinkedHashSet<Dependency> toDependencies(DependencyType[] dependencyArray) {
        LinkedHashSet<Dependency> dependencies = new LinkedHashSet<Dependency>();
        for (int i = 0; i < dependencyArray.length; i++) {
            DependencyType artifactType = dependencyArray[i];
            Dependency dependency = toDependency(artifactType);
            dependencies.add(dependency);
        }
        return dependencies;
    }

    private static Dependency toDependency(DependencyType dependencyType) {
        Artifact artifact = toArtifact(dependencyType, null);
        if (ImportType.CLASSES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);
        } else if (ImportType.SERVICES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.SERVICES);
        } else if (dependencyType.getImport() == null) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
        } else {
            throw new IllegalArgumentException("Unknown import type: " + dependencyType.getImport());
        }
    }

    private static Artifact toArtifact(ArtifactType artifactType, String defaultType) {
        String groupId = artifactType.isSetGroupId() ? trim(artifactType.getGroupId()) : null;
        String type = artifactType.isSetType() ? trim(artifactType.getType()) : defaultType;
        String artifactId = trim(artifactType.getArtifactId());
        String version = artifactType.isSetVersion() ? trim(artifactType.getVersion()) : null;
        return new Artifact(groupId, artifactId, version, type);
    }

    private static String trim(String string) {
        if (string == null || string.length() == 0) {
        return null;
        }
        return string.trim();
    }


    public String getNamespace() {
        return NAMESPACE;
    }

    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {

        EnvironmentType environmentType;
        if (xmlObject instanceof EnvironmentType) {
            environmentType = (EnvironmentType) xmlObject;
        } else {
            environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
        }
        try {
            XmlOptions xmlOptions = new XmlOptions();
            xmlOptions.setLoadLineNumbers();
            Collection errors = new ArrayList();
            xmlOptions.setErrorListener(errors);
            if (!environmentType.validate(xmlOptions)) {
                throw new XmlException("Invalid deployment descriptor: " + errors + "\nDescriptor: " + environmentType.toString(), null, errors);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        return buildEnvironment(environmentType);
    }

    public String getAsText() {
        Environment environment = (Environment) getValue();
        EnvironmentType environmentType = buildEnvironmentType(environment);
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveSyntheticDocumentElement(QNAME);
        xmlOptions.setSavePrettyPrint();
        return environmentType.xmlText(xmlOptions);
    }

    public void setAsText(String text) {
        try {
            EnvironmentDocument environmentDocument = EnvironmentDocument.Factory.parse(text);
            EnvironmentType environmentType = environmentDocument.getEnvironment();
            Environment environment = (Environment) getValue(environmentType, null, null, null);
            setValue(environment);

        } catch (XmlException e) {
            throw new PropertyEditorException(e);
        } catch (DeploymentException e) {
            throw new PropertyEditorException(e);
        }
    }


    //This is added by hand to the xmlAttributeBuilders since it is needed to bootstrap the ServiceConfigBuilder.
}
