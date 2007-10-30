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

import java.util.Map;

/**
 * Represents a clustered session.
 * <p>
 * A Session is created by a SessionManager and is uniquely identified by its sessionId. More accurately, this 
 * sessionId is unique within the set of SessionManagers from which this Session has been sourced. If two Sessions 
 * have the same sessionId, then a client can be sure that they have been created from two distinct set of 
 * SessionManagers.
 * <p>
 * A Session provides Map like contracts to manipulate state information. State information must be Serializable as
 * it may be marshalled automatically by the underpinning local SessionManager. At any given point of time, a Session
 * is uniquely "instantiated" once cluster wide. Also, cluster wide accesses to a given Session are 
 * ensured to be serialized by the set of SessionManagers from which the Session has been sourced. The interposition
 * of a ClusteredInvocation between a client and the Session this client would like to access enforces unique
 * instantiation and access serialization cluster wide for a given Session.
 *
 * @version $Rev$ $Date$
 */
public interface Session {
    
    /**
     * Gets the sessionId.
     * 
     * @return sessionId.
     */
    String getSessionId();

    /**
     * Map like contract to manipulate state information.
     */
    Object addState(String key, Object value);

    /**
     * Map like contract to manipulate state information.
     */
    Object getState(String key);

    /**
     * Map like contract to manipulate state information.
     */
    Object removeState(String key);
    
    /**
     * Map like contract to manipulate state information.
     * <p>
     * The returned Map is mutable and is backed by the session.
     */
    Map getState();
    
    /**
     * Releases the session.
     * <p>
     * When a Session is released, it is released from the underlying set of SessionManagers. In other words, its
     * sessionId is unknown and its state is permanently lost. After the release of a Session, the behavior of
     * the other methods is undefined.
     */
    void release();
    
    /**
     * Notifies the session that state accesses are now completed. 
     * <p>
     * When state accesses end, the underlying local SessionManager may decide to replicate synchronously or
     * asynchronously the current state to remote SessionManagers.
     */
    void onEndAccess();
}
