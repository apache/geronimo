/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2004/01/24 21:07:44 $
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
