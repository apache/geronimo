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
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.enterprise.deploy.tool.EjbDeployableObject;

/**
 * Loads the deployment descriptor information from the specific EJB JAR
 * file.
 *
 * @version $Rev$ $Date$
 */
public class InitializeEJBJAR extends TextController {
    private static final Log log = LogFactory.getLog(InitializeEJBJAR.class);

    public InitializeEJBJAR(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        if(!(context.moduleInfo instanceof EJBJARInfo)) {
            throw new IllegalStateException("Tried to load an EJB JAR but the current module is "+context.moduleInfo.getClass().getName());
        }
        EJBJARInfo jarInfo = (EJBJARInfo)context.moduleInfo;
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{jarInfo.file.toURL()}, ClassLoader.getSystemClassLoader());
            context.standardModule = new EjbDeployableObject(jarInfo.jarFile, loader);
        } catch(MalformedURLException e) {
            context.out.println("ERROR: "+jarInfo.file+" is not a valid JAR file!");
            context.moduleInfo = null;
            return;
        }
        try {
            context.serverModule = context.deployer.createConfiguration(context.standardModule);
        } catch(InvalidModuleException e) {
            context.out.println("ERROR: Unable to initialize a Geronimo DD for EJB JAR "+jarInfo.file);
            context.moduleInfo = null;
            return;
        }
        jarInfo.ejbJar = context.standardModule.getDDBeanRoot();
        jarInfo.editingEjbJar = true;
        try {
            jarInfo.ejbJarConfig = context.serverModule.getDConfigBeanRoot(jarInfo.ejbJar);
            initializeDConfigBean(jarInfo.ejbJarConfig);
        } catch(ConfigurationException e) {
            log.error("Unable to initialize server-specific deployment information", e);
            context.moduleInfo = null;
            return;
        }
    }
}
