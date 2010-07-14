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

package org.apache.geronimo.deployment.cli;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager;
import org.apache.geronimo.kernel.Kernel;

/**
 * Base class for CLI deployer commands.  Tracks some simple properties and
 * has common utility methods.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractCommand implements DeployCommand {

    public AbstractCommand() {
    }

    public boolean isLocalOnly() {
        return false;
    }

    protected boolean isOffline(ServerConnection connection) {
        if (connection instanceof OfflineServerConnection) {
            return ((OfflineServerConnection) connection).isOfflineDeployerStarted();
        } else {
            return false;
        }
    }
    
    protected Kernel getKernel(ServerConnection connection) {
        Kernel kernel = null;
        DeploymentManager dm = connection.getDeploymentManager();
        if (dm instanceof JMXDeploymentManager) {
            kernel = ((JMXDeploymentManager) dm).getKernel();
        }
        if (kernel == null) {
            throw new NullPointerException("Could not get kernel instance");
        }
        return kernel;
    }
    
    protected void emit(ConsoleReader out, String message) throws IOException {
        out.printString(DeployUtils.reformat(message, 4, 72));
        out.flushConsole();
    }

    /**
     * Busy-waits until the provided <code>ProgressObject</code>
     * indicates that it's no longer running.
     *
     * @param out a <code>PrintWriter</code> value, only used in case
     *            of an <code>InterruptedException</code> to output the stack
     *            trace.
     * @param po  a <code>ProgressObject</code> value
     */
    protected void waitForProgress(final ConsoleReader out, ProgressObject po) {
        po.addProgressListener(new ProgressListener() {
            String last = null;

            public void handleProgressEvent(ProgressEvent event) {
                String msg = event.getDeploymentStatus().getMessage();
                if (last != null && !last.equals(msg)) {
                    try {
                        emit(out, last);
                    } catch (IOException e1) {
                        //ignore
                    }
                }
                last = msg;
            }
        });
        while (po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                try {
                    out.println(e.getMessage());
                } catch (IOException e1) {
                    //ignore
                }
            }
        }
        return;
    }

    protected static boolean isMultipleTargets(TargetModuleID[] ids) {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < ids.length; i++) {
            TargetModuleID id = ids[i];
            set.add(id.getTarget().getName());
        }
        return set.size() > 1;
    }

    protected static Target[] identifyTargets(List<String> targetNames, final DeploymentManager mgr) throws DeploymentException {
        //TODO consider if nicknames that match multiple servers should be allowed.  Also if regexps should be used in matching
        Target[] tlist = new Target[targetNames.size()];
        Target[] all = mgr.getTargets();
        Set<String> found = new HashSet<String>();
        for (int i = 0; i < tlist.length; i++) {
            for (int j = 0; j < all.length; j++) {
                Target server = all[j];
                // check for exact target name match
                if (server.getName().equals(targetNames.get(i))
                        // check for "target-nickname" match (they match if
                        // the full target name contains the user-provided
                        // nickname)
                        || server.getName().indexOf(targetNames.get(i).toString()) > -1) {
                    tlist[i] = server;
                    if (found.contains(server.getName())) {
                        throw new DeploymentException("Target list should not contain duplicates or nicknames that match duplicates (" + targetNames.get(i) + ")");
                    }
                    found.add(server.getName());
                    break;
                }
            }
            if (tlist[i] == null) {
                throw new DeploymentException("No target named or matching '" + targetNames.get(i) + "' was found");
            }
        }
        return tlist;
    }
}
