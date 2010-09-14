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

package org.apache.geronimo.openejb.cluster.stateful.deployment;

import java.util.Map;
import java.util.Set;

import org.apache.geronimo.clustering.SessionManager;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.cluster.infra.SessionManagerTracker;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType = NameFactory.STATEFUL_SESSION_BEAN)
public class ClusteredStatefulDeployment extends EjbDeployment {

    private final SessionManager sessionManager;

    public ClusteredStatefulDeployment(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                         @ParamAttribute(name = "deploymentId") String deploymentId,
                         @ParamAttribute(name = "ejbName") String ejbName,
                         @ParamAttribute(name = "homeInterfaceName") String homeInterfaceName,
                         @ParamAttribute(name = "remoteInterfaceName") String remoteInterfaceName,
                         @ParamAttribute(name = "localHomeInterfaceName") String localHomeInterfaceName,
                         @ParamAttribute(name = "localInterfaceName") String localInterfaceName,
                         @ParamAttribute(name = "serviceEndpointInterfaceName") String serviceEndpointInterfaceName,
                         @ParamAttribute(name = "beanClassName") String beanClassName,
                         @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                         @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                         @ParamAttribute(name = "securityEnabled") boolean securityEnabled,
                         @ParamAttribute(name = "defaultRole") String defaultRole,
                         @ParamAttribute(name = "runAsRole") String runAsRole,
                         @ParamReference(name = "RunAsSource", namingType = SecurityNames.JACC_MANAGER) RunAsSource runAsSource,
                         @ParamReference(name = "ApplicationJndi", namingType = "GBEAN") ApplicationJndi applicationJndi,
                         @ParamAttribute(name = "moduleContextMap") Map<String, Object> moduleJndi,
                         @ParamAttribute(name = "componentContextMap") Map<String, Object> compContext,
                         @ParamAttribute(name = "unshareableResources") Set<String> unshareableResources,
                         @ParamAttribute(name = "applicationManagedSecurityResources") Set<String> applicationManagedSecurityResources,
                         @ParamReference(name = "OpenEjbSystem") TrackedConnectionAssociator trackedConnectionAssociator,
                         @ParamReference(name = "TransactionManager", namingType = NameFactory.JTA_RESOURCE) GeronimoTransactionManager transactionManager,
                         @ParamAttribute(name = "beanManagedTransactions") boolean beanManagedTransactions,
                         @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
                         @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                         @ParamReference(name = GBEAN_REF_SESSION_MANAGER) SessionManager sessionManager) throws Exception {
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
                bundle,
                securityEnabled,
                defaultRole,
                runAsRole,
                runAsSource,
                applicationJndi,
                moduleJndi,
                compContext,
                unshareableResources,
                applicationManagedSecurityResources,
                trackedConnectionAssociator,
                transactionManager,
                beanManagedTransactions,
                openEjbSystem,
                kernel);
        if (null == sessionManager) {
            throw new IllegalArgumentException("sessionManager is required");
        }
        this.sessionManager = sessionManager;
    }

    @Override
    protected EjbDeployment initialize(BeanContext deploymentInfo) {
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
        BeanContext info = deploymentInfo.get();
        if (null != info) {
            Container container = info.getContainer();
            if (null != container) {
                SessionManagerTracker sessionManagerTracker = (SessionManagerTracker) container;
                sessionManagerTracker.removeSessionManager(deploymentId, sessionManager);
            }
        }

        super.destroy();
    }

    public static final String GBEAN_REF_SESSION_MANAGER = "SessionManager";

}
