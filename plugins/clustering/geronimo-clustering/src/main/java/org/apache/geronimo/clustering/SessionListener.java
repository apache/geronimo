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
 * Callback listener for inbound and outbound Session migration.
 * <p>
 * A Session is preemptively moved between its associated set of SessionManagers. For instance, when a 
 * ClusteredInvocation for a given Session is invoked on a Node where the local SessionManager, RequestingSM, does not 
 * own the targeted Session, the SessionManager owning the Session, OwningSM, the Session may be moved from OwningSM
 * to RequestingSM. OwningSM, prior to relinquish the Session, executes notifyOutboundSessionMigration and provides
 * the Session under migration. RequestingRM, after having acquired the Session ownership, executes 
 * notifyInboundSessionMigration and provides the Session under migration.
 * <p>
 * The typical usage of these migration callbacks are to allow a wrapping SessionManager, e.g. an HTTPSession manager,
 * to perform bookkeeping operations.
 *
 * @version $Rev$ $Date$
 */
public interface SessionListener {
    
    /**
     * Calls when the ownership of the provided Session is acquired by the SessionManager to which this listener
     * is attached.
     * 
     * @param session New Session now owned by the attached SessionManager.
     */
    void notifyInboundSessionMigration(Session session);
    
    /**
     * Calls when the ownership of the provided Session is relinquished to another SessionManager.
     * 
     * @param session Session now owned by another SessionManager.
     */
    void notifyOutboundSessionMigration(Session session);
    
    /**
     * Calls when a Session is destroyed.
     * 
     * @param session Destroyed session.
     */
    void notifySessionDestruction(Session session);
}
