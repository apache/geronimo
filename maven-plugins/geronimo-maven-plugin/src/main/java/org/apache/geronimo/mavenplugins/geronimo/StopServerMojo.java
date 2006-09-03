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

import org.apache.tools.ant.taskdefs.Java;

/**
 * Stop the Geronimo server.
 *
 * @goal stop
 *
 * @version $Rev$ $Date$
 */
public class StopServerMojo
    extends ServerMojoSupport
{
    /**
     * Time in seconds to wait before timing out the stop operation.
     *
     * @parameter default-value="60"
     * @required
     */
    private int timeout = -1;

    protected void doExecute() throws Exception {
        log.info("Stopping Geronimo server...");

        Artifact artifact = getAssemblyArtifact();

        File assemblyDir = new File(outputDirectory, artifact.getArtifactId() + "-" + artifact.getVersion());
        if (!assemblyDir.exists()) {
            // Complain if there is no assemblyDir, as that probably means that 'start' was not executed.
            throw new MojoExecutionException("Missing assembly directory: " + assemblyDir);
        }

        Java java = (Java)createTask("java");
        java.setJar(new File(assemblyDir, "bin/shutdown.jar"));
        java.setDir(assemblyDir);
        java.setFailonerror(true);
        java.setFork(true);
        java.setLogError(true);

        if (timeout > 0) {
            // Convert to milliseconds
            java.setTimeout(new Long(timeout * 1000));
        }

        if (port > 0) {
            java.createArg().setValue("--port");
            java.createArg().setValue(String.valueOf(port));
        }

        if (username != null) {
            java.createArg().setValue("--user");
            java.createArg().setValue(username);
        }

        if (password != null) {
            java.createArg().setValue("--password");
            java.createArg().setValue(password);
        }

        java.execute();

        //
        // TODO: Verify that it actually stopped?
        //
    }
}
