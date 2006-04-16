/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.deployment.cli;

import java.io.File;
import java.io.PrintWriter;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.DownloadResults;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev: 356097 $ $Date: 2005-12-11 20:29:03 -0500 (Sun, 11 Dec 2005) $
 */
public class CommandInstallCAR extends AbstractCommand {
    public CommandInstallCAR() {
        super("install-plugin", "3. Geronimo Configurations", "PluginFile",
                "Installs a Geronimo plugin you've exported from a Geronimo server " +
                "or downloaded from an external repository.  The file must be a " +
                "properly configured Geronimo CAR file.  This is used to add new " +
                "functionality to the Geronimo server.");
    }

    //todo: provide a way to handle a username and password for the remote repo?

    public CommandInstallCAR(String command, String group, String helpArgumentList, String helpText) {
        super(command, group, helpArgumentList, helpText);
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(args.length == 0) {
            throw new DeploymentSyntaxException("Must specify Plugin CAR file");
        }
        DeploymentManager dmgr = connection.getDeploymentManager();
        if(dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            File carFile = new File(args[0]);
            carFile = carFile.getAbsoluteFile();
            if(!carFile.exists() || !carFile.canRead()) {
                throw new DeploymentException("CAR file cannot be read: "+carFile.getAbsolutePath());
            }
            Object key = mgr.startInstall(carFile, null, null);
            long start = System.currentTimeMillis();
            DownloadResults results = showProgress(mgr, key);
            int time = (int)(System.currentTimeMillis() - start) / 1000;
            System.out.println();
            if(!results.isFailed()) {
                System.out.println(DeployUtils.reformat("**** Installation Complete!", 4, 72));
                for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                    Artifact uri = results.getDependenciesPresent()[i];
                    System.out.print(DeployUtils.reformat("Used existing: "+uri, 4, 72));
                }
                for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                    Artifact uri = results.getDependenciesInstalled()[i];
                    System.out.print(DeployUtils.reformat("Installed new: "+uri, 4, 72));
                }
                System.out.println();
                System.out.println(DeployUtils.reformat("Downloaded "+(results.getTotalDownloadBytes()/1024)+" kB in "+time+"s ("+results.getTotalDownloadBytes()/(1024*time)+" kB/s)", 4, 72));
            }
            if(results.isFinished() && !results.isFailed() && results.getInstalledConfigIDs().length == 1) {
                Artifact target = results.getInstalledConfigIDs()[0];
                System.out.print(DeployUtils.reformat("Now starting "+target+"...", 4, 72));
                System.out.flush();
                new CommandStart().execute(out, connection, new String[]{target.toString()});
            }
        } else {
            throw new DeploymentException("Cannot install plugins when connected to "+connection.getServerURI());
        }
    }

    private DownloadResults showProgress(GeronimoDeploymentManager mgr, Object key) {
        System.out.println("Checking for status every 1000ms:");
        while(true) {
            DownloadResults results = mgr.checkOnInstall(key);
            if(results.getCurrentFile() != null) {
                if(results.getCurrentFilePercent() > -1) {
                    System.out.println(results.getCurrentMessage()+" ("+results.getCurrentFilePercent()+"%)");
                } else {
                    System.out.println(results.getCurrentMessage());
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
