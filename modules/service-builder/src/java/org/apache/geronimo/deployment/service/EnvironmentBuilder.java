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

import org.apache.geronimo.deployment.Environment;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.NameKeyType;
import org.apache.geronimo.deployment.xbeans.ClassloaderType;
import org.apache.geronimo.deployment.xbeans.ClassFilterType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentDocument;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

/**
 * @version $Rev:$ $Date:$
 */
public class EnvironmentBuilder implements XmlAttributeBuilder {
    private final static String NAMESPACE = EnvironmentDocument.type.getDocumentElementName().getNamespaceURI();


    public static Environment buildEnvironment(EnvironmentType environmentType) {
        Environment environment = new Environment();
        environment.setConfigId(toArtifact(environmentType.getConfigId()));

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

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = buildEnvironment(environmentType);
        mergeEnvironments(environment, defaultEnvironment);
        return environment;
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
