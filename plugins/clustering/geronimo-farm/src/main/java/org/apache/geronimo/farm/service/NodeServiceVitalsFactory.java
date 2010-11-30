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
package org.apache.geronimo.farm.service;

import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version $Rev$ $Date$
 */
@GBean(name="NodeServiceVitalsFactory",j2eeType=GBeanInfoBuilder.DEFAULT_J2EE_TYPE)
public class NodeServiceVitalsFactory {

    static final Logger log = LoggerFactory.getLogger(NodeServiceVitalsFactory.class);

    // specific settings
    private final long initialReconnectDelay;
    private final long maxReconnectDelay;
    private final long backOffMultiplier;
    private final boolean useExponentialBackOff;
    private final int maxReconnectAttempts;

    public NodeServiceVitalsFactory(@ParamAttribute(name = "initialReconnectDelay") long initialReconnectDelay,
            @ParamAttribute(name = "maxReconnectDelay") long maxReconnectDelay,
            @ParamAttribute(name = "maxReconnectAttempts") int maxReconnectAttempts,
            @ParamAttribute(name = "backOffMultiplier") long backOffMultiplier,
            @ParamAttribute(name = "useExponentialBackOff") boolean useExponentialBackOff) {

        this.initialReconnectDelay = initialReconnectDelay;
        this.maxReconnectDelay = maxReconnectDelay;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.backOffMultiplier = backOffMultiplier;
        this.useExponentialBackOff = useExponentialBackOff;

    }

    public NodeServiceVitals createSerivceVitals(NodeService nodeService) {

        NodeServiceVitals serviceVitals = new NodeServiceVitals(nodeService);
        
        serviceVitals.setBackOffMultiplier(backOffMultiplier);
        serviceVitals.setInitialReconnectDelay(initialReconnectDelay);
        serviceVitals.setMaxReconnectAttempts(maxReconnectAttempts);
        serviceVitals.setMaxReconnectDelay(maxReconnectDelay);
        serviceVitals.setUseExponentialBackOff(useExponentialBackOff);

        return serviceVitals;
    }

}