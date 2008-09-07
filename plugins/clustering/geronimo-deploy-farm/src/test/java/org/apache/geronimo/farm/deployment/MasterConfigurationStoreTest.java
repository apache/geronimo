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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.WritableListableRepository;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class MasterConfigurationStoreTest extends RMockTestCase {

    private Kernel kernel;
    private WritableListableRepository repository;
    private ClusterInfo clusterInfo;
    private ClusterConfigurationStoreClient storeClient;
    private AbstractName clusterInfoName;
    private ConfigurationStore delegate;
    private SlaveConfigurationNameBuilder nameBuilder;
    private Artifact configId;

    @Override
    protected void setUp() throws Exception {
        kernel = (Kernel) mock(Kernel.class);
        repository = (WritableListableRepository) mock(WritableListableRepository.class);
        clusterInfo = (ClusterInfo) mock(ClusterInfo.class);
        storeClient = (ClusterConfigurationStoreClient) mock(ClusterConfigurationStoreClient.class);
        kernel.getAbstractNameFor(clusterInfo);
        configId = new Artifact("groupId", "artifactId", "2.0", "car");
        clusterInfoName = new AbstractName(configId, Collections.singletonMap("name", "ClusterInfo"));
        modify().returnValue(clusterInfoName);
        
        delegate = (ConfigurationStore) mock(ConfigurationStore.class);
        nameBuilder = (SlaveConfigurationNameBuilder) mock(SlaveConfigurationNameBuilder.class);
    }

    private MasterConfigurationStore newMasterConfigurationStore() {
        return new MasterConfigurationStore(kernel,
            "objectName",
                null,
            repository,
            new Environment(),
            clusterInfo,
            storeClient) {
            @Override
            protected ConfigurationStore newConfigurationStore(Kernel kernel,
                String objectName,
                AbstractName abstractName,
                WritableListableRepository repository) {
                return delegate;
            }
            @Override
            protected SlaveConfigurationNameBuilder newSlaveConfigurationNameBuilder() {
                return nameBuilder;
            }
        };
    }
    
    public void testContainsConfigurationOK() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);

        delegate.containsConfiguration(configId);
        modify().returnValue(true);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertTrue(store.containsConfiguration(configId));
    }
    
    public void testContainsConfigurationFailsWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);

        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertFalse(store.containsConfiguration(configId));
    }

    public void testDelegateCreateNewConfigurationDir() throws Exception {
        Artifact slaveId = new Artifact("groupId", "slaveId", "2.0", "car");
        nameBuilder.buildSlaveConfigurationName(configId);
        modify().returnValue(slaveId);
        
        delegate.createNewConfigurationDir(slaveId);
        File expectedFile = new File("confDir");
        modify().returnValue(expectedFile);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertSame(expectedFile, store.createNewConfigurationDir(configId));
    }
    
    public void testExportFailsWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);

        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        try {
            store.exportConfiguration(configId, null);
            fail();
        } catch (NoSuchConfigException e) {
        }
    }
    
    public void testDelegateExport() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        delegate.exportConfiguration(configId, out);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        store.exportConfiguration(configId, out);
    }

    public void testDelegateGetAbstractName() throws Exception {
        delegate.getAbstractName();
        modify().returnValue(clusterInfoName);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertSame(clusterInfoName, store.getAbstractName());
    }
    
    public void testDelegateGetObjectName() throws Exception {
        delegate.getObjectName();
        String expectedName = "name";
        modify().returnValue(expectedName);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertSame(expectedName, store.getObjectName());
    }
    
    public void testIsInPlaceConfigurationWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);

        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        try {
            store.isInPlaceConfiguration(configId);
            fail();
        } catch (NoSuchConfigException e) {
        }
    }

    public void testIsInPlaceConfigurationReturnsFalse() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        assertFalse(store.isInPlaceConfiguration(configId));
    }
    
    public void testListConfigurationFilterNoneMasterConfigIds() throws Exception {
        List<ConfigurationInfo> configurationInfos = new ArrayList<ConfigurationInfo>();

        ConfigurationInfo configurationInfo =  newConfigurationInfo(configId);
        configurationInfos.add(configurationInfo);
        
        Artifact configId2 = new Artifact("groupId", "artifactId2", "2.0", "car");
        ConfigurationInfo configurationInfo2 =  newConfigurationInfo(configId2);
        configurationInfos.add(configurationInfo2);
        
        delegate.listConfigurations();
        modify().returnValue(configurationInfos);
        
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        nameBuilder.isSlaveConfigurationName(configId2);
        modify().returnValue(true);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        List<ConfigurationInfo> listedConfigurations = store.listConfigurations();
        assertEquals(1, listedConfigurations.size());
        assertTrue(listedConfigurations.contains(configurationInfo));
    }

    public void testLoadConfigurationWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);

        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        try {
            store.loadConfiguration(configId);
            fail();
        } catch (NoSuchConfigException e) {
        }
    }

    public void testDelegateLoadConfiguration() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        delegate.loadConfiguration(configId);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        store.loadConfiguration(configId);
    }

    public void testResolveWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        try {
            store.resolve(configId, null, null);
            fail();
        } catch (NoSuchConfigException e) {
        }
    }
    
    public void testDelegateResolve() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        delegate.resolve(configId, null, null);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        store.resolve(configId, null, null);
    }
    
    public void testUninstall() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(false);
        
        nameBuilder.buildSlaveConfigurationName(configId);
        Artifact slaveId = new Artifact("groupId", "slaveId", "2.0", "car");
        modify().returnValue(slaveId);

        storeClient.uninstall(clusterInfo, slaveId);
        
        delegate.uninstall(slaveId);
        delegate.uninstall(configId);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        store.uninstall(configId);
    }
    
    public void testUninstallWhenNotMasterConfigId() throws Exception {
        nameBuilder.isSlaveConfigurationName(configId);
        modify().returnValue(true);
        
        startVerification();
        
        MasterConfigurationStore store = newMasterConfigurationStore();
        try {
            store.uninstall(configId);
            fail();
        } catch (NoSuchConfigException e) {
        }
    }
    
    public void testInstallOK() throws Exception {
        ConfigurationData configurationData = new ConfigurationData(ConfigurationModuleType.CAR,
            new LinkedHashSet(),
            new ArrayList(),
            Collections.EMPTY_MAP,
            new Environment(configId),
            new File("configurationDir"),
            null,
            new Jsr77Naming());
        
        final Artifact slaveId = new Artifact("groupId", "slaveId", "2.0", "car");
        nameBuilder.buildSlaveConfigurationName(configId);
        modify().returnValue(slaveId);
        
        storeClient.install(clusterInfo, configurationData);
        modify().args(is.AS_RECORDED, new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                ConfigurationData configurationData = (ConfigurationData) arg;
                assertSame(slaveId, configurationData.getId());
                return true;
            }
        });
        
        delegate.install(configurationData);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                ConfigurationData configurationData = (ConfigurationData) arg;
                assertSame(slaveId, configurationData.getId());
                return true;
            }
        });
        
        NodeInfo nodeInfo = (NodeInfo) mock(NodeInfo.class);
        nodeInfo.getName();
        final String nodeName = "nodeName";
        modify().multiplicity(expect.from(0)).returnValue(nodeName);
        clusterInfo.getNodeInfos();
        modify().returnValue(Collections.singleton(nodeInfo));
        
        delegate.createNewConfigurationDir(configId);
        final File masterDir = new File("masterDir");
        modify().returnValue(masterDir);
        
        delegate.install(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                ConfigurationData configurationData = (ConfigurationData) arg;
                assertSame(configId, configurationData.getId());
                assertSame(masterDir, configurationData.getConfigurationDir());
                
                List<GBeanData> gbeans;
                try {
                    gbeans = configurationData.getGBeans(getClass().getClassLoader());
                } catch (InvalidConfigException e) {
                    throw new AssertionFailedError();
                }
                assertEquals(1, gbeans.size());
                GBeanData gbean = gbeans.get(0);
                assertEquals(BasicClusterConfigurationController.class.getName(), gbean.getGBeanInfo().getClassName());
                assertEquals(slaveId, gbean.getAttribute(BasicClusterConfigurationController.GBEAN_ATTR_ARTIFACT));
                assertEquals(nodeName, gbean.getAttribute(BasicClusterConfigurationController.GBEAN_ATTR_NODE_NAME));
                return true;
            }
        });
        
        startVerification();

        MasterConfigurationStore store = newMasterConfigurationStore();
        store.install(configurationData);
    }
    
    private ConfigurationInfo newConfigurationInfo(Artifact configId) {
        return new ConfigurationInfo(
            clusterInfoName, 
            configId,
            ConfigurationModuleType.CAR,
            1l, 
            Collections.EMPTY_SET, 
            Collections.EMPTY_SET,
            null);
    }
    
    
}
