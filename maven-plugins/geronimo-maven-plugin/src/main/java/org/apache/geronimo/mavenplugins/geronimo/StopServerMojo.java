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

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;

import java.io.File;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.geronimo.plugin.ArtifactItem;
import org.apache.commons.lang.SystemUtils;
import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * Stop the Geronimo server.
 *
 * @goal stop
 *
 * @version $Id$
 */
public class StopServerMojo
    extends AntMojoSupport
{
    /**
     * The assembly to unpack which contains the server to stop.
     *
     * @parameter
     * @required
     */
    private ArtifactItem assembly = null;

    /**
     * Directory to extract the assembly into.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory = null;

    //
    // MojoSupport Hooks
    //

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project = null;

    protected MavenProject getProject() {
        return project;
    }

    /**
     * ???
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory = null;

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
    private ArtifactResolver artifactResolver = null;

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
    private ArtifactRepository artifactRepository = null;

    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    //
    // Mojo
    //

    protected void doExecute() throws Exception {
        log.info("Stopping server...");

        log.debug("Using assembly: " + assembly);

        // Unzip the assembly
        Artifact artifact = getArtifact(assembly);

        // What are we running... where?
        final File workDir = new File(outputDirectory, artifact.getArtifactId() + "-" + artifact.getVersion());
        final String executable = "java" +  (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");

        ExecTask exec = (ExecTask)createTask("exec");
        exec.setExecutable(executable);
        exec.createArg().setValue("-jar");
        exec.createArg().setFile(new File(workDir, "bin/shutdown.jar"));
        exec.createArg().setValue("--user");
        exec.createArg().setValue("system");
        exec.createArg().setValue("--password");
        exec.createArg().setValue("manager");
        exec.setDir(workDir);
        exec.setLogError(true);
        exec.execute();

        log.info("Server stopped");
    }
}
