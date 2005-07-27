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

package org.apache.geronimo.deployment.plugin.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.StringWriter;
import java.io.PrintWriter;
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

import org.apache.geronimo.deployment.plugin.jmx.JMXDeploymentManager.CommandContext;
import org.apache.geronimo.kernel.InternalKernelException;

/**
 * @version $Rev$ $Date$
 */
public abstract class CommandSupport implements ProgressObject, Runnable {
    private final CommandType command;
    private ActionType action;
    private StateType state;
    private String message;
    private final Set listeners = new HashSet();
    private final List moduleIDs = new ArrayList();
    private CommandContext commandContext = new CommandContext();

    private ProgressEvent event = null;

    protected CommandSupport(CommandType command) {
        this.command = command;
        this.action = ActionType.EXECUTE;
        this.state = StateType.RUNNING;
        this.message = null;
    }

    protected synchronized void addModule(TargetModuleID moduleID) {
        moduleIDs.add(moduleID);
    }

    protected synchronized int getModuleCount() {
        return moduleIDs.size();
    }

    public synchronized TargetModuleID[] getResultTargetModuleIDs() {
        return (TargetModuleID[]) moduleIDs.toArray(new TargetModuleID[moduleIDs.size()]);
    }

    public synchronized DeploymentStatus getDeploymentStatus() {
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

    public void addProgressListener(ProgressListener pol) {
        ProgressEvent event = null;
        synchronized (this) {
            listeners.add(pol);
            event = this.event;
        }
        if (event != null) {
            pol.handleProgressEvent(event);
        }
    }

    public synchronized void removeProgressListener(ProgressListener pol) {
        listeners.remove(pol);
    }

    protected final void fail(String message) {
        sendEvent(message, StateType.FAILED);
    }

    protected final void complete(String message) {
        sendEvent(message, StateType.COMPLETED);
    }

    protected final void updateStatus(String message) {
        sendEvent(message, state);
    }

    protected void doFail(Exception e) {
        if (e instanceof InternalKernelException) {
            Exception test = (Exception)((InternalKernelException)e).getCause();
            if(test != null) {
                e = test;
            }
        }

        if (commandContext.isLogErrors()) {
            System.err.println("Deployer operation failed: " + e.getMessage());
            if (commandContext.isVerbose()) {
                e.printStackTrace(System.err);
            }
        }

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println(e.getMessage());
        if (commandContext.isVerbose()) {
            e.printStackTrace(printWriter);
        } else {
            Throwable throwable = e;
            while (null != (throwable = throwable.getCause())) {
                printWriter.println("\t" + throwable.getMessage());
            }
        }
        fail(writer.toString());
    }

    private void sendEvent(String message, StateType state) {
        assert !Thread.holdsLock(this) : "Trying to send event whilst holding lock";

        ProgressListener[] toNotify;
        DeploymentStatus newStatus;
        synchronized (this) {
            this.message = message;
            this.state = state;
            newStatus = new Status(command, action, state, message);
            toNotify = (ProgressListener[]) listeners.toArray(new ProgressListener[listeners.size()]);
            event = new ProgressEvent(this, null, newStatus);
        }

        for (int i = 0; i < toNotify.length; i++) {
            toNotify[i].handleProgressEvent(event);
        }
    }

    protected static String clean(String value) {
        if(value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        }
        return value;
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

    public CommandContext getCommandContext() {
        return commandContext;
    }

    public void setCommandContext(CommandContext commandContext) {
        this.commandContext = commandContext;
    }
}
