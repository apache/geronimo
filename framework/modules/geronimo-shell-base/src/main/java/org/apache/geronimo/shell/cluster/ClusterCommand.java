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

package org.apache.geronimo.shell.cluster;

import java.util.Map;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.geronimo.deployment.cli.CommandInstallCAR;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.shell.deploy.ConnectCommand;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.Farm;

/**
 * @version $Rev$ $Date$
 */
@Command(scope = "geronimo", name = "cluster", description = "administer cluster")
public class ClusterCommand extends ConnectCommand {
    
    @Option(name="-c", aliases={"--cluster"}, description="Cluster to perform action on")
    String clusterName;

    @Option(name="-l", aliases={"--pluginlist"}, description="Plugin List to perform action on")
    String pluginList;

    @Option(name="-a", aliases={"--pluginartifact"}, description="Plugin Artifact to perform action on")
    String plugin;

    @Argument(name="ACTION", required=true, index=0, description="Action (add/remove) to perform")
    String action;

    /**
     * Override of the post-connection execute method.
     *
     * @param connection The current session connection.
     *
     * @return The return value from the command.
     * @exception Exception
     */
    protected Object doExecute(ServerConnection connection) throws Exception {

        DeploymentManager deploymentManager = connection.getDeploymentManager();
        Farm farm = (Farm)((GeronimoDeploymentManager)deploymentManager).getImplementation(Farm.class);
        Map<String, DownloadResults> results;
        if (action == "add") {
            if (clusterName != null && pluginList != null && plugin != null) {
                results = farm.addPluginToCluster(clusterName, pluginList, plugin);
            } else if (clusterName != null && pluginList != null) {
                results = farm.addPluginList(clusterName, pluginList);
            } else if (pluginList != null && plugin != null) {
                results = farm.addPlugin(pluginList, plugin);
            } else {
                println("add requires -c <cluster> and -l <plugin list name>, -l <plugin list name> and -a <plugin>, or all three");
                return null;
            }
        } else if (action == "remove") {
            if (clusterName != null && pluginList != null && plugin != null) {
                println("remove requires -c <cluster> and -l <plugin list name>, or -l <plugin list name> and -a <plugin>, but not all three");
                return null;
            } else if (clusterName != null && pluginList != null) {
                results = farm.removePluginListFromCluster(clusterName, pluginList);
            } else if (pluginList != null && plugin != null) {
                results = farm.removePluginFromPluginList(pluginList, plugin);
            } else {
                println("remove requires -c <cluster> and -l <plugin list name>, or -l <plugin list> and -a <plugin>");
                return null;
            }
        } else {
            println("unknown command, expecting add or remove");
            return null;
        }
        println("Results:");
        for (Map.Entry<String, DownloadResults> entry : results.entrySet()) {
            println("results for node: " + entry.getKey());
            CommandInstallCAR.printResults(this, entry.getValue(), 0);
        }
        return null;
    }
}
