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

import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;

import jline.ConsoleReader;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.jmx.RemoteDeploymentManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchOperationException;

/**
 * Utility to enable/disable stats query on Default Server
 * @version $Rev$ $Date$
 */
public class CommandStatsQuery extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        String args[] = commandArgs.getArgs();
        if (args.length == 0) {
            throw new DeploymentException("Specify either to 'enable' or 'disable' stats query on Default Server.");
        }
        DeploymentManager dm = connection.getDeploymentManager();
        Kernel kernel = null;
        if (dm instanceof RemoteDeploymentManager) {
            kernel = ((RemoteDeploymentManager) dm).getKernel();
        }
        AbstractNameQuery nameQuery = new AbstractNameQuery("org.apache.geronimo.monitoring.MasterRemoteControlJMX");
        Set<AbstractName> it = kernel.listGBeans(nameQuery);
        AbstractName gbeanName = (AbstractName) it.iterator().next();
        Integer running = 0;
        try {
            running = (Integer) kernel.invoke(gbeanName, "SnapshotStatus");
            if("disable".equals(args[0])) {
                if(running == 1) {
                    Boolean status = (Boolean) kernel.invoke(gbeanName, "stopSnapshot");
                    consoleReader.printString(DeployUtils.reformat(status ? "Stats query snapshot is now disabled." : "Disabling Stats query snapashot failed.", 4, 72));
                } else {
                    consoleReader.printString(DeployUtils.reformat("Stats query snapshot is not running.", 4, 72));
                }
            } else if("enable".equals(args[0])) {
                if(args.length == 1) {
                    consoleReader.printString(DeployUtils.reformat("Please specify snapshotDuration in minutes.", 4, 72));
                    return;
                }
                Long snapshotDuration = Integer.parseInt(args[1]) * 60000L;
                Long prevSnapshotDuration;
                if(running == 1) {
                    prevSnapshotDuration = (Long) kernel.invoke(gbeanName, "getSnapshotDuration");
                    if(prevSnapshotDuration.equals(snapshotDuration)) {
                        consoleReader.printString(DeployUtils.reformat("Stats query snapshot is already running with duration of " + snapshotDuration/60000 + " min.", 4, 72));
                    } else {
                        kernel.invoke(gbeanName, "setSnapshotDuration", new Object[] {snapshotDuration}, new String[]{Long.class.getName()});
                        consoleReader.printString(DeployUtils.reformat("Stats query snapshot was already running with duration of " + prevSnapshotDuration/60000 + " min. Now it is changed to "+ snapshotDuration/60000 + " min.", 4, 72)); 
                    }
                } else {
                    Boolean status = (Boolean) kernel.invoke(gbeanName, "startSnapshot", new Object[] {snapshotDuration}, new String[]{Long.class.getName()});
                    consoleReader.printString(DeployUtils.reformat(status ? "Stats query snapshot started successfully with snapshot duration of " + snapshotDuration/60000 + " min.": "Enabling Stats query snapshot failed.", 4, 72));
                }
            }
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Unable to find the gbean associated with monitoring.", e);
        } catch (NoSuchOperationException e) {
            throw new DeploymentException("Operation does not exist", e);
        } catch (InternalKernelException e) {
            throw new DeploymentException(e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
}
