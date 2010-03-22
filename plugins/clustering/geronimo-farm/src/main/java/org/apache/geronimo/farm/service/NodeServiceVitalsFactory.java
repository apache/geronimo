/**
 * 
 */
package org.apache.geronimo.farm.service;

import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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