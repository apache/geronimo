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

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/01 12:44:22 $
 */
public class MultiProgressObjectTest extends TestCase
{

    public void testGetDeploymentStatus1() throws Exception {
        MultiProgressObject progressObject = new MultiProgressObject();
        MockProgressObject mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.RUNNING, null);
        progressObject.addProgressObject(mockProgressObject);
        mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.RUNNING, null);
        progressObject.addProgressObject(mockProgressObject);
        progressObject.consolidate();
        DeploymentStatus status = progressObject.getDeploymentStatus();
        assertEquals(StateType.RUNNING, status.getState());
    }

    public void testGetDeploymentStatus2() throws Exception {
        MultiProgressObject progressObject = new MultiProgressObject();
        MockProgressObject mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.RUNNING, null);
        progressObject.addProgressObject(mockProgressObject);
        mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.FAILED, null);
        progressObject.addProgressObject(mockProgressObject);
        progressObject.consolidate();
        DeploymentStatus status = progressObject.getDeploymentStatus();
        assertEquals(StateType.FAILED, status.getState());
    }
    
    public void testGetDeploymentStatus3() throws Exception {
        MultiProgressObject progressObject = new MultiProgressObject();
        MockProgressObject mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.COMPLETED, null);
        progressObject.addProgressObject(mockProgressObject);
        mockProgressObject = new MockProgressObject();
        mockProgressObject.status =
            new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                ActionType.EXECUTE, StateType.COMPLETED, null);
        progressObject.addProgressObject(mockProgressObject);
        progressObject.consolidate();
        DeploymentStatus status = progressObject.getDeploymentStatus();
        assertEquals(StateType.COMPLETED, status.getState());
    }
    
    private static class MockProgressObject implements ProgressObject {
        private DeploymentStatus status;
        public DeploymentStatus getDeploymentStatus() {
            return status;
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
