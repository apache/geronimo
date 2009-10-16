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

package org.apache.geronimo.common;

import java.net.URL;
import java.io.IOException;


/**
 * Used to perform common initialization tasks for Geronimo server, deploy, and client environments.
 * @version $Rev$ $Date$
 */
public class GeronimoEnvironment {

    /**
     * Performs common initialization for the various Geronimo process environments: server, deploy, and client.
     */
    public static void init() {
        // Setting useCaches to false avoids a memory leak of URLJarFile instances
        // It's a workaround for a Sun bug (see bug id 4167874). Otherwise, 
        // URLJarFiles will never be garbage collected. o.a.g.deployment.util.DeploymentUtil.readAll() 
        // causes URLJarFiles to be created
        try {
            // Protocol/file shouldn't matter. 
            // As long as we don't get an input/output stream, no operations should occur...
            new URL("http://a").openConnection().setDefaultUseCaches(false);
        }
        catch (IOException ioe) {
            // Can't Log this. Should we send to STDOUT/STDERR?
        }
    }

}
