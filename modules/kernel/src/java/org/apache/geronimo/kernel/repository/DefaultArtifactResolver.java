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
package org.apache.geronimo.kernel.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;

/**
 * @version $Rev$ $Date$
 */
public class DefaultArtifactResolver implements ArtifactResolver {
    private final ArtifactManager artifactManager;
    private final Collection repositories;

    public DefaultArtifactResolver(ArtifactManager artifactManager, ListableRepository repository) {
        this.artifactManager = artifactManager;
        this.repositories = Collections.singleton(repository);
    }

    public DefaultArtifactResolver(ArtifactManager artifactManager, Collection repositories) {
        this.artifactManager = artifactManager;
        this.repositories = repositories;
    }


    public Artifact generateArtifact(Artifact source, String defaultType) {
        if(source.isResolved()) {
            return source;
        }
        String groupId = source.getGroupId() == null ? Artifact.DEFAULT_GROUP_ID : source.getGroupId();
        String artifactId = source.getArtifactId();
        String type = source.getType() == null ? defaultType : source.getType();
        Version version = source.getVersion() == null ? new Version(Long.toString(System.currentTimeMillis())) : source.getVersion();

        return new Artifact(groupId, artifactId, version, type);
    }

    public Artifact queryArtifact(Artifact artifact) throws MultipleMatchesException {
        Artifact[] all = queryArtifacts(artifact);
        if(all.length > 1) {
            throw new MultipleMatchesException(artifact);
        }
        return all.length == 0 ? null : all[0];
    }

    public Artifact[] queryArtifacts(Artifact artifact) {
        LinkedHashSet set = new LinkedHashSet();
        for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
            ListableRepository repository = (ListableRepository) iterator.next();
            set.addAll(repository.list(artifact));
        }
        return (Artifact[]) set.toArray(new Artifact[set.size()]);
    }

    public LinkedHashSet resolveInClassLoader(Collection artifacts) throws MissingDependencyException {
        return resolveInClassLoader(artifacts, Collections.EMPTY_SET);
    }

    public LinkedHashSet resolveInClassLoader(Collection artifacts, Collection parentConfigurations) throws MissingDependencyException {
        LinkedHashSet resolvedArtifacts = new LinkedHashSet();
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (!artifact.isResolved()) {
                artifact = resolveInClassLoader(artifact, parentConfigurations);
            }
            resolvedArtifacts.add(artifact);
        }
        return resolvedArtifacts;
    }

    public Artifact resolveInClassLoader(Artifact source) throws MissingDependencyException {
        return resolveInClassLoader(source, Collections.EMPTY_SET);
    }

    public Artifact resolveInClassLoader(Artifact source, Collection parentConfigurations) throws MissingDependencyException {
        // Some tests break if we acntually try to search for fully-resolved artifacts
        if(source.isResolved()) {
            return source;
        }
//        if (artifact.getType() == null) {
//            throw new IllegalArgumentException("Type not set " + artifact);
//        }
//
//        String groupId = source.getGroupId();
//        if (groupId == null) {
//            groupId = Artifact.DEFAULT_GROUP_ID;
//        }

//        Version version = source.getVersion();

        Artifact working = resolveVersion(parentConfigurations, source);
        if (working == null || !working.isResolved()) {
            throw new MissingDependencyException("Unable to resolve dependency " + source);
        }

        return working;
    }

    private Artifact resolveVersion(Collection parentConfigurations, Artifact working) {
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
            SortedSet list = new TreeSet();
            for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
                ListableRepository repository = (ListableRepository) iterator.next();
                list.addAll(repository.list(working));
            }

            if (list.isEmpty()) {
                return null;
            }
            return (Artifact) list.last();
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

    private Artifact searchParents(Collection parentConfigurations, Artifact working) {
        for (Iterator iterator = parentConfigurations.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();

            // check if this parent matches the groupId, artifactId, and type
            if (matches(configuration.getId(), working)) {
                return configuration.getId();
            }

            Environment environment = configuration.getEnvironment();
            if (environment.isInverseClassLoading()) {
                // Search dependencies of the configuration before searching the parents
                Artifact artifact = getArtifactVersion(configuration.getDependencies(), working);
                if (artifact != null) {
                    return artifact;
                }

                // wasn't declared in the dependencies, so search the parents of the configuration
                artifact = searchParents(configuration.getClassParents(), working);
                if (artifact != null) {
                    return artifact;
                }

            } else {
                // Search the parents before the dependencies of the configuration
                Artifact artifact = searchParents(configuration.getClassParents(), working);
                if (artifact != null) {
                    return artifact;
                }

                // wasn't declared in a parent check the dependencies of the configuration
                artifact = getArtifactVersion(configuration.getDependencies(), working);
                if (artifact != null) {
                    return artifact;
                }
            }
        }
        return null;
    }

    private Artifact getArtifactVersion(Collection artifacts, Artifact query) {
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (matches(artifact, query)) {
                return artifact;
            }
        }
        return null;
    }

    private boolean matches(Artifact candidate, Artifact query) {
        return query.matches(candidate);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultArtifactResolver.class, "ArtifactResolver");
        infoFactory.addReference("ArtifactManager", ArtifactManager.class, "ArtifactManager");
        infoFactory.addReference("Repositories", Repository.class, "Repository");
        infoFactory.addInterface(ArtifactResolver.class);

        infoFactory.setConstructor(new String[]{
                "ArtifactManager",
                "Repositories",
        });


        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}