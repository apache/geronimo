/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import org.apache.geronimo.common.DeploymentException;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The CLI deployer logic to list modules.
 *
 * @version $Rev$ $Date$
 */
public class CommandListModules extends AbstractCommand {
    public CommandListModules() {
        super("list-modules", "2. Other Commands", "[--all|--started|--stopped] [target*]",
                "Lists the modules available on the specified targets.  If " +
                "--started or --stopped is specified, only started or stopped modules will " +
                "be listed; otherwise all modules will be listed.  If no targets are " +
                "specified, then modules on all targets will be listed; otherwise only " +
                "modules on the specified targets.");
    }

    public void execute(PrintWriter out, ServerConnection connection, String[] args) throws DeploymentException {
        List targets = new ArrayList();
        Boolean started = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.startsWith("--")) {
                if(arg.equals("--all")) {
                    if(started != null) {
                        throw new DeploymentSyntaxException("Cannot specify more than one of --all, --started, --stopped");
                    }
                } else if(arg.equals("--started")) {
                    if(started != null) {
                        throw new DeploymentSyntaxException("Cannot specify more than one of --all, --started, --stopped");
                    }
                    started = Boolean.TRUE;
                } else if(arg.equals("--stopped")) {
                    if(started != null) {
                        throw new DeploymentSyntaxException("Cannot specify more than one of --all, --started, --stopped");
                    }
                    started = Boolean.FALSE;
                } else {
                    throw new DeploymentSyntaxException("Unrecognized option '"+arg+"'");
                }
            } else {
                targets.add(arg);
            }
        }
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
            throw new DeploymentSyntaxException(e.getMessage());
        }
        if(running == null) {
            running = new TargetModuleID[0];
        }
        if(notrunning == null) {
            notrunning = new TargetModuleID[0];
        }

        int total = running.length+notrunning.length;
        out.println("Found "+total+" module"+(total != 1 ? "s" : ""));
        for (int i = 0; i < running.length; i++) {
            TargetModuleID result = running[i];
            out.println("  + "+result.getModuleID()+(tlist.length > 1 ? " on "+result.getTarget().getName(): "")+(result.getWebURL() == null ? "" : " @ "+result.getWebURL()));
            if(result.getChildTargetModuleID() != null) {
                for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                    TargetModuleID child = result.getChildTargetModuleID()[j];
                    out.println("      `-> "+child.getModuleID()+(child.getWebURL() == null ? "" : " @ "+child.getWebURL()));
                }
            }
        }
        for (int i = 0; i < notrunning.length; i++) {
            TargetModuleID result = notrunning[i];
            out.println("    "+result.getModuleID()+(tlist.length > 1 ? " on "+result.getTarget().getName(): ""));
            if(result.getChildTargetModuleID() != null) {
                for (int j = 0; j < result.getChildTargetModuleID().length; j++) {
                    TargetModuleID child = result.getChildTargetModuleID()[j];
                    out.println("      `-> "+child.getModuleID());
                }
            }
        }
    }
}
