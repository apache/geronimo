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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic NodeServiceTracker implementation
 * 
 * @version $Rev:$ $Date:$
 */

public class BasicNodeSerivceTracker implements NodeServiceTracker {

    private static final Logger log = LoggerFactory.getLogger(BasicNodeSerivceTracker.class);

    private final Map<String, Map<String, NodeServiceVitals>> remoteNodeServices;

    private final Map<String, NodeServiceVitals> localServices;

    private final Set<NodeServiceTrackerListener> nodeServiceLisenters;

    private NodeServiceVitalsFactory serviceVitalsFactory;

    public BasicNodeSerivceTracker() {
        remoteNodeServices = new HashMap<String, Map<String, NodeServiceVitals>>();
        nodeServiceLisenters = new HashSet<NodeServiceTrackerListener>();
        localServices = new HashMap<String, NodeServiceVitals>();
    }

    public void registerLocalNodeService(URI localServiceURI) {

        localServices.put(localServiceURI.toString(), serviceVitalsFactory.createSerivceVitals(new NodeService(
                localServiceURI)));

    }

    public void registerNodeService(String nodeName, URI serviceURI) {

        log.debug("registerNodeService:" + serviceURI + " to node:" + nodeName);

        Map<String, NodeServiceVitals> services = this.getNodeServicesInRemoteNode(nodeName);

        if (services.containsKey(serviceURI)) {
            
            log.debug("service heartbeat from service" + serviceURI + " of node:" + nodeName);

            services.get(serviceURI).heartbeat();

        } else {
            
            log.debug("fire service add event for service:" + serviceURI + " of node:" + nodeName);

            services.put(serviceURI.toString(), serviceVitalsFactory.createSerivceVitals(new NodeService(serviceURI)));
            fireServiceAddEvent(serviceURI);
        }

    }

    public void registerNodeServices(String nodeName, Map<String, NodeServiceVitals> newNodeServices) {

        log.debug("registerNodeServices:" + newNodeServices + " to node:" + nodeName);

        Map<String, NodeServiceVitals> services = this.getNodeServicesInRemoteNode(nodeName);

        for (String newServiceURI : newNodeServices.keySet()) {

            if (services.containsKey(newServiceURI)) {
                log.debug("service heartbeat from service" + newServiceURI + " of node:" + nodeName);
                services.get(newServiceURI).heartbeat();

            } else {
                log.debug("fire service add event for service:" + newServiceURI + " of node:" + nodeName);
                services.put(newServiceURI, newNodeServices.get(newServiceURI));
                fireServiceAddEvent(newNodeServices.get(newServiceURI).getService().getUri());
            }

        }

    }

    public void unregisterNodeService(String nodeName, URI serviceURI) {

        log.info("unregisterNodeServices:" + serviceURI + " to node:" + nodeName);

        Map<String, NodeServiceVitals> services = this.getNodeServicesInRemoteNode(nodeName);

        if (services.containsKey(serviceURI.toString())) {
            services.remove(serviceURI.toString());
            fireServiceRemovedEvent(serviceURI);
        }

    }

    public void unregisterNodeServices(String nodeName) {

        log.info("unregister all NodeServices for node:" + nodeName);

        synchronized (remoteNodeServices) {

            Map<String, NodeServiceVitals> services = remoteNodeServices.remove(nodeName);

            for (NodeServiceVitals vitals : services.values()) {
                fireServiceRemovedEvent(vitals.getService().getUri());
            }

        }
    }

    public Map<String, NodeServiceVitals> getNodeServicesInRemoteNode(String nodeName) {

        if (remoteNodeServices.get(nodeName) == null) {

            remoteNodeServices.put(nodeName, new HashMap<String, NodeServiceVitals>());
        }

        return remoteNodeServices.get(nodeName);

    }

    public Map<String, NodeServiceVitals> getLocalNodeServices() {

        return localServices;
    }

    public Map<String, Map<String, NodeServiceVitals>> getAllRemoteNodeServices() {

        return remoteNodeServices;
    }

    public void addNodeServiceTrackerListener(NodeServiceTrackerListener nodeServiceListener) {
        log.info("setNodeServiceListener "+nodeServiceListener);
        nodeServiceLisenters.add(nodeServiceListener);
    }

    void setServiceVitalsFactory(NodeServiceVitalsFactory _serviceVitalsFactory) {
        serviceVitalsFactory = _serviceVitalsFactory;
    }

    private final Executor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                public Thread newThread(Runnable runable) {
                    Thread t = new Thread(runable, "Multicast Discovery Agent Notifier");
                    t.setDaemon(true);
                    return t;
                }
            });

    private void fireServiceRemovedEvent(final URI uri) {

        if (nodeServiceLisenters == null || nodeServiceLisenters.size() == 0) {
            return;
        }

        for (final NodeServiceTrackerListener listener : nodeServiceLisenters) {
            // Have the listener process the event async so that
            // he does not block this thread since we are doing time sensitive
            // processing of events.
            executor.execute(new Runnable() {
                public void run() {
                    if (listener != null) {
                        listener.serviceRemoved(uri);
                    }
                }
            });
        }

    }

    private void fireServiceAddEvent(final URI uri) {


        if (nodeServiceLisenters == null || nodeServiceLisenters.size() == 0) {
            
            log.info("nodeServiceLisenters is null or nodeServiceLisenters.size =0");
            return;
        }

        for (final NodeServiceTrackerListener listener : nodeServiceLisenters) {

            // Have the listener process the event async so that
            // he does not block this thread since we are doing time sensitive
            // processing of events.
            executor.execute(new Runnable() {
                public void run() {
                    if (listener != null) {
                        log.info("listener.serviceAdded(uri):"+uri);
                        listener.serviceAdded(uri);
                    }
                }
            });
        }
    }

}
