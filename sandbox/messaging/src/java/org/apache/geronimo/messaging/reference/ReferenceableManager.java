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

package org.apache.geronimo.messaging.reference;

import org.apache.geronimo.messaging.EndPoint;
import org.apache.geronimo.messaging.Request;

/**
 * Referenceable manager.
 *
 * @version $Rev$ $Date$
 */
public interface ReferenceableManager extends EndPoint
{

    /**
     * Starts.
     */
    public void start();

    /**
     * Stops.
     */
    public void stop();

    /**
     * Builds a proxy for the provided Referenceable.
     * <BR>
     * If the Referenceable is hosted by this manager, the Referenceable itself
     * is returned.
     * <BR>
     * An IllegalStateException is raised by this proxy if its methods are
     * invoked while the ReferenceableManager which has built it is stopped.
     * 
     * @param aReferenceInfo Reference meta-data.
     * @return An instance implementing the Reference Class and delegating
     * all the invokations to the Reference.
     */
    public Object factoryProxy(ReferenceableInfo aReferenceInfo);
    
    /**
     * Registers a Referenceable.
     * <BR>
     * If the same Referenceable is registered twice, then the returned
     * ReferenceableInfos have the same identifier.
     * <BR>
     * If a Referenceable is equal to another Referenceable via the equals
     * contract, then the ReferenceableInfos have distinct Referenceable
     * identifiers. 
     * 
     * @param aReference Referenceable to be registered.
     * @return Referenceable meta-data.
     */
    public ReferenceableInfo register(Referenceable aReference);
    
    /**
     * Unregisters a Referenceable.
     * 
     * @param aReferenceInfo Meta-data of the Referenceable to be unregistered.
     */
    public void unregister(ReferenceableInfo aReferenceInfo);
    
    /**
     * Invoke a request on the Referenceable having the specified identifier.
     * 
     * @param anId Referenceable identifier.
     * @param aRequest Request to be executed.
     * @return Result.
     * @exception Throwable raised by the execution of aRequest against the
     * Referenceable identified by anId.
     */
    public Object invoke(int anId, Request aRequest) throws Throwable;

}