/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.TextController;

/**
 * Distribute a module to the current deployment manager.
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/16 04:39:41 $
 */
public class DistributeModule
    extends TextController
{

    private static final Log log = LogFactory.getLog(DistributeModule.class);

    public DistributeModule(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        context.out.println("\nCurrent directory is "+context.saveDir);
        context.out.println("Select a plan or module to distribute");
        String choice;
        File file;
        while(true) {
            context.out.print("File Name: ");
            context.out.flush();
            try {
                choice = context.in.readLine().trim();
            } catch(IOException e) {
                log.error("Unable to read user input", e);
                return;
            }
            file = new File(context.saveDir, choice);
            if( !file.canRead() || file.isDirectory() ) {
                context.out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            context.saveDir = file.getParentFile();
            break;
        }

        println("Starting the distribute operation...");
        context.deployer.distribute(context.targets, file, null);
        println("Distributed.");
    }

}
