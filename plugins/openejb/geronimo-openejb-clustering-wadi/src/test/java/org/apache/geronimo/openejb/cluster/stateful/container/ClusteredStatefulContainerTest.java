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
package org.apache.geronimo.openejb.cluster.stateful.container;

import java.net.URI;
import java.util.Collections;

import com.agical.rmock.extension.junit.RMockTestCase;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTracker;
import org.apache.openejb.BeanContext;
import org.apache.openejb.AppContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;

/**
 * @version $Rev$ $Date$
 */
public class ClusteredStatefulContainerTest extends RMockTestCase {

    private WADISessionManager sessionManager;
    private ClusteredStatefulContainer container;
    private BeanContext deploymentInfo;
    private String deploymentId;
    private NetworkConnectorTracker tracker;

    @Override
    protected void setUp() throws Exception {
        sessionManager = (WADISessionManager) mock(WADISessionManager.class);
        sessionManager.getManager();
        modify().multiplicity(expect.from(0));
        
        sessionManager.registerListener(null);
        modify().multiplicity(expect.from(0)).args(is.NOT_NULL);
        
        ServiceSpace serviceSpace = sessionManager.getServiceSpace();
        modify().multiplicity(expect.from(0));
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        modify().multiplicity(expect.from(0));
        serviceRegistry.getStartedService(NetworkConnectorTracker.NAME);
        modify().multiplicity(expect.from(0));
        tracker = (NetworkConnectorTracker) mock(NetworkConnectorTracker.class);
        modify().returnValue(tracker);

        SecurityService securityService = (SecurityService) mock(SecurityService.class);
        container = (ClusteredStatefulContainer) intercept(ClusteredStatefulContainer.class, new Object[] {"id",
                securityService});
        deploymentId = "deploymentId";
        deploymentInfo = new BeanContext(deploymentId, 
            null, 
            new ModuleContext(deploymentId, null, null, new AppContext(deploymentId, SystemInstance.get(), getClass().getClassLoader(), null, null, false), null),
            SFSB.class,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false) {
            @Override
            public Object getDeploymentID() {
                return deploymentId;
            }
        };
    }
    
    
    public void testGetLocationsRetunsNullWhenDeploymentIsNotRegistered() throws Exception {
        startVerification();
        
        assertNull(container.getLocations(deploymentInfo));
    }
    
    public void testGetLocationsOK() throws Exception {
        tracker.getConnectorURIs(deploymentId);
        URI location = new URI("ejbd://host:1");
        modify().returnValue(Collections.singleton(location));
        
        startVerification();
        
        container.addSessionManager(deploymentId, sessionManager);
        
        URI[] actualLocations = container.getLocations(deploymentInfo);
        assertEquals(1, actualLocations.length);
        assertSame(location, actualLocations[0]);
    }
    
    public static class SFSB implements Runnable {
        public void run() {
        }
    }
    
}
