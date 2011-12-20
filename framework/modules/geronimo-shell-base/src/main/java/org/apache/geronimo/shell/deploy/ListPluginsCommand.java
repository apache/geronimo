/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.shell.deploy;

import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.deployment.cli.CommandListConfigurations;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.system.plugin.model.PluginListType;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "list-plugins", description = "List plugins")
public class ListPluginsCommand extends ConnectCommand {

    @Option(name = "-rr", aliases = { "--refresh-repository" }, description = "refresh repository")
    boolean refreshRepo = false;

    @Option(name = "-rl", aliases = { "--refresh-list" }, description = "refresh plugin list")
    boolean refreshList = false;

    @Option(name = "-r", aliases = { "--respository" }, description = "Repository URL")
    String mavenRepoURL;

    @Argument(multiValued = true)
    List<String> pluginArtifacts;

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        CommandListConfigurations command = new CommandListConfigurations();

        String repo = null;
        PluginListType plugins = null;

        if (mavenRepoURL != null) {
            plugins = command.getPluginCategories(mavenRepoURL, (GeronimoDeploymentManager) connection
                    .getDeploymentManager(), this);
            repo = mavenRepoURL;
        } else {
            println("Listing configurations from Geronimo server");

            repo = (String) session.get("PluginRepository");
            if (refreshRepo || repo == null) {
                session.put("AvailablePlugins", null);
                repo = command.getRepository(this, (GeronimoDeploymentManager) connection
                        .getDeploymentManager());
                session.put("PluginRepository", repo);
            }

            plugins = (PluginListType) session.get("AvailablePlugins");
            if (refreshList || plugins == null) {
                plugins = command.getPluginCategories(repo, (GeronimoDeploymentManager) connection
                        .getDeploymentManager(), this);
                session.put("AvailablePlugins", plugins);
            }
        }

        if (plugins != null) {
            if (pluginArtifacts != null) {
                command.installPlugins((GeronimoDeploymentManager) connection.getDeploymentManager(), pluginArtifacts,
                        plugins, repo, this, connection);
            } else {
                PluginListType pluginsToInstall = command.getInstallList(plugins, this, repo);

                if (pluginsToInstall != null) {
                    command.installPlugins((GeronimoDeploymentManager) connection.getDeploymentManager(),
                            pluginsToInstall, repo, this, connection);
                }
            }
        }

        println("list ended");
        return null;
    }
}
