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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;


/**
 * A ProgressObject which consolidates multiple ProgressObjects.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/01 12:44:22 $
 */
public class MultiProgressObject implements ProgressObject {

    /**
     * ProgressObjects to be consolidated.
     */
    private Collection progressObjects = new ArrayList();        

    /**
     * Consolidates the ProgressObject registered until now.
     * <BR>
     * ProgressObjects can no more be added.
     */
    public void consolidate() {
        progressObjects =
            Collections.unmodifiableCollection(progressObjects);
    }

    /**
     * Adds a ProgressObject. 
     * 
     * @param aProgObject Progress to be added.
     */
    public void addProgressObject(ProgressObject aProgObject) {
        synchronized(progressObjects) {
            progressObjects.add(aProgObject);
        }
    }

    /**
     * Gets the deployment status.
     * <BR>
     * If one of the contained ProgressObjects is failed, then the status is
     * considered as failed.<BR>
     * If the contained ProgressObjects are completed, then the status is
     * completed.<BR>
     * In all the other cases, the status is running.
     * 
     * @return Deployment status.
     */
    public DeploymentStatus getDeploymentStatus() {
        StringBuffer message = new StringBuffer();
        CommandType commandType = null;
        ActionType actionType = null;
        boolean completed = true;
        boolean failed = false;
        for (Iterator iter = progressObjects.iterator(); iter.hasNext();) {
            ProgressObject progress = (ProgressObject) iter.next();
            DeploymentStatus status = progress.getDeploymentStatus();
            CommandType curCommandType = status.getCommand();
            ActionType curActionType = status.getAction();
            if ( null == commandType ) {
                commandType = curCommandType;
                actionType = curActionType;
            } else if ( commandType != curCommandType ) {
                throw new AssertionError("Heterogeneous CommandType");
            } else if ( actionType != curActionType ) {
                throw new AssertionError("Heterogeneous ActionType");
            }
            message.append(status.getMessage() + "\n");
            if ( StateType.FAILED == status.getState() ) {
                failed = true;
            } else if ( StateType.COMPLETED != status.getState() ) {
                completed = false;
            }
            commandType = status.getCommand();
            actionType = status.getAction();
        }
        if ( failed ) {
            return new DeploymentStatusImpl(commandType, actionType,
                StateType.FAILED, message.toString());
        } else if ( completed ) {
            return new DeploymentStatusImpl(commandType, actionType,
                StateType.COMPLETED, message.toString());
        }
        return new DeploymentStatusImpl(commandType, actionType,
            StateType.RUNNING, message.toString());
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
        throw new OperationUnsupportedException("Not supported");
    }

    public boolean isStopSupported() {
        return false;
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Not supported");
    }

    public void addProgressListener(ProgressListener pol) {
    }

    public void removeProgressListener(ProgressListener pol) {
    }
    
}