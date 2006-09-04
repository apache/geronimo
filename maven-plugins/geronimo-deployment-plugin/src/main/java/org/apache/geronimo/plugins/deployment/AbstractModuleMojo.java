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
import java.io.IOException;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;

import org.apache.geronimo.genesis.MojoSupport;

//
// TODO: Rename to AbstractDeploymentMojo
//

/**
 * Support for deployment Mojos.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractModuleMojo extends MojoSupport {
    
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

    protected DeploymentManager getDeploymentManager() throws IOException, DeploymentManagerCreationException {
        //
        // NOTE: username, password, and distributeURI will never be null
        //

        //
        // TODO: Document why this is here... seems very odd
        //
        new DeploymentFactoryImpl();

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            DeploymentFactoryManager factoryManager = DeploymentFactoryManager.getInstance();
            return factoryManager.getDeploymentManager(getDistributeURI(), getUsername(), getPassword());
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
    }
}
