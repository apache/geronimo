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
import java.util.Collections;

import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.openejb.cluster.infra.NetworkConnectorTracker;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.spi.SecurityService;
import org.codehaus.wadi.core.contextualiser.Invocation;
import org.codehaus.wadi.core.contextualiser.InvocationException;
import org.codehaus.wadi.core.manager.Manager;
import org.codehaus.wadi.servicespace.ServiceRegistry;
import org.codehaus.wadi.servicespace.ServiceSpace;

import com.agical.rmock.core.Action;
import com.agical.rmock.core.MethodHandle;
import com.agical.rmock.extension.junit.RMockTestCase;

/**
 * @version $Rev$ $Date$
 */
public class ClusteredStatefulContainerTest extends RMockTestCase {

    private WADISessionManager sessionManager;
    private ClusteredStatefulContainer container;
    private CoreDeploymentInfo deploymentInfo;
    private String deploymentId;
    private Method callMethod;
    private Manager wadiManager;
    private NetworkConnectorTracker tracker;
    private String primKey;
    private Class<Runnable> callInterface;
    private Object[] args;

    @Override
    protected void setUp() throws Exception {
        primKey = "primKey";
        callInterface = Runnable.class;
        callMethod = Runnable.class.getDeclaredMethod("run");
        args = new Object[0];
        
        sessionManager = (WADISessionManager) mock(WADISessionManager.class);
        wadiManager = sessionManager.getManager();
        modify().multiplicity(expect.from(0));
        
        sessionManager.registerListener(null);
        modify().multiplicity(expect.from(0)).args(is.NOT_NULL);
        
        ServiceSpace serviceSpace = sessionManager.getServiceSpace();
        modify().multiplicity(expect.from(0));
        ServiceRegistry serviceRegistry = serviceSpace.getServiceRegistry();
        modify().multiplicity(expect.from(0));
        serviceRegistry.getStartedService(NetworkConnectorTracker.NAME);
        modify().multiplicity(expect.from(0));
        tracker = (NetworkConnectorTracker) mock(NetworkConnectorTracker.class);
        modify().returnValue(tracker);

        SecurityService securityService = (SecurityService) mock(SecurityService.class);
        container = (ClusteredStatefulContainer) intercept(ClusteredStatefulContainer.class, new Object[] {"id",
                securityService});
        deploymentId = "deploymentId";
        deploymentInfo = new CoreDeploymentInfo(new DeploymentContext(deploymentId, null, null),
            SFSB.class,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null) {
            @Override
            public Object getDeploymentID() {
                return deploymentId;
            }
        };
    }
    
    public void testInvokeBusinessMethodForUnknownDeploymentThrowsOEJBE() throws Exception {
        startVerification();
        try {
            container.businessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            fail();
        } catch (OpenEJBException e) {
        }
    }
    
    public void testInvokeRemoteMethodForUnknownDeploymentThrowsOEJBE() throws Exception {
        startVerification();
        try {
            container.removeEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            fail();
        } catch (OpenEJBException e) {
        }
    }
    
    public void testOEJBIsRethrownForBusinessMethod() throws Exception {
        new OEJBIsRethrownTest() {
            @Override
            protected void executeMethod() throws OpenEJBException {
                container.businessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            }
            @Override
            protected void executeSuperMethod() throws OpenEJBException {
                container.superBusinessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            }
        }.executeTest();
    }
    
    public void testOEJBIsRethrownForRemoveMethod() throws Exception {
        new OEJBIsRethrownTest() {
            @Override
            protected void executeMethod() throws OpenEJBException {
                container.removeEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            }
            @Override
            protected void executeSuperMethod() throws OpenEJBException {
                container.superRemoveEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            }
        }.executeTest();
    }
    
    protected abstract class OEJBIsRethrownTest {
        public void executeTest() throws Exception {
            recordContextualiseInvocation();
            
            executeSuperMethod();
            OpenEJBException exception = new OpenEJBException();
            modify().throwException(exception);
            
            startVerification();
            
            container.addSessionManager(deploymentId, sessionManager);

            try {
                executeMethod();
                fail();
            } catch (OpenEJBException e) {
                assertSame(exception, e);
            }
        }
        
        protected abstract void executeSuperMethod() throws OpenEJBException;
        
        protected abstract void executeMethod() throws OpenEJBException;
    }
    
    public void testBusinessMethodInvokationOK() throws Exception {
        new MethodIsExecutedTest() {
            @Override
            protected Object executeMethod() throws OpenEJBException {
                return container.businessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            }
            @Override
            protected void executeSuperMethod() throws OpenEJBException {
                container.superBusinessMethod(deploymentInfo, primKey, callInterface, callMethod, args);
            }
        }.executeTest();
    }

    public void testRemoveMethodInvokationOK() throws Exception {
        new MethodIsExecutedTest() {
            @Override
            protected Object executeMethod() throws OpenEJBException {
                return container.removeEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            }
            @Override
            protected void executeSuperMethod() throws OpenEJBException {
                container.superRemoveEJBObject(deploymentInfo, primKey, callInterface, callMethod, args);
            }
        }.executeTest();
    }

    protected abstract class MethodIsExecutedTest {
        public void executeTest() throws Exception {
            recordContextualiseInvocation();
            
            executeSuperMethod();
            Object result = new Object();
            modify().returnValue(result);
            
            startVerification();
            
            container.addSessionManager(deploymentId, sessionManager);
            
            Object actualResult = executeMethod();
            assertSame(result, actualResult);
        }
        
        protected abstract void executeSuperMethod() throws OpenEJBException;
        
        protected abstract Object executeMethod() throws OpenEJBException;
    }
    
    public void testGetLocationsRetunsNullWhenDeploymentIsNotRegistered() throws Exception {
        startVerification();
        
        assertNull(container.getLocations(deploymentInfo));
    }
    
    public void testGetLocationsOK() throws Exception {
        tracker.getConnectorURIs(deploymentId);
        URI location = new URI("ejbd://host:1");
        modify().returnValue(Collections.singleton(location));
        
        startVerification();
        
        container.addSessionManager(deploymentId, sessionManager);
        
        URI[] actualLocations = container.getLocations(deploymentInfo);
        assertEquals(1, actualLocations.length);
        assertSame(location, actualLocations[0]);
    }
    
    public static class SFSB implements Runnable {
        public void run() {
        }
    }
    
    protected void recordContextualiseInvocation() throws InvocationException {
        wadiManager.contextualise(null);
        modify().args(is.NOT_NULL).perform(new Action() {
            public Object invocation(Object[] arg0, MethodHandle arg1) throws Throwable {
                ((Invocation) arg0[0]).invoke();
                return true;
            }
        });
    }
    
}
