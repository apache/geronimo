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

package org.apache.geronimo.mavenplugins.selenium;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.geronimo.plugin.ArtifactItem;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.io.IOUtils;

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
    private int port = -1;
    
    /**
     * Timeout for the server in seconds.
     *
     * @parameter default-value="-1"
     */
    private int timeout = -1;

    /**
     * Enable the server's debug mode..
     *
     * @parameter default-value="false"
     */
    private boolean debug = false;

    /**
     * The file or resource to use for default user-extentions.js.
     *
     * @parameter default-value="org/apache/geronimo/mavenplugins/selenium/default-user-extentions.js"
     */
    private String defaultUserExtensions = null;

    /**
     * Enable or disable default user-extentions.js
     *
     * @parameter default-value="true"
     */
    private boolean defaultUserExtensionsEnabled = true;

    /**
     * Location of the user-extentions.js to load into the server.
     *
     * <p>
     * If defaultUserExtensionsEnabled is true, then this file will be appended to the defaults.
     *
     * @parameter
     */
    private String userExtensions = null;
    
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

        final ExecTask exec = (ExecTask)createTask("exec");
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

        File userExtentionsFile = getUserExtentionsFile();
        if (userExtentionsFile != null) {
            log.info("Using user extensions: " + userExtentionsFile);

            exec.createArg().setValue("-userExtensions");
            exec.createArg().setFile(userExtentionsFile);
        }

        exec.setLogError(true);

        // Start the server int a seperate thread
        Thread t = new Thread("Server Runner") {
            public void run() {
                try {
                    exec.execute();
                }
                catch (Exception e) {
                    log.error("Failed to start server", e);
                }
            }
        };
        t.start();

        log.info("Server started");
    }

    /**
     * Resolve a resource to a file, URL or resource.
     */
    private URL resolveResource(final String name) throws MalformedURLException, MojoFailureException {
        assert name != null;

        URL url;

        File file = new File(name);
        if (file.exists()) {
            url = file.toURL();
        }
        else {
            try {
                url = new URL(name);
            }
            catch (MalformedURLException e) {
                url = Thread.currentThread().getContextClassLoader().getResource(name);
            }
        }

        if (url == null) {
            throw new MojoFailureException("Could not resolve resource: " + name);
        }

        log.debug("Resolved resource '" + name + "' as: " + url);

        return url;
    }

    /**
     * Retutn the user-extentions.js file to use, or null if it should not be installed.
     */
    private File getUserExtentionsFile() throws Exception {
        if (!defaultUserExtensionsEnabled && userExtensions == null) {
            return null;
        }

        File file = File.createTempFile("user-extentions-", ".js");
        file.deleteOnExit();
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));

        if (defaultUserExtensionsEnabled) {
            URL url = resolveResource(defaultUserExtensions);
            log.debug("Using defaults: " + url);

            writer.println("//");
            writer.println("// Default user extentions; from: " + url);
            writer.println("//");

            IOUtils.copy(url.openStream(), writer);
        }

        if (userExtensions != null) {
            URL url = resolveResource(userExtensions);
            log.debug("Using user extentions: " + url);

            writer.println("//");
            writer.println("// User extentions; from: " + url);
            writer.println("//");

            IOUtils.copy(url.openStream(), writer);
        }

        writer.flush();
        writer.close();

        return file;
    }
}
