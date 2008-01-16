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

import java.io.File;
import java.io.IOException;

import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicClusterConfigurationStoreTest extends RMockTestCase {

    private ConfigurationStore actualConfigurationStore;
    private DirectoryPackager directoryPackager;
    private BasicClusterConfigurationStore store;
    private Artifact configId;

    @Override
    protected void setUp() throws Exception {
        actualConfigurationStore = (ConfigurationStore) mock(ConfigurationStore.class);
        directoryPackager = (DirectoryPackager) mock(DirectoryPackager.class);
        store = new BasicClusterConfigurationStore(actualConfigurationStore) {
            @Override
            protected DirectoryPackager newDirectoryPackager() {
                return directoryPackager;
            }
            @Override
            protected void deleteDir(File packedConfigurationDir) {
            }
        };
        configId = new Artifact("groupId", "artifactId", "2.0", "car");
    }
    
    public void testInstall() throws Exception {
        ConfigurationData configurationData = new ConfigurationData(new Environment(configId), new Jsr77Naming());
        File packedConfigurationDir = new File("packedConfigurationDir");
        final File configurationDir = new File("configurationDir");
        
        actualConfigurationStore.createNewConfigurationDir(configId);
        modify().returnValue(configurationDir);

        directoryPackager.unpack(configurationDir, packedConfigurationDir);
        modify().returnValue(configurationDir);
        
        actualConfigurationStore.install(configurationData);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                ConfigurationData configurationData = (ConfigurationData) arg;
                assertSame(configurationDir, configurationData.getConfigurationDir());
                return true;
            }
        });
        
        startVerification();
        
        store.install(configurationData, packedConfigurationDir);
    }
    
    public void testUninstall() throws Exception {
        actualConfigurationStore.uninstall(configId);
        
        startVerification();
        
        store.uninstall(configId);
    }
    
}
