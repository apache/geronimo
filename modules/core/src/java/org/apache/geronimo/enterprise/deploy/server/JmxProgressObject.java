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
package org.apache.geronimo.enterprise.deploy.server;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ActionType;
import javax.management.MBeanServer;
import javax.swing.event.EventListenerList;

/**
 * A ProgressObject implementation that listens for JMX notifications
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/17 10:51:53 $
 */
public class JmxProgressObject implements ProgressObject {
    private int jobID;
    private MBeanServer server;
    private JobDeploymentStatus status = new JobDeploymentStatus();
    private EventListenerList listenerList = new EventListenerList();
//todo: this class is just a beginning
    public JmxProgressObject(int jobID, MBeanServer server) {
        this.jobID = jobID;
        this.server = server;
    }

    /**
     * Retrieve the status of this activity.
     *
     * @return An object containing the status information.
     */
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }

    /**
     * Retrieve the list of TargetModuleIDs successfully processed or created
     * by the associated DeploymentManager operation.
     *
     * @return a list of TargetModuleIDs.
     */
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[0]; //todo: how can we generate this list?
    }

    /**
     * Return the ClientConfiguration object associated with the
     * TargetModuleID.
     *
     * @return ClientConfiguration for a given TargetModuleID or <tt>null</tt>
     *         if none exists.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID id) {
        return null; //todo: implement me
    }

    /**
     * Tests whether the vendor supports a cancel operation for this
     * deployment action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         canceled.
     */
    public boolean isCancelSupported() {
        return false;
    }

    /**
     * (optional) A cancel request on an in-process operation stops all further
     * processing of the operation and returns the environment to it original
     * state before the operation was executed.  An operation that has run to
     * completion cannot be cancelled.
     *
     * @throws OperationUnsupportedException occurs when this optional command
     *         is not supported by this implementation.
     */
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Cancel is not supported");
    }

    /**
     * Tests whether the vendor supports a stop operation for the deployment
     * action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         stopped.
     */
    public boolean isStopSupported() {
        return false; //todo: implement something in the DeploymentController to stop a job based on ID
    }

    /**
     * (optional) A stop request on an in-process operation allows the
     * operation on the current TargetModuleID to run to completion but does
     * not process any of the remaining unprocessed TargetModuleID objects.
     * The processed TargetModuleIDs must be returned by the method
     * getResultTargetModuleIDs.
     *
     * @throws OperationUnsupportedException occurs when this optional command
     *         is not supported by this implementation.
     */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Stop is not supported");
    }

    /**
     * Add a listener to receive progress events on deployment actions.
     *
     * @param pol the listener to receive events
     */
    public void addProgressListener(ProgressListener pol) {
        listenerList.add(ProgressListener.class, pol);
    }

    /**
     * Remove a progress listener.
     *
     * @param pol the listener to remove
     */
    public void removeProgressListener(ProgressListener pol) {
        listenerList.remove(ProgressListener.class, pol);
    }

    protected void fireProgressEvent(TargetModuleID tmid, DeploymentStatus status) {
        ProgressEvent event = null;
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ProgressListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new ProgressEvent(this, tmid, status);
                ((ProgressListener)listeners[i+1]).handleProgressEvent(event);
            }
        }
    }

    private static class TMDeploymentStatus implements DeploymentStatus {
        /**
         * Retrieve the StateType value.
         *
         * @return the StateType object
         */
        public StateType getState() {
            return null;
        }

        /**
         * Retrieve the deployment CommandType of this event.
         *
         * @return the CommandType Object
         */
        public CommandType getCommand() {
            return null;
        }

        /**
         * Retrieve the deployment ActionType for this event.
         *
         * @return the ActionType Object
         */
        public ActionType getAction() {
            return null;
        }

        /**
         * Retrieve any additional information about the status of this event.
         *
         * @return message text
         */
        public String getMessage() {
            return null;
        }

        /**
         * A convience method to report if the operation is in the completed state.
         *
         * @return <tt>true</tt> if this command has completed successfully
         */
        public boolean isCompleted() {
            return false;
        }

        /**
         * A convience method to report if the operation is in the failed state.
         *
         * @return <tt>true</tt> if this command has failed
         */
        public boolean isFailed() {
            return false;
        }

        /**
         * A convience method to report if the operation is in the running state.
         *
         * @return <tt>true</tt> if this command is still running
         */
        public boolean isRunning() {
            return false;
        }
    }

    private static class JobDeploymentStatus implements DeploymentStatus {
        /**
         * Retrieve the StateType value.
         *
         * @return the StateType object
         */
        public StateType getState() {
            return null;
        }

        /**
         * Retrieve the deployment CommandType of this event.
         *
         * @return the CommandType Object
         */
        public CommandType getCommand() {
            return null;
        }

        /**
         * Retrieve the deployment ActionType for this event.
         *
         * @return the ActionType Object
         */
        public ActionType getAction() {
            return null;
        }

        /**
         * Retrieve any additional information about the status of this event.
         *
         * @return message text
         */
        public String getMessage() {
            return null;
        }

        /**
         * A convience method to report if the operation is in the completed state.
         *
         * @return <tt>true</tt> if this command has completed successfully
         */
        public boolean isCompleted() {
            return false;
        }

        /**
         * A convience method to report if the operation is in the failed state.
         *
         * @return <tt>true</tt> if this command has failed
         */
        public boolean isFailed() {
            return false;
        }

        /**
         * A convience method to report if the operation is in the running state.
         *
         * @return <tt>true</tt> if this command is still running
         */
        public boolean isRunning() {
            return false;
        }
    }
}
