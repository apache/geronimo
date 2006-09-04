/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.mavenplugins.geronimo;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.tools.ant.taskdefs.Expand;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.FileUtils;

/**
 * Common assembly install support.
 *
 * @version $Rev$ $Date$
 */
public abstract class InstallerMojoSupport
    extends ServerMojoSupport
{
    /**
     * Enable forced install refresh.
     *
     * @parameter expression="${refresh}" default-value="false"
     */
    protected boolean refresh = false;

    /**
     * List of assembly artifact configurations.  Artifacts need to point to ZIP archives.
     *
     * @parameter
     * @required
     */
    protected AssemblyConfig[] assemblies = null;

    /**
     * Identifer of the assembly configuration to use.
     *
     * @parameter expression="${assemblyId}"
     */
    protected String assemblyId = null;

    /**
     * The default assemblyId to use when no assemblyId configured.
     *
     * @parameter
     */
    protected String defaultAssemblyId = null;

    /**
     * A file which points to a specific assembly ZIP archive.
     * If this parameter is set, then it will be used instead of from the
     * assemblies configuration.
     *
     * @parameter expression="${assemblyArchive}"
     */
    protected File assemblyArchive = null;

    /**
     * Directory to extract the assembly into.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory = null;

    //
    // MojoSupport Hooks
    //

    /**
     * ???
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory = null;

    protected ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    /**
     * ???
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver = null;

    protected ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    /**
     * ???
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository artifactRepository = null;

    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    /**
     * The assembly archive to use when installing.
     */
    protected File installArchive;

    /**
     * The directory where the assembly has been installed to.
     */
    protected File installDir;

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        // Determine which archive and directory to use... either manual or from artifacts
        if (assemblyArchive != null) {
            log.debug("Using non-artifact based assembly archive: " + installArchive);

            installArchive = assemblyArchive;

            //
            // FIXME: This probably will not work...
            //

            installDir = new File(outputDirectory, "assembly-archive");
        }
        else {
            Artifact artifact = getAssemblyArtifact();

            if (!"zip".equals(artifact.getType())) {
                throw new MojoExecutionException("Assembly file does not look like a ZIP archive");
            }

            installArchive = artifact.getFile();
            installDir = new File(outputDirectory, artifact.getArtifactId() + "-" + artifact.getVersion());
        }
    }

    protected Artifact getAssemblyArtifact() throws MojoExecutionException {
        assert assemblies != null;

        AssemblyConfig config;

        if (assemblies.length == 0) {
            throw new MojoExecutionException("At least one assembly configuration must be specified");
        }
        else if (assemblies.length > 1 && assemblyId == null && defaultAssemblyId == null) {
            throw new MojoExecutionException("Must specify assemblyId (or defaultAssemblyId) when more than on assembly configuration is given");
        }
        else if (assemblies.length == 1) {
            config = assemblies[0];
        }
        else {
            if (assemblyId == null) {
                assemblyId = defaultAssemblyId;
            }

            log.debug("Searching for assembly config for id: " + assemblyId);

            // Make sure there are no duplicate ids
            Map idMap = new HashMap();

            for (int i=0; i < assemblies.length; i++) {
                String id = assemblies[i].getId();

                if (id == null) {
                    throw new MojoExecutionException("Missing id for assembly configuration: " + assemblies[i]);
                }

                if (idMap.containsKey(id)) {
                    throw new MojoExecutionException("Duplicate assembly id: " + id);
                }

                idMap.put(id, assemblies[i]);
            }

            config = (AssemblyConfig) idMap.get(assemblyId);
            if (config == null) {
                throw new MojoExecutionException("Missing assembly configuration for id: " + assemblyId);
            }
        }

        log.info("Using assembly configuration: " + config);
        Artifact artifact = getArtifact(config);

        if (artifact.getFile() == null) {
            throw new MojoExecutionException("Assembly artifact does not have an attached file: " + artifact);
        }

        return artifact;
    }

    protected void doInstall() throws Exception {
        // Check if there is a newer archive or missing marker to trigger assembly install
        File installMarker = new File(installDir, ".installed");
        boolean refresh = this.refresh; // don't override config state with local state

        if (!refresh) {
            if (!installMarker.exists()) {
                refresh = true;
            }
            else if (installArchive.lastModified() > installMarker.lastModified()) {
                log.debug("Detected new assembly archive");
                refresh = true;
            }
        }
        else {
            log.debug("User requested installation refresh");
        }

        if (refresh) {
            if (installDir.exists()) {
                log.debug("Removing: " + installDir);
                FileUtils.forceDelete(installDir);
            }
        }

        // Install the assembly
        if (!installMarker.exists()) {
            log.info("Installing assembly...");

            Expand unzip = (Expand)createTask("unzip");
            unzip.setSrc(installArchive);
            unzip.setDest(outputDirectory);
            unzip.execute();

            installMarker.createNewFile();
        }
        else {
            log.debug("Assembly already installed");
        }
    }
}
