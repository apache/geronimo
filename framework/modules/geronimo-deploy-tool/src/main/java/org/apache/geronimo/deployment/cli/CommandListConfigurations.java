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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.security.auth.login.FailedLoginException;

import jline.ConsoleReader;
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

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        DeploymentManager dmgr = connection.getDeploymentManager();
        if (dmgr instanceof GeronimoDeploymentManager) {
            GeronimoDeploymentManager mgr = (GeronimoDeploymentManager) dmgr;
            try {
                String repo;
                if (commandArgs.getArgs().length == 1) {
                    repo = commandArgs.getArgs()[0];
                } else {
                    repo = getRepository(consoleReader, mgr);
                }
                Map<String, List<PluginType>> categories = getPluginCategories(repo, mgr, consoleReader);
                if (categories == null) {
                    return;
                }

                PluginListType list = getInstallList(categories, consoleReader, repo);
                if (list == null) {
                    return;
                }

                installPlugins(mgr, list, consoleReader, connection);
            } catch (IOException e) {
                throw new DeploymentException("Unable to install configuration", e);
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid response");
            }
        } else {
            throw new DeploymentException("Cannot list repositories when connected to " + connection.getServerURI());
        }
    }

    public void installPlugins(GeronimoDeploymentManager mgr, PluginListType list, ConsoleReader consoleReader, ServerConnection connection) throws IOException, DeploymentException {
        long start = System.currentTimeMillis();
        Object key = mgr.startInstall(list, null, null);
        DownloadResults results = CommandInstallCAR.showProgress(mgr, key);
        int time = (int) (System.currentTimeMillis() - start) / 1000;
        consoleReader.printNewline();
        if (!results.isFailed()) {
            consoleReader.printString(DeployUtils.reformat("**** Installation Complete!", 4, 72));
            consoleReader.printNewline();

            for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                Artifact uri = results.getDependenciesPresent()[i];
                consoleReader.printString(DeployUtils.reformat("Used existing: " + uri, 4, 72));
                consoleReader.printNewline();

            }
            for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                Artifact uri = results.getDependenciesInstalled()[i];
                consoleReader.printString(DeployUtils.reformat("Installed new: " + uri, 4, 72));
                consoleReader.printNewline();

            }
            consoleReader.printNewline();
            consoleReader.printString(DeployUtils.reformat(
                    "Downloaded " + (results.getTotalDownloadBytes() / 1024) + " kB in " + time + "s (" + results.getTotalDownloadBytes() / (1024 * time) + " kB/s)",
                    4, 72));
            consoleReader.printNewline();

        }
        if (results.isFinished() && !results.isFailed()) {
            for (PluginType plugin : list.getPlugin()) {
                for (PluginArtifactType targetInstance : plugin.getPluginArtifact()) {
                    consoleReader.printString(DeployUtils.reformat("Now starting " + PluginInstallerGBean.toArtifact(targetInstance.getModuleId()) + "...", 4, 72));
                    consoleReader.flushConsole();
                    new CommandStart().execute(consoleReader, connection,
                            new BaseCommandArgs(new String[]{PluginInstallerGBean.toArtifact(targetInstance.getModuleId()).toString()}));
                }
            }
        }
    }

    public PluginListType getInstallList(Map<String, List<PluginType>> categories, ConsoleReader consoleReader, String repo) throws IOException {
        List<PluginType> available = new ArrayList<PluginType>();
        for (Map.Entry<String, List<PluginType>> entry : categories.entrySet()) {
            String category = entry.getKey();
            if (category == null) {
                category = "<no category>";
            }
            List<PluginType> items = entry.getValue();
            consoleReader.printNewline();
            consoleReader.printString(DeployUtils.reformat(category, 4, 72));
            for (PluginType metadata : items) {
                consoleReader.printString("  " + metadata.getName());
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
                    consoleReader.printString(DeployUtils.reformat(
                            prefix + " (" + instance.getModuleId().getVersion() + ")", 8, 72));
                    consoleReader.printNewline();
                }
            }
        }
        if (available.size() == 0) {
            consoleReader.printNewline();
            consoleReader.printString("No plugins from this site are eligible for installation.");
            consoleReader.printNewline();
            return null;
        }
        consoleReader.printNewline();
//                consoleReader.print("Install Service [enter number or 'q' to quit]: ");
        consoleReader.flushConsole();
        String answer = consoleReader.readLine("Install Services [enter a comma separated list of numbers or 'q' to quit]: ").trim();
        if (answer.equalsIgnoreCase("q")) {
            return null;
        }
        PluginListType list = new PluginListType();
        for (String instance : answer.split(",")) {
            int selection = Integer.parseInt(instance.trim());
            PluginType target = available.get(selection - 1);
            list.getPlugin().add(target);
        }
        list.getDefaultRepository().add(repo);
        return list;
    }

    public Map<String, List<PluginType>> getPluginCategories(String repo, GeronimoDeploymentManager mgr, ConsoleReader consoleReader) throws DeploymentException, IOException {
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
            consoleReader.printNewline();
            consoleReader.printString("No plugins were returned from this site.");
            consoleReader.printNewline();
            consoleReader.flushConsole();
            return null;
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
        return categories;
    }

    public String getRepository(ConsoleReader consoleReader, GeronimoDeploymentManager mgr) throws IOException, DeploymentException {
        URL[] all = mgr.getRepositories();
        if (all.length == 0) {
            throw new DeploymentException("No default repositories available.  Please either specify the repository " +
                    "URL on the command line, or go into the console Plugin page and update the list of available " +
                    "repositories.");
        }
        consoleReader.printNewline();
        consoleReader.printString("Select repository:");
        consoleReader.printNewline();
        for (int i = 0; i < all.length; i++) {
            URL url = all[i];
            consoleReader.printString("  " + (i + 1) + ". " + url);
            consoleReader.printNewline();
        }
        consoleReader.printNewline();
//        consoleReader.print("Enter Repository Number: ");
        consoleReader.flushConsole();
        String entry = consoleReader.readLine("Enter Repository Number: ").trim();
        int index = Integer.parseInt(entry);
        return all[index - 1].toString();
    }
}
