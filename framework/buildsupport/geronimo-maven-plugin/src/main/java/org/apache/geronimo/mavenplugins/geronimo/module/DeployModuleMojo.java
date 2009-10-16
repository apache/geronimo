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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.geronimo.mavenplugins.geronimo.ModuleConfig;

/**
 * Deploy modules (and optionally starting them) to a Geronimo server.
 *
 * @goal deploy-module
 * 
 * @version $Rev$ $Date$
 */
public class DeployModuleMojo
    extends ModuleMojoSupport
{
    /**
     * A file which points to a specific module's jar | war | ear | rar archive.
     * If this parameter is set, then it will be used instead of from the
     * modules configuration.
     *
     * @parameter expression="${moduleArchive}"
     * @optional
     */
    protected File moduleArchive = null;

    /**
     * The fully qualified path of the external plan file (geronimo-web.xml).
     * The application module may already have included in the package a deployment plan or the
     * application is so simple that may not require any deployment plan.
     * 
     * @parameter expression="${modulePlan}"
     * @optional
     */
    private File modulePlan = null;

    /**
     * Flag to indicate if modules should be started after they have been distributed to the server.
     *
     * @parameter default-value="true"
     * @optional
     */
    private boolean startModules = false;

    protected void doExecute() throws Exception {
        List completed = new ArrayList();

        if (moduleArchive != null || modulePlan != null) {
            log.info("Using non-artifact based module archive: " + moduleArchive);
            if (modulePlan != null) {
                log.info("Using non-artifact based plan: " + modulePlan);
            }

            TargetModuleID[] ids = distribute(moduleArchive, modulePlan);
            completed.add(ids);
        }
        else if (modules == null || modules.length == 0) {
            throw new MojoExecutionException("At least one module configuration (or moduleArchive) must be specified");
        }

        if (modules != null && modules.length != 0) {
            log.info("Using artifact based module archive(s)...");

            for (int i=0; i<modules.length; i++) {
                TargetModuleID[] ids = distribute(getModuleArchive(modules[i]), modules[i].getPlan());
                completed.add(ids);
            }
        }

        if (startModules) {
            log.info("Starting modules...");

            Iterator iter = completed.iterator();
            while (iter.hasNext()) {
                TargetModuleID[] moduleIds = (TargetModuleID[])iter.next();
                for (int i=0; i < moduleIds.length; i++) {
                    String url = moduleIds[i].getWebURL();
                    log.info("Starting module: " + moduleIds[i].getModuleID() + (url == null ? "" : ("; URL: " + url)));
                }

                ProgressObject progress = getDeploymentManager().start(moduleIds);
                DeploymentStatus status = waitFor(progress);

                if (status.isFailed()) {
                    throw new MojoExecutionException("Failed to start modules: " + status.getMessage());
                }

                log.info("Started module(s):");
                logModules(moduleIds, "    ");
            }
        }
    }

    private File getModuleArchive(final ModuleConfig module) throws MojoExecutionException {
        Artifact artifact = getArtifact(module);

        File file = artifact.getFile();
        if (file == null) {
            throw new MojoExecutionException("Module artifact does not have an attached file: " + module);
        }

        String type = artifact.getType();
        log.debug("Artifact file is: " + file + " (" + type + ")");

        if ((!"war".equals(type)) &&
            (!"ear".equals(type)) &&
            (!"rar".equals(type)) &&
            (!"jar".equals(type)))
        {
            throw new MojoExecutionException("Module does not look like a JavaEE archive: " + module);
        }

        return file;
    }

    private TargetModuleID[] distribute(final File file, final File plan) throws Exception {
        if (log.isInfoEnabled()) {
            String msg = "Distributing module artifact: " + file;
            if (plan != null) {
                msg += " with plan " + plan;
            }
            log.info(msg);
        }

        DeploymentManager manager = getDeploymentManager();
        Target[] targets = manager.getTargets();
        if (null == targets) {
            throw new IllegalStateException("No target to distribute to");
        }
        targets = new Target[] {targets[0]};
        
        ProgressObject progress = manager.distribute(targets, file, plan);
        DeploymentStatus status = waitFor(progress);

        if (status.isFailed()) {
            //
            // FIXME: There must be a better way to handle this.
            //
            if (status.getMessage().indexOf("already exists") < 0 ) {
                throw new MojoExecutionException("Distribution failed: " + status.getMessage());
            }
            log.info("Module already exists");
        }
        

        return (progress != null) ? progress.getResultTargetModuleIDs() : null;
    }

    protected String getFullClassName() {
        return this.getClass().getName();
    } 
}
