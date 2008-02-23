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

import java.util.Set;

/**
 * Represents a local SessionManager.
 * <p>
 * A local SessionManager works collaboratively with remote SessionManagers to manage Session instances. A local 
 * SessionMananger along with its associated remote SessionManagers are a single space where Session instances live.
 * In this space, each Session is ensured to have a unique sessionId. This contract is enforced during creation of
 * a Session instance by a local SessionManager. A Session in this space is preemptively migrated from one local 
 * SessionManager to another. The interposition of a ClusteredInvocation between a Client and the Session he wants to
 * access ensures that at any point in time a Session is uniquely instantiated once cluster wide. Clients can
 * receive migration callbacks via the registration of SessionListener.
 *
 * @version $Rev$ $Date$
 */
public interface SessionManager {
    
    /**
     * Creates a Session having the specified sessionId.
     * 
     * @param sessionId Unique identifier of the Session instance.
     * @return Session instance.
     * @throws SessionAlreadyExistException Thrown when the provided sessiondId already exists in the Session space
     * of this local SessionManager and its associated remote SessionManagers.
     */
    Session createSession(String sessionId) throws SessionAlreadyExistException;
    
    /**
     * Registers a migration listener. 
     */
    void registerListener(SessionListener listener);    

    /**
     * Unregisters a migration listener.
     */
    void unregisterListener(SessionListener listener);    

    /**
     * Gets the Cluster this local SessionManager is associated to.
     * 
     * @return Associated Cluster.
     */
    Cluster getCluster();
    
    /**
     * Gets the Node hosting this local SessionManager.
     * 
     * @return Hosting Node.
     */
    Node getNode();

    /**
     * Gets the remote Nodes hosting the corresponding remote SessionManagers.
     * 
     * @return Hosting Node.
     */
    Set<Node> getRemoteNodes();
    
    void registerSessionManagerListener(SessionManagerListener listener);
    
    void unregisterSessionManagerListener(SessionManagerListener listener);
}
