/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.kernel.config;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.ImportType;

/**
 * @version $Rev$ $Date$
 */
public class DependencyNodeUtil {

    //TODO not clear if we need to pass in ConfigurationManager rather than configsById
    public static  List<Configuration> getAllServiceParents(DependencyNode root, ConfigurationManager configurationManager) throws NoSuchConfigException {
        List<Configuration> ancestors = new ArrayList<Configuration>();
        Set<Artifact> ids = new HashSet<Artifact>();
        addDepthFirstServiceParents(root, ancestors, ids, configurationManager);
        return ancestors;
    }

    private static void addDepthFirstServiceParents(DependencyNode root, List<Configuration> ancestors, Set<Artifact> ids, ConfigurationSource configurationManager) throws NoSuchConfigException {
        Artifact id = root.getId();
        if (!ids.contains(id)) {
            Configuration configuration = getConfiguration(id, configurationManager, ids);
            ancestors.add(configuration);
            ids.add(root.getId());
            for (Artifact parentId : root.getServiceParents()) {
                DependencyNode parent = getConfiguration(parentId, configurationManager, ids).getDependencyNode();
                addDepthFirstServiceParents(parent, ancestors, ids, configurationManager);
            }
        }
    }

    private static Configuration getConfiguration(Artifact id, ConfigurationSource configurationManager, Object info) throws NoSuchConfigException {
        Configuration configuration = configurationManager.getConfiguration(id);
        if (configuration == null) {
            throw new NoSuchConfigException(id, "Configuration " + id + " not found in configuration manager " + configurationManager + " after finding: " + info);
        }
        return configuration;
    }

    public static void addClassParents(DependencyNode node, LinkedHashSet<Configuration> parents, ConfigurationSource configurationManager) throws NoSuchConfigException {
        for (Artifact artifact: node.getClassParents()) {
            parents.add(getConfiguration(artifact, configurationManager, parents));
        }
    }
    public static void addServiceParents(DependencyNode node, LinkedHashSet<Configuration> parents, ConfigurationSource configurationManager) throws NoSuchConfigException {
        for (Artifact artifact: node.getServiceParents()) {
            parents.add(getConfiguration(artifact, configurationManager, parents));
        }
    }

    public static DependencyNode toDependencyNode(Environment environment, ArtifactResolver artifactResolver, ConfigurationManager configurationFilter) throws MissingDependencyException {
        Artifact id = environment.getConfigId();
        LinkedHashSet<Artifact> classParents = new LinkedHashSet<Artifact>();
        LinkedHashSet<Artifact> serviceParents = new LinkedHashSet<Artifact>();
        for (Dependency dependency: environment.getDependencies()) {
            try {
                Artifact parent = artifactResolver.resolveInClassLoader(dependency.getArtifact());
                if (configurationFilter.isConfiguration(parent)) {
                    if (dependency.getImportType() == ImportType.ALL || dependency.getImportType() == ImportType.SERVICES) {
                        serviceParents.add(parent);
                    }
                    if (dependency.getImportType() == ImportType.ALL || dependency.getImportType() == ImportType.CLASSES) {
                        classParents.add(parent);
                    }
                } else {
                    if (dependency.getImportType() == ImportType.SERVICES) {
                        throw new MissingDependencyException("Not a configuration but import type services only", parent, id);
                    }
                }
            } catch (MissingDependencyException e) {
                throw (MissingDependencyException)new MissingDependencyException("Attempting to resolve environment: " + environment, dependency.getArtifact(), id).initCause(e);
            }
        }
        return new DependencyNode(id, classParents, serviceParents);
    }

}
