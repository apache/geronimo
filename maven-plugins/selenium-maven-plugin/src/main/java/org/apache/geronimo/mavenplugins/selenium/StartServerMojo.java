/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.mavenplugins.selenium;

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.geronimo.genesis.AntMojoSupport;
import org.apache.geronimo.genesis.ObjectHolder;

import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;

import org.codehaus.plexus.util.FileUtils;

/**
 * Start the Selenium server.
 *
 * @goal start-server
 *
 * @version $Rev$ $Date$
 */
public class StartServerMojo
    extends AntMojoSupport
{
    /**
     * The port number the server will use.
     *
     * @parameter expression="${port}" default-value="4444"
     */
    private int port = -1;

    /**
     * Timeout for the server in seconds.
     *
     * @parameter expression="${timeout}" default-value="-1"
     */
    private int timeout = -1;

    /**
     * Enable the server's debug mode..
     *
     * @parameter expression="${debug}" default-value="false"
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
     * Enable logging mode.
     *
     * @parameter expression="${logOutput}" default-value="false"
     */
    protected boolean logOutput = false;
    
    /**
     * The file that Selenium server logs will be written to.
     *
     * @parameter expression="${logFile}" default-value="${project.build.directory}/selenium/server.log"
     * @required
     */
    private File logFile = null;

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

    //
    // Mojo
    //

    private File getPluginArchive() {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        return new File(path);
    }

    private Artifact getPluginArtifact(final String name) throws MojoExecutionException {
        Artifact artifact = (Artifact)pluginArtifactMap.get(name);
        if (artifact == null) {
            throw new MojoExecutionException("Unable to locate '" + name + "' in the list of plugin artifacts");
        }

        return artifact;
    }

    protected void doExecute() throws Exception {
        log.info("Starting Selenium server...");

        final Java java = (Java)createTask("java");
        
        FileUtils.forceMkdir(workingDirectory);
        
        java.setFork(true);
        java.setDir(workingDirectory);
        java.setFailonerror(true);
        
        if (logOutput) {
            FileUtils.forceMkdir(logFile.getParentFile());

            log.info("Redirecting output to: " + logFile);
            
            java.setLogError(true);
            java.setOutput(logFile);
        }
        
        java.setClassname("org.openqa.selenium.server.SeleniumServer");

        Path classpath = java.createClasspath();
        classpath.createPathElement().setLocation(getPluginArchive());
        classpath.createPathElement().setLocation(getPluginArtifact("log4j:log4j").getFile());
        classpath.createPathElement().setLocation(getPluginArtifact("org.openqa.selenium.server:selenium-server").getFile());

        Environment.Variable var;

        var = new Environment.Variable();
        var.setKey("selenium.log");
        var.setFile(logFile);
        java.addSysproperty(var);

        var = new Environment.Variable();
        var.setKey("selenium.loglevel");
        var.setValue(debug == true ? "DEBUG" : "INFO");
        java.addSysproperty(var);

        var = new Environment.Variable();
        var.setKey("log4j.configuration");
        var.setValue("org/apache/geronimo/mavenplugins/selenium/log4j.properties");
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
            log.info("User extensions: " + userExtentionsFile);

            java.createArg().setValue("-userExtensions");
            java.createArg().setFile(userExtentionsFile);
        }

        // Holds any exception that was thrown during startup
        final ObjectHolder errorHolder = new ObjectHolder();

        // Start the server int a seperate thread
        Thread t = new Thread("Selenium Server Runner") {
            public void run() {
                try {
                    java.execute();
                }
                catch (Exception e) {
                    errorHolder.set(e);

                    //
                    // NOTE: Don't log here, as when the JVM exists an exception will get thrown by Ant
                    //       but that should be fine.
                    //
                }
            }
        };
        t.start();

        log.debug("Waiting for Selenium server...");

        // Verify server started
        URL url = new URL("http://localhost:" + port + "/selenium-server");
        boolean started = false;
        while (!started) {
            if (errorHolder.isSet()) {
                throw new MojoExecutionException("Failed to start Selenium server", (Throwable)errorHolder.get());
            }

            log.debug("Trying connection to: " + url);

            try {
                Object input = url.openConnection().getContent();
                started = true;
            }
            catch (Exception e) {
                // ignore
            }

            Thread.sleep(1000);
        }

        log.info("Selenium server started");

        if (!background) {
            log.info("Waiting for Selenium to shutdown...");

            t.join();
        }
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
        if (file.exists()) {
            log.debug("Reusing previously generated file: " + file);

            return file;
        }

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
