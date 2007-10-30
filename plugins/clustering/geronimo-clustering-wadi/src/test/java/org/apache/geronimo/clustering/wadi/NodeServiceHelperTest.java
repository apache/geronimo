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

import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.servicespace.InvocationMetaData;
import org.codehaus.wadi.servicespace.ServiceProxy;
import org.codehaus.wadi.servicespace.ServiceProxyFactory;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * 
 * @version $Rev$ $Date$
 */
public class NodeServiceHelperTest extends RMockTestCase {

    private ServiceSpace serviceSpace;
    private NodeServiceHelper serviceHelper;
    private NodeService nodeService;

    @Override
    protected void setUp() throws Exception {
        nodeService = (NodeService) mock(NodeServiceProxy.class);
        serviceSpace = (ServiceSpace) mock(ServiceSpace.class);
        serviceHelper = new NodeServiceHelper(serviceSpace);
    }
    
    public void testNodeServiceRegistration() throws Exception {
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        serviceRegistry.register(NodeService.SERVICE_NAME, nodeService);
        
        startVerification();
        
        serviceHelper.registerNodeService(nodeService);
    }
    
    public void testGetNodeServiceProxy() throws Exception {
        Peer peer = (Peer) mock(Peer.class);
        ServiceProxyFactory proxyFactory = 
            serviceSpace.getServiceProxyFactory(NodeService.SERVICE_NAME, new Class[] {NodeService.class});
        proxyFactory.getInvocationMetaData();

        InvocationMetaData invMetaData = (InvocationMetaData) intercept(InvocationMetaData.class, "InvocationMetaData");
        modify().returnValue(invMetaData);

        invMetaData.setTargets(new Peer[] {peer});
        
        proxyFactory.getProxy();
        modify().returnValue(nodeService);
        
        startVerification();
        
        assertSame(nodeService, serviceHelper.getNodeServiceProxy(peer));
    }
    
    public static interface NodeServiceProxy extends NodeService, ServiceProxy {
    }
    
}
