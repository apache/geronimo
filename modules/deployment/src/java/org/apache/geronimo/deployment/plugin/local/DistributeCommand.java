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

package org.apache.geronimo.deployment.plugin.local;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.jar.JarOutputStream;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleDeployer;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.kernel.config.ConfigurationStore;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:57:38 $
 */
public class DistributeCommand extends CommandSupport {
    private final Target target;
    private final ConfigurationParent parent;
    private final URI configID;
    private final ConfigurationStore store;
    private final DeploymentModule module;

    public DistributeCommand(Target target, ConfigurationParent parent, URI configID, ConfigurationStore store, DeploymentModule module) {
        super(CommandType.DISTRIBUTE);
        this.target = target;
        this.parent = parent;
        this.configID = configID;
        this.store = store;
        this.module = module;
    }

    public void run() {
        File configFile = null;
        File workDir = null;
        try {
            // create some working space
            configFile = File.createTempFile("deploy", ".car");
            workDir = File.createTempFile("deploy", "");
            workDir.delete();
            workDir.mkdir();

            // convert the module to a Configuration
            TargetModuleID targetID = new TargetModuleIDImpl(target, configID.toString());
            ModuleDeployer deployer = new ModuleDeployer(parent, configID, workDir);
            deployer.addModule(module);
            deployer.deploy();

            // Save the Configuration into a CAR
            FileOutputStream os = new FileOutputStream(configFile);
            try {
                JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(os));
                deployer.saveConfiguration(jos);
                jos.flush();
            } finally {
                os.close();
            }

            // install in our local server
            store.install(configFile.toURL());
            addModule(targetID);
            complete("Completed");
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (workDir != null) {
                FileUtil.recursiveDelete(workDir);
            }
            if (configFile != null) {
                configFile.delete();
            }
        }
    }
}
