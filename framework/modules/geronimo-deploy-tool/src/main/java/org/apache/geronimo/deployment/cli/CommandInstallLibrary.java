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

package org.apache.geronimo.deployment.cli;

import java.io.File;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.InstallLibraryCommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class CommandInstallLibrary extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        if (!(commandArgs instanceof InstallLibraryCommandArgs)) {
            throw new DeploymentSyntaxException("CommandArgs has the type [" + commandArgs.getClass() + "]; expected [" + InstallLibraryCommandArgs.class + "]");
        }
        InstallLibraryCommandArgs installLibraryCommandArgs = (InstallLibraryCommandArgs)commandArgs;
        if (installLibraryCommandArgs.getArgs().length == 0) {
            throw new DeploymentException("Must specify a LibraryFile");
        }
        File libFile = new File(installLibraryCommandArgs.getArgs()[0]);
        if(!libFile.exists() || !libFile.isFile() || !libFile.canRead()) {
            throw new DeploymentException("File does not exist or not a normal file or not readable. "+libFile);
        }
        DeploymentManager dmgr = connection.getDeploymentManager();
        if(dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            String groupId = installLibraryCommandArgs.getGroupId();
            try {
                Artifact artifact = mgr.installLibrary(libFile, groupId);
                if(artifact != null) {
                    consoleReader.printString(DeployUtils.reformat("Installed "+artifact, 4, 72));
                } else {
                    throw new DeploymentException("Unable to install library "+installLibraryCommandArgs.getArgs()[0]);
                }
            } catch (Exception e) {
                throw new DeploymentException("Unable to install library "+installLibraryCommandArgs.getArgs()[0], e);
            }
        } else {
            throw new DeploymentException("Cannot install library using " + dmgr.getClass().getName() + " deployment manager");
        }
    }
}
