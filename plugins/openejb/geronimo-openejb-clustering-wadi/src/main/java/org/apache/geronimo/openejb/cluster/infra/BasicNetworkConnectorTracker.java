/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.cluster.infra;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicNetworkConnectorTracker implements NetworkConnectorTracker {
    private final Map<Object, Set<URI>> idToLocations;
    private final Map<String, Set<URI>> nodeNameToLocations;
    
    public BasicNetworkConnectorTracker() {
        idToLocations = new HashMap<Object, Set<URI>>();
        nodeNameToLocations = new HashMap<String, Set<URI>>();
    }

    public Set<URI> getConnectorURIs(Object deploymentId) throws NetworkConnectorTrackerException {
        Set<URI> locations;
        synchronized (idToLocations) {
            locations = idToLocations.get(deploymentId);
            if (null == locations) {
                throw new NetworkConnectorTrackerException("[" + deploymentId + "] is not registered");
            }
            locations = new HashSet<URI>(locations);
        }
        return locations;
    }

    public void registerNetworkConnectorLocations(Object deploymentId, String nodeName, Set<URI> locations) {
        synchronized (idToLocations) {
            Set<URI> allLocations = idToLocations.get(deploymentId);
            if (null == allLocations) {
                allLocations = new HashSet<URI>();
                idToLocations.put(deploymentId, allLocations);
            }
            allLocations.addAll(locations);
            
            allLocations = nodeNameToLocations.get(nodeName);
            if (null == allLocations) {
                allLocations = new HashSet<URI>();
                nodeNameToLocations.put(nodeName, allLocations);
            }
            allLocations.addAll(locations);
        }
    }

    public void unregisterNetworkConnectorLocations(Object deploymentId, String nodeName, Set<URI> locations) {
        synchronized (idToLocations) {
            Set<URI> allLocations = idToLocations.get(deploymentId);
            if (null == allLocations) {
                return;
            }
            allLocations.removeAll(locations);
            if (allLocations.isEmpty()) {
                idToLocations.remove(deploymentId);
            }
            
            allLocations = nodeNameToLocations.get(nodeName);
            allLocations.removeAll(locations);
            if (allLocations.isEmpty()) {
                nodeNameToLocations.remove(nodeName);
            }
        }
    }

    public void unregisterNetworkConnectorLocations(String nodeName) {
        synchronized (idToLocations) {
            Set<URI> locationsToRemove = nodeNameToLocations.remove(nodeName);
            if (null == locationsToRemove) {
                return;
            }
            Map<Object, Set<URI>> clonedIdToLocations = new HashMap<Object, Set<URI>>(idToLocations);
            for (Map.Entry<Object, Set<URI>> entry : clonedIdToLocations.entrySet()) {
                Set<URI> allLocations = entry.getValue();
                allLocations.removeAll(locationsToRemove);
                if (allLocations.isEmpty()) {
                    idToLocations.remove(entry.getKey());
                }
            }
        }
    }
    
}
