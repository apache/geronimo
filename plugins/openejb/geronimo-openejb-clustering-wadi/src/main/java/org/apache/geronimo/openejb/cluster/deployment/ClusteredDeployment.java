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

package org.apache.geronimo.openejb.cluster.deployment;

import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.EjbDeploymentGBean;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.Container;
import org.apache.openejb.core.CoreDeploymentInfo;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClusteredDeployment extends EjbDeployment {

    private final SessionManager sessionManager;

    public ClusteredDeployment() throws LoginException {
        sessionManager = null;
    }

    public ClusteredDeployment(String objectName,
        String deploymentId,
        String ejbName,
        String homeInterfaceName,
        String remoteInterfaceName,
        String localHomeInterfaceName,
        String localInterfaceName,
        String serviceEndpointInterfaceName,
        String beanClassName,
        ClassLoader classLoader,
        boolean securityEnabled,
        String defaultRole,
        String runAsRole,
        RunAsSource runAsSource,
        Map componentContext,
        Set unshareableResources,
        Set applicationManagedSecurityResources,
        TrackedConnectionAssociator trackedConnectionAssociator,
        GeronimoTransactionManager transactionManager,
        OpenEjbSystem openEjbSystem,
        SessionManager sessionManager,
        Kernel kernel) throws Exception {
        this(objectName,
            deploymentId,
            ejbName,
            homeInterfaceName,
            remoteInterfaceName,
            localHomeInterfaceName,
            localInterfaceName,
            serviceEndpointInterfaceName,
            beanClassName,
            classLoader,
            securityEnabled,
            defaultRole,
            runAsRole,
            runAsSource,
            EnterpriseNamingContext.createEnterpriseNamingContext(componentContext,
                transactionManager,
                kernel,
                classLoader),
            unshareableResources,
            applicationManagedSecurityResources,
            trackedConnectionAssociator,
            openEjbSystem,
            sessionManager);
    }
    
    public ClusteredDeployment(String objectName,
        String deploymentId,
        String ejbName,
        String homeInterfaceName,
        String remoteInterfaceName,
        String localHomeInterfaceName,
        String localInterfaceName,
        String serviceEndpointInterfaceName,
        String beanClassName,
        ClassLoader classLoader,
        boolean securityEnabled,
        String defaultRole,
        String runAsRole,
        RunAsSource runAsSource,
        Context componentContext,
        Set unshareableResources,
        Set applicationManagedSecurityResources,
        TrackedConnectionAssociator trackedConnectionAssociator,
        OpenEjbSystem openEjbSystem,
        SessionManager sessionManager) throws LoginException {
        super(objectName,
            deploymentId,
            ejbName,
            homeInterfaceName,
            remoteInterfaceName,
            localHomeInterfaceName,
            localInterfaceName,
            serviceEndpointInterfaceName,
            beanClassName,
            classLoader,
            securityEnabled,
            defaultRole,
            runAsRole,
            runAsSource,
            componentContext,
            unshareableResources,
            applicationManagedSecurityResources,
            trackedConnectionAssociator,
            openEjbSystem);
        if (null == sessionManager) {
            throw new IllegalArgumentException("sessionManager is required");
        }
        this.sessionManager = sessionManager;
    }

    @Override
    protected EjbDeployment initialize(CoreDeploymentInfo deploymentInfo) {
        super.initialize(deploymentInfo);

        Container container = deploymentInfo.getContainer();
        if (null == container) {
            throw new IllegalStateException("Container not assigned to deployment " + deploymentId);
        }
        if (!(container instanceof SessionManagerTracker)) {
            throw new IllegalStateException("Container for deployment [" + deploymentId + "] is not a ["
                    + SessionManagerTracker.class.getName() + "]. It is a [" + container.getClass().getName() + "]");
        }
        SessionManagerTracker sessionManagerTracker = (SessionManagerTracker) container;
        sessionManagerTracker.addSessionManager(deploymentId, sessionManager);

        return this;
    }

    @Override
    protected void destroy() {
        CoreDeploymentInfo info = deploymentInfo.get();
        if (null != info) {
            Container container = info.getContainer();
            if (null != container) {
                SessionManagerTracker sessionManagerTracker = (SessionManagerTracker) container;
                sessionManagerTracker.removeSessionManager(deploymentId, sessionManager);
            }
        }

        super.destroy();
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_REF_SESSION_MANAGER = "SessionManager";

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(ClusteredDeployment.class,
            ClusteredDeployment.class,
            EjbDeploymentGBean.GBEAN_INFO,
            NameFactory.STATEFUL_SESSION_BEAN);
        
        builder.addReference(GBEAN_REF_SESSION_MANAGER, SessionManager.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        
        builder.setConstructor(new String[] { "objectName",
            "deploymentId",
            "ejbName",

            "homeInterfaceName",
            "remoteInterfaceName",
            "localHomeInterfaceName",
            "localInterfaceName",
            "serviceEndpointInterfaceName",
            "beanClassName",
            "classLoader",

            "securityEnabled",
            "defaultRole",
            "runAsRole",
            "RunAsSource",

            "componentContextMap",

            "unshareableResources",
            "applicationManagedSecurityResources",
            "TrackedConnectionAssociator",
            "TransactionManager",

            "OpenEjbSystem",
            GBEAN_REF_SESSION_MANAGER,

            "kernel"
        });
        
        GBEAN_INFO = builder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
