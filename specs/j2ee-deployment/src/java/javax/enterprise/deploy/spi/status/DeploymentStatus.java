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

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.ActionType;

/**
 * The DeploymentStatus interface provides information about the progress of a
 * deployment action.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:35 $
 */
public interface DeploymentStatus {
    /**
     * Retrieve the StateType value.
     *
     * @return the StateType object
     */
    public StateType getState();

    /**
     * Retrieve the deployment CommandType of this event.
     *
     * @return the CommandType Object
     */
    public CommandType getCommand();

    /**
     * Retrieve the deployment ActionType for this event.
     *
     * @return the ActionType Object
     */
    public ActionType getAction();

    /**
     * Retrieve any additional information about the status of this event.
     *
     * @return message text
     */
    public String getMessage();

    /**
     * A convience method to report if the operation is in the completed state.
     *
     * @return <tt>true</tt> if this command has completed successfully
     */
    public boolean isCompleted();

    /**
     * A convience method to report if the operation is in the failed state.
     *
     * @return <tt>true</tt> if this command has failed
     */
    public boolean isFailed();

    /**
     * A convience method to report if the operation is in the running state.
     *
     * @return <tt>true</tt> if this command is still running
     */
    public boolean isRunning();
}