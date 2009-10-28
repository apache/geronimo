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

package org.apache.geronimo.farm.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType=BasicClusterConfigurationController.GBEAN_J2EE_TYPE)
public class BasicClusterConfigurationController implements GBeanLifecycle, ClusterConfigurationController {
    private static final Logger log = LoggerFactory.getLogger(BasicClusterConfigurationController.class);

    private final ClusterInfo clusterInfo;
    private final String nodeName;
    private final Artifact artifact;
    private boolean startConfigurationUponStart;
    private boolean ignoreStartConfigurationFailureUponStart;

    public BasicClusterConfigurationController(@ParamReference(name=GBEAN_REF_CLUSTER_INFO) ClusterInfo clusterInfo,
            @ParamAttribute(name=GBEAN_ATTR_NODE_NAME) String nodeName,
            @ParamAttribute(name=GBEAN_ATTR_ARTIFACT) Artifact artifact,
            @ParamAttribute(name=GBEAN_ATTR_START_CONF_UPON_START) boolean startConfigurationUponStart,
            @ParamAttribute(name=GBEAN_ATTR_IGNORE_START_CONF_FAIL_UPON_START) boolean ignoreStartConfigurationFailureUponStart) {
        if (null == clusterInfo) {
            throw new IllegalArgumentException("clusterInfo is required");
        } else if (null == nodeName) {
            throw new IllegalArgumentException("nodeName is required");
        } else if (null == artifact) {
            throw new IllegalArgumentException("artifact is required");
        }
        this.clusterInfo = clusterInfo;
        this.nodeName = nodeName;
        this.artifact = artifact;
        this.startConfigurationUponStart = startConfigurationUponStart;
        this.ignoreStartConfigurationFailureUponStart = ignoreStartConfigurationFailureUponStart;
    }

    public void doStart() throws Exception {
        if (startConfigurationUponStart) {
            try {
                startConfiguration();
            } catch (Exception e) {
                if (ignoreStartConfigurationFailureUponStart) {
                    log.info("Exception while starting configuration [" + artifact + "] on [" + nodeName
                        + "]. Ignoring.", e);
                } else {
                    log.error("Exception while starting configuration [" + artifact + "] on [" + nodeName + "].", e);
                    throw e;
                }
            }
        }
    }

    public void doFail() {
        try {
            stopConfiguration();
        } catch (Exception e) {
            log.error("Exception while stopping configuration [" + artifact + "] on [" + nodeName + "].", e);
        }
    }

    public void doStop() throws Exception {
        try {
            stopConfiguration();
        } catch (Exception e) {
            log.error("Exception while stopping configuration [" + artifact + "] on [" + nodeName + "].", e);
            throw e;
        }
    }

    public void startConfiguration() throws Exception {
        for (NodeInfo nodeInfo : clusterInfo.getNodeInfos()) {
            if (!nodeInfo.getName().equals(nodeName)) {
                continue;
            }

            Kernel kernel = nodeInfo.newKernel();

            ConfigurationManager configurationManager = newConfigurationManager(kernel);
            if (!configurationManager.isLoaded(artifact)) {
                configurationManager.loadConfiguration(artifact);
            }
            configurationManager.startConfiguration(artifact);
        }
    }

    public void stopConfiguration() throws Exception {
        for (NodeInfo nodeInfo : clusterInfo.getNodeInfos()) {
            if (!nodeInfo.getName().equals(nodeName)) {
                continue;
            }

            Kernel kernel = nodeInfo.newKernel();

            ConfigurationManager configurationManager = newConfigurationManager(kernel);
            configurationManager.stopConfiguration(artifact);
        }
    }

    protected ConfigurationManager newConfigurationManager(Kernel kernel) throws GBeanNotFoundException {
        return ConfigurationUtil.getConfigurationManager(kernel);
    }

    public static final String GBEAN_J2EE_TYPE = "ClusterConfigurationController";
    public static final String GBEAN_ATTR_NODE_NAME = "nodeName";
    public static final String GBEAN_ATTR_ARTIFACT = "artifact";
    public static final String GBEAN_ATTR_START_CONF_UPON_START= "startConfigurationUponStart";
    public static final String GBEAN_ATTR_IGNORE_START_CONF_FAIL_UPON_START= "ignoreStartConfigurationFailureUponStart";
    public static final String GBEAN_REF_CLUSTER_INFO = "ClusterInfo";

}
