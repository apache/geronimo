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

import java.io.Externalizable;

/**
 * Event to be multicasted by a ReplicationCapable upon modification.
 *
 * @version $Rev$ $Date$
 */
public interface UpdateEvent extends Externalizable
{

    /**
     * Gets the event identifier.
     * 
     * @return Event identifier.
     */
    public int getId();
    
    /**
     * Gets the target of the event. It must be the ReplicationCapable which
     * has been updated.
     * 
     * @return Instance which has been updated.
     */
    public Object getTarget();
    
    /**
     * Sets the target of this event.
     * 
     * @param aTarget Instance which has been updated.
     */
    public void setTarget(Object aTarget);
    
}
