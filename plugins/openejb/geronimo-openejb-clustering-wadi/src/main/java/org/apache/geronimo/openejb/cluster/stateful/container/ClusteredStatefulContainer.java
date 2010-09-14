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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTracker;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTrackerException;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ClusteredRPCContainer;
import org.apache.openejb.core.stateful.StatefulContainer;
import org.apache.openejb.spi.SecurityService;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;

/**
 *
 * @version $Rev:$ $Date:$
 */
// todo Consider replacing this class with an RpcContainerWrapper subclass
public class ClusteredStatefulContainer extends StatefulContainer implements SessionManagerTracker, ClusteredRPCContainer {
    private final Map<Object, Manager> deploymentIdToManager;
    private final Map<Object, NetworkConnectorTracker> deploymentIdToNetworkConnectorTracker;

    public ClusteredStatefulContainer(Object id, SecurityService securityService) {
        super(id, securityService, new WadiCache());

        deploymentIdToManager = new HashMap<Object, Manager>();
        deploymentIdToNetworkConnectorTracker = new HashMap<Object, NetworkConnectorTracker>();
    }

    public void addSessionManager(Object deploymentId, SessionManager sessionManager) {
        ((WadiCache) cache).addSessionManager(deploymentId, sessionManager);
        
        WADISessionManager wadiSessionManager = (WADISessionManager) sessionManager;
        
        Manager manager = wadiSessionManager.getManager();
        synchronized (deploymentIdToManager) {
            deploymentIdToManager.put(deploymentId, manager);
        }

        ServiceSpace serviceSpace = wadiSessionManager.getServiceSpace();
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        NetworkConnectorTracker networkConnectorTracker;
        try {
            networkConnectorTracker = (NetworkConnectorTracker) serviceRegistry.getStartedService(NetworkConnectorTracker.NAME);
        } catch (Exception e) {
            throw new IllegalStateException("Should never occur" ,e);
        }
        synchronized (deploymentIdToNetworkConnectorTracker) {
            deploymentIdToNetworkConnectorTracker.put(deploymentId, networkConnectorTracker);   
        }
    }

    public void removeSessionManager(Object deploymentId, SessionManager sessionManager) {
        ((WadiCache) cache).removeSessionManager(deploymentId, sessionManager);
        
        synchronized (deploymentIdToManager) {
            deploymentIdToManager.remove(deploymentId);
        }
        synchronized (deploymentIdToNetworkConnectorTracker) {
            deploymentIdToNetworkConnectorTracker.remove(deploymentId);
        }
    }
    
    public URI[] getLocations(BeanContext beanContext) {
        Object deploymentID = beanContext.getDeploymentID();
        NetworkConnectorTracker networkConnectorTracker;
        synchronized (deploymentIdToNetworkConnectorTracker) {
            networkConnectorTracker = deploymentIdToNetworkConnectorTracker.get(deploymentID);
        }
        if (null == networkConnectorTracker) {
            return null;
        }

        Set<URI> connectorURIs;
        try {
            connectorURIs = networkConnectorTracker.getConnectorURIs(deploymentID);
        } catch (NetworkConnectorTrackerException e) {
            return null;
        }
        return connectorURIs.toArray(new URI[connectorURIs.size()]);
    }

}
