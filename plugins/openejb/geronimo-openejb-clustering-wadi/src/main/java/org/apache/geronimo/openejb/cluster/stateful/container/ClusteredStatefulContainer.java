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

package org.apache.geronimo.openejb.cluster.stateful.container;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTracker;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTrackerException;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.openejb.ClusteredRPCContainer;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.stateful.StatefulContainer;
import org.apache.openejb.spi.SecurityService;
import org.codehaus.wadi.core.contextualiser.BasicInvocation;
import org.codehaus.wadi.core.contextualiser.InvocationContext;
import org.codehaus.wadi.core.contextualiser.InvocationException;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;

/**
 *
 * @version $Rev:$ $Date:$
 */
// todo Consider replacing this class with an RpcContainerWrapper subclass
public class ClusteredStatefulContainer extends StatefulContainer implements SessionManagerTracker, ClusteredRPCContainer {
    private final Map<Object, Manager> deploymentIdToManager;
    private final Map<Object, NetworkConnectorTracker> deploymentIdToNetworkConnectorTracker;

    public ClusteredStatefulContainer(Object id, SecurityService securityService) {
        super(id, securityService, new WadiCache());

        deploymentIdToManager = new HashMap<Object, Manager>();
        deploymentIdToNetworkConnectorTracker = new HashMap<Object, NetworkConnectorTracker>();
    }

    public void addSessionManager(Object deploymentId, SessionManager sessionManager) {
        ((WadiCache) cache).addSessionManager(deploymentId, sessionManager);
        
        WADISessionManager wadiSessionManager = (WADISessionManager) sessionManager;
        
        Manager manager = wadiSessionManager.getManager();
        synchronized (deploymentIdToManager) {
            deploymentIdToManager.put(deploymentId, manager);
        }

        ServiceSpace serviceSpace = wadiSessionManager.getServiceSpace();
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        NetworkConnectorTracker networkConnectorTracker;
        try {
            networkConnectorTracker = (NetworkConnectorTracker) serviceRegistry.getStartedService(NetworkConnectorTracker.NAME);
        } catch (Exception e) {
            throw new IllegalStateException("Should never occur" ,e);
        }
        synchronized (deploymentIdToNetworkConnectorTracker) {
            deploymentIdToNetworkConnectorTracker.put(deploymentId, networkConnectorTracker);   
        }
    }

    public void removeSessionManager(Object deploymentId, SessionManager sessionManager) {
        ((WadiCache) cache).removeSessionManager(deploymentId, sessionManager);
        
        synchronized (deploymentIdToManager) {
            deploymentIdToManager.remove(deploymentId);
        }
        synchronized (deploymentIdToNetworkConnectorTracker) {
            deploymentIdToNetworkConnectorTracker.remove(deploymentId);
        }
    }
    
    public URI[] getLocations(DeploymentInfo deploymentInfo) {
        Object deploymentID = deploymentInfo.getDeploymentID();
        NetworkConnectorTracker networkConnectorTracker;
        synchronized (deploymentIdToNetworkConnectorTracker) {
            networkConnectorTracker = deploymentIdToNetworkConnectorTracker.get(deploymentID);
        }
        if (null == networkConnectorTracker) {
            return null;
        }

        Set<URI> connectorURIs;
        try {
            connectorURIs = networkConnectorTracker.getConnectorURIs(deploymentID);
        } catch (NetworkConnectorTrackerException e) {
            return null;
        }
        return connectorURIs.toArray(new URI[connectorURIs.size()]);
    }

    @Override
    protected Object businessMethod(CoreDeploymentInfo deploymentInfo,
        Object primKey,
        Class callInterface,
        Method callMethod,
        Object[] args) throws OpenEJBException {
        AbstractEJBInvocation invocation = new BusinessMethodInvocation(primKey.toString(),
            5000,
            deploymentInfo,
            primKey,
            callInterface,
            callMethod,
            args);
        return invoke(deploymentInfo, invocation);
    }

