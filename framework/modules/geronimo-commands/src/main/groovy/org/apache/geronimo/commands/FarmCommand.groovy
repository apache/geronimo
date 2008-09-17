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
    @Argument
    String action

    @Argument (index = 1, multiValued = true)
    List<String> params

    protected Object doExecute() throws Exception {
        def connection = connect()

        def ConsoleReader consoleReader = new ConsoleReader(io.inputStream, io.out)
        def repo = null
        def plugins = null

        def deploymentManager = connection.getDeploymentManager();
        def Farm farm = ((RemoteDeploymentManager)deploymentManager).getImplementation(Farm.class);
        def Map<String, DownloadResults> results;
        if (action == "add-plugin-list") {
            if (params.size == 2) {
                results = farm.addPluginList(params[0], params[1]);
            } else {
                io.out.println("add-plugin-list requires 2 parameters, cluster name and plugin list name")
                return;
            }
        } else if (action == "add-plugin") {
            if (params.size == 2) {
                results = farm.addPlugin(params[0], params[1]);
            } else {
                io.out.println("add-plugin requires 2 parameters, plugin list name and the artifact")
                return;
            }
        } else if (action == "add-plugin-to-cluster") {
            if (params.size == 3) {
                results = farm.addPluginToCluster(params[0], params[1], params[2]);
            } else {
                io.out.println("add-plugin requires 3 parameters, cluster name, plugin list name and the artifact")
                return;
            }
        } else {
            io.out.println("unknown command, expecting add-plugin-list, add-plugin, or add-plugin-to-cluster")
            return;
        }
        io.out.println("Results:")
        for (Map.Entry<String, DownloadResults> entry in results) {
            io.out.println("results for node: " + entry.getKey());
            CommandInstallCAR.printResults(consoleReader, entry.getValue(), 0);
        }
    }
}
