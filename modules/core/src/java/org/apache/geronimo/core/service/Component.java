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

package org.apache.geronimo.core.service;


/**
 * Implements the JSR 77 state model
 *
 *
 * @version $Rev$ $Date$
 */
public interface Component 
{
    /**
     * Gets the container to which this component belongs.
     * @return the container for which invocations will be intercepted
     */
    Container getContainer();

    /**
     * Sets the container which ownes this component.
     * The contianer can only be set before create() or to null after the destroy().
     *
     * @param container which owns this component
     * @throws java.lang.IllegalStateException if this component is not in the not-created or destroyed state
     * @throws java.lang.IllegalArgumentException if this comonent has not been created and the container
     * parameter is null, or the component has been destroyed and the container parameter is NOT null
     */
    void setContainer(Container container) throws IllegalStateException, IllegalArgumentException;

}
