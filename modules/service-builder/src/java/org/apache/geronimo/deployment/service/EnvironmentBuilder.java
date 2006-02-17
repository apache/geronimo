/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.deployment.service;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.EnvironmentDocument;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PropertyType;
import org.apache.geronimo.deployment.xbeans.ImportType;
import org.apache.geronimo.deployment.xbeans.PropertiesType;
import org.apache.geronimo.deployment.xbeans.DependenciesType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev:$ $Date:$
 */
public class EnvironmentBuilder implements XmlAttributeBuilder {
    private final static String NAMESPACE = EnvironmentDocument.type.getDocumentElementName().getNamespaceURI();


    public static Environment buildEnvironment(EnvironmentType environmentType) {
        Environment environment = new Environment();
        if (environmentType.isSetConfigId()) {
            environment.setConfigId(toArtifact(environmentType.getConfigId()));
        }

        Map propertiesMap = new HashMap();
        if (environmentType.isSetProperties()) {
            PropertyType[] propertiesArray = environmentType.getProperties().getPropertyArray();
            for (int i = 0; i < propertiesArray.length; i++) {
                PropertyType property = propertiesArray[i];
                String key = property.getName().trim();
                String value = property.getValue().trim();
                propertiesMap.put(key, value);
            }
        }
        environment.setProperties(propertiesMap);

        if (environmentType.isSetDependencies()) {
            ArtifactType[] dependencyArray = environmentType.getDependencies().getDependencyArray();
            Collection dependencies = new LinkedHashSet();
            Collection imports = new LinkedHashSet();
            Collection references = new LinkedHashSet();
            for (int i = 0; i < dependencyArray.length; i++) {
                ArtifactType artifactType = dependencyArray[i];
                Artifact artifact = toArtifact(artifactType);
                if (artifact.getType() == null) {
                    artifact.setType("jar");
                }
                String type = artifact.getType();
                if (type.equals("jar")) {
                    dependencies.add(artifact);
                } else if (type.equals("car")) {
                    if ("classes".equals(artifactType.getImport())) {
                        throw new IllegalArgumentException("classes-only dependency on car files not yet supported");
                    } else if ("services".equals(artifactType.getImport())) {
                        references.add(artifact);
                    } else {
                        imports.add(artifact);
                    }
                }
            }
            environment.setImports(imports);
            environment.setDependencies(dependencies);
            environment.setReferences(references);

        }
        environment.setInverseClassLoading(environmentType.isSetInverseClassloading());
        environment.setSuppressDefaultEnvironment(environmentType.isSetSuppressDefaultEnvironment());
        environment.setHiddenClasses(toFilters(environmentType.getHiddenClassesArray()));
        environment.setNonOverrideableClasses(toFilters(environmentType.getNonOverridableClassesArray()));

        return environment;
    }

