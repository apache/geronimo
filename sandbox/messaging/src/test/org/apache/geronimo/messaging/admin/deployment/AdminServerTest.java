/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.messaging.admin.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/27 14:45:58 $
 */
public class AdminServerTest
    extends TestCase
{

    private MockNode node;
    private AdminServer server;
    private NodeInfo nodeInfo1;
    private MockServer server1;
    private TargetModuleID[] runningID1;
    private TargetModuleID[] nonRunningID1;
    private NodeInfo nodeInfo2;
    private MockServer server2;
    private TargetModuleID[] nonRunningID2;
    private TargetModuleID[] availableID2;
    
    protected void setUp() throws Exception {
        node = new MockNode();
        Set nodes = node.getMockGetRemoteNodeInfos();
        InetAddress address = InetAddress.getLocalHost();
        nodeInfo1 = new NodeInfo("node1", address, 1234);
        nodes.add(nodeInfo1);
        nodeInfo2 = new NodeInfo("node2", address, 1234);
        nodes.add(nodeInfo2);
        
        Map factories = node.getMockFactoryEndPointProxy();
        server1 = new MockServer(1);
        
        runningID1 = new TargetModuleID[] {new MockID()};
        nonRunningID1 = new TargetModuleID[] {new MockID()};
        server1.setMockGetRunningModules(runningID1);
        server1.setMockGetNonRunningModules(nonRunningID1);
        factories.put(nodeInfo1, server1);
        
        server2 = new MockServer(2);
        nonRunningID2 = new TargetModuleID[] {new MockID()};
        availableID2 = new TargetModuleID[] {new MockID()};
        server2.setMockGetNonRunningModules(nonRunningID2);
        server2.setMockGetAvailableModules(availableID2);
        
        factories.put(nodeInfo2, server2);
        
        server = new AdminServer(node, "DUMMY");
    }
    
    public void testGetTargets() throws Exception {
        Target[] targets = server.getTargets();
        assertEquals(2, targets.length);
        Collection names = new ArrayList();
        names.add(nodeInfo1.getName());
        names.add(nodeInfo2.getName());
        for (int i = 0; i < targets.length; i++) {
            assertTrue(names.contains(targets[i].getName()));
        }
    }
    
    public void testGetRunningModules() throws Exception {
        Target[] targets = server.getTargets();
        TargetModuleID[] ids = server.getRunningModules(ModuleType.WAR, targets);
        assertEquals(runningID1.length, ids.length);
    }

    public void testGetNonRunningModules() throws Exception {
        Target[] targets = server.getTargets();
        TargetModuleID[] ids = server.getNonRunningModules(ModuleType.WAR, targets);
        assertEquals(nonRunningID1.length + nonRunningID2.length, ids.length);
    }

    public void testGetAvailableModules() throws Exception {
        Target[] targets = server.getTargets();
        TargetModuleID[] ids = server.getAvailableModules(ModuleType.WAR, targets);
        assertEquals(availableID2.length, ids.length);
    }

    public void testDistribute() throws Exception {
        Target[] targets = server.getTargets();
        for (int i = 0; i < targets.length; i++) {
            if ( targets[i].getName().equals(nodeInfo1.getName()) ) {
                server.distribute(new Target[] {targets[i]}, new MockBuilder(), null, null);
            }
        }
        assertTrue(server1.getMockIsDistributed());
        assertFalse(server2.getMockIsDistributed());
    }

    public void testStart() throws Exception {
        Target[] targets = server.getTargets();
        for (int i = 0; i < targets.length; i++) {
            if ( targets[i].getName().equals(nodeInfo2.getName()) ) {
                MockID mockID = new MockID();
                mockID.target = targets[i]; 
                server.start(new TargetModuleID[] {mockID});
            }
        }
        assertFalse(server1.getMockIsStarted());
        assertTrue(server2.getMockIsStarted());
    }

    public void testStop() throws Exception {
        Target[] targets = server.getTargets();
        for (int i = 0; i < targets.length; i++) {
            if ( targets[i].getName().equals(nodeInfo2.getName()) ) {
                MockID mockID = new MockID();
                mockID.target = targets[i]; 
                server.stop(new TargetModuleID[] {mockID});
            }
        }
        assertFalse(server1.getMockIsStopped());
        assertTrue(server2.getMockIsStopped());
    }

    public void testUndeploy() throws Exception {
        Target[] targets = server.getTargets();
        for (int i = 0; i < targets.length; i++) {
            if ( targets[i].getName().equals(nodeInfo2.getName()) ) {
                MockID mockID = new MockID();
                mockID.target = targets[i]; 
                server.undeploy(new TargetModuleID[] {mockID});
            }
        }
        assertFalse(server1.getMockIsUndeploy());
        assertTrue(server2.getMockIsUndeploy());
    }
    
    private static class MockID implements TargetModuleID {
        private Target target;
        public Target getTarget() {
            return target;
        }
        public String getModuleID() {
            return null;
        }
        public String getWebURL() {
            return null;
        }
        public TargetModuleID getParentTargetModuleID() {
            return null;
        }
        public TargetModuleID[] getChildTargetModuleID() {
            return null;
        }
    }

    private static class MockBuilder implements ConfigurationBuilder {
        public SchemaTypeLoader[] getTypeLoaders() {
            return null;
        }
        public boolean canConfigure(XmlObject plan) {
            return false;
        }
        public XmlObject getDeploymentPlan(URL module) throws XmlException {
            return null;
        }
        public void buildConfiguration(File outfile, Manifest manifest, File module, XmlObject plan) throws IOException, DeploymentException {
        }
        public void buildConfiguration(File outfile, Manifest manifest, InputStream module, XmlObject plan) throws IOException, DeploymentException {
        }
    }
    
}
