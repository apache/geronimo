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

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.WARInfo;
import org.apache.geronimo.enterprise.deploy.tool.WebDeployableObject;

/**
 * Loads the deployment descriptor information from the specific WAR
 * file.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:41 $
 */
public class InitializeWAR extends TextController {
    private static final Log log = LogFactory.getLog(InitializeWAR.class);

    public InitializeWAR(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        if(!(context.moduleInfo instanceof WARInfo)) {
            throw new IllegalStateException("Tried to load a WAR but the current module is "+context.moduleInfo.getClass().getName());
        }
        WARInfo warInfo = (WARInfo)context.moduleInfo;
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{warInfo.file.toURL()}, ClassLoader.getSystemClassLoader());
            context.standardModule = new WebDeployableObject(warInfo.jarFile, loader);
        } catch(MalformedURLException e) {
            context.out.println("ERROR: "+warInfo.file+" is not a valid JAR file!");
            context.moduleInfo = null;
            return;
        }
        try {
            context.serverModule = context.deployer.createConfiguration(context.standardModule);
        } catch(InvalidModuleException e) {
            context.out.println("ERROR: Unable to initialize a Geronimo DD for WAR "+warInfo.file);
            context.moduleInfo = null;
            return;
        }
        warInfo.war = context.standardModule.getDDBeanRoot();
        try {
            warInfo.warConfig = context.serverModule.getDConfigBeanRoot(warInfo.war);
            initializeDConfigBean(warInfo.warConfig);
        } catch(ConfigurationException e) {
            log.error("Unable to initialize server-specific deployment information", e);
            context.moduleInfo = null;
            return;
        }
        return;
    }
}
