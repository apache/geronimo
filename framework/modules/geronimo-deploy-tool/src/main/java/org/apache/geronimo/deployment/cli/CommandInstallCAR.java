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
import java.io.IOException;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.geronimo.cli.deployer.BaseCommandArgs;
import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.DownloadResults;
import jline.ConsoleReader;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev$ $Date$
 */
public class CommandInstallCAR extends AbstractCommand {

    //todo: provide a way to handle a username and password for the remote repo?

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        DeploymentManager dmgr = connection.getDeploymentManager();
        if(dmgr instanceof GeronimoDeploymentManager) {
            try {
                GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
                File carFile = new File(commandArgs.getArgs()[0]);
                carFile = carFile.getAbsoluteFile();
                if(!carFile.exists() || !carFile.canRead()) {
                    throw new DeploymentException("CAR file cannot be read: "+carFile.getAbsolutePath());
                }
                Object key = mgr.startInstall(carFile, null, null);
                long start = System.currentTimeMillis();
                DownloadResults results = showProgress(mgr, key);
                int time = (int)(System.currentTimeMillis() - start) / 1000;
                consoleReader.printNewline();
                if(!results.isFailed()) {
                    consoleReader.printString(DeployUtils.reformat("**** Installation Complete!", 4, 72));
                    for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                        Artifact uri = results.getDependenciesPresent()[i];
                        consoleReader.printString(DeployUtils.reformat("Used existing: "+uri, 4, 72));
                    }
                    for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                        Artifact uri = results.getDependenciesInstalled()[i];
                        consoleReader.printString(DeployUtils.reformat("Installed new: "+uri, 4, 72));
                    }
                    if(results.getTotalDownloadBytes() > 0 && time > 0) {
                        System.out.println();
                        consoleReader.printString(DeployUtils.reformat("Downloaded "+(results.getTotalDownloadBytes()/1024)+" kB in "+time+"s ("+results.getTotalDownloadBytes()/(1024*time)+" kB/s)", 4, 72));
                    }
                }
                if(results.isFinished() && !results.isFailed() && results.getInstalledConfigIDs().length == 1) {
                    Artifact target = results.getInstalledConfigIDs()[0];
                    consoleReader.printString(DeployUtils.reformat("Now starting "+target+"...", 4, 72));
                    consoleReader.flushConsole();
                    new CommandStart().execute(consoleReader, connection, new BaseCommandArgs(new String[]{target.toString()}));
                }
            } catch (IOException e) {
                throw new DeploymentException("Cannot install plugin", e);
            }
        } else {
            throw new DeploymentException("Cannot install plugins when connected to "+connection.getServerURI());
        }
    }

    static DownloadResults showProgress(GeronimoDeploymentManager mgr, Object key) {
        System.out.println("Checking for status every 1000ms:");
        String last = null, status;
        while(true) {
            DownloadResults results = mgr.checkOnInstall(key);
            if(results.getCurrentFile() != null) {
                if(results.getCurrentFilePercent() > -1) {
                    status = results.getCurrentMessage()+" ("+results.getCurrentFilePercent()+"%)";
                } else {
                    status = results.getCurrentMessage();
                }
                if(last == null || !last.equals(status)) {
                    last = status;
                    System.out.println(status);
                }
            }
            if(results.isFinished()) {
                if(results.isFailed()) {
                    System.err.println("Installation FAILED: "+results.getFailure().getMessage());
                }
                return results;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return results;
            }
        }
    }
}
