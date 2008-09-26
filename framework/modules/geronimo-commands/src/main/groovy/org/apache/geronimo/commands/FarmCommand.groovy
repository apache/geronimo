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

package org.apache.geronimo.commands

import jline.ConsoleReader
import org.apache.geronimo.deployment.cli.CommandInstallCAR
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager
import org.apache.geronimo.gshell.clp.Argument
import org.apache.geronimo.gshell.clp.Option
import org.apache.geronimo.gshell.command.annotation.CommandComponent
import org.apache.geronimo.system.plugin.DownloadResults
import org.apache.geronimo.system.plugin.Farm

/**
 * List plugins.
 *
 * @version $Rev: 580864 $ $Date: 2007-09-30 23:47:39 -0700 (Sun, 30 Sep 2007) $
 */
@CommandComponent (id = 'geronimo-commands:farm', description = 'administer farm')
class FarmCommand
    extends ConnectCommand
{
    @Option(name='-f', aliases=['--farm'], description='Farm to perform action on')
    String farmName

    @Option(name='-l', aliases=['--pluginlist'], description='Plugin List to perform action on')
    String pluginList

    @Option(name='-a', aliases=['--pluginartifact'], description='Plugin Artifact to perform action on')
    String plugin

    @Argument(metaVar='ACTION', required=true, index=0, description='Action (add/remove) to perform')
    String action

    protected Object doExecute() throws Exception {
        def connection = connect()

        def ConsoleReader consoleReader = new ConsoleReader(io.inputStream, io.out)
        def repo = null
        def plugins = null

        def deploymentManager = connection.getDeploymentManager();
        def Farm farm = (Farm)((RemoteDeploymentManager)deploymentManager).getImplementation(Farm.class);
        def Map<String, DownloadResults> results;
        if (action == "add") {
            if (farmName && pluginList && plugin) {
                results = farm.addPluginToCluster(farmName, pluginList, plugin);
            } else if (farmName && pluginList) {
                results = farm.addPluginList(farmName, pluginList);
            } else if (pluginList && plugin) {
                results = farm.addPlugin(pluginList, plugin);
            } else {
                io.out.println("add requires -f farm and -l plugin list, -l plugin list and -a plugin, or all three")
                return;
            }
        } else if (action == "remove") {
            if (farmName && pluginList && plugin) {
                io.out.println("remove requires -f farm and -l plugin list, or -l plugin list and -a plugin, but not all three")
                return;
            } else if (farmName && pluginList) {
                results = farm.removePluginListFromCluster(farmName, pluginList);
            } else if (pluginList && plugin) {
                results = farm.removePluginFromPluginList(pluginList, plugin);
            } else {
                io.out.println("remove requires -f farm and -l plugin list, or -l plugin list and -a plugin")
                return;
            }
        } else {
            io.out.println("unknown command, expecting add or remove")
            return;
        }
        io.out.println("Results:")
        for (Map.Entry<String, DownloadResults> entry in results) {
            io.out.println("results for node: " + entry.getKey());
            CommandInstallCAR.printResults(consoleReader, entry.getValue(), 0);
        }
    }
}
