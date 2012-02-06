/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.tomcat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import javax.naming.directory.DirContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.RuntimeCustomizer;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ContextSource;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.ApplicationPolicyConfigurationManager;
import org.apache.geronimo.security.jacc.RunAsSource;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.stats.ModuleStats;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerFactory;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.tomcat.InstanceManager;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a WebApplicationContext that sets up its J2EE environment.
 *
 * @version $Rev$ $Date$
 */

@GBean(name = "Tomcat WebApplication Context",
        j2eeType = NameFactory.WEB_MODULE)
public class TomcatWebAppContext implements GBeanLifecycle, TomcatContext, WebModule, StatisticsProvider {

    private static final Logger log = LoggerFactory.getLogger(TomcatWebAppContext.class);
    public static final String GBEAN_REF_CLUSTERED_VALVE_RETRIEVER = "ClusteredValveRetriever";
    public static final String GBEAN_REF_MANAGER_RETRIEVER = "ManagerRetriever";

    protected final TomcatContainer container;
    private final ClassLoader classLoader;
    private final Bundle bundle;
    protected Context context = null;
    private String contextPath = null;
    private String docBase = null;
    private String virtualServer = null;
    private final Realm realm;
    private final List<Valve> valveChain;
    private final List<LifecycleListener> listenerChain;
    private final CatalinaCluster catalinaCluster;
    private final Manager manager;
    private final UserTransaction userTransaction;
    private final javax.naming.Context componentContext;
    private final Kernel kernel;
    private final Set<String> unshareableResources;
    private final Set<String> applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;
    private final SecurityHolder securityHolder;
    private final J2EEServer server;
    private Map<String, WebServiceContainer> webServices = null;
    private final String objectName;
    private final String originalSpecDD;
    private final String modulePath;
    private final Holder holder;
    private final RuntimeCustomizer contextCustomizer;
    private final Map<String, Object> deploymentAttributes;
    private final ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager;
    private final Map<String,String> contextAttributes;
    //private final WebBeansContext owbContext;
    private final InstanceManager instanceManager;
    private final AbstractName abName;

    // JSR 77
    private final String j2EEServer;
    private final String j2EEApplication;

    //  statistics
    private ModuleStats statsProvider;
    private boolean reset = true;

    private final Valve clusteredValve;
    private final WebAppInfo webAppInfo;

    private Map<String, AbstractName> webServiceAbNames;