    public static void mergeEnvironments(Environment environment, Environment additionalEnvironment) {
        if (additionalEnvironment != null) {
            environment.addProperties(additionalEnvironment.getProperties());
            environment.addImports(additionalEnvironment.getImports());
            environment.addDependencies(additionalEnvironment.getDependencies());
            environment.addIncludes(additionalEnvironment.getIncludes());
            environment.setInverseClassLoading(environment.isInverseClassLoading() || additionalEnvironment.isInverseClassLoading());
            environment.setSuppressDefaultEnvironment(environment.isSuppressDefaultEnvironment() || additionalEnvironment.isSuppressDefaultEnvironment());
            environment.addHiddenClasses(additionalEnvironment.getHiddenClasses());
            environment.addNonOverrideableClasses(additionalEnvironment.getNonOverrideableClasses());
            environment.addReferences(additionalEnvironment.getReferences());
        }
    }

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = buildEnvironment(environmentType);
        mergeEnvironments(environment, defaultEnvironment);
        return environment;
    }

    public static EnvironmentType buildEnvironmentType(Environment environment) {
        EnvironmentType environmentType = EnvironmentType.Factory.newInstance();
        ArtifactType configId = toArtifactType(environment.getConfigId(), null);
        environmentType.setConfigId(configId);

        if (environment.getProperties().size() >0) {
            PropertiesType propertiesType = environmentType.addNewProperties();
            for (Iterator iterator = environment.getProperties().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                PropertyType propertyType = propertiesType.addNewProperty();
                propertyType.setName(name);
                propertyType.setValue(value);
            }
        }
        List dependencies = new ArrayList();
        toArtifactTypes(environment.getImports(), null, dependencies);
//        toArtifactTypes(environment.getIncludes(), null, dependencies));
        toArtifactTypes(environment.getDependencies(), null, dependencies);
        toArtifactTypes(environment.getReferences(), ImportType.SERVICES, dependencies);
        ArtifactType[] artifactTypes = (ArtifactType[]) dependencies.toArray(new ArtifactType[dependencies.size()]);
        DependenciesType dependenciesType = environmentType.addNewDependencies();
        dependenciesType.setDependencyArray(artifactTypes);
        if (environment.isInverseClassLoading()) {
            environmentType.addNewInverseClassloading();
        }
        if (environment.isSuppressDefaultEnvironment()) {
            environmentType.addNewSuppressDefaultEnvironment();
        }
        environmentType.setHiddenClassesArray(toFilterType(environment.getHiddenClasses()));
        environmentType.setNonOverridableClassesArray(toFilterType(environment.getNonOverrideableClasses()));
        return environmentType;
    }

    private static ClassFilterType[] toFilterType(Set filters) {
        ClassFilterType[] classFilters = new ClassFilterType[filters.size()];
        int i = 0;
        for (Iterator iterator = filters.iterator(); iterator.hasNext();) {
            String filter = (String) iterator.next();
            ClassFilterType classFilter  = ClassFilterType.Factory.newInstance();
            classFilter.setFilter(filter);
            classFilters[i++] = classFilter;
        }
        return classFilters;
    }

    private static void toArtifactTypes(Collection artifacts, ImportType.Enum importType, List dependencies) {
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            ArtifactType artifactType = toArtifactType(artifact, importType);
            dependencies.add(artifactType);
        }
    }

    private static ArtifactType toArtifactType(Artifact artifact, ImportType.Enum importType) {
        ArtifactType artifactType = ArtifactType.Factory.newInstance();
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
        if (importType != null) {
            artifactType.setImport(importType);
        }
        return artifactType;
    }

    private static Set toFilters(ClassFilterType[] filterArray) {
        Set filters = new HashSet();
        for (int i = 0; i < filterArray.length; i++) {
            ClassFilterType classFilterType = filterArray[i];
            String filter = classFilterType.getFilter().trim();
            filters.add(filter);
        }
        return filters;
    }

    //package level for testing
    static LinkedHashSet toArtifacts(ArtifactType[] artifactTypes) {
        LinkedHashSet artifacts = new LinkedHashSet();
        for (int i = 0; i < artifactTypes.length; i++) {
            ArtifactType artifactType = artifactTypes[i];
            Artifact artifact = toArtifact(artifactType);
            artifacts.add(artifact);
        }
        return artifacts;
    }

    //TODO make private
    static Artifact toArtifact(ArtifactType artifactType) {
        String groupId = artifactType.isSetGroupId() ? artifactType.getGroupId().trim() : null;
        String type = artifactType.isSetType() ? artifactType.getType().trim() : "jar";
        String artifactId = artifactType.getArtifactId().trim();
        String version = artifactType.isSetVersion() ? artifactType.getVersion().trim() : null;
        return new Artifact(groupId, artifactId, version, type, false);
    }


    public String getNamespace() {
        return NAMESPACE;
    }

    public Object getValue(XmlObject xmlObject, String type, ClassLoader cl) throws DeploymentException {

        EnvironmentType environmentType;
        if (xmlObject instanceof EnvironmentType) {
            environmentType = (EnvironmentType) xmlObject;
        } else {
            environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
        }
        try {
            SchemaConversionUtils.validateDD(environmentType);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        return buildEnvironment(environmentType);
    }

    //This is added by hand to the xmlAttributeBuilders since it is needed to bootstrap the ServiceConfigBuilder.
}
