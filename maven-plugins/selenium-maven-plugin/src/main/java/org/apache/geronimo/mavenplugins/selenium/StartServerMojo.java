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

import java.io.File;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.geronimo.plugin.ArtifactItem;
import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.ExecTask;

/**
 * Start the Selenium server.
 *
 * @goal start
 *
 * @version $Id$
 */
public class StartServerMojo
    extends AntMojoSupport
{
    /**
     * The port number the server will use.
     *
     * @parameter default-value="4444"
     */
    private int port;
    
    /**
     * Timeout for the server in seconds.
     *
     * @parameter default-value="-1"
     */
    private int timeout;
    
    
    /**
     * Enable the server's debug mode..
     *
     * @parameter default-value="false"
     */
    private boolean debug;
    
    /**
     * Location of the user-extentions.js to load into the server.
     *
     * @parameter
     */
    private File userExtensions;
    
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
        
        //
        // HACK: Need a better way to find this jar
        //
        
        ArtifactItem item = new ArtifactItem();
        item.setGroupId("org.openqa.selenium.server");
        item.setArtifactId("selenium-server");
        item.setVersion("0.8.1");
        
        final Artifact artifact = getArtifact(item);

        final String executable = "java" +  (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");

        // Start the server int a seperate thread
        Thread t = new Thread("Server Runner") {
            public void run() {
                try {
                    ExecTask exec = (ExecTask)createTask("exec");
                    exec.setExecutable(executable);
                    
                    //
                    // HACK: Use Simple log instead of evil JDK 1.4 logging
                    //       Should change to Java task and setup log4j.properties in the cp
                    //
                    exec.createArg().setValue("-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog");
                    
                    exec.createArg().setValue("-jar");
                    exec.createArg().setFile(artifact.getFile());
                    
                    exec.createArg().setValue("-port");
                    exec.createArg().setValue(String.valueOf(port));
                    
                    if (debug) {
                        exec.createArg().setValue("-debug");
                    }
                    
                    if (timeout > 0) {
                        log.info("Timeout after: " + timeout + " seconds");
                        
                        exec.createArg().setValue("-timeout");
                        exec.createArg().setValue(String.valueOf(timeout));
                    }
                    
                    if (userExtensions != null) {
                        log.info("Using user extensions: " + userExtensions);
                        
                        exec.createArg().setValue("-userExtensions");
                        exec.createArg().setFile(userExtensions);
                    }
                    
                    exec.setLogError(true);
                    exec.execute();
                }
                catch (Exception e) {
                    log.error("Failed to start server", e);
                }
                
                try {
                    synchronized(this) {
                        wait();
                    }
                }
                catch (Exception e) {
                    // ignore
                }
            }
        };
        t.start();

        log.info("Server started");
    }
}
