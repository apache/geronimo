/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering.wadi;

import java.net.URI;
import java.util.Collection;

import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version $Rev$ $Date$
 */
@GBean(name = "BasicWADISessionManagerHolder", j2eeType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE)
public class BasicWADISessionManagerHolder implements GBeanLifecycle {

    private static final String GBEAN_ATTR_SERVICE_SPACE_URI = "serviceSpaceURI";
    private static final String GBEAN_ATTR_SWEEP_INTERVAL = "sweepInterval";
    private static final String GBEAN_ATTR_SESSION_TIMEOUT = "sessionTimeout";
    private static final String GBEAN_ATTR_NUM_PARTITIONS = "numPartitions";
    private static final String GBEAN_ATTR_DISABLE_REPLICATION = "disableReplication";
    private static final String GBEAN_ATTR_DELTA_REPLICATION = "deltaReplication";

    private final BasicWADISessionManager basicWADISessionManager;

    private static final Logger log = LoggerFactory.getLogger(BasicWADISessionManagerHolder.class);

    public BasicWADISessionManagerHolder(
            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader cl,
            @ParamAttribute(name = GBEAN_ATTR_SERVICE_SPACE_URI) URI serviceSpaceURI,
            @ParamAttribute(name = GBEAN_ATTR_SWEEP_INTERVAL) int sweepInterval,
            @ParamAttribute(name = GBEAN_ATTR_SESSION_TIMEOUT) int sessionTimeout,
            @ParamAttribute(name = GBEAN_ATTR_NUM_PARTITIONS) int numPartitions,
            @ParamAttribute(name = GBEAN_ATTR_DISABLE_REPLICATION) boolean disableReplication,
            @ParamAttribute(name = GBEAN_ATTR_DELTA_REPLICATION) boolean deltaReplication,
            @ParamReference(name = BasicWADISessionManager.GBEAN_REF_CLUSTER) WADICluster cluster,
            @ParamReference(name = BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY) BackingStrategyFactory backingStrategyFactory,
            @ParamReference(name = BasicWADISessionManager.GBEAN_REF_SERVICE_HOLDERS) Collection<ClusteredServiceHolder> serviceHolders) {
        if (null == cl) {
            throw new IllegalArgumentException("cl is required");
        } else if (null == serviceSpaceURI) {
            throw new IllegalArgumentException("serviceSpaceURI is required");
        } else if (null == cluster) {
            throw new IllegalArgumentException("cluster is required");
        } else if (null == backingStrategyFactory) {
            throw new IllegalArgumentException("backingStrategyFactory is required");
        }

        WADISessionManagerConfigInfo configInfo = new WADISessionManagerConfigInfo(serviceSpaceURI, sweepInterval,
                numPartitions, sessionTimeout, disableReplication, deltaReplication);

        basicWADISessionManager = new BasicWADISessionManager(cl, configInfo, cluster, backingStrategyFactory,
                serviceHolders);

    }

    public BasicWADISessionManager getBasicWADISessionManager() {
        return basicWADISessionManager;
    }

    public void doStart() throws Exception {
        basicWADISessionManager.doStart();
    }

    public void doStop() throws Exception {
        basicWADISessionManager.doStop();
    }

    public void doFail() {
        basicWADISessionManager.doFail();
    }

}
