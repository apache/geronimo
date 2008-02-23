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
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicNetworkConnectorTrackerTest extends TestCase {

    public void testRegistration() throws Exception {
        BasicNetworkConnectorTracker tracker = new BasicNetworkConnectorTracker();
        String deploymentId = "deploymentId";

        URI uri1 = new URI("uri1");
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri1));
        Set<URI> connectorURIs = tracker.getConnectorURIs(deploymentId);
        assertEquals(1, connectorURIs.size());
        assertTrue(connectorURIs.contains(uri1));
        
        URI uri2 = new URI("uri2");
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName2", Collections.singleton(uri2));
        connectorURIs = tracker.getConnectorURIs(deploymentId);
        assertEquals(2, connectorURIs.size());
        assertTrue(connectorURIs.contains(uri1));
        assertTrue(connectorURIs.contains(uri2));
    }
    
    public void testUnregistration() throws Exception {
        BasicNetworkConnectorTracker tracker = new BasicNetworkConnectorTracker();
        String deploymentId = "deploymentId";

        URI uri1 = new URI("uri1");
        URI uri2 = new URI("uri2");
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri1));
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri2));
        tracker.unregisterNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri2));
        Set<URI> connectorURIs = tracker.getConnectorURIs(deploymentId);
        assertEquals(1, connectorURIs.size());
        assertTrue(connectorURIs.contains(uri1));
    }
    
    public void testUnregistrationForAllURIs() throws Exception {
        BasicNetworkConnectorTracker tracker = new BasicNetworkConnectorTracker();
        String deploymentId = "deploymentId";
        
        URI uri1 = new URI("uri1");
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri1));
        tracker.unregisterNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri1));
        try {
            tracker.getConnectorURIs(deploymentId);
            fail();
        } catch (NetworkConnectorTrackerException e) {
        }
    }
    
    public void testUnregistrationForNode() throws Exception {
        BasicNetworkConnectorTracker tracker = new BasicNetworkConnectorTracker();
        String deploymentId = "deploymentId";
        
        URI uri1 = new URI("uri1");
        tracker.registerNetworkConnectorLocations(deploymentId, "nodeName", Collections.singleton(uri1));
        tracker.unregisterNetworkConnectorLocations("nodeName");
        try {
            tracker.getConnectorURIs(deploymentId);
            fail();
        } catch (NetworkConnectorTrackerException e) {
        }
    }
    
    public void testGetConnectorURIsThrowsNCEWhenNoURIsForDeploymentId() throws Exception {
        BasicNetworkConnectorTracker tracker = new BasicNetworkConnectorTracker();
        try {
            tracker.getConnectorURIs("deploymentId");
            fail();
        } catch (NetworkConnectorTrackerException e) {
        }
    }
    
}
