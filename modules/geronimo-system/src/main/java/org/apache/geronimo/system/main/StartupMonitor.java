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
package org.apache.geronimo.system.main;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * An interface used by the Daemon to convey the status of the server
 * startup.
 *
 * @version $Revision: 1.0$
 */
public interface StartupMonitor {
    // Normal calls, will generally occur in this order
    void systemStarting(long startTime);
    void systemStarted(Kernel kernel);
    void foundModules(Artifact[] modules);
    void moduleLoading(Artifact module);
    void moduleLoaded(Artifact module);
    void moduleStarting(Artifact module);
    void moduleStarted(Artifact module);
    void startupFinished();

    // Indicate failures during load
    void serverStartFailed(Exception problem);
}
