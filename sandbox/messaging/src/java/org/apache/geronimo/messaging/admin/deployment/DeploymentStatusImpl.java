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

import java.io.Serializable;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 * A Serializable DeploymentStatus implementation.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentStatusImpl
    implements DeploymentStatus, Serializable
{

    private final int commandType;
    private final int actionType;
    private final int stateType;
    private final String message;
    
    public DeploymentStatusImpl(CommandType aCommand, ActionType anAction,
        StateType aState, String aMessage) {
        if ( null == aCommand ) {
            throw new IllegalArgumentException("Command is required");
        } else if ( null == anAction ) {
            throw new IllegalArgumentException("Action is required");
        } else if ( null == aState ) {
            throw new IllegalArgumentException("State is required");
        }
        commandType = aCommand.getValue();
        actionType = anAction.getValue();
        stateType = aState.getValue();
        message = aMessage;
    }
    
    public StateType getState() {
        return StateType.getStateType(stateType);
    }

    public CommandType getCommand() {
        return CommandType.getCommandType(commandType);
    }

    public ActionType getAction() {
        return ActionType.getActionType(actionType);
    }

    public String getMessage() {
        return message;
    }

    public boolean isCompleted() {
        return StateType.COMPLETED == getState();
    }

    public boolean isFailed() {
        return StateType.FAILED == getState();
    }

    public boolean isRunning() {
        return StateType.RUNNING == getState();
    }

}
