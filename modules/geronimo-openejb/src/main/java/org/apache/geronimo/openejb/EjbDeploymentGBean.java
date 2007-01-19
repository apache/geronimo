/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.kernel.Kernel;

public class EjbDeploymentGBean extends EjbDeployment implements GBeanLifecycle {
    public EjbDeploymentGBean(String objectName,
            String deploymentId,
            String ejbName,
            String homeInterfaceName,
            String remoteInterfaceName,
            String localHomeInterfaceName,
            String localInterfaceName,
            String serviceEndpointInterfaceName,
            String beanClassName,
            ClassLoader classLoader,
            DefaultPrincipal defaultPrincipal,
            Subject runAs,
            Map componentContext,
            Set unshareableResources,
            Set applicationManagedSecurityResources,
            TrackedConnectionAssociator trackedConnectionAssociator,
            GeronimoTransactionManager transactionManager,
            OpenEjbSystem openEjbSystem,
            Kernel kernel) throws Exception {
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
                generateDefaultSubject(defaultPrincipal, classLoader),
                runAs,
                EnterpriseNamingContext.createEnterpriseNamingContext(componentContext, transactionManager, kernel, classLoader),
                unshareableResources,
                applicationManagedSecurityResources,
                trackedConnectionAssociator,
                openEjbSystem);
    }

    private static Subject generateDefaultSubject(DefaultPrincipal defaultPrincipal, ClassLoader classLoader) throws DeploymentException {
        if (defaultPrincipal != null) {
            return ConfigurationUtil.generateDefaultSubject(defaultPrincipal, classLoader);
        } else {
            return null;
        }
    }

    public void doStart() throws Exception {
        start();
    }

    public void doStop() throws Exception {
        stop();
    }

    public void doFail() {
        stop();
    }

    // do not use this gbean info, instead use StatelessDeploymentGBean, StatefulDeploymentGBean, EntityDeploymentGBean, or MessageDrivenDeploymentGBean
    static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EjbDeploymentGBean.class, EjbDeploymentGBean.class, NameFactory.STATELESS_SESSION_BEAN);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("deploymentId", String.class, true);
        infoFactory.addAttribute("ejbName", String.class, true);

        infoFactory.addAttribute("homeInterfaceName", String.class, true);
        infoFactory.addAttribute("remoteInterfaceName", String.class, true);
        infoFactory.addAttribute("localHomeInterfaceName", String.class, true);
        infoFactory.addAttribute("localInterfaceName", String.class, true);
        infoFactory.addAttribute("serviceEndpointInterfaceName", String.class, true);
        infoFactory.addAttribute("beanClassName", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);
        infoFactory.addAttribute("runAs", Subject.class, true);

        infoFactory.addAttribute("componentContextMap", Map.class, true);

        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);
        infoFactory.addReference("TransactionManager", GeronimoTransactionManager.class);

        infoFactory.addReference("OpenEjbSystem", OpenEjbSystem.class);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "deploymentId",
                "ejbName",

                "homeInterfaceName",
                "remoteInterfaceName",
                "localHomeInterfaceName",
                "localInterfaceName",
                "serviceEndpointInterfaceName",
                "beanClassName",
                "classLoader",

                "defaultPrincipal",
                "runAs",

                "componentContextMap",

                "unshareableResources",
                "applicationManagedSecurityResources",
                "TrackedConnectionAssociator",
                "TransactionManager",

                "OpenEjbSystem",

                "kernel",
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
