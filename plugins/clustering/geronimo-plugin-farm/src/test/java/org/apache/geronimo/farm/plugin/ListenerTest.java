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


package org.apache.geronimo.farm.plugin;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;
import org.apache.geronimo.farm.config.ExtendedJMXConnectorInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.farm.discovery.DiscoveryAgent;
import org.apache.geronimo.farm.discovery.DiscoveryListener;
import org.apache.geronimo.farm.discovery.JmxDiscoveryPublisher;
import org.apache.geronimo.kernel.rmi.RMIRegistryService;
import org.apache.geronimo.system.plugin.DownloadResults;

/**
 * @version $Rev$ $Date$
 */
public class ListenerTest extends TestCase {
    private DiscoveryAgent agent;
    private FarmDiscoveryListener listener;
    private NodeListener nodeListener;
    private String clusterName;
    private NodeInfo nodeInfo;

    protected void setUp() throws Exception {
        agent = new DiscoveryAgent() {
            private DiscoveryListener listener;

            /**
             * Sets the discovery listener
             *
             * @param listener listener for addeed/removed services
             */
            public void setDiscoveryListener(DiscoveryListener listener) {
                this.listener = listener;
            }

            /**
             * register a service
             *
             * @param serviceUri uri for new service
             * @throws java.io.IOException on error
             */
            public void registerService(URI serviceUri) throws IOException {
                if (listener != null) {
                    listener.serviceAdded(serviceUri);
                }
            }

            /**
             * register a service
             *
             * @param serviceUri uri for removed service
             * @throws java.io.IOException on error
             */
            public void unregisterService(URI serviceUri) throws IOException {
                if (listener != null) {
                    listener.serviceRemoved(serviceUri);
                }
            }

            /**
             * A process actively using a service may see it go down before the DiscoveryAgent notices the
             * service's failure.  That process can use this method to notify the DiscoveryAgent of the failure
             * so that other listeners of this DiscoveryAgent can also be made aware of the failure.
             *
             * @param serviceUri uri for failed service
             * @throws java.io.IOException on error
             */
            public void reportFailed(URI serviceUri) throws IOException {
                if (listener != null) {
                    listener.serviceRemoved(serviceUri);
                }
            }
        };
        nodeListener = new NodeListener() {

            public Map<String, DownloadResults> addNode(String clusterName, NodeInfo nodeInfo) {
                ListenerTest.this.clusterName = clusterName;
                ListenerTest.this.nodeInfo = nodeInfo;
                return new HashMap<String, DownloadResults>();
            }

            public void removeNode(String clusterName, String nodeName) {
                if (!clusterName.equals(ListenerTest.this.clusterName)) {
                    throw new IllegalArgumentException("Mismatched cluster name: was: " + ListenerTest.this.clusterName + " now: " + clusterName);
                }
                if (!nodeName.equals(nodeInfo.getName())) {
                    throw new IllegalArgumentException("Mismatched node name: was: " + nodeInfo.getName() + " now: " + nodeName);
                }
                ListenerTest.this.clusterName = null;
                nodeInfo = null;
            }
        };
        listener = new FarmDiscoveryListener("cluster1", "farm", "user", "pw",
                agent, nodeListener);

    }

    public void testURIs() throws Exception {
        URI uri = new URI("test:jmx://localhost:8080/service?key=value");
        String scheme = uri.getScheme();
        String schemePart = uri.getSchemeSpecificPart();
        URI uri2 = URI.create(schemePart);
        String scheme2 = uri2.getScheme();
        String host = uri2.getHost();
        int port = uri2.getPort();
        String path = uri2.getPath();
        String query = uri2.getQuery();
        String fragment = uri2.getFragment();
    }

    public void testListener() throws Exception {
        URI service = URI.create("farm:jmx://localhost:1099/JMXService?node=node1&cluster=cluster2");
        assertEquals("cluster2", listener.toClusterName(service));
        NodeInfo nodeInfo = listener.toNodeInfo(service);
        checkNodeInfo(nodeInfo, "node1", "localhost", 1099, "user", "pw", "jmx", "JMXService");
    }

    private void checkNodeInfo(NodeInfo nodeInfo, String nodeName, String host, int port, String userName, String password, String protocol, String urlPath) {
        ExtendedJMXConnectorInfo info = nodeInfo.getConnectorInfo();
        assertEquals(nodeName, nodeInfo.getName());
        assertEquals(host, info.getHost());
        assertEquals(port, info.getPort());
        assertEquals(userName, info.getUsername());
        assertEquals(password, info.getPassword());
        assertEquals(protocol, info.getProtocol());
        assertEquals(urlPath, info.getUrlPath());
    }

    public void testAgent() throws Exception {
        RMIRegistryService rmiRegistryService = new RMIRegistryService();
        rmiRegistryService.setHost("localhost");
        rmiRegistryService.setPort(1099);
        JmxDiscoveryPublisher jmxDiscoveryPublisher = new JmxDiscoveryPublisher("node2", "cluster2", "rmi", "JMXService", "farm",agent, rmiRegistryService);
        assertEquals("cluster2", clusterName);
        checkNodeInfo(nodeInfo, "node2", "localhost", 1099, "user", "pw", "rmi", "JMXService");


    }
}
