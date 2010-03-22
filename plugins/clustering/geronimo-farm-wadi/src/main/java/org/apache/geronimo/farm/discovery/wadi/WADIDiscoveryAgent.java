/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.farm.discovery.wadi;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geronimo.clustering.wadi.BasicWADISessionManager;
import org.apache.geronimo.clustering.wadi.BasicWADISessionManagerHolder;
import org.apache.geronimo.farm.discovery.DiscoveryAgent;
import org.apache.geronimo.farm.discovery.DiscoveryListener;
import org.apache.geronimo.farm.service.NodeServiceTracker;
import org.apache.geronimo.farm.service.NodeServiceTrackerListener;
import org.apache.geronimo.farm.service.NodeServiceVitals;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.codehaus.wadi.servicespace.InvocationMetaData;
import org.codehaus.wadi.servicespace.ServiceProxy;
import org.codehaus.wadi.servicespace.ServiceProxyFactory;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "WADIDiscoveryAgent", j2eeType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE)
public class WADIDiscoveryAgent implements DiscoveryAgent, GBeanLifecycle {

    static final Logger log = LoggerFactory.getLogger(WADIDiscoveryAgent.class);

    private final BasicWADISessionManager WADISessionManager;
    private NodeServiceTracker localTracker;
    private NodeServiceTracker clusterNodeServiceTrackerProxy;

    private AtomicBoolean started = new AtomicBoolean(false);

    private int maxMissedHeartbeats = 10;
    private long heartRate = 500;

    private final NodeServiceHandlerThread listener;

    private final String localNodeName;

    public WADIDiscoveryAgent(
            @ParamReference(name = "BasicWADISessionManagerHolder") BasicWADISessionManagerHolder WADISessionManagerHolder,
            @ParamAttribute(name = "heartRate") long heartRate,
            @ParamAttribute(name = "maxMissedHeartbeats") int maxMissedHeartbeats) {

        this.WADISessionManager = WADISessionManagerHolder.getBasicWADISessionManager();
        this.heartRate = heartRate;
        this.maxMissedHeartbeats = maxMissedHeartbeats;
        this.localNodeName = WADISessionManager.getNode().getName();
        listener = new NodeServiceHandlerThread();
    }

    public String getName() {
        return "WADI disovery Agent";
    }

    /* start of discoveryAgent interface implementation *************** */
    public void setDiscoveryListener(final DiscoveryListener listener) {
        // this.listener.setDiscoveryListener(listener);

        localTracker.addNodeServiceTrackerListener(new NodeServiceTrackerListener() {

            public void serviceAdded(URI service) {
                listener.serviceAdded(service);

            }

            public void serviceRemoved(URI service) {
                listener.serviceRemoved(service);

            }

        });
    }

    public void registerService(URI serviceUri) throws IOException {

        localTracker.registerLocalNodeService(serviceUri);

        // register local service to remote peers
        clusterNodeServiceTrackerProxy.registerNodeService(localNodeName, serviceUri);
    }

    public void unregisterService(URI serviceUri) throws IOException {

        localTracker.unregisterNodeService(localNodeName, serviceUri);

        clusterNodeServiceTrackerProxy.unregisterNodeService(localNodeName, serviceUri);
    }

    public void reportFailed(URI serviceUri) throws IOException {
        Map<String, NodeServiceVitals> localServices = localTracker.getLocalNodeServices();
        NodeServiceVitals vitals = localServices.get(serviceUri);

        if (vitals != null && !vitals.isDead()) {
            localServices.remove(vitals.getService().getUriString());
        }
    }

    /* end of discoveryAgent interface implementation ************** */

    /**
     * start the discovery agent
     * 
     * @throws Exception
     *             on error
     */
    public void doStart() throws Exception {

        clusterNodeServiceTrackerProxy = getClusterNodeServiceTrackerProxy();
        localTracker = getLocalNodeServiceTracker();

        NodeServiceHandlerThread nodeServiceHandlerThread = new NodeServiceHandlerThread();
        Timer timer = new Timer("WADIDiscoveryAgent: checkRemoteService", true);

        timer.scheduleAtFixedRate(nodeServiceHandlerThread, 0, heartRate);

        if (started.compareAndSet(false, true)) {

            Thread listenerThread = new Thread(listener);
            listenerThread.setName("WADIDiscoveryAgent: ListenerThread");
            listenerThread.setDaemon(true);
            listenerThread.start();
        }
    }

    /**
     * stop the channel
     * 
     * @throws Exception
     *             pm error
     */
    public void doStop() throws Exception {
        if (started.compareAndSet(true, false)) {
            // multicast.close();
        }
    }

    /**
     * Fails the GBean. This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            // ignore
        }
    }

    class NodeServiceHandlerThread extends TimerTask {

        public void run() {

            checkRemoteServices();
            broadCastLocalServicesToRemotePeers();
        }

        private void checkRemoteServices() {

            Map<String, Map<String, NodeServiceVitals>> remoteServices = localTracker.getAllRemoteNodeServices();

            //logAllRemoteServices(false, remoteServices);

            long expireTime = System.currentTimeMillis() - (heartRate * maxMissedHeartbeats);

            if (remoteServices == null || remoteServices.size() == 0)
                return;

            for (String nodeName : remoteServices.keySet()) {

                Map<String, NodeServiceVitals> services = remoteServices.get(nodeName);

                for (NodeServiceVitals serviceVitals : services.values()) {

                    if (serviceVitals.getLastHeartbeat() < expireTime) {

                        NodeServiceVitals vitals = services.remove(serviceVitals.getService().getUri());

                        if (vitals != null && !vitals.isDead()) {

                            localTracker.unregisterNodeService(nodeName, vitals.getService().getUri());

                        }
                    }
                }

            }

        }

        private void broadCastLocalServicesToRemotePeers() {
            clusterNodeServiceTrackerProxy.registerNodeServices(localNodeName, localTracker.getLocalNodeServices());
        }

    }

    private NodeServiceTracker getClusterNodeServiceTrackerProxy() {

        ServiceSpace serviceSpace = WADISessionManager.getServiceSpace();
        ServiceProxyFactory proxyFactory = serviceSpace.getServiceProxyFactory(NodeServiceTracker.NAME,
                new Class[] { NodeServiceTracker.class });

        InvocationMetaData invocationMetaData = proxyFactory.getInvocationMetaData();
        invocationMetaData.setOneWay(true);

        ServiceProxy proxy = proxyFactory.getProxy();

        return (NodeServiceTracker) proxy;

    }

    private NodeServiceTracker getLocalNodeServiceTracker() {

        ServiceSpace serviceSpace = WADISessionManager.getServiceSpace();
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();

        try {
            return (NodeServiceTracker) serviceRegistry.getStartedService(NodeServiceTracker.NAME);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void logAllRemoteServices(boolean isVerbose, Map<String, Map<String, NodeServiceVitals>> results) {

        log.info("*" + localNodeName + " service registry:");

        Set<String> NodeNameSet = results.keySet();
        Map<String, NodeServiceVitals> services_in_node;
        int i = 0;
        for (String nodeName : NodeNameSet) {

            i++;
            log.info("    No." + i + ": " + nodeName);
            services_in_node = results.get(nodeName);
            Set<String> serviceURIs = services_in_node.keySet();

            for (String serviceURI : serviceURIs) {
                log.info("");
                log.info("        Service:" + serviceURI);

                if (isVerbose) {

                    log.info("        " + services_in_node.get(serviceURI));

                }
            }

        }

        log.info("");
        log.info("");

    }

}
