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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version $Rev$ $Date$
 */
public class NodeServiceVitals implements Serializable {

    static final Logger log = LoggerFactory.getLogger(NodeServiceVitals.class);

    private static final long serialVersionUID = -7874058757277568424L;

    private long initialReconnectDelay;
    private long maxReconnectDelay;
    private long backOffMultiplier;
    private boolean useExponentialBackOff;
    private int maxReconnectAttempts;

    private final NodeService service;

    public NodeService getService() {
        return service;
    }

    long lastHeartBeat;
    long recoveryTime;
    int failureCount;
    boolean dead;

    NodeServiceVitals(NodeService service) {
        this.service = service;
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public synchronized void heartbeat() {
        lastHeartBeat = System.currentTimeMillis();

        // Consider that the service recovery has succeeded if it has not
        // failed in 60 seconds.
        if (!dead && failureCount > 0 && (lastHeartBeat - recoveryTime) > 1000 * 60) {

            log.debug("I now think that the " + service + " service has recovered.");

            failureCount = 0;
            recoveryTime = 0;
        }
    }

    public synchronized long getLastHeartbeat() {
        return lastHeartBeat;
    }

    public synchronized boolean pronounceDead() {
        if (!dead) {
            dead = true;
            failureCount++;

            long reconnectDelay;
            if (useExponentialBackOff) {
                reconnectDelay = (long) Math.pow(backOffMultiplier, failureCount);
                if (reconnectDelay > maxReconnectDelay) {
                    reconnectDelay = maxReconnectDelay;
                }
            } else {
                reconnectDelay = initialReconnectDelay;
            }

            log.debug("Remote failure of " + service + " while still receiving multicast advertisements.  "
                    + "Advertising events will be suppressed for " + reconnectDelay
                    + " ms, the current failure count is: " + failureCount);

            recoveryTime = System.currentTimeMillis() + reconnectDelay;
            return true;
        }
        return false;
    }

    /**
     * @return true if this broker is marked failed and it is now the right time to start recovery.
     */
    public synchronized boolean doRecovery() {
        if (!dead) {
            return false;
        }

        // Are we done trying to recover this guy?
        if (maxReconnectAttempts > 0 && failureCount > maxReconnectAttempts) {

            log.debug("Max reconnect attempts of the " + service + " service has been reached.");

            return false;
        }

        // Is it not yet time?
        if (System.currentTimeMillis() < recoveryTime) {
            return false;
        }

        log.debug("Resuming event advertisement of the " + service + " service.");

        dead = false;
        return true;
    }

    public boolean isDead() {
        return dead;
    }

    void setInitialReconnectDelay(long initialReconnectDelay) {
        this.initialReconnectDelay = initialReconnectDelay;
    }

    void setMaxReconnectDelay(long maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }

    void setBackOffMultiplier(long backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    void setUseExponentialBackOff(boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    void setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    @Override
    public String toString() {
        return "ServiceVitals [backOffMultiplier=" + backOffMultiplier + ", dead=" + dead + ", failureCount="
                + failureCount + ", initialReconnectDelay=" + initialReconnectDelay + ", lastHeartBeat="
                + lastHeartBeat + ", maxReconnectAttempts=" + maxReconnectAttempts + ", maxReconnectDelay="
                + maxReconnectDelay + ", recoveryTime=" + recoveryTime + ", useExponentialBackOff="
                + useExponentialBackOff + "]";
    }

}