    public TomcatWebAppContext(
            @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
            @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
            @ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
            @ParamAttribute(name = "contextPath") String contextPath,
            @ParamAttribute(name = "deploymentDescriptor") String originalSpecDD,
            @ParamAttribute(name = "modulePath") String modulePath,
            @ParamAttribute(name = "securityHolder") SecurityHolder securityHolder,
            @ParamAttribute(name = "virtualServer") String virtualServer,
            @ParamAttribute(name = "unshareableResources") Set<String> unshareableResources,
            @ParamAttribute(name = "applicationManagedSecurityResources") Set<String> applicationManagedSecurityResources,
            @ParamReference(name = "TransactionManager") TransactionManager transactionManager,
            @ParamReference(name = "TrackedConnectionAssociator") TrackedConnectionAssociator trackedConnectionAssociator,
            @ParamReference(name = "Container") TomcatContainer container,
            @ParamReference(name = "RunAsSource") RunAsSource runAsSource,
            @ParamReference(name = "ConfigurationFactory") ConfigurationFactory configurationFactory,
            @ParamReference(name = "TomcatRealm") ObjectRetriever tomcatRealm,
            @ParamReference(name = GBEAN_REF_CLUSTERED_VALVE_RETRIEVER) ObjectRetriever clusteredValveRetriever,
            @ParamReference(name = "TomcatValveChain") ValveGBean tomcatValveChain,
            @ParamReference(name = "LifecycleListenerChain") LifecycleListenerGBean lifecycleListenerChain,
            @ParamReference(name = "Cluster") CatalinaClusterGBean cluster,
            @ParamReference(name = GBEAN_REF_MANAGER_RETRIEVER) ObjectRetriever managerRetriever,
            @ParamAttribute(name = "webServices") Map<String, AbstractName> webServices,
            @ParamAttribute(name = "holder") Holder holder,
            @ParamReference(name = "ContextCustomizer") RuntimeCustomizer contextCustomizer,
            @ParamReference(name = "J2EEServer") J2EEServer server,
            @ParamReference(name = "ContextSource") ContextSource contextSource,
            @ParamReference(name = "applicationPolicyConfigurationManager") ApplicationPolicyConfigurationManager applicationPolicyConfigurationManager,
            @ParamAttribute(name = "deploymentAttributes") Map<String, Object> deploymentAttributes,
            @ParamAttribute(name = "webAppInfo") WebAppInfo webAppInfo,
            @ParamAttribute(name = "contextAttributes") Map<String, String> contextAttributes,
            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abName)
            throws Exception {
        assert classLoader != null;
        assert bundle != null;
        assert transactionManager != null;
        assert trackedConnectionAssociator != null;
        assert contextSource != null;
        assert container != null;

        if (null != clusteredValveRetriever) {
            clusteredValve = (Valve) clusteredValveRetriever.getInternalObject();
        } else {
            clusteredValve = null;
        }

        this.objectName = objectName;
        this.abName = abName;
        this.deploymentAttributes = deploymentAttributes;
        this.webAppInfo = webAppInfo;
        this.container = container;
        this.bundle = bundle;
        this.modulePath = modulePath;
        this.originalSpecDD = originalSpecDD;
        this.contextPath = contextPath;

        this.virtualServer = virtualServer;
        this.securityHolder = securityHolder;

        this.componentContext = contextSource.getContext();
        this.userTransaction = new GeronimoUserTransaction(transactionManager);

        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.applicationPolicyConfigurationManager = applicationPolicyConfigurationManager;

        this.contextAttributes = contextAttributes;

        this.server = server;
        if (securityHolder != null) {
            securityHolder.setRunAsSource(runAsSource == null ? RunAsSource.NULL : runAsSource);
            securityHolder.setConfigurationFactory(configurationFactory);
        }


        this.holder = holder == null ? new Holder() : holder;
        this.contextCustomizer = contextCustomizer;

        if (tomcatRealm != null) {
            realm = (Realm) tomcatRealm.getInternalObject();
            if (realm == null) {
                throw new IllegalArgumentException("tomcatRealm must be an instance of org.apache.catalina.Realm.");
            }
        } else {
            realm = null;
        }

        //Add the valve list
        if (tomcatValveChain != null) {
            ArrayList<Valve> chain = new ArrayList<Valve>();
            ValveGBean valveGBean = tomcatValveChain;
            while (valveGBean != null) {
                chain.add((Valve) valveGBean.getInternalObject());
                valveGBean = valveGBean.getNextValve();
            }
            valveChain = chain;
        } else {
            valveChain = null;
        }

        //Add the Lifecycle Listener list
        if (lifecycleListenerChain != null) {
            ArrayList<LifecycleListener> chain = new ArrayList<LifecycleListener>();
            LifecycleListenerGBean listenerGBean = lifecycleListenerChain;
            while (listenerGBean != null) {
                chain.add((LifecycleListener) listenerGBean.getInternalObject());
                listenerGBean = listenerGBean.getNextListener();
            }
            listenerChain = chain;
        } else {
            listenerChain = null;
        }

        //Add the cluster
        if (cluster != null) {
            catalinaCluster = (CatalinaCluster) cluster.getInternalObject();
        } else {
            catalinaCluster = null;
        }

        //Add the manager
        if (managerRetriever != null) {
            this.manager = (Manager) managerRetriever.getInternalObject();
        } else {
            this.manager = null;
        }

        this.webServiceAbNames = webServices;

        this.classLoader = classLoader;

        this.kernel = kernel;

        if (objectName != null) {
            ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
            verifyObjectName(myObjectName);
            j2EEServer = myObjectName.getKeyProperty(NameFactory.J2EE_SERVER);
            j2EEApplication = myObjectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
        } else {
            // StandardContext uses default value of these as "none"
            j2EEServer = null;
            j2EEApplication = null;
        }
        instanceManager = new TomcatInstanceManager(this.holder, classLoader, componentContext);
    }

    private Map<String, WebServiceContainer> createWebServices(Map<String, AbstractName> webServiceFactoryMap, Kernel kernel) throws Exception {
        if (webServiceFactoryMap == null) {
            return Collections.<String, WebServiceContainer>emptyMap();
        }
        Map<String, WebServiceContainer> webServices = new HashMap<String, WebServiceContainer>();
        for (Map.Entry<String, AbstractName> entry : webServiceFactoryMap.entrySet()) {
            String servletName = entry.getKey();
            AbstractName factoryName = entry.getValue();
            WebServiceContainerFactory webServiceContainerFactory = (WebServiceContainerFactory) kernel.getGBean(factoryName);
            WebServiceContainer webServiceContainer = webServiceContainerFactory.getWebServiceContainer();
            webServices.put(servletName, webServiceContainer);
        }
        return webServices;
    }

