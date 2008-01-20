/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.system.repository.Maven2Repository;

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.artifact.Artifact;

import java.io.File;

/**
 * Installs one or more artifacts into a local Geronimo repository.
 *
 * @goal install-artifacts
 *
 * @version $Rev$ $Date$
 */
public class InstallArtifactsMojo
    extends AbstractCarMojo
{
    /**
     * The location of the target repository to install artifacts into.
     *
     * @parameter
     * @required
     */
    private File repositoryDirectory = null;

    /**
     * A array {@link ArtifactItem} instances to be installed into the repository.
     *
     * @parameter
     * @required
     */
    private ArtifactItem[] artifacts = null;

    /**
     * Flag to indicate that if an artifact exists already, that we should delete it and re-install.
     *
     * @parameter default-value="true"
     */
    private boolean force = true;

    protected void doExecute() throws Exception {
        if (!repositoryDirectory.exists()) {
            repositoryDirectory.mkdirs();
            log.info("Created directory: " + repositoryDirectory);
        }
        else if (!repositoryDirectory.isDirectory()) {
            throw new MojoExecutionException("Invalid reposiory directory: " + repositoryDirectory);
        }

        WriteableRepository repository = new Maven2Repository(repositoryDirectory);

        // Install all of the artifacts we were asked to...
        for (int i=0; i<artifacts.length; i++) {
            Artifact artifact = getArtifact(artifacts[i]);
            log.info("Installing: " + artifact);

            org.apache.geronimo.kernel.repository.Artifact gartifact = mavenArtifactToGeronimo(artifact);
            if (repository.contains(gartifact)) {
                if (force) {
                    File file = repository.getLocation(gartifact);
                    log.debug("Force deletion of: " + file);

                    if (!file.delete()) {
                        throw new MojoExecutionException("Failed to delete artifact from repository: " + artifacts[i]);
                    }
                }
                else {
                    throw new MojoExecutionException("Artifact already exists in repository: " + artifacts[i]);
                }
            }

            repository.copyToRepository(artifact.getFile(), gartifact, null);
        }
    }

    /**
     * Convert a Maven artifact into a the Geronimo flavor.
     */
    private org.apache.geronimo.kernel.repository.Artifact mavenArtifactToGeronimo(final Artifact artifact) {
        assert artifact != null;

        return new org.apache.geronimo.kernel.repository.Artifact(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            artifact.getVersion(),
            artifact.getType()
        );
    }
}
