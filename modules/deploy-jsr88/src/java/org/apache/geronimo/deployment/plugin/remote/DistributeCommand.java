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
package org.apache.geronimo.deployment.plugin.remote;

import org.apache.geronimo.kernel.Kernel;

import javax.enterprise.deploy.spi.Target;
import java.io.File;
import java.io.InputStream;

/**
 * A version of the distribute command that works on a server different
 * than the application server.
 *
 * @version $Rev: 190584 $ $Date: 2005-12-04 12:07:10 -0500 (Sun, 04 Dec 2005) $
 */
public class DistributeCommand extends org.apache.geronimo.deployment.plugin.local.DistributeCommand {
    public DistributeCommand(Kernel kernel, Target[] targetList, File moduleArchive, File deploymentPlan) {
        super(kernel, targetList, moduleArchive, deploymentPlan);
    }

    public DistributeCommand(Kernel kernel, Target[] targetList, InputStream moduleStream, InputStream deploymentStream) {
        super(kernel, targetList, moduleStream, deploymentStream);
    }

    protected void massageFileNames(File[] inputs) {
        RemoteDeployUtil.uploadFilesToServer(inputs, this);
    }
}
