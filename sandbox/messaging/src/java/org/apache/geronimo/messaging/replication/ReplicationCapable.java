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

import java.io.Serializable;

/**
 *
 * TODO introduce versioning.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public interface ReplicationCapable
    extends Serializable
{

    /**
     * Identifier of this ReplicationCapable. It identifies uniquely the
     * instance in the scope of a replication group.
     * <BR>
     * Identifiers are automatically created by ReplicationMembers upon
     * registration.
     * 
     * @return Identifier of this instance.
     */
    public Object getID();
    
    /**
     * Sets the identifier of this instance in the scope of the replication
     * group managing it.
     * 
     * @param anID Identifier.
     */
    public void setID(Object anID);
    
    /**
     * Adds an UpdateEvent listener.
     * 
     * @param aListener Listener to be notified when an update is performed
     * on this instance.
     */
    public void addUpdateListener(UpdateListener aListener);
    
    /**
     * Removes the specified UpdateListener. 
     * 
     * @param aListener Listener to be removed.
     */
    public void removeUpdateListener(UpdateListener aListener);
    
    /**
     * Merges an UpdateEvent with the state of this instance. 
     * 
     * @param anEvent UpdateEvent to be merged with this instance.
     * @throws ReplicationException Indicates that the merge can not be
     * performed.
     */
    public void mergeWithUpdate(UpdateEvent anEvent)
        throws ReplicationException;
    
}
