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
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;

/**
 * The ProgressObject interface tracks and reports the progress of the
 * deployment activities: distribute, start, stop, undeploy.
 *
 * This class has an <i>optional</i> cancel method.  The support of the cancel
 * function can be tested by the isCancelSupported method.
 *
 * The ProgressObject structure allows the user the option of polling for
 * status or to provide a callback.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:35 $
 */
public interface ProgressObject {
    /**
     * Retrieve the status of this activity.
     *
     * @return An object containing the status information.
     */
    public DeploymentStatus getDeploymentStatus();

    /**
     * Retrieve the list of TargetModuleIDs successfully processed or created
     * by the associated DeploymentManager operation.
     *
     * @return a list of TargetModuleIDs.
     */
    public TargetModuleID[] getResultTargetModuleIDs();

    /**
     * Return the ClientConfiguration object associated with the
     * TargetModuleID.
     *
     * @return ClientConfiguration for a given TargetModuleID or <tt>null</tt>
     *         if none exists.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID id);

    /**
     * Tests whether the vendor supports a cancel operation for this
     * deployment action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         canceled.
     */
    public boolean isCancelSupported();

    /**
     * (optional) A cancel request on an in-process operation stops all further
     * processing of the operation and returns the environment to it original
     * state before the operation was executed.  An operation that has run to
     * completion cannot be cancelled.
     *
     * @throws OperationUnsupportedException occurs when this optional command
     *         is not supported by this implementation.
     */
    public void cancel() throws OperationUnsupportedException;

    /**
     * Tests whether the vendor supports a stop operation for the deployment
     * action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         stopped.
     */
    public boolean isStopSupported();

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
    public void stop() throws OperationUnsupportedException;

    /**
     * Add a listener to receive progress events on deployment actions.
     *
     * @param pol the listener to receive events
     */
    public void addProgressListener(ProgressListener pol);

    /**
     * Remove a progress listener.
     *
     * @param pol the listener to remove
     */
    public void removeProgressListener(ProgressListener pol);
}
