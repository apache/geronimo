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

import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class SilentStartupMonitor implements StartupMonitor {
    private static final Logger log = LoggerFactory.getLogger(SilentStartupMonitor.class);

    private Kernel kernel;

    public void systemStarting(long startTime) {
    }

    public void systemStarted(Kernel kernel) {
        this.kernel = kernel;
    }

    public void foundModules(Artifact[] modules) {
    }

    public void moduleLoading(Artifact module) {
    }

    public void moduleLoaded(Artifact module) {
    }

    public void moduleStarting(Artifact module) {
    }

    public void moduleStarted(Artifact module) {
    }

    public void startupFinished() {
        try {
            Set gbeans = kernel.listGBeans((AbstractNameQuery)null);
            for (Iterator it = gbeans.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next();
                int state = kernel.getGBeanState(name);
                if (state != State.RUNNING_INDEX) {
                    log.warn("Unable to start {} ({})", name, State.fromInt(state).getName());
                }
            }
        } catch (GBeanNotFoundException ignore) {}
        
        System.out.println("Geronimo startup complete");
    }

    public void serverStartFailed(Exception problem) {
        System.out.println("Geronimo startup failed:");
        problem.printStackTrace(System.out);
    }

}
