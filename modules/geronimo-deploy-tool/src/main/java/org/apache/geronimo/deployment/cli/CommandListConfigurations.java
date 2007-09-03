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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.cli.deployer.BaseCommandArgs;
import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev$ $Date$
 */
public class CommandListConfigurations extends AbstractCommand {

    //todo: provide a way to handle a username and password for the remote repo?

    public void execute(PrintWriter out, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        DeploymentManager dmgr = connection.getDeploymentManager();
        if (dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            try {
                String repo;
                if (commandArgs.getArgs().length == 1) {
                    repo = commandArgs.getArgs()[0];
                } else {
                    repo = getRepository(out, new BufferedReader(new InputStreamReader(System.in)), mgr);
                }
                PluginListType data;
                URL repository;
                try {
                    repository = new URL(repo);
                    data = mgr.listPlugins(repository, null, null);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to list configurations", e);
                } catch (FailedLoginException e) {
                    throw new DeploymentException("Invalid login for Maven repository '" + repo + "'", e);
                }
                if (data == null) {
                    out.println();
                    out.println("No plugins were returned from this site.");
                    return;
                }
                Map<String, List<PluginType>> categories = new HashMap<String, List<PluginType>>();
                for (PluginType metadata : data.getPlugin()) {
                    List<PluginType> list = categories.get(metadata.getCategory());
                    if (list == null) {
                        list = new ArrayList<PluginType>();
                        categories.put(metadata.getCategory(), list);
                    }
                    list.add(metadata);
                }
                List<PluginType> available = new ArrayList<PluginType>();
                for (Map.Entry<String, List<PluginType>> entry : categories.entrySet()) {
                    String category = entry.getKey();
                    List<PluginType> items = entry.getValue();
                    out.println();
                    out.print(DeployUtils.reformat(category, 4, 72));
                    for (PluginType metadata : items) {
                        out.println("  " + metadata.getName());
                        for (PluginArtifactType instance : metadata.getPluginArtifact()) {
                            String prefix;
//                            if (!instance.isInstalled() && instance.isEligible()) {
                            PluginType copy = PluginInstallerGBean.copy(metadata, instance);
                                available.add(copy);
                                prefix = Integer.toString(available.size());
                                if (available.size() < 10) {
                                    prefix += " ";
                                }
                                prefix += ": ";
//                            }
                            out.print(DeployUtils.reformat(
                                    prefix + " (" + instance.getModuleId().getVersion() + ")", 8, 72));
                            out.flush();
                        }
                    }
                }
                if (available.size() == 0) {
                    out.println();
                    out.println("No plugins from this site are eligible for installation.");
                    return;
                }
                out.println();
                out.print("Install Service [enter number or 'q' to quit]: ");
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String answer = in.readLine();
                if (answer.equalsIgnoreCase("q")) {
                    return;
                }
                int selection = Integer.parseInt(answer);
                PluginType target = available.get(selection - 1);
                PluginArtifactType targetInstance = target.getPluginArtifact().get(0);
                PluginListType list = new PluginListType();
                list.getPlugin().add(target);
                list.getDefaultRepository().add(repo);
                long start = System.currentTimeMillis();
                Object key = mgr.startInstall(list, null, null);
                DownloadResults results = CommandInstallCAR.showProgress(mgr, key);
                int time = (int) (System.currentTimeMillis() - start) / 1000;
                out.println();
                if (!results.isFailed()) {
                    out.print(DeployUtils.reformat("**** Installation Complete!", 4, 72));
                    for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                        Artifact uri = results.getDependenciesPresent()[i];
                        out.print(DeployUtils.reformat("Used existing: " + uri, 4, 72));
                    }
                    for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                        Artifact uri = results.getDependenciesInstalled()[i];
                        out.print(DeployUtils.reformat("Installed new: " + uri, 4, 72));
                    }
                    out.println();
                    out.print(DeployUtils.reformat(
                            "Downloaded " + (results.getTotalDownloadBytes() / 1024) + " kB in " + time + "s (" + results.getTotalDownloadBytes() / (1024 * time) + " kB/s)",
                            4, 72));
                }
                if (results.isFinished() && !results.isFailed()) {
                    out.print(DeployUtils.reformat("Now starting " + PluginInstallerGBean.toArtifact(targetInstance.getModuleId()) + "...", 4, 72));
                    out.flush();
                    new CommandStart().execute(out, connection,
                            new BaseCommandArgs(new String[]{PluginInstallerGBean.toArtifact(targetInstance.getModuleId()).toString()}));
                }
            } catch (IOException e) {
                throw new DeploymentException("Unable to install configuration", e);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid response");
            }
        } else {
            throw new DeploymentException("Cannot list repositories when connected to " + connection.getServerURI());
        }
    }

    private String getRepository(PrintWriter out, BufferedReader in, GeronimoDeploymentManager mgr) throws IOException, DeploymentException {
        URL[] all = mgr.getRepositories();
        if (all.length == 0) {
            throw new DeploymentException("No default repositories available.  Please either specify the repository " +
                    "URL on the command line, or go into the console Plugin page and update the list of available " +
                    "repositories.");
        }
        out.println();
        out.println("Select repository:");
        for (int i = 0; i < all.length; i++) {
            URL url = all[i];
            out.println("  " + (i + 1) + ". " + url);
        }
        out.println();
        out.print("Enter Repository Number: ");
        out.flush();
        String entry = in.readLine().trim();
        int index = Integer.parseInt(entry);
        return all[index - 1].toString();
    }
}
