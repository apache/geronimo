/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @version $Rev:$ $Date:$
 */
public abstract class AbstractModuleMojo extends AbstractMojo {
    
    /**
     * The uri to look up the JMXConnector.
     * 
     * @parameter default-value="jmx:rmi://localhost/jndi/rmi:/JMXConnector"
     */
    private String uri;


    /**
     * @parameter
     */
    protected String id;
    

    /**
     * The uri to connect to the jmx connector with.
     * 
     * @parameter default-value="deployer:geronimo:jmx"
     */
    private String distributeURI;

    /**
     * The authentication user name.
     * 
     * @parameter default-value="system"
     */
    private String username;

    /**
     * The authentication password.
     * 
     * @parameter default-value="manager"
     */
    private String password;

    /**
     * The time between connect attempts.
     * 
     * @parameter default-value=0
     */
    private long sleepTimer;

    /**
     * @parameter default-value=100
     */
    private int maxTries;

    /**
     * @parameter default-value=2000
     */
    private int retryIntervalMilliseconds;

    /**
     * @parameter default-value=true
     */
    private boolean failOnError;

    /**
     * @parameter
     */
    private File outputDirectory;

    /**
     * @parameter default-value=null
     */
    private File resultsLog;

    

    protected final String lineSep = "===========================================";

    public abstract void execute() throws MojoExecutionException;

    protected DeploymentManager getDeploymentManager() throws IOException, DeploymentManagerCreationException {
        if (getUsername() == null) {
            throw new IllegalStateException("No user specified");
        }
        if (getPassword() == null) {
            throw new IllegalStateException("No password specified");
        }
        if (getDistributeURI() == null) {
            throw new IllegalStateException("No uri specified");
        }
        new DeploymentFactoryImpl();

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            DeploymentFactoryManager factoryManager = DeploymentFactoryManager.getInstance();
            DeploymentManager manager = factoryManager.getDeploymentManager(getDistributeURI(), getUsername(), getPassword());
            return manager;
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
    }

    /**
     * @return Returns the maxTries.
     */
    public int getMaxTries() {
        return maxTries;
    }

    /**
     * @return Returns the retryIntervalMilliseconds.
     */
    public int getRetryIntervalMilliseconds() {
        return retryIntervalMilliseconds;
    }

    /**
     * @return Returns the sleepTimer.
     */
    public long getSleepTimer() {
        return sleepTimer;
    }

    public String getUri() {
        return uri;
    }

    public String getDistributeURI() {
        return distributeURI;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public PrintStream getResultsStream() {
        PrintStream resultStream = null;
        if (this.resultsLog != null) {
            try {
                resultStream = new PrintStream(new FileOutputStream(this.resultsLog, true), true);
            }
            catch (FileNotFoundException e) {
                //e.printStackTrace();
                getLog().warn(e.toString());
                getLog().warn("Results cannot be logged");
            }
        }
        return resultStream;
    }

    public PrintStream getLogStream(String goalName) {
        PrintStream stream = System.out;
    
        if (this.outputDirectory != null) {
            if (!this.outputDirectory.exists())
                this.outputDirectory.mkdirs();
    
            String fileName = this.outputDirectory.getAbsolutePath() + File.separator + goalName + ".log";
    
            try {
                stream = new PrintStream(new FileOutputStream(fileName, true), true);
            }
            catch (FileNotFoundException e) {
                //e.printStackTrace();
                getLog().warn(e.toString());
                getLog().warn("No logs will be available");
            }
        }
        return stream;
    }

    public void logResults(PrintStream resultStream, String goalName, String result) {
        if (resultStream != null)
            getResultsStream().println(goalName + ":" + result);
    }

    /**
     * Method is used to point to the log location for more errors.
     */
    protected void seeLog() {
        if (this.outputDirectory != null)
            getLog().error("See log at " + getOutputDirectory().getAbsolutePath() + " for more details");
    }

    public void debug(String debugString) {
        System.out.println(debugString);
    }

    /**
     * @param e
     * @param logStream
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected void handleError(Exception e, PrintStream logStream) throws MojoExecutionException {
        seeLog();
        e.printStackTrace(logStream);
        logStream.println(lineSep);
        if (isFailOnError()) {            
            throw (MojoExecutionException) new MojoExecutionException(e.toString(), e);
        }
        else {            
             try {
                throw (MojoFailureException) new MojoFailureException(e, e.toString(), e.getMessage());
            }
            catch (MojoFailureException e1) {
                e1.printStackTrace(logStream);
                logStream.println(lineSep);
            }            
        }        
    }
    
    
}
