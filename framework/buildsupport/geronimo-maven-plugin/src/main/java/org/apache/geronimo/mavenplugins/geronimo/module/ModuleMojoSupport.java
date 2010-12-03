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

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.mavenplugins.geronimo.ModuleConfig;
import org.apache.geronimo.mavenplugins.geronimo.reporting.ReportingMojoSupport;

import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Support for mojos that operate on modules.
 *
 * @version $Rev$ $Date$
 */
public abstract class ModuleMojoSupport
    extends ReportingMojoSupport
{
    private static final String URI_PREFIX = "deployer:geronimo:jmx";

    /**
     * List of module artifact configurations.  Artifacts need to point to jar | war | ear | rar archive.
     * Module artifact's configurations should match the moduleId specified in the plan, if plan exists.
     *
     * @parameter
     */
    protected ModuleConfig[] modules;

    /**
     * Cached deployment manager.
     */
    private DeploymentManager deploymentManager;

    /**
     * Get a deployment manager; if the manager was previosuly initialized then that cached instance is used.
     *
     * @return  Deployment manager instance; never null
     *
     * @throws IOException
     * @throws DeploymentManagerCreationException
     */
    protected DeploymentManager getDeploymentManager() throws IOException, DeploymentManagerCreationException {
        if (deploymentManager == null) {
            // Register the Geronimo factory
            DeploymentFactoryManager manager = DeploymentFactoryManager.getInstance();
            manager.registerDeploymentFactory(new DeploymentFactoryImpl());

            String uri = URI_PREFIX + "://" + hostname + ":" + port;

            DeploymentFactoryManager factoryManager = DeploymentFactoryManager.getInstance();
            deploymentManager = factoryManager.getDeploymentManager(uri, username, password);
        }

        return deploymentManager;
    }

    /**
     * Waits for the given progress to stop running.
     *
     * @param progress  The progress object to wait for.
     * @return          The status of the deployment; never null
     *
     * @throws InterruptedException
     */
    protected DeploymentStatus waitFor(final ProgressObject progress) throws InterruptedException {
        assert progress != null;

        //
        // TODO: Add timeout?
        //
        
        ProgressListener listener = new ProgressListener()
        {
            public void handleProgressEvent(final ProgressEvent event) {
                DeploymentStatus status = event.getDeploymentStatus();

                if (!status.isRunning()) {
                    synchronized (progress) {
                        progress.notify();
                    }
                }
            }
        };

        progress.addProgressListener(listener);

        synchronized (progress) {
            while (progress.getDeploymentStatus().isRunning()) {
                progress.wait();
            }
        }

        return progress.getDeploymentStatus();
    }

    /**
     * Returns the Geronimo moduleId for the given artifact.
     *
     * @param item  The artifact item to get the moduleId for.
     * @return      The moduleId of the given artifact item.
     */
    protected String getModuleId(final ArtifactItem item) {
        assert item != null;

        return item.getGroupId() + "/" + item.getArtifactId() + "/" + item.getVersion() + "/" + item.getType();
    }

    /**
     * Given a list of modules, return a list of non-running ones.
     *
     * @param moduleIds  The list of modules to check
     * @return          The list of modules that are not running
     *
     * @throws Exception
     */
    protected TargetModuleID[] getNonRunningModules(final TargetModuleID[] moduleIds) throws Exception {
        assert moduleIds != null;

        List modulesList = new ArrayList();

        DeploymentManager manager = getDeploymentManager();

        Target[] targets = manager.getTargets();
        TargetModuleID runningModuleIds[] = manager.getRunningModules(null, targets);

        for (int j = 0; j < moduleIds.length; j++) {
            String moduleId = moduleIds[j].getModuleID();
            log.debug("Checking if module is running: " + moduleId);

            boolean found = false;
            for (int i = 0; i < runningModuleIds.length; i++) {
            String runningModuleId = runningModuleIds[i].getModuleID();
                if (moduleId.equals(runningModuleId)) {
                    log.debug("Module is running: " + moduleId);
                    found = true;
                    break;
                }
            }

            if (!found) {
                log.debug("Module is not running: " + moduleId);
                modulesList.add(moduleIds[j]);
            }
        }
        return (TargetModuleID[]) modulesList.toArray(new TargetModuleID[modulesList.size()]);
    }

    /**
     * Check if the given module is started.
     *
     * @param moduleId  The module ID to check
     * @return          True if the module for this ID is started.
     *
     * @throws Exception
     */
    protected boolean isModuleStarted(final String moduleId) throws Exception {
        assert moduleId != null;

        log.debug("Checking if module is started: " + moduleId);
        
        DeploymentManager manager = getDeploymentManager();

        Target[] targets = manager.getTargets();
        TargetModuleID targetIds[] = manager.getRunningModules(null, targets);

        for (int i = 0; i < targetIds.length; i++) {
            if (moduleId.equals(targetIds[i].getModuleID())) {
                return true;
            }
        }

        return false;
    }

    protected TargetModuleID[] findModules(final String moduleId, final TargetModuleID targetIds[]) {
        assert moduleId != null;
        assert targetIds != null;

        List found = new ArrayList();

        log.debug("Scanning for modules that match: " + moduleId);
        for (int i = 0; i < targetIds.length; i++) {
            log.debug("Checking: " + targetIds[i].getModuleID());

            if (moduleId.equals(targetIds[i].getModuleID())) {
                found.add(targetIds[i]);
            }
        }

        return (TargetModuleID[]) found.toArray(new TargetModuleID[found.size()]);
    }

    //
    // TODO: Can probably wrap up some of this into findModules with a flag for running or non-running
    //
    
    protected void startModule() throws Exception {
        assert modules != null;

        for (int i=0; i<modules.length; i++) {
            String moduleId = getModuleId(modules[i]);
            
            if (isModuleStarted(moduleId)) {
                log.warn("Module is already started: " + moduleId);
                continue;
                //throw new MojoExecutionException("Module is already started: " + moduleId);
            }
            
            DeploymentManager manager = getDeploymentManager();
            Target[] targets = manager.getTargets();
            TargetModuleID[] targetIds = manager.getNonRunningModules(null, targets);

            TargetModuleID[] found = findModules(moduleId, targetIds);

            if (found.length == 0) {
                throw new MojoExecutionException("Module is not deployed: " + moduleId);
            }

            log.info("Starting module: " + moduleId);
            ProgressObject progress = manager.start(found);
            
            DeploymentStatus status = waitFor(progress);
            if (status.isFailed()) {
                throw new MojoExecutionException("Failed to start module: " + moduleId);
            }
            
            log.info("Started module(s):");
            logModules(found, "    ");
        }
    }

    protected void stopModule() throws Exception {
        assert modules != null;

        DeploymentManager manager = getDeploymentManager();
        Target[] targets = manager.getTargets();
        TargetModuleID[] targetIds = manager.getRunningModules(null, targets);

        for (int i=0; i<modules.length; i++) {
           String moduleId = getModuleId(modules[i]);
           if (!isModuleStarted(moduleId)) {
               log.info("Module is already stopped: " + moduleId);
               continue;
               //throw new MojoExecutionException("Module is not started: " + moduleId);
           }

           TargetModuleID[] found = findModules(moduleId, targetIds);

           if (found.length == 0) {
               throw new MojoExecutionException("Module not deployed: " + moduleId);
           }

           log.info("Stopping module: " + moduleId);
           ProgressObject progress = manager.stop(found);

           DeploymentStatus status = waitFor(progress);
           if (status.isFailed()) {
               throw new MojoExecutionException("Failed to stop module: " + moduleId);
           }

           log.info("Stopped module(s):");
           logModules(found, "    ");
        }
    }

    protected void undeployModule() throws Exception {
        assert modules != null;

        stopModule();

        DeploymentManager manager = getDeploymentManager();
        Target[] targets = manager.getTargets();
        TargetModuleID[] targetIds = manager.getNonRunningModules(null, targets);

        for (int i=0; i<modules.length; i++) {
          String moduleId = getModuleId(modules[i]);

          TargetModuleID[] found = findModules(moduleId, targetIds);

          if (found.length == 0) {
              log.info("Module is not deployed: " + moduleId);
              continue;
              //throw new Exception("Module is not deployed: " + moduleId);
          }

          log.info("Undeploying module: " + moduleId);
          ProgressObject progress = manager.undeploy(found);

          DeploymentStatus status = waitFor(progress);
          if (status.isFailed()) {
              throw new MojoExecutionException("Failed to undeploy module: " + moduleId);
          }

          log.info("Undeployed module(s):");
          logModules(found, "    ");
        }
    }

    protected void logModules(final TargetModuleID[] targetIds) {
        logModules(targetIds, "");
    }

    protected void logModules(final TargetModuleID[] targetIds, final String pad) {
        assert targetIds != null;
        assert pad != null;

        for (int i=0; i<targetIds.length; i++) {
            String url = targetIds[i].getWebURL();
            getLog().info(pad + "[" + i + "] " + targetIds[i].getModuleID() + (url == null ? "" : ("; URL: " + url)));

            TargetModuleID[] children = targetIds[i].getChildTargetModuleID();
            if (children != null) {
                logModules(children, pad + "    ");
            }
        }
    }
}
