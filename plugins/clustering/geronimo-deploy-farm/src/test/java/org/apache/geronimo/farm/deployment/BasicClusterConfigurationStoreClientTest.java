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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.ExtendedJMXConnectorInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.deployment.plugin.remote.FileUploadClient;
import org.apache.geronimo.deployment.plugin.remote.FileUploadProgress;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicClusterConfigurationStoreClientTest extends RMockTestCase {

    private ClusterInfo clusterInfo;
    private NodeInfo node1Info;
    private Kernel node1Kernel;
    private ExtendedJMXConnectorInfo connectorInfo1;
    private NodeInfo node2Info;
    private Kernel node2Kernel;
    private ExtendedJMXConnectorInfo connectorInfo2;
    private DirectoryPackager packager;
    private BasicClusterConfigurationStoreClient client;
    private FileUploadClient fileUploadClient;
    private AbstractNameQuery clusterConfigurationStoreNameQuery;
    private Artifact configId;
    private ConfigurationData configurationData;
    private ConfigurationManager configurationManager;

    @Override
    protected void setUp() throws Exception {
        configId = new Artifact("groupId", "artifactId", "2.0", "car");
        configurationData = new ConfigurationData(new Environment(configId), new Jsr77Naming());
        File configurationDir = new File("configurationDir");
        configurationData.setConfigurationDir(configurationDir);
        configurationManager = (ConfigurationManager) mock(ConfigurationManager.class);
        clusterInfo = (ClusterInfo) mock(ClusterInfo.class);

        Collection<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        setUpNode1Local(nodeInfos);
        setUpNode2Remote(nodeInfos);
        
        clusterInfo.getNodeInfos();
        modify().multiplicity(expect.from(0)).returnValue(nodeInfos);
        
        packager = (DirectoryPackager) mock(DirectoryPackager.class);
        fileUploadClient = (FileUploadClient) mock(FileUploadClient.class);
        
        clusterConfigurationStoreNameQuery = new AbstractNameQuery("interfaceType");

        client = new BasicClusterConfigurationStoreClient(clusterConfigurationStoreNameQuery) {
            @Override
            protected DirectoryPackager newDirectoryPackager() {
                return packager;
            }
            @Override
            protected FileUploadClient newFileUploadClient() {
                return fileUploadClient;
            }
        };
    }

    private void setUpNode2Remote(Collection<NodeInfo> nodeInfos) throws IOException {
        node2Info = (NodeInfo) mock(NodeInfo.class, "NodeInfo2");
        nodeInfos.add(node2Info);
        node2Kernel = node2Info.newKernel();
        modify().multiplicity(expect.from(0));
        connectorInfo2 = node2Info.getConnectorInfo();
        setUpConnectorInfo(connectorInfo2, false);
    }

    private void setUpNode1Local(Collection<NodeInfo> nodeInfos) throws IOException {
        node1Info = (NodeInfo) mock(NodeInfo.class, "NodeInfo1");
        nodeInfos.add(node1Info);
        node1Kernel = node1Info.newKernel();
        modify().multiplicity(expect.from(0));
        connectorInfo1 = node1Info.getConnectorInfo();
        setUpConnectorInfo(connectorInfo1, true);
    }

    private void setUpConnectorInfo(ExtendedJMXConnectorInfo connectorInfo, boolean local) {
        modify().multiplicity(expect.from(0));
        connectorInfo.isLocal();
        modify().multiplicity(expect.from(0)).returnValue(local);
        connectorInfo.getUsername();
        modify().multiplicity(expect.from(0)).returnValue("username");
        connectorInfo.getPassword();
        modify().multiplicity(expect.from(0)).returnValue("password");
    }
    
    public void testInstallOK() throws Exception {
        packager.pack(configurationData.getConfigurationDir());
        File packedConfigurationDir = new File("packedConfigurationDir");
        modify().multiplicity(expect.exactly(2)).returnValue(packedConfigurationDir);
        
        File packedConfigurationDirRemote = updloadToNode2(packedConfigurationDir);
        
        recordInstall(node1Kernel, packedConfigurationDir);
        recordInstall(node2Kernel, packedConfigurationDirRemote);
        
        startVerification();
        
        client.install(clusterInfo, configurationData);   
    }

    public void testInstallFailsTriggersUninstall() throws Exception {
        packager.pack(configurationData.getConfigurationDir());
        File packedConfigurationDir = new File("packedConfigurationDir");
        modify().returnValue(packedConfigurationDir);
        
        recordInstall(node1Kernel, packedConfigurationDir);
        
        node2Kernel.listGBeans(clusterConfigurationStoreNameQuery);
        modify().returnValue(Collections.EMPTY_SET);
        
        recordUninstall(node1Kernel);

        startVerification();

        try {
            client.install(clusterInfo, configurationData);
            fail();
        } catch (IOException e) {
        }
    }

    public void testUploadConfigurationFailsWithMessage() throws Exception {
        packager.pack(configurationData.getConfigurationDir());
        
        URL remoteDeployURL = recordGetDeployURL();
        
        fileUploadClient.uploadFilesToServer(remoteDeployURL, "username", "password", null, null);
        modify().args(is.AS_RECORDED, is.AS_RECORDED, is.AS_RECORDED, is.NOT_NULL, new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                FileUploadProgress progress = (FileUploadProgress) arg;
                progress.fail("message");
                return true;
            }
        });
        
        startVerification();

        try {
            client.uploadConfiguration(node2Kernel, node2Info, configurationData);
            fail();
        } catch (IOException e) {
        }
    }

    public void testUploadConfigurationFailsWithException() throws Exception {
        packager.pack(configurationData.getConfigurationDir());
        
        URL remoteDeployURL = recordGetDeployURL();
        
        fileUploadClient.uploadFilesToServer(remoteDeployURL, "username", "password", null, null);
        modify().args(is.AS_RECORDED, is.AS_RECORDED, is.AS_RECORDED, is.NOT_NULL, new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                FileUploadProgress progress = (FileUploadProgress) arg;
                progress.fail(new Exception());
                return true;
            }
        });
        
        startVerification();

        try {
            client.uploadConfiguration(node2Kernel, node2Info, configurationData);
            fail();
        } catch (IOException e) {
        }
    }
    
    public void testUninstall() throws Exception {
        recordUninstall(node1Kernel);
        recordUninstall(node2Kernel);

        startVerification();

        client.uninstall(clusterInfo, configId);
    }
    
    private void recordUninstall(Kernel kernel) throws GBeanNotFoundException, NoSuchOperationException, Exception {
        AbstractName storeName = new AbstractName(configId, Collections.singletonMap("name", "Store"));
        Set<AbstractName> storeNames = Collections.singleton(storeName);
        kernel.getGBean(ConfigurationManager.class);
        modify().returnValue(configurationManager);
        configurationManager.stopConfiguration(configId);
        configurationManager.unloadConfiguration(configId);
        configurationManager.uninstallConfiguration(configId);
    }
    
    private void recordInstall(Kernel kernel, File packedDir) throws Exception {
        AbstractName storeName = new AbstractName(configId, Collections.singletonMap("name", "Store"));
        Set<AbstractName> storeNames = Collections.singleton(storeName);

        kernel.listGBeans(clusterConfigurationStoreNameQuery);
        modify().returnValue(storeNames);
        kernel.invoke(storeName, "install", new Object[] {configurationData, packedDir},
            new String[] {ConfigurationData.class.getName(), File.class.getName()});
    }

    private File updloadToNode2(final File packedConfigurationDir) throws MalformedURLException {
        URL remoteDeployURL = recordGetDeployURL();
        
        final File packedConfigurationDirRemote = new File("packedConfigurationDirRemote");
        fileUploadClient.uploadFilesToServer(remoteDeployURL, "username", "password", null, null);
        modify().args(is.AS_RECORDED, is.AS_RECORDED, is.AS_RECORDED, new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg) throws IOException {
            }

            public boolean passes(Object arg) {
                File[] files = (File[]) arg;
                assertSame(packedConfigurationDir, files[0]);
                files[0] = packedConfigurationDirRemote;
                return true;
            }
        },  is.NOT_NULL);
        return packedConfigurationDirRemote;
    }

    private URL recordGetDeployURL() throws MalformedURLException {
        fileUploadClient.getRemoteDeployUploadURL(node2Kernel);
        URL remoteDeployURL = new URL("http", "localhost", "file");
        modify().returnValue(remoteDeployURL);
        return remoteDeployURL;
    }

}
