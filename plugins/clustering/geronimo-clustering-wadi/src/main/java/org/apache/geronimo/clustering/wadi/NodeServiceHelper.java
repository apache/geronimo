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

import org.codehaus.wadi.group.Dispatcher;
import org.codehaus.wadi.group.Peer;
import org.codehaus.wadi.servicespace.InvocationMetaData;
import org.codehaus.wadi.servicespace.ServiceAlreadyRegisteredException;
import org.codehaus.wadi.servicespace.ServiceProxyFactory;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;
import org.codehaus.wadi.servicespace.admin.AdminServiceSpace;
import org.codehaus.wadi.servicespace.admin.AdminServiceSpaceHelper;


/**
 *
 * @version $Rev$ $Date$
 */
public class NodeServiceHelper {
    private final ServiceSpace serviceSpace;
    
    public NodeServiceHelper(Dispatcher dispatcher) {
        if (null == dispatcher) {
            throw new IllegalArgumentException("dispatcher is required");
        }
        this.serviceSpace = getAdminServiceSpace(dispatcher);
    }

    public NodeServiceHelper(ServiceSpace serviceSpace) {
        if (null == serviceSpace) {
            throw new IllegalArgumentException("serviceSpace is required");
        }
        this.serviceSpace = serviceSpace;
    }

    public void registerNodeService(NodeService nodeService) {
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        try {
            serviceRegistry.register(NodeService.SERVICE_NAME, nodeService);
        } catch (ServiceAlreadyRegisteredException e) {
            throw new IllegalStateException("NodeService already registered.", e);
        }
    }

    public NodeService getNodeServiceProxy(Peer peer) {
        ServiceProxyFactory proxyFactory = serviceSpace.getServiceProxyFactory(NodeService.SERVICE_NAME,
            new Class[] { NodeService.class });
        InvocationMetaData invocationMetaData = proxyFactory.getInvocationMetaData();
        invocationMetaData.setTargets(new Peer[] {peer});
        return (NodeService) proxyFactory.getProxy();
    }

    protected ServiceSpace getAdminServiceSpace(Dispatcher dispatcher) {
        AdminServiceSpaceHelper helper = new AdminServiceSpaceHelper();
        AdminServiceSpace adminServiceSpace = helper.getAdminServiceSpace(dispatcher);
        return adminServiceSpace;
    }

}
