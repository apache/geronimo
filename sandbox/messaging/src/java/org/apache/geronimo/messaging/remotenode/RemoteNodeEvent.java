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

package org.apache.geronimo.messaging.remotenode;

/**
 * Event for RemoteNodes.
 * 
 * @version $Rev$ $Date$
 */
public class RemoteNodeEvent
{
    
    /**
     * Event type indicating that a RemoteNode has been added.
     */
    public static final Object NODE_ADDED = new Object();
    
    /**
     * Event type indicating that a RemoteNode has been removed.
     */
    public static final Object NODE_REMOVED = new Object(); 

    /**
     * RemoteNode.
     */
    private final RemoteNode remoteNode;
    
    /**
     * Event type.
     */
    private final Object type;
    
    /**
     * Creates an event for the specified RemoteNode.
     * 
     * @param aNode Event focus.
     * @param aType Type.
     */
    public RemoteNodeEvent(RemoteNode aNode, Object aType) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        } else if ( null == aType ) {
            throw new IllegalArgumentException("Type is required.");
        }
        remoteNode = aNode;
        type = aType;
    }
 
    /**
     * Gets the focused RemoteNode.
     * 
     * @return RemoteNode.
     */
    public RemoteNode getRemoteNode() {
        return remoteNode;
    }
    
    /**
     * @return true if the focused RemoteNode has been added.
     */
    public boolean isAddEvent() {
        return type == NODE_ADDED;
    }
    
    /**
     * @return true if the focused RemoteNode has been removed.
     */
    public boolean isRemoveEvent() {
        return type == NODE_REMOVED;
    }
    
}