    public ApplicationPolicyConfigurationManager getApplicationPolicyConfigurationManager() {
        return applicationPolicyConfigurationManager;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getJ2EEApplication() {
        return j2EEApplication;
    }

    public String getJ2EEServer() {
        return j2EEServer;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return true;
    }

    public boolean isEventProvider() {
        return true;
    }

    public String getWARName() {
        //todo: make this return something more consistent
        try {
            return ObjectName.getInstance(objectName).getKeyProperty(NameFactory.J2EE_NAME);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public WebContainer getContainer() {
        return container;
    }

    public String getServer() {
        return server == null ? null : server.getObjectName();
    }

    public String getDocBase() {
        if (context != null) {
            return context.getDocBase();
        }
        return docBase;
    }

    public UserTransaction getUserTransaction() {
        return userTransaction;
    }

    public javax.naming.Context getJndiContext() {
        return componentContext;
    }

    public String getVirtualServer() {
        return virtualServer;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public boolean isDisableCookies() {
        String cookies = contextAttributes.get("cookies");
        return cookies == null ? false : !Boolean.parseBoolean(cookies);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getContextPath() {
        return contextPath;
    }

    public SecurityHolder getSecurityHolder() {
        return securityHolder;
    }


    public Set<String> getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return trackedConnectionAssociator;
    }

    public Set<String> getUnshareableResources() {
        return unshareableResources;
    }

    public Realm getRealm() {
        return realm;
    }

    public Valve getClusteredValve() {
        return clusteredValve;
    }

    public List<Valve> getValveChain() {
        return valveChain;
    }

    public List<LifecycleListener> getLifecycleListenerChain() {
        return listenerChain;
    }

    public CatalinaCluster getCluster() {
        return catalinaCluster;
    }

    public Manager getManager() {
        return manager;
    }

    public boolean isCrossContext() {
        String crossContext = contextAttributes.get("crossContext");
        return crossContext == null ? false : Boolean.parseBoolean(crossContext);
    }

    public String getWorkDir() {
        return contextAttributes.get("workDir");
    }

    public Map<String, WebServiceContainer> getWebServices() {
        if(webServices == null) {
            try {
                webServices = createWebServices(webServiceAbNames, kernel);
            } catch (Exception e) {
                throw new RuntimeException("Fail to initialize web service", e);
            }
        }
        return webServices;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getModulePath() {
        return modulePath;
    }

    public RuntimeCustomizer getRuntimeCustomizer() {
        return contextCustomizer;
    }

    public String[] getServlets() {
        String[] result = null;
        if ((context != null) && (context instanceof StandardContext)) {
            result = ((StandardContext) context).getServlets();
        }

        return result;
    }

    public Collection<String> getListeners() {
        return Collections.unmodifiableCollection(webAppInfo.listeners);
    }

    public String getDisplayName() {
        return webAppInfo.displayName;
    }

    public Object getDeploymentAttribute(String name) {
        return deploymentAttributes.get(name);
    }

    @Override
    public WebAppInfo getWebAppInfo() {
        return webAppInfo;
    }

    public AbstractName getAbstractName() {
        return abName;
    }

    /**
     * ObjectName must match this pattern: <p/>
     * domain:j2eeType=WebModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException(
                    "ObjectName can not be a pattern", objectName);
        }
        Hashtable<String, String> keyPropertyList = objectName.getKeyPropertyList();
        if (!NameFactory.WEB_MODULE.equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException(
                    "WebModule object name j2eeType property must be 'WebModule'",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_NAME)) {
            throw new InvalidObjectNameException(
                    "WebModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_SERVER)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEServer property",
                    objectName);
        }
        if (!keyPropertyList.containsKey(NameFactory.J2EE_APPLICATION)) {
            throw new InvalidObjectNameException(
                    "WebModule object name must contain a J2EEApplication property",
                    objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException(
                    "WebModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties",
                    objectName);
        }
    }

    public String[] getJavaVMs() {
        return server == null ? new String[0] : server.getJavaVMs();
    }

    public String getDeploymentDescriptor() {
        return originalSpecDD;
    }

    //  JSR 77 statistics - The static values are initialized at the time of
    // creration, getStats return fresh value everytime
    public Stats getStats() {
        if (reset) {
            reset = false;
            return statsProvider.getStats();
        } else return statsProvider.updateStats();
    }

    public void resetStats() {
        reset = true;
    }

    @Override
    public Map<String, String> getContextAttributes() {
        return contextAttributes;
    }

    public void doStart() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            // See the note of TomcatContainer::addContext
            container.addContext(this);
            // Is it necessary - doesn't Tomcat Embedded take care of it?
            // super.start();
            if (context instanceof StandardContext) {
                statsProvider = new ModuleStats((StandardContext) context);
            }
            if (log.isDebugEnabled()) {
                log.debug("TomcatWebAppContext started for " + contextPath);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void doStop() throws Exception {
        statsProvider = null;
        container.removeContext(this);
        log.debug("TomcatWebAppContext stopped");
    }

    public void doFail() {
        statsProvider = null;
        container.removeContext(this);
        log.warn("TomcatWebAppContext failed");
    }

}
