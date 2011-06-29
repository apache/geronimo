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

import java.io.IOException;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;

public class CommandUninstallBundle extends AbstractCommand {
    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        
        if (!isOffline(connection)) {
            
            long bundleId = Long.parseLong(commandArgs.getArgs()[0]);
            
            DeploymentManager dmgr = connection.getDeploymentManager();
            if(dmgr instanceof GeronimoDeploymentManager) {
                GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
                try {
                    mgr.eraseUninstall(bundleId);
                    consoleReader.printString(DeployUtils.reformat("Uninstalled and erased bundle: " + bundleId, 4, 72));
                    
                } catch (Exception e) {
                    throw new DeploymentException("Unable to erase bundle: " + bundleId, e);
                }
            } else {
                throw new DeploymentException("Unable to uninstall bundle using " + dmgr.getClass().getName() + " deployment manager");
            }
            
        } else { //offline not supported
            try {
                consoleReader.printString(DeployUtils.reformat("Uninstall bundle offline is not supported!", 4, 72));
            } catch (IOException e) {
                throw new DeploymentException("Uninstall bundle offline is not supported!");
            }
        }
    }
    
}
