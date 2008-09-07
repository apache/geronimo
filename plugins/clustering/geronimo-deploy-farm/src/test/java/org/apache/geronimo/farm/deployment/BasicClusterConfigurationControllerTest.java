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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicClusterConfigurationControllerTest extends RMockTestCase {

    private ClusterInfo clusterInfo;
    private NodeInfo nodeInfo;
    private String nodeName;
    private Artifact configurationId;
    private ConfigurationManager configurationManager;

    @Override
    protected void setUp() throws Exception {
        nodeName = "nodeName";

        clusterInfo = (ClusterInfo) mock(ClusterInfo.class);

        Collection<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        
        nodeInfo = (NodeInfo) mock(NodeInfo.class, "NodeInfo1");
        nodeInfos.add(nodeInfo);
        nodeInfo.getName();
        modify().multiplicity(expect.from(0)).returnValue(nodeName);
        nodeInfo.newKernel();
        modify().multiplicity(expect.from(0));
        
        NodeInfo secondNodeInfo = (NodeInfo) mock(NodeInfo.class, "NodeInfo2");
        nodeInfos.add(secondNodeInfo);
        secondNodeInfo.getName();
        modify().multiplicity(expect.from(0)).returnValue("unkown");
        
        clusterInfo.getNodeInfos();
        modify().multiplicity(expect.from(0)).returnValue(nodeInfos);
        
        configurationId = new Artifact("groupId", "artifactId", "2.0", "car");
        configurationManager = (ConfigurationManager) mock(ConfigurationManager.class);
    }
    
    public void testDoNotStartConfigurationUponStart() throws Exception {
        startVerification();

        BasicClusterConfigurationController controller = newController(false, false);
        
        controller.doStart();
    }

    public void testStartConfigurationUponStartOK() throws Exception {
        configurationManager.isLoaded(configurationId);
        modify().returnValue(false);
        configurationManager.loadConfiguration(configurationId);
        configurationManager.startConfiguration(configurationId);
        
        startVerification();

        BasicClusterConfigurationController controller = newController(true, false);
        
        controller.doStart();
    }
    
    public void testStartConfigurationFailsUponStartAndIgnore() throws Exception {
        configurationManager.isLoaded(configurationId);
        modify().returnValue(true);
        configurationManager.startConfiguration(configurationId);
        modify().throwException(new NoSuchConfigException(configurationId));
        
        startVerification();

        BasicClusterConfigurationController controller = newController(true, true);
        
        controller.doStart();
    }
    
    public void testStartConfigurationFailsUponStart() throws Exception {
        configurationManager.isLoaded(configurationId);
        modify().returnValue(true);
        configurationManager.startConfiguration(configurationId);
        NoSuchConfigException expectedException = new NoSuchConfigException(configurationId);
        modify().throwException(expectedException);
        
        startVerification();

        BasicClusterConfigurationController controller = newController(true, false);

        try {
            controller.startConfiguration();
            fail();
        } catch (Exception e) {
            assertSame(expectedException, e);
        }
    }
    
    public void testStopConfiguration() throws Exception {
        configurationManager.stopConfiguration(configurationId);

        startVerification();

        BasicClusterConfigurationController controller = newController(true, false);
        controller.doStop();
    }

    public void testFail() throws Exception {
        configurationManager.stopConfiguration(configurationId);
        
        startVerification();
        
        BasicClusterConfigurationController controller = newController(true, false);
        controller.doFail();
    }
    
    private BasicClusterConfigurationController newController(boolean startConfigurationUponStart,
            boolean ignoreStartConfigurationFailureUponStart) {
        return new BasicClusterConfigurationController(clusterInfo,
            nodeName,
            configurationId,
            startConfigurationUponStart,
            ignoreStartConfigurationFailureUponStart) {
            @Override
            protected ConfigurationManager newConfigurationManager(Kernel kernel) {
                return configurationManager;
            }
        };
    }
    
}
