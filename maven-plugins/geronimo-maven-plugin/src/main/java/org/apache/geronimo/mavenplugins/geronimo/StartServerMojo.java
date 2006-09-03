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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.URL;

import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Java;

/**
 * Start the Geronimo server.
 *
 * @goal start
 *
 * @version $Rev$ $Date$
 */
public class StartServerMojo
    extends ServerMojoSupport
{
    /**
     * Flag to control if we background the server or block Maven execution.
     *
     * @parameter expression="${background}" default-value="false"
     * @required
     */
    private boolean background = false;

    /**
     * Set the maximum memory for the forked JVM.
     *
     * @parameter expression="${maxMemory}"
     */
    private String maxMemory = null;

    /**
     * Enable quiet mode..
     *
     * @parameter expression="${quiet}" default-value="false"
     * @required
     */
    private boolean quiet = false;

    /**
     * Enable verbose mode..
     *
     * @parameter expression="${verbose}" default-value="false"
     * @required
     */
    private boolean verbose = false;

    /**
     * Enable veryverbose mode..
     *
     * @parameter expression="${veryverbose}" default-value="false"
     * @required
     */
    private boolean veryverbose = false;

    protected void doExecute() throws Exception {
        log.info("Starting Geronimo server...");

        Artifact artifact = getAssemblyArtifact();

        if (!"zip".equals(artifact.getType())) {
            throw new MojoExecutionException("Assembly file does not look like a ZIP archive");
        }

        File assemblyDir = new File(outputDirectory, artifact.getArtifactId() + "-" + artifact.getVersion());
        if (!assemblyDir.exists()) {
            log.info("Extracting assembly: " + artifact.getFile());

            Expand unzip = (Expand)createTask("unzip");
            unzip.setSrc(artifact.getFile());
            unzip.setDest(outputDirectory);
            unzip.execute();
        }
        else {
            log.debug("Assembly already unpacked... reusing");
        }

        final Java java = (Java)createTask("java");
        java.setJar(new File(assemblyDir, "bin/server.jar"));
        java.setDir(assemblyDir);
        java.setFailonerror(true);
        java.setFork(true);
        java.setLogError(true);

        //
        // TODO: Capture output/error to files
        //

        if (maxMemory != null) {
            java.setMaxmemory(maxMemory);
        }

        if (quiet) {
            java.createArg().setValue("--quiet");
        }
        else {
            java.createArg().setValue("--long");
        }

        if (verbose) {
            java.createArg().setValue("--verbose");
        }

        if (veryverbose) {
            java.createArg().setValue("--veryverbose");
        }

        //
        // TODO: Support --override
        //

        //
        // TODO: Support JVM args for debug mode, add debug flag to enable or disable
        //

        // Holds any exception that was thrown during startup (as the cause)
        final Throwable errorHolder = new Throwable();

        // Start the server int a seperate thread
        Thread t = new Thread("Geronimo Server Runner") {
            public void run() {
                try {
                    java.execute();
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
        // TODO: Check the status via JMX:
        //
        //       "service:jmx:rmi://localhost/jndi/rmi://localhost:" + port + "/JMXConnector"
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
                url.openConnection().getContent();
                started = true;
            }
            catch (Exception e) {
                // ignore
            }

            Thread.sleep(1000);
        }

        //
        // HACK: Give it a few seconds... our detection method here is lossy
        //

        Thread.sleep(10000);

        log.info("Geronimo server started");

        if (!background) {
            log.info("Waiting for Geronimo server to shutdown...");

            t.join();
        }
    }
}
