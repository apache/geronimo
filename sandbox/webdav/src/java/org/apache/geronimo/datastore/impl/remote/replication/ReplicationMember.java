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

package org.apache.geronimo.datastore.impl.remote.replication;

import org.apache.geronimo.datastore.impl.remote.messaging.Connector;

/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/24 11:37:06 $
 */
public interface ReplicationMember
    extends UpdateListener, Connector {
    
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