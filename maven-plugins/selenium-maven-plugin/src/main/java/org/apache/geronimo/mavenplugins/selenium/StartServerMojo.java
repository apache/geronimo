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
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;

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

    /**
     * Map of of plugin artifacts.
     *
     * @parameter expression="${plugin.artifactMap}"
     * @required
     * @readonly
     */
    private Map pluginArtifactMap = null;

    /**
     * Working directory where Selenium server will be started from.
     *
     * @parameter expression="${project.build.directory}/selenium"
     * @required
     */
    private File workingDirectory = null;

    /**
     * The file that Selenium server output will be written to.
     *
     * @parameter expression="${project.build.directory}/selenium/server.out"
     * @required
     */
    private File outputFile = null;

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
        log.info("Starting Selenium server...");

        Artifact seleniumArtifact = (Artifact)pluginArtifactMap.get("org.openqa.selenium.server:selenium-server");
        if (seleniumArtifact == null) {
            throw new MojoExecutionException("Unable to locate 'selenium-server' in the list of plugin artifacts");
        }

        final Java java = (Java)createTask("java");

        java.setFork(true);
        mkdir(workingDirectory);
        java.setDir(workingDirectory);
        java.setOutput(outputFile);
        java.setFailonerror(true);
        java.setLogError(true);

        java.setClassname("org.openqa.selenium.server.SeleniumServer");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(seleniumArtifact.getFile());

        //
        // HACK: Use Simple log instead of evil JDK 1.4 logging
        //
        Environment.Variable var = new Environment.Variable();
        var.setKey("org.apache.commons.logging.Log");
        var.setValue("org.apache.commons.logging.impl.SimpleLog");
        java.addSysproperty(var);

        // Server arguments

        java.createArg().setValue("-port");
        java.createArg().setValue(String.valueOf(port));

        if (debug) {
            java.createArg().setValue("-debug");
        }

        if (timeout > 0) {
            log.info("Timeout after: " + timeout + " seconds");

            java.createArg().setValue("-timeout");
            java.createArg().setValue(String.valueOf(timeout));
        }

        File userExtentionsFile = getUserExtentionsFile();
        if (userExtentionsFile != null) {
            log.info("Using user extensions: " + userExtentionsFile);

            java.createArg().setValue("-userExtensions");
            java.createArg().setFile(userExtentionsFile);
        }

        final Throwable errorHolder = new Throwable();

        // Start the server int a seperate thread
        Thread t = new Thread("Selenium Server Runner") {
            public void run() {
                try {
                    java.execute();
                }
                catch (Exception e) {
                    errorHolder.initCause(e);

                    log.error("Failed to start Selenium server", e);
                }
            }
        };
        t.start();

        log.info("Waiting for Selenium server...");

        // Verify server started
        URL url = new URL("http://localhost:" + port + "/selenium-server");
        boolean started = false;
        while (!started) {
            if (errorHolder.getCause() != null) {
                throw new MojoExecutionException("Failed to start Selenium server", errorHolder.getCause());
            }

            log.debug("Trying connection to: " + url);

            try {
                Object input = url.openConnection().getContent();
                log.debug("Input: " + input);
                started = true;
            }
            catch (Exception e) {
                // ignore
            }

            Thread.sleep(1000);
        }

        log.info("Selenium server started");
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

        // File needs to be named 'user-extensions.js' or Selenium server will puke
        File file = new File(workingDirectory, "user-extensions.js");
        file.mkdirs();
        file.delete();
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
