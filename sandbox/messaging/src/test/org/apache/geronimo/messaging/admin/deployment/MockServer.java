/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.admin.deployment;

import java.io.InputStream;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.plugin.DeploymentServer;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/27 14:45:58 $
 */
public class MockServer implements DeploymentServer {

    private final int id;
    private TargetModuleID[] runningIDs;
    private TargetModuleID[] nonRunningIDs;
    private TargetModuleID[] availableIDs;
    private boolean distribute;
    private boolean start;
    private boolean stop;
    private boolean undeploy;
    private boolean redeploy;
    
    public MockServer(int anID) {
        id = anID;
    }
    
    public int getMockID() {
        return id;
    }
    
    public boolean isLocal() {
        return false;
    }

    public Target[] getTargets() throws IllegalStateException {
        return null;
    }

    public void setMockGetRunningModules(TargetModuleID[] anIDs) {
        runningIDs = anIDs;
    }
    
    public TargetModuleID[] getRunningModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return runningIDs;
    }

    public void setMockGetNonRunningModules(TargetModuleID[] anIDs) {
        nonRunningIDs = anIDs;
    }
    
    public TargetModuleID[] getNonRunningModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return nonRunningIDs;
    }

    public void setMockGetAvailableModules(TargetModuleID[] anIDs) {
        availableIDs = anIDs;
    }
    
    public TargetModuleID[] getAvailableModules(
        ModuleType moduleType,
        Target[] targetList)
        throws TargetException, IllegalStateException {
        return availableIDs;
    }

    public boolean getMockIsDistributed() {
        return distribute;
    }
    
    public ProgressObject distribute(
        Target[] targetList,
        ConfigurationBuilder builder,
        InputStream jis,
        XmlObject plan)
        throws IllegalStateException {
        distribute = true;
        return new MockProgressObject();
    }

    public boolean getMockIsStarted() {
        return start;
    }
    
    public ProgressObject start(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        start = true;
        return new MockProgressObject();
    }

    public boolean getMockIsStopped() {
        return stop;
    }
    
    public ProgressObject stop(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        stop = true;
        return new MockProgressObject();
    }
    
    public boolean getMockIsUndeploy() {
        return undeploy;
    }
    
    public ProgressObject undeploy(TargetModuleID[] moduleIDList)
        throws IllegalStateException {
        undeploy = true;
        return new MockProgressObject();
    }
    
    public boolean isRedeploySupported() {
        return false;
    }

    public boolean getMockIsRedeploy() {
        redeploy = true;
        return redeploy;
    }
    
    public ProgressObject redeploy(
        TargetModuleID[] moduleIDList,
        InputStream moduleArchive,
        InputStream deploymentPlan)
        throws UnsupportedOperationException, IllegalStateException {
        return new MockProgressObject();
    }

    public void release() {
    }
    
    public class MockProgressObject implements ProgressObject {

        public DeploymentStatus getDeploymentStatus() {
            return null;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return null;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID id) {
            return null;
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
        }

        public void addProgressListener(ProgressListener pol) {
        }

        public void removeProgressListener(ProgressListener pol) {
        }
        
    }
    
}
