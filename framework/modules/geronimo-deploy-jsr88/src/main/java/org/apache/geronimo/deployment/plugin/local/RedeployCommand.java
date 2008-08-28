/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.plugin.ConfigIDExtractor;
import org.apache.geronimo.deployment.plugin.TargetImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.LifecycleResults;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class RedeployCommand extends AbstractDeployCommand {
    private static final String[] IS_IN_PLACE_CONFIGURATION_SIG =  {Artifact.class.getName()};
    private static final String IS_IN_PLACE_CONFIGURATION_METH = "isInPlaceConfiguration";

    private final TargetModuleID[] modules;

    public RedeployCommand(Kernel kernel, TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        super(CommandType.REDEPLOY, kernel, moduleArchive, deploymentPlan, null, null, null, false);
        this.modules = moduleIDList;
    }

    public RedeployCommand(Kernel kernel, TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        super(CommandType.REDEPLOY, kernel, null, null, null, moduleArchive, deploymentPlan, true);
        this.modules = moduleIDList;
    }

    public void run() {
        if (deployer == null) {
            return;
        }

        try {
            if (spool) {
                if (moduleStream != null) {
                    moduleArchive = createTempFile(moduleType == null? null: moduleType.getModuleExtension());
                    copyTo(moduleArchive, moduleStream);
                }
                if (deploymentStream != null) {
                    deploymentPlan = createTempFile(null);
                    copyTo(deploymentPlan, deploymentStream);
                }
            }
            Artifact configID = null;
            if(deploymentPlan != null) {
                String extracted = ConfigIDExtractor.extractModuleIdFromPlan(deploymentPlan);
                if(extracted != null) {
                    configID = Artifact.create(extracted);
                }
            } else {
                String extracted = ConfigIDExtractor.extractModuleIdFromArchive(moduleArchive);
                if(extracted != null) {
                    configID = Artifact.create(extracted);
                }
            }
            if(configID != null && configID.getGroupId() == null) {
                configID = new Artifact(Artifact.DEFAULT_GROUP_ID, configID.getArtifactId(),
                                        configID.getVersion(), configID.getType());
            }

            ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
            try {
                for (int i = 0; i < modules.length; i++) {
                    TargetModuleIDImpl module = (TargetModuleIDImpl) modules[i];
                    Artifact artifact = Artifact.create(module.getModuleID());
                    if(configID != null && configID.isResolved()) {
                        if(configID.getGroupId().equals(artifact.getGroupId()) &&
                                configID.getArtifactId().equals(artifact.getArtifactId()) &&
                                configID.getVersion().equals(artifact.getVersion())) {
                            redeploySameConfiguration(configurationManager, artifact, module.getTarget());
                        } else {
                            redeployUpdatedConfiguration(configurationManager, artifact, module.getTarget());
                        }
                    } else {
                        redeployUpdatedConfiguration(configurationManager, artifact, module.getTarget());
                    }
                }
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
            }
            addWebURLs(kernel);
            complete("Completed");
        } catch (Throwable e) {
            doFail(e);
        } finally {
            if (spool) {
                if (moduleArchive != null) {
                    moduleArchive.delete();
                }
                if (deploymentPlan != null) {
                    deploymentPlan.delete();
                }
            }
        }
    }

    private void redeployUpdatedConfiguration(ConfigurationManager manager, Artifact previous, Target target) throws Exception, NoSuchConfigException {
        // Send the new configuration to the server

            // if the configuration is an in-place one, then redeploys
            // in in-place mode.
        TargetImpl impl = (TargetImpl) target;
        AbstractName storeName = impl.getAbstractName();
        Boolean inPlaceConfiguration = (Boolean) kernel.invoke(storeName, IS_IN_PLACE_CONFIGURATION_METH, new Object[]{previous}, IS_IN_PLACE_CONFIGURATION_SIG);
        commandContext.setInPlace(inPlaceConfiguration.booleanValue());
        doDeploy(target, false);
        Artifact configID = Artifact.create(getResultTargetModuleIDs()[0].getModuleID());
        LifecycleResults results = manager.reloadConfiguration(previous, configID.getVersion());

        // Activate it
        //todo: make this asynchronous
        boolean newStarted = false;
        for (Iterator it = results.getStopped().iterator(); it.hasNext();) {
            Artifact name = (Artifact) it.next();
            updateStatus("Stopped "+name);
        }
        for (Iterator it = results.getUnloaded().iterator(); it.hasNext();) {
            Artifact name = (Artifact) it.next();
            updateStatus("Unloaded "+name);
        }
        for (Iterator it = results.getLoaded().iterator(); it.hasNext();) {
            Artifact name = (Artifact) it.next();
            updateStatus("Loaded "+name);
        }
        for (Iterator it = results.getStarted().iterator(); it.hasNext();) {
            Artifact name = (Artifact) it.next();
            updateStatus("Started "+name);
            if(configID.matches(name)) {
                newStarted = true;
            }
        }
        for (Iterator it = results.getFailed().keySet().iterator(); it.hasNext();) {
            Artifact name = (Artifact) it.next();
            updateStatus("Failed on "+name+": "+results.getFailedCause(name).getMessage());
            doFail((Exception)results.getFailedCause(name));
        }
        if(results.getFailed().size() == 0 && !newStarted) {
            updateStatus("Note: new module was not started (probably because old module was not running).");
        }
    }

    private void redeploySameConfiguration(ConfigurationManager configurationManager, Artifact configID, Target target) throws Exception {
        if(!configID.isResolved()) {
            throw new IllegalStateException("Cannot redeploy same module when module ID is not fully resolved ("+configID+")");
        }
        try {
            configurationManager.stopConfiguration(configID);
            updateStatus("Stopped "+configID);
        } catch (InternalKernelException e) {
            Exception cause = (Exception)e.getCause();
            if(cause instanceof NoSuchConfigException) {
                // The modules isn't loaded -- that's OK
            } else {
                throw cause;
            }
        } catch(NoSuchConfigException e) {
            // The module isn't loaded -- that's OK
        }
        try {
            configurationManager.unloadConfiguration(configID);
            updateStatus("Unloaded "+configID);
        } catch(InternalKernelException e) {
            Exception cause = (Exception)e.getCause();
            if(cause instanceof NoSuchConfigException) {
                // The modules isn't loaded -- that's OK
            } else {
                throw cause;
            }
        } catch (NoSuchConfigException e) {
            // The modules isn't loaded -- that's OK
        }

        // if the configuration is an in-place one, then redeploys
        // in in-place mode.
        TargetImpl impl = (TargetImpl) target;
        AbstractName storeName = impl.getAbstractName();
        Boolean inPlaceConfiguration = (Boolean) kernel.invoke(storeName, IS_IN_PLACE_CONFIGURATION_METH, new Object[]{configID}, IS_IN_PLACE_CONFIGURATION_SIG);
        commandContext.setInPlace(inPlaceConfiguration.booleanValue());

        try {
            configurationManager.uninstallConfiguration(configID);
            updateStatus("Uninstalled "+configID);
        } catch(InternalKernelException e) {
            Exception cause = (Exception)e.getCause();
            if(cause instanceof NoSuchConfigException) {
                throw new IllegalStateException("Module "+configID+" is not installed!", cause);
            } else {
                throw cause;
            }
        } catch (NoSuchConfigException e) {
            throw new IllegalStateException("Module "+configID+" is not installed!", e);
        }

        doDeploy(target, false);
        updateStatus("Deployed "+configID);

        configurationManager.loadConfiguration(configID);
        configurationManager.startConfiguration(configID);
        updateStatus("Started " + configID);
    }
}
