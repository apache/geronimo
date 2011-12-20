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
package org.apache.geronimo.kernel.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;

/**
 * @version $Rev$ $Date$
 */

@Component(componentAbstract = true)
public class DefaultArtifactResolver implements ArtifactResolver {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ArtifactManager artifactManager;

    @Reference(name = "repository", referenceInterface = ListableRepository.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    private final Collection<ListableRepository> repositories = new LinkedHashSet<ListableRepository>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY, policy = ReferencePolicy.DYNAMIC)
    private ConfigurationManager configurationManager;

    private final Map<Artifact, Artifact> explicitResolution = new ConcurrentHashMap<Artifact, Artifact>();

    public DefaultArtifactResolver() {
    }

    public DefaultArtifactResolver(ArtifactManager artifactManager, ListableRepository repository) {
        this.artifactManager = artifactManager;
        if (repository != null) {
            repositories.add(repository);
        }
    }

    public DefaultArtifactResolver(
            @ParamReference(name="ArtifactManager", namingType = "ArtifactManager")ArtifactManager artifactManager,
            @ParamReference(name="Repositories", namingType = "Repository")Collection<ListableRepository> repositories,
            @ParamAttribute(name="explicitResolution")Map<Artifact, Artifact> explicitResolution,
            @ParamReference(name="ConfigurationManagers", namingType = "ConfigurationManager")Collection<ConfigurationManager> configurationManagers) {
        this.artifactManager = artifactManager;
        this.repositories.addAll(repositories);
        if (explicitResolution != null) {
            this.explicitResolution.putAll(explicitResolution);
        }
        if (!configurationManagers.isEmpty()) {
            this.configurationManager = configurationManagers.iterator().next();
        }
    }

    public void setArtifactManager(ArtifactManager artifactManager) {
        this.artifactManager = artifactManager;
    }

