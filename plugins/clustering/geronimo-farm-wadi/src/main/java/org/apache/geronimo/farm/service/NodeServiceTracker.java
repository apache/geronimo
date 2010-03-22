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

package org.apache.geronimo.farm.service;

import java.net.URI;
import java.util.Map;

import org.codehaus.wadi.servicespace.ServiceName;

/**
 * A tracker used to track all services in cluster.
 * 
 * 1, It's a cluster service registry to keep all local services as well as remote node services.
 * 2, It will notify nodeServiceListener when services are added/removed.
 * 
 * @version $Rev:$ $Date:$
 */
public interface NodeServiceTracker {
    
    /**
     * The name to identify this service in NodeServiceTrackerClusteredServiceHolder
     */
    ServiceName NAME = new ServiceName("NodeServiceTracker");

    /**
     * To register a service to a Node identified by nodeName, Usually used to register local service when server starts up.
     * 
     * @param serviceURI The service URI to register
     */
    void registerLocalNodeService(URI localServiceURI);
    
    /**
     * To register a service to a Node identified by nodeName, Usually used to register local service to remote tracker when server starts up.
     * 
     * @param nodeName The node name to register the service.
     * @param serviceURI The service URI to register
     */
    void registerNodeService(String nodeName, URI serviceURI);
    
    
    /**
     * Usually used in repeated task/thread to register local services to all remote nodes in cluster.
     * @param nodeName The node name to register the services.
     * @param nodeServices The services to register
     */
    void registerNodeServices(String nodeName, Map<String, NodeServiceVitals> nodeServices);

    /**
     * To unregister a service from a Node identified by nodeName, Usually used to unregister service from all nodes in cluster when the service is removed.
     * @param nodeName nodeName The node name to unregister the service.
     * @param serviceURI serviceURI The service URI to unregister
     */
    void unregisterNodeService(String nodeName, URI serviceURI);
    
    
    /**
     * To unregister all service from a Node identified by nodeName, Usually used to unregister services from all nodes in cluster when the current node is stopped/shutdown.
     * @param nodeName The node name to unregister the services.
     */
    void unregisterNodeServices(String nodeName);
    
    /**
     * To get all services in a peer Node in cluster .
     * 
     * @param nodeName the target node to get the services
     * @return all services in a cluster Node identified by nodeName
     */
    Map<String, NodeServiceVitals> getNodeServicesInRemoteNode(String nodeName);
    
    
    /**
     * To get all local services in current node .
     * 
     * @return all local services in current Node
     */
    Map<String, NodeServiceVitals> getLocalNodeServices();
    
    /**
     * To get all services in remote nodes.
     * 
     * @return all services in remote nodes.
     */
    Map<String, Map<String, NodeServiceVitals>>  getAllRemoteNodeServices();
      
    /**
     * Set node listener to the tracker. the listeners will be notified when service are added or removed.
     * 
     * @return all services in remote nodes.
     */
    void addNodeServiceTrackerListener(NodeServiceTrackerListener nodeServiceListener);

}
