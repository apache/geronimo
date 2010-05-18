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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

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
                              boolean securityEnabled,
                              String defaultRole,
                              String runAsRole,
                              RunAsSource runAsSource,
                              Map<String, Object> componentContext,
                              Set<String> unshareableResources,
                              Set<String> applicationManagedSecurityResources,
                              TrackedConnectionAssociator trackedConnectionAssociator,
                              GeronimoTransactionManager transactionManager,
                              OpenEjbSystem openEjbSystem,
                              EjbModuleImpl ejbModule, Kernel kernel) throws Exception {
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
                buildJndiContext(componentContext, ejbModule, transactionManager, kernel, classLoader),
                unshareableResources,
                applicationManagedSecurityResources,
                trackedConnectionAssociator,
                openEjbSystem);
    }

    private static Context buildJndiContext(Map<String, Object> componentContext, EjbModuleImpl ejbModule, GeronimoTransactionManager transactionManager, Kernel kernel, ClassLoader classLoader) throws NamingException {
        Context compContext = EnterpriseNamingContext.livenReferences(componentContext, transactionManager, kernel, classLoader, "comp/");
        Set<Context> contexts = new LinkedHashSet<Context>(4);
        contexts.add(compContext);
        contexts.add(ejbModule.getModuleContext());
        contexts.add(ejbModule.getApplicationJndi().getApplicationContext());
        contexts.add(ejbModule.getApplicationJndi().getGlobalContext());
        return EnterpriseNamingContext.createEnterpriseNamingContext(contexts);
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
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EjbDeploymentGBean.class, EjbDeploymentGBean.class, NameFactory.STATELESS_SESSION_BEAN);
        //TODO GERONIMO-5322 simple way to get all the ejbs to start before any servlet gbeans
        infoBuilder.setPriority(3);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("deploymentId", String.class, true);
        infoBuilder.addAttribute("ejbName", String.class, true);

        infoBuilder.addAttribute("homeInterfaceName", String.class, true);
        infoBuilder.addAttribute("remoteInterfaceName", String.class, true);
        infoBuilder.addAttribute("localHomeInterfaceName", String.class, true);
        infoBuilder.addAttribute("localInterfaceName", String.class, true);
        infoBuilder.addAttribute("serviceEndpointInterfaceName", String.class, true);
        infoBuilder.addAttribute("beanClassName", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addAttribute("securityEnabled", boolean.class, true);
        infoBuilder.addAttribute("defaultRole", String.class, true);
        infoBuilder.addAttribute("runAsRole", String.class, true);
        infoBuilder.addReference("RunAsSource", RunAsSource.class, SecurityNames.JACC_MANAGER);

        infoBuilder.addAttribute("componentContextMap", Map.class, true);

        infoBuilder.addAttribute("unshareableResources", Set.class, true);
        infoBuilder.addAttribute("applicationManagedSecurityResources", Set.class, true);
        infoBuilder.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class);
        infoBuilder.addReference("TransactionManager", GeronimoTransactionManager.class);

        infoBuilder.addReference("OpenEjbSystem", OpenEjbSystem.class);
        infoBuilder.addReference("EjbModule", EjbModuleImpl.class);

        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{
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
                "EjbModule",

                "kernel",
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
}
