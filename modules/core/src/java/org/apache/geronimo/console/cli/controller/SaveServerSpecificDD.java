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
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Saves the server-specific deployment information to a file on disk.
 * Note that in JSR-88, server-specific DDs are not saved in the
 * JAR/EAR/whatever.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:26 $
 */
public class SaveServerSpecificDD extends TextController {
    private static final Log log = LogFactory.getLog(SaveServerSpecificDD.class);

    public SaveServerSpecificDD(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        context.out.println("\nCurrent directory is "+context.saveDir);
        context.out.println("Select a file name.  The server-specific deployment information for the ");
        context.out.println(context.moduleInfo.getFileName()+" will be saved to the file you specify.");
        String choice;
        try {
            while(true) {
                context.out.print("File Name: ");
                context.out.flush();
                choice = context.in.readLine().trim();
                File file = new File(context.saveDir, choice);
                if((file.exists() && !file.canWrite()) || (!file.exists() && !file.getParentFile().canWrite()) || file.isDirectory()) {
                    context.out.println("ERROR: cannot write to this file.  Please try again.");
                    continue;
                }
                if(file.exists()) {
                    context.out.print("File already exists.  Overwrite (Y/N)? ");
                    context.out.flush();
                    choice = context.in.readLine().trim().toLowerCase();
                    if(choice.equals("n")) { // todo: make sure they entered y or n
                        continue;
                    }
                }
                context.saveDir = file.getParentFile();
                try {
                    context.moduleInfo.saveDConfigBean(file);
                    context.out.println("Deployment information saved to "+file.getName());
                    return;
                } catch(IOException e) {
                    log.error("Unable to write to file", e);
                    return;
                } catch(ConfigurationException e) {
                    context.out.println("ERROR: "+e.getMessage());
                    return;
                }
            }
        } catch(IOException e) {
            log.error("Unable to read user input", e);
            return;
        }
   }
}
