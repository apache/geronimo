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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.Container;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.xbean.naming.context.ImmutableFederatedContext;
import org.osgi.framework.Bundle;

public class EjbDeployment implements EJB, EjbDeploymentIdAccessor {
    private final String objectName;
    protected final String deploymentId;
    private final String ejbName;

    private final String homeInterfaceName;
    private final String remoteInterfaceName;
    private final String localHomeInterfaceName;
    private final String localInterfaceName;
    private final String serviceEndpointInterfaceName;
    private final String beanClassName;
    private final ClassLoader classLoader;
    private final Bundle bundle;
    private final boolean securityEnabled;
    private final Subject defaultSubject;
    private final Subject runAs;

    private final Context componentContext;
    private final Context moduleContext;
    private final Context applicationContext;
    private final Context globalContext;

    // connector stuff
    private final Set<String> unshareableResources;
    private final Set<String> applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    protected final OpenEjbSystem openEjbSystem;

    protected AtomicReference<BeanContext> deploymentInfo = new AtomicReference<BeanContext>();

    public EjbDeployment(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
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
                         @ParamReference(name = "TrackedConnectionAssociator") TrackedConnectionAssociator trackedConnectionAssociator,
                         @ParamReference(name = "TransactionManager", namingType = NameFactory.JTA_RESOURCE) GeronimoTransactionManager transactionManager,
                         @ParamAttribute(name = "beanManagedTransactions") boolean beanManagedTransactions,
                         @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
                         @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel) throws LoginException, NamingException {
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
        this.bundle = bundle;
        this.securityEnabled = securityEnabled;
        if (runAsSource == null) {
            runAsSource = RunAsSource.NULL;
        }
        this.defaultSubject = defaultRole == null ? runAsSource.getDefaultSubject() : runAsSource.getSubjectForRole(defaultRole);
        this.runAs = runAsSource.getSubjectForRole(runAsRole);
        this.componentContext = EnterpriseNamingContext.livenReferences(compContext, (beanManagedTransactions) ? transactionManager : null, kernel, classLoader, bundle, "comp/");
        this.moduleContext = EnterpriseNamingContext.livenReferences(moduleJndi, null, kernel, classLoader, bundle, "module/");
        this.applicationContext = applicationJndi.getApplicationContext();
        this.globalContext = applicationJndi.getGlobalContext();
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.openEjbSystem = openEjbSystem;
    }

    public BeanContext getDeploymentInfo() {
        return deploymentInfo.get();
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

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public Set<String> getUnshareableResources() {
        return unshareableResources;
    }

    public Set<String> getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public EJBHome getEJBHome() {
        return getDeploymentInfo().getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() {
        return getDeploymentInfo().getEJBLocalHome();
    }

    public Object getBusinessLocalHome() {
        return getDeploymentInfo().getBusinessLocalHome();
    }

    public Object getBusinessRemoteHome() {
        return getDeploymentInfo().getBusinessRemoteHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return (EJBObject) EjbObjectProxyHandler.createProxy(getDeploymentInfo(), primaryKey, InterfaceType.EJB_HOME, getHomeInterface());
    }

    public Class getHomeInterface() {
        return getDeploymentInfo().getHomeInterface();
    }

    public Class getRemoteInterface() {
        return getDeploymentInfo().getRemoteInterface();
    }

    public Class getLocalHomeInterface() {
        return getDeploymentInfo().getLocalHomeInterface();
    }

    public Class getLocalInterface() {
        return getDeploymentInfo().getLocalInterface();
    }

    public Class getBeanClass() {
        return getDeploymentInfo().getBeanClass();
    }

    public Class getBusinessLocalInterface() {
        return getDeploymentInfo().getBusinessLocalInterface();
    }

    public Class getBusinessRemoteInterface() {
        return getDeploymentInfo().getBusinessRemoteInterface();
    }

    public Class getMdbInterface() {
        return getDeploymentInfo().getMdbInterface();
    }

    public Class getServiceEndpointInterface() {
        return getDeploymentInfo().getServiceEndpointInterface();
    }

    public BeanType getComponentType() {
        return getDeploymentInfo().getComponentType();
    }

    public Container getContainer() {
        return getDeploymentInfo().getContainer();
    }

    public boolean isBeanManagedTransaction() {
        return getDeploymentInfo().isBeanManagedTransaction();
    }

    public TransactionType getTransactionType(Method method) {
        return getDeploymentInfo().getTransactionType(method);
    }
    
    public TransactionType getTransactionType(Method method, InterfaceType interfaceType) {
        return getDeploymentInfo().getTransactionType(method, interfaceType);
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

    protected EjbDeployment initialize(BeanContext deploymentInfo) {
        try {
            ImmutableFederatedContext federatedContext = (ImmutableFederatedContext) ((DeepBindableContext.ContextWrapper)deploymentInfo.getJndiEnc()).getRootContext();
            federatedContext.federateContext(componentContext);
            federatedContext.federateContext(moduleContext);
            federatedContext.federateContext(applicationContext);
            federatedContext.federateContext(globalContext);
            deploymentInfo.set(EjbDeployment.class, this);

            this.deploymentInfo.set(deploymentInfo);

            return this;
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to complete EjbDeployment initialization", e);
        }
    }

    protected void destroy() {
        BeanContext info = deploymentInfo.getAndSet(null);
        if (info != null) {
            info.set(EjbDeployment.class, null);
        }
    }
}
