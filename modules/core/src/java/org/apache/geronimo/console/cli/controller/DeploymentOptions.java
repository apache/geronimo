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

package org.apache.geronimo.console.cli.controller;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * The screen that lets you distribute, deploy, or redeploy the current module.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:41 $
 */
public class DeploymentOptions extends TextController {
    private static final Log log = LogFactory.getLog(DeploymentOptions.class);

    public DeploymentOptions(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            newScreen("Deploy Module");
            if(!context.connected) {
                println("ERROR: cannot deploy in disconnected mode.");
                return;
            }
            context.out.println((context.targets.length == 0 ? "No" : String.valueOf(context.targets.length))+" target"+(context.targets.length != 1 ? "s" : "")+" currently selected.");
            context.out.println("  1) Select targets (usually servers or clusters) to work with");
            context.out.println("  "+(context.targets.length > 0 ? "2)" : "--")+" Distribute "+context.moduleInfo.file.getName()+" to selected targets");
            context.out.println("  "+(context.targets.length > 0 ? "3)" : "--")+" Deploy "+context.moduleInfo.file.getName()+" to selected targets");
            context.out.println("  "+(context.targets.length > 0 && context.deployer.isRedeploySupported() ? "4)" : "--")+" Redeploy "+context.moduleInfo.file.getName()+" to selected targets");
            String choice;
            while(true) {
                context.out.print("Action ([1"+(context.targets.length > 0 ? "-4" : "")+"] or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                try {
                    if(choice.equals("1")) {
                        new SelectServer(context).execute();
                        break;
                    } else if(choice.equals("2")) {
                        distribute();
                        break;
                    } else if(choice.equals("3")) {
                        ProgressObject po = distribute();
                        if(po != null) {
                            TargetModuleID[] ids = po.getResultTargetModuleIDs();
                            println("Successfully distributed "+ids.length+" modules."+(ids.length > 0 ? "Now starting them..." : ""));
                            if(ids.length > 0) {
                                po = context.deployer.start(ids);
                                if(po != null) {
                                    new ProgressMonitor(context, po).execute();
                                }
                            }
                        } else {
                            println("ERROR: Modules have been distributed but must be manually started.");
                        }
                        break;
                    } else if(choice.equals("4")) {
                        new SelectDistributedModules(context, new SelectDistributedModules.NonRunningModules("redeploy")).execute();
                        if(context.modules.length > 0) {
                            println("Prepared to update "+context.modules.length+" deployments with new "+context.moduleInfo.file.getName());
                            if(confirmModuleAction("Redeploy")) {
                                ProgressObject po = context.deployer.redeploy(context.modules, context.moduleInfo.file, spool());
                                if(po != null) {
                                    new ProgressMonitor(context, po).execute();
                                }
                            }
                        }
                        break;
                    } else if(choice.equals("b")) {
                        return;
                    }
                } catch(ConfigurationException e) {
                    log.error("Server action failed", e);
                } catch(IOException e) {
                    log.error("Server action failed", e);
                }
            }
        }
    }

    private ProgressObject distribute() throws ConfigurationException, IOException {
        File dd = spool();
        ProgressObject po = context.deployer.distribute(context.targets, context.moduleInfo.file, dd);
        if(po != null) {
            new ProgressMonitor(context, po).execute();
        }
        return po;
    }

    private File spool() throws IOException, ConfigurationException {
        File temp = File.createTempFile("gerdd", "xml");
        temp.deleteOnExit();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        context.serverModule.save(out);
        out.flush();
        out.close();
        return temp;
    }
}
