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
package org.apache.geronimo.clustering.wadi;

import java.io.Serializable;
import java.net.URI;

/**
 * 
 * @version $Rev$ $Date$
 */
public class WADISessionManagerConfigInfo implements Serializable {
    private final URI serviceSpaceURI;
    private final int sweepInterval;
    private final int numPartitions;
    private final int sessionTimeoutSeconds;
    private final boolean disableReplication;
    private final boolean deltaReplication;
    
    public WADISessionManagerConfigInfo(URI serviceSpaceURI, int sweepInterval, int numPartitions,
            int sessionTimeoutSeconds,
            boolean disableReplication,
            boolean deltaReplication) {
        if (null == serviceSpaceURI) {
            throw new IllegalArgumentException("serviceSpaceURI is required");
        } else if (1 > sweepInterval) {
            throw new IllegalArgumentException("sweepInterval must be greater than 0");
        } else if (1 > numPartitions) {
            throw new IllegalArgumentException("numPartitions must be greater than 0");
        } else if (1 > sessionTimeoutSeconds) {
            throw new IllegalArgumentException("sessionTimeoutSeconds must be greater than 0");
        }
        this.serviceSpaceURI = serviceSpaceURI;
        this.sweepInterval = sweepInterval;
        this.numPartitions = numPartitions;
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
        this.disableReplication = disableReplication;
        this.deltaReplication = deltaReplication;
    }

    public int getNumPartitions() {
        return numPartitions;
    }

    public URI getServiceSpaceURI() {
        return serviceSpaceURI;
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public int getSweepInterval() {
        return sweepInterval;
    }

    public boolean isDisableReplication() {
        return disableReplication;
    }

    public boolean isDeltaReplication() {
        return deltaReplication;
    }
    
}
