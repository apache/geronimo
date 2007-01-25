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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.util.proxy.Jdk13ProxyFactory;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.dynamic.PassthroughFactory;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.ApplicationServer;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbSystemGBean implements OpenEjbSystem {
    private final ConfigurationFactory configurationFactory;
    private final Assembler assembler;

    public OpenEjbSystemGBean(TransactionManager transactionManager) throws Exception {
        this(transactionManager, null);
    }
    public OpenEjbSystemGBean(TransactionManager transactionManager, Kernel kernel) throws Exception {
        System.setProperty("duct tape","");
        System.setProperty("openejb.naming", "xbean");
        if (transactionManager == null) {
            throw new NullPointerException("transactionManager is null");
        }

        boolean offline = true;
        configurationFactory = new ConfigurationFactory(offline);
        assembler = new Assembler();

        // install application server
        ApplicationServer applicationServer = new ServerFederation();
        SystemInstance.get().setComponent(ApplicationServer.class, applicationServer);

        // install transaction manager
        transactionManager = getRawService(kernel, transactionManager);
        TransactionServiceInfo transactionServiceInfo = new TransactionServiceInfo();
        PassthroughFactory.add(transactionServiceInfo, transactionManager);
        try {
            transactionServiceInfo.id = "Default Transaction Manager";
            transactionServiceInfo.serviceType = "TransactionManager";
            assembler.createTransactionManager(transactionServiceInfo);
        } finally {
            PassthroughFactory.remove(transactionServiceInfo);
        }

        // install security service
        SecurityServiceInfo securityServiceInfo = configurationFactory.configureService(SecurityServiceInfo.class);
        assembler.createSecurityService(securityServiceInfo);

        // install proxy factory
        ProxyFactoryInfo proxyFactoryInfo = new ProxyFactoryInfo();
        proxyFactoryInfo.id = "Default JDK 1.3 ProxyFactory";
        proxyFactoryInfo.serviceType = "ProxyFactory";
        proxyFactoryInfo.className = Jdk13ProxyFactory.class.getName();
        proxyFactoryInfo.properties = new Properties();
        assembler.createProxyFactory(proxyFactoryInfo);

        // add our thread context listener
        GeronimoThreadContextListener.init();
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T getRawService(Kernel kernel, T proxy) {
        if (kernel == null) return proxy;

        AbstractName abstractName = kernel.getAbstractNameFor(proxy);
        if (abstractName == null) return proxy;

        try {
            Object service = kernel.getGBean(abstractName);
            return (T) service;
        } catch (GBeanNotFoundException e) {
        }

        return proxy;
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
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.setConstructor(new String[] {
                "TransactionManager",
                "kernel",
        });
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
