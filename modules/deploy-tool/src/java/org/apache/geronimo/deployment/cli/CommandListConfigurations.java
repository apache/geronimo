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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.ConfigurationMetadata;
import org.apache.geronimo.system.configuration.DownloadResults;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev: 356097 $ $Date: 2005-12-11 20:29:03 -0500 (Sun, 11 Dec 2005) $
 */
public class CommandListConfigurations extends AbstractCommand {
    public CommandListConfigurations() {
        super("install-configs", "3. Geronimo Configurations", "MavenRepoURL",
                "Lists the Geronimo configurations available in a Maven repository "+
                "and lets you select configurations to download and install.  This "+
                "is used to add new functionality to the Geronimo server.");
    }

    //todo: provide a way to handle a username and password for the remote repo?

    public CommandListConfigurations(String command, String group, String helpArgumentList, String helpText) {
        super(command, group, helpArgumentList, helpText);
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        if(args.length == 0) {
            throw new DeploymentSyntaxException("Must specify Maven repository URL");
        }
        DeploymentManager dmgr = connection.getDeploymentManager();
        if(dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            ConfigurationMetadata[] data;
            URL repository;
            try {
                repository = new URL(args[0]);
                data = mgr.listConfigurations(repository, null, null);
            } catch (IOException e) {
                throw new DeploymentException("Unable to list configurations", e);
            }
            Map categories = new HashMap();
            List available = new ArrayList();
            for (int i = 0; i < data.length; i++) {
                ConfigurationMetadata metadata = data[i];
                List list = (List) categories.get(metadata.getCategory());
                if(list == null) {
                    list = new ArrayList();
                    categories.put(metadata.getCategory(), list);
                }
                list.add(metadata);
            }
            for (Iterator it = categories.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String category = (String) entry.getKey();
                List items = (List) entry.getValue();
                System.out.println();
                System.out.print(DeployUtils.reformat(category, 4, 72));
                for (int i = 0; i < items.size(); i++) {
                    ConfigurationMetadata metadata = (ConfigurationMetadata) items.get(i);
                    String prefix = "    ";
                    if(!metadata.isInstalled() && metadata.isEligible()) {
                        available.add(metadata);
                        prefix = Integer.toString(available.size());
                        if(available.size() < 10) {
                            prefix += " ";
                        }
                        prefix += ": ";
                    }
                    System.out.print(DeployUtils.reformat(prefix+metadata.getDescription()+" ("+metadata.getVersion()+")", 8, 72));
                    System.out.flush();
                }
            }
            System.out.println();
            System.out.print("Install Service [enter number or 'q' to quit]: ");
            System.out.flush();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String answer = in.readLine();
                if(answer.equalsIgnoreCase("q")) {
                    return;
                }
                int selection = Integer.parseInt(answer);
                ConfigurationMetadata target = ((ConfigurationMetadata) available.get(selection - 1));
                DownloadResults results = mgr.install(repository, null, null, target.getConfigId());
                for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                    Artifact uri = results.getDependenciesPresent()[i];
                    System.out.print(DeployUtils.reformat("Using existing dependency "+uri, 4, 72));
                }
                for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                    Artifact uri = results.getDependenciesInstalled()[i];
                    System.out.print(DeployUtils.reformat("Installed new dependency "+uri, 4, 72));
                }
                new CommandStart().execute(out, connection, new String[]{target.getConfigId().toString()});
            } catch (IOException e) {
                throw new DeploymentException("Unable to install configuration", e);
            } catch(NumberFormatException e) {
                throw new DeploymentException("Invalid response");
            }
        } else {
            throw new DeploymentException("Cannot list repositories when connected to "+connection.getServerURI());
        }
    }
}
