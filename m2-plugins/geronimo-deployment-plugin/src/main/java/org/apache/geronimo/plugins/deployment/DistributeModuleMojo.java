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
import java.io.PrintStream;
import java.net.MalformedURLException;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.plugins.util.DeploymentClient;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * 
 * @goal distribute
 * 
 * @version $Rev:$ $Date:$
 *  
 */
public class DistributeModuleMojo extends AbstractModuleMojo {

    /**
     * @parameter
     */
    private String module;

    /**
     * @parameter
     */
    private String plan;

    private PrintStream logStream = System.out;    
    private PrintStream resultStream;
    
    private final String goalName = "Deploy Module";

    public void execute() throws MojoExecutionException {        
        resultStream = getResultsStream();        
        logStream = getLogStream(goalName);
        
        DeploymentManager manager;
        try {
            manager = getDeploymentManager();

            Target[] targets = manager.getTargets();
            File moduleFile = (this.module == null) ? null : getFile(this.module);
            File planFile = (this.plan == null) ? null : getFile((this.plan));
            ProgressObject progress = manager.distribute(targets, moduleFile, planFile);
            DeploymentClient.waitFor(progress);
        }
        catch (Exception e) {
            logResults(resultStream, goalName, "fail");
            handleError(e, logStream);
            return;
        }
        logResults(resultStream, goalName, "success");  
    }

    private File getFile(String location) throws MalformedURLException {
        try {
            File f = new File(location).getCanonicalFile();
            if (!f.exists() || !f.canRead()) {
                throw new MalformedURLException("Invalid location: " + location);
            }
            return f;
        }
        catch (IOException e) {
            throw (MalformedURLException) new MalformedURLException("Invalid location: " + location).initCause(e);
        }

    }

}
