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

import java.io.IOException;
import java.io.File;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Loads the server-specific deployment information from a file on disk.
 * Note that in JSR-88, server-specific DDs are not saved in the
 * JAR/EAR/whatever.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:26 $
 */
public class LoadServerSpecificDD extends TextController {
    private static final Log log = LogFactory.getLog(LoadServerSpecificDD.class);

    public LoadServerSpecificDD(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        context.out.println("\nCurrent directory is "+context.saveDir);
        context.out.println("Select a file name.  The server-specific deployment information for the ");
        context.out.println(context.moduleInfo.getFileName()+" will be loaded from the file you specify.");
        String choice;
        while(true) {
            context.out.print("File Name: ");
            context.out.flush();
            try {
                choice = context.in.readLine().trim();
            } catch(IOException e) {
                log.error("Unable to read user input", e);
                return;
            }
            File file = new File(context.saveDir, choice);
            if(!file.canRead() || file.isDirectory()) {
                context.out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            context.saveDir = file.getParentFile();
            try {
                context.moduleInfo.loadDConfigBean(file);
                context.out.println("Deployment information loaded from "+file.getName());
                return;
            } catch(IOException e) {
                log.error("Unable to read from file", e);
                return;
            } catch(ConfigurationException e) {
                context.out.println("ERROR: "+e.getMessage());
                if(e.getCause() != null) {
                    e.printStackTrace(context.out);
                }
                return;
            }
        }
    }
}
