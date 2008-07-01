/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment.plugin.remote;

import org.apache.geronimo.kernel.Kernel;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.shared.ModuleType;

import java.io.File;
import java.io.InputStream;

/**
 * A version of the distribute command that works on a server different
 * than the application server.
 *
 * @version $Rev$ $Date$
 */
public class DistributeCommand extends org.apache.geronimo.deployment.plugin.local.DistributeCommand {
    public DistributeCommand(Kernel kernel, Target[] targetList, File moduleArchive, File deploymentPlan) {
        super(kernel, targetList, moduleArchive, deploymentPlan);
    }

    public DistributeCommand(Kernel kernel, Target[] targetList, ModuleType moduleType, InputStream moduleStream, InputStream deploymentStream) {
        super(kernel, targetList, moduleType, moduleStream, deploymentStream);
    }

    protected void massageFileNames(File[] inputs) {
        RemoteDeployUtil.uploadFilesToServer(inputs, this);
    }
    
    public void run() {
        if (commandContext.isInPlace()) {
            fail("Remote in-place deployment is not supported");
        } else {
            super.run();
        }
    }
}
