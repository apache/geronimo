/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.config;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.io.File;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationResolver {
    private final Artifact configurationId;
    private final ArtifactResolver artifactResolver;
    private final Collection repositories;
    private final File baseDir;
    private final ConfigurationStore configurationStore;

    public ConfigurationResolver(Artifact configurationId, File baseDir) {
        if (configurationId == null)  throw new NullPointerException("configurationId is null");

        this.configurationId = configurationId;
        this.baseDir = baseDir;
        artifactResolver = null;
        repositories = Collections.EMPTY_SET;
        configurationStore = null;
    }

    public ConfigurationResolver(Artifact configurationId, File baseDir, Collection repositories) {
        if (configurationId == null)  throw new NullPointerException("configurationId is null");
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.configurationId = configurationId;
        if (!repositories.isEmpty()) {
            this.artifactResolver = new DefaultArtifactResolver(null, repositories);
        } else {
            this.artifactResolver = null;
        }
        this.repositories = repositories;
        this.baseDir = baseDir;
        configurationStore = null;
    }

    public ConfigurationResolver(Artifact configurationId, File baseDir, Collection repositories, ArtifactResolver artifactResolver) {
        if (configurationId == null)  throw new NullPointerException("configurationId is null");
        if (repositories == null) repositories = Collections.EMPTY_SET;

        this.configurationId = configurationId;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
        this.baseDir = baseDir;
        configurationStore = null;
    }

    public ConfigurationResolver(Artifact configurationId, ConfigurationStore configurationStore, Collection repositories, ArtifactResolver artifactResolver) {
        if (configurationId == null)  throw new NullPointerException("configurationId is null");
        if (repositories == null) repositories = Collections.EMPTY_SET;
        if (configurationStore == null)  throw new NullPointerException("configurationStore is null");

        this.configurationId = configurationId;
        this.artifactResolver = artifactResolver;
        this.repositories = repositories;
        this.configurationStore = configurationStore;
        baseDir = null;
    }

    public File resolve(Artifact artifact) throws MissingDependencyException {
        for (Iterator j = repositories.iterator(); j.hasNext();) {
            Repository repository = (Repository) j.next();
            if (repository.contains(artifact)) {
                File file = repository.getLocation(artifact);
                return file;
            }
        }
        throw new MissingDependencyException("Unable to resolve dependency " + artifact);
    }

    public URL resolve(URI uri) throws MalformedURLException, NoSuchConfigException {
        if (configurationStore != null) {
            return configurationStore.resolve(configurationId, uri);
        } else if (baseDir != null) {
            return new File(baseDir, uri.toString()).toURL();
        } else {
            throw new IllegalStateException("No configurationStore or baseDir supplied so paths can not be resolved");
        }
    }

    public List resolveTransitiveDependencies(Collection parents, List dependencies) throws MissingDependencyException {
        List resolvedDependencies = new ArrayList();
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Dependency dependency = resolveDependency(parents, (Dependency) iterator.next());

            if (!resolvedDependencies.contains(dependency)) {
                resolvedDependencies.add(dependency);

                List childDependencies = getChildDependencies(dependency);
                if (!childDependencies.isEmpty()) {
                    childDependencies = resolveTransitiveDependencies(parents, childDependencies);
                    resolvedDependencies.addAll(childDependencies);
                }
            }
        }
        return resolvedDependencies;
    }

    private Dependency resolveDependency(Collection parents, Dependency dependency) throws MissingDependencyException {
        Artifact artifact = dependency.getArtifact();

        // if it is already resolved we are done
        if (artifact.isResolved()) {
            return dependency;
        }

        // we need an artifact resolver at this point
        if (artifactResolver == null) {
            throw new MissingDependencyException("Artifact is not resolved and there no artifact resolver available: " + artifact);
        }

        // resolve the artifact
        artifact = artifactResolver.resolve(parents, artifact);

        // build a new dependency object to contain the resolved artifact
        Dependency resolvedDependency = new Dependency(artifact, dependency.getImportType());
        return resolvedDependency;
    }

    private ArrayList getChildDependencies(Dependency dependency) {
        ArrayList childDependencies = new ArrayList();
        for (Iterator repositoryIterator = repositories.iterator(); repositoryIterator.hasNext();) {
            Repository repository = (Repository) repositoryIterator.next();
            if (repository.contains(dependency.getArtifact())) {
                // get the child artifacts
                LinkedHashSet childArtifacts = repository.getDependencies(dependency.getArtifact());
                for (Iterator artifactIterator = childArtifacts.iterator(); artifactIterator.hasNext();) {
                    Artifact artifact = (Artifact) artifactIterator.next();
                    // add each child as a classes-only dependency
                    childDependencies.add(new Dependency(artifact,  ImportType.CLASSES));
                }
            }
        }
        return childDependencies;
    }
}
