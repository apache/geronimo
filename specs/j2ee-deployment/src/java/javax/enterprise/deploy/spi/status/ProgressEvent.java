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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi.status;

import javax.enterprise.deploy.spi.TargetModuleID;
import java.util.EventObject;

/**
 * An event which indicates that a deployment status change has occurred.
 *
 * @see ProgressObject
 * @see ProgressListener
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:35 $
 */
public class ProgressEvent extends EventObject {
    private TargetModuleID targetModuleID;
    private DeploymentStatus deploymentStatus;

    /**
     * Creates a new object representing a deployment progress event.
     *
     * @param source         the object on which the Event initially occurred.
     * @param targetModuleID the combination of target and module for which the
     *                       event occured.
     * @param sCode          the object containing the status information.
     */
    public ProgressEvent(Object source, TargetModuleID targetModuleID, DeploymentStatus sCode) {
        super(source);
        this.targetModuleID = targetModuleID;
        this.deploymentStatus = sCode;
    }

    /**
     * Retrieves the TargetModuleID for this event.
     *
     * @return the TargetModuleID
     */
    public TargetModuleID getTargetModuleID() {
        return targetModuleID;
    }

    /**
     * Retrieves the status information for this event.
     *
     * @return the object containing the status information.
     */
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
}