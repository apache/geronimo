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

package org.apache.geronimo.kernel.deployment.client;

import javax.management.Notification;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 * A notification for a deployment
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:01 $
 */
public class DeploymentNotification extends Notification {
    public final static String DEPLOYMENT_COMPLETED = "app.deploy.completed";
    public final static String DEPLOYMENT_FAILED = "app.deploy.failure";
    public final static String DEPLOYMENT_UPDATE = "app.deploy.update";
    private int deploymentID;
    private TargetModuleID targetModuleID;

    public DeploymentNotification(String type, Object source, long sequenceNumber, int deploymentID, TargetModuleID targetModuleID) {
        super(type, source, sequenceNumber);
        this.deploymentID = deploymentID;
        this.targetModuleID = targetModuleID;
    }

    public DeploymentNotification(String type, Object source, long sequenceNumber, long timeStamp, int deploymentID, TargetModuleID targetModuleID) {
        super(type, source, sequenceNumber, timeStamp);
        this.deploymentID = deploymentID;
        this.targetModuleID = targetModuleID;
    }

    public DeploymentNotification(String type, Object source, long sequenceNumber, String message, int deploymentID, TargetModuleID targetModuleID) {
        super(type, source, sequenceNumber, message);
        this.deploymentID = deploymentID;
        this.targetModuleID = targetModuleID;
    }

    public DeploymentNotification(String type, Object source, long sequenceNumber, long timeStamp, String message, int deploymentID, TargetModuleID targetModuleID) {
        super(type, source, sequenceNumber, timeStamp, message);
        this.deploymentID = deploymentID;
        this.targetModuleID = targetModuleID;
    }

    public int getDeploymentID() {
        return deploymentID;
    }

    public void setDeploymentID(int deploymentID) {
        this.deploymentID = deploymentID;
    }

    public TargetModuleID getTargetModuleID() {
        return targetModuleID;
    }

    public void setTargetModuleID(TargetModuleID targetModuleID) {
        this.targetModuleID = targetModuleID;
    }
}