    protected Object superBusinessMethod(CoreDeploymentInfo deploymentInfo,
        Object primKey,
        Class callInterface,
        Method callMethod,
        Object[] args) throws OpenEJBException {
        return super.businessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
    }
    
    @Override
    protected Object removeEJBObject(CoreDeploymentInfo deploymentInfo,
        Object primKey,
        Class callInterface,
        Method callMethod,
        Object[] args) throws OpenEJBException {
        AbstractEJBInvocation invocation = new RemoveEJBObjectInvocation(primKey.toString(),
            5000,
            deploymentInfo,
            primKey,
            callInterface,
            callMethod,
            args);
        return invoke(deploymentInfo, invocation);
    }

    protected Object superRemoveEJBObject(CoreDeploymentInfo deploymentInfo,
        Object primKey,
        Class callInterface,
        Method callMethod,
        Object[] args) throws OpenEJBException {
        return super.removeEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
    }

    protected Object invoke(CoreDeploymentInfo deploymentInfo, AbstractEJBInvocation invocation) throws OpenEJBException {
        Manager manager;
        synchronized (deploymentIdToManager) {
            manager = deploymentIdToManager.get(deploymentInfo.getDeploymentID());
        }
        if (null == manager) {
            throw new OpenEJBException("No manager registered for [" + deploymentInfo + "]");
        }
        try {
            manager.contextualise(invocation);
        } catch (InvocationException e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof OpenEJBException) {
                throw (OpenEJBException) throwable;
            } else {
                throw new OpenEJBException(e);
            }
        }
        return invocation.getResult();
    }
    
    protected abstract class AbstractEJBInvocation extends BasicInvocation {
        protected final CoreDeploymentInfo deploymentInfo;
        protected final Object primKey;
        protected final Class callInterface;
        protected final Method callMethod;
        protected final Object[] args;
        protected Object result;

        protected AbstractEJBInvocation(String sessionKey,
            long exclusiveSessionLockWaitTime,
            CoreDeploymentInfo deploymentInfo,
            Object primKey,
            Class callInterface,
            Method callMethod,
            Object[] args) {
            super(sessionKey, exclusiveSessionLockWaitTime);
            this.deploymentInfo = deploymentInfo;
            this.primKey = primKey;
            this.callMethod = callMethod;
            this.callInterface = callInterface;
            this.args = args;
        }

        public Object getResult() {
            return result;
        }

        @Override
        protected void doInvoke() throws InvocationException {
            invokeEJBMethod();
        }
        
        @Override
        protected void doInvoke(InvocationContext context) throws InvocationException {
            invokeEJBMethod();
        }

        protected abstract void invokeEJBMethod() throws InvocationException;
    }
    
    protected class BusinessMethodInvocation extends AbstractEJBInvocation {
        
        protected BusinessMethodInvocation(String sessionKey,
            long exclusiveSessionLockWaitTime,
            CoreDeploymentInfo deploymentInfo,
            Object primKey,
            Class callInterface,
            Method callMethod,
            Object[] args) {
            super(sessionKey, exclusiveSessionLockWaitTime, deploymentInfo, primKey, callInterface, callMethod, args);
        }
    
        protected void invokeEJBMethod() throws InvocationException {
            try {
                result = superBusinessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            } catch (OpenEJBException e) {
                throw new InvocationException(e);
            }
        }
    }
    
    protected class RemoveEJBObjectInvocation extends AbstractEJBInvocation {

        protected RemoveEJBObjectInvocation(String sessionKey,
            long exclusiveSessionLockWaitTime,
            CoreDeploymentInfo deploymentInfo,
            Object primKey,
            Class callInterface,
            Method callMethod,
            Object[] args) {
            super(sessionKey, exclusiveSessionLockWaitTime, deploymentInfo, primKey, callInterface, callMethod, args);
        }
        
        protected void invokeEJBMethod() throws InvocationException {
            try {
                result = superRemoveEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            } catch (OpenEJBException e) {
                throw new InvocationException(e);
            }
        }
        
    }

}
