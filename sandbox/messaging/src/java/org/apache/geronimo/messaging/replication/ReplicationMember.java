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

package org.apache.geronimo.messaging.replication;

import org.apache.geronimo.messaging.EndPoint;

/**
 * A replication group member.
 * <BR>
 * This is an EndPoint in charge of replicating the state of registered
 * ReplicantCapables across N-nodes, which constitute a replication group.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public interface ReplicationMember
    extends UpdateListener, EndPoint
{

    /**
     * Gets the replication group identifier.
     *  
     * @return Identifier.
     */
    public Object getReplicationGroupID();
    
    /**
     * Merges an UpdateEvent with a registered ReplicationCapable.
     * 
     * @param anEvent Update event to be merged.
     * @throws ReplicationException Indicates that the merge can not be
     * performed.
     */
    public void mergeWithUpdate(UpdateEvent anEvent)
        throws ReplicationException;
    
    /**
     * Registers a ReplicantCapable. From now, UpdateEvents multicasted
     * by the provided ReplicantCapable are also pushed to the replication
     * group.
     * 
     * @param aReplicant ReplicantCapable to be controlled by this group.
     */
    public void registerReplicantCapable(ReplicationCapable aReplicant);

    /**
     * Unregisters a ReplicantCapable.
     * 
     * @param aReplicant ReplicantCapable to be removed from the replication
     * group.
     */
    public void unregisterReplicantCapable(ReplicationCapable aReplicant); 
    
    /**
     * This method is for internal use only.
     * <BR>
     * It registers with this member a ReplicationCapable, which has been
     * registered by a remote member.  
     * 
     * @param aReplicant ReplicantCapable to be locally registered.
     */
    public void registerLocalReplicantCapable(ReplicationCapable aReplicant);
    
    /**
     * Retrieves the ReplicationCapable having the specified id.
     * 
     * @param anID Replicant identifier.
     * @return ReplicantCapable having the specified id or null if such an
     * identifier is not known.
     */
    public ReplicationCapable retrieveReplicantCapable(Object anID);
    
    
}