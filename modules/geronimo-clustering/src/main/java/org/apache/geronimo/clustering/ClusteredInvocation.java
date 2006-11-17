/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering;


/**
 * Represents a clustered invocation.
 * <p>
 * A clustered invocation is intended to be a thin wrapper around an actual invocation enhancing this latter with
 * an association to a local SessionManager. For instance, an HTTPRequest is an actual invocation.
 * <p> 
 * A clustered invocation is interposed between a client and the Session he wants to access to provide cluster wide
 * access serialization to the requested Session. A clustered invocation is associated to a local SessionManager, even
 * if no contract captures such a relationship. When a clustered invocation is executed one of the two following 
 * scenarios happen:
 * <ul>
 * <li>the clustered invocation is executed locally. A local execution implies that the local SessionManager associated
 * to the clustered invocation is owning the Session (may be after a migration); or</li>
 * <li>the clustered invocation is executed remotely on the Node where the Session is being owned.</li>
 * </ul>
 *
 * @version $Rev$ $Date$
 */
public interface ClusteredInvocation {
    
    /**
     * Invokes the clustered invocation.
     * 
     * @throws ClusteredInvocationException Thrown when the invocation cannot be successfully executed. This may
     * be either due to the fact that the actual invocation has failed or the requestedSessionId is unknown by
     * the associated local SessionManager and its remote peers.
     */
    void invoke() throws ClusteredInvocationException;
    
    /**
     * Gets the sessionId of the Session bound to the invocation represented by this instance.
     *  
     * @return sessionId of the targeted Session.
     */
    String getRequestedSessionId();
    
}
