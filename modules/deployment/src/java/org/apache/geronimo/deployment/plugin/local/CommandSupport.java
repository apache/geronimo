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

package org.apache.geronimo.deployment.plugin.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:38 $
 */
public abstract class CommandSupport implements ProgressObject, Runnable {
    private final CommandType command;
    private ActionType action;
    private StateType state;
    private String message;
    private final Set listeners = new HashSet();
    private final List moduleIDs = new ArrayList();

    protected CommandSupport(CommandType command) {
        this.command = command;
        this.action = ActionType.EXECUTE;
        this.state = StateType.RUNNING;
        this.message = null;
    }

    protected synchronized void addModule(TargetModuleID moduleID) {
        moduleIDs.add(moduleID);
    }

    public synchronized TargetModuleID[] getResultTargetModuleIDs() {
        return (TargetModuleID[]) moduleIDs.toArray(new TargetModuleID[moduleIDs.size()]);
    }

    public DeploymentStatus getDeploymentStatus() {
        return new Status(command, action, state, message);
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        return null;
    }

    public boolean isCancelSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported");
    }

    public boolean isStopSupported() {
        return false;
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported");
    }

    public synchronized void addProgressListener(ProgressListener pol) {
        listeners.add(pol);
    }

    public synchronized void removeProgressListener(ProgressListener pol) {
        listeners.remove(pol);
    }

    protected void setState(StateType state) {
        Set toNotify;
        DeploymentStatus newStatus;
        synchronized (this) {
            this.state = state;
            newStatus = getDeploymentStatus();
            toNotify = listeners;
        }
        sendEvent(toNotify, newStatus);
    }

    protected void fail(String message) {
        Set toNotify;
        DeploymentStatus newStatus;
        synchronized (this) {
            this.message = message;
            this.state = StateType.FAILED;
            newStatus = getDeploymentStatus();
            toNotify = listeners;
        }
        sendEvent(toNotify, newStatus);
    }

    protected synchronized void complete(String message) {
        Set toNotify;
        DeploymentStatus newStatus;
        synchronized (this) {
            this.message = message;
            this.state = StateType.COMPLETED;
            newStatus = getDeploymentStatus();
            toNotify = listeners;
        }
        sendEvent(toNotify, newStatus);
    }

    private void sendEvent(Set toNotify, DeploymentStatus newStatus) {
        assert !Thread.holdsLock(this) : "Trying to send event whilst holding lock";
        ProgressEvent event = new ProgressEvent(this, null, newStatus);
        for (Iterator i = toNotify.iterator(); i.hasNext();) {
            ProgressListener listener = (ProgressListener) i.next();
            listener.handleProgressEvent(event);
        }
    }

    private static class Status implements DeploymentStatus {
        private final CommandType command;
        private final ActionType action;
        private final StateType state;
        private final String message;

        public Status(CommandType command, ActionType action, StateType state, String message) {
            this.command = command;
            this.action = action;
            this.state = state;
            this.message = message;
        }

        public CommandType getCommand() {
            return command;
        }

        public ActionType getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public StateType getState() {
            return state;
        }

        public boolean isRunning() {
            return StateType.RUNNING.equals(state);
        }

        public boolean isCompleted() {
            return StateType.COMPLETED.equals(state);
        }

        public boolean isFailed() {
            return StateType.FAILED.equals(state);
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("DeploymentStatus[").append(command).append(',');
            buf.append(action).append(',');
            buf.append(state);
            if (message != null) {
                buf.append(',').append(message);
            }
            buf.append(']');
            return buf.toString();
        }
    }
}
