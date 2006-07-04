/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.Kernel;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * In vm server stop.  May not have been tested.

 * @goal stopServer
 *
 * @version $Rev:$ $Date:$
 *
 * */
public class StopServerMojo extends AbstractMojo {

    private String kernelName;

    public void execute() throws MojoExecutionException {
        Kernel kernel = KernelRegistry.getKernel(this.kernelName);
        kernel.shutdown();
    }
}
