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

package org.apache.geronimo.console.cli;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.model.DeployableObject;

/**
 * The information required in order to perform deployment operations.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:41 $
 */
public class DeploymentContext {
    public DeploymentManager deployer;
    public DeployableObject standardModule;
    public DeploymentConfiguration serverModule;
    public PrintWriter out;
    public BufferedReader in;
    public ModuleInfo moduleInfo;
    public File saveDir = new File(System.getProperty("user.dir"));
    public boolean connected = false;
    public Target[] targets = new Target[0];
    public TargetModuleID[] modules = new TargetModuleID[0];
}
