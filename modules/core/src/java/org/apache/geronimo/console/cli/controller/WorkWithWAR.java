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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Main screen for operating on a web application WAR
 *
 * @version $Rev$ $Date$
 */
public class WorkWithWAR extends TextController {
    private static final Log log = LogFactory.getLog(WorkWithWAR.class);

    public WorkWithWAR(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        while(true) {
            context.out.println("\n\nLoaded a WAR.  Working with the "+context.moduleInfo.getFileName()+" deployment descriptor.");
            context.out.println("  UN Edit the standard Web App deployment descriptor ("+context.moduleInfo.getFileName()+")");
            context.out.println("  2) Edit the corresponding server-specific deployment information");
            context.out.println("  3) Load a saved set of server-specific deployment information");
            context.out.println("  4) Save the current set of server-specific deployment information");
            context.out.println("  5) Deploy or redeploy the WAR into the application server");
            context.out.println("  6) Select a new EJB JAR or WAR to work with"); //todo: adjust text when other modules are accepted
            context.out.println("  7) Manage existing deployments in the server");
            String choice;
            while(true) {
                context.out.print("Action ([2-7] or [B]ack): ");
                context.out.flush();
                try {
                    choice = context.in.readLine().trim().toLowerCase();
                } catch(IOException e) {
                    log.error("Unable to read user input", e);
                    return;
                }
                if(choice.equals("2")) {
                    new EditServerSpecificDD(context).execute();
                    break;
                } else if(choice.equals("3")) {
                    new LoadServerSpecificDD(context).execute();
                    break;
                } else if(choice.equals("4")) {
                    new SaveServerSpecificDD(context).execute();
                    break;
                } else if(choice.equals("5")) {
                    new DeploymentOptions(context).execute();
                    break;
                } else if(choice.equals("6")) {
                    new SelectModule(context).execute();
                    return;
                } else if(choice.equals("7")) { //todo: prompt to save if modifications were made
                    new ControlDeployments(context).execute();
                    break;
                } else if(choice.equals("b")) {
                    context.moduleInfo = null;
                    return;
                }
            }
        }
    }
}
