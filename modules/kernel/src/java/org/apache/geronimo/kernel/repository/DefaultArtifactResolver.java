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
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.apache.geronimo.kernel.config.Configuration;

/**
 * @version $Rev$ $Date$
 */
public class DefaultArtifactResolver implements ArtifactResolver {
    private final ArtifactManager artifactManager;
    private final ListableRepository repository;

    public DefaultArtifactResolver(ArtifactManager artifactManager, ListableRepository repository) {
        this.artifactManager = artifactManager;
        this.repository = repository;
    }

    public void resolve(Collection parentConfigurations, Collection artifacts) throws MissingDependencyException {
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (!artifact.isResolved()) {
                resolveArtifact(parentConfigurations, artifact);
            }
        }
    }

    private void resolveArtifact(Collection parentConfigurations, Artifact artifact) throws MissingDependencyException {
        if (artifact.getArtifactId() == null) {
            throw new IllegalArgumentException("ArtifactId not set " + artifact);
        }

        if (artifact.getGroupId() == null) {
            artifact.setGroupId(Artifact.DEFAULT_GROUP_ID);
        }

        if (artifact.getVersion() == null) {
            // check if we have any existing artifacts loaded
            Version version = resolveVersion(parentConfigurations, artifact.getGroupId(), artifact.getArtifactId(), artifact.getType());
            if (version == null) {
                throw new MissingDependencyException("Unable to resolve dependency " + artifact);
            }
            artifact.setVersion(version);
        }
    }

    private Version resolveVersion(Collection parentConfigurations, String groupId, String artifactId, String type) {
        List existingArtifacts = artifactManager.getLoadedArtifacts(groupId, artifactId, type);

        // if we have exactally one artifact loaded use it's version
        if (existingArtifacts.size() == 1) {
            Artifact existingArtifact = ((Artifact) existingArtifacts.get(0));
            return existingArtifact.getVersion();
        }

        // if we have no existing loaded artifacts grab the highest version from the repository
        if (existingArtifacts.size() == 0) {
            List list = repository.list(groupId, artifactId, type);
            Collections.sort(list);

            Artifact repositoryArtifact = (Artifact) list.get(list.size() -1);
            return repositoryArtifact.getVersion();
        }

        // more than one version of the artifact was loaded...

        // if one of parents already loaded the artifact, use that version
        Version version = searchParents(parentConfigurations, groupId, artifactId, type);
        if (version != null) {
            return version;
        }

        // it wasn't declared by the parent so just use the highest verstion already loaded
        Collections.sort(existingArtifacts);
        Artifact repositoryArtifact = (Artifact) existingArtifacts.get(existingArtifacts.size() -1);
        return repositoryArtifact.getVersion();
    }

    private Version searchParents(Collection parentConfigurations, String groupId, String artifactId, String type) {
        for (Iterator iterator = parentConfigurations.iterator(); iterator.hasNext();) {
            Configuration configuration = (Configuration) iterator.next();

            if (configuration.isInverseClassLoading()) {
                // Search dependencies of the configuration before searching the parents
                List dependencies = configuration.getDependencies();
                Version version = getArtifactVersion(dependencies, groupId, artifactId, type);
                if (version != null) {
                    return version;
                }

                // wasn't declared in the dependencies, so searcht the parents of the configuration
                version = searchParents(configuration.getParents(), groupId, artifactId, type);
                if (version != null) {
                    return version;
                }

            } else {
                // Search the parents before the dependencies of the configuration
                Version version = searchParents(configuration.getParents(), groupId, artifactId, type);
                if (version != null) {
                    return version;
                }

                // wasn't declared in a parent check the dependencies of the configuration
                List dependencies = configuration.getDependencies();
                version = getArtifactVersion(dependencies, groupId, artifactId, type);
                if (version != null) {
                    return version;
                }
            }
        }
        return null;
    }

    private Version getArtifactVersion(List artifacts, String groupId, String artifactId, String type) {
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (groupId.equals(artifact.getGroupId()) &&
                    artifactId.equals(artifact.getArtifactId()) &&
                    type.equals(artifact.getType())) {
                return artifact.getVersion();
            }
        }
        return null;
    }

}
