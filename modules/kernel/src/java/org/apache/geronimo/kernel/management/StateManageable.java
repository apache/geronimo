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

package org.apache.geronimo.kernel.management;

import org.apache.geronimo.kernel.management.State;


/**
 * A Java interface the meets the J2EE Management specification for a state manageable object.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:02 $
 */
public interface StateManageable {
    /**
     * Gets the state of this component as an int.
     * The int return is required by the JSR77 specification.
     * @see #getStateInstance to obtain the State instance
     * @return the current state of this component
     */
    int getState();

    /**
     * Gets the state of this component as a State instance.
     * @return the current state of this component
     */
    State getStateInstance();

    /**
     * Gets the start time of this component
     * @return time in milliseonds since epoch that this component was started.
     */
    long getStartTime();


    /**
     * Transitions the component to the starting state.  This method has access to the
     * container.
     *
     * Normally a component uses this to cache data from other components. The other components will
     * have been created at this stage, but not necessairly started and may not be ready to have methods
     * invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the stopped or failed state
     */
    void start() throws Exception, IllegalStateException;

    /**
     * Transitions the component to the starting state.  This method has access to the
     * container.
     *
     * If this Component is a Container, then startRecursive is called on all child Components
     * that are in the STOPPED or FAILED state.
     * Normally a component uses this to cache data from other components. The other components will
     * have been created at this stage, but not necessairly started and may not be ready to have methods
     * invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the STOPPED or FAILED state
     */
    void startRecursive() throws Exception, IllegalStateException;

    /**
     * Transitions the component to the stopping state.  This method has access to the
     * container.
     *
     * If this is Component is a Container, then all its child components must be in the
     * STOPPED or FAILED State.
     *
     * Normally a component uses this to drop references to data cached in the start method.
     * The other components will not necessairly have been stopped at this stage and may not be ready
     * to have methods invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the STOPPED or FAILED state
     */
    void stop() throws Exception, IllegalStateException;

}
