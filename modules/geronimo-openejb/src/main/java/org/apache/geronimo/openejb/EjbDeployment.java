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

import java.util.Set;
import java.lang.reflect.Method;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.management.EJB;
import org.apache.openejb.BeanType;
import org.apache.openejb.Container;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.entity.EntityEjbHomeHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public class EjbDeployment implements EJB {
    private final String objectName;
    private final String deploymentId;
    private final String ejbName;

    private final String homeInterfaceName;
    private final String remoteInterfaceName;
    private final String localHomeInterfaceName;
    private final String localInterfaceName;
    private final String serviceEndpointInterfaceName;
    private final String beanClassName;
    private final ClassLoader classLoader;

    private final Subject defaultSubject;
    private final Subject runAs;

    private final Context componentContext;

    // connector stuff
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    private final OpenEjbSystem openEjbSystem;

    private CoreDeploymentInfo deploymentInfo;

    public EjbDeployment(String objectName,
                         String deploymentId,
                         String ejbName,
                         String homeInterfaceName,
                         String remoteInterfaceName,
                         String localHomeInterfaceName,
                         String localInterfaceName,
                         String serviceEndpointInterfaceName,
                         String beanClassName,
                         ClassLoader classLoader,
                         Subject defaultSubject,
                         Subject runAs,
                         Context componentContext,
                         Set unshareableResources,
                         Set applicationManagedSecurityResources,
                         TrackedConnectionAssociator trackedConnectionAssociator,
                         OpenEjbSystem openEjbSystem) {
        this.objectName = objectName;
        this.deploymentId = deploymentId;
        this.ejbName = ejbName;
        this.homeInterfaceName = homeInterfaceName;
        this.remoteInterfaceName = remoteInterfaceName;
        this.localHomeInterfaceName = localHomeInterfaceName;
        this.localInterfaceName = localInterfaceName;
        this.serviceEndpointInterfaceName = serviceEndpointInterfaceName;
        this.beanClassName = beanClassName;
        this.classLoader = classLoader;
        this.defaultSubject = defaultSubject;
        this.runAs = runAs;
        this.componentContext = componentContext;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.openEjbSystem = openEjbSystem;
    }

    public CoreDeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getEjbName() {
        return ejbName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public String getServiceEndpointInterfaceName() {
        return serviceEndpointInterfaceName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public Context getComponentContext() {
        return deploymentInfo.getJndiEnc();
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public EJBHome getEJBHome() {
        return deploymentInfo.getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() {
        return deploymentInfo.getEJBLocalHome();
    }

    public Object getBusinessLocalHome() {
        return deploymentInfo.getBusinessLocalHome();
    }

    public Object getBusinessRemoteHome() {
        return deploymentInfo.getBusinessRemoteHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        // get the local interface
        Class localInterface = deploymentInfo.getLocalInterface();

        // get the container
        RpcContainer container = (RpcContainer) deploymentInfo.getContainer();

        // create a new ProxyInfo based on the deployment info and primary key
        ProxyInfo proxyInfo = new ProxyInfo(deploymentInfo, primaryKey, localInterface, container);

        // get the home proxy handler
        EJBHome homeProxy = deploymentInfo.getEJBHome();
        EntityEjbHomeHandler handler = (EntityEjbHomeHandler) ProxyManager.getInvocationHandler(homeProxy);

        // create the proxy
        EJBObject ejbObject = (EJBObject) handler.createProxy(proxyInfo);
        return ejbObject;
    }

    public Class getHomeInterface() {
        return deploymentInfo.getHomeInterface();
    }

    public Class getRemoteInterface() {
        return deploymentInfo.getRemoteInterface();
    }

    public Class getLocalHomeInterface() {
        return deploymentInfo.getLocalHomeInterface();
    }

    public Class getLocalInterface() {
        return deploymentInfo.getLocalInterface();
    }

    public Class getBeanClass() {
        return deploymentInfo.getBeanClass();
    }

    public Class getBusinessLocalInterface() {
        return deploymentInfo.getBusinessLocalInterface();
    }

    public Class getBusinessRemoteInterface() {
        return deploymentInfo.getBusinessRemoteInterface();
    }

    public Class getMdbInterface() {
        return deploymentInfo.getMdbInterface();
    }

    public BeanType getComponentType() {
        return deploymentInfo.getComponentType();
    }

    public Container getContainer() {
        return deploymentInfo.getContainer();
    }

    public boolean isBeanManagedTransaction() {
        return deploymentInfo.isBeanManagedTransaction();
    }

    public byte getTransactionAttribute(Method method) {
        return deploymentInfo.getTransactionAttribute(method);
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    protected void start() throws Exception {
        deploymentInfo = (CoreDeploymentInfo) openEjbSystem.getDeploymentInfo(deploymentId);
        if (deploymentInfo == null) {
            throw new IllegalStateException("Ejb does not exist " + deploymentId);
        }
        if (componentContext != null) {
            ((Context)deploymentInfo.getJndiEnc().lookup("java:comp")).bind("geronimo", componentContext);
        }
        deploymentInfo.set(EjbDeployment.class, this);
    }

    protected void stop() {
        if (deploymentInfo != null) {
            deploymentInfo.set(EjbDeployment.class, null);
            deploymentInfo = null;
        }
    }
}
