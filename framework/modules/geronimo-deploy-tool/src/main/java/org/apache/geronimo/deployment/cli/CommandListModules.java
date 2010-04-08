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
import java.util.Arrays;
import java.util.List;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.cli.deployer.ListModulesCommandArgs;
import org.apache.geronimo.common.DeploymentException;

/**
 * The CLI deployer logic to list modules.
 *
 * @version $Rev$ $Date$
 */
public class CommandListModules extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        if (!(commandArgs instanceof ListModulesCommandArgs)) {
            throw new DeploymentSyntaxException("CommandArgs has the type [" + commandArgs.getClass() + "]; expected [" + ListModulesCommandArgs.class + "]");
        }
        ListModulesCommandArgs listModulesCommandArgs = (ListModulesCommandArgs) commandArgs;

        Boolean started = null;
        if (listModulesCommandArgs.isStarted()) {
            started = Boolean.TRUE;
        } else if (listModulesCommandArgs.isStopped()) {
            started = Boolean.FALSE;
        }

        List targets = Arrays.asList(listModulesCommandArgs.getArgs());

        final DeploymentManager mgr = connection.getDeploymentManager();
        TargetModuleID[] running = null, notrunning = null;
        Target[] tlist = identifyTargets(targets, mgr);
        if(tlist.length == 0) {
            tlist = mgr.getTargets();
        }
        try {
            if(started == null || started.booleanValue()) {
                running = mgr.getRunningModules(null, tlist);
            }
            if(started == null || !started.booleanValue()) {
                notrunning = mgr.getNonRunningModules(null, tlist);
            }
        } catch (TargetException e) {
            throw new DeploymentException("Unable to query modules", e);
        } catch (IllegalStateException e) {
            throw new DeploymentSyntaxException(e.getMessage(), e);
        }
        if(running == null) {
            running = new TargetModuleID[0];
        }
        if(notrunning == null) {
            notrunning = new TargetModuleID[0];
        }

        // print the module count, and if there are more than one
        // targets print that count, too
        int total = running.length+notrunning.length;
        try {
            consoleReader.printString("Found "+total+" module"+(total != 1 ? "s" : ""));
            if ((tlist != null) && (tlist.length > 1)) {
                consoleReader.printString(" deployed to " + tlist.length + " target" + (tlist.length != 1 ? "s" : ""));
            }
            consoleReader.printNewline();

            // for each target, print the modules that were deployed to it
            for (int i = 0; (tlist != null) && (i < tlist.length); i++) {
                Target target = tlist[i];
                if (tlist.length > 1) {
                    consoleReader.printNewline();
                    consoleReader.printString(" Target " + target);
                    consoleReader.printNewline();
                }
                printTargetModules(consoleReader, target, running, "  + ");
                printTargetModules(consoleReader, target, notrunning, "    ");
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not print to console", e);
        }
    }


    /**
     * Prints the names of the modules (that belong to the target) on
     * the provided PrintWriter.
     *
     * @param out a <code>PrintWriter</code>
     * @param target a <code>Target</code> value; only the modules
     * whose target equals this one will be listed.  Must not be null.
     * @param modules a <code>TargetModuleID[]</code> value, must not
     * be null.
     * @param prefix a <code>String</code> value that will be
     * prepended to each module
     */
    void printTargetModules(ConsoleReader out, Target target, TargetModuleID[] modules, String prefix) throws IOException {
        for (int i = 0; i < modules.length; i++) {
            TargetModuleID result = modules[i];
            if (result.getTarget().equals(target)) {
                out.printString(prefix+result.getModuleID());
                out.printNewline();
                if(result.getChildTargetModuleID() != null) {
                    for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                        TargetModuleID child = result.getChildTargetModuleID()[j];
                        out.printString("      `-> "+child.getModuleID());
                        out.printNewline();
                    }
                }
            }
        }
    }
}
