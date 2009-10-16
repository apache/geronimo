/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo.module;

import java.io.File;
import java.io.IOException;

import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Deploy library to a Geronimo server.
 *
 * @version $Rev$ $Date$
 * @goal install-library
 */
public class InstallLibraryMojo extends ModuleMojoSupport {

    /**
     * A file which points to a specific library archive.
     * 
     * @parameter expression="${libraryFile}"
     * @required
     */
    protected File libraryFile = null;

    /**
     * Parameter to specify a non-default group id for the library. Otherwise,
     * the library file will be installed with the group id named default.
     * 
     * @parameter expression="${groupId}"
     * @optional
     */
    protected String groupId = null;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("libraryFile: " + libraryFile.getAbsolutePath());
        GeronimoDeploymentManager mgr = getGeronimoDeploymentManager();
        try {
            mgr.installLibrary(libraryFile, groupId);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not install library", e);
        }
    }

    protected GeronimoDeploymentManager getGeronimoDeploymentManager() throws MojoExecutionException {
        try {
            return (GeronimoDeploymentManager) getDeploymentManager();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not communicate with server", e);
        } catch (DeploymentManagerCreationException e) {
            throw new MojoExecutionException("Could not create deployment manager", e);
        }
    }
    
    @Override
    protected String getFullClassName() {
        return this.getClass().getName();
    }
}
