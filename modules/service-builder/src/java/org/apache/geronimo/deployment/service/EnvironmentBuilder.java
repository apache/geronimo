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
import org.apache.geronimo.deployment.Environment;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.ClassloaderType;
import org.apache.geronimo.deployment.xbeans.EnvironmentDocument;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.NameKeyType;
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

        NameKeyType[] nameKeyArray = environmentType.getNameKeyArray();
        Map nameKeyMap = new HashMap();
        for (int i = 0; i < nameKeyArray.length; i++) {
            NameKeyType nameKey = nameKeyArray[i];
            String key = nameKey.getKey().trim();
            String value = nameKey.getValue().trim();
            nameKeyMap.put(key, value);
        }
        environment.setNameKeys(nameKeyMap);

        if (environmentType.isSetClassloader()) {
            ClassloaderType classloaderType = environmentType.getClassloader();
            environment.setImports(toArtifacts(classloaderType.getImportArray()));
            environment.setDependencies(toArtifacts(classloaderType.getDependencyArray()));
            environment.setIncludes(toArtifacts(classloaderType.getIncludeArray()));

            environment.setInverseClassloading(classloaderType.isSetInverseClassloading());
            environment.setSuppressDefaultEnvironment(classloaderType.isSetSuppressDefaultEnvironment());
            environment.setHiddenClasses(toFilters(classloaderType.getHiddenClassesArray()));
            environment.setNonOverrideableClasses(toFilters(classloaderType.getNonOverridableClassesArray()));
        }
        environment.setReferences(toArtifacts(environmentType.getReferenceArray()));

        return environment;
    }

    public static void mergeEnvironments(Environment environment, Environment additionalEnvironment) {
        if (additionalEnvironment != null) {
            environment.addNameKeys(additionalEnvironment.getNameKeys());
            environment.addImports(additionalEnvironment.getImports());
            environment.addDependencies(additionalEnvironment.getDependencies());
            environment.addIncludes(additionalEnvironment.getIncludes());
            environment.setInverseClassloading(environment.isInverseClassloading() || additionalEnvironment.isInverseClassloading());
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
        ArtifactType configId = environmentType.addNewConfigId();
        toArtifactType(configId, environment.getConfigId());
        for (Iterator iterator = environment.getNameKeys().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            NameKeyType nameKeyType = environmentType.addNewNameKey();
            nameKeyType.setKey(key);
            nameKeyType.setValue(value);
        }
        ClassloaderType classloaderType = environmentType.addNewClassloader();
        classloaderType.setImportArray(toArtifactTypes(environment.getImports()));
        classloaderType.setIncludeArray(toArtifactTypes(environment.getIncludes()));
        classloaderType.setDependencyArray(toArtifactTypes(environment.getDependencies()));
        if (environment.isInverseClassloading()) {
            classloaderType.addNewInverseClassloading();
        }
        if (environment.isSuppressDefaultEnvironment()) {
            classloaderType.addNewSuppressDefaultEnvironment();
        }
        classloaderType.setHiddenClassesArray(toFilterType(environment.getHiddenClasses()));
        classloaderType.setNonOverridableClassesArray(toFilterType(environment.getNonOverrideableClasses()));
        environmentType.setReferenceArray(toArtifactTypes(environment.getReferences()));
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

    private static ArtifactType[] toArtifactTypes(Collection artifacts) {
        ArtifactType[] artifactTypes = new ArtifactType[artifacts.size()];
        int i = 0;
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            ArtifactType artifactType = ArtifactType.Factory.newInstance();
            toArtifactType(artifactType, artifact);
            artifactTypes[i++] = artifactType;
        }
        return artifactTypes;
    }

    private static void toArtifactType(ArtifactType artifactType, Artifact artifact) {
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
