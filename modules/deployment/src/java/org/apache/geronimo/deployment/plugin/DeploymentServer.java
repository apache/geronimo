/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.plugin;

import java.io.InputStream;
import java.net.URI;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.DeploymentModule;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:36 $
 */
public interface DeploymentServer {
    public boolean isLocal();

    public Target[] getTargets() throws IllegalStateException;

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException, IllegalStateException;

    public ProgressObject distribute(Target[] targetList, DeploymentModule module, URI configID) throws IllegalStateException;

    public ProgressObject start(TargetModuleID[] moduleIDList) throws IllegalStateException;

    public ProgressObject stop(TargetModuleID[] moduleIDList) throws IllegalStateException;

    public ProgressObject undeploy(TargetModuleID[] moduleIDList) throws IllegalStateException;

    public boolean isRedeploySupported();

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) throws UnsupportedOperationException, IllegalStateException;

    public void release();

}
