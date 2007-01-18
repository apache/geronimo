/**
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

import java.io.IOException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.alt.config.ClientModule;
import org.apache.openejb.alt.config.ConfigurationFactory;
import org.apache.openejb.alt.config.EjbModule;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.spi.ContainerSystem;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbSystemGBean implements OpenEjbSystem {
    private final ConfigurationFactory configurationFactory;
    private final Assembler assembler;

    public OpenEjbSystemGBean(TransactionManager transactionManager) throws Exception {
        System.setProperty("duct tape","");
        if (transactionManager == null) {
            throw new NullPointerException("transactionManager is null");
        }

        boolean offline = true;
        configurationFactory = new ConfigurationFactory(offline);
        assembler = new Assembler();

        TransactionServiceInfo transactionServiceInfo = new TransactionServiceInfo();
        PassthroughFactory.add(transactionServiceInfo, transactionManager);
        try {
            transactionServiceInfo.id = "Default Transaction Manager";
            transactionServiceInfo.serviceType = "TransactionManager";
            assembler.createTransactionManager(transactionServiceInfo);
        } finally {
            PassthroughFactory.remove(transactionServiceInfo);
        }
        GeronimoThreadContextListener.init();
    }

    public ContainerSystem getContainerSystem() {
        return assembler.getContainerSystem();
    }

    public Container createContainer(Class<? extends ContainerInfo> type, String serviceId, Properties declaredProperties, String providerId) throws OpenEJBException {
        ContainerInfo containerInfo = configurationFactory.configureService(type, serviceId, declaredProperties, providerId, "Container");
        assembler.createContainer(containerInfo);
        Container container = assembler.getContainerSystem().getContainer(serviceId);
        return container;
    }

    public ClientInfo configureApplication(ClientModule clientModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(clientModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(clientModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public EjbJarInfo configureApplication(EjbModule ejbModule) throws OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ejbModule.getClassLoader());
        try {
            return configurationFactory.configureApplication(ejbModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void createClient(ClientInfo clientInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            assembler.createClient(clientInfo, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void createEjbJar(EjbJarInfo ejbJarInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            assembler.createEjbJar(ejbJarInfo, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public DeploymentInfo getDeploymentInfo(String deploymentId) {
        return getContainerSystem().getDeploymentInfo(deploymentId);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbSystemGBean.class);
        infoBuilder.addReference("TransactionManager", TransactionManager.class);
        infoBuilder.setConstructor(new String[] {
                "TransactionManager",
        });
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
