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

package org.apache.geronimo.mavenplugins.server;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.geronimo.plugin.ArtifactItem;
import org.apache.commons.lang.SystemUtils;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Expand;

/**
 * Start the Geronimo server.
 *
 * @goal start
 *
 * @version $Id$
 */
public class StartServerMojo
    extends AntMojoSupport
{
    /**
     * The assembly to unpack which contains the serer to start.
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

    /**
     * Flag to control if we background the server or block Maven execution.
     *
     * @parameter default-value="false"
     * @required
     */
    private boolean background = false;

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
        log.info("Starting server...");

        log.debug("Using assembly: " + assembly);

        // Unzip the assembly
        Artifact artifact = getArtifact(assembly);

        File workDir = new File(outputDirectory, artifact.getArtifactId() + "-" + artifact.getVersion());
        if (!workDir.exists()) {
            log.info("Extracting assembly: " + artifact.getFile());

            Expand unzip = (Expand)createTask("unzip");
            unzip.setSrc(artifact.getFile());
            unzip.setDest(outputDirectory);
            unzip.execute();
        }
        else {
            log.debug("Assembly already unpacked... reusing");
        }

        //
        // TODO: Change to Java task
        //

        final ExecTask exec = (ExecTask)createTask("exec");
        exec.setExecutable("java" +  (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        exec.createArg().setValue("-jar");
        exec.createArg().setFile(new File(workDir, "bin/server.jar"));
        exec.createArg().setValue("--quiet");
        exec.setDir(workDir);
        exec.setLogError(true);

        // Holds any exception that was thrown during startup (as the cause)
        final Throwable errorHolder = new Throwable();

        // Start the server int a seperate thread
        Thread t = new Thread("Geronimo Server Runner") {
            public void run() {
                try {
                    exec.execute();
                }
                catch (Exception e) {
                    errorHolder.initCause(e);

                    //
                    // NOTE: Don't log here, as when the JVM exists an exception will get thrown by Ant
                    //       but that should be fine.
                    //
                }
            }
        };
        t.start();

        log.info("Waiting for Geronimo server...");

        //
        // TODO: Check the status via JMX
        //

        // Verify server started
        URL url = new URL("http://localhost:8080");
        boolean started = false;
        while (!started) {
            if (errorHolder.getCause() != null) {
                throw new MojoExecutionException("Failed to start Geronimo server", errorHolder.getCause());
            }

            log.debug("Trying connection to: " + url);

            try {
                URLConnection c = url.openConnection();
                Object input = c.getContent();
                log.debug("Input: " + input);
                started = true;
            }
            catch (Exception e) {
                // ignore
            }

            Thread.sleep(1000);
        }

        log.info("Server started");

        if (!background) {
            log.info("Waiting for Geronimo to shutdown...");
            synchronized (this) {
                wait();
            }
        }
    }
}
