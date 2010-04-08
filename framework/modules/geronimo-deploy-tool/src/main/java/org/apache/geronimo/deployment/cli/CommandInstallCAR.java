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

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.plugin.DownloadResults;

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
                if (commandArgs.getArgs().length == 0) {
                    throw new DeploymentException("Must specify Plugin CAR file");
                }
                File carFile = new File(commandArgs.getArgs()[0]);
                carFile = carFile.getAbsoluteFile();
                if(!carFile.exists() || !carFile.canRead()) {
                    throw new DeploymentException("CAR file cannot be read: "+carFile.getAbsolutePath());
                }
                //TODO figure out if there is a plausible default repo
                Object key = mgr.startInstall(carFile, null, false, null, null);
                long start = System.currentTimeMillis();
                DownloadResults results = showProgress(consoleReader, mgr, key);
                int time = (int)(System.currentTimeMillis() - start) / 1000;
                printResults(consoleReader, results, time);
            } catch (IOException e) {
                throw new DeploymentException("Cannot install plugin", e);
            }
        } else {
            throw new DeploymentException("Cannot install plugins using " + dmgr.getClass().getName() + " deployment manager");
        }
    }

    static DownloadResults showProgress(ConsoleReader consoleReader, GeronimoDeploymentManager mgr, Object key) throws IOException {
        DeployUtils.println("Checking for status every 1000ms:", 0, consoleReader);
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
                    DeployUtils.println(status, 0, consoleReader);
                    consoleReader.flushConsole();
                }
            }
            if(results.isFinished()) {
                if(results.isFailed()) {
                    DeployUtils.println("Installation FAILED: "+results.getFailure().getMessage(), 0, consoleReader);
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

    public static void printResults(ConsoleReader consoleReader, DownloadResults results, int time) throws IOException, DeploymentException {
        consoleReader.printNewline();
        if (!results.isFailed()) {
            DeployUtils.println("**** Installation Complete!", 0, consoleReader);
            for (MissingDependencyException e : results.getSkippedPlugins()) {
                DeployUtils.println(e.getMessage(), 0, consoleReader);
            }
            for (Artifact uri: results.getDependenciesPresent()) {
                DeployUtils.println("Used existing: " + uri, 0, consoleReader);
            }
            for (Artifact uri: results.getDependenciesInstalled()) {
                DeployUtils.println("Installed new: " + uri, 0, consoleReader);
            }
            consoleReader.printNewline();
            if (results.getTotalDownloadBytes() > 0 && time > 0) {
                DeployUtils.println(
                    "Downloaded " + (results.getTotalDownloadBytes() / 1024) + " kB in " + time + "s (" + results.getTotalDownloadBytes() / (1024 * time) + " kB/s)",
                    0, consoleReader);
            }
        }
    }
}
