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

package org.apache.geronimo.messaging.cluster;

import org.apache.geronimo.messaging.NodeInfo;

/**
 * A cluster-wide event.
 *
 * @version $Rev$ $Date$
 */
public class ClusterEvent
{

    /**
     * Event type when a member is added to a cluster.
     */
    public static final Object MEMBER_ADDED = new Object();
    
    /**
     * Event type when a member is removed from a cluster.
     */
    public static final Object MEMBER_REMOVED = new Object();
    
    /**
     * Source of the event.
     */
    private final Cluster cluster;
    
    /**
     * Type of event.
     */
    private final Object type;

    /**
     * Cluster member added/removed.
     */
    private final NodeInfo member;

    public ClusterEvent(Cluster aCluster, NodeInfo aMember, Object aType) {
        if ( null == aCluster ) {
            throw new IllegalArgumentException("Cluster is required");
        } else if ( null == aMember ) {
            throw new IllegalArgumentException("Member is required");
        } else if ( aType != MEMBER_ADDED && aType != MEMBER_REMOVED ) {
            throw new IllegalArgumentException("Wrong type");
        }
        cluster = aCluster;
        member = aMember;
        type = aType;
    }
    
    /**
     * @return Returns the cluster.
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @return Returns the type.
     */
    public Object getType() {
        return type;
    }

    public NodeInfo getMember() {
        return member;
    }
    
}