    public void unsetArtifactManager(ArtifactManager artifactManager) {
        if (this.artifactManager == artifactManager) {
            this.artifactManager = null;
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void unsetConfigurationManager(ConfigurationManager configurationManager) {
        if (this.configurationManager == configurationManager) {
            this.configurationManager = null;
        }
    }

    public void bindRepository(ListableRepository repository) {
        repositories.add(repository);
    }

    public void unbindRepository(ListableRepository repository) {
        repositories.remove(repository);
    }

    protected Map<Artifact, Artifact> getExplicitResolution() {
        return explicitResolution;
    }

    protected void activate(Map<Artifact, Artifact> explicitResolution) {
        this.explicitResolution.putAll(explicitResolution);
    }

    @Override
    public Artifact generateArtifact(Artifact source, String defaultType) {
        if(source.isResolved()) {
            Artifact deAliased = explicitResolution.get(source);
            if (deAliased !=  null) {
                return deAliased;
            }
            return source;
        }
        String groupId = source.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : source.getGroupId();
        String artifactId = source.getArtifactId();
        String type = source.getType() == null ? defaultType : source.getType();
        Version version = source.getVersion() == null ? new Version(Long.toString(System.currentTimeMillis())) : source.getVersion();

        return new Artifact(groupId, artifactId, version, type);
    }

    @Override
    public Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException {
        Artifact[] all = queryArtifacts(artifact);
        if(all.length > 1) {
            throw new MultipleMatchesException(artifact);
        }
        return all.length == 0 ? null : all[0];
    }

    @Override
    public Artifact[] queryArtifacts(Artifact artifact) {
        //see if there is an explicit resolution for this artifact.
        Artifact deAliased = explicitResolution.get(artifact);
        if (deAliased != null) {
            artifact = deAliased;
        }
        LinkedHashSet<Artifact> set = new LinkedHashSet<Artifact>();
        for (ListableRepository repository : repositories) {
            set.addAll(repository.list(artifact));
        }
        return set.toArray(new Artifact[set.size()]);
    }

    @Override
    public LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> artifacts) throws MissingDependencyException {
        return resolveInClassLoader(artifacts, Collections.<Configuration>emptySet());
    }

    @Override
    public LinkedHashSet<Artifact> resolveInClassLoader(Collection<Artifact> artifacts, Collection<Configuration> parentConfigurations) throws MissingDependencyException {
        LinkedHashSet<Artifact> resolvedArtifacts = new LinkedHashSet<Artifact>();
        for (Artifact artifact : artifacts) {
            if (!artifact.isResolved()) {
                artifact = resolveInClassLoader(artifact, parentConfigurations);
            }
            resolvedArtifacts.add(artifact);
        }
        return resolvedArtifacts;
    }

    @Override
    public Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException {
        return resolveInClassLoader(source, Collections.<Configuration>emptySet());
    }

    @Override
    public Artifact resolveInClassLoader(Artifact source, Collection<Configuration> parentConfigurations) throws MissingDependencyException {
        Artifact working = resolveVersion(parentConfigurations, source);
        if (working == null || !working.isResolved()) {
            //todo can parentConfigurations be included?
            throw new MissingDependencyException(source);
        }

        return working;
    }

    private Artifact resolveVersion(Collection<Configuration> parentConfigurations, Artifact working) {
        //see if there is an explicit resolution for this artifact.
        Artifact deAliased = explicitResolution.get(working);
        if (deAliased != null) {
            working = deAliased;
        }
        if (working.isResolved()) {
            return working;
        }
        SortedSet existingArtifacts;
        if (artifactManager != null) {
            existingArtifacts = artifactManager.getLoadedArtifacts(working);
        } else {
            existingArtifacts = new TreeSet();
        }

        // if we have exactly one artifact loaded use its' version
        if (existingArtifacts.size() == 1) {
            return (Artifact) existingArtifacts.first();
        }


        // if we have no existing loaded artifacts grab the highest version from the repository
        if (existingArtifacts.size() == 0) {
            SortedSet<Artifact> list = new TreeSet<Artifact>();
            for (ListableRepository repository : repositories) {
                list.addAll(repository.list(working));
            }

            if (list.isEmpty()) {
                return null;
            }
            return list.last();
        }

        // more than one version of the artifact was loaded...

        // if one of parents already loaded the artifact, use that version
        Artifact artifact = searchParents(parentConfigurations, working);
        if (artifact != null) {
            return artifact;
        }

        // it wasn't declared by the parent so just use the highest verstion already loaded
        return (Artifact) existingArtifacts.last();
    }

    //TODO not clear modifications to use artifactManager work for SimpleConfigurationManager
    private Artifact searchParents(Collection<Configuration> parentConfigurations, Artifact working) {
        for (Configuration configuration : parentConfigurations) {
            Artifact artifact = searchParent(configuration, working);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }

    private Artifact searchParent(Configuration configuration, Artifact working) {
        // check if this parent matches the groupId, artifactId, and type
        if (matches(configuration.getId(), working)) {
            return configuration.getId();
        }

        Environment environment = configuration.getEnvironment();
        if (environment.getClassLoadingRules().isInverseClassLoading()) {
            // Search dependencies of the configuration before searching the parents
//                Artifact artifact = getArtifactVersion(configuration.getDependencies(), working);
            Artifact artifact = getArtifactVersion(artifactManager.getLoadedArtifacts(configuration.getId()), working);
            if (artifact != null) {
                return artifact;
            }

            // wasn't declared in the dependencies, so search the parents of the configuration
            artifact = searchOneParent(configuration, working);
            if (artifact != null) {
                return artifact;
            }

        } else {
            // Search the parents before the dependencies of the configuration
            Artifact artifact = searchOneParent(configuration, working);

            if (artifact != null) {
                return artifact;
            }

            // wasn't declared in a parent check the dependencies of the configuration
//                Artifact artifact = getArtifactVersion(configuration.getDependencies(), working);
            artifact = getArtifactVersion(artifactManager.getLoadedArtifacts(configuration.getId()), working);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }

    private Artifact searchOneParent(Configuration configuration, Artifact working) {
        LinkedHashSet<Artifact> parentIds = configuration.getDependencyNode().getClassParents();
        if (!parentIds.isEmpty()) {
            for (Artifact parentId: parentIds) {
                Configuration parent = getConfiguration(parentId);
                Artifact artifact = searchParent(parent, working);
                if (artifact != null) {
                    return artifact;
                }
            }
        }
        return null;
    }

    private Configuration getConfiguration(Artifact parentId) {
        if (configurationManager == null) {
            throw new IllegalStateException("no configuration manager");
        }
        return configurationManager.getConfiguration(parentId);
    }

    private Artifact getArtifactVersion(Collection<Artifact> artifacts, Artifact query) {
        for (Artifact artifact : artifacts) {
            if (matches(artifact, query)) {
                return artifact;
            }
        }
        return null;
    }

    private boolean matches(Artifact candidate, Artifact query) {
        return query.matches(candidate);
    }

}