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

import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.cli.CommandListConfigurations;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.model.PluginListType;


/**
 * @version $Rev$ $Date$
 */

@Command(scope = "deploy", name = "assemble-server", description = "Extract a geronimo server from the current one")
public class AssembleServerCommand extends ConnectCommand {
    @Option (name = "-l", aliases = {"--list"}, description = "refresh plugin list")
    boolean refreshList = false;

    @Option (name = "-m", aliases = {"--mode"}, description = "custom assembly mode")
    String mode;

    @Option (name = "-t", aliases = {"--path"}, description = "assembly location")
    String relativeServerPath = "var/temp/assembly";

    @Option (name = "-g", aliases = {"--groupId"}, description = "server groupId")
    String group;

    @Option (name = "-a", aliases = {"--artifact"}, description = "server artifact name")
    String artifact;

    @Option (name = "-v", aliases = {"--version"}, description = "server version")
    String version = "1.0";

    @Option (name = "-f", aliases = {"--format"}, description = "zip or tar.gz")
    String format = "tar.gz";

    @Argument (multiValued = true)
    List<String> pluginArtifacts;

    /**
     * Override of the post-connection execute method.
     *
     * @param connection The current session connection.
     *
     * @return The return value from the command.
     * @exception Exception
     */
    protected Object doExecute(ServerConnection connection) throws Exception {
        CommandListConfigurations command = new CommandListConfigurations();

        if (mode == null) {
            println("Available custom assembly modes:");
            println(" 1:    Function Centric");
            println(" 2:    Application Centric");
            println(" 3:    Expert Users");

            mode = readLine("Please select a custom assembly mode [1,2,3]").trim();
            if (mode == null || (mode.compareTo("1") != 0 && mode.compareTo("2") != 0 && mode.compareTo("3") != 0)) {
                throw new IllegalArgumentException("Please enter a valid Assembly server mode");
            }
        }

        if (group == null) {
            group = readLine("Assembly server group name: ").trim();
            if (group.equals("")) {
                throw new IllegalArgumentException("Assembly server group name is required");
            }
        }

        if (artifact == null) {
            artifact = readLine("Assembly server artifact name: ").trim();
            if (artifact.equals("")) {
                throw new IllegalArgumentException("Assembly server artifact name is required");
            }
        }

        PluginListType plugins = (PluginListType)session.get("LocalPlugins");

        if (refreshList || plugins == null) {
            plugins = command.getLocalPluginCategories((GeronimoDeploymentManager)connection.getDeploymentManager(), this);
            session.put("LocalPlugins", plugins);
        }

        if (pluginArtifacts != null) {
            command.assembleServer((GeronimoDeploymentManager)connection.getDeploymentManager(), pluginArtifacts, plugins, "repository", relativeServerPath, this);
            ((GeronimoDeploymentManager)connection.getDeploymentManager()).archive(relativeServerPath, "var/temp", new Artifact(group, artifact, version, format));
        } else {
            PluginListType pluginsToInstall;

            if (mode.compareTo("1") == 0) {
                println("Listing plugin groups and application plugins from the local Geronimo server");
                PluginListType pluginGroups = (PluginListType)session.get("LocalPluginGroups");
                PluginListType appPlugins = (PluginListType)session.get("LocalAppPlugins");

                if (refreshList || pluginGroups == null) {
                    pluginGroups = command.getLocalPluginGroups((GeronimoDeploymentManager)connection.getDeploymentManager(), this);
                    session.put("LocalPluginGroups", pluginGroups);
                }

                if (refreshList || appPlugins == null) {
                    appPlugins = command.getLocalApplicationPlugins((GeronimoDeploymentManager)connection.getDeploymentManager(), this);
                    session.put("LocalAppPlugins", appPlugins);
                }
                pluginsToInstall = command.getInstallList(pluginGroups, appPlugins, this, null);

            } else if (mode.compareTo("2") == 0) {
                println("Listing application plugins and required framework plugin group from the local Geronimo server");
                PluginListType appPlugins = (PluginListType)session.get("LocalAppPlugins");

                if (refreshList || appPlugins == null) {
                    appPlugins = command.getLocalApplicationPlugins((GeronimoDeploymentManager)connection.getDeploymentManager(), this);
                    session.put("LocalAppPlugins", appPlugins);
                }
                pluginsToInstall = command.getInstallList(appPlugins, this, null);
            } else {
                println("Listing plugins from the local Geronimo server");
                pluginsToInstall = command.getInstallList(plugins, this, null);
            }

            if (pluginsToInstall != null) {
                command.assembleServer((GeronimoDeploymentManager) connection.getDeploymentManager(), pluginsToInstall, "repository", relativeServerPath, this);
                ((GeronimoDeploymentManager) connection.getDeploymentManager()).archive(relativeServerPath, "var/temp", new Artifact(group, artifact, version, format));
            }
        }
        return null;
    }
}
