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


package org.apache.geronimo.farm.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.rmi.RMIRegistryService;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class JmxDiscoveryPublisher implements GBeanLifecycle {

    private final URI service;
    private final DiscoveryAgent discoveryAgent;

    public JmxDiscoveryPublisher(@ParamAttribute(name = "nodeName")String nodeName,
                                 @ParamAttribute(name = "clusterName")String clusterName,
                                 @ParamAttribute(name = "protocol")String protocol,
                                 @ParamAttribute(name = "urlPath")String urlPath,
                                 @ParamAttribute(name = "discoveryType")String discoveryType,
                                 @ParamReference(name = "DiscoveryAgent")DiscoveryAgent discoveryAgent,
                                 @ParamReference(name = "RMIRegistryService")RMIRegistryService rmiRegistryService
    ) throws URISyntaxException, IOException {
        this.discoveryAgent = discoveryAgent;
        String query = null;
        if (nodeName != null && nodeName.length() > 0) {
            query = "node=" + nodeName;
        }
        if (clusterName != null) {
            query = (query == null? "": query + "&") + "cluster=" + clusterName;
        }
        service = new URI(discoveryType + ":" + protocol, null,  rmiRegistryService.getHost(), rmiRegistryService.getPort(), "/" + urlPath , query, null);
        discoveryAgent.registerService(service);
    }

    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        discoveryAgent.unregisterService(service);
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }
    }
}
