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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.console.cli.module.WARInfo;

/**
 * Selects a J2EE module to deploy/edit/etc.
 *
 * @version $Rev$ $Date$
 */
public class SelectModule extends TextController {
    private static final Log log = LogFactory.getLog(SelectModule.class);

    public SelectModule(DeploymentContext context) {
        super(context);
    }

    public void execute() { //todo: handle more than JAR/WAR
        context.out.println("\nCurrent directory is "+context.saveDir);
        context.out.println("Select an EJB JAR or WAR file to load.");
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
            if(!file.canRead() || file.isDirectory()) {
                context.out.println("ERROR: cannot read from this file.  Please try again.");
                continue;
            }
            context.saveDir = file.getParentFile();
            break;
        }

        if(file.getName().endsWith(".jar")) {
            context.moduleInfo = new EJBJARInfo(context);
        } else if(file.getName().endsWith(".war")) {
            context.moduleInfo = new WARInfo(context);
        } else {
            context.out.println("ERROR: Expecting file name to end in .jar or .war");
        }
        try {
            context.moduleInfo.file = file;
            context.moduleInfo.jarFile = new JarFile(file);
        } catch(IOException e) {
            context.out.println("ERROR: "+file+" is not a valid JAR file!");
            context.moduleInfo = null;
            return;
        }
        if(!context.moduleInfo.initialize()) {
            context.moduleInfo = null;
            return;
        }
    }
}